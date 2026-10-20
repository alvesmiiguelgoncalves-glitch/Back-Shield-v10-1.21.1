package net.totemslot.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Posicao do totem pendurado (somente visual, cliente). Arquivo: config/totemslot-client.json
 *
 *   x, y, z      = ponto onde o totem esta preso, no referencial do torso (blocos; 1 pixel = 0.0625).
 *                  x: lateral (negativo = direita do jogador); y: para BAIXO a partir do pescoco
 *                  (0.70 = cintura); z: para TRAS (negativo = a frente do corpo).
 *   rotX/rotY/rotZ = graus (rotY 180 vira o totem para o outro lado)
 *   scale        = tamanho (1 = totem do tamanho de item de moldura)
 *   swing        = quanto o totem balanca ao andar (graus)
 *   idle         = balanco leve parado (graus)
 */
@Environment(EnvType.CLIENT)
public final class TotemConfig {

    public static final List<String> FIELDS = List.of("x", "y", "z", "rotX", "rotY", "rotZ", "scale", "swing", "idle");

    private static final Logger LOGGER = LoggerFactory.getLogger("totemslot");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("totemslot-client.json");
    private static final Path FILE_TMP = FabricLoader.getInstance().getConfigDir().resolve("totemslot-client.json.tmp");

    private static volatile Settings current = new Settings();

    private TotemConfig() {
    }

    public static final class Settings {
        public float x = -0.2F;
        public float y = 0.7F;
        public float z = -0.17F;
        public float rotX = 0.0F;
        public float rotY = 0.0F;
        public float rotZ = 0.0F;
        public float scale = 0.6F;
        public float swing = 25.0F;
        public float idle = 2.0F;
    }

    public static Settings get() {
        return current;
    }

    public static synchronized void load() {
        Settings loaded = null;
        if (Files.exists(FILE)) {
            try {
                loaded = GSON.fromJson(Files.readString(FILE, StandardCharsets.UTF_8), Settings.class);
            } catch (Exception e) {
                LOGGER.warn("Could not read {} - keeping a copy as totemslot-client.broken.json", FILE, e);
                try {
                    Files.copy(FILE, FILE.resolveSibling("totemslot-client.broken.json"), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ignored) {
                }
            }
        }
        current = loaded != null ? loaded : new Settings();
        save();
    }

    public static synchronized void save() {
        try {
            Files.writeString(FILE_TMP, GSON.toJson(current), StandardCharsets.UTF_8);
            try {
                Files.move(FILE_TMP, FILE, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicUnsupported) {
                Files.move(FILE_TMP, FILE, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            LOGGER.warn("Could not write {}", FILE, e);
        }
    }

    public static synchronized void reset() {
        current = new Settings();
        save();
    }

    public static synchronized boolean set(String field, float value) {
        if (!FIELDS.contains(field)) {
            return false;
        }
        try {
            Field f = Settings.class.getField(field);
            f.setFloat(current, value);
            save();
            return true;
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }

    public static String describe() {
        Settings s = current;
        return "x=" + s.x + " y=" + s.y + " z=" + s.z + " rotX=" + s.rotX + " rotY=" + s.rotY + " rotZ=" + s.rotZ
                + " scale=" + s.scale + " swing=" + s.swing + " idle=" + s.idle;
    }
}
