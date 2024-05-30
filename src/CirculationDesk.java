import com.oocourse.library2.LibraryBookId;
import com.oocourse.library2.LibraryMoveInfo;

import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CirculationDesk {
    private Map<LibraryBookId, Integer> books;
    private Set<LibraryBookId> driftBooks;
    private Map<LibraryBookId, Integer> borrowTime;
    private Set<LibraryBookId> drift2FormalBooks;

    public CirculationDesk() {
        books = new HashMap<>();
        driftBooks = new HashSet<>();
        borrowTime = new HashMap<>();
        drift2FormalBooks = new HashSet<>();
    }

    public Set<LibraryBookId> getDriftBooks() { return driftBooks; }

    public Set<LibraryBookId> getDrift2FormalBooks() { return drift2FormalBooks; }

    public ArrayList<LibraryMoveInfo> moveDriftBooks() {
        ArrayList<LibraryMoveInfo> driftMoveInfos = new ArrayList<>();
        for (LibraryBookId bookId : driftBooks) {
            driftMoveInfos.add(new LibraryMoveInfo(bookId, "bro", "bdc"));
        }
        for (LibraryBookId bookId : drift2FormalBooks) {
            driftMoveInfos.add(new LibraryMoveInfo(bookId, "bro", "bs"));
        }
        return driftMoveInfos;
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
        if (bookId.isTypeB()) { return student.canBorrowOrOrderB(); }
        else if (bookId.isTypeC()) { return student.canBorrowOrOrderC(bookId); }
        else if (bookId.isTypeBU()) { return student.canBorrowBU(); }
        else if (bookId.isTypeCU()) { return student.canBorrowCU(bookId); }
        else { return false; }
    }

    private void addOneDonatedBook(LibraryBookId bookId) {
        if (borrowTime.containsKey(bookId)) {
            drift2FormalBooks.add(bookId);
            driftBooks.remove(bookId);
            borrowTime.remove(bookId);
        } else {
            borrowTime.put(bookId, 1);
            driftBooks.add(bookId);
        }
    }

    public boolean borrow(User student, LibraryBookId bookId, LocalDate date) {
        if (checkStuLimit(bookId, student)) {
            student.successBorrowBook(bookId, date);
            return true;
        } else {
            if (bookId.isFormal()) { addOneFormalBook(bookId); }
            else { driftBooks.add(bookId); }
            return false;
        }
    }

    public boolean returnBook(LibraryBookId bookId, User student, LocalDate date) {
        boolean ret = student.returnBook(bookId, date);
        if (bookId.isFormal()) { addOneFormalBook(bookId); }
        else { addOneDonatedBook(bookId); }
        return ret;
    }

    private void addOneFormalBook(LibraryBookId bookId) {
        if (books.containsKey(bookId)) {
            int num = books.get(bookId) + 1;
            books.put(bookId, num);
        } else {
            books.put(bookId, 1);
        }
    }
}
