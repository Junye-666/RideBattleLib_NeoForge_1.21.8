package com.jpigeon.ridebattlelib.core.system.network.packet;

import com.jpigeon.ridebattlelib.RideBattleLib;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record UnhenshinPacket(UUID playerId) implements CustomPacketPayload {
    public static final Identifier ID =
            Identifier.fromNamespaceAndPath(RideBattleLib.MODID, "unhenshin");

    public static final StreamCodec<@NotNull RegistryFriendlyByteBuf, @NotNull UnhenshinPacket> STREAM_CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC,
                    UnhenshinPacket::playerId,
                    UnhenshinPacket::new
            );

    public static final Type<@NotNull UnhenshinPacket> TYPE = new Type<>(ID);

    @Override
    public @NotNull Type<?> type() { return TYPE; }
}
