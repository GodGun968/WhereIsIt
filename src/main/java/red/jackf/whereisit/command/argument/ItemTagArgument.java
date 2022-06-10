package red.jackf.whereisit.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import red.jackf.whereisit.WhereIsIt;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class ItemTagArgument implements ArgumentType<TagKey<Item>> {
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_TAG = new DynamicCommandExceptionType(
        object -> Component.translatable("whereisit.arguments.unknownItemTag", object)
    );

    public static void register() {
        ArgumentTypeRegistry.registerArgumentType(WhereIsIt.id("item_tag"), ItemTagArgument.class, SingletonArgumentInfo.contextFree(ItemTagArgument::itemTag));
    }

    public static ItemTagArgument itemTag() {
        return new ItemTagArgument();
    }

    @Override
    public TagKey<Item> parse(StringReader reader) throws CommandSyntaxException {
        var id = ResourceLocation.read(reader);
        var key = TagKey.create(Registry.ITEM_REGISTRY, id);
        if (Registry.ITEM.isKnownTagName(key)) return key;
        throw ERROR_UNKNOWN_TAG.create(id);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggestResource(Registry.ITEM.getTagNames().map(TagKey::location), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return Arrays.asList("#logs", "#minecraft:anvil");
    }
}
