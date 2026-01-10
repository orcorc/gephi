package org.gephi.utils;

import java.awt.Color;

public class ColorUtils {

    public static Color darken(Color color, double factor) {
        if (factor >= 1.0) {
            throw new IllegalArgumentException("Factor must be < 1.0 to darken");
        }
        return darkenOrLighten(color, factor);
    }

    public static Color lighten(Color color, double factor) {
        if (factor <= 1.0) {
            throw new IllegalArgumentException("Factor must be > 1.0 to lighten");
        }
        return darkenOrLighten(color, factor);
    }

    private static Color darkenOrLighten(Color color, double factor) {
        // factor < 1.0 makes it darker, > 1.0 would make it lighter
        int r = (int) Math.round(color.getRed()   * factor);
        int g = (int) Math.round(color.getGreen() * factor);
        int b = (int) Math.round(color.getBlue()  * factor);

        // Clamp to valid range [0, 255]
        r = Math.min(255, Math.max(0, r));
        g = Math.min(255, Math.max(0, g));
        b = Math.min(255, Math.max(0, b));

        // Preserve alpha
        return new Color(r, g, b, color.getAlpha());
    }
}
