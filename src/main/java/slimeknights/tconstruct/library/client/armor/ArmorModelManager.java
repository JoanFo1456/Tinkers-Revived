package slimeknights.tconstruct.library.client.armor;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import net.minecraft.client.model.Model;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.client.armor.texture.ArmorTextureSupplier;
import slimeknights.tconstruct.library.client.materials.MaterialRenderInfo;
import slimeknights.tconstruct.library.client.materials.MaterialRenderInfoLoader;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.tools.client.material.CombatFishingHookRenderer;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ArmorModelManager extends SimpleJsonResourceReloadListener<JsonElement> {
  /** Folder containing the logic */
  public static final String FOLDER = "tinkering/armor_models";

  /** Object representing parsed models */
  public record ArmorModel(List<ArmorTextureSupplier> layers) {
    /** Empty instace for fallback */
    public static final ArmorModel EMPTY = new ArmorModel(List.of());
    /** Loadable for JSON parsing */
    public static final RecordLoadable<ArmorModel> LOADABLE = RecordLoadable.create(ArmorTextureSupplier.LOADER.list(1).requiredField("layers", ArmorModel::layers), ArmorModel::new);
  }

  /* Instance data */
  public static final ArmorModelManager INSTANCE = new ArmorModelManager();
  /** Map of location to texture suppliers */
  private Map<Identifier,ArmorModel> models = Collections.emptyMap();

  private static final List<ArmorModelDispatcher> DISPATCHERS = new ArrayList<>();

  /**
   * Initializes this manager, registering it with the resource manager
   * @param manager  Manager
   */
  public static void init(AddClientReloadListenersEvent manager) {
    manager.addListener(TConstruct.getResource("armor_model_manager"), INSTANCE);
  }

  private ArmorModelManager() {
    super(ExtraCodecs.JSON, FileToIdConverter.json(FOLDER));
  }

  @Override
  protected void apply(Map<Identifier,JsonElement> splashList, ResourceManager manager, ProfilerFiller pProfiler) {
    long time = System.nanoTime();

    // first, load in all fluid textures, means we are allowed to reference them in fluid texture supplier constructors
    ArmorTextureSupplier.TEXTURE_VALIDATOR.onReloadSafe(manager);
    // reuses armor model logic for simplicity
    CombatFishingHookRenderer.clearCache();

    // load all models
    ImmutableMap.Builder<Identifier,ArmorModel> builder = ImmutableMap.builder();
    for (Entry<Identifier,JsonElement> entry : splashList.entrySet()) {
      Identifier key = entry.getKey();
      JsonElement element = entry.getValue();
      try {
        builder.put(key, ArmorModel.LOADABLE.convert(element, key.toString()));
      } catch (JsonSyntaxException e) {
        TConstruct.LOG.error("Failed to load armor model from {}", key, e);
      }
    }

    this.models = builder.build();
    // clear dispatcher model cache
    Set<Identifier> missing = new HashSet<>();
    for (ArmorModelDispatcher dispatcher : DISPATCHERS) {
      dispatcher.model = null;
      Identifier name = dispatcher.getName();
      if (!this.models.containsKey(name)) {
        missing.add(name);
      }
    }
    if (!missing.isEmpty()) {
      TConstruct.LOG.error("Missing armor models used by items: {}", missing);
    }
    TConstruct.LOG.info("Loaded {} armor models in {} ms", models.size(), (System.nanoTime() - time) / 1000000f);
  }

  /** Gets the armor model for the given location. Location typically corresponds to armor material name */
  public ArmorModel getModel(Identifier name) {
    return models.getOrDefault(name, ArmorModel.EMPTY);
  }

  /** Helper to cache armor models in the item */
  public abstract static class ArmorModelDispatcher implements IClientItemExtensions {
    private ArmorModel model;

    public ArmorModelDispatcher() {
      DISPATCHERS.add(this);
    }

    /**
     * Gets the name of the model to use.
     * Not a constructor parameter as forge initializes client extensions before we can store fields from the parent constructor.
     */
    protected abstract Identifier getName();

    /** Fetches the model from the cache */
    protected ArmorModel getModel(ItemStack stack) {
      if (model == null) {
        model = ArmorModelManager.INSTANCE.getModel(getName());
        if (model == ArmorModel.EMPTY) {
          TConstruct.LOG.warn("Failed to find armor model {}, will skip rendering {}", getName(), stack);
        }
      }
      return model;
    }

    @Nonnull
    @Override
    public Model getGenericArmorModel(ItemStack stack, EquipmentClientInfo.LayerType layerType, Model original) {
      // 26.1: worn armor is drawn by the vanilla EquipmentLayerRenderer from the equipment asset (assets/tconstruct/equipment/
      // <set>.json), which submits the vanilla humanoid armor model per layer. We keep the vanilla model and supply the
      // per-material tint via getArmorLayerTintColor below; the layer textures are grayscale masks tinted by that color.
      getModel(stack);
      return original;
    }

    @Override
    public int getArmorLayerTintColor(ItemStack stack, EquipmentClientInfo.Layer layer, int layerIdx, int fallbackColor) {
      // tint each equipment layer by its material's color so the grayscale layer textures show the crafted material.
      // Keyed off the layer texture's leaf name (not its index) since body and leggings layer lists differ (e.g. travelers'
      // "base" only exists on the body). Never return 0 (that hides the layer); -1 renders the layer untinted.
      int materialIndex = layerMaterialIndex(layer.textureId());
      if (materialIndex >= 0) {
        return getMaterialColor(stack, materialIndex);
      }
      return -1;
    }

    /**
     * Maps an equipment layer texture to the tool material index whose color tints it, or -1 for an untinted layer (a
     * fixed base). Keyed by the texture's leaf name, matching the layer naming used by the Tinkers equipment assets:
     * {@code plating}/{@code metal} are the outer material (index 0), {@code maille}/{@code cuirass} the inner (index 1),
     * {@code base} is fixed, and single-material sets (slime) default to index 1.
     */
    protected int layerMaterialIndex(Identifier textureId) {
      String path = textureId.getPath();
      int slash = path.lastIndexOf('/');
      String leaf = slash >= 0 ? path.substring(slash + 1) : path;
      return switch (leaf) {
        case "base" -> -1;
        case "plating", "metal" -> 0;
        case "maille", "cuirass" -> 1;
        default -> 1;
      };
    }

    /** Gets the render color of the tool material at the given index on the stack, or -1 (untinted) if absent. */
    private static int getMaterialColor(ItemStack stack, int index) {
      CompoundTag tag = TagUtil.getTag(stack);
      if (tag != null && tag.contains(ToolStack.TAG_MATERIALS)) {
        String material = tag.getListOrEmpty(ToolStack.TAG_MATERIALS).getString(index).orElse("");
        MaterialVariantId id = MaterialVariantId.tryParse(material);
        if (id != null) {
          return MaterialRenderInfoLoader.INSTANCE.getRenderInfo(id).map(MaterialRenderInfo::vertexColor).orElse(-1);
        }
      }
      return -1;
    }
  }
}
