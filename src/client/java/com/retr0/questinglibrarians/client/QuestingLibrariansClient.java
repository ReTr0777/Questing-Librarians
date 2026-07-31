package com.retr0.questinglibrarians.client;

import com.retr0.questinglibrarians.client.config.TutorialConfig;
import com.retr0.questinglibrarians.config.ConfigSyncPayload;
import com.retr0.questinglibrarians.config.QuestingLibrariansConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class QuestingLibrariansClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Load client tutorial config on client startup
		TutorialConfig.load();

		// Register config sync receiver
		ClientPlayNetworking.registerGlobalReceiver(ConfigSyncPayload.TYPE, (payload, context) -> {
			context.client().execute(() -> {
				QuestingLibrariansConfig.maxBooksNormal = payload.maxBooksNormal();
				QuestingLibrariansConfig.maxBooksMaster = payload.maxBooksMaster();
				QuestingLibrariansConfig.maxBooksCured = payload.maxBooksCured();
			});
		});
	}
}