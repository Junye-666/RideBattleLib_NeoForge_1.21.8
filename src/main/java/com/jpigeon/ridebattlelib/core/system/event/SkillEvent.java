package com.jpigeon.ridebattlelib.core.system.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;

public class SkillEvent extends Event {
    private final Player player;
    private final ResourceLocation formId;
    private final ResourceLocation skillId;

    public SkillEvent(Player player, ResourceLocation formId, ResourceLocation skillId) {
        this.player = player;
        this.formId = formId;
        this.skillId = skillId;
    }

    // 预触发事件（可取消）
    public static class Pre extends SkillEvent {
        private boolean canceled = false;

        public Pre(Player player, ResourceLocation formId, ResourceLocation skillId) {
            super(player, formId, skillId);
        }

        public boolean isCanceled() { return canceled; }
        public void setCanceled(boolean canceled) { this.canceled = canceled; }
    }

    // 技能触发后事件
    public static class Post extends SkillEvent {
        public Post(Player player, ResourceLocation formId, ResourceLocation skillId) {
            super(player, formId, skillId);
        }
    }

    // Getter方法
    public Player getPlayer() { return player; }
    public ResourceLocation getFormId() { return formId; }
    public ResourceLocation getSkillId() { return skillId; }
}
