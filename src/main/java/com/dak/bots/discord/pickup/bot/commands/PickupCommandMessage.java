package com.dak.bots.discord.pickup.bot.commands;

import com.dak.bots.discord.pickup.exception.PickupBotException;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class PickupCommandMessage {

	private final String[] args;
	private final PickupCommand command;

	public PickupCommandMessage(final String rawMessage) {
		this.args = this.parseArguments(rawMessage);
		this.command = PickupCommand.fromString(rawMessage.split("\\s")[1]);
	}
	
	public static Boolean validateMessage(final String s) {
		final String[] parts = s.split("\\s");
		if(parts.length < 2) {
			return false;
		}
		
		try {
			final PickupCommand cmd = PickupCommand.fromString(parts[1]);
			if(parts.length != cmd.getNumArgs() + 2) {
				return false;
			}
		} catch (PickupBotException pbe) {
			return false;
		}
		
		return true;
	}
	
	private String[] parseArguments(final String input) {
		final String[] parts = input.split("\\s");
		final String[] myArgs = new String[parts.length - 2];
		
		for(int i = 0; i < myArgs.length; i++) {
			myArgs[i] = parts[i+2];
		}
		
		return myArgs;
	}

}
