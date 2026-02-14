package com.jpigeon.ridebattlelib.core.system.network.packet;

import com.jpigeon.ridebattlelib.RideBattleLib;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record DriverDataDiffPacket(
        UUID playerId,
        Map<Identifier, ItemStack> changes,
        boolean fullSync
) implements CustomPacketPayload {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(RideBattleLib.MODID, "driver_diff_sync");

    public static final Type<@NotNull DriverDataDiffPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<@NotNull RegistryFriendlyByteBuf, @NotNull DriverDataDiffPacket> STREAM_CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC,
                    DriverDataDiffPacket::playerId,
                    createChangesCodec(),
                    DriverDataDiffPacket::changes,
                    ByteBufCodecs.BOOL,
                    DriverDataDiffPacket::fullSync,
                    DriverDataDiffPacket::new
            );

    private static StreamCodec<@NotNull RegistryFriendlyByteBuf, @NotNull Map<Identifier, ItemStack>> createChangesCodec() {
        return StreamCodec.of(
                (buf, changes) -> {
                    buf.writeVarInt(changes.size());
                    for (Map.Entry<Identifier, ItemStack> entry : changes.entrySet()) {
                        Identifier.STREAM_CODEC.encode(buf, entry.getKey());

                        if (entry.getValue().isEmpty()) {
                            buf.writeBoolean(false);
                        } else {
                            buf.writeBoolean(true);
                            ItemStack.STREAM_CODEC.encode(buf, entry.getValue());
                        }
                    }
                },
                buf -> {
                    Map<Identifier, ItemStack> changes = new HashMap<>();
                    int size = buf.readVarInt();
                    for (int i = 0; i < size; i++) {
                        Identifier slotId = Identifier.STREAM_CODEC.decode(buf);
                        boolean hasItem = buf.readBoolean();

                        if (hasItem) {
                            ItemStack stack = ItemStack.STREAM_CODEC.decode(buf);
                            changes.put(slotId, stack);
                        } else {
                            changes.put(slotId, ItemStack.EMPTY);
                        }
                    }
                    return changes;
                }
        );
    }

    @Override
    public @NotNull Type<?> type() { return TYPE; }
}
