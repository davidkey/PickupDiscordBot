package com.dak.bots.discord.pickup.bot.commands.impl;

import com.dak.bots.discord.pickup.bot.commands.PickupCommandExecutor;
import com.dak.bots.discord.pickup.bot.model.PickupSession;
import com.dak.bots.discord.pickup.service.BotService;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@Slf4j
public class RemoveCommand implements PickupCommandExecutor {

	@Override
	public void execute(MessageReceivedEvent event, BotService service) {
		final String guildId = event.getGuild().getId().replaceAll("[^0-9]", "");
		if(service.hasExistingSession(guildId)) {
			final PickupSession session = service.getSession(guildId).get();
			final User user = event.getAuthor(); 

			log.trace("attempting to remove player {} from session for guild {}", user.getAsMention(), guildId);
			if(session.isGameFull()) {
				event.getChannel().sendMessage("Player " + user.getAsMention() + " cannot be removed from session as game is already full!").queue();
			} else if(session.removePlayer(user.getId())) {
				event.getChannel().sendMessage("Player " + user.getAsMention() + " removed from session.").queue();
			} else {
				event.getChannel().sendMessage("Removing " + user.getAsMention() + " failed for unknown reasons. Player may not have been queued.").queue();
			}
		} else {
			service.sendNoSessionMessage(event.getChannel());
		}
	}

}
