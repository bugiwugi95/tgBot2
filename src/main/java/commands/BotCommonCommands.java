package commands;

import javax.swing.*;
import java.awt.*;

public class BotCommonCommands {
    long id;
    @AppBotCommand(name = "/hello", desc = "when request hello", showInHelp = true)
    String hello() {
        return "Hello,User";
    }

    @AppBotCommand(name = "/bye", desc = "when request bye", showInHelp = true)
    String bye() {
        return "Good bye,User";
    }

    @AppBotCommand(name = "/help", desc = "when request help", showInHelp = true)
    String help() {
        return "HHHHHhelp";
    }

    @AppBotCommand(name = "/admin", desc = "when request admin", showInHelp = true)
    String admin() {
        adminCommand(this.id);
        return "admin";
    }


    public static JList adminCommand(long chatId) {
        JFrame frame = new JFrame("InfoIdUsers"); // Для окна нужна "рама" - Frame
        // стандартное поведение при закрытии окна - завершение приложения
        String[] elements = new String[50];
        for (int i = 0; i < elements.length; i++) {
            elements[i] = "id user: " + i + " - " + String.valueOf(chatId);
        }

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        JList list = new JList(elements);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(0);
        JScrollPane listScroll = new JScrollPane(list);
        listScroll.setPreferredSize(new Dimension(100, 100));
        mainPanel.add(listScroll);
        frame.setSize(300, 300); // размеры окна
        frame.setLocationRelativeTo(null); // окно - в центре экрана
        frame.getContentPane().add(mainPanel);
        frame.setVisible(true); // Делаем окно видимым


        return list;
    }

}
