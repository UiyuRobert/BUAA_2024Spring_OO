import com.oocourse.library3.LibraryBookId;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class User {
    private String id;
    private boolean numB;
    private boolean numBU;
    private int credits;
    private Map<LibraryBookId, Book> books;

    public User(String id) {
        this.id = id;
        numB = false;
        numBU = false;
        credits = 10;
        books = new HashMap<>();
    }

    public boolean canRenewBook(LibraryBookId bookId, LocalDate date) {
        if (bookId.isFormal() && books.containsKey(bookId)) {
            return books.get(bookId).canBeRenewed(date);
        }
        return false;
    }

    public int getCredits() {
        return credits;
    }

    public void updateCredits(LocalDate date) {
        for (Book book : books.values()) {
            if (book.meetPunishment(date)) {
                punish4NotReturn();
            }
        }
    }

    public void punish4NoPick() { credits -= 3; }

    public void punish4NotReturn() { credits -= 2; }

    public void reward4Donate() { credits = Math.min(credits + 2, 20); }

    public void reward4HotDonate() { credits = Math.min(credits + 2, 20); }

    public void reward4ReturnInTime() { credits = Math.min(credits + 1, 20); }

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
