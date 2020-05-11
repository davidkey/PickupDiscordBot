package com.dak.bots.discord.pickup.service;

import org.springframework.stereotype.Service;

import com.dak.bots.discord.pickup.bot.PickupBot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;

@Service
@RequiredArgsConstructor
@Slf4j
public class BotRunnerService {

	private final JDA jda;
	private final PickupBot pickupBot;
	
	public void start() {
		log.debug("starting bot...");
		jda.addEventListener(pickupBot);
	}
}
