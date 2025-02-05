package chanceCubes.renderer;

import chanceCubes.tileentities.TileChanceD20;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IModelData;

import java.awt.*;
import java.util.Random;

public class TileChanceD20Renderer implements BlockEntityRenderer<TileChanceD20>
{
	private static final Random random = new Random();

	private static final float HOVER_SPEED = 6000F;
	private static final float ROTATION_SPEED = 100F;
	private static final float HALF_SQRT_3 = (float) (Math.sqrt(3.0D) / 2.0D);

	private static BakedModel bakedIcoModel;
	private static IModelData modelData;

	private final BlockRenderDispatcher blockRenderDispatcher;
	private final ModelBlockRenderer modelRenderer;
	private final BlockColors blockColors;

	public TileChanceD20Renderer()
	{
		this.blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();
		this.blockColors = BlockColors.createDefault();
		this.modelRenderer = new net.minecraftforge.client.model.pipeline.ForgeBlockModelRenderer(this.blockColors);
	}

	@Override
	public void render(TileChanceD20 d20, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		random.setSeed(432L);
		VertexConsumer vertexConsumer = bufferIn.getBuffer(RenderType.lightning());
		poseStack.pushPose();

		// cache model
		if (this.modelData == null)
			this.modelData = d20.getModelData();

		Level level = d20.getLevel();

		long gameTime = 0;

		int stage = d20.getStage();
		int maxStage = 200;

		if(level != null)
			gameTime = level.getGameTime();

		float wave;
		if(stage == 0)
			wave = (float) (Math.sin(((gameTime % HOVER_SPEED + partialTicks) / HOVER_SPEED) * 360F) * 0.3f);
		else
			wave = ((stage + partialTicks) / 70f);

		d20.wave = wave;
		float rotation = ((float) gameTime + partialTicks) / (ROTATION_SPEED / ((stage / 20) + 1));
		float f7 = Math.min(rotation > 0.8F ? (rotation - 0.8F) / 0.2F : 0.0F, 1.0F);

		float color = (gameTime % 75) / 75F;
		Color tmpClr = new Color(Color.HSBtoRGB(color, 1F, 1F));
		int r = tmpClr.getRed();
		int g = tmpClr.getGreen();
		int b = tmpClr.getBlue();

		poseStack.translate(0.5D, 0.5D + wave, 0.5D);

		for(int i = 0; (float) i < 5; ++i)
		{

			poseStack.mulPose(Vector3f.XP.rotationDegrees(random.nextFloat() * 360.0F));
			poseStack.mulPose(Vector3f.YP.rotationDegrees(random.nextFloat() * 360.0F));
			poseStack.mulPose(Vector3f.ZP.rotationDegrees(random.nextFloat() * 360.0F));
			poseStack.mulPose(Vector3f.XP.rotationDegrees(random.nextFloat() * 360.0F));
			poseStack.mulPose(Vector3f.YP.rotationDegrees(random.nextFloat() * 360.0F));
			poseStack.mulPose(Vector3f.ZP.rotationDegrees(random.nextFloat() * 360.0F + rotation * 90.0F));
			float beamScale = (float) stage / ((float) maxStage * 10f);
			float f3 = (random.nextFloat() * 20.0F + 5.0F + f7 * 10.0F) * beamScale;
			float f4 = (random.nextFloat() * 2.0F + 1.0F + f7 * 2.0F) * beamScale;
			Matrix4f matrix4f = poseStack.last().pose();
			int alpha = 130;
			vertex01(vertexConsumer, matrix4f, r, g, b, alpha);
			vertex2(vertexConsumer, matrix4f, f3, f4, r, g, b, alpha);
			vertex3(vertexConsumer, matrix4f, f3, f4, r, g, b, alpha);
			vertex01(vertexConsumer, matrix4f, r, g, b, alpha);
			vertex3(vertexConsumer, matrix4f, f3, f4, r, g, b, alpha);
			vertex4(vertexConsumer, matrix4f, f3, f4, r, g, b, alpha);
			vertex01(vertexConsumer, matrix4f, r, g, b, alpha);
			vertex4(vertexConsumer, matrix4f, f3, f4, r, g, b, alpha);
			vertex2(vertexConsumer, matrix4f, f3, f4, r, g, b, alpha);

		}

		poseStack.popPose();

		float scale = Math.max(1.0f - ((float) stage / (float) maxStage), 0.1f);
		poseStack.translate(0.5D, 0.5D + wave, 0.5D);
		poseStack.scale(scale, scale, scale);
		poseStack.mulPose(Vector3f.YP.rotationDegrees(rotation * 360.0F));
		BlockState blockState = d20.getBlockState();
		// render d20 model, based on blockRenderDispatcher.renderSingleBlock()
		// unfortunately if RenderType is set to anything other than RenderType.MODEL it doesn't load the model,
		// so instead this is extracted code
		if (this.bakedIcoModel == null)
			this.bakedIcoModel = blockRenderDispatcher.getBlockModel(blockState);

		int i = this.blockColors.getColor(blockState, (BlockAndTintGetter) null, (BlockPos) null, 0);
		float f = (float)(i >> 16 & 255) / 255.0F;
		float f1 = (float)(i >> 8 & 255) / 255.0F;
		float f2 = (float)(i & 255) / 255.0F;
		modelRenderer.renderModel(poseStack.last(), bufferIn.getBuffer(ItemBlockRenderTypes.getRenderType(blockState, false)), blockState, this.bakedIcoModel, f, f1, f2, combinedLightIn, combinedOverlayIn, this.modelData);

	}

	private static void vertex01(VertexConsumer vc, Matrix4f p_114221_, int r, int g, int b, int alpha)
	{
		vc.vertex(p_114221_, 0.0F, 0.0F, 0.0F).color(r, g, b, alpha).endVertex();
	}

	private static void vertex2(VertexConsumer vc, Matrix4f p_114216_, float p_114217_, float p_114218_, int r, int g, int b, int alpha)
	{
		vc.vertex(p_114216_, -HALF_SQRT_3 * p_114218_, p_114217_, -0.5F * p_114218_).color(r, g, b, 0).endVertex();
	}

	private static void vertex3(VertexConsumer vc, Matrix4f p_114225_, float p_114226_, float p_114227_, int r, int g, int b, int alpha)
	{
		vc.vertex(p_114225_, HALF_SQRT_3 * p_114227_, p_114226_, -0.5F * p_114227_).color(r, g, b, 0).endVertex();
	}

	private static void vertex4(VertexConsumer vc, Matrix4f p_114230_, float p_114231_, float p_114232_, int r, int g, int b, int alpha)
	{
		vc.vertex(p_114230_, 0.0F, p_114231_, 1.0F * p_114232_).color(r, g, b, 0).endVertex();
	}

}