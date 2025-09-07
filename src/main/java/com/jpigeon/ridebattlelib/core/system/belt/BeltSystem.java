package com.jpigeon.ridebattlelib.core.system.belt;

import com.jpigeon.ridebattlelib.RideBattleLib;
import com.jpigeon.ridebattlelib.api.IBeltSystem;
import com.jpigeon.ridebattlelib.core.system.attachment.RiderAttachments;
import com.jpigeon.ridebattlelib.core.system.attachment.RiderData;
import com.jpigeon.ridebattlelib.core.system.event.ItemInsertionEvent;
import com.jpigeon.ridebattlelib.core.system.event.ReturnItemsEvent;
import com.jpigeon.ridebattlelib.core.system.event.SlotExtractionEvent;
import com.jpigeon.ridebattlelib.core.system.henshin.RiderConfig;
import com.jpigeon.ridebattlelib.core.system.henshin.RiderRegistry;
import com.jpigeon.ridebattlelib.core.system.network.handler.PacketHandler;
import com.jpigeon.ridebattlelib.core.system.network.packet.BeltDataDiffPacket;
import com.jpigeon.ridebattlelib.core.system.network.packet.BeltDataSyncPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;

import java.util.*;

/*
 * 腰带系统
 * 管理腰带内部物品的存储和操作
 * insertItem 存入物品
 * extractItem 提取物品
 * returnItems 退回所有物品
 */
public class BeltSystem implements IBeltSystem {
    public static final BeltSystem INSTANCE = new BeltSystem();

    //====================核心方法====================

    @Override
    public boolean insertItem(Player player, ResourceLocation slotId, ItemStack stack) {
        if (stack.isEmpty() || stack.getCount() <= 0) {
            RideBattleLib.LOGGER.error("无法插入无效物品");
            return false;
        }

        RiderConfig config = RiderConfig.findActiveDriverConfig(player);
        if (config == null) return false;

        ItemInsertionEvent.Pre preInsertion = new ItemInsertionEvent.Pre(player, slotId, stack, config);
        NeoForge.EVENT_BUS.post(preInsertion);

        stack = preInsertion.getStack();

        if (preInsertion.isCanceled()) return false;

        // 阻止驱动器物品
        if (stack.is(config.getDriverItem()) || stack.is(config.getAuxDriverItem())) {
            return false;
        }

        // 阻止触发物品
        if (stack.is(config.getTriggerItem())) {
            return false;
        }

        boolean isAuxSlot = config.getAuxSlotDefinitions().containsKey(slotId);
        SlotDefinition slot = isAuxSlot ?
                config.getAuxSlotDefinition(slotId) :
                config.getSlotDefinition(slotId);

        if (slot == null || !slot.allowedItems().contains(stack.getItem())) {
            return false;
        }

        RiderData data = player.getData(RiderAttachments.RIDER_DATA);
        // 确保我们操作的是副本，而不是只读Map
        Map<ResourceLocation, ItemStack> mainItems = new HashMap<>(data.getBeltItems(config.getRiderId()));
        Map<ResourceLocation, ItemStack> auxItems = new HashMap<>(data.auxBeltItems.getOrDefault(config.getRiderId(), new HashMap<>()));

        Map<ResourceLocation, ItemStack> targetMap = isAuxSlot ? auxItems : mainItems;

        // 检查槽位是否被占用
        if (targetMap.containsKey(slotId)) {
            ItemStack existing = targetMap.get(slotId);
            if (!existing.isEmpty()) {
                if (slot.allowReplace()) {
                    // 返还旧物品
                    returnItemToPlayer(player, existing.copyWithCount(1));

                    // 插入新物品
                    ItemStack toInsert = stack.copyWithCount(1);
                    targetMap.put(slotId, toInsert);

                    // 更新数据存储
                    data.setBeltItems(config.getRiderId(), mainItems);
                    data.setAuxBeltItems(config.getRiderId(), auxItems);

                    syncBeltData(player);
                    stack.shrink(1);
                    return true;
                }
                return false;
            }
        }

        // 插入新物品
        targetMap.put(slotId, stack.copyWithCount(1));
        cleanInvalidStacks(mainItems);
        cleanInvalidStacks(auxItems);
        data.setBeltItems(config.getRiderId(), mainItems);
        data.setAuxBeltItems(config.getRiderId(), auxItems);

        syncBeltData(player);

        ItemInsertionEvent.Post postEvent = new ItemInsertionEvent.Post(player, slotId, stack, config);
        NeoForge.EVENT_BUS.post(postEvent);

        return true;
    }

