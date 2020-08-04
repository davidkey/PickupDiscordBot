package com.dak.bots.discord.pickup.bot.commands.impl;

import com.dak.bots.discord.pickup.bot.commands.PickupCommand;
import com.dak.bots.discord.pickup.bot.commands.PickupCommandExecutor;
import com.dak.bots.discord.pickup.bot.commands.PickupCommandMessage;
import com.dak.bots.discord.pickup.bot.model.PickupSession;
import com.dak.bots.discord.pickup.bot.model.PickupTeam;
import com.dak.bots.discord.pickup.service.BotService;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@Slf4j
public class ResizeCommand implements PickupCommandExecutor {

	@Override
	public void execute(MessageReceivedEvent event, BotService service) {
		final String channelId = event.getChannel().getId().replaceAll("[^0-9]", "");
		if(service.hasExistingSession(channelId)) {
			final PickupSession session = service.getSession(channelId).get();
			if(!service.hasPermissions(PickupCommand.RESIZE, event)){
				service.sendPermissionsErrorMsg(event.getChannel());
				return;
			} 

			if(session.isGameFull()) {
				event.getChannel().sendMessage("Cannot resize game as it is already full!").queue();
				return;
			}

			log.trace("attempting to resize session for guild {}", channelId);

			final String text = event.getMessage().getContentRaw().trim();
			final PickupCommandMessage pickupMessage = new PickupCommandMessage(text);
			final String[] args = pickupMessage.getArgs();

			final PickupSession resizedSession = session.toBuilder().teamSize(Integer.parseInt(args[0])).build();
			service.addSession(channelId, resizedSession);

			if(resizedSession.isOversized()) {
				resizedSession.trimDownToSize();
			} 

			if(resizedSession.getNumberOfPlayersNeeded() == 0) {
				if(resizedSession.isAutoFilled()) {
					resizedSession.populateSession();
					service.sendGameReadyMsg(event.getChannel(), resizedSession);
					service.removeSession(channelId);
					return;
				} else {
					if(resizedSession.isGameFull()) {
						service.sendGameReadyMsg(event.getChannel(), resizedSession);
					} else {
						final User nextCaptainUser = event.getGuild().getMemberById(resizedSession.getNextCaptainToPick().get().getId()).getUser();
						event.getChannel().sendMessage("Enough players are queued for captains to pick players. " 
								+ nextCaptainUser.getAsMention() + ", please ``" + service.getCommandString() + " pick`` a player. \n```" 
								+ resizedSession.getPrettyPrintedPlayersByTeam(PickupTeam.NO_TEAM) + "```").queue();
					}
					return;
				}
			} else {
				service.sendGameResizedMsg(event.getChannel(), resizedSession);
			}

		} else {
			service.sendNoSessionMessage(event.getChannel());
		}
	}

}
