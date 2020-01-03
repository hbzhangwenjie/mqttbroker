import com.zwj.mqtt.server.back.codec.back.BackMessage;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @Author: zwj
 * @Date: 2019-12-12 18:36
 */

@Sharable
public class clienthandler extends SimpleChannelInboundHandler<BackMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BackMessage msg) throws Exception {
        //原样返回
        System.out.println("shoudao :"+msg);
    }
}
