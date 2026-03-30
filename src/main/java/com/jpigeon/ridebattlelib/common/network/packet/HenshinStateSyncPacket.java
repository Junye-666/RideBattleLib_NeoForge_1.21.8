package com.jpigeon.ridebattlelib.common.network.packet;

import com.jpigeon.ridebattlelib.RideBattleLib;
import com.jpigeon.ridebattlelib.common.data.HenshinState;
import com.jpigeon.ridebattlelib.common.util.PayloadUtils;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record HenshinStateSyncPacket(
        UUID playerId,
        boolean isTransformed,
        HenshinState state,
        Identifier currentFormId,
        Identifier pendingFormId
) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(RideBattleLib.MODID, "henshin_state_sync");
    public static final Type<@NotNull HenshinStateSyncPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<@NotNull RegistryFriendlyByteBuf, @NotNull HenshinStateSyncPacket> STREAM_CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC, HenshinStateSyncPacket::playerId,
                    ByteBufCodecs.BOOL, HenshinStateSyncPacket::isTransformed,
                    ByteBufCodecs.fromCodec(HenshinState.CODEC), HenshinStateSyncPacket::state,
                    PayloadUtils.nullableResourceLocation(), HenshinStateSyncPacket::currentFormId,
                    PayloadUtils.nullableResourceLocation(), HenshinStateSyncPacket::pendingFormId,
                    HenshinStateSyncPacket::new
            );

    @Override
    public @NotNull Type<?> type() {
        return TYPE;
    }
}