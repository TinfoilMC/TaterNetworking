package com.github.creeper123123321.taternetworking.mixin;

import net.minecraft.network.encryption.PacketEncryptionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import javax.crypto.Cipher;

@Mixin(PacketEncryptionManager.class)
public interface PacketEncryptionManagerAccessor {
	@Accessor
	Cipher getCipher();
}
