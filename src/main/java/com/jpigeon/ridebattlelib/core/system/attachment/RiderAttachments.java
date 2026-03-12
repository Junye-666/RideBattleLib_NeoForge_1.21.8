package com.jpigeon.ridebattlelib.core.system.attachment;

import com.jpigeon.ridebattlelib.RideBattleLib;
import com.jpigeon.ridebattlelib.core.system.henshin.helper.HenshinState;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.function.Supplier;

public class RiderAttachments {
    public static final DeferredRegister<@NotNull AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, RideBattleLib.MODID);

    public static final Supplier<AttachmentType<@NotNull RiderData>> RIDER_DATA =
            ATTACHMENTS.register("rider_data",
                    () -> AttachmentType.builder(() -> new RiderData(
                            new HashMap<>(),
                                    new HashMap<>(),
                                    null,
                                    HenshinState.IDLE,
                                    null,
                                    0,
                                    0
                            ))
                            .serialize(RiderData.CODEC.fieldOf("rider_data"))
                            .build());
}
