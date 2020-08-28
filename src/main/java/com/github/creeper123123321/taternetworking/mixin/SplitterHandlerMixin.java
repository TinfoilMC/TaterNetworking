package com.github.creeper123123321.taternetworking.mixin;

import com.github.creeper123123321.taternetworking.handlers.FrameVarIntDecoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.SplitterHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(SplitterHandler.class)
public class SplitterHandlerMixin {
	// Based on https://github.com/VelocityPowered/Velocity/blob/dev/1.1.0/proxy/src/main/java/com/velocitypowered/proxy/protocol/netty/MinecraftVarintFrameDecoder.java
	// which is licensed under MIT, Copyright 2018 Velocity team
	private FrameVarIntDecoder reader = new FrameVarIntDecoder();
	private Exception VARINT_BIG_CACHED = new DecoderException("Varint prefix is too big!");
	private Exception BAD_LENGTH_CACHED = new DecoderException("Varint prefix is invalid!");

	@Inject(method = "decode", at = @At("HEAD"), cancellable = true)
	private void onDecode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out, CallbackInfo ci) throws Exception {
		ci.cancel();

		if (!ctx.channel().isActive()) {
			in.clear();
			return;
		}

		reader.reset();

		int varintEnd = in.forEachByte(reader);
		if (varintEnd == -1) {
			// We tried to go beyond the end of the buffer. This is probably a good sign that the
			// buffer was too short to hold a proper varint.
			return;
		}

		if (reader.result == FrameVarIntDecoder.DecodeResult.SUCCESS) {
			if (reader.readVarint < 0) {
				throw BAD_LENGTH_CACHED;
			} else if (reader.readVarint == 0) {
				// skip over the empty packet and ignore it
				in.readerIndex(varintEnd + 1);
			} else {
				int minimumRead = reader.bytesRead + reader.readVarint;
				if (in.isReadable(minimumRead)) {
					out.add(in.retainedSlice(varintEnd + 1, reader.readVarint));
					in.skipBytes(minimumRead);
				}
			}
		} else if (reader.result == FrameVarIntDecoder.DecodeResult.TOO_BIG) {
			throw VARINT_BIG_CACHED;
		}
	}
}
