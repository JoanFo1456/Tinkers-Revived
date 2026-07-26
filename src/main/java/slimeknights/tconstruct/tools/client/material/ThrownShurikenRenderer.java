package slimeknights.tconstruct.tools.client.material;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.projectile.Projectile;
import slimeknights.tconstruct.tools.entity.ThrownShuriken;
import slimeknights.tconstruct.tools.entity.ToolProjectile;

/**
 * Renderer for {@link ThrownShuriken}.
 * Minimal placeholder shape pending the EntityRenderer render-state system rewrite; the spinning item submission is deferred.
 */
public class ThrownShurikenRenderer<T extends Projectile & ToolProjectile> extends EntityRenderer<T, EntityRenderState> {
  private final ItemRenderer itemRenderer;
  public ThrownShurikenRenderer(EntityRendererProvider.Context context) {
    super(context);
    this.itemRenderer = context.getItemRenderer();
  }

  @Override
  public EntityRenderState createRenderState() {
    return new EntityRenderState();
  }
}
