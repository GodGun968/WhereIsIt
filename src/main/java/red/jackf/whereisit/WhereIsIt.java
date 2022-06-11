package red.jackf.whereisit;

import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.LoggerFactory;
import red.jackf.whereisit.command.WhereIsItCommand;
import red.jackf.whereisit.command.argument.ItemTagArgument;
import red.jackf.whereisit.config.WhereIsItConfig;
import red.jackf.whereisit.search.SearchCriteriaRegistry;

public class WhereIsIt implements ModInitializer {
	public static final String MOD_ID = "whereisit";
	public static ResourceLocation id(String path) {
		return new ResourceLocation(MOD_ID, path);
	}

	public static WhereIsItConfig CONFIG;

	public static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		CONFIG = AutoConfig.register(WhereIsItConfig.class, WhereIsItConfig::getSerializer).get();
		SearchCriteriaRegistry.init();
		WhereIsItCommand.setup();
		ItemTagArgument.register();
	}
}
