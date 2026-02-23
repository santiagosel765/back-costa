package com.ferrisys.common.util;

import java.text.Normalizer;
import java.util.Locale;

public final class ModuleKeyNormalizer {

    private ModuleKeyNormalizer() {
    }

    public static String normalize(String key) {
        if (key == null) {
            return null;
        }

        return stripAccents(key)
                .trim()
                .replaceAll("[^A-Za-z0-9]+", "_")
                .replaceAll("^_+|_+$", "")
                .toUpperCase(Locale.ROOT);
    }

    private static String stripAccents(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
    }
}
