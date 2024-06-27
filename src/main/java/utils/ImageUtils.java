package utils;

import commands.AppBotCommand;
import commands.BotCommonCommands;
import functions.FilterOperation;
import functions.ImageOperation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ImageUtils {
    public static BufferedImage getImage(String path) throws IOException {
        return ImageIO.read(new File(path));
    }

    public static void saveImage(BufferedImage image, String path) throws IOException {
        ImageIO.write(image, "jpg", new File(path));
    }

    static float[] rgbIntToArray(int pixel) {
        Color color = new Color(pixel);
        return color.getRGBColorComponents(null);
    }

    static int arrayToRgbInt(float[] pixel) {
        Color color = null;
        if (pixel.length == 3) {
            color = new Color(pixel[0], pixel[1], pixel[2]);
        } else if (pixel.length == 4) {
            color = new Color(pixel[0], pixel[1], pixel[2], pixel[3]);
        }
        if (color != null) {
            return color.getRGB();
        }
        throw  new RuntimeException();

    }
   public static ImageOperation getOperation(String opName) {
        FilterOperation filterOperation = new FilterOperation();
        Method[] methodsClass = filterOperation.getClass().getDeclaredMethods();
        for (Method method : methodsClass) {
            if (method.isAnnotationPresent(AppBotCommand.class)) {
                AppBotCommand command = method.getAnnotation(AppBotCommand.class);
                if (command.name().equals(opName)) {
                   return (f) -> {
                       try {
                           return ((float[]) method.invoke(filterOperation,f));
                       } catch (IllegalAccessException | InvocationTargetException e) {
                           throw new RuntimeException(e);
                       }
                   };


                }
            }
        }
        return null;

    }
}
