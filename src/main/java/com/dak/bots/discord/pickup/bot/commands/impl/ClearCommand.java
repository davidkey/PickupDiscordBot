package com.dak.bots.discord.pickup.bot.commands.impl;

import com.dak.bots.discord.pickup.bot.commands.PickupCommandExecutor;
import com.dak.bots.discord.pickup.service.BotService;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ClearCommand implements PickupCommandExecutor {

	@Override
	public void execute(MessageReceivedEvent event, BotService service) {
		final String channelId = event.getChannel().getId().replaceAll("[^0-9]", "");
		if(service.hasExistingSession(channelId)) {
			service.removeSession(channelId);
			event.getChannel().sendMessage("Pickup session cleared.").queue();
		} else {
			service.sendNoSessionMessage(event.getChannel());
		}
	}

}
