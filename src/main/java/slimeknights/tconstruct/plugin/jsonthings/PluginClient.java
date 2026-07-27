package slimeknights.tconstruct.plugin.jsonthings;

import net.minecraft.world.item.Item;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.client.model.TinkerItemProperties;
import slimeknights.tconstruct.library.client.model.tools.ToolModel;

/** Handles anything that requires clientside class loading */
public class PluginClient {
  public static void init() {
    // The removed runtime ItemColor system no longer applies; tool tints are baked into the model quads (see ToolModel).
    slimeknights.tconstruct.TConstruct.getModBus().addListener(PluginClient::clientSetup);
  }

  private static void clientSetup(FMLClientSetupEvent event) {
    event.enqueueWork(() -> {
      for (Item item : FlexItemTypes.TOOL_ITEMS) {
        TinkerItemProperties.registerToolProperties(item);
      }
      for (Item item : FlexItemTypes.CROSSBOW_ITEMS) {
        TinkerItemProperties.registerCrossbowProperties(item);
      }
      for (Item item : FlexItemTypes.ARMOR_ITEMS) {
        TinkerItemProperties.registerBrokenProperty(item);
      }
    });
  }
}
