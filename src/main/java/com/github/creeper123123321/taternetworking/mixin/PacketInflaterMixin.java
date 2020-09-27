package com.github.creeper123123321.taternetworking.mixin;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.PacketInflater;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

@Mixin(PacketInflater.class)
public class PacketInflaterMixin {
	@Shadow
	private int compressionThreshold;
	@Shadow
	@Final
	private Inflater inflater;

	@Inject(at = @At("HEAD"), method = "decode", cancellable = true)
	private void onDecode(ChannelHandlerContext ctx, ByteBuf in, List<Object> list, CallbackInfo ci) throws DataFormatException {
		ci.cancel();

		if (in.isReadable()) {
			PacketByteBuf packetByteBuf = new PacketByteBuf(in);
			int informedSize = packetByteBuf.readVarInt();
			if (informedSize == 0) {
				list.add(in.retain());
			} else {
				if (informedSize < this.compressionThreshold) {
					throw new DecoderException("Badly compressed packet - size of " + informedSize + " is below server threshold of " + this.compressionThreshold);
				}

				if (informedSize > 2097152) {
					throw new DecoderException("Badly compressed packet - size of " + informedSize + " is larger than protocol maximum of " + 2097152);
				}

				this.inflater.setInput(in.nioBuffer());
				ByteBuf decompressed = ctx.alloc().buffer(informedSize, informedSize);
				try {
					decompressed.writerIndex(this.inflater.inflate(decompressed.nioBuffer(0, informedSize)));
					list.add(decompressed.retain());
				} finally {
					decompressed.release();
					this.inflater.reset();
					in.clear();
				}
			}
		}
	}
}
