package com.github.creeper123123321.taternetworking.mixin;

import com.github.creeper123123321.taternetworking.handlers.TaterEncryptor;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.PreferHeapByteBufAllocator;
import io.netty.handler.codec.MessageToByteEncoder;
import net.minecraft.network.encryption.PacketEncryptionManager;
import net.minecraft.network.encryption.PacketEncryptor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PacketEncryptor.class)
public abstract class PacketEncryptorMixin extends MessageToByteEncoder<ByteBuf> {
	@Shadow
	@Final
	private PacketEncryptionManager manager;

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) {
		ctx.pipeline().replace("encrypt", "encrypt", new TaterEncryptor(manager));
		ctx.channel().config().setAllocator(PreferHeapByteBufAllocator.DEFAULT);
	}
}
