package red.jackf.whereisit;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Jankson;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import red.jackf.whereisit.config.WhereIsItConfig;

public class WhereIsIt implements ModInitializer {
	public static final String MOD_ID = "whereisit";
	public static ResourceLocation id(String path) {
		return new ResourceLocation(MOD_ID, path);
	}

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static WhereIsItConfig CONFIG;

	@Override
	public void onInitialize() {
		CONFIG = AutoConfig.register(WhereIsItConfig.class, WhereIsItConfig::getSerializer).get();
	}
}
