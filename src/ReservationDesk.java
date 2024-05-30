import com.oocourse.library2.LibraryBookId;
import com.oocourse.library2.LibraryMoveInfo;
import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ReservationDesk {
    private final List<Map.Entry<LibraryBookId, String>> reservationTable;
    private final List<Map.Entry<LibraryBookId, String>> arrived;
    private final List<Order> timeTable;
    private static final int KEEPLIMIT = 5;

    public ReservationDesk() {
        reservationTable = new LinkedList<>();
        arrived = new LinkedList<>();
        timeTable = new LinkedList<>();
    }

    public boolean existReservation4Book(LibraryBookId bookId) {
        for (Map.Entry<LibraryBookId, String> entry : reservationTable) {
            if (entry.getKey().equals(bookId)) {
                return true;
            }
        }
        for (Map.Entry<LibraryBookId, String> entry : arrived) {
            if (entry.getKey().equals(bookId)) {
                return true;
            }
        }
        return false;
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

    public boolean pickOneBook(LibraryBookId bookId, User student, LocalDate date) {
        Iterator<Map.Entry<LibraryBookId, String>> iterator = arrived.iterator();
        Iterator<Order> orderIterator = timeTable.iterator();
        while (iterator.hasNext()) {
            Map.Entry<LibraryBookId, String> entry = iterator.next();
            orderIterator.next();
            if (entry.getKey().equals(bookId) && entry.getValue().equals(student.getId())) {
                if (checkStuLimit(bookId, student)) {
                    iterator.remove();
                    orderIterator.remove();
                    student.successBorrowBook(bookId, date);
                    return true;
                }
                break;
            }
        }
        return false;
    }

    public void cleanOverTimeBooks(ArrayList<LibraryMoveInfo> moveInfos, LocalDate date) {
        Iterator<Order> iterator = timeTable.iterator();
        Iterator<Map.Entry<LibraryBookId, String>> arrivedIterator = arrived.iterator();
        while (iterator.hasNext()) {
            Order order = iterator.next();
            Map.Entry<LibraryBookId, String> entry = arrivedIterator.next();
            if (order.getResidenceTime(date) >= KEEPLIMIT) {
                moveInfos.add(new LibraryMoveInfo(entry.getKey(), "ao", "bs"));
                iterator.remove();
                arrivedIterator.remove();
            }
        }
    }

    public boolean try2MoveOneBook(Map<LibraryBookId, Integer> bookShelf, LibraryBookId bookId,
                                   String studentId, ArrayList<LibraryMoveInfo> moveInfos) {
        int bookNum = bookShelf.get(bookId);
        if (bookShelf.containsKey(bookId) && bookNum > 0) {
            moveInfos.add(new LibraryMoveInfo(bookId, "bs", "ao", studentId));
            bookShelf.put(bookId, bookNum - 1);
            return true;
        }
        return false;
    }

    public void move2FinishReservation(Map<LibraryBookId, Integer> bookShelf, LocalDate date,
                                      ArrayList<LibraryMoveInfo> moveInfos) {
        Iterator<Map.Entry<LibraryBookId, String>> iterator = reservationTable.iterator();
        while (iterator.hasNext()) {
            Map.Entry<LibraryBookId, String> entry = iterator.next();
            LibraryBookId bookId = entry.getKey();
            String stuId = entry.getValue();
            if (try2MoveOneBook(bookShelf, bookId, stuId, moveInfos)) {
                arrived.add(new AbstractMap.SimpleEntry<>(bookId, stuId));
                timeTable.add(new Order(date, stuId));
                iterator.remove();
            }
        }
    }

    private void addOneReservation(User student, LibraryBookId bookId) {
        reservationTable.add(new AbstractMap.SimpleEntry<>(bookId, student.getId()));
    }

    public boolean reserveOneBook(User student, LibraryBookId bookId) {
        if (bookId.isTypeA()) {
            return false;
        } else if (checkStuLimit(bookId, student)) {
            addOneReservation(student, bookId);
            return true;
        }
        return false;
    }
}
