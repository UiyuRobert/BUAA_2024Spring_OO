import com.oocourse.library3.annotation.SendMessage;

public class MainClass {
    @SendMessage(from = "MainClass", to = "Library")
    public static void main(String[] args) {
        Library library = new Library();
        library.run();
    }
}
