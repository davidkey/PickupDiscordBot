package com.dak.bots.discord.pickup.bot.commands.impl;

import com.dak.bots.discord.pickup.bot.commands.PickupCommand;
import com.dak.bots.discord.pickup.bot.commands.PickupCommandExecutor;
import com.dak.bots.discord.pickup.bot.commands.PickupCommandMessage;
import com.dak.bots.discord.pickup.bot.model.PickupSession;
import com.dak.bots.discord.pickup.service.BotService;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class AutoCommand implements PickupCommandExecutor {

	@Override
	public void execute(final MessageReceivedEvent event, final BotService service){
		if(service.hasPermissions(PickupCommand.AUTO, event)) {
			final String guildId = event.getGuild().getId().replaceAll("[^0-9]", "");
			if(service.hasExistingSession(guildId)) {
				if(!service.getSession(guildId).get().isGameFull()) {
					// there's still a matchmaking session in progress...
					event.getChannel().sendMessage("There is already a pickup session in progress.").queue();
					return;
				}
			}

			final String text = event.getMessage().getContentRaw().trim();
			final PickupCommandMessage pickupMessage = new PickupCommandMessage(text);
			final String[] args = pickupMessage.getArgs();
			final Integer teamSize = Integer.parseInt(args[0]);

			final PickupSession pickupSession = new PickupSession(event.getChannel().getId().replaceAll("[^0-9]", ""), teamSize);
			service.addSession(guildId, pickupSession);

			event.getChannel().sendMessage("Auto-populated pickup session created. Players, please ``!pickup add`` to join.").queue();
		} else {
			service.sendPermissionsErrorMsg(event.getChannel());
		}
	}

}
