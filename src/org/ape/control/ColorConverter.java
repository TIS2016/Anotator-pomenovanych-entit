package org.ape.control;

import javafx.scene.paint.Color;

public interface ColorConverter {

    static int colorToInt(Color color) {
        int red = Math.round(255 * (float) color.getRed());
        int green = Math.round(255 * (float) color.getGreen());
        int blue = Math.round(255 * (float) color.getBlue());
        red = (red << 16) & 0x00FF0000;
        green = (green << 8) & 0x0000FF00;
        blue = blue & 0x000000FF;
        return 0xFF000000 | red | green | blue;
    }

    static Color intToColor(int color) {
        int red = (color>>16) & 0xFF;
        int green = (color>>8) & 0xFF;
        int blue = color & 0xFF;
        float opacity = ((color>>24) & 0xFF) / 0xFF;
        return Color.rgb(red, green, blue, opacity);
    }

    static String intToStringColor(int color) {
        return String.format("#%06X", color & 0xFFFFFF);
    }
}
