package com.dak.bots.discord.pickup.bot.commands.impl;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

import org.springframework.boot.info.GitProperties;
import org.springframework.stereotype.Component;

import com.dak.bots.discord.pickup.bot.commands.PickupCommandExecutor;
import com.dak.bots.discord.pickup.service.BotService;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@Component
public class DebugCommand implements PickupCommandExecutor {

	private final DateTimeFormatter formatter;

	public DebugCommand() {
		this.formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(Locale.getDefault()).withZone(ZoneOffset.UTC);
	}

	@Override
	public void execute(MessageReceivedEvent event, BotService service) {
		final GitProperties gitProperties = service.getGitProperties();
		final String scmUrl = service.getScmUrl();
		
		final MessageEmbed msg = new EmbedBuilder()
				.setAuthor(scmUrl, scmUrl)
				.addField("Branch", gitProperties.getBranch(), true)
				.addField("Time", formatter.format(gitProperties.getCommitTime()) + " UTC", true)
				.addField("Commit ID", gitProperties.getCommitId(), true)
				.build();

		event.getChannel().sendMessage(msg).queue();
	}
}
