package com.dak.bots.discord.pickup.exception;

public class PickupBotException extends RuntimeException {

	private static final long serialVersionUID = -177710800312815789L;

	public PickupBotException() {
	}

	public PickupBotException(String message) {
		super(message);
	}

	public PickupBotException(Throwable cause) {
		super(cause);
	}

	public PickupBotException(String message, Throwable cause) {
		super(message, cause);
	}

	public PickupBotException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
