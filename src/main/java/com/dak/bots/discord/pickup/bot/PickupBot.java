package com.dak.bots.discord.pickup.bot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.dak.bots.discord.pickup.exception.PickupBotException;
import com.dak.bots.discord.pickup.model.PickupCommandType;
import com.dak.bots.discord.pickup.model.PickupMessage;
import com.dak.bots.discord.pickup.model.PickupPlayer;
import com.dak.bots.discord.pickup.model.PickupSession;
import com.dak.bots.discord.pickup.model.PickupTeam;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Component
@Slf4j
public class PickupBot extends ListenerAdapter {

	private final Map<PickupCommandType, Consumer<MessageReceivedEvent>> commands;
	private final Map<String, PickupSession> sessions;

	public PickupBot(final JDA jda) {
		this.commands = this.getPickupCommands();
		this.sessions = new HashMap<>();
	}

	@Override
	public void onMessageReceived(final MessageReceivedEvent event) {
		if(event.isFromType(ChannelType.TEXT)) {
			/**
			 * Ignore messages from other bots
			 */
			if(event.getAuthor().isBot()) {
				return;
			}

			final String text = event.getMessage().getContentRaw().trim();

			if(text != null && text.startsWith("!pickup")) {
				final MessageChannel channel = event.getChannel();

				/**
				 * Validate message; send help if invalid
				 */
				if(!PickupMessage.validateMessage(text)) {
					sendHelpMsg(channel, true);
					return;
				}

				final PickupMessage pickupMessage = new PickupMessage(text);

				log.trace("pickup message received: {}", pickupMessage);

				/**
				 * Dispatch fart command
				 */
				commands.get(pickupMessage.getCommand()).accept(event);
			}
		}
	}

