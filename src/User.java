import com.oocourse.library2.LibraryBookId;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class User {
    private String id;
    private boolean numB;
    private boolean numBU;
    private Map<LibraryBookId, Book> books;

    public User(String id) {
        this.id = id;
        numB = false;
        numBU = false;
        books = new HashMap<>();
    }

    public boolean canRenewBook(LibraryBookId bookId, LocalDate date) {
        if (bookId.isFormal() && books.containsKey(bookId)) {
            return books.get(bookId).canBeRenewed(date);
        }
        return false;
    }

    public boolean returnBook(LibraryBookId bookId, LocalDate date) {
        if (bookId.isTypeB()) { numB = false; }
        else if (bookId.isTypeBU()) { numBU = false; }
        boolean ret = books.get(bookId).getDaysB4Return(date) >= 0;
        books.remove(bookId);
        return ret;
    }

    public boolean canBorrowOrOrderB() {
        return !numB;
    }

    public boolean canBorrowBU() {
        return !numBU;
    }

    public boolean canBorrowOrOrderC(LibraryBookId bookId) {
        return !books.containsKey(bookId);
    }

    public boolean canBorrowCU(LibraryBookId bookId) {
        return !books.containsKey(bookId);
    }

    public void successBorrowBook(LibraryBookId bookId, LocalDate date) {
        if (bookId.isTypeB()) { numB = true; }
        else if (bookId.isTypeBU()) { numBU = true; }
        books.put(bookId, new Book(bookId, date));
    }

    public void successRenewBook(LibraryBookId bookId) {
        books.get(bookId).renewBook();
    }

    public String getId() {
        return id;
    }
}
