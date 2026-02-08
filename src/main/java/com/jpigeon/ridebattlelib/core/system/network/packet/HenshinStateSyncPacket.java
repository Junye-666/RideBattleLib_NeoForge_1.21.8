package com.jpigeon.ridebattlelib.core.system.network.packet;

import com.jpigeon.ridebattlelib.RideBattleLib;
import com.jpigeon.ridebattlelib.core.system.henshin.helper.HenshinState;
import com.jpigeon.ridebattlelib.core.system.network.handler.UUIDStreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record HenshinStateSyncPacket(UUID playerId, HenshinState state,
                                     Identifier pendingFormId)
        implements CustomPacketPayload {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(RideBattleLib.MODID, "henshin_state_sync");

    public static final StreamCodec<@NotNull RegistryFriendlyByteBuf, @NotNull HenshinStateSyncPacket> STREAM_CODEC =
            StreamCodec.composite(
                    UUIDStreamCodec.INSTANCE,
                    HenshinStateSyncPacket::playerId,
                    ByteBufCodecs.fromCodec(HenshinState.CODEC),
                    HenshinStateSyncPacket::state,
                    Identifier.STREAM_CODEC,
                    HenshinStateSyncPacket::pendingFormId,
                    HenshinStateSyncPacket::new
            );

    public static final Type<@NotNull HenshinStateSyncPacket> TYPE = new Type<>(ID);

    @Override
    public @NotNull Type<?> type() { return TYPE; }
}