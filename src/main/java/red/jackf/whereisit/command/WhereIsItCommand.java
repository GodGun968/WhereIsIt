package red.jackf.whereisit.command;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.search.SearchCriteriaRegistry;

import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.Commands.argument;

@Environment(EnvType.CLIENT)
public class WhereIsItCommand {

    public static void setup() {
        CommandRegistrationCallback.EVENT.register((dispatcher, commandBuildContext, environment) -> {
            var names = WhereIsIt.CONFIG.server.commandNames;
            if (names.size() == 0) return;

            var root = literal(names.get(0)).build();

            SearchCriteriaRegistry.CRITERIA.forEach((id, criteria) -> {
                var argName = literal(id.getPath())
                    .then(
                        argument(id.getPath(), criteria.getArgumentType(commandBuildContext))
                            .redirect(root))
                    .build();

                root.addChild(argName);
            });

            dispatcher.getRoot().addChild(root);

            for (int i = 1; i < names.size(); i++)
                dispatcher.getRoot().addChild(literal(names.get(i))
                    .redirect(root)
                    .build());
        });
    }
}
