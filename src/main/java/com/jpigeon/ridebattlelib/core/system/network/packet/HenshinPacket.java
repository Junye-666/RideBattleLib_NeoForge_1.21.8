package com.jpigeon.ridebattlelib.core.system.network.packet;

import com.jpigeon.ridebattlelib.RideBattleLib;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record HenshinPacket(UUID playerId, Identifier riderId) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(RideBattleLib.MODID, "henshin");

    public static final StreamCodec<@NotNull RegistryFriendlyByteBuf, @NotNull HenshinPacket> STREAM_CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC,
                    HenshinPacket::playerId,
                    Identifier.STREAM_CODEC,
                    HenshinPacket::riderId,
                    HenshinPacket::new
            );

    public static final Type<@NotNull HenshinPacket> TYPE = new Type<>(ID);

    @Override
    public @NotNull Type<?> type() { return TYPE; }
}
