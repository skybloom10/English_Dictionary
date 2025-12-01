import java.util.Objects;

public class Word {
    String eng;
    String kor;

    public Word(String eng, String kor) {
        super();
        this.eng = eng;
        this.kor = kor;
    }

    @Override
    public String toString() {
        return eng + " : " + kor;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Word word)) return false;
        return Objects.equals(eng, word.eng) && Objects.equals(kor, word.kor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eng, kor);
    }
}
