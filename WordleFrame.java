package team10;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class WordleFrame extends JFrame {

    private final VocManager manager;
    private String targetWord;
    private String targetKor;
    private final int MAX_ATTEMPTS = 6;
    private int attempt = 0;

    private JTextArea historyArea;
    private JTextField guessField;
    private JLabel infoLabel;

    public WordleFrame(Frame owner, VocManager manager) {
        super("Wordle Game - 5글자 영단어 맞추기");
        this.manager = manager;

        Word target = manager.chooseWordleTarget();
        if (target == null) {
            JOptionPane.showMessageDialog(owner,
                    "Wordle을 위해 5글자 영단어가 필요합니다.\n" +
                            "예: apple, major 등 5글자 단어를 단어장에 추가하세요.");
            dispose();
            return;
        }
        targetWord = target.getEng().toLowerCase();
        targetKor = target.getKor();

        initComponents();
        setSize(450, 400);
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JLabel title = new JLabel("5글자 영단어를 6번 안에 맞춰보세요!", SwingConstants.CENTER);
        add(title, BorderLayout.NORTH);

        historyArea = new JTextArea();
        historyArea.setEditable(false);
        historyArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        add(new JScrollPane(historyArea), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Guess: "));
        guessField = new JTextField(10);
        inputPanel.add(guessField);
        JButton tryBtn = new JButton("Try");
        inputPanel.add(tryBtn);
        bottom.add(inputPanel, BorderLayout.NORTH);

        infoLabel = new JLabel("시도: 0 / " + MAX_ATTEMPTS, SwingConstants.CENTER);
        bottom.add(infoLabel, BorderLayout.SOUTH);

        add(bottom, BorderLayout.SOUTH);

        tryBtn.addActionListener(this::handleGuess);
        guessField.addActionListener(this::handleGuess);
    }

    private void handleGuess(ActionEvent e) {
        if (attempt >= MAX_ATTEMPTS) return;

        String guess = guessField.getText().trim().toLowerCase();
        if (guess.length() != 5 || !guess.matches("[A-Za-z]{5}")) {
            JOptionPane.showMessageDialog(this,
                    "영문자 5글자만 입력해야 합니다. (공백/숫자 X)");
            return;
        }

        attempt++;

        if (guess.equals(targetWord)) {
            historyArea.append(guess.toUpperCase() + "  ->  ⭐⭐⭐⭐⭐ (정답!)\n");
            infoLabel.setText("시도: " + attempt + " / " + MAX_ATTEMPTS);

            manager.appendWordleLog(
                    "SUCCESS\t" + targetWord.toUpperCase() +
                            "\t" + attempt + " tries\t" + targetKor);

            JOptionPane.showMessageDialog(this,
                    "축하합니다! " + attempt + "번 만에 정답을 맞혔습니다.\n" +
                            targetWord.toUpperCase() + " : " + targetKor);
            guessField.setEditable(false);
            return;
        } else {
            String feedback = VocManager.generateWordleFeedback(targetWord, guess);
            historyArea.append(guess.toUpperCase() + "  ->  " + feedback + "\n");
        }

        infoLabel.setText("시도: " + attempt + " / " + MAX_ATTEMPTS);
        guessField.setText("");
        guessField.requestFocusInWindow();

        if (attempt >= MAX_ATTEMPTS) {
            manager.appendWordleLog(
                    "FAIL\t" + targetWord.toUpperCase() +
                            "\t" + attempt + " tries\t" + targetKor);

            JOptionPane.showMessageDialog(this,
                    "아쉽습니다. 기회를 모두 사용했습니다.\n" +
                            "정답: " + targetWord.toUpperCase() + "\n뜻: " + targetKor);
            guessField.setEditable(false);
        }
    }
}
