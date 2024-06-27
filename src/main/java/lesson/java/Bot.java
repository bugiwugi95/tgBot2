package lesson.java;

import commands.AppBotCommand;
import commands.BotCommonCommands;
import functions.FilterOperation;
import functions.ImageOperation;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
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
import java.util.HashMap;
import java.util.List;

import static commands.BotCommonCommands.adminCommand;

public class Bot extends TelegramLongPollingBot {
    HashMap<String, Message> messages = new HashMap<>();

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        try {
            SendMessage responseTextMessage = runCommonCommand(message);
            if (responseTextMessage != null) {
                execute(responseTextMessage);
                return;
            }

            responseTextMessage = runPhotoMessage(message);
            if (responseTextMessage != null) {
                execute(responseTextMessage);
                return;
            }


            Object responseMediaMessage = runPhotoFilter(message);
            if (responseMediaMessage != null) {
                if (responseMediaMessage instanceof SendMediaGroup) {
                    execute((SendMediaGroup) responseMediaMessage);
                } else {
                    if (responseMediaMessage instanceof SendMessage) {
                        execute((SendMessage) responseMediaMessage);
                    }
                }


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

    private SendMessage runPhotoMessage(Message message) {
        List<File> files = getFilesByMessage(message);
        if (files.isEmpty()) {
            return null;
        }
        String chatId = message.getChatId().toString();
        this.messages.put(chatId, message);

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        ArrayList<KeyboardRow> allKeyboardRows = new ArrayList<>(getKeyboardRow(FilterOperation.class));
        replyKeyboardMarkup.setKeyboard(allKeyboardRows);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        sendMessage.setChatId(chatId);
        sendMessage.setText("Выберите фильтр");
        return sendMessage;
    }

    private Object runPhotoFilter(Message newMessage) throws InvocationTargetException, IllegalAccessException {
        final String text = newMessage.getText();
        ImageOperation operation = ImageUtils.getOperation(text);
        if (operation == null) return null;
        String chatId = newMessage.getChatId().toString();
        Message photoMessage = messages.get(chatId);
        if (photoMessage != null) {
            List<File> files = getFilesByMessage(photoMessage);
            try {
                List<String> paths = PhotoMessage.savePhotos(files, getBotToken());
                return preparePhotoMessage(paths, operation, chatId);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText("Отправьте фото");
            return sendMessage;
        }

    }


    private static SendMediaGroup preparePhotoMessage(List<String> localPaths, ImageOperation operation, String chatId) {
        SendMediaGroup sendMediaGroup = new SendMediaGroup();
        ArrayList<InputMedia> medias = new ArrayList<>();

        for (String paths : localPaths) {
            InputMedia inputMedia = new InputMediaPhoto();
            inputMedia.setMedia(new java.io.File(paths), "path");
            medias.add(inputMedia);
            try {
                PhotoMessage.processingImage(paths, operation);
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
        if (photoSizes == null) return new ArrayList<>();
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
    public String getBotToken() {
        return "7259093033:AAG-sIuZ-7DcP-crun47CnDU7DW9MGNrQLo";
    }


}
