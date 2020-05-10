package com.dak.bots.discord.pickup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.dak.bots.discord.pickup.service.BotService;

@SpringBootApplication
public class PickupDiscordBotApplication implements CommandLineRunner {
	
	@Autowired
	private BotService botService;

	public static void main(String[] args) {
		SpringApplication.run(PickupDiscordBotApplication.class, args);
	}
	
	@Override
	public void run(String... args) throws Exception {
		botService.start();
	}
}
