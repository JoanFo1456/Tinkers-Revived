package slimeknights.tconstruct.tools.item;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.client.armor.ArmorModelManager.ArmorModelDispatcher;
import slimeknights.tconstruct.library.tools.definition.ModifiableArmorMaterial;
import slimeknights.tconstruct.library.tools.item.armor.ModifiableArmorItem;
import net.minecraft.world.item.equipment.ArmorType;

import java.util.function.Consumer;

/** This item is mainly to return the proper model for a slimeskull */
public class SlimeskullItem extends ModifiableArmorItem {
  /** Model ID for our slimeskull. You may want your own for a custom slimeskull */
  public static final Identifier MODEL_LOCATION = TConstruct.getResource("slimeskull");

  private final Identifier name;

  public SlimeskullItem(ModifiableArmorMaterial material, Identifier name, Properties properties) {
    super(material, ArmorType.HELMET, properties);
    this.name = name;
  }

  public SlimeskullItem(ModifiableArmorMaterial material, Properties properties) {
    this(material, material.getId(), properties);
  }

  @Override
  public void initializeClient(Consumer<IClientItemExtensions> consumer) {
    // The custom slimeskull head model is applied through the dispatcher; the per-slot generic model override moved to
    // the data-driven equipment render layer in 26.1 and is wired up during the client armor render pass.
    consumer.accept(new ArmorModelDispatcher() {
      @Override
      protected Identifier getName() {
        return name;
      }
    });
  }
}
