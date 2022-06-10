package red.jackf.whereisit.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.command.WhereIsItCommand;
import red.jackf.whereisit.mixins.MixinAbstractContainerScreen;
import red.jackf.whereisit.search.SearchRequest;

@Environment(EnvType.CLIENT)
public class WhereIsItClient implements ClientModInitializer {
    KeyMapping SEARCH_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
        "key.whereisit.search_key",
        InputConstants.Type.KEYSYM,
        InputConstants.KEY_Y,
        "category.whereisit"
    ));

    @Override
    public void onInitializeClient() {
        WhereIsItCommand.setup();

        // Hovering over item and pressing Y on a stack
        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof AbstractContainerScreen<?> containerScreen) {
                ScreenKeyboardEvents.afterKeyPress(containerScreen).register((screen1, key, scancode, modifiers) -> {
                    if (SEARCH_KEY.matches(key, scancode)) {
                        var stack = getHoveredStack(containerScreen);
                        if (stack != null) SearchRequest.fromItemStack(stack, Screen.hasShiftDown());
                    }
                });
            }
        });
    }

    @Nullable
    private static ItemStack getHoveredStack(AbstractContainerScreen<?> screen) {
        Slot hoveredSlot = ((MixinAbstractContainerScreen) screen).getHoveredSlot();
        if (hoveredSlot != null && hoveredSlot.hasItem()) {
            return hoveredSlot.getItem();
        } else {
            return null;
        }
    }
}
