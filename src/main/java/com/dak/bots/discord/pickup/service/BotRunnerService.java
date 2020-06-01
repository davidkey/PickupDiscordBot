package com.dak.bots.discord.pickup.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.dak.bots.discord.pickup.bot.PickupBot;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;

@Service
@Slf4j
public class BotRunnerService {

	private final JDA jda;
	private final PickupBot pickupBot;
	private final String clientId;
	
	public BotRunnerService(final JDA jda, final PickupBot pickupBot, @Value("${bot.clientId}") final String clientId) {
		this.jda = jda;
		this.pickupBot = pickupBot;
		this.clientId = clientId;
	}
	
	public void start() {
		log.debug("starting bot - client id {}", clientId);
		jda.addEventListener(pickupBot);
	}
}
