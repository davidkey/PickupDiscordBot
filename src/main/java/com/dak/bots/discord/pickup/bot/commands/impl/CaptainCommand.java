package com.dak.bots.discord.pickup.bot.commands.impl;

import com.dak.bots.discord.pickup.bot.commands.PickupCommand;
import com.dak.bots.discord.pickup.bot.commands.PickupCommandExecutor;
import com.dak.bots.discord.pickup.bot.commands.PickupCommandMessage;
import com.dak.bots.discord.pickup.bot.model.PickupPlayer;
import com.dak.bots.discord.pickup.bot.model.PickupSession;
import com.dak.bots.discord.pickup.bot.model.PickupTeam;
import com.dak.bots.discord.pickup.service.BotService;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CaptainCommand implements PickupCommandExecutor {
	
	@Override
	public void execute(MessageReceivedEvent event, BotService service) {
		if(service.hasPermissions(PickupCommand.CAPTAIN, event)) {
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
			final String teamOneCaptainTag = args[0].replaceAll("[^0-9]", "");
			final String teamTwoCaptainTag = args[1].replaceAll("[^0-9]", "");
			final Integer teamSize = Integer.parseInt(args[2]);

			final Member memberCaptainOne = event.getGuild().getMemberById(teamOneCaptainTag);
			final Member memberCaptainTwo = event.getGuild().getMemberById(teamTwoCaptainTag);

			final PickupPlayer captainOne = PickupPlayer.builder()
					.id(memberCaptainOne.getId())
					.tag(memberCaptainOne.getUser().getAsTag())
					.nickname(memberCaptainOne.getNickname() != null ? memberCaptainOne.getNickname() : memberCaptainOne.getUser().getName())
					.isCaptain(true)
					.team(PickupTeam.TEAM_ONE)
					.build();
			final PickupPlayer captainTwo = PickupPlayer.builder()
					.id(memberCaptainTwo.getId())
					.tag(memberCaptainTwo.getUser().getAsTag())
					.nickname(memberCaptainTwo.getNickname() != null ? memberCaptainTwo.getNickname() : memberCaptainTwo.getUser().getName())
					.isCaptain(true)
					.team(PickupTeam.TEAM_TWO)
					.build();

			final PickupSession session = new PickupSession(event.getChannel().getId().replaceAll("[^0-9]", ""), teamSize, captainOne, captainTwo);
			service.addSession(guildId, session);

			if(session.isGameFull()) {
				service.sendGameReadyMsg(event.getChannel(), session);
				service.removeSession(guildId);
				return;
			}

			final StringBuilder sb = new StringBuilder();
			sb.append("Pickup session created: \n\n" + session.prettyPrint());
			sb.append("\nPlayers, please queue for the session by entering ``" + service.getCommandString() + " add``");

			event.getChannel().sendMessage(sb.toString()).queue();

		} else {
			service.sendPermissionsErrorMsg(event.getChannel());
		}
	}
}
