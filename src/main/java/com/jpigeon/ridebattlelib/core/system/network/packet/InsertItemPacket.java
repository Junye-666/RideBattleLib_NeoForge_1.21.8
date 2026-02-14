package com.jpigeon.ridebattlelib.core.system.network.packet;

import com.jpigeon.ridebattlelib.RideBattleLib;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record InsertItemPacket(UUID playerId, Identifier slotId, ItemStack stack) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(RideBattleLib.MODID, "insert_item");

    public static final StreamCodec<@NotNull RegistryFriendlyByteBuf, @NotNull InsertItemPacket> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            InsertItemPacket::playerId,
            Identifier.STREAM_CODEC,
            InsertItemPacket::slotId,
            ItemStack.OPTIONAL_STREAM_CODEC,
            InsertItemPacket::stack,
            InsertItemPacket::new
    );

    public static final Type<@NotNull InsertItemPacket> TYPE = new Type<>(ID);

    @Override
    public @NotNull Type<?> type() { return TYPE; }
}
