package com.github.creeper123123321.taternetworking;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class TaterPreLaunch implements PreLaunchEntrypoint {
	@Override
	public void onPreLaunch() {
		// See https://github.com/VelocityPowered/Velocity/blob/dev/1.1.0/proxy/src/main/java/com/velocitypowered/proxy/Velocity.java#L26
		if (System.getProperty("io.netty.allocator.maxOrder") == null) {
			System.setProperty("io.netty.allocator.maxOrder", "9");
		}
	}
}
