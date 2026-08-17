package modernmods.modernfoundry.world.block;

import com.mojang.serialization.MapCodec;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import modernmods.modernfoundry.common.TinkerTags;

public class SlimeLeavesBlock extends LeavesBlock {
  /** Chance a falling leaf particle spawns per tick, matching vanilla leaves */
  private static final float LEAF_PARTICLE_CHANCE = 0.01F;
  private static final MapCodec<SlimeLeavesBlock> CODEC = simpleCodec(properties -> new SlimeLeavesBlock(properties, FoliageType.EARTH));

  @Getter
  private final FoliageType foliageType;
  public SlimeLeavesBlock(Properties properties, FoliageType foliageType) {
    super(LEAF_PARTICLE_CHANCE, properties);
    this.foliageType = foliageType;
  }

  @Override
  public MapCodec<? extends LeavesBlock> codec() {
    return CODEC;
  }

  @Override
  protected void spawnFallingLeavesParticle(net.minecraft.world.level.Level level, BlockPos pos, RandomSource random) {
    ColorParticleOption particle = ColorParticleOption.create(ParticleTypes.TINTED_LEAVES, 0xFF000000 | foliageType.getColor());
    ParticleUtils.spawnParticleBelow(level, pos, random, particle);
  }

  @Override
  protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
    if (state.getValue(WATERLOGGED)) {
      ticks.scheduleTick(pos, net.minecraft.world.level.material.Fluids.WATER, net.minecraft.world.level.material.Fluids.WATER.getTickDelay(level));
    }
    int distance = getDistance(neighbourState) + 1;
    if (distance != 1 || state.getValue(DISTANCE) != distance) {
      ticks.scheduleTick(pos, this, 1);
    }
    return state;
  }

  @Override
  public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random) {
    worldIn.setBlock(pos, updateDistance(state, worldIn, pos), 3);
  }

  private static BlockState updateDistance(BlockState state, LevelAccessor world, BlockPos pos) {
    int i = 7;

    BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
    for (Direction direction : Direction.values()) {
      mutableBlockPos.set(pos).move(direction);
      i = Math.min(i, getDistance(world.getBlockState(mutableBlockPos)) + 1);
      if (i == 1) {
        break;
      }
    }

    return state.setValue(DISTANCE, i);
  }

  private static int getDistance(BlockState neighbor) {
    if (neighbor.is(TinkerTags.Blocks.SLIMY_LOGS)) {
      return 0;
    } else {
      return neighbor.getBlock() instanceof SlimeLeavesBlock ? neighbor.getValue(DISTANCE) : 7;
    }
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    return updateDistance(this.defaultBlockState().setValue(PERSISTENT, Boolean.TRUE), context.getLevel(), context.getClickedPos());
  }
}
