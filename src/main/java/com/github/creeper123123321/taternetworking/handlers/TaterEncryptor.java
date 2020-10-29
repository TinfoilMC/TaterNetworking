package com.github.creeper123123321.taternetworking.handlers;

import com.github.creeper123123321.taternetworking.mixin.PacketEncryptionManagerAccessor;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import net.minecraft.network.encryption.PacketEncryptionManager;

import javax.crypto.Cipher;
import java.util.List;

public class TaterEncryptor extends MessageToMessageEncoder<ByteBuf> {
	private final PacketEncryptionManager crypto;

	public TaterEncryptor(PacketEncryptionManager crypto) {
		this.crypto = crypto;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
		ByteBuf in;
		if (msg.hasArray()) {
			in = msg.retain();
		} else {
			in = ctx.alloc().heapBuffer().writeBytes(msg); // aes cipher uses byte[]
		}
		try {
			Cipher cipher = ((PacketEncryptionManagerAccessor) crypto).getCipher();
			int index = in.readerIndex();
			int length = in.readableBytes();
			in.writerIndex(index);
			in.ensureWritable(cipher.getOutputSize(length));
			int bytes = cipher.update(in.nioBuffer(), in.nioBuffer());
			in.writerIndex(index + bytes);
			out.add(in.retain());
		} finally {
			in.release();
		}
	}
}
