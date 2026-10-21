package net.backtooltweak.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapmethod.Operation;
import net.backtooltweak.TweakRender;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Marca o "escopo" em que o BackSlot desenha itens nas costas / cintura.
 * Os alvos sao classes do proprio BackSlot (nomes nao ofuscados), mas os tipos do Minecraft
 * na assinatura estao em intermediary (como o jar do BackSlot e distribuido).
 */
@Mixin(targets = {
        "net.backslot.client.feature.BackToolFeatureRenderer",
        "net.backslot.client.feature.BeltSlotFeatureRenderer"
})
public abstract class BackRenderScopeMixin {
    @WrapMethod(method = "render(Lnet/minecraft/class_4587;Lnet/minecraft/class_4597;ILnet/minecraft/class_742;FFFFFF)V", remap = false)
    private void backtooltweak$scope(MatrixStack matrices, VertexConsumerProvider providers, int light,
                                     AbstractClientPlayerEntity player,
                                     float a, float b, float c, float d, float e, float f,
                                     Operation<Void> original) {
        boolean previous = TweakRender.IN_BACK;
        TweakRender.IN_BACK = true;
        try {
            original.call(matrices, providers, light, player, a, b, c, d, e, f);
        } finally {
            TweakRender.IN_BACK = previous;
        }
    }
}
