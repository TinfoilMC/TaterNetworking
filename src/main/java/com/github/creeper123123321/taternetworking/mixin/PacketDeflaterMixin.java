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
			
			int start = out.writerIndex();

			// https://github.com/VelocityPowered/Velocity/blob/dev/1.1.0/proxy/src/main/java/com/velocitypowered/proxy/protocol/netty/MinecraftCompressEncoder.java#L44
			out.ensureWritable(nBytes + 1);
			try {
				out.writerIndex(start + this.deflater.deflate(out.nioBuffer(start, nBytes + 1)));
			} finally {
				in.clear();
				this.deflater.reset();
			}
		}
	}
}
