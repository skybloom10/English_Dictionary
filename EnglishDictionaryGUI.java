package team10;

import javax.swing.*;
import java.awt.*;
import java.util.Comparator;
import java.util.Vector;

public class EnglishDictionaryGUI extends JFrame {

    private final VocManager manager;

    private DefaultListModel<String> listModel;
    private JList<String> wordList;
    private JTextArea meaningArea;
    private JTextField searchField;
    private JLabel userLabel;
    private JLabel wrongLabel;

    public EnglishDictionaryGUI(String userName) {
        super("English Dictionary - Team10");
        this.manager = new VocManager(userName);

        initComponents();
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setVisible(true);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        createMenuBar();
        createTopPanel();
        createCenterPanel();
        createStatusBar();

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                exitApp();
            }
        });

        refreshWordList();
        updateUserInfo();
    }

    /* -------- MENU BAR -------- */
    private void createMenuBar() {
        JMenuBar mb = new JMenuBar();
        JMenu menu = new JMenu("Menu");

        JMenuItem addItem = new JMenuItem("1) Add");
        JMenuItem editItem = new JMenuItem("2) Edit");
        JMenuItem deleteItem = new JMenuItem("3) Delete");
        JMenuItem writtenEngKorItem = new JMenuItem("6) Written (ENG→KOR)");
        JMenuItem writtenKorEngItem = new JMenuItem("7) Written (KOR→ENG)");
        JMenuItem wrongNoteQuizItem = new JMenuItem("10) Written from Wrong Note");
        JMenuItem wordleItem = new JMenuItem("12) Wordle Game");
        JMenuItem wordleArchiveItem = new JMenuItem("Wordle Archive");
        JMenuItem exitItem = new JMenuItem("Exit (Save & Close)");

        addItem.addActionListener(e -> onAdd());
        editItem.addActionListener(e -> onEdit());
        deleteItem.addActionListener(e -> onDelete());
        writtenEngKorItem.addActionListener(
                e -> manager.writtenQuizEngToKorSwing(this, meaningArea));
        writtenKorEngItem.addActionListener(
                e -> manager.writtenQuizKorToEngSwing(this, meaningArea));
        wrongNoteQuizItem.addActionListener(
                e -> manager.wrongNoteQuizSwing(this, meaningArea));
        wordleItem.addActionListener(e -> new WordleFrame(this, manager));
        wordleArchiveItem.addActionListener(e -> showWordleArchive());
        exitItem.addActionListener(e -> exitApp());

        menu.add(addItem);
        menu.add(editItem);
        menu.add(deleteItem);
        menu.addSeparator();
        menu.add(writtenEngKorItem);
        menu.add(writtenKorEngItem);
        menu.add(wrongNoteQuizItem);
        menu.addSeparator();
        menu.add(wordleItem);
        menu.add(wordleArchiveItem);
        menu.addSeparator();
        menu.add(exitItem);

        mb.add(menu);
        setJMenuBar(mb);
    }

    /* -------- TOP SEARCH BAR -------- */
    private void createTopPanel() {
        JPanel top = new JPanel(new BorderLayout());
        searchField = new JTextField();
        JButton searchButton = new JButton("검색");
        searchButton.addActionListener(e -> doSearch());
        top.add(new JLabel(" Search: "), BorderLayout.WEST);
        top.add(searchField, BorderLayout.CENTER);
        top.add(searchButton, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);
    }

    /* -------- CENTER: word list + meaning -------- */
    private void createCenterPanel() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.3);

        listModel = new DefaultListModel<>();
        wordList = new JList<>(listModel);
        wordList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        wordList.addListSelectionListener(e -> showSelectedWord());
        JScrollPane listScroll = new JScrollPane(wordList);
        split.setLeftComponent(listScroll);

        meaningArea = new JTextArea();
        meaningArea.setEditable(false);
        meaningArea.setLineWrap(true);
        meaningArea.setWrapStyleWord(true);
        JScrollPane rightScroll = new JScrollPane(meaningArea);
        split.setRightComponent(rightScroll);

        add(split, BorderLayout.CENTER);
    }

    /* -------- STATUS BAR -------- */
    private void createStatusBar() {
        JPanel status = new JPanel(new GridLayout(1, 2));
        userLabel = new JLabel();
        wrongLabel = new JLabel();
        status.add(userLabel);
        status.add(wrongLabel);
        add(status, BorderLayout.SOUTH);
    }

    /* -------- View helpers -------- */
    private void refreshWordList() {
        listModel.clear();
        Vector<Word> copy = new Vector<>(manager.getVoc());
        copy.sort(Comparator.comparing(w -> w.getEng().toLowerCase()));
        for (Word w : copy) {
            listModel.addElement(w.getEng());
        }
        if (!listModel.isEmpty()) {
            wordList.setSelectedIndex(0);
        } else {
            meaningArea.setText("");
        }
    }

    private void updateUserInfo() {
        userLabel.setText("User: " + manager.getUserName());
        wrongLabel.setText("WrongNotes: " + manager.getWrongNotes().size());
    }

    private void showSelectedWord() {
        String eng = wordList.getSelectedValue();
        if (eng == null) {
            meaningArea.setText("");
            return;
        }
        Word w = manager.searchWord(eng);
        if (w != null) {
            meaningArea.setText(w.getEng() + " :\n" + w.getKor());
        } else {
            meaningArea.setText("");
        }
    }

    private void doSearch() {
        String keyword = searchField.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            refreshWordList();
            return;
        }
        listModel.clear();
        for (Word w : manager.getVoc()) {
            if (w.getEng().toLowerCase().contains(keyword) ||
                    w.getKor().toLowerCase().contains(keyword)) {
                listModel.addElement(w.getEng());
            }
        }
        if (!listModel.isEmpty()) wordList.setSelectedIndex(0);
        else meaningArea.setText("검색 결과가 없습니다.");
    }

    /* -------- Menu actions: Add/Edit/Delete -------- */
    private void onAdd() {
        String eng = JOptionPane.showInputDialog(this, "추가할 영어 단어:");
        if (eng == null || eng.trim().isEmpty()) return;
        if (manager.searchWord(eng.trim()) != null) {
            JOptionPane.showMessageDialog(this, "이미 존재하는 단어입니다.");
            return;
        }
        String kor = JOptionPane.showInputDialog(this, "뜻(한국어):");
        if (kor == null || kor.trim().isEmpty()) return;
        manager.addWord(eng.trim(), kor.trim());
        manager.saveVocToFile();
        refreshWordList();
        updateUserInfo();
    }

    private void onEdit() {
        String eng = wordList.getSelectedValue();
        if (eng == null) {
            eng = JOptionPane.showInputDialog(this, "수정할 영어 단어:");
            if (eng == null || eng.trim().isEmpty()) return;
        }
        Word w = manager.searchWord(eng.trim());
        if (w == null) {
            JOptionPane.showMessageDialog(this, "단어를 찾을 수 없습니다.");
            return;
        }
        String newKor = JOptionPane.showInputDialog(this,
                "새 뜻(한국어):", w.getKor());
        if (newKor == null || newKor.trim().isEmpty()) return;
        manager.editWord(w.getEng(), newKor.trim());
        manager.saveVocToFile();
        refreshWordList();
        updateUserInfo();
    }

    private void onDelete() {
        String eng = wordList.getSelectedValue();
        if (eng == null) {
            eng = JOptionPane.showInputDialog(this, "삭제할 영어 단어:");
            if (eng == null || eng.trim().isEmpty()) return;
        }
        int result = JOptionPane.showConfirmDialog(this,
                "정말 삭제할까요? " + eng,
                "삭제 확인",
                JOptionPane.YES_NO_OPTION);
        if (result != JOptionPane.YES_OPTION) return;

        if (!manager.deleteWord(eng.trim())) {
            JOptionPane.showMessageDialog(this, "단어를 찾을 수 없습니다.");
        } else {
            manager.saveVocToFile();
            refreshWordList();
            updateUserInfo();
        }
    }

    /* -------- Wordle Archive -------- */
    private void showWordleArchive() {
        String log = manager.loadWordleLog();

        JTextArea area = new JTextArea(log, 15, 40);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(area);

        JOptionPane.showMessageDialog(this, scroll,
                "Wordle Archive - " + manager.getUserName(),
                JOptionPane.INFORMATION_MESSAGE);
    }

    /* -------- Exit -------- */
    private void exitApp() {
        manager.saveAll();
        dispose();
        System.exit(0);
    }
}
