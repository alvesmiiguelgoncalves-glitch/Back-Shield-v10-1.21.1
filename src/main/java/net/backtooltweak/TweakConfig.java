package net.backtooltweak;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** config/backtooltweak.json  ->  { "antarchy:battle_axe": { x, y, z, rotX, rotY, rotZ, scale, visible, mirror }, ... } */
public final class TweakConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type TYPE = new TypeToken<LinkedHashMap<String, TweakEntry>>() {}.getType();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("backtooltweak.json");

    /** Itens do Antarchy que costumam ir nas costas. Entradas zeradas nao mudam nada ate voce editar. */
    public static final String[] DEFAULT_ITEMS = {
            "battle_axe", "big_bertha", "giant_frying_pan",
            "royal_guardian_sword", "royal_guardian_shield", "royal_assailant_battleaxe", "royal_assailant_staff",
            "ultimate_sword", "ultimate_axe", "ultimate_pickaxe", "ultimate_bow", "ultimate_crossbow", "ultimate_mace",
            "blood_crystal_katana", "blood_gods_blade", "nightmare_sword",
            "scorpion_whip", "mantis_claw", "attitude_adjuster", "krakens_grasp", "eye_of_the_storm",
            "squidzooka", "rpo_launcher", "water_cannon", "shrink_ray", "growth_ray", "gravity_gun",
            "portal_gun", "portal_gun_atlas", "portal_gun_p_body", "portal_gun_craisin", "portal_gun_laynce"
    };

    public static Map<String, TweakEntry> ITEMS = new LinkedHashMap<>();

    private TweakConfig() {}

    public static void load() {
        ITEMS = new LinkedHashMap<>();
        if (Files.exists(FILE)) {
            try (Reader r = Files.newBufferedReader(FILE)) {
                Map<String, TweakEntry> loaded = GSON.fromJson(r, TYPE);
                if (loaded != null) ITEMS.putAll(loaded);
            } catch (Exception e) {
                BackToolTweakClient.LOGGER.error("Falha ao ler {}", FILE, e);
            }
        } else {
            for (String id : DEFAULT_ITEMS) ITEMS.put("antarchy:" + id, new TweakEntry());
            save();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            try (Writer w = Files.newBufferedWriter(FILE)) {
                GSON.toJson(ITEMS, TYPE, w);
            }
        } catch (IOException e) {
            BackToolTweakClient.LOGGER.error("Falha ao salvar {}", FILE, e);
        }
    }

    public static TweakEntry get(Item item) {
        if (ITEMS.isEmpty()) return null;
        return ITEMS.get(Registries.ITEM.getId(item).toString());
    }

    public static TweakEntry getOrCreate(Identifier id) {
        return ITEMS.computeIfAbsent(id.toString(), k -> new TweakEntry());
    }

    public static List<Identifier> ids() {
        List<Identifier> out = new ArrayList<>();
        for (String k : ITEMS.keySet()) {
            Identifier id = Identifier.tryParse(k);
            if (id != null) out.add(id);
        }
        return out;
    }
}
