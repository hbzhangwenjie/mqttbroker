
package com.zwj.mqtt.server.back;

import com.zwj.mqtt.config.BrokerProperties;
import com.zwj.mqtt.server.back.codec.BackServerHandler;
import com.zwj.mqtt.server.back.codec.back.BackMessageDecoder;
import com.zwj.mqtt.server.back.codec.back.BackMessageEncoder;
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
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @Author: zwj
 * @Date: 2019-12-11 16:22
 */

@Component
@Slf4j
@SuppressWarnings("all")
public class BackServer implements ApplicationListener<ApplicationReadyEvent> {

    private static final int DEFAULT_BACK_BOSS_THREAD = 1;

    private static final int DEFAULT_BACK_WORKER_THREAD = Runtime.getRuntime().availableProcessors() * 8;

    @Autowired
    private BrokerProperties brokerProperties;
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        Integer brokerId = brokerProperties.getBrokerId();
        if (brokerId == null) {
            throw new RuntimeException("hub.mqtt.id 必须配置！");
        }
        String host = brokerProperties.getHost();
        int port = brokerProperties.getBackPort();
        log.info("starting hubmqtt backServer: {} ", brokerId);

        Boolean useEpoll = brokerProperties.getUseEpoll();
        Boolean sslEnable = brokerProperties.getSslEnable();
        int backBoss = brokerProperties.getBackBossThread() == null ? DEFAULT_BACK_BOSS_THREAD : brokerProperties.getBackBossThread();
        int backWork = brokerProperties.getBackWorkThread() == null ? DEFAULT_BACK_WORKER_THREAD : brokerProperties.getBackWorkThread();
        EventLoopGroup bossGroup = useEpoll ? new EpollEventLoopGroup(backBoss, new DefaultThreadFactory("backBoss", true))
                : new NioEventLoopGroup(backBoss,
                        new DefaultThreadFactory("backBoss", true));
        EventLoopGroup workerGroup = useEpoll ? new EpollEventLoopGroup(backWork, new DefaultThreadFactory("backWork", true))
                : new NioEventLoopGroup(backWork,
                        new DefaultThreadFactory("backWork", true));
        try {
            ServerBootstrap backServer = new ServerBootstrap();
            backServer.group(bossGroup, workerGroup);
            backServer.channel(useEpoll ? EpollServerSocketChannel.class : NioServerSocketChannel.class);
            backServer.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    ChannelPipeline channelPipeline = socketChannel.pipeline();
                    // Netty心跳检测
                    channelPipeline.addFirst("idle", new IdleStateHandler(brokerProperties.getReaderIdleTimeSeconds(),
                            brokerProperties.getWriterIdleTimeSeconds(),
                            brokerProperties.getAllIdleTimeSeconds()));
                    channelPipeline.addLast(new LengthFieldBasedFrameDecoder(1024 * 1024, 5, 4, 0, 0));
                    channelPipeline.addLast("decoder", new BackMessageDecoder());
                    channelPipeline.addLast("encoder", new BackMessageEncoder());
                    channelPipeline.addLast(new BackServerHandler());
                }
            });
            //todo
            backServer.childOption(ChannelOption.TCP_NODELAY, Boolean.TRUE)
                    .childOption(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

            ChannelFuture channelFuture = backServer.bind(host, port).sync();
            log.info("started hubmqtt backServer: {} ,host:{}, port: {}", brokerId, host, port);
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException("backServer bind failed", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}

