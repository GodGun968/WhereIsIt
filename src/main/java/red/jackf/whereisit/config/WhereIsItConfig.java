package red.jackf.whereisit.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.ConfigSerializer;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Jankson;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import red.jackf.whereisit.WhereIsIt;

import java.util.List;

@Config(name = WhereIsIt.MOD_ID)
public class WhereIsItConfig implements ConfigData {

    @Comment("Used for config updates between versions - do not change here.")
    @ConfigEntry.Gui.Excluded
    public int configVersion = 1;

    @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
    public ClientOptions client = new ClientOptions();

    @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
    public ServerOptions server = new ServerOptions();

    public static class ClientOptions {
        @Comment("Highlight fade time in ticks (20 ticks = 1 second)")
        @ConfigEntry.BoundedDiscrete(min = 40, max = 600)
        public int highlightFadeTime = 200;
    }

    public static class ServerOptions {
        @Comment("Search radius in blocks")
        @ConfigEntry.BoundedDiscrete(min = 8, max = 64)
        public int searchRange = 32;

        @Comment("Command names - removing all of these disables the command.")
        public List<String> commandNames = List.of("whereis", "wi", "find");
    }

    public static <T extends ConfigData> ConfigSerializer<T> getSerializer(Config configDefinition, Class<T> configClass) {
        Jankson jankson = new Jankson.Builder().build();
        return new JanksonConfigSerializer<>(configDefinition, configClass, jankson);
    }
}
