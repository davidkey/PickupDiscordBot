package com.dak.bots.discord.pickup.exception;

public class InvalidPickupCommandException extends PickupBotException {

	private static final long serialVersionUID = -1086507641512498727L;

	public InvalidPickupCommandException() {
	}

	public InvalidPickupCommandException(String message) {
		super(message);
	}

	public InvalidPickupCommandException(Throwable cause) {
		super(cause);
	}

	public InvalidPickupCommandException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidPickupCommandException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
