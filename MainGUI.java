import javax.swing.*;

public class MainGUI {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String userName = JOptionPane.showInputDialog(null,
                    "사용자 이름을 입력하세요:", "User", JOptionPane.QUESTION_MESSAGE);
            if (userName == null || userName.trim().isEmpty()) userName = "User";

            VocManager manager = new VocManager(userName);

            // өмнө нь: manager.makeVoc("words.txt");
            manager.makeVoc();   // ✅ одоо параметргүй

            new VocManagerFrame(manager);
        });
    }
}
