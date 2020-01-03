package com.zwj.mqtt.server;

import com.zwj.mqtt.config.BrokerProperties;
import com.zwj.mqtt.security.CustomSslcontextBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.ssl.ClientAuth;
import io.netty.util.concurrent.DefaultThreadFactory;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @Author: zwj
 * @Date: 2019-11-18 11:19
 */
@Component
@Slf4j
public class MqttServer implements ApplicationListener<ApplicationReadyEvent> {

    private static final int BOSS_THREAD = 1;

    private static final int WORKER_THREAD = Runtime.getRuntime().availableProcessors() * 8;

    @Resource
    private MqttServerHandler mqttServerHandler;

    @Autowired
    private BrokerProperties brokerProperties;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        Integer brokerId = brokerProperties.getBrokerId();
        if (brokerId == null) {
            throw new RuntimeException("hub.mqtt.id 必须配置！");
        }
        String host = brokerProperties.getHost();
        int port = brokerProperties.getPort();
        log.info("starting mqtt broker: {} ", brokerId);

        Boolean useEpoll = brokerProperties.getUseEpoll();
        Boolean sslEnable = brokerProperties.getSslEnable();

        EventLoopGroup bossGroup = useEpoll ? new EpollEventLoopGroup(BOSS_THREAD, new DefaultThreadFactory("boss", true))
                : new NioEventLoopGroup(BOSS_THREAD,
                        new DefaultThreadFactory("boss", true));
        EventLoopGroup workerGroup = useEpoll ? new EpollEventLoopGroup(WORKER_THREAD, new DefaultThreadFactory("worker", true))
                : new NioEventLoopGroup(WORKER_THREAD,
                        new DefaultThreadFactory("worker", true));
        try {
            ServerBootstrap server = new ServerBootstrap();
            server.group(bossGroup, workerGroup);
            server.channel(useEpoll ? EpollServerSocketChannel.class : NioServerSocketChannel.class);
            server.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    ChannelPipeline channelPipeline = socketChannel.pipeline();
                    // Netty提供的SSL处理
                    if (sslEnable) {
                        channelPipeline.addLast(CustomSslcontextBuilder.buildServer(ClientAuth.REQUIRE).newHandler(socketChannel.alloc()));
                    }
                    channelPipeline.addLast("decoder", new MqttDecoder());
                    channelPipeline.addLast("encoder", MqttEncoder.INSTANCE);
                    channelPipeline.addLast("broker", mqttServerHandler);
                }
            });
            //todo
            server.childOption(ChannelOption.TCP_NODELAY, Boolean.TRUE)
                    .childOption(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

            ChannelFuture channelFuture = server.bind(host, port).sync();
            log.info("started mqtt broker: {} ,host:{}, port: {}", brokerId, host, port);
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException("mqtt bind failed", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
