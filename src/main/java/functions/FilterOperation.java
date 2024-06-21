package functions;

import java.util.Random;

public class FilterOperation {
    public static float[] greyScale(float[] rgb) {
        float mean = (rgb[0] + rgb[1] + rgb[2]) / 3;
        rgb[0] = mean;
        rgb[1] = mean;
        rgb[2] = mean;
        return rgb;

    }

    public static float[] onlyRed(float[] rgb) {
        rgb[1] = 0;
        rgb[2] = 0;
        return rgb;

    }

    public static float[] onlyGreen(float[] rgb) {
        rgb[0] = 0;
        rgb[2] = 0;
        return rgb;

    }

    public static float[] onlyBlue(float[] rgb) {
        rgb[0] = 0;
        rgb[1] = 0;
        return rgb;

    }

    public static float[] sepia(float[] rgb) {
        Random random = new Random();
        float rand = random.nextFloat() * 50/255;
        rgb[0] += rand;
        rgb[1] += rand;
        rgb[2] += rand;
        for (int i = 0; i < rgb.length; i++) {
            if (rgb[i] > 1) {
                rgb[i] = 1;
            }
        }
        return rgb;

    }
}
