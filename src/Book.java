import com.oocourse.library3.LibraryBookId;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Book {
    private LibraryBookId bookId;
    private LocalDate returnDate;
    private boolean hasBeenPunished;

    public Book(LibraryBookId bookId, LocalDate startDate) { // initial borrow
        this.bookId = bookId;
        hasBeenPunished = false;
        setInitialReturnDate(startDate);
    }

    private void setInitialReturnDate(LocalDate startDate) {
        if (bookId.isTypeB()) {
            returnDate = startDate.plusDays(30);
        } else if (bookId.isTypeC()) {
            returnDate = startDate.plusDays(60);
        } else if (bookId.isTypeBU()) {
            returnDate = startDate.plusDays(7);
        } else {
            returnDate = startDate.plusDays(14);
        }
    }

    public boolean meetPunishment(LocalDate date) {
        if (!hasBeenPunished && getDaysB4Return(date) < 0) {
            hasBeenPunished = true;
            return true;
        }
        return false;
    }

    public LibraryBookId getBookId() {
        return bookId;
    }

    public int getDaysB4Return(LocalDate date) {
        long daysBetween = ChronoUnit.DAYS.between(date, returnDate);
        return (int) daysBetween;
    }

    public boolean canBeRenewed(LocalDate date) {
        int daysBetween = getDaysB4Return(date);
        return 0 <= daysBetween && daysBetween < 5;
    }

    public void renewBook() {
        returnDate = returnDate.plusDays(30);
    }
}
