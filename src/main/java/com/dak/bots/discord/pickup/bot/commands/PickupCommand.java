package com.dak.bots.discord.pickup.bot.commands;

import com.dak.bots.discord.pickup.bot.commands.impl.AddCommand;
import com.dak.bots.discord.pickup.bot.commands.impl.AutoCommand;
import com.dak.bots.discord.pickup.bot.commands.impl.CaptainCommand;
import com.dak.bots.discord.pickup.bot.commands.impl.ClearCommand;
import com.dak.bots.discord.pickup.bot.commands.impl.HelpCommand;
import com.dak.bots.discord.pickup.bot.commands.impl.PickCommand;
import com.dak.bots.discord.pickup.bot.commands.impl.StatusCommand;
import com.dak.bots.discord.pickup.exception.InvalidPickupCommandException;
import com.dak.bots.discord.pickup.service.BotService;

import lombok.Getter;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@Getter
public enum PickupCommand implements PickupCommandExecutor {
	HELP("help", 0, new HelpCommand()),
	CAPTAIN("captain", 3, new CaptainCommand()),
	AUTO("auto", 1, new AutoCommand()),
	ADD("add", 0, new AddCommand()),
	PICK("pick", 1, new PickCommand()),
	STATUS("status", 0, new StatusCommand()),
	CLEAR("clear", 0, new ClearCommand());

	private final String commandText;
	private final Integer numArgs;
	private final PickupCommandExecutor executor;

	private PickupCommand(final String commandText, final Integer numArgs, final PickupCommandExecutor executor) {
		this.commandText = commandText;
		this.numArgs = numArgs;
		this.executor = executor;
	}

	@Override
	public void execute(final MessageReceivedEvent event, final BotService service) {
		this.executor.execute(event, service);
	}

	public static PickupCommand fromString(final String s) {
		for(PickupCommand fc : PickupCommand.values()) {
			if(fc.commandText.equalsIgnoreCase(s)) {
				return fc;
			}
		}

		throw new InvalidPickupCommandException("Command " + s + " not valid!");
	}
}