    @Override
    public ItemStack extractItem(Player player, ResourceLocation slotId) {
        RiderData data = player.getData(RiderAttachments.RIDER_DATA);
        RiderConfig config = RiderConfig.findActiveDriverConfig(player);
        if (config == null) return ItemStack.EMPTY;

        boolean isAuxSlot = config.getAuxSlotDefinitions().containsKey(slotId);
        Map<ResourceLocation, ItemStack> targetMap = isAuxSlot ?
                new HashMap<>(data.auxBeltItems.getOrDefault(config.getRiderId(), new HashMap<>())) :
                new HashMap<>(data.getBeltItems(config.getRiderId()));

        // 先检查物品是否存在
        if (!targetMap.containsKey(slotId) || targetMap.get(slotId).isEmpty()) {
            return ItemStack.EMPTY;
        }

        // 获取物品副本用于返回
        ItemStack extracted = targetMap.get(slotId).copy();

        // 使用内部方法执行提取
        boolean success = extractItemInternal(player, slotId, targetMap, config, isAuxSlot);

        return success ? extracted : ItemStack.EMPTY;
    }

    @Override
    public void returnItems(Player player) {
        RiderData data = player.getData(RiderAttachments.RIDER_DATA);
        RiderConfig config = RiderConfig.findActiveDriverConfig(player);
        if (config == null) return;

        // 创建事件用于检查是否应该取消返还
        ReturnItemsEvent.Pre preReturn = new ReturnItemsEvent.Pre(player, config);
        NeoForge.EVENT_BUS.post(preReturn);

        // 如果事件被取消，直接返回
        if (preReturn.isCanceled()) {
            RideBattleLib.LOGGER.debug("返还所有物品事件被取消");
            return;
        }

        // 返还主驱动器物品
        Map<ResourceLocation, ItemStack> mainItems = new HashMap<>(data.getBeltItems(config.getRiderId()));
        // 创建副本用于迭代，避免并发修改异常
        List<ResourceLocation> mainSlots = new ArrayList<>(mainItems.keySet());

        for (ResourceLocation slotId : mainSlots) {
            extractItemInternal(player, slotId, mainItems, config, false);
        }

        // 返还辅助驱动器物品
        Map<ResourceLocation, ItemStack> auxItems = new HashMap<>(data.auxBeltItems.getOrDefault(config.getRiderId(), new HashMap<>()));
        // 创建副本用于迭代，避免并发修改异常
        List<ResourceLocation> auxSlots = new ArrayList<>(auxItems.keySet());

        for (ResourceLocation slotId : auxSlots) {
            extractItemInternal(player, slotId, auxItems, config, true);
        }

        // 触发返还完成事件
        ReturnItemsEvent.Post postReturn = new ReturnItemsEvent.Post(player, config);
        NeoForge.EVENT_BUS.post(postReturn);
    }

    private void returnItemToPlayer(Player player, ItemStack stack) {
        if (!player.addItem(stack.copy())) {
            player.drop(stack.copy(), false);
        }
    }

    /**
     * 内部提取方法，包含提取物品的核心逻辑
     * @return 如果成功提取并返还给玩家，返回 true；如果事件被取消，返回 false
     */
    private boolean extractItemInternal(Player player, ResourceLocation slotId,
                                        Map<ResourceLocation, ItemStack> targetMap,
                                        RiderConfig config, boolean isAuxSlot) {
        RiderData data = player.getData(RiderAttachments.RIDER_DATA);

        // 先检查物品是否存在
        if (!targetMap.containsKey(slotId) || targetMap.get(slotId).isEmpty()) {
            return false;
        }

        // 获取物品副本用于事件
        ItemStack extracted = targetMap.get(slotId).copy();

        // 创建事件并发布
        SlotExtractionEvent.Pre preExtraction = new SlotExtractionEvent.Pre(player, slotId, extracted.copy(), config);
        NeoForge.EVENT_BUS.post(preExtraction);

        // 如果事件被取消，直接返回 false，不执行任何操作
        if (preExtraction.isCanceled()) {
            RideBattleLib.LOGGER.debug("提取事件被取消，物品保留在腰带中");
            return false;
        }

        // 从腰带中移除物品
        targetMap.remove(slotId);
        extracted = preExtraction.getExtractedStack();

        if (extracted.isEmpty()) {
            // 更新数据
            if (isAuxSlot) {
                data.setAuxBeltItems(config.getRiderId(), targetMap);
            } else {
                data.setBeltItems(config.getRiderId(), targetMap);
            }
            syncBeltData(player);
            return false;
        }

        returnItemToPlayer(player, extracted);

        // 更新数据
        if (isAuxSlot) {
            data.setAuxBeltItems(config.getRiderId(), targetMap);
        } else {
            data.setBeltItems(config.getRiderId(), targetMap);
        }

        syncBeltData(player);

        SlotExtractionEvent.Post postEvent = new SlotExtractionEvent.Post(player, slotId, extracted, config);
        NeoForge.EVENT_BUS.post(postEvent);

        return true;
    }

