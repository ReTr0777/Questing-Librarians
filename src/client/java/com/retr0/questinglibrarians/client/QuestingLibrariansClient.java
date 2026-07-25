package com.retr0.questinglibrarians.client;

import com.retr0.questinglibrarians.client.config.TutorialConfig;
import net.fabricmc.api.ClientModInitializer;

public class QuestingLibrariansClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Load client tutorial config on client startup
		TutorialConfig.load();
	}
}