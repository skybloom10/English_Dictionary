package team10;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String userName =
                    JOptionPane.showInputDialog(null, "사용자 이름을 입력하세요:");
            if (userName == null || userName.trim().isEmpty())
                userName = "Guest";
            new EnglishDictionaryGUI(userName.trim());
        });
    }
}
