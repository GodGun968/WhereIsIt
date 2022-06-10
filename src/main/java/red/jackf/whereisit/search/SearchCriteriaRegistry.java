package red.jackf.whereisit.search;

import net.minecraft.resources.ResourceLocation;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.search.criteria.InTagSearchCriteria;
import red.jackf.whereisit.search.criteria.ItemIdSearchCriteria;
import red.jackf.whereisit.search.criteria.NameSearchCriteria;
import red.jackf.whereisit.search.criteria.SearchCriteria;

import java.util.HashMap;
import java.util.Map;

public abstract class SearchCriteriaRegistry {
    public static Map<ResourceLocation, SearchCriteria> CRITERIA = new HashMap<>();

    public static ResourceLocation ITEM_ID_KEY = WhereIsIt.id("item");
    public static ItemIdSearchCriteria ITEM_ID = new ItemIdSearchCriteria();
    public static ResourceLocation IN_TAG_KEY = WhereIsIt.id("tag");
    public static InTagSearchCriteria IN_TAG = new InTagSearchCriteria();
    public static ResourceLocation NAME_KEY = WhereIsIt.id("name");
    public static NameSearchCriteria NAME = new NameSearchCriteria();

    public static void init() {
        CRITERIA.put(ITEM_ID_KEY, ITEM_ID);
        CRITERIA.put(IN_TAG_KEY, IN_TAG);
        CRITERIA.put(NAME_KEY, NAME);
    }
}
