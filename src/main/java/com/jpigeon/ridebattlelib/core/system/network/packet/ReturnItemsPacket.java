package com.jpigeon.ridebattlelib.core.system.network.packet;

import com.jpigeon.ridebattlelib.RideBattleLib;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public record ReturnItemsPacket() implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(RideBattleLib.MODID, "return_items");

    public static final StreamCodec<@NotNull RegistryFriendlyByteBuf, @NotNull ReturnItemsPacket> STREAM_CODEC =
            StreamCodec.unit(new ReturnItemsPacket());

    public static final Type<@NotNull ReturnItemsPacket> TYPE = new Type<>(ID);

    @Override
    public @NotNull Type<?> type() { return TYPE; }
}
