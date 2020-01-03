package com.zwj.mqtt.server.back.codec.back;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: zwj
 * @Date: 2019-12-12 15:48
 */
@Sharable
@Slf4j
public class BackMessageEncoder extends MessageToByteEncoder<BackMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, BackMessage msg, ByteBuf out) throws Exception {
        out.writeInt(msg.getBackHeader().getCrcCode());
        out.writeByte(msg.getBackHeader().getType());
        out.writeInt(msg.getBackHeader().getLength());
        out.writeBytes(msg.getBody().getBytes(StandardCharsets.UTF_8));
    }
}
