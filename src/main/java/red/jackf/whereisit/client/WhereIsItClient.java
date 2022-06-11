package red.jackf.whereisit.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import red.jackf.whereisit.compat.WhereIsItREICompat;
import red.jackf.whereisit.mixins.AccessorAbstractContainerScreen;
import red.jackf.whereisit.search.SearchCriteriaRegistry;
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
        // Hovering over item and pressing Y on a stack
        ScreenEvents.BEFORE_INIT.register((client, _screen, scaledWidth, scaledHeight) -> ScreenKeyboardEvents.afterKeyPress(_screen).register((screen, key, scancode, modifiers) -> {
            if (SEARCH_KEY.matches(key, scancode)) {
                boolean preciseSearch = Screen.hasShiftDown();

                if (screen instanceof AbstractContainerScreen<?> containerScreen) {
                    // Standard Inventory Stacks
                    var stack = getHoveredInventoryStack(containerScreen);
                    if (stack != null) {
                        SearchRequest.fromItemStack(stack, preciseSearch).trigger();
                        return;
                    }
                }

                if (FabricLoader.getInstance().isModLoaded("roughlyenoughitems")) {
                    // Item List / Favourite List
                    var reiStack = WhereIsItREICompat.getOverlayStack();
                    if (reiStack != null) {
                        SearchRequest.fromItemStack(reiStack, preciseSearch).trigger();
                        return;
                    }

                    // Recipe Screen
                    var recipeStacks = WhereIsItREICompat.getRecipeStacks(screen, preciseSearch);
                    if (!recipeStacks.isEmpty()) {
                        new SearchRequest.Builder()
                            .withCriteria(SearchCriteriaRegistry.ITEMS_KEY, SearchCriteriaRegistry.ITEMS.fromItems(recipeStacks))
                            .build()
                            .trigger();
                    }
                }
            }
        }));
    }

    @Nullable
    private static ItemStack getHoveredInventoryStack(AbstractContainerScreen<?> screen) {
        Slot hoveredSlot = ((AccessorAbstractContainerScreen) screen).getHoveredSlot();
        if (hoveredSlot != null && hoveredSlot.hasItem()) {
            return hoveredSlot.getItem();
        } else {
            return null;
        }
    }
}
