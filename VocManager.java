import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Scanner;
import java.util.Vector;

public class VocManager {

    String userName;
    Vector<Word> voc = new Vector<>();
    Vector<Word> wrongNotes = new Vector<>();
    static Scanner scan = new Scanner(System.in);

    String vocFileName = "src/words.txt";
    String wrongNoteFileName;

    public VocManager(String userName) {
        this.userName = userName;
        this.wrongNoteFileName = userName + "_wrong_notes.txt";
    }

    public Vector<Word> getVoc(){
        return voc;
    }
    public Vector<Word> getWrongNotes(){
        return wrongNotes;
    }
    public void saveAll(){
        saveVoc();
        saveWrongNotes();
    }

    // 파일 불러오기 / 저장

    public void makeVoc() {
        File f = new File(vocFileName);
        if (!f.exists()) {
            System.out.println("단어장 파일이 존재하지 않습니다. 새 파일로 시작합니다.");
        } else {
            try (Scanner file = new Scanner(f)) {
                while (file.hasNextLine()) {
                    String line = file.nextLine().trim();
                    if (line.isEmpty()) continue;
                    String[] temp = line.split("\\s+", 2);
                    if (temp.length >= 2) {
                        String eng = temp[0].trim();
                        String kor = temp[1].trim();
                        addWord(eng, kor);
                    }
                }
                System.out.println("[" + vocFileName + "] 에서 단어 " + voc.size() + "개를 불러왔습니다.");
            } catch (FileNotFoundException e) {
                System.out.println("단어장 파일을 읽을 수 없습니다: " + e.getMessage());
            }
        }

        loadWrongNotes();
    }

    // loadWrongNotes(), saveVoc(), saveWrongNotes() 안에는
    // 이미 wrongNoteFileName / vocFileName을 쓰고 있으니 그대로 두면 됩니다.

