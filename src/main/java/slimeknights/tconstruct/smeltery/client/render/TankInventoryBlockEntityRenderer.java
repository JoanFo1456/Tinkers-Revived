package slimeknights.tconstruct.smeltery.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import slimeknights.mantle.client.render.FluidCuboid;
import slimeknights.mantle.client.render.RenderItem;
import slimeknights.mantle.client.render.RenderingHelper;
import slimeknights.tconstruct.common.config.Config;
import slimeknights.tconstruct.library.client.RenderUtils;
import slimeknights.tconstruct.library.fluid.FluidTankAnimated;
import slimeknights.tconstruct.smeltery.block.entity.ITankBlockEntity.ITankInventoryBlockEntity;

import java.util.List;

public class TankInventoryBlockEntityRenderer<T extends BlockEntity & ITankInventoryBlockEntity> implements BlockEntityRenderer<T, BlockEntityRenderState> {
  private final EnumProperty<Direction> directionProperty;
  public TankInventoryBlockEntityRenderer(EnumProperty<Direction> directionProperty) {
    this.directionProperty = directionProperty;
  }

  public BlockEntityRenderState createRenderState() {
    return new BlockEntityRenderState();
  }

  @Override
  public void submit(BlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
    // 26.1: immediate-mode render replaced by submit against SubmitNodeCollector. Fetch the (dynamic) fluid from the live
    // block entity via the render state's position, then submit the fluid cuboids as custom geometry.
    net.minecraft.world.level.Level level = net.minecraft.client.Minecraft.getInstance().level;
    if (level == null || Config.CLIENT.tankFluidModel.get()) {
      return;
    }
    BlockState blockState = level.getBlockState(state.blockPos);
    List<FluidCuboid> fluids = FluidCuboid.REGISTRY.get(blockState, List.of());
    if (fluids.isEmpty() || !(level.getBlockEntity(state.blockPos) instanceof ITankInventoryBlockEntity tankBE)) {
      return;
    }
    FluidTankAnimated tank = tankBE.getTank();
    if (tank.getFluid().isEmpty() || tank.getCapacity() <= 0) {
      return;
    }
    int light = state.lightCoords;
    float offset = tank.getRenderOffset();
    // rotate to face the block's direction, matching the pre-26.1 renderer
    boolean isRotated = RenderingHelper.applyRotation(poseStack, blockState.getValue(directionProperty));
    collector.submitCustomGeometry(poseStack, slimeknights.mantle.client.render.MantleRenderTypes.FLUID, (pose, buffer) -> {
      PoseStack local = new PoseStack();
      local.last().pose().set(pose.pose());
      for (FluidCuboid cube : fluids) {
        slimeknights.mantle.client.render.FluidRenderer.renderScaledCuboid(local, buffer, cube, tank.getFluid(), offset, tank.getCapacity(), light, true);
      }
    });
    if (isRotated) {
      poseStack.popPose();
    }
  }
}
