package team10;

public class Word {
    private String eng;
    private String kor;
    private int wrongCount;

    public Word(String eng, String kor) {
        this.eng = eng;
        this.kor = kor;
        this.wrongCount = 0;
    }

    public String getEng() { return eng; }
    public void setEng(String eng) { this.eng = eng; }

    public String getKor() { return kor; }
    public void setKor(String kor) { this.kor = kor; }

    public int getWrongCount() { return wrongCount; }
    public void increaseWrongCount() { wrongCount++; }

    @Override
    public String toString() {
        return eng + "\t" + kor;
    }

    // eng тэнцүү байвал нэг үг гэж үзнэ (contains() ажиллуулахын тулд)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Word)) return false;
        Word other = (Word) o;
        return eng != null && eng.equalsIgnoreCase(other.eng);
    }

    @Override
    public int hashCode() {
        return eng == null ? 0 : eng.toLowerCase().hashCode();
    }
}
