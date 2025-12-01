import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Scanner scan = new Scanner(System.in);
        System.out.print("사용자 이름을 입력하세요: ");
        String name = scan.nextLine().trim();

        if (name.isEmpty()) {
            name = "User";
        }

        VocManager manager = new VocManager(name);

        // ❌ manager.makeVoc("src/words.txt");
        // ✅ параметргүй хувилбарыг дуудаж ашиглана
        manager.makeVoc();

        manager.menu();   // console version үргэлжилнэ
    }
}
