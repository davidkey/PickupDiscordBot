package com.dak.bots.discord.pickup.bot.commands.impl;

import com.dak.bots.discord.pickup.bot.commands.PickupCommandExecutor;
import com.dak.bots.discord.pickup.bot.commands.PickupCommandMessage;
import com.dak.bots.discord.pickup.bot.model.PickupSession;
import com.dak.bots.discord.pickup.service.BotService;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class TeamlessCommand implements PickupCommandExecutor {

	@Override
	public void execute(final MessageReceivedEvent event, final BotService service){
		final String channelId = event.getChannel().getId().replaceAll("[^0-9]", "");
		if(service.hasExistingSession(channelId)) {
			if(!service.getSession(channelId).get().isGameFull()) {
				// there's still a matchmaking session in progress...
				event.getChannel().sendMessage("There is already a pickup session in progress.").queue();
				return;
			}
		}

		final String text = event.getMessage().getContentRaw().trim();
		final PickupCommandMessage pickupMessage = new PickupCommandMessage(text);
		final String[] args = pickupMessage.getArgs();
		final Integer teamSize = Integer.parseInt(args[0]);

		final PickupSession pickupSession = new PickupSession(event.getChannel().getId().replaceAll("[^0-9]", ""), teamSize, true);
		service.addSession(channelId, pickupSession);

		event.getChannel().sendMessage("Teamless pickup session created. Players, please ``" + service.getCommandString() + " add`` to join.").queue();
	}

}
