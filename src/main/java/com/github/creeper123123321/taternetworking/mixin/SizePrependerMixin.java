package com.github.creeper123123321.taternetworking.mixin;

import com.github.creeper123123321.taternetworking.handlers.TaterFramer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.minecraft.network.SizePrepender;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SizePrepender.class)
public abstract class SizePrependerMixin extends MessageToByteEncoder<ByteBuf> {
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) {
		ctx.pipeline().replace("prepender", "prepender", TaterFramer.INSTANCE);
	}
}
