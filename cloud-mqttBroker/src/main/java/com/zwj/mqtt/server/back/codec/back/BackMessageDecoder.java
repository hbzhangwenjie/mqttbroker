package com.zwj.mqtt.server.back.codec.back;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: zwj
 * @Date: 2019-12-12 15:18
 */
@Sharable
@Slf4j
public class BackMessageDecoder extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        BackMessage backMessage = new BackMessage();
        BackHeader backHeader = new BackHeader();
        backHeader.setCrcCode(msg.readInt());
        backHeader.setType(msg.readByte());
        backHeader.setLength(msg.readInt());
        byte[] body = new byte[backHeader.getLength()];
        msg.readBytes(body);
        backMessage.setBody(new String(body, StandardCharsets.UTF_8));
        backMessage.setBackHeader(backHeader);
        ctx.fireChannelRead(backMessage);
    }
}
