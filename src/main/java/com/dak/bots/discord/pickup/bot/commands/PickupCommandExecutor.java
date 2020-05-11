package com.dak.bots.discord.pickup.bot.commands;

import com.dak.bots.discord.pickup.service.BotService;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@FunctionalInterface
public interface PickupCommandExecutor {
	void execute(MessageReceivedEvent event, BotService service);
}
