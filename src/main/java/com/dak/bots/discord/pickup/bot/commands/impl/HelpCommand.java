package com.dak.bots.discord.pickup.bot.commands.impl;

import com.dak.bots.discord.pickup.bot.commands.PickupCommandExecutor;
import com.dak.bots.discord.pickup.service.BotService;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class HelpCommand implements PickupCommandExecutor {
	
	@Override
	public void execute(MessageReceivedEvent event, BotService service) {
		service.sendHelpMsg(event.getChannel(), false);
	}
}