	private Map<PickupCommandType, Consumer<MessageReceivedEvent>> getPickupCommands(){
		final Map<PickupCommandType, Consumer<MessageReceivedEvent>> cmds = new HashMap<>();

		/**
		 * Help command
		 */
		cmds.put(PickupCommandType.HELP, event -> {
			sendHelpMsg(event.getChannel(), false);
		});

		/**
		 * Captain command
		 */
		cmds.put(PickupCommandType.CAPTAIN, event -> {
			if(hasPermissions(PickupCommandType.CAPTAIN, event)) {
				final String guildId = event.getGuild().getId().replaceAll("[^0-9]", "");
				if(sessions.containsKey(guildId)) {
					if(!sessions.get(guildId).isGameFull()) {
						// there's still a matchmaking session in progress...
						event.getChannel().sendMessage("There is already a pickup session in progress.").queue();
						return;
					}
				}

				final String text = event.getMessage().getContentRaw().trim();
				final PickupMessage pickupMessage = new PickupMessage(text);
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
				sessions.put(guildId, session);

				if(session.isGameFull()) {
					sendGameReadyMsg(event.getChannel(), session);
					sessions.remove(guildId);
					return;
				}

				final StringBuilder sb = new StringBuilder();
				sb.append("Pickup session created: \n\n" + session.prettyPrint());
				sb.append("\nPlayers, please queue for the session by entering ``!pickup add``");

				event.getChannel().sendMessage(sb.toString()).queue();

			} else {
				sendPermissionsErrorMsg(event.getChannel());
			}
			//event.getAuthor().g
			//sendHelpMsg(event.getChannel(), false);
		});

		/**
		 * Add command
		 */
		cmds.put(PickupCommandType.ADD, event -> {
			final String guildId = event.getGuild().getId().replaceAll("[^0-9]", "");
			if(sessions.containsKey(guildId)) {
				final PickupSession session = sessions.get(guildId);

				if(session.isPlayerInSession(event.getAuthor().getId())) {
					event.getChannel().sendMessage(event.getAuthor().getAsMention() + " - you're already queued!").queue();
					return;
				}

				final User user = event.getAuthor(); 
				final Member userAsMember = event.getGuild().getMemberById(user.getId());

				session.addPlayer(PickupPlayer.builder()
						.id(user.getId())
						.tag(user.getAsTag())
						.nickname(userAsMember.getNickname() != null ? userAsMember.getNickname() : user.getName())
						.build());
				
				log.trace("nickname: {}; name: {}; user: {}", userAsMember.getNickname(), user.getName(), user);
				
				log.trace("player added. all players: ");
				session.getPlayers().forEach(p -> {
					log.trace("player: {}", p);
				});
				
				event.getMessage().addReaction("👍").queue();

				if(session.getNumberOfPlayersNeeded() == 0) {
					event.getChannel().sendMessage(user.getAsMention() + " added to session.");

					if(session.getIsAutoFilled()) {
						// autofill rosters and display
						session.populateRosters();
						sendGameReadyMsg(event.getChannel(), session);
						sessions.remove(guildId);
					} else {
						// session is ready for captains to pick
						event.getChannel().sendMessage("Enough players are queued for captains to pick players. " 
								+ session.getNextCaptainToPick().get().getTag() + ", please ``!pick`` a player. \n" 
								+ session.getPrettyPrintedPlayersByTeam(PickupTeam.NO_TEAM)).queue();
					}
				} else {
					event.getChannel().sendMessage(user.getAsMention() + " added to session. Players in queue: \n" + 
							"```" + session.getPrettyPrintedPlayersByTeam(PickupTeam.NO_TEAM) + "```").queue();
				}


			} else {
				sendNoSessionMessage(event.getChannel());
				return;
			}


		});

		/**
		 * Pick command
		 */
		cmds.put(PickupCommandType.PICK, event -> {
			final String guildId = event.getGuild().getId().replaceAll("[^0-9]", "");

			// make sure there's an outstanding session
			if(sessions.containsKey(guildId)) {
				final PickupSession session = sessions.get(guildId);

				final Optional<PickupPlayer> captain = session.getNextCaptainToPick();
				if(captain.isPresent() && captain.get().getId().equals(event.getAuthor().getId())){
					final String text = event.getMessage().getContentRaw().trim();
					final PickupMessage pickupMessage = new PickupMessage(text);
					final String[] args = pickupMessage.getArgs();

					final String playerTagToAdd = args[0];
					final User playerToAdd = event.getGuild().getMemberByTag(playerTagToAdd).getUser(); // is tag right?
					session.assignPlayerToTeam(playerToAdd.getId(), captain.get().getTeam());

					if(session.isGameFull()) {
						// game is full - show message
						sendGameReadyMsg(event.getChannel(), session);
						sessions.remove(guildId);
					} else {
						final Optional<PickupPlayer> nextCaptain = session.getNextCaptainToPick();
						event.getChannel().sendMessage(
								playerToAdd.getAsMention() + " added to " + captain.get().getTeam() + ". "
										+ nextCaptain.get().getTag()
										+ ", it's your turn. Please ``!pickup pick`` a player. \n" 
										+ session.getPrettyPrintedPlayersByTeam(PickupTeam.NO_TEAM)).queue();
					}

				} else {
					// it's not your turn!
					event.getChannel().sendMessage(event.getAuthor().getAsMention() + " - it's not your turn to pick!");
					return;
				}
			} else {
				sendNoSessionMessage(event.getChannel());
				return;
			}
		});

		/**
		 * Status command
		 */
		cmds.put(PickupCommandType.STATUS, event -> {
			final String guildId = event.getGuild().getId().replaceAll("[^0-9]", "");
			if(sessions.containsKey(guildId)) {
				event.getChannel().sendMessage(sessions.get(guildId).prettyPrint()).queue();
			} else {
				sendNoSessionMessage(event.getChannel());
			}
		});

		/**
		 * Clear command
		 */
		cmds.put(PickupCommandType.CLEAR, event -> {
			if(hasPermissions(PickupCommandType.CLEAR, event)) {
				final String guildId = event.getGuild().getId().replaceAll("[^0-9]", "");
				if(sessions.containsKey(guildId)) {
					sessions.remove(guildId);
					event.getChannel().sendMessage("Pickup session cleared.").queue();
				} else {
					sendNoSessionMessage(event.getChannel());
				}
			} else {
				sendPermissionsErrorMsg(event.getChannel());
			}
		});

		/**
		 * Auto command
		 */
		cmds.put(PickupCommandType.AUTO, event -> {
			if(hasPermissions(PickupCommandType.AUTO, event)) {
				final String guildId = event.getGuild().getId().replaceAll("[^0-9]", "");
				if(sessions.containsKey(guildId)) {
					if(!sessions.get(guildId).isGameFull()) {
						// there's still a matchmaking session in progress...
						event.getChannel().sendMessage("There is already a pickup session in progress.").queue();
						return;
					}
				}

				final String text = event.getMessage().getContentRaw().trim();
				final PickupMessage pickupMessage = new PickupMessage(text);
				final String[] args = pickupMessage.getArgs();
				final Integer teamSize = Integer.parseInt(args[0]);

				final PickupSession pickupSession = new PickupSession(event.getChannel().getId().replaceAll("[^0-9]", ""), teamSize);
				sessions.put(guildId, pickupSession);

				event.getChannel().sendMessage("Auto-populated pickup session created. Players, please ``!pickup add`` to join.").queue();
			} else {
				sendPermissionsErrorMsg(event.getChannel());
			}
		});


		return cmds;
	}

