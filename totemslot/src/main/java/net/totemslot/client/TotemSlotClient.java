package net.totemslot.client;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.emi.trinkets.api.client.TrinketRendererRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.command.CommandSource;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

public class TotemSlotClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        TotemConfig.load();

        // O Trinkets so desenha itens que tem renderizador. As tags ainda nao existem aqui (so carregam ao
        // entrar no mundo), entao registramos para todo item que ainda nao tem um (o renderizador so desenha no
        // slot de totem). Itens com renderizador proprio de outro mod continuam com o deles.
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            TotemRenderer renderer = new TotemRenderer();
            for (Item item : Registries.ITEM) {
                if (TrinketRendererRegistry.getRenderer(item).isEmpty()) {
                    TrinketRendererRegistry.registerRenderer(item, renderer);
                }
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("totemslot")
                        .then(ClientCommandManager.literal("show").executes(ctx -> {
                            ctx.getSource().sendFeedback(Text.literal(TotemConfig.describe()));
                            return 1;
                        }))
                        .then(ClientCommandManager.literal("reset").executes(ctx -> {
                            TotemConfig.reset();
                            ctx.getSource().sendFeedback(Text.literal("Totem Slot: valores padrao restaurados"));
                            return 1;
                        }))
                        .then(ClientCommandManager.literal("reload").executes(ctx -> {
                            TotemConfig.load();
                            ctx.getSource().sendFeedback(Text.literal("Totem Slot: config recarregada"));
                            return 1;
                        }))
                        .then(ClientCommandManager.literal("set")
                                .then(ClientCommandManager.argument("field", StringArgumentType.word())
                                        .suggests((ctx, builder) -> CommandSource.suggestMatching(TotemConfig.FIELDS, builder))
                                        .then(ClientCommandManager.argument("value", FloatArgumentType.floatArg())
                                                .executes(ctx -> {
                                                    String field = StringArgumentType.getString(ctx, "field");
                                                    float value = FloatArgumentType.getFloat(ctx, "value");
                                                    boolean ok = TotemConfig.set(field, value);
                                                    ctx.getSource().sendFeedback(Text.literal(ok
                                                            ? field + " = " + value
                                                            : "Campo desconhecido: " + field));
                                                    return ok ? 1 : 0;
                                                }))))));
    }
}
