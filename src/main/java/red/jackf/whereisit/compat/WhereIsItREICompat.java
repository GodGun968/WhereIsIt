package red.jackf.whereisit.compat;

import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.gui.screen.DisplayScreen;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Obtain the hovered stack in REI, or tag on recipe screens.
 */
@Environment(EnvType.CLIENT)
public class WhereIsItREICompat {
    /**
     * Get a hovered stack from the item list, or null if not applicable.
     */
    @Nullable
    public static ItemStack getEntryListStack() {
        var reiInstance = REIRuntime.getInstance();
        if (!reiInstance.isOverlayVisible()) return null;
        var overlay = reiInstance.getOverlay();
        if (overlay.isEmpty()) return null;

        var entryListStack = overlay.get().getEntryList().getFocusedStack();
        if (entryListStack.getType() == VanillaEntryTypes.ITEM) {
            return entryListStack.castValue();
        }

        return null;
    }

    @Nullable
    public static ItemStack getFavouriteListStack() {
        var reiInstance = REIRuntime.getInstance();
        if (!reiInstance.isOverlayVisible()) return null;
        var overlay = reiInstance.getOverlay();
        if (overlay.isEmpty()) return null;

        var favouriteList = overlay.get().getFavoritesList();
        if (favouriteList.isPresent()) {
            var favouriteListStack = favouriteList.get().getFocusedStack();
            if (favouriteListStack.getType() == VanillaEntryTypes.ITEM) {
                return favouriteListStack.castValue();
            }
        }

        // TODO: support REI's Display recipes in the favourites menu
        return null;
    }

    /**
     * Gets a hovered entry from a recipe screen; this can return multiple in some cases, or the workbenches.
     */
    public static List<Item> getRecipeStacks(Screen screen, boolean onlyDisplayed) {
        if (screen instanceof DisplayScreen displayScreen) {
            double gameScale = (double) Minecraft.getInstance().getWindow().getGuiScaledWidth() / (double) Minecraft.getInstance().getWindow().getWidth();
            double mouseX = Minecraft.getInstance().mouseHandler.xpos() * gameScale;
            double mouseY = Minecraft.getInstance().mouseHandler.ypos() * gameScale;
            for (Slot slot : Widgets.<Slot>walk(screen.children(), listener -> listener instanceof Slot slot && slot.containsMouse(mouseX, mouseY))) {
                if (onlyDisplayed) {
                    if (slot.getCurrentEntry().getType() == VanillaEntryTypes.ITEM) {
                        return Collections.singletonList(slot.getCurrentEntry().<ItemStack>castValue().getItem());
                    }
                } else {
                    List<Item> entries = new ArrayList<>();
                    for (EntryStack<?> stack : slot.getEntries()) {
                        if (stack.getType() == VanillaEntryTypes.ITEM) {
                            entries.add(stack.<ItemStack>castValue().getItem());
                        }
                    }
                    if (entries.size() > 0) return entries;
                }
            }
        }
        return Collections.emptyList();
    }
}
