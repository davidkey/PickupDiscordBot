package com.dak.bots.discord.pickup;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.info.GitProperties;

//import com.dak.bots.discord.pickup.config.GitProperties;
import com.dak.bots.discord.pickup.service.BotRunnerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class PickupDiscordBotApplication implements CommandLineRunner {
	
	private final BotRunnerService botRunnerService;
	private final GitProperties gitProperties;

	public static void main(String[] args) {
		SpringApplication.run(PickupDiscordBotApplication.class, args);
	}
	
	@Override
	public void run(String... args) throws Exception {
		botRunnerService.start();
		log.debug("git properties: branch: {}; commit: {}; time: {}", 
				gitProperties.getBranch(), gitProperties.getCommitId(), gitProperties.getCommitTime());
	}
}
