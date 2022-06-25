package red.jackf.whereisit;

import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import red.jackf.whereisit.command.WhereIsItCommand;
import red.jackf.whereisit.command.argument.ItemTagArgument;
import red.jackf.whereisit.config.WhereIsItConfig;
import red.jackf.whereisit.search.SearchCriteriaRegistry;
import red.jackf.whereisit.search.SearchExecutor;
import red.jackf.whereisit.search.SearchRequest;

public class WhereIsIt implements ModInitializer {
	public static final String MOD_ID = "whereisit";
	public static final ResourceLocation CHANNEL_ID = id("main");
	public static ResourceLocation id(String path) {
		return new ResourceLocation(MOD_ID, path);
	}

	public static WhereIsItConfig CONFIG;

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		CONFIG = AutoConfig.register(WhereIsItConfig.class, WhereIsItConfig::getSerializer).get();
		SearchCriteriaRegistry.init();
		WhereIsItCommand.setup();
		ItemTagArgument.register();

		ServerPlayNetworking.registerGlobalReceiver(CHANNEL_ID, (server, player, handler, buf, response) -> {
			var predicate = SearchRequest.fromByteBuf(buf);
			server.execute(() -> {
				var results = SearchExecutor.search(player, (ServerLevel) player.level, predicate);
				System.out.println(results);
			});
		});
	}
}
