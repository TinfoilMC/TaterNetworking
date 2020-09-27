package com.github.creeper123123321.taternetworking.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import net.minecraft.network.PacketByteBuf;

import java.util.List;

@ChannelHandler.Sharable
public class TaterFramer extends MessageToMessageEncoder<ByteBuf> {
	public static final TaterFramer INSTANCE = new TaterFramer();

	@Override
	protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
		ByteBuf prefix = ctx.alloc().ioBuffer();
		try {
			new PacketByteBuf(prefix).writeVarInt(msg.readableBytes());
			out.add(prefix.retain());
			out.add(msg.retain());
		} finally {
			prefix.release();
		}
	}
}
