package slimeknights.tconstruct.library.events.teleport;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import slimeknights.tconstruct.compat.neoforged.bus.api.Cancelable;

/** Event fired when {@link slimeknights.tconstruct.shared.TinkerEffects#returning} teleport triggers */
@Cancelable
public class ReturningTeleportEvent extends EntityTeleportEvent {
  public ReturningTeleportEvent(LivingEntity entity, double targetX, double targetY, double targetZ) {
    super(entity, (ServerLevel) entity.level(), targetX, targetY, targetZ);
  }
}
