package team10;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.Component;
import java.io.*;
import java.util.Collections;
import java.util.Random;
import java.util.Vector;

public class VocManager {

    private String userName;
    Vector<Word> voc = new Vector<>();
    Vector<Word> wrongNotes = new Vector<>();

    private String vocFileName;
    private String wrongNoteFileName;
    private String wordleLogFileName;   // Wordle archive

    public VocManager(String userName) {
        this.userName = userName;
        this.vocFileName = "src/team10/words.txt";
        this.wrongNoteFileName = userName + "_wrong_notes.txt";
        this.wordleLogFileName = userName + "_wordle_log.txt";

        loadVocFromFile();
        loadWrongNotes();
    }

    public String getUserName() { return userName; }
    public Vector<Word> getVoc() { return voc; }
    public Vector<Word> getWrongNotes() { return wrongNotes; }

    /* ================== 파일 로드 / 저장 ================== */

    private void loadVocFromFile() {
        File f = new File(vocFileName);
        if (!f.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] temp = line.split("\t");
                if (temp.length >= 2) {
                    addWord(temp[0].trim(), temp[1].trim());
                }
            }
        } catch (IOException e) {
            System.out.println("단어장 파일 읽기 오류: " + e.getMessage());
        }
    }

    public void saveVocToFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(vocFileName))) {
            for (Word w : voc) {
                pw.println(w.getEng() + "\t" + w.getKor());
            }
        } catch (IOException e) {
            System.out.println("단어장 파일 저장 오류: " + e.getMessage());
        }
    }

    private void loadWrongNotes() {
        File f = new File(wrongNoteFileName);
        if (!f.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] temp = line.split("\t");
                if (temp.length >= 2) {
                    Word w = new Word(temp[0].trim(), temp[1].trim());
                    if (!wrongNotes.contains(w)) {
                        wrongNotes.add(w);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("오답노트 파일 읽기 오류: " + e.getMessage());
        }
    }

    public void saveWrongNotes() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(wrongNoteFileName))) {
            for (Word w : wrongNotes) {
                pw.println(w.getEng() + "\t" + w.getKor());
            }
        } catch (IOException e) {
            System.out.println("오답노트 파일 저장 오류: " + e.getMessage());
        }
    }

    public void saveAll() {
        saveVocToFile();
        saveWrongNotes();
    }

    /* ================== 기본 단어 조작 ================== */

    public void addWord(String eng, String kor) {
        Word w = new Word(eng, kor);
        if (!voc.contains(w)) {
            voc.add(w);
        }
    }

    public Word searchWord(String eng) {
        if (eng == null) return null;
        String target = eng.trim().toLowerCase();
        for (Word w : voc) {
            if (w.getEng().toLowerCase().equals(target)) return w;
        }
        return null;
    }

    public boolean deleteWord(String eng) {
        Word w = searchWord(eng);
        if (w != null) {
            voc.remove(w);
            return true;
        }
        return false;
    }

    public void editWord(String eng, String newKor) {
        Word w = searchWord(eng);
        if (w != null) {
            w.setKor(newKor);
        }
    }

    private void addWrongNote(Word w) {
        if (w == null) return;
        if (!wrongNotes.contains(w)) {
            wrongNotes.add(new Word(w.getEng(), w.getKor()));
        }
    }

    private Vector<Word> getRandomQuestions(Vector<Word> source, int count) {
        Vector<Word> list = new Vector<>(source);
        Collections.shuffle(list);
        if (list.size() > count) {
            Vector<Word> result = new Vector<>();
            for (int i = 0; i < count; i++) result.add(list.get(i));
            return result;
        }
        return list;
    }

    /* ================== QUIZ (Swing) ================== */

    public void writtenQuizEngToKorSwing(Component parent, JTextComponent outputArea) {
        if (voc.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "단어장이 비어 있어 퀴즈를 진행할 수 없습니다.");
            return;
        }
        String input = JOptionPane.showInputDialog(parent,
                "출제할 문제 개수 (최대 " + voc.size() + "):");
        if (input == null) return;
        int n;
        try {
            n = Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(parent, "숫자를 입력해 주세요.");
            return;
        }
        if (n <= 0) return;
        if (n > voc.size()) n = voc.size();

        Vector<Word> questions = getRandomQuestions(voc, n);
        int score = 0;
        StringBuilder log = new StringBuilder("[Written Quiz ENG→KOR]\n");

        for (int i = 0; i < n; i++) {
            Word q = questions.get(i);
            String answer = JOptionPane.showInputDialog(parent,
                    "(" + (i + 1) + "/" + n + ") " + q.getEng() + " 의 뜻(한국어)을 입력하세요:");
            if (answer == null) {
                log.append("사용자가 퀴즈를 중단했습니다.\n");
                break;
            }
            String userAns = answer.trim();
            if (userAns.equalsIgnoreCase(q.getKor())) {
                score++;
                log.append(i + 1).append(") ").append(q.getEng())
                        .append(" : 정답! (").append(q.getKor()).append(")\n");
            } else {
                log.append(i + 1).append(") ").append(q.getEng())
                        .append(" : 오답 (입력: ").append(userAns)
                        .append(", 정답: ").append(q.getKor()).append(")\n");
                addWrongNote(q);
            }
        }

        log.append("\n점수: ").append(score).append(" / ").append(n).append("\n");
        if (outputArea != null) outputArea.setText(log.toString());
        JOptionPane.showMessageDialog(parent,
                "퀴즈가 끝났습니다. 점수: " + score + " / " + n);
    }

    public void writtenQuizKorToEngSwing(Component parent, JTextComponent outputArea) {
        if (voc.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "단어장이 비어 있어 퀴즈를 진행할 수 없습니다.");
            return;
        }
        String input = JOptionPane.showInputDialog(parent,
                "출제할 문제 개수 (최대 " + voc.size() + "):");
        if (input == null) return;
        int n;
        try {
            n = Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(parent, "숫자를 입력해 주세요.");
            return;
        }
        if (n <= 0) return;
        if (n > voc.size()) n = voc.size();

        Vector<Word> questions = getRandomQuestions(voc, n);
        int score = 0;
        StringBuilder log = new StringBuilder("[Written Quiz KOR→ENG]\n");

        for (int i = 0; i < n; i++) {
            Word q = questions.get(i);
            String answer = JOptionPane.showInputDialog(parent,
                    "(" + (i + 1) + "/" + n + ") \"" + q.getKor() + "\" 의 영어 단어를 입력하세요:");
            if (answer == null) {
                log.append("사용자가 퀴즈를 중단했습니다.\n");
                break;
            }
            String userAns = answer.trim();
            if (userAns.equalsIgnoreCase(q.getEng())) {
                score++;
                log.append(i + 1).append(") ").append(q.getKor())
                        .append(" : 정답! (").append(q.getEng()).append(")\n");
            } else {
                log.append(i + 1).append(") ").append(q.getKor())
                        .append(" : 오답 (입력: ").append(userAns)
                        .append(", 정답: ").append(q.getEng()).append(")\n");
                addWrongNote(q);
            }
        }

        log.append("\n점수: ").append(score).append(" / ").append(n).append("\n");
        if (outputArea != null) outputArea.setText(log.toString());
        JOptionPane.showMessageDialog(parent,
                "퀴즈가 끝났습니다. 점수: " + score + " / " + n);
    }

    public void wrongNoteQuizSwing(Component parent, JTextComponent outputArea) {
        if (wrongNotes.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "오답노트가 비어 있습니다.");
            return;
        }
        String input = JOptionPane.showInputDialog(parent,
                "오답노트에서 출제할 문제 개수 (최대 " + wrongNotes.size() + "):");
        if (input == null) return;
        int n;
        try {
            n = Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(parent, "숫자를 입력해 주세요.");
            return;
        }
        if (n <= 0) return;
        if (n > wrongNotes.size()) n = wrongNotes.size();

        Vector<Word> questions = getRandomQuestions(wrongNotes, n);
        int score = 0;
        StringBuilder log = new StringBuilder("[Written Quiz from Wrong Notes]\n");
        Vector<Word> toRemove = new Vector<>();

        for (int i = 0; i < n; i++) {
            Word q = questions.get(i);
            String answer = JOptionPane.showInputDialog(parent,
                    "(" + (i + 1) + "/" + n + ") " + q.getEng() + " 의 뜻(한국어)을 입력하세요:");
            if (answer == null) {
                log.append("사용자가 퀴즈를 중단했습니다.\n");
                break;
            }
            String userAns = answer.trim();
            if (userAns.equalsIgnoreCase(q.getKor())) {
                score++;
                log.append(i + 1).append(") ").append(q.getEng())
                        .append(" : 정답! (").append(q.getKor()).append(")\n");
                toRemove.add(q);
            } else {
                log.append(i + 1).append(") ").append(q.getEng())
                        .append(" : 오답 (입력: ").append(userAns)
                        .append(", 정답: ").append(q.getKor()).append(")\n");
            }
        }

        wrongNotes.removeAll(toRemove);

        log.append("\n점수: ").append(score).append(" / ").append(n).append("\n");
        if (outputArea != null) outputArea.setText(log.toString());
        JOptionPane.showMessageDialog(parent,
                "오답노트 퀴즈가 끝났습니다. 점수: " + score + " / " + n);
    }

    /* ================== Wordle Helper + Archive ================== */

    public Word chooseWordleTarget() {
        Vector<Word> candidates = new Vector<>();
        for (Word w : voc) {
            String e = w.getEng();
            if (e != null &&
                    e.length() == 5 &&
                    !e.contains(" ") &&
                    e.matches("[A-Za-z]{5}")) {
                candidates.add(w);
            }
        }
        if (candidates.isEmpty()) return null;
        Random rand = new Random();
        return candidates.get(rand.nextInt(candidates.size()));
    }

    public static String generateWordleFeedback(String targetWord, String guess) {
        char[] result = new char[5];
        Vector<Character> remaining = new Vector<>();
        for (char c : targetWord.toCharArray()) remaining.add(c);

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

    // ---- Wordle 로그 ----
    public void appendWordleLog(String entry) {
        try (FileWriter fw = new FileWriter(wordleLogFileName, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(entry);
        } catch (IOException e) {
            System.out.println("Wordle 로그 저장 오류: " + e.getMessage());
        }
    }

    public String loadWordleLog() {
        File f = new File(wordleLogFileName);
        if (!f.exists()) return "아직 Wordle 기록이 없습니다.";

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            return "Wordle 로그를 읽는 중 오류가 발생했습니다.\n" + e.getMessage();
        }
        return sb.toString();
    }
}
