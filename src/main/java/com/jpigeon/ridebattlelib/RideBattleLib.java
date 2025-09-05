package com.jpigeon.ridebattlelib;

import com.jpigeon.ridebattlelib.core.system.attachment.AttachmentHandler;
import com.jpigeon.ridebattlelib.core.system.attachment.RiderAttachments;
import com.jpigeon.ridebattlelib.core.system.belt.BeltHandler;
import com.jpigeon.ridebattlelib.core.system.henshin.RiderRegistry;
import com.jpigeon.ridebattlelib.core.system.henshin.handler.KeyHandler;
import com.jpigeon.ridebattlelib.core.system.henshin.handler.TriggerItemHandler;
import com.jpigeon.ridebattlelib.core.system.network.handler.PacketHandler;
import com.jpigeon.ridebattlelib.core.system.penalty.PenaltyHandler;
import com.jpigeon.ridebattlelib.example.ExampleBasic;
import com.jpigeon.ridebattlelib.example.ExampleDynamicForm;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(RideBattleLib.MODID)
public class RideBattleLib {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "ridebattlelib";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public RideBattleLib(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(PacketHandler::register);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (RideBattleLib) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(KeyHandler.class);
        NeoForge.EVENT_BUS.register(BeltHandler.class);
        NeoForge.EVENT_BUS.register(TriggerItemHandler.class);
        NeoForge.EVENT_BUS.register(AttachmentHandler.class);
        NeoForge.EVENT_BUS.register(PenaltyHandler.class);
        RiderAttachments.RIDER_ATTACHMENTS.register(modEventBus);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("请确保骑士初始化在ClientSetup中哦~");
            ExampleBasic.init();
            ExampleDynamicForm.init();

            event.enqueueWork(() -> RiderRegistry.getRegisteredRiders().forEach(config -> {
                if (config.getDriverItem() == null) {
                    LOGGER.error("骑士 {} 未设置驱动器物品!", config.getRiderId());
                }
            }));
        }
    }
}