	private Boolean hasPermissions(final PickupCommandType commandType, final MessageReceivedEvent event) {
		final String userId = event.getAuthor().getId();
		if(PickupCommandType.CAPTAIN.equals(commandType) || PickupCommandType.CLEAR.equals(commandType) || PickupCommandType.AUTO.equals(commandType)) {
			final List<Role> captainRoles = event.getGuild().getRoles().stream()
					.filter(r -> r.getName().equalsIgnoreCase("Pickup Bot Admin Role"))
					.collect(Collectors.toList());
			if(captainRoles.isEmpty()) {
				sendRolesMissingMessage(event.getChannel());
				throw new PickupBotException("required roles missing from server!");
			}

			final List<Member> membersWithCaptainRole = event.getGuild().getMembersWithRoles(captainRoles);

			for(final Member member : membersWithCaptainRole) {
				if(member.getId().equals(userId)) {
					return true;
				}
			}

			return false;
		}

		return true; // should this default to true or false? hmm...
	}

	private void sendHelpMsg(final MessageChannel channel, final Boolean hasError) {
		channel.sendMessage((hasError ? "Invalid Pickup Bot format! \n\n" : "") + 
				"Start a new pickup session with captains:" +
				"```!pickup captain @captain_one @captain_two [team size]```" +
				"\nStart a new pickup game with randomly selected teams:" +
				"```!pickup auto [team size]```" +
				"\nAdd yourself as a player to an ongoing pickup session:" +
				"```!pickup add```" + 
				"\nAs a captain, pick a player from the queue (only if queue is full):" +
				"```!pickup pick @user```" +
				"\nView session status:" +
				"```!pickup status```" +
				"\nClear existing session:" +
				"```!pickup clear```"
				)
		.queue();
	}

	private void sendPermissionsErrorMsg(final MessageChannel channel) {
		channel.sendMessage("User does not have permissions to run that command!").queue();
	}

	private void sendGameReadyMsg(final MessageChannel channel, final PickupSession session) {
		channel.sendMessage("The game is ready to begin! \n\n" + session.prettyPrint()).queue();
	}

	private void sendNoSessionMessage(final MessageChannel channel) {
		channel.sendMessage("No pickup sessions currently available. Please ask a mod to start a session.").queue();
	}

	private void sendRolesMissingMessage(final MessageChannel channel) {
		channel.sendMessage("Role ``Pickup Bot Admin Role`` is missing from this server. Please ask a server admin to add it.").queue();
	}

}
