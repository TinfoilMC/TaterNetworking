package com.github.creeper123123321.taternetworking.mixin;

import io.netty.buffer.*;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DebugHud.class)
public class MixinDebugHud {
	@Inject(method = "getRightText", at = @At("RETURN"))
	private void onGetRightText(CallbackInfoReturnable<List<String>> cir) {
		cir.getReturnValue().add("PBBAD DM: " + PooledByteBufAllocator.DEFAULT.metric().usedDirectMemory() / 1024 / 1024 + "MiB HM: " + PooledByteBufAllocator.DEFAULT.metric().usedHeapMemory() / 1024 / 1024 + "MiB");
	}
}
