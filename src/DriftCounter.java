import com.oocourse.library2.LibraryBookId;

import java.util.HashSet;
import java.util.Set;

public class DriftCounter {
    private Set<LibraryBookId> driftBooks;

    public DriftCounter() {
        driftBooks = new HashSet<>();
    }

    public void donateOneBook(LibraryBookId bookId) {
        driftBooks.add(bookId);
    }

    public boolean canBeBorrowed(LibraryBookId bookId) {
        return !bookId.isTypeAU() && driftBooks.contains(bookId);
    }

    public void addBooks(Set<LibraryBookId> books2Add) {
        driftBooks.addAll(books2Add);
        books2Add.clear();
    }

    public void successBorrow(LibraryBookId bookId) {
        driftBooks.remove(bookId);
    }

    public int query(LibraryBookId bookId) {
        if (driftBooks.contains(bookId)) {
            return 1;
        }
        return 0;
    }
}