    //====================检测方法====================

    @Override
    public boolean validateItems(Player player, ResourceLocation riderId) {
        RiderConfig config = RiderRegistry.getRider(riderId);
        if (config == null) return false;

        for (ResourceLocation slotId : config.getRequiredSlots()) {
            // 如果是辅助槽位且未装备辅助驱动器，则跳过验证
            if (config.getAuxSlotDefinitions().containsKey(slotId)) {
                // 辅助槽位仅在装备辅助驱动器时验证
                if (!config.hasAuxDriverEquipped(player)) {
                    RideBattleLib.LOGGER.info("跳过辅助槽位{}验证（未装备驱动器）", slotId);
                    continue;
                }
            }

            ItemStack item = getBeltItems(player).get(slotId);
            SlotDefinition slot = config.getSlotDefinition(slotId);

            // 详细日志输出
            RideBattleLib.LOGGER.info("验证槽位: {} | 物品: {} | 允许物品: {}",
                    slotId, item.getItem(), slot.allowedItems()
            );

            if (item.isEmpty() || !slot.allowedItems().contains(item.getItem())) {
                return false;
            }
        }
        return true;
    }


    //====================Getters====================

    @Override
    public Map<ResourceLocation, ItemStack> getBeltItems(Player player) {
        RiderData data = player.getData(RiderAttachments.RIDER_DATA);

        // 根据当前激活的骑士获取腰带数据
        RiderConfig config = RiderConfig.findActiveDriverConfig(player);
        if (config == null) return new HashMap<>();

        Map<ResourceLocation, ItemStack> allItems = new HashMap<>(data.getBeltItems(config.getRiderId()));

        // 只在装备辅助驱动器时添加辅助槽位
        if (config.hasAuxDriverEquipped(player)) {
            for (ResourceLocation slotId : config.getAuxSlotDefinitions().keySet()) {
                ItemStack item = data.getAuxBeltItems(config.getRiderId(), slotId);
                if (!item.isEmpty()) {
                    allItems.put(slotId, item);
                }
            }
        }
        return allItems;
    }

    //====================网络通信方法====================

