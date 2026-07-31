package com.retr0.questinglibrarians.config;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ConfigSyncPayload(int maxBooksNormal, int maxBooksMaster, int maxBooksCured) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ConfigSyncPayload> TYPE = new CustomPacketPayload.Type<>(
            Identifier.fromNamespaceAndPath("questing-librarians", "config_sync")
    );

    public static final StreamCodec<FriendlyByteBuf, ConfigSyncPayload> CODEC = StreamCodec.ofMember(
            ConfigSyncPayload::write,
            ConfigSyncPayload::read
    );

    private void write(FriendlyByteBuf buf) {
        buf.writeInt(maxBooksNormal);
        buf.writeInt(maxBooksMaster);
        buf.writeInt(maxBooksCured);
    }

    private static ConfigSyncPayload read(FriendlyByteBuf buf) {
        return new ConfigSyncPayload(buf.readInt(), buf.readInt(), buf.readInt());
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
