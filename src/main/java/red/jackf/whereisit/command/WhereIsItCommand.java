package red.jackf.whereisit.command;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import red.jackf.whereisit.WhereIsIt;

import static net.minecraft.commands.Commands.literal;

public class WhereIsItCommand {

    public static void setup() {
        CommandRegistrationCallback.EVENT.register((dispatcher, commandBuildContext, environment) -> {
            var names = WhereIsIt.CONFIG.server.commandNames;
            if (names.size() == 0) return;

            var root = literal(names.get(0))
                .build();

            dispatcher.getRoot().addChild(root);
        });
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        System.out.println(context.getInput());
        return 0;
    }
}
