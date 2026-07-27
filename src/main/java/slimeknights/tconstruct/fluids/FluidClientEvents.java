package slimeknights.tconstruct.fluids;

import net.minecraft.resources.Identifier;
import slimeknights.tconstruct.compat.minecraft.world.item.alchemy.PotionUtils;
import net.neoforged.api.distmarker.Dist;
// 26.1: RegisterColorHandlersEvent.Item removed (item tints are data-driven now); potion color must be a tint source.
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import slimeknights.mantle.registration.object.FlowingFluidObject;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.ClientEventBase;
import slimeknights.tconstruct.library.client.model.FluidContainerModel;

@EventBusSubscriber(modid = TConstruct.MOD_ID, value = Dist.CLIENT)
public class FluidClientEvents extends ClientEventBase {
  @SubscribeEvent
  static void clientSetup(final FMLClientSetupEvent event) {
    setTranslucent(TinkerFluids.honey);
    // slime
    setTranslucent(TinkerFluids.earthSlime);
    setTranslucent(TinkerFluids.skySlime);
    setTranslucent(TinkerFluids.enderSlime);
    // molten
    setTranslucent(TinkerFluids.moltenDiamond);
    setTranslucent(TinkerFluids.moltenEmerald);
    setTranslucent(TinkerFluids.moltenGlass);
    setTranslucent(TinkerFluids.moltenGlass);
    setTranslucent(TinkerFluids.liquidSoul);
    setTranslucent(TinkerFluids.moltenSoulsteel);
    setTranslucent(TinkerFluids.moltenAmethyst);
  }


  @SubscribeEvent
  static void registerItemModels(net.neoforged.neoforge.client.event.RegisterItemModelsEvent event) {
    event.register(FluidContainerModel.ID, FluidContainerModel.Unbaked.MAP_CODEC);
  }

  private static void setTranslucent(FlowingFluidObject<?> fluid) {
    // 26.1: ItemBlockRenderTypes.setRenderLayer was removed; fluid/block render layers are data-driven now
    // (block render_type in the blockstate/model). Kept as a no-op so the setup call sites still compile;
    // translucency must be declared on the fluid block's model.
  }
}
