package chanceCubes.blocks;

import chanceCubes.items.CCubesItems;
import chanceCubes.items.ItemChanceCube;
import chanceCubes.network.CCubesPacketHandler;
import chanceCubes.network.PacketTriggerD20;
import chanceCubes.tileentities.TileChanceD20;
import chanceCubes.util.RewardsUtil;
import chanceCubes.util.StatsRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public class BlockChanceD20 extends BaseChanceBlock implements EntityBlock
{

	public BlockChanceD20()
	{
		super(getBuilder().strength(-1f, Integer.MAX_VALUE).lightLevel(state -> 7).noOcclusion(), "chance_icosahedron");
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
		return pBlockEntityType == CCubesBlocks.TILE_CHANCE_ICOSAHEDRON ? TileChanceD20::tick : null;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return new TileChanceD20(pos, state);
	}

	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext)
	{
		TileChanceD20 d20 = (TileChanceD20) pLevel.getBlockEntity(pPos);
		if (d20 != null) {
			double wave = (double) d20.wave * 16.0D;
			return Block.box(2.0D, 0.5D + wave, 2.0D, 14.0D, 15.5D + wave, 14.0D);
		} else {
			return Block.box(2.0D, 0.5D, 2.0D, 14.0D, 15.5D, 14.0D);
		}
	}

	@Override
	public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		return Block.box(2.0D, 0.5D, 2.0D, 14.0D, 15.5D, 14.0D);
	}

	@Override
	public boolean hasDynamicShape() {
		return true;
	}

	@Override
	public void attack(BlockState state, Level level, BlockPos pos, Player player) {
		this.startd20(level, pos, player);
	}

	@Override
	public RenderShape getRenderShape(BlockState state)
	{
		// I don't know how else to make it so the block doesn't render when placed down, but still provides
		// the OBJModel to singleRenderBlock
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result)
	{
		return this.startd20(level, pos, player) ? InteractionResult.PASS : InteractionResult.FAIL;
	}

	public boolean startd20(Level level, BlockPos pos, Player player)
	{
		if(level.isClientSide() || player == null || player instanceof FakePlayer)
			return true;

		if(level.getBlockEntity(pos) instanceof TileChanceD20 te)
		{
			if(!player.getInventory().getSelected().isEmpty() && player.getInventory().getSelected().getItem().equals(CCubesItems.silkPendant))
			{
				ItemStack stack = new ItemStack(CCubesItems.CHANCE_ICOSAHEDRON, 1);
				((ItemChanceCube) stack.getItem()).setChance(stack, te.isScanned() ? te.getChance() : -101);
				popResource(level, pos, stack);
				level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
				level.removeBlockEntity(pos);
				return false;
			}

			RewardsUtil.executeCommand((ServerLevel) level, player, player.getOnPos(), "/advancement grant @p only chancecubes:chance_icosahedron");
			player.awardStat(StatsRegistry.OPENED_D20);
			te.startBreaking(player);
			CCubesPacketHandler.CHANNEL.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(pos.getX(), pos.getY(), pos.getZ(), 50, level.dimension())), new PacketTriggerD20(pos));
		}
		return false;
	}
}