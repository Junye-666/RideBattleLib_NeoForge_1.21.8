package com.jpigeon.ridebattlelib.core;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    public static final KeyMapping.Category RIDEBATTLE_CATEGORY = new KeyMapping.Category(
            ResourceLocation.parse("ridebattlelib")
    );
    public static final KeyMapping UNHENSHIN_KEY = new KeyMapping(
            "key.ridebattlelib.unhenshin",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            RIDEBATTLE_CATEGORY
    );
    public static final KeyMapping DRIVER_KEY = new KeyMapping(
            "key.ridebattlelib.driver_activate",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            RIDEBATTLE_CATEGORY
    );
    public static final KeyMapping RETURN_ITEMS_KEY = new KeyMapping(
            "key.ridebattlelib.return_items",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
            RIDEBATTLE_CATEGORY
    );
    public static final KeyMapping SKILL_KEY = new KeyMapping(
            "key.ridebattlelib.skill",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            RIDEBATTLE_CATEGORY
    );
}
