package modernmods.modernfoundry.tables.block.entity.inventory;

import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import modernmods.hilt.block.entity.HiltBlockEntity;

/** Interface for tinker chest TEs */
public interface IChestItemHandler extends IItemHandlerModifiable, ValueIOSerializable, IScalingContainer {
  /** Sets the parent of this block */
  void setParent(HiltBlockEntity parent);
}
