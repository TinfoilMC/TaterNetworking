package com.github.creeper123123321.taternetworking.mixin;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
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
public abstract class PacketDeflaterMixin extends MessageToByteEncoder<ByteBuf> {
	@Shadow
	private int compressionThreshold;
	@Shadow
	@Final
	private Deflater deflater;
	@Shadow
	private byte[] deflateBuffer;

	{
		deflateBuffer = null; // Don't need it, we use pooled bytebuf
	}

	@Override
	protected ByteBuf allocateBuffer(ChannelHandlerContext ctx, ByteBuf msg, boolean preferDirect) throws Exception {
		// https://github.com/VelocityPowered/Velocity/blob/dev/1.1.0/proxy/src/main/java/com/velocitypowered/proxy/protocol/netty/MinecraftCompressEncoder.java#L44
		int capacity = msg.readableBytes() + 1;
		return preferDirect ? ctx.alloc().heapBuffer(capacity) : ctx.alloc().ioBuffer(capacity);
	}

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

			try {
				while (!deflater.finished()) {
					if (!out.isWritable()) out.ensureWritable(8192);
					out.writerIndex(out.writerIndex() + this.deflater.deflate(
							out.nioBuffer(out.writerIndex(), out.writableBytes())));
				}
			} finally {
				in.clear();
				this.deflater.reset();
			}
		}
	}
}
