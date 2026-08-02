package slimeknights.tconstruct.gadgets.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/**
 * Rail that drops items from a passing hopper minecart into the inventory below it.
 * The NeoForge onMinecartPass rail hook was removed in 26.1, so this reacts via {@link #entityInside}.
 * The item movement below still uses the legacy IItemHandler capability accessors; the item/fluid
 * capabilities were replaced by the new ResourceHandler transfer API in 26.1 (Capabilities.Item returns
 * ResourceHandler&lt;ItemResource&gt;). Migrating this transfer logic is deferred to the capability pass.
 */
public class DropperRailBlock extends RailBlock {

  public DropperRailBlock(Properties properties) {
    super(properties);
  }

  @Override
  protected void entityInside(BlockState state, Level world, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
    if (!(entity instanceof AbstractMinecart cart) || !(entity instanceof Hopper)) {
      return;
    }
    // pull the item handler off the minecart
    IItemHandler itemHandlerCart = Capabilities.ItemHandler.ENTITY_AUTOMATION.getCapability(cart, Direction.UP);
    if (itemHandlerCart == null) {
      return;
    }
    // find the inventory directly below the rail
    BlockEntity below = world.getBlockEntity(pos.below());
    if (below == null) {
      return;
    }
    IItemHandler itemHandlerTE = world.getCapability(Capabilities.ItemHandler.BLOCK, pos.below(), below.getBlockState(), below, Direction.UP);
    if (itemHandlerTE == null) {
      return;
    }

    for (int i = 0; i < itemHandlerCart.getSlots(); i++) {
      ItemStack itemStack = itemHandlerCart.extractItem(i, 1, true);
      if (itemStack.isEmpty()) {
        continue;
      }
      if (ItemHandlerHelper.insertItem(itemHandlerTE, itemStack, true).isEmpty()) {
        itemStack = itemHandlerCart.extractItem(i, 1, false);
        ItemHandlerHelper.insertItem(itemHandlerTE, itemStack, false);
        break;
      }
    }
  }

}
