import com.oocourse.library1.LibraryBookId;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CirculationDesk {
    private Map<LibraryBookId, Integer> books;

    public CirculationDesk() {
        books = new HashMap<>();
    }

    public ArrayList<Map.Entry<LibraryBookId, Integer>> move4CleanUp() {
        ArrayList<Map.Entry<LibraryBookId, Integer>> books2Move = new ArrayList<>();
        Map<LibraryBookId, Integer> newBooks = new HashMap<>();
        for (Map.Entry<LibraryBookId, Integer> entry : books.entrySet()) {
            if (entry.getValue() != 0) {
                books2Move.add(new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue()));
            }
            newBooks.put(entry.getKey(), 0);
        }
        books = newBooks;
        return books2Move;
    }

    private boolean checkStuLimit(LibraryBookId bookId, User student) {
        if (bookId.isTypeB()) {
            return student.canBorrowOrOrderB();
        } else if (bookId.isTypeC()) {
            return student.canBorrowOrOrderC(bookId);
        } else {
            return false;
        }
    }

    public boolean borrow(User student, LibraryBookId bookId) {
        if (checkStuLimit(bookId, student)) {
            student.successBorrowBook(bookId);
            return true;
        } else {
            addOneBook(bookId);
            return false;
        }
    }

    public void returnBook(LibraryBookId bookId) {
        addOneBook(bookId);
    }

    private void addOneBook(LibraryBookId bookId) {
        if (books.containsKey(bookId)) {
            int num = books.get(bookId) + 1;
            books.put(bookId, num);
        } else {
            books.put(bookId, 1);
        }
    }
}
