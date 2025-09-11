package com.jpigeon.ridebattlelib.core.system.network.packet;

import com.jpigeon.ridebattlelib.RideBattleLib;
import com.jpigeon.ridebattlelib.core.system.network.handler.UUIDStreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;


public record SwitchFormPacket(UUID playerId, ResourceLocation formId) implements CustomPacketPayload {
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(RideBattleLib.MODID, "switch_form");

    public static final Type<SwitchFormPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, SwitchFormPacket> STREAM_CODEC =
            StreamCodec.composite(
                    UUIDStreamCodec.INSTANCE,
                    SwitchFormPacket::playerId,
                    ResourceLocation.STREAM_CODEC,
                    SwitchFormPacket::formId,
                    SwitchFormPacket::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
