package com.jpigeon.ridebattlelib.core.system.network.packet;

import com.jpigeon.ridebattlelib.RideBattleLib;
import com.jpigeon.ridebattlelib.core.system.network.handler.UUIDStreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;


public record SwitchFormPacket(UUID playerId, Identifier formId) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(RideBattleLib.MODID, "switch_form");

    public static final StreamCodec<@NotNull RegistryFriendlyByteBuf, @NotNull SwitchFormPacket> STREAM_CODEC =
            StreamCodec.composite(
                    UUIDStreamCodec.INSTANCE,
                    SwitchFormPacket::playerId,
                    Identifier.STREAM_CODEC,
                    SwitchFormPacket::formId,
                    SwitchFormPacket::new
            );

    public static final Type<@NotNull SwitchFormPacket> TYPE = new Type<>(ID);

    @Override
    public @NotNull Type<?> type() { return TYPE; }
}
