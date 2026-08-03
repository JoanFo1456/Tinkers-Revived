package slimeknights.tconstruct.common.data;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.data.CachedOutput;
import slimeknights.mantle.recipe.data.FinishedRecipe;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import slimeknights.mantle.data.GenericDataProvider;
import slimeknights.mantle.recipe.data.IRecipeHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.utils.ResourceId;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Shared logic for each module's recipe provider
 */
public abstract class BaseRecipeProvider extends GenericDataProvider implements IRecipeHelper {
  /**
   * 26.1 added a {@code HolderGetter<Item>} as the first argument to the vanilla recipe builders
   * (shaped/shapeless). During datagen the built-in item registry lookup is sufficient.
   */
  protected static final net.minecraft.core.HolderGetter<Item> ITEM_LOOKUP = net.minecraft.core.registries.BuiltInRegistries.ITEM.asLookup();

  public BaseRecipeProvider(PackOutput generator) {
    super(generator, Target.DATA_PACK, "recipes");
    TConstruct.sealTinkersClass(this, "BaseRecipeProvider", "BaseRecipeProvider is trivial to recreate and directly extending can lead to addon recipes polluting our namespace.");
  }

  protected abstract void buildRecipes(Consumer<FinishedRecipe> consumer);

  @Override
  public abstract String getName();

  @Override
  public CompletableFuture<?> run(CachedOutput cache) {
    List<CompletableFuture<?>> tasks = new ArrayList<>();
    buildRecipes(recipe -> tasks.add(saveJson(cache, recipe.getId(), recipe.serializeRecipe())));
    return allOf(tasks);
  }

  protected static Criterion<?> has(ItemLike item) {
    return InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(item).build());
  }

  protected static Criterion<?> has(TagKey<Item> tag) {
    return InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(tag).build());
  }

  @Override
  public String getModId() {
    return TConstruct.MOD_ID;
  }

  /* ResourceId overloads: 26.1 made Identifier final so our ID wrappers (ModifierId/MaterialId/MaterialStatsId)
     no longer ARE Identifiers. These delegate to the IRecipeHelper Identifier variants via getIdentifier(). */

  /** Gets a resource location for the mod prefixed onto the given ID's path */
  protected Identifier prefix(ResourceId location, String prefix) {
    return prefix(location.getIdentifier(), prefix);
  }

  /** Gets a resource location for the mod suffixed onto the given ID's path */
  protected Identifier suffix(ResourceId location, String suffix) {
    return suffix(location.getIdentifier(), suffix);
  }

  /** Gets a resource location for the mod wrapping the given ID's path */
  protected Identifier wrap(ResourceId location, String prefix, String suffix) {
    return wrap(location.getIdentifier(), prefix, suffix);
  }
}
