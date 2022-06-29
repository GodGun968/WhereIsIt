package red.jackf.whereisit.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
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
import red.jackf.whereisit.networking.WhereIsItNetworking;
import red.jackf.whereisit.search.SearchExecutor;
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
        ClientPlayNetworking.registerGlobalReceiver(WhereIsItNetworking.SHOW_FOUND_RESULTS_S2C, ((client, handler1, buf, responseSender) -> {
            var results = SearchExecutor.Result.fromByteBuf(buf);

            client.execute(() -> {
                if (results.positions().size() > 0 && client.level != null) {
                    HighlightRendering.setResults(results, client.level.getGameTime());
                    if (client.screen != null) client.screen.onClose();
                }
            });
        }));

        HighlightRendering.setupEvents();

        // Reset per-server data points
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            SearchRequest.resetShownNotInstalledMessage();
            HighlightRendering.clear();
        });

        // Hovering over item and pressing Y on a stack
        ScreenEvents.BEFORE_INIT.register((client, _screen, scaledWidth, scaledHeight) -> ScreenKeyboardEvents.afterKeyPress(_screen).register((screen, key, scancode, modifiers) -> {
            if (SEARCH_KEY.matches(key, scancode)) {
                boolean alternateBehaviour = Screen.hasShiftDown();

                if (screen instanceof AbstractContainerScreen<?> containerScreen) {
                    // Standard Inventory Stacks
                    var stack = getHoveredInventoryStack(containerScreen);
                    if (stack != null) {
                        SearchRequest.fromItemStack(stack, alternateBehaviour).trigger();
                        return;
                    }
                }

                if (FabricLoader.getInstance().isModLoaded("roughlyenoughitems")) {
                    // Item List
                    var entryListStack = WhereIsItREICompat.getEntryListStack();
                    if (entryListStack != null) {
                        SearchRequest.fromItemStack(entryListStack, alternateBehaviour).trigger();
                        return;
                    }

                    // Favourite List
                    var favouriteListStack = WhereIsItREICompat.getFavouriteListStack();
                    if (favouriteListStack != null) {
                        SearchRequest.fromItemStack(favouriteListStack, !alternateBehaviour).trigger();
                        return;
                    }

                    // Recipe Screen
                    var recipeStacks = WhereIsItREICompat.getRecipeStacks(screen, alternateBehaviour);
                    if (!recipeStacks.isEmpty()) {
                        SearchRequest.fromItemStacks(recipeStacks, alternateBehaviour).trigger();
                        return;
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
