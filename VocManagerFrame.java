import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class VocManagerFrame extends JFrame {

    private final VocManager manager;

    // left list
    private final DefaultListModel<Word> listModel = new DefaultListModel<>();
    private final JList<Word> wordList = new JList<>(listModel);

    // top search
    private final JTextField searchField = new JTextField(20);

    // right detail
    private final JLabel engLabel = new JLabel(" ");
    private final JLabel korLabel = new JLabel(" ");

    // bottom game/quiz area (Wordle)
    private final JTextArea gameArea = new JTextArea(6, 40);
    private final JTextField gameInput = new JTextField(15);
    private final JButton gameSubmitBtn = new JButton("입력");

    // Wordle state
    private Word wordleTarget = null;
    private String wordleTargetWord = null;
    private String wordleTargetKor = null;
    private int wordleAttempt = 0;
    private static final int WORDLE_MAX_ATTEMPTS = 6;
    private boolean wordleRunning = false;

    public VocManagerFrame(VocManager manager) {
        this.manager = manager;

        setTitle("Vocabulary - " + manager.userName);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        initTopBar();
        initCenter();
        initGamePanel();

        refreshWordList();

        pack();
        setSize(700, 550);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /* ================== UI 구성 ================== */

    private void initTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(0x003399));
        top.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // ☰ menu button
        JButton menuButton = new JButton("☰");
        menuButton.setFocusPainted(false);

        JPopupMenu popupMenu = createMenu();
        menuButton.addActionListener(e ->
                popupMenu.show(menuButton, 0, menuButton.getHeight())
        );

        JLabel title = new JLabel("WORD OF THE DAY / VOCABULARY", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));

        top.add(menuButton, BorderLayout.WEST);
        top.add(title, BorderLayout.CENTER);

        // Search bar
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        JButton searchBtn = new JButton("Go");
        searchBtn.addActionListener(this::onSearch);
        searchPanel.add(searchBtn);

        JPanel container = new JPanel(new BorderLayout());
        container.add(top, BorderLayout.NORTH);
        container.add(searchPanel, BorderLayout.SOUTH);

        add(container, BorderLayout.NORTH);
    }

    private void initCenter() {
        // left list
        wordList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        wordList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Word w = wordList.getSelectedValue();
                showWordDetail(w);
            }
        });
        JScrollPane listScroll = new JScrollPane(wordList);

        // right detail
        JPanel detail = new JPanel();
        detail.setLayout(new BoxLayout(detail, BoxLayout.Y_AXIS));
        detail.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        engLabel.setFont(engLabel.getFont().deriveFont(Font.BOLD, 22f));
        korLabel.setFont(korLabel.getFont().deriveFont(Font.PLAIN, 18f));

        detail.add(engLabel);
        detail.add(Box.createVerticalStrut(10));
        detail.add(korLabel);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                listScroll, detail);
        split.setDividerLocation(250);

        add(split, BorderLayout.CENTER);
    }

    private void initGamePanel() {
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(BorderFactory.createTitledBorder("Game / Quiz"));

        gameArea.setEditable(false);
        gameArea.setLineWrap(true);
        gameArea.setWrapStyleWord(true);

        bottom.add(new JScrollPane(gameArea), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.add(new JLabel("추측 / 답:"));
        inputPanel.add(gameInput);
        inputPanel.add(gameSubmitBtn);

        bottom.add(inputPanel, BorderLayout.SOUTH);

        // Wordle submit action
        gameSubmitBtn.addActionListener(e -> onWordleSubmit());

        add(bottom, BorderLayout.SOUTH);

        // 처음에는 아무 ч тоглоомгүй
        clearGameArea();
    }

    private JPopupMenu createMenu() {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem addItem = new JMenuItem("Add word");
        addItem.addActionListener(e -> onAddWord());
        menu.add(addItem);

        JMenuItem editItem = new JMenuItem("Edit word");
        editItem.addActionListener(e -> onEditWord());
        menu.add(editItem);

        JMenuItem deleteItem = new JMenuItem("Delete word");
        deleteItem.addActionListener(e -> onDeleteWord());
        menu.add(deleteItem);

        menu.addSeparator();

        // ===== console menu 6~10: all quizzes =====
        JMenuItem w1 = new JMenuItem("Written Quiz (ENG → KOR)");
        w1.addActionListener(e -> onQuizEngToKor());
        menu.add(w1);

        JMenuItem w2 = new JMenuItem("Written Quiz (KOR → ENG)");
        w2.addActionListener(e -> onQuizKorToEng());
        menu.add(w2);

        JMenuItem m1 = new JMenuItem("Multiple-choice (ENG → KOR)");
        m1.addActionListener(e -> onMultiQuizEngToKor());
        menu.add(m1);

        JMenuItem m2 = new JMenuItem("Multiple-choice (KOR → ENG)");
        m2.addActionListener(e -> onMultiQuizKorToEng());
        menu.add(m2);

        JMenuItem wrong = new JMenuItem("Written from Wrong Note");
        wrong.addActionListener(e -> onWrongNoteQuiz());
        menu.add(wrong);

        menu.addSeparator();

        JMenuItem wordleItem = new JMenuItem("Wordle Game (5글자)");
        wordleItem.addActionListener(e -> startWordle());
        menu.add(wordleItem);

        menu.addSeparator();

        JMenuItem saveItem = new JMenuItem("Save now");
        saveItem.addActionListener(e -> {
            manager.saveAll();
            JOptionPane.showMessageDialog(this,
                    "단어장과 오답노트가 저장되었습니다.",
                    "Save", JOptionPane.INFORMATION_MESSAGE);
        });
        menu.add(saveItem);

        JMenuItem exitItem = new JMenuItem("Exit (자동 저장)");
        exitItem.addActionListener(e -> {
            manager.saveAll();
            dispose();
            System.exit(0);
        });
        menu.add(exitItem);

        return menu;
    }

    /* ================== Actions ================== */

    private void onSearch(ActionEvent e) {
        String key = searchField.getText().trim();
        if (key.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "검색할 영단어를 입력해 주세요.",
                    "Search", JOptionPane.WARNING_MESSAGE);
            return;
        }

        for (int i = 0; i < listModel.size(); i++) {
            Word w = listModel.get(i);
            if (w.eng.equalsIgnoreCase(key)) {
                wordList.setSelectedIndex(i);
                wordList.ensureIndexIsVisible(i);
                return;
            }
        }
        JOptionPane.showMessageDialog(this,
                "해당 단어를 찾을 수 없습니다: " + key,
                "Search", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onAddWord() {
        JTextField engField = new JTextField(15);
        JTextField korField = new JTextField(15);

        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("영단어:"));
        panel.add(engField);
        panel.add(new JLabel("뜻(한국어):"));
        panel.add(korField);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Add word", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String eng = engField.getText().trim();
            String kor = korField.getText().trim();

            if (eng.isEmpty() || kor.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "영단어와 뜻을 모두 입력해 주세요.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            for (Word w : manager.getVoc()) {
                if (w.eng.equalsIgnoreCase(eng)) {
                    JOptionPane.showMessageDialog(this,
                            "이미 존재하는 단어입니다.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            manager.addWord(eng, kor);
            refreshWordList();
        }
    }

    private void onEditWord() {
        Word selected = wordList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                    "먼저 수정할 단어를 리스트에서 선택해 주세요.",
                    "Edit", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String newKor = JOptionPane.showInputDialog(this,
                "새로운 뜻(한국어)을 입력하세요:", selected.kor);
        if (newKor == null) return;
        newKor = newKor.trim();
        if (newKor.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "뜻이 비어 있습니다.",
                    "Edit", JOptionPane.ERROR_MESSAGE);
            return;
        }

        selected.kor = newKor;
        refreshWordList();
        wordList.setSelectedValue(selected, true);
    }

    private void onDeleteWord() {
        Word selected = wordList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                    "먼저 삭제할 단어를 리스트에서 선택해 주세요.",
                    "Delete", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int r = JOptionPane.showConfirmDialog(this,
                "정말 삭제하시겠습니까?\n" + selected,
                "Delete", JOptionPane.YES_NO_OPTION);

        if (r == JOptionPane.YES_OPTION) {
            manager.getVoc().remove(selected);
            refreshWordList();
        }
    }

    /* ---------- Written Quiz (ENG→KOR) ---------- */
    private void onQuizEngToKor() {
        Vector<Word> all = manager.getVoc();
        if (all.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "단어장이 비어 있어 퀴즈를 진행할 수 없습니다.",
                    "Quiz", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int n = askQuizCount(all.size());
        if (n <= 0) return;

        List<Word> list = new ArrayList<>(all);
        Collections.shuffle(list);
        list = list.subList(0, n);

        int score = 0;
        for (Word q : list) {
            String answer = JOptionPane.showInputDialog(this,
                    "영단어: " + q.eng + "\n뜻을 입력하세요:");
            if (answer == null) continue;
            answer = answer.trim();

            if (answer.equalsIgnoreCase(q.kor)) {
                JOptionPane.showMessageDialog(this, "정답!");
                score++;
            } else {
                JOptionPane.showMessageDialog(this,
                        "오답! 정답: " + q.kor);
                addWrongNote(q);
            }
        }

        JOptionPane.showMessageDialog(this,
                "점수: " + score + " / " + list.size(),
                "Quiz 결과", JOptionPane.INFORMATION_MESSAGE);
    }

    /* ---------- Written Quiz (KOR→ENG) ---------- */
    private void onQuizKorToEng() {
        Vector<Word> all = manager.getVoc();
        if (all.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "단어장이 비어 있어 퀴즈를 진행할 수 없습니다.",
                    "Quiz", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int n = askQuizCount(all.size());
        if (n <= 0) return;

        List<Word> list = new ArrayList<>(all);
        Collections.shuffle(list);
        list = list.subList(0, n);

        int score = 0;
        for (Word q : list) {
            String answer = JOptionPane.showInputDialog(this,
                    "뜻: " + q.kor + "\n영단어를 입력하세요:");
            if (answer == null) continue;
            answer = answer.trim();

            if (answer.equalsIgnoreCase(q.eng)) {
                JOptionPane.showMessageDialog(this, "정답!");
                score++;
            } else {
                JOptionPane.showMessageDialog(this,
                        "오답! 정답: " + q.eng);
                addWrongNote(q);
            }
        }

        JOptionPane.showMessageDialog(this,
                "점수: " + score + " / " + list.size(),
                "Quiz 결과", JOptionPane.INFORMATION_MESSAGE);
    }

    /* ---------- Multiple-choice (ENG→KOR) ---------- */
    private void onMultiQuizEngToKor() {
        Vector<Word> all = manager.getVoc();
        if (all.size() < 4) {
            JOptionPane.showMessageDialog(this,
                    "객관식 퀴즈를 위해 최소 4개 이상의 단어가 필요합니다.",
                    "Quiz", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int n = askQuizCount(all.size());
        if (n <= 0) return;

        List<Word> questions = new ArrayList<>(all);
        Collections.shuffle(questions);
        questions = questions.subList(0, n);

        int score = 0;

        for (Word q : questions) {
            // 4 보기
            List<Word> options = new ArrayList<>(all);
            Collections.shuffle(options);
            options = options.subList(0, 4);
            if (!options.contains(q)) {
                options.set(0, q);
            }
            Collections.shuffle(options);

            StringBuilder msg = new StringBuilder();
            msg.append("영단어: ").append(q.eng).append("\n");
            for (int i = 0; i < options.size(); i++) {
                msg.append(i + 1).append(") ")
                        .append(options.get(i).kor).append("\n");
            }
            String ansStr = JOptionPane.showInputDialog(this,
                    msg + "\n정답 번호를 입력하세요:");
            if (ansStr == null) continue;

            int ans;
            try {
                ans = Integer.parseInt(ansStr.trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "잘못된 번호입니다. 오답 처리됩니다.",
                        "Quiz", JOptionPane.ERROR_MESSAGE);
                addWrongNote(q);
                continue;
            }

            if (ans < 1 || ans > options.size()) {
                JOptionPane.showMessageDialog(this,
                        "잘못된 번호입니다. 오답 처리됩니다.",
                        "Quiz", JOptionPane.ERROR_MESSAGE);
                addWrongNote(q);
            } else if (options.get(ans - 1).equals(q)) {
                JOptionPane.showMessageDialog(this, "정답!");
                score++;
            } else {
                JOptionPane.showMessageDialog(this,
                        "오답! 정답: " + q.kor);
                addWrongNote(q);
            }
        }

        JOptionPane.showMessageDialog(this,
                "점수: " + score + " / " + questions.size(),
                "Quiz 결과", JOptionPane.INFORMATION_MESSAGE);
    }

    /* ---------- Multiple-choice (KOR→ENG) ---------- */
    private void onMultiQuizKorToEng() {
        Vector<Word> all = manager.getVoc();
        if (all.size() < 4) {
            JOptionPane.showMessageDialog(this,
                    "객관식 퀴즈를 위해 최소 4개 이상의 단어가 필요합니다.",
                    "Quiz", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int n = askQuizCount(all.size());
        if (n <= 0) return;

        List<Word> questions = new ArrayList<>(all);
        Collections.shuffle(questions);
        questions = questions.subList(0, n);

        int score = 0;

        for (Word q : questions) {
            List<Word> options = new ArrayList<>(all);
            Collections.shuffle(options);
            options = options.subList(0, 4);
            if (!options.contains(q)) {
                options.set(0, q);
            }
            Collections.shuffle(options);

            StringBuilder msg = new StringBuilder();
            msg.append("뜻: ").append(q.kor).append("\n");
            for (int i = 0; i < options.size(); i++) {
                msg.append(i + 1).append(") ")
                        .append(options.get(i).eng).append("\n");
            }
            String ansStr = JOptionPane.showInputDialog(this,
                    msg + "\n정답 번호를 입력하세요:");
            if (ansStr == null) continue;

            int ans;
            try {
                ans = Integer.parseInt(ansStr.trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "잘못된 번호입니다. 오답 처리됩니다.",
                        "Quiz", JOptionPane.ERROR_MESSAGE);
                addWrongNote(q);
                continue;
            }

            if (ans < 1 || ans > options.size()) {
                JOptionPane.showMessageDialog(this,
                        "잘못된 번호입니다. 오답 처리됩니다.",
                        "Quiz", JOptionPane.ERROR_MESSAGE);
                addWrongNote(q);
            } else if (options.get(ans - 1).equals(q)) {
                JOptionPane.showMessageDialog(this, "정답!");
                score++;
            } else {
                JOptionPane.showMessageDialog(this,
                        "오답! 정답: " + q.eng);
                addWrongNote(q);
            }
        }

        JOptionPane.showMessageDialog(this,
                "점수: " + score + " / " + questions.size(),
                "Quiz 결과", JOptionPane.INFORMATION_MESSAGE);
    }

    /* ---------- Written from Wrong Note (ENG→KOR) ---------- */
    private void onWrongNoteQuiz() {
        Vector<Word> wrong = manager.getWrongNotes();
        if (wrong.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "오답노트가 비어 있습니다.",
                    "Wrong Note Quiz", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int n = askQuizCount(wrong.size());
        if (n <= 0) return;

        List<Word> list = new ArrayList<>(wrong);
        Collections.shuffle(list);
        list = list.subList(0, n);

        int score = 0;
        List<Word> toRemove = new ArrayList<>();

        for (Word q : list) {
            String answer = JOptionPane.showInputDialog(this,
                    "영단어: " + q.eng + "\n뜻을 입력하세요:");
            if (answer == null) continue;
            answer = answer.trim();

            if (answer.equalsIgnoreCase(q.kor)) {
                JOptionPane.showMessageDialog(this, "정답!");
                score++;
                toRemove.add(q);
            } else {
                JOptionPane.showMessageDialog(this,
                        "오답! 정답: " + q.kor);
            }
        }

        // 정답 맞힌 것은 오답노트에서 제거
        wrong.removeAll(toRemove);

        JOptionPane.showMessageDialog(this,
                "정답으로 맞힌 단어는 오답노트에서 제거되었습니다.\n" +
                        "점수: " + score + " / " + list.size(),
                "Wrong Note Quiz 결과", JOptionPane.INFORMATION_MESSAGE);
    }

    /* ---------- Wordle: main window 아래에서 입력 ---------- */

    private void startWordle() {
        Vector<Word> all = manager.getVoc();
        List<Word> fiveLetter = new ArrayList<>();
        for (Word w : all) {
            if (w.eng != null &&
                    w.eng.length() == 5 &&
                    !w.eng.contains(" ") &&
                    w.eng.matches("[A-Za-z]{5}")) {
                fiveLetter.add(w);
            }
        }

        if (fiveLetter.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Wordle 퀴즈를 위해 띄어쓰기 없는 5글자 영어 단어가 필요합니다.\n" +
                            "예: major, thesis 등",
                    "Wordle", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Collections.shuffle(fiveLetter);
        wordleTarget = fiveLetter.get(0);
        wordleTargetWord = wordleTarget.eng.toLowerCase();
        wordleTargetKor = wordleTarget.kor;
        wordleAttempt = 0;
        wordleRunning = true;

        gameArea.setText(
                "Wordle (5글자 단어)\n" +
                        "⭐ : 위치까지 정확\n" +
                        "⟳ : 글자는 맞지만 위치가 틀림\n" +
                        "x : 없는 글자\n\n" +
                        "시도 " + (wordleAttempt + 1) + "/" + WORDLE_MAX_ATTEMPTS + "\n"
        );
        gameInput.setText("");
        gameInput.requestFocus();
    }

    private void onWordleSubmit() {
        if (!wordleRunning || wordleTargetWord == null) {
            // Wordle ажиллаагүй байвал юу ч хийхгүй
            return;
        }

        String guess = gameInput.getText().trim().toLowerCase();
        if (guess.length() != 5 || guess.contains(" ") || guess.matches("\\d+")) {
            JOptionPane.showMessageDialog(this,
                    "5글자 영단어만 입력해야 합니다.",
                    "Wordle", JOptionPane.ERROR_MESSAGE);
            return;
        }

        wordleAttempt++;

        if (guess.equals(wordleTargetWord)) {
            gameArea.append(guess + "  ->  ⭐⭐⭐⭐⭐\n\n");
            gameArea.append("축하합니다! " + wordleAttempt + "번 만에 정답을 맞혔습니다.\n");
            gameArea.append("정답: " + wordleTargetWord + " (" + wordleTargetKor + ")\n");
            wordleRunning = false;
        } else {
            String feedback = generateWordleFeedback(wordleTargetWord, guess);
            gameArea.append(guess + "  ->  " + feedback + "\n");

            if (wordleAttempt >= WORDLE_MAX_ATTEMPTS) {
                gameArea.append("\n아쉽습니다. 기회를 모두 사용했습니다.\n");
                gameArea.append("정답: " + wordleTargetWord + " (" + wordleTargetKor + ")\n");
                wordleRunning = false;
                addWrongNote(wordleTarget);
            } else {
                gameArea.append("\n시도 " +
                        (wordleAttempt + 1) + "/" + WORDLE_MAX_ATTEMPTS + "\n");
            }
        }

        gameInput.setText("");
        gameInput.requestFocus();
    }

    /* ================== Helper ================== */

    private void refreshWordList() {
        listModel.clear();
        List<Word> copy = new ArrayList<>(manager.getVoc());
        copy.sort((a, b) -> a.eng.compareToIgnoreCase(b.eng));
        for (Word w : copy) {
            listModel.addElement(w);
        }
        if (!copy.isEmpty()) {
            wordList.setSelectedIndex(0);
        } else {
            showWordDetail(null);
        }
    }

    private void showWordDetail(Word w) {
        if (w == null) {
            engLabel.setText(" ");
            korLabel.setText(" ");
        } else {
            engLabel.setText(w.eng);
            korLabel.setText(w.kor);
        }
    }

    private void clearGameArea() {
        gameArea.setText("아직 실행중인 게임이 없습니다.\n" +
                "메뉴(☰)에서 Wordle 또는 Quiz를 선택해 보세요.");
        gameInput.setText("");
    }

    private int askQuizCount(int max) {
        String nStr = JOptionPane.showInputDialog(this,
                "출제할 문제 개수 (최대 " + max + "):", "5");
        if (nStr == null) return -1;
        try {
            int n = Integer.parseInt(nStr.trim());
            if (n <= 0) {
                JOptionPane.showMessageDialog(this,
                        "0개 이하는 출제가 불가능합니다.",
                        "Quiz", JOptionPane.ERROR_MESSAGE);
                return -1;
            }
            if (n > max) n = max;
            return n;
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "숫자를 입력해 주세요.",
                    "Quiz", JOptionPane.ERROR_MESSAGE);
            return -1;
        }
    }

    private void addWrongNote(Word w) {
        Vector<Word> wrong = manager.getWrongNotes();
        if (!wrong.contains(w)) {
            wrong.add(w);
        }
    }

    private String generateWordleFeedback(String targetWord, String guess) {
        char[] result = new char[5];
        List<Character> remaining = new ArrayList<>();

        for (char c : targetWord.toCharArray()) {
            remaining.add(c);
        }

        for (int i = 0; i < 5; i++) {
            if (guess.charAt(i) == targetWord.charAt(i)) {
                result[i] = '⭐';
                remaining.remove((Character) targetWord.charAt(i));
            } else {
                result[i] = ' ';
            }
        }

        for (int i = 0; i < 5; i++) {
            if (result[i] == ' ') {
                char g = guess.charAt(i);
                if (remaining.contains(g)) {
                    result[i] = '⟳';
                    remaining.remove((Character) g);
                } else {
                    result[i] = 'x';
                }
            }
        }

        return new String(result);
    }
}
