package com.zwj.mqtt.server.back.codec;

import com.zwj.mqtt.server.back.codec.back.BackMessage;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @Author: zwj
 * @Date: 2019-12-12 17:07
 */
@Sharable
public class BackServerHandler extends SimpleChannelInboundHandler<BackMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BackMessage msg) throws Exception {
        //原样返回
        ctx.writeAndFlush(msg);
        System.out.println("ssss:"+msg);
    }
}
