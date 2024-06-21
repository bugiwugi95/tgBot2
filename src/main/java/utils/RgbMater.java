package utils;

import functions.ImageOperation;

import java.awt.image.BufferedImage;

public class RgbMater {


    private final BufferedImage image;
    private final int width;
    private final int height;
    private boolean hasAlphaChannel;
    private int[] pixels;

    public RgbMater(BufferedImage image) {
        this.image = image;
        width = image.getWidth();
        height = image.getHeight();
        hasAlphaChannel = image.getAlphaRaster() != null;
        pixels = image.getRGB(0, 0, width, height, pixels, 0, width);
    }


    public BufferedImage getImage() {
        return image;
    }

    public void changeImage(ImageOperation operation) {
        for (int i = 0; i < pixels.length; i++) {
            float[] pixel = ImageUtils.rgbIntToArray(pixels[i]);
            float[] newPixel = operation.execute(pixel);
            pixels[i] = ImageUtils.arrayToRgbInt(newPixel);

        }
        image.setRGB(0,0,width,height,pixels,0,width);
    }
}
