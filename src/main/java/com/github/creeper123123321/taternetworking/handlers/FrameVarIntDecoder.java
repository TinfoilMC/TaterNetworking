package com.github.creeper123123321.taternetworking.handlers;

import io.netty.util.ByteProcessor;

public class FrameVarIntDecoder implements ByteProcessor {
	// Based on https://github.com/VelocityPowered/Velocity/blob/dev/1.1.0/proxy/src/main/java/com/velocitypowered/proxy/protocol/netty/MinecraftVarintFrameDecoder.java
	// which is licensed under MIT, Copyright 2018 Velocity team

	public int readVarint;
	public int bytesRead;
	public DecodeResult result = DecodeResult.TOO_SHORT;

	@Override
	public boolean process(byte k) {
		readVarint |= (k & 0x7F) << bytesRead++ * 7;
		if (bytesRead > 3) {
			result = DecodeResult.TOO_BIG;
			return false;
		}
		if ((k & 0x80) != 128) {
			result = DecodeResult.SUCCESS;
			return false;
		}
		return true;
	}

	public void reset() {
		readVarint = 0;
		bytesRead = 0;
		result = DecodeResult.TOO_SHORT;
	}

	public enum DecodeResult {
		SUCCESS,
		TOO_SHORT,
		TOO_BIG
	}
}
