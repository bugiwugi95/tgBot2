import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class SwingJava {
    public static void main(String[] args) {


        JFrame frame = new JFrame("My First GUI"); // Для окна нужна "рама" - Frame
        // стандартное поведение при закрытии окна - завершение приложения
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        String[] elements = new String[]{"Chrome", "Firefox", "Internet Explorer", "Safari",
                                   "Opera", "Morrowind", "Oblivion", "NFS", "Half Life 2",
                                   "Hitman", "Morrowind", "Oblivion", "NFS", "Half Life 2",
                                   "Hitman", "Morrowind"};

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


    }
}
