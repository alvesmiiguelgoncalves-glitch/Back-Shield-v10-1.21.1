package net.totemslot.client;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.client.TrinketRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;
import net.totemslot.slot.TotemSlot;

/**
 * Desenha o totem pendurado na cintura, preso ao torso (body.rotate) para acompanhar agachar/nadar/voar,
 * balancando ao andar. Referencial do modelo: Y para BAIXO, +Z para TRAS; por isso o item leva uma rotacao de
 * 180 graus em Z para ficar em pe, e depois e descido para "pendurar" pelo topo.
 */
@Environment(EnvType.CLIENT)
public final class TotemRenderer implements TrinketRenderer {

    /** Metade da altura de um item plano no modo FIXED (escala 0.5 do vanilla). */
    private static final float HALF_ITEM_HEIGHT = 0.25F;

    @Override
    public void render(ItemStack stack, SlotReference slotReference, EntityModel<? extends LivingEntity> contextModel,
                       MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, LivingEntity entity,
                       float limbAngle, float limbDistance, float tickDelta, float animationProgress,
                       float headYaw, float headPitch) {
        if (!(contextModel instanceof BipedEntityModel<?> biped)) {
            return;
        }
        // O renderizador vale por item; so desenha quando o item esta no NOSSO slot.
        if (!TotemSlot.SLOT.equals(slotReference.inventory().getSlotType().getName())) {
            return;
        }
        TotemConfig.Settings c = TotemConfig.get();

        float sway = (float) Math.sin(limbAngle * 0.6662F) * limbDistance * c.swing
                + (float) Math.sin(animationProgress * 0.08F) * c.idle;

        matrices.push();
        biped.body.rotate(matrices);

        matrices.translate(c.x, c.y, c.z);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(c.rotX + sway));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(c.rotY));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0F + c.rotZ));
        matrices.scale(c.scale, c.scale, c.scale);
        // Depois do giro de 180 em Z, "para baixo" e -Y: o centro do item desce meia altura, e o topo fica no pivo.
        matrices.translate(0.0F, -HALF_ITEM_HEIGHT, 0.0F);

        MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformationMode.FIXED, light,
                OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, entity.getWorld(), entity.getId());
        matrices.pop();
    }
}
