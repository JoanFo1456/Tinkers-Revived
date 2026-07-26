package slimeknights.tconstruct.tools.client.material;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import slimeknights.tconstruct.tools.entity.ThrownTool;
import slimeknights.tconstruct.tools.entity.ToolProjectile;

/**
 * Renderer for {@link ThrownTool}.
 * Minimal placeholder shape pending the EntityRenderer render-state system rewrite; the thrown item submission is deferred.
 */
public class ThrownToolRenderer<T extends AbstractArrow & ToolProjectile> extends EntityRenderer<T, EntityRenderState> {
  protected final ItemRenderer itemRenderer;
  public ThrownToolRenderer(Context context) {
    super(context);
    this.itemRenderer = context.getItemRenderer();
  }

  @Override
  public EntityRenderState createRenderState() {
    return new EntityRenderState();
  }
}