    public void syncBeltData(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            RiderData data = player.getData(RiderAttachments.RIDER_DATA);
            RiderConfig config = RiderConfig.findActiveDriverConfig(player);
            if (config == null) return;

            // 分别发送主/辅助驱动器数据
            Map<ResourceLocation, ItemStack> mainItems = data.getBeltItems(config.getRiderId());
            Map<ResourceLocation, ItemStack> auxItems = data.auxBeltItems.getOrDefault(config.getRiderId(), new HashMap<>());

            PacketHandler.sendToClient(serverPlayer, new BeltDataSyncPacket(
                    player.getUUID(),
                    new HashMap<>(mainItems),
                    new HashMap<>(auxItems)
            ));
        }
    }

    // 客户端应用同步包
    public void applySyncPacket(BeltDataSyncPacket packet) {
        Player player = findPlayer(packet.playerId());
        if (player == null) return;

        RiderData oldData = player.getData(RiderAttachments.RIDER_DATA);
        RiderConfig config = RiderConfig.findActiveDriverConfig(player);
        if (config == null) return;

        ResourceLocation riderId = config.getRiderId();

        // 创建新数据
        Map<ResourceLocation, Map<ResourceLocation, ItemStack>> newRiderBeltItems =
                new HashMap<>(oldData.mainBeltItems);
        Map<ResourceLocation, Map<ResourceLocation, ItemStack>> newAuxBeltItems =
                new HashMap<>(oldData.auxBeltItems);

        // 更新主驱动器数据
        newRiderBeltItems.put(riderId, new HashMap<>(packet.mainItems()));

        // 更新辅助驱动器数据
        newAuxBeltItems.put(riderId, new HashMap<>(packet.auxItems()));

        player.setData(RiderAttachments.RIDER_DATA,
                new RiderData(
                        newRiderBeltItems,
                        newAuxBeltItems,
                        oldData.getTransformedData(),
                        oldData.getHenshinState(),
                        oldData.getPendingFormId(),
                        oldData.getPenaltyCooldownEnd(),
                        oldData.getCurrentSkillIndex()
                )
        );
    }

    // 客户端应用差异包
    public void applyDiffPacket(BeltDataDiffPacket packet) {
        Player player = findPlayer(packet.playerId());
        if (player == null) return;

        RiderData oldData = player.getData(RiderAttachments.RIDER_DATA);
        RiderConfig config = RiderConfig.findActiveDriverConfig(player);
        if (config == null) return;
        ResourceLocation riderId = config.getRiderId();

        // 创建新数据（深拷贝）
        Map<ResourceLocation, Map<ResourceLocation, ItemStack>> newRiderBeltItems = new HashMap<>();
        Map<ResourceLocation, Map<ResourceLocation, ItemStack>> newAuxBeltItems = new HashMap<>();

        // 复制主驱动器数据
        oldData.mainBeltItems.forEach((id, items) ->
                newRiderBeltItems.put(id, new HashMap<>(items))
        );

        // 复制辅助驱动器数据
        oldData.auxBeltItems.forEach((id, items) ->
                newAuxBeltItems.put(id, new HashMap<>(items))
        );

        // 获取当前骑士的主驱动器腰带数据
        Map<ResourceLocation, ItemStack> currentMainItems =
                new HashMap<>(newRiderBeltItems.getOrDefault(riderId, new HashMap<>()));

        // 获取当前骑士的辅助驱动器腰带数据
        Map<ResourceLocation, ItemStack> currentAuxItems =
                new HashMap<>(newAuxBeltItems.getOrDefault(riderId, new HashMap<>()));

        // 应用变更
        if (packet.fullSync()) {
            // 分离主驱动器和辅助驱动器数据
            Map<ResourceLocation, ItemStack> mainChanges = new HashMap<>();
            Map<ResourceLocation, ItemStack> auxChanges = new HashMap<>();

            for (Map.Entry<ResourceLocation, ItemStack> entry : packet.changes().entrySet()) {
                if (config.getSlotDefinitions().containsKey(entry.getKey())) {
                    mainChanges.put(entry.getKey(), entry.getValue());
                } else if (config.getAuxSlotDefinitions().containsKey(entry.getKey())) {
                    auxChanges.put(entry.getKey(), entry.getValue());
                }
            }

            currentMainItems = mainChanges;
            currentAuxItems = auxChanges;
        } else {
            Map<ResourceLocation, ItemStack> finalMainItems = currentMainItems;
            Map<ResourceLocation, ItemStack> finalAuxItems = currentAuxItems;

            packet.changes().forEach((slotId, stack) -> {
                // 根据槽位类型分离数据
                if (config.getSlotDefinitions().containsKey(slotId)) {
                    if (stack.isEmpty()) {
                        finalMainItems.remove(slotId);
                    } else {
                        finalMainItems.put(slotId, stack);
                    }
                } else if (config.getAuxSlotDefinitions().containsKey(slotId)) {
                    if (stack.isEmpty()) {
                        finalAuxItems.remove(slotId);
                    } else {
                        finalAuxItems.put(slotId, stack);
                    }
                }
            });
        }

        // 更新数据
        newRiderBeltItems.put(riderId, currentMainItems);
        newAuxBeltItems.put(riderId, currentAuxItems);

        player.setData(RiderAttachments.RIDER_DATA,
                new RiderData(
                        newRiderBeltItems,
                        newAuxBeltItems,
                        oldData.getTransformedData(),
                        oldData.getHenshinState(),
                        oldData.getPendingFormId(),
                        oldData.getPenaltyCooldownEnd(),
                        oldData.getCurrentSkillIndex()
                )
        );
    }

    private Player findPlayer(UUID playerId) {
        if (Minecraft.getInstance().level == null) return null;
        return Minecraft.getInstance().level.getPlayerByUUID(playerId);
    }

    private void cleanInvalidStacks(Map<ResourceLocation, ItemStack> items) {
        items.entrySet().removeIf(entry ->
                entry.getValue().isEmpty() ||
                        entry.getValue().getCount() <= 0
        );
    }
}
