import com.zwj.mqtt.server.back.codec.back.BackHeader;
import com.zwj.mqtt.server.back.codec.back.BackMessage;
import com.zwj.mqtt.server.back.codec.back.BackMessageDecoder;
import com.zwj.mqtt.server.back.codec.back.BackMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.concurrent.GenericFutureListener;
import java.nio.charset.StandardCharsets;

/**
 * @Author: zwj
 * @Date: 2019-12-12 17:53
 */
public class BackClient {

    public static void main(String[] args) {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap client = new Bootstrap();
        client.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY,true).handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024 * 1024*10, 5, 4));
                ch.pipeline().addLast("decoder", new BackMessageDecoder());
                ch.pipeline().addLast("encoder", new BackMessageEncoder());
                ch.pipeline().addLast(new clienthandler());
            }
        });
        try {
            String body = "hi,zwj";
            ChannelFuture f =  client.connect("127.0.0.1",9089).sync();
            BackMessage b =  new BackMessage();
            BackHeader h = new BackHeader();
            h.setCrcCode(0xbacf0101);
            h.setType((byte) 1);
            h.setLength(body.getBytes(StandardCharsets.UTF_8).length);
            b.setBody(body);
            System.out.println(h);
            b.setBackHeader(h);
                 f.channel().writeAndFlush(b).addListener((GenericFutureListener) future -> {
                     if(future.isSuccess()){
                         System.out.println("sucess");
                     }
                 });
          f.channel().writeAndFlush(b).addListener((GenericFutureListener) future -> {
              if(future.isSuccess()){
                  System.out.println("sucess");
              }
          });
          f.channel().writeAndFlush(b).addListener((GenericFutureListener) future -> {
              if(future.isSuccess()){
                  System.out.println("sucess");
              }
          });

        } catch (InterruptedException e) {
            group.shutdownGracefully();
        }
    }


}
