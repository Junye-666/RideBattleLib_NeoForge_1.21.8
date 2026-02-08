package com.jpigeon.ridebattlelib.core.system.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public record TransformedAttachmentData(
        Identifier riderId,
        Identifier formId,
        Map<EquipmentSlot, ItemStack> originalGear,
        Map<Identifier, ItemStack> driverSnapshot
) {
    public static final Codec<TransformedAttachmentData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Identifier.CODEC.fieldOf("riderId").forGetter(TransformedAttachmentData::riderId),
                    Identifier.CODEC.fieldOf("formId").forGetter(TransformedAttachmentData::formId),

                    Codec.unboundedMap(EquipmentSlot.CODEC, ItemStack.OPTIONAL_CODEC)
                            .fieldOf("originalGear").forGetter(TransformedAttachmentData::originalGear),

                    Codec.unboundedMap(Identifier.CODEC, ItemStack.OPTIONAL_CODEC)
                            .fieldOf("driverSnapshot").forGetter(TransformedAttachmentData::driverSnapshot)
            ).apply(instance, TransformedAttachmentData::new)
    );
}
