package com.github.creeper123123321.taternetworking.mixin;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.encryption.PacketEncryptionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.crypto.Cipher;
import javax.crypto.ShortBufferException;

@Mixin(PacketEncryptionManager.class)
public class PacketDecryptorMixin {
	@Shadow
	private Cipher cipher;

	@Inject(method = "decrypt", at = @At("HEAD"), cancellable = true)
	private void onDecrypt(ChannelHandlerContext ctx, ByteBuf msg, CallbackInfoReturnable<ByteBuf> cir) throws ShortBufferException {
		cir.cancel();

		ByteBuf in;

		if (msg.hasArray()) {
			in = msg.retain();
		} else {
			in = ctx.alloc().heapBuffer().writeBytes(msg);
		}
		try {
			int index = in.readerIndex();
			int length = in.readableBytes();
			in.writerIndex(index);
			in.ensureWritable(cipher.getOutputSize(in.readableBytes()));
			int bytes = cipher.update(in.array(), in.arrayOffset() + index, length,
					in.array(), in.arrayOffset() + index);
			in.writerIndex(index + bytes);
			cir.setReturnValue(in.retain());
		} finally {
			in.release();
		}
	}
}
