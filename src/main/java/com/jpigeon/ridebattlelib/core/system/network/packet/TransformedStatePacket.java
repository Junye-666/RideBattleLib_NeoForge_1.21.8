package com.jpigeon.ridebattlelib.core.system.network.packet;

import com.jpigeon.ridebattlelib.RideBattleLib;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record TransformedStatePacket(UUID playerId, boolean isTransformed) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(RideBattleLib.MODID, "transformed_state");

    public static final StreamCodec<@NotNull RegistryFriendlyByteBuf, @NotNull TransformedStatePacket> STREAM_CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC,
                    TransformedStatePacket::playerId,
                    ByteBufCodecs.BOOL,
                    TransformedStatePacket::isTransformed,
                    TransformedStatePacket::new
            );

    public static final Type<@NotNull TransformedStatePacket> TYPE = new Type<>(ID);

    @Override
    public @NotNull Type<?> type() { return TYPE; }
}
