package net.fabricmc.memorydmp.client.renderer;


import net.fabricmc.memorydmp.items.OrbitalTNTCannons.StabShotBobberEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

public class StabShotBobberRenderer extends EntityRenderer<StabShotBobberEntity> {
    private final ItemRenderer itemRenderer;

    public StabShotBobberRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(StabShotBobberEntity bobber, float yaw, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light) {

        matrices.push();

        // move to entity position
        matrices.translate(0.0D, 0.0D, 0.0D);

        // render as an item (like the vanilla fishing bobber)
        itemRenderer.renderItem(Items.FISHING_ROD.getDefaultStack(),
                net.minecraft.client.render.model.json.ModelTransformation.Mode.GROUND,
                light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, 0);

        matrices.pop();

        super.render(bobber, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    @Override
    public Identifier getTexture(StabShotBobberEntity entity) {
        // optional fallback, not used if we render the item
        return new Identifier("minecraft", "textures/entity/fishing_hook.png");
    }
}
