package slimeknights.tconstruct.common.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import slimeknights.tconstruct.common.data.tags.BiomeTagProvider;
import slimeknights.tconstruct.common.data.tags.BlockEntityTypeTagProvider;
import slimeknights.tconstruct.common.data.tags.BlockTagProvider;
import slimeknights.tconstruct.common.data.tags.DamageTypeTagProvider;
import slimeknights.tconstruct.common.data.tags.EnchantmentTagProvider;
import slimeknights.tconstruct.common.data.tags.EntityTypeTagProvider;
import slimeknights.tconstruct.common.data.tags.FluidTagProvider;
import slimeknights.tconstruct.common.data.tags.ItemTagProvider;
import slimeknights.tconstruct.common.data.tags.MaterialTagProvider;
import slimeknights.tconstruct.common.data.tags.MenuTypeTagProvider;
import slimeknights.tconstruct.common.data.tags.ModifierTagProvider;
import slimeknights.tconstruct.common.data.tags.PotionTagProvider;
import slimeknights.tconstruct.tools.data.StationSlotLayoutProvider;
import slimeknights.tconstruct.tools.data.material.MaterialDataProvider;
import slimeknights.tconstruct.tools.data.material.MaterialStatsDataProvider;
import slimeknights.tconstruct.tools.data.material.MaterialTraitsDataProvider;
import slimeknights.tconstruct.world.data.MobEquipmentProvider;

import java.util.concurrent.CompletableFuture;

/**
 * Central registration of the SERVER-side data providers.
 * <p>
 * 26.1 split {@code GatherDataEvent} into {@code GatherDataEvent.Server} and {@code GatherDataEvent.Client}; the server
 * event runs via the {@code runServerData} run. Only providers that regenerate world-load data (tags, and later loot,
 * recipes, advancements, material/tool JSON) are registered here. The CLIENT model/sprite providers are wired
 * separately once ported to the vanilla model provider API.
 */
public final class TinkerServerData {
  private TinkerServerData() {}

  /** Registers all currently ported server data providers. Registered on the mod bus for {@link GatherDataEvent.Server}. */
  public static void gatherData(final GatherDataEvent.Server event) {
    DataGenerator generator = event.getGenerator();
    PackOutput packOutput = generator.getPackOutput();
    CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

    // tags: the block tag provider feeds its contents to the item tag provider for block->item tag copying
    BlockTagProvider blockTags = new BlockTagProvider(packOutput, lookupProvider);
    generator.addProvider(true, blockTags);
    generator.addProvider(true, new ItemTagProvider(packOutput, lookupProvider, blockTags.contentsGetter()));
    generator.addProvider(true, new FluidTagProvider(packOutput, lookupProvider));
    generator.addProvider(true, new EntityTypeTagProvider(packOutput, lookupProvider));
    generator.addProvider(true, new BlockEntityTypeTagProvider(packOutput, lookupProvider));
    generator.addProvider(true, new BiomeTagProvider(packOutput, lookupProvider));
    generator.addProvider(true, new EnchantmentTagProvider(packOutput, lookupProvider));
    generator.addProvider(true, new MenuTypeTagProvider(packOutput, lookupProvider));
    generator.addProvider(true, new PotionTagProvider(packOutput, lookupProvider));
    generator.addProvider(true, new DamageTypeTagProvider(packOutput, lookupProvider));
    generator.addProvider(true, new MaterialTagProvider(packOutput));
    generator.addProvider(true, new ModifierTagProvider(packOutput));

    // material JSON: stats/traits providers consume the material list from the data provider
    MaterialDataProvider materials = new MaterialDataProvider(packOutput);
    generator.addProvider(true, materials);
    generator.addProvider(true, new MaterialStatsDataProvider(packOutput, materials));
    generator.addProvider(true, new MaterialTraitsDataProvider(packOutput, materials));

    // station layouts + mob spawn equipment
    generator.addProvider(true, new StationSlotLayoutProvider(packOutput));
    generator.addProvider(true, new MobEquipmentProvider(packOutput));
  }
}
