package com.github.creeper123123321.taternetworking.mixin;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.PacketDeflater;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.zip.Deflater;

@Mixin(PacketDeflater.class)
public class PacketDeflaterMixin {
	@Shadow
	private int compressionThreshold;

	@Shadow
	@Final
	private Deflater deflater;

	@Inject(method = "encode", at = @At("HEAD"), cancellable = true)
	private void onEncode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out, CallbackInfo ci) {
		ci.cancel();

		int nBytes = in.readableBytes();
		if (nBytes < this.compressionThreshold) {
			out.writeByte(0); // 0 varint
			out.writeBytes(in);
		} else {
			PacketByteBuf packetByteBuf = new PacketByteBuf(out);
			packetByteBuf.writeVarInt(nBytes);
			this.deflater.setInput(in.nioBuffer());
			this.deflater.finish();

			ByteBuf buffer = ctx.alloc().buffer(8192, 8192);
			try {
				while (!deflater.finished()) {
					buffer.writerIndex(this.deflater.deflate(buffer.nioBuffer(0, 8192)));
					out.writeBytes(buffer);
					buffer.clear();
				}
			} finally {
				buffer.release();
				in.clear();
				this.deflater.reset();
			}
		}
	}
}