    private void loadWrongNotes() {
        File f = new File(wrongNoteFileName);
        if (!f.exists()) {
            return;
        }
        try (Scanner file = new Scanner(f)) {
            while (file.hasNextLine()) {
                String line = file.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] temp = line.split("\\t");
                if (temp.length >= 2) {
                    String eng = temp[0].trim();
                    String kor = temp[1].trim();
                    Word w = new Word(eng, kor);
                    if (!wrongNotes.contains(w)) {
                        wrongNotes.add(w);
                    }
                }
            }
            System.out.println("[오답노트] " + wrongNotes.size() + "개를 불러왔습니다.");
        } catch (FileNotFoundException e) {
            System.out.println("오답노트 파일을 읽을 수 없습니다: " + e.getMessage());
        }
    }

    private void saveVoc() {
        if (vocFileName == null) return;
        try (PrintWriter out = new PrintWriter(vocFileName)) {
            for (Word w : voc) {
                out.println(w.eng + "\t" + w.kor);
            }
            System.out.println("단어장이 [" + vocFileName + "] 에 저장되었습니다.");
        } catch (FileNotFoundException e) {
            System.out.println("단어장 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    private void saveWrongNotes() {
        try (PrintWriter out = new PrintWriter(wrongNoteFileName)) {
            for (Word w : wrongNotes) {
                out.println(w.eng + "\t" + w.kor);
            }
            System.out.println("오답노트가 [" + wrongNoteFileName + "] 에 저장되었습니다.");
        } catch (FileNotFoundException e) {
            System.out.println("오답노트 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 공통 유틸

    void addWord(String eng, String kor) {
        Word w = new Word(eng, kor);
        if (!voc.contains(w)) {
            this.voc.add(w);
        }
    }

    private int getIntInput(String msg) {
        while (true) {
            System.out.print(msg);
            String line = scan.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("숫자를 입력해 주세요.");
            }
        }
    }

    private String getEngInput(String msg) {
        while (true) {
            System.out.print(msg);
            String input = scan.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("입력이 비어 있습니다. 다시 입력해 주세요.");
                continue;
            }

            if (input.matches("\\d+")) {
                System.out.println("영단어를 입력해 주세요. 숫자만 입력하면 안 됩니다.");
                continue;
            }

            return input;
        }
    }

    private Word findWordByEng(String eng) {
        for (Word w : voc) {
            if (w.eng.equalsIgnoreCase(eng)) {
                return w;
            }
        }
        return null;
    }

    private void addWrongNote(Word w) {
        if (!wrongNotes.contains(w)) {
            wrongNotes.add(w);
        }
    }

    private Vector<Word> getRandomQuestions(Vector<Word> source, int count) {
        Vector<Word> list = new Vector<>(source);
        Collections.shuffle(list);
        if (list.size() > count) {
            Vector<Word> sub = new Vector<>();
            for (int i = 0; i < count; i++) sub.add(list.get(i));
            return sub;
        }
        return list;
    }

    // 메인 메뉴

    public void menu() {
        while (true) {
            System.out.println("\n===== Vocabulary for " + userName + " =====");
            System.out.println("1) Add    2) Edit    3) Delete    4) Search    5) List all");
            System.out.println("6) Written (ENG→KOR)    7) Written (KOR→ENG)");
            System.out.println("8) Multiple-choice (ENG→KOR)    9) Multiple-choice (KOR→ENG)");
            System.out.println("10) Written from Wrong Note");
            System.out.println("11) Save now");
            System.out.println("12) Wordle Game (5글자 영단어 맞추기)");
            System.out.println("0) Exit (자동 저장)");

            int choice = getIntInput("메뉴를 선택하세요: ");

            switch (choice) {
                case 1 -> addWordMenu();
                case 2 -> editWordMenu();
                case 3 -> deleteWordMenu();
                case 4 -> searchWordMenu();
                case 5 -> listAllWords();
                case 6 -> writtenQuizEngToKor();
                case 7 -> writtenQuizKorToEng();
                case 8 -> multiQuizEngToKor();
                case 9 -> multiQuizKorToEng();
                case 10 -> writtenQuizFromWrongNotes();
                case 11 -> {
                    saveVoc();
                    saveWrongNotes();
                }
                case 12 -> wordleQuiz();
                case 0 -> {
                    System.out.println("프로그램을 종료합니다. 자동 저장 중...");
                    saveVoc();
                    saveWrongNotes();
                    return;
                }
                default -> System.out.println("잘못된 선택입니다. 다시 입력해 주세요.");
            }
        }
    }

    // 기본 단어 기능 (CRUD, Search, List)

    private void addWordMenu() {
        String eng = getEngInput("추가할 단어(영단어)를 입력하세요 (예: computer_science): ");

        Word exist = findWordByEng(eng);
        if (exist != null) {
            System.out.println("이미 존재하는 단어입니다: " + exist);
            return;
        }

        System.out.print("뜻(한국어)을 입력하세요: ");
        String kor = scan.nextLine().trim();
        if (kor.isEmpty()) {
            System.out.println("뜻이 비어 있습니다.");
            return;
        }

        addWord(eng, kor);
        System.out.println("단어가 추가되었습니다: " + eng + " : " + kor);
    }

    private void editWordMenu() {
        String eng = getEngInput("수정할 단어(영단어)를 입력하세요: ");
        Word w = findWordByEng(eng);
        if (w == null) {
            System.out.println("해당 단어가 단어장에 없습니다.");
            return;
        }
        System.out.println("현재 단어: " + w);
        System.out.print("새로운 뜻(한국어)을 입력하세요: ");
        String newKor = scan.nextLine().trim();
        if (newKor.isEmpty()) {
            System.out.println("뜻이 비어 있습니다. 수정 취소.");
            return;
        }
        w.kor = newKor;
        System.out.println("수정 완료: " + w);
    }

    private void deleteWordMenu() {
        String eng = getEngInput("삭제할 단어(영단어)를 입력하세요: ");
        Word w = findWordByEng(eng);
        if (w == null) {
            System.out.println("해당 단어가 단어장에 없습니다.");
            return;
        }
        voc.remove(w);
        System.out.println("삭제되었습니다: " + w);
    }

    private void searchWordMenu() {
        String eng = getEngInput("검색할 단어(영단어)를 입력하세요: ");
        Word w = findWordByEng(eng);
        if (w == null) {
            System.out.println("해당 단어가 단어장에 없습니다.");
        } else {
            System.out.println("검색 결과: " + w);
        }
    }

    private void listAllWords() {
        if (voc.isEmpty()) {
            System.out.println("단어장이 비어 있습니다.");
            return;
        }
        Vector<Word> copy = new Vector<>(voc);
        copy.sort((a, b) -> a.eng.compareToIgnoreCase(b.eng));
        System.out.println("===== 전체 단어 목록 (" + copy.size() + "개) =====");
        for (Word w : copy) {
            System.out.println(w);
        }
    }

    // 주관식 퀴즈

    private void writtenQuizEngToKor() {
        if (voc.isEmpty()) {
            System.out.println("단어장이 비어 있어 퀴즈를 진행할 수 없습니다.");
            return;
        }
        int n = getIntInput("출제할 문제 개수(최대 " + voc.size() + "): ");
        if (n <= 0) {
            System.out.println("0개 이하는 출제가 불가능합니다.");
            return;
        }
        Vector<Word> questions = getRandomQuestions(voc, n);
        int score = 0;

        System.out.println("\n[주관식 퀴즈 ENG→KOR]");

        for (Word q : questions) {
            System.out.println("영단어: " + q.eng);
            System.out.print("뜻을 입력하세요: ");
            String answer = scan.nextLine().trim();

            if (answer.equalsIgnoreCase(q.kor)) {
                System.out.println("정답!");
                score++;
            } else {
                System.out.println("오답! 정답: " + q.kor);
                addWrongNote(q);
            }
            System.out.println();
        }

        System.out.println("점수: " + score + " / " + questions.size());
    }

    private void writtenQuizKorToEng() {
        if (voc.isEmpty()) {
            System.out.println("단어장이 비어 있어 퀴즈를 진행할 수 없습니다.");
            return;
        }
        int n = getIntInput("출제할 문제 개수(최대 " + voc.size() + "): ");
        if (n <= 0) {
            System.out.println("0개 이하는 출제가 불가능합니다.");
            return;
        }
        Vector<Word> questions = getRandomQuestions(voc, n);
        int score = 0;

        System.out.println("\n[주관식 퀴즈 KOR→ENG]");

        for (Word q : questions) {
            System.out.println("뜻: " + q.kor);
            System.out.print("영단어를 입력하세요: ");
            String answer = scan.nextLine().trim();

            if (answer.equalsIgnoreCase(q.eng)) {
                System.out.println("정답!");
                score++;
            } else {
                System.out.println("오답! 정답: " + q.eng);
                addWrongNote(q);
            }
            System.out.println();
        }

        System.out.println("점수: " + score + " / " + questions.size());
    }

    // 객관식 퀴즈

    private void multiQuizEngToKor() {
        if (voc.size() < 4) {
            System.out.println("객관식 퀴즈를 위해 최소 4개 이상의 단어가 필요합니다.");
            return;
        }
        int n = getIntInput("출제할 문제 개수(최대 " + voc.size() + "): ");
        if (n <= 0) {
            System.out.println("0개 이하는 출제가 불가능합니다.");
            return;
        }
        Vector<Word> questions = getRandomQuestions(voc, n);
        int score = 0;

        System.out.println("\n[객관식 퀴즈 ENG→KOR]");

        for (Word q : questions) {
            Vector<Word> options = getRandomQuestions(voc, 4);
            if (!options.contains(q)) {
                options.set(0, q);
            }
            Collections.shuffle(options);

            System.out.println("영단어: " + q.eng);
            for (int i = 0; i < options.size(); i++) {
                System.out.println((i + 1) + ") " + options.get(i).kor);
            }
            int ans = getIntInput("정답 번호를 입력하세요: ");

            if (ans < 1 || ans > options.size()) {
                System.out.println("잘못된 번호입니다. 오답 처리됩니다.");
                addWrongNote(q);
            } else if (options.get(ans - 1).equals(q)) {
                System.out.println("정답!");
                score++;
            } else {
                System.out.println("오답! 정답: " + q.kor);
                addWrongNote(q);
            }
            System.out.println();
        }
        System.out.println("점수: " + score + " / " + questions.size());
    }

    private void multiQuizKorToEng() {
        if (voc.size() < 4) {
            System.out.println("객관식 퀴즈를 위해 최소 4개 이상의 단어가 필요합니다.");
            return;
        }
        int n = getIntInput("출제할 문제 개수(최대 " + voc.size() + "): ");
        if (n <= 0) {
            System.out.println("0개 이하는 출제가 불가능합니다.");
            return;
        }
        Vector<Word> questions = getRandomQuestions(voc, n);
        int score = 0;

        System.out.println("\n[객관식 퀴즈 KOR→ENG]");

        for (Word q : questions) {
            Vector<Word> options = getRandomQuestions(voc, 4);
            if (!options.contains(q)) {
                options.set(0, q);
            }
            Collections.shuffle(options);

            System.out.println("뜻: " + q.kor);
            for (int i = 0; i < options.size(); i++) {
                System.out.println((i + 1) + ") " + options.get(i).eng);
            }
            int ans = getIntInput("정답 번호를 입력하세요: ");

            if (ans < 1 || ans > options.size()) {
                System.out.println("잘못된 번호입니다. 오답 처리됩니다.");
                addWrongNote(q);
            } else if (options.get(ans - 1).equals(q)) {
                System.out.println("정답!");
                score++;
            } else {
                System.out.println("오답! 정답: " + q.eng);
                addWrongNote(q);
            }
            System.out.println();
        }
        System.out.println("점수: " + score + " / " + questions.size());
    }

    // 오답노트 기반 퀴즈

    private void writtenQuizFromWrongNotes() {
        if (wrongNotes.isEmpty()) {
            System.out.println("오답노트가 비어 있습니다.");
            return;
        }
        int n = getIntInput("오답노트에서 출제할 문제 개수(최대 " + wrongNotes.size() + "): ");
        if (n <= 0) {
            System.out.println("0개 이하는 출제가 불가능합니다.");
            return;
        }

        Vector<Word> questions = getRandomQuestions(wrongNotes, n);
        int score = 0;

        System.out.println("\n[오답노트 주관식 퀴즈 ENG→KOR]");
        Vector<Word> toRemove = new Vector<>(); // 정답 시 제거할 단어 저장

        for (Word q : questions) {
            System.out.println("영단어: " + q.eng);
            System.out.print("뜻을 입력하세요: ");
            String answer = scan.nextLine().trim();

            if (answer.equalsIgnoreCase(q.kor)) {
                System.out.println("정답!");
                score++;
                toRemove.add(q);
            } else {
                System.out.println("오답! 정답: " + q.kor);
            }
            System.out.println();
        }

        for (Word w : toRemove) {
            wrongNotes.remove(w);
        }

        System.out.println("정답으로 맞힌 단어는 오답노트에서 제거되었습니다.");
        System.out.println("점수: " + score + " / " + questions.size());
    }
    // Wordle 창의 기능

    private void wordleQuiz() {
        Vector<Word> fiveletterWords = new Vector<>();
        for (Word w : voc) {
            if (w.eng != null &&
                    w.eng.length() == 5 &&
                    !w.eng.contains(" ") &&
                    w.eng.matches("[A-Za-z]{5}")) {
                fiveletterWords.add(w);
            }
        }

        if (fiveletterWords.isEmpty()) {
            System.out.println("Wordle 퀴즈를 위해 띄어쓰기 없는 5글자 영어 단어가 단어장에 필요합니다.");
            System.out.println("예: campus, major, thesis 같은 단어를 추가해 주세요.");
            return;
        }

        Collections.shuffle(fiveletterWords);
        Word target = fiveletterWords.get(0);
        String targetWord = target.eng.toLowerCase();
        String targetKor = target.kor;
        final int MAX_ATTEMPTS = 6;
        int attempt = 0;
        boolean guessed = false;

        System.out.println("\n===== Wordle Start (5글자 영단어, " + MAX_ATTEMPTS + "회 기회) =====");
        System.out.println("⭐ -> 정확한 위치에 있는 글자");
        System.out.println(" ⟳ -> 맞는 글자지만 위치가 틀림");
        System.out.println(" x -> 단어에 없는 글자");

        while (attempt < MAX_ATTEMPTS && !guessed) {
            attempt++;
            System.out.println("\n시도: " + attempt + "/" + MAX_ATTEMPTS);
            System.out.print("추측할 5글자 영단어를 입력하세요: ");
            String guess = scan.nextLine().trim().toLowerCase();

            if (guess.length() != 5 || guess.contains(" ")) {
                System.out.println("5글자만, 띄어쓰기 없이 입력해야 합니다.");
                attempt--;
                continue;
            }
            if (guess.matches("\\d+")) {
                System.out.println("숫자만 입력할 수 없습니다. 단어를 입력해주세요.");
                attempt--;
                continue;
            }

            if (guess.equals(targetWord)) {
                guessed = true;
                System.out.println("⭐⭐⭐⭐⭐");
                break;
            } else {
                String feedback = generateWordleFeedback(targetWord, guess);
                System.out.println(guess + "  ->  " + feedback);
            }
        }

        if (guessed) {
            System.out.println("축하합니다! " + attempt + "번 만에 정답을 맞혔습니다.");
            System.out.println(targetWord + " : " + targetKor);
        } else {
            System.out.println("아쉽습니다. 기회를 모두 사용했습니다. 정답은: " + targetWord);
            System.out.println("뜻: " + targetKor);
            addWrongNote(target);
        }
    }

    private String generateWordleFeedback(String targetWord, String guess) {
        char[] result = new char[5];
        Vector<Character> remainingTargetLetters = new Vector<>();

        for (char c : targetWord.toCharArray()) {
            remainingTargetLetters.add(c);
        }

        for (int i = 0; i < 5; i++) {
            if (guess.charAt(i) == targetWord.charAt(i)) {
                result[i] = '⭐';
                remainingTargetLetters.remove((Character) targetWord.charAt(i));
            } else {
                result[i] = ' ';
            }
        }

        for (int i = 0; i < 5; i++) {
            if (result[i] == ' ') {
                char gChar = guess.charAt(i);
                if (remainingTargetLetters.contains(gChar)) {
                    result[i] = '⟳';
                    remainingTargetLetters.remove((Character) gChar);
                } else {
                    result[i] = 'x';
                }
            }
        }

        return new String(result);
    }
}
