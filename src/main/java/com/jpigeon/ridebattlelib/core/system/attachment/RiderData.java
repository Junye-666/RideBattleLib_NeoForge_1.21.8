package com.jpigeon.ridebattlelib.core.system.attachment;

import com.jpigeon.ridebattlelib.RideBattleLib;
import com.jpigeon.ridebattlelib.core.system.henshin.helper.HenshinState;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RiderData {
    public final Map<Identifier, Map<Identifier, ItemStack>> mainDriverItems;
    public final Map<Identifier, Map<Identifier, ItemStack>> auxDriverItems;
    private final @Nullable TransformedAttachmentData transformedData;
    private HenshinState henshinState;
    private @Nullable Identifier pendingFormId;
    private long penaltyCooldownEnd;
    private int currentSkillIndex;

    public RiderData(
            Map<Identifier, Map<Identifier, ItemStack>> mainDriverItems,
            Map<Identifier, Map<Identifier, ItemStack>> auxDriverItems,
            @Nullable TransformedAttachmentData transformedData,
            HenshinState henshinState,
            @Nullable Identifier pendingFormId,
            long penaltyCooldownEnd,
            int currentSkillIndex
    ) {
        this.mainDriverItems = mainDriverItems != null ?
                new HashMap<>(mainDriverItems) : new HashMap<>();
        this.auxDriverItems = auxDriverItems != null ?
                new HashMap<>(auxDriverItems) : new HashMap<>();
        this.transformedData = transformedData;
        this.henshinState = henshinState;
        this.pendingFormId = pendingFormId;
        this.penaltyCooldownEnd = penaltyCooldownEnd;
        this.currentSkillIndex = currentSkillIndex;
    }

    //====================Setter方法====================

    public void setAuxDriverItems(Identifier riderId, Map<Identifier, ItemStack> items) {
        if (items == null) {
            auxDriverItems.remove(riderId);
        } else {
            auxDriverItems.put(riderId, new HashMap<>(items));
        }
    }

    public void setHenshinState(HenshinState state) {
        this.henshinState = state;
    }

    public void setPendingFormId(Identifier formId) {
        this.pendingFormId = formId != null ? formId : Identifier.fromNamespaceAndPath(RideBattleLib.MODID, "empty");
    }

    // Setter 方法
    public void setDriverItems(Identifier riderId, Map<Identifier, ItemStack> items) {
        if (items == null) {
            mainDriverItems.remove(riderId);
        } else {
            mainDriverItems.put(riderId, new HashMap<>(items));
        }
    }

    public void setPenaltyCooldownEnd(long endTime) {
        this.penaltyCooldownEnd = endTime;
    }

    public void setCurrentSkillIndex(int index) {
        this.currentSkillIndex = index;
    }

    //====================Getter方法====================

    public Map<Identifier, ItemStack> getDriverItems(Identifier riderId) {
        return mainDriverItems.getOrDefault(riderId, new HashMap<>());
    }

    public ItemStack getAuxDriverItems(Identifier riderId, Identifier slotId) {
        return auxDriverItems.getOrDefault(riderId, Collections.emptyMap())
                .getOrDefault(slotId, ItemStack.EMPTY);
    }

    public @Nullable TransformedAttachmentData getTransformedData() {
        return transformedData;
    }

    public HenshinState getHenshinState() {
        return henshinState;
    }

    public Identifier getPendingFormId() {
        return pendingFormId != null ? pendingFormId : Identifier.fromNamespaceAndPath(RideBattleLib.MODID, "empty");
    }

    public long getPenaltyCooldownEnd() {
        return penaltyCooldownEnd;
    }

    public boolean isInPenaltyCooldown() {
        return System.currentTimeMillis() < penaltyCooldownEnd;
    }

    public int getCurrentSkillIndex() {
        return currentSkillIndex;
    }

    //====================Codec====================

    public static final Codec<RiderData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(

                    Codec.unboundedMap(
                                    Identifier.CODEC,
                                    Codec.unboundedMap(Identifier.CODEC, ItemStack.OPTIONAL_CODEC)
                            ).optionalFieldOf("mainDriverItems", new HashMap<>())
                            .forGetter(data -> data.mainDriverItems),

                    Codec.unboundedMap(Identifier.CODEC,
                                    Codec.unboundedMap(Identifier.CODEC, ItemStack.OPTIONAL_CODEC)
                            ).optionalFieldOf("auxDriverItems", new HashMap<>())
                            .forGetter(data -> data.auxDriverItems),

                    TransformedAttachmentData.CODEC.optionalFieldOf("getTransformedData")
                            .forGetter(data -> Optional.ofNullable(data.transformedData)),


                    HenshinState.CODEC.fieldOf("henshinState")
                            .forGetter(data -> data.henshinState),


                    Identifier.CODEC.optionalFieldOf("pendingFormId")
                            .forGetter(data -> Optional.ofNullable(data.pendingFormId)),

                    Codec.LONG.fieldOf("penaltyCooldownEnd")
                            .forGetter(data -> data.penaltyCooldownEnd),

                    Codec.INT.optionalFieldOf("currentSkillIndex", 0)
                            .forGetter(RiderData::getCurrentSkillIndex)
            ).apply(instance, (riderDriverItems, auxDriverItems, transformedDataOpt, henshinState, pendingFormIdOpt, penaltyCooldownEnd, currentSkillIndex) ->
                    new RiderData(
                            riderDriverItems != null ? riderDriverItems : new HashMap<>(),
                            auxDriverItems,
                            transformedDataOpt.orElse(null),
                            henshinState,
                            pendingFormIdOpt.orElse(null),
                            penaltyCooldownEnd,
                            currentSkillIndex
                    )
            )
    );
}
