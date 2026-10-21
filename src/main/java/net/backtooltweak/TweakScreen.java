package net.backtooltweak;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.Map;

/** Tela de config (Cloth Config): uma aba por item, com x, y, z, rotX, rotY, rotZ, scale, visivel e espelhar. */
public final class TweakScreen {
    private TweakScreen() {}

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("Back Tool Tweak"))
                .setSavingRunnable(TweakConfig::save);
        ConfigEntryBuilder eb = builder.entryBuilder();

        for (Map.Entry<String, TweakEntry> e : TweakConfig.ITEMS.entrySet()) {
            TweakEntry t = e.getValue();
            String name = e.getKey().startsWith("antarchy:") ? e.getKey().substring("antarchy:".length()) : e.getKey();
            ConfigCategory c = builder.getOrCreateCategory(Text.literal(name));

            c.addEntry(eb.startBooleanToggle(Text.literal("Visivel"), t.visible)
                    .setDefaultValue(true).setSaveConsumer(v -> t.visible = v).build());
            c.addEntry(eb.startBooleanToggle(Text.literal("Espelhar lado"), t.mirror)
                    .setDefaultValue(false).setSaveConsumer(v -> t.mirror = v).build());
            c.addEntry(f(eb, "x (lado, px)", t.x, -200, 200, v -> t.x = v));
            c.addEntry(f(eb, "y (altura, px)", t.y, -200, 200, v -> t.y = v));
            c.addEntry(f(eb, "z (profundidade, px)", t.z, -200, 200, v -> t.z = v));
            c.addEntry(f(eb, "rotX (graus)", t.rotX, -360, 360, v -> t.rotX = v));
            c.addEntry(f(eb, "rotY (graus)", t.rotY, -360, 360, v -> t.rotY = v));
            c.addEntry(f(eb, "rotZ (graus)", t.rotZ, -360, 360, v -> t.rotZ = v));
            c.addEntry(f(eb, "scale (0 = normal, 0.5 = 50% maior)", t.scale, -0.95f, 10, v -> t.scale = v));
        }
        return builder.build();
    }

    private static me.shedaniel.clothconfig2.api.AbstractConfigListEntry<Float> f(
            ConfigEntryBuilder eb, String label, float value, float min, float max,
            java.util.function.Consumer<Float> save) {
        return eb.startFloatField(Text.literal(label), value)
                .setDefaultValue(0f).setMin(min).setMax(max).setSaveConsumer(save).build();
    }
}
