package lesson.java;

import commands.AppBotCommand;
import commands.BotCommonCommands;
import functions.FilterOperation;
import functions.ImageOperation;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import utils.ImageUtils;
import utils.PhotoMessage;


import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static utils.PhotoMessage.processingImage;

public class Bot extends TelegramLongPollingBot {


    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        try {
            SendMessage responseTextMessage = runCommonCommand(message);
            if (responseTextMessage != null) {
                execute(responseTextMessage);
                return;
            }
        } catch (InvocationTargetException | IllegalAccessException | TelegramApiException e) {
            throw new RuntimeException(e);
        }

        try {
       SendMediaGroup responseMediaMessage = runPhotoFilter(message);
            if (responseMediaMessage != null) {
                execute(responseMediaMessage);
                return;
                }

            } catch (TelegramApiException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }


    }

    private static SendMessage runCommonCommand(Message message) throws InvocationTargetException, IllegalAccessException {
        String text = message.getText();
        BotCommonCommands commands = new BotCommonCommands();
        Method[] methodsClass = commands.getClass().getDeclaredMethods();
        for (Method method : methodsClass) {
            if (method.isAnnotationPresent(AppBotCommand.class)) {
                AppBotCommand command = method.getAnnotation(AppBotCommand.class);
                if (command.name().equals(text)) {
                    method.setAccessible(true);
                    String response = (String) method.invoke(commands);
                    if (response != null) {
                        SendMessage sendMessage = new SendMessage();
                        sendMessage.setChatId(message.getChatId().toString());
                        sendMessage.setText(response);
                        return sendMessage;
                    }

                }
            }
        }
        return null;
    }

    private SendMediaGroup runPhotoFilter(Message message) throws InvocationTargetException, IllegalAccessException {
        String caption = message.getCaption();
        ImageOperation operation = ImageUtils.getOperation(caption);
        if (operation == null) return null;
        List<File> files = getFilesByMessage(message);
        try {
            List<String> paths = PhotoMessage.savePhotos(files, getBotToken());
            return preparePhotoMessage(paths,operation, message.getChatId().toString());

        } catch (IOException  e) {
            throw new RuntimeException(e);
        }

    }


    private static SendMediaGroup preparePhotoMessage(List<String> localPaths, ImageOperation operation, String chatId) {
        SendMediaGroup sendMediaGroup = new SendMediaGroup();
        ArrayList<InputMedia> medias = new ArrayList<>();

        for (String paths : localPaths) {
            InputMedia inputMedia = new InputMediaPhoto();
            inputMedia.setMedia(new java.io.File(paths),"path");
            medias.add(inputMedia);
            try {
                PhotoMessage.processingImage(paths,operation);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
        sendMediaGroup.setMedias(medias);
        sendMediaGroup.setChatId(chatId);


        return sendMediaGroup;
    }

    private List<File> getFilesByMessage(Message message) {
        List<PhotoSize> photoSizes = message.getPhoto();
        ArrayList<File> files = new ArrayList<>();
        for (PhotoSize photoSize : photoSizes) {
            final String fileId = photoSize.getFileId();
            try {
                files.add(sendApiMethod(new GetFile(fileId)));

            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }

        }
        return files;
    }

    private static ReplyKeyboardMarkup getKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        ArrayList<KeyboardRow> allKeyboardRows = new ArrayList<>();
        allKeyboardRows.addAll(getKeyboardRow(BotCommonCommands.class));
        allKeyboardRows.addAll(getKeyboardRow(FilterOperation.class));

        replyKeyboardMarkup.setKeyboard(allKeyboardRows);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        return replyKeyboardMarkup;
    }

    private static ArrayList<KeyboardRow> getKeyboardRow(Class someClass) {
        Method[] methodsClass = someClass.getDeclaredMethods();
        ArrayList<AppBotCommand> commands = new ArrayList<>();
        for (Method method : methodsClass) {
            if (method.isAnnotationPresent(AppBotCommand.class)) {
                commands.add(method.getAnnotation(AppBotCommand.class));
            }
        }
        ArrayList<KeyboardRow> keyboardRows = new ArrayList<>();
        int columnCount = 3;
        int rowsCount = commands.size() / columnCount + ((commands.size() % columnCount == 0) ? 0 : 1);
        for (int rowIndex = 0; rowIndex < rowsCount; rowIndex++) {
            KeyboardRow row = new KeyboardRow();
            for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                int index = rowIndex * columnCount + columnIndex;
                if (index >= commands.size()) continue;
                AppBotCommand command = commands.get(rowIndex * columnCount + columnIndex);
                KeyboardButton button = new KeyboardButton(command.name());
                row.add(button);

            }
            keyboardRows.add(row);
        }
        return keyboardRows;
    }

    @Override
    public String getBotUsername() {
        return "magk95_bot";
    }

    @Override
    public  String getBotToken() {
        return "7259093033:AAG-sIuZ-7DcP-crun47CnDU7DW9MGNrQLo";
    }


}
