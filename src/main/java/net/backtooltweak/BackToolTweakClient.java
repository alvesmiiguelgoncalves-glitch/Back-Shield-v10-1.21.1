package net.backtooltweak;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class BackToolTweakClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("backtooltweak");

    private static final SuggestionProvider<FabricClientCommandSource> ITEM_SUGGEST =
            (ctx, b) -> CommandSource.suggestIdentifiers(TweakConfig.ids(), b);
    private static final SuggestionProvider<FabricClientCommandSource> FIELD_SUGGEST =
            (ctx, b) -> CommandSource.suggestMatching(Arrays.asList(TweakEntry.FIELDS), b);

    @Override
    public void onInitializeClient() {
        TweakConfig.load();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
                literal("backtool")
                        .then(literal("list").executes(ctx -> {
                            msg(ctx, "Itens configurados: " + TweakConfig.ITEMS.size());
                            TweakConfig.ITEMS.forEach((k, v) -> msg(ctx, k + "  ->  " + v.summary()));
                            return 1;
                        }))
                        .then(literal("reload").executes(ctx -> {
                            TweakConfig.load();
                            msg(ctx, "Config recarregada.");
                            return 1;
                        }))
                        .then(literal("add")
                                .executes(ctx -> {
                                    ItemStack held = ctx.getSource().getPlayer().getMainHandStack();
                                    if (held.isEmpty()) { msg(ctx, "Segure um item ou use /backtool add <item>."); return 0; }
                                    return add(ctx, Registries.ITEM.getId(held.getItem()));
                                })
                                .then(argument("item", IdentifierArgumentType.identifier())
                                        .executes(ctx -> add(ctx, ctx.getArgument("item", Identifier.class)))))
                        .then(literal("remove")
                                .then(argument("item", IdentifierArgumentType.identifier()).suggests(ITEM_SUGGEST)
                                        .executes(ctx -> {
                                            Identifier id = ctx.getArgument("item", Identifier.class);
                                            boolean ok = TweakConfig.ITEMS.remove(id.toString()) != null;
                                            TweakConfig.save();
                                            msg(ctx, ok ? "Removido: " + id : "Item nao estava na lista.");
                                            return ok ? 1 : 0;
                                        })))
                        .then(literal("show")
                                .then(argument("item", IdentifierArgumentType.identifier()).suggests(ITEM_SUGGEST)
                                        .executes(ctx -> {
                                            Identifier id = ctx.getArgument("item", Identifier.class);
                                            TweakEntry t = TweakConfig.ITEMS.get(id.toString());
                                            if (t == null) { msg(ctx, "Item nao esta na lista. Use /backtool add."); return 0; }
                                            msg(ctx, id + "  ->  " + t.summary());
                                            return 1;
                                        })))
                        .then(literal("reset")
                                .then(argument("item", IdentifierArgumentType.identifier()).suggests(ITEM_SUGGEST)
                                        .executes(ctx -> {
                                            Identifier id = ctx.getArgument("item", Identifier.class);
                                            TweakEntry t = TweakConfig.ITEMS.get(id.toString());
                                            if (t == null) { msg(ctx, "Item nao esta na lista."); return 0; }
                                            t.reset();
                                            TweakConfig.save();
                                            msg(ctx, "Zerado: " + id);
                                            return 1;
                                        })))
                        .then(literal("set")
                                .then(argument("item", IdentifierArgumentType.identifier()).suggests(ITEM_SUGGEST)
                                        .then(argument("field", StringArgumentType.word()).suggests(FIELD_SUGGEST)
                                                .then(argument("value", FloatArgumentType.floatArg())
                                                        .executes(ctx -> change(ctx, false))))))
                        .then(literal("nudge")
                                .then(argument("item", IdentifierArgumentType.identifier()).suggests(ITEM_SUGGEST)
                                        .then(argument("field", StringArgumentType.word()).suggests(FIELD_SUGGEST)
                                                .then(argument("delta", FloatArgumentType.floatArg())
                                                        .executes(ctx -> change(ctx, true))))))
        ));
    }

    private static int add(CommandContext<FabricClientCommandSource> ctx, Identifier id) {
        if (!Registries.ITEM.containsId(id)) { msg(ctx, "Item inexistente: " + id); return 0; }
        TweakConfig.getOrCreate(id);
        TweakConfig.save();
        msg(ctx, "Adicionado: " + id + ". Ajuste com /backtool set ou pelo Mod Menu.");
        return 1;
    }

    private static int change(CommandContext<FabricClientCommandSource> ctx, boolean nudge) {
        Identifier id = ctx.getArgument("item", Identifier.class);
        if (!Registries.ITEM.containsId(id)) { msg(ctx, "Item inexistente: " + id); return 0; }
        String field = StringArgumentType.getString(ctx, "field");
        float v = FloatArgumentType.getFloat(ctx, nudge ? "delta" : "value");
        TweakEntry t = TweakConfig.getOrCreate(id);
        if (nudge) v += t.get(field);
        if (!t.set(field, v)) { msg(ctx, "Campo invalido. Use: " + String.join(", ", TweakEntry.FIELDS)); return 0; }
        TweakConfig.save();
        msg(ctx, id + "  ->  " + t.summary());
        return 1;
    }

    private static void msg(CommandContext<FabricClientCommandSource> ctx, String s) {
        ctx.getSource().sendFeedback(Text.literal(s));
    }
}
