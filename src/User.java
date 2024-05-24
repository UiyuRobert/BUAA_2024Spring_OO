import com.oocourse.library1.LibraryBookId;

import java.util.HashMap;
import java.util.Map;

public class User {
    private String id;
    private boolean numB;
    private Map<LibraryBookId, Integer> books;

    public User(String id) {
        this.id = id;
        numB = false;
        books = new HashMap<>();
    }

    public void returnBook(LibraryBookId bookId) {
        if (bookId.isTypeB()) {
            numB = false;
        }
        int num = books.get(bookId) - 1;
        books.put(bookId, num);
    }

    public int getBookNum(LibraryBookId bookId) {
        if (books.containsKey(bookId)) {
            return books.get(bookId);
        }
        return 0;
    }

    public boolean canBorrowOrOrderB() {
        return !numB;
    }

    public boolean canBorrowOrOrderC(LibraryBookId bookId) {
        return !books.containsKey(bookId) || books.get(bookId) == 0;
    }

    public void successBorrowBook(LibraryBookId bookId) {
        if (bookId.isTypeB()) {
            numB = true;
            books.put(bookId, 1);
        } else {
            if (books.containsKey(bookId)) {
                int num = books.get(bookId) + 1;
                books.put(bookId, num);
            } else {
                books.put(bookId, 1);
            }
        }
    }

    public String getId() {
        return id;
    }
}
