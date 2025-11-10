package net.fabricmc.memorydmp.mixin;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
@Mixin(ItemRenderer.class)
public class ItemRendererMixin {

    /**
     * 1.18.2 method signature:
     * void renderGuiItemOverlay(MatrixStack matrices, TextRenderer textRenderer, ItemStack stack, int x, int y, String countLabel)
     */
    @Inject(method = "renderGuiItemOverlay", at = @At("HEAD"), cancellable = true)
    private void renderGuiItemOverlayHook(MatrixStack matrices, TextRenderer textRenderer, ItemStack stack, int x, int y, String countLabel, CallbackInfo ci) {
        if (stack == null || stack.isEmpty()) return;

        int count = stack.getCount();
        if (count <= 64) return;

        String text = Integer.toString(count);
        int textWidth = textRenderer.getWidth(text);
        int textX = x + 16 - textWidth + 1;
        int textY = y + 16 - 6;

        textRenderer.drawWithShadow(matrices, text, (float) textX, (float) textY, 0xFFFFFF);
        ci.cancel();
    }
}
