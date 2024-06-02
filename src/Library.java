import com.oocourse.library2.LibraryBookId;
import com.oocourse.library2.LibraryCloseCmd;
import com.oocourse.library2.LibraryCommand;
import com.oocourse.library2.LibraryMoveInfo;
import com.oocourse.library2.LibraryOpenCmd;
import com.oocourse.library2.LibraryReqCmd;
import com.oocourse.library2.LibraryRequest;
import com.oocourse.library2.LibrarySystem;
import com.oocourse.library2.annotation.Trigger;

import static com.oocourse.library2.LibrarySystem.PRINTER;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Library {
    private final Map<LibraryBookId, Integer> books; //
    private final CirculationDesk circulationDesk;
    private final ReservationDesk reservationDesk;
    private final DriftCounter driftCounter;
    private final Map<String, User> students;
    private static LocalDate date;

    public Library() {
        circulationDesk = new CirculationDesk();
        reservationDesk = new ReservationDesk();
        driftCounter = new DriftCounter();
        books = LibrarySystem.SCANNER.getInventory();
        students = new HashMap<>();
    }

    @Trigger(from = "InitState", to = "bs")
    public void run() {
        LibraryCommand command;
        while ((command = LibrarySystem.SCANNER.nextCommand()) != null) {
            if (command instanceof LibraryOpenCmd) {
                date = command.getDate();
                cleanUpWhenOpen();
            } else if (command instanceof LibraryCloseCmd) {
                date = command.getDate();
                cleanUpWhenClose();
            } else {
                LibraryReqCmd req = (LibraryReqCmd) command;
                LibraryRequest.Type type = req.getType();
                switch (type) {
                    case QUERIED:
                        query(req);
                        break;
                    case BORROWED:
                        borrow(req);
                        break;
                    case RETURNED:
                        returnBook(req);
                        break;
                    case ORDERED:
                        order(req);
                        break;
                    case RENEWED:
                        renew(req);
                        break;
                    case DONATED:
                        donate(req);
                        break;
                    default:
                        pick(req);
                }
            }
        }
    }

    @Trigger(from = "user", to = "bdc")
    private void donate(LibraryReqCmd request) {
        LibraryBookId bookId = request.getBookId();
        driftCounter.donateOneBook(bookId);
        PRINTER.accept(request);
    }

    private void renew(LibraryReqCmd request) {
        LibraryBookId bookId = request.getBookId();
        if (!bookId.isFormal()) {
            PRINTER.reject(request);
            return;
        }
        User student = getStudent(request.getStudentId());
        if (student.canRenewBook(bookId, date)) {
            if ((!books.containsKey(bookId) || books.get(bookId) == 0)
                    && reservationDesk.existReservation4Book(bookId)) {
                PRINTER.reject(request);
            } else {
                student.successRenewBook(bookId);
                PRINTER.accept(request);
            }
        } else {
            PRINTER.reject(request);
        }
    }

    @Trigger(from = "ao", to = "user")
    private void pick(LibraryReqCmd request) {
        LibraryBookId bookId = request.getBookId();
        User student = getStudent(request.getStudentId());
        if (reservationDesk.pickOneBook(bookId, student, date)) {
            PRINTER.accept(request);
        } else {
            PRINTER.reject(request);
        }
    }

    private void moveBooksFromBroToBs(ArrayList<LibraryMoveInfo> moveInfos) {
        ArrayList<Map.Entry<LibraryBookId, Integer>> books2Move = circulationDesk.move4CleanUp();
        for (Map.Entry<LibraryBookId, Integer> entry : books2Move) {
            LibraryBookId bookId = entry.getKey();
            int bookNum = entry.getValue();
            int num = books.get(bookId) + bookNum;
            books.put(bookId, num);
            for (int i = 0; i < bookNum; ++i) {
                moveInfos.add(new LibraryMoveInfo(bookId, "bro", "bs"));
            }
        }
    }

    private void refillBookShelf(ArrayList<LibraryMoveInfo> moveInfos) {
        for (LibraryMoveInfo moveInfo : moveInfos) {
            LibraryBookId bookId = moveInfo.getBookId();
            if (books.containsKey(bookId)) {
                int num = books.get(bookId) + 1;
                books.put(bookId, num);
            } else {
                books.put(bookId, 1);
            }
        }
    }

    private void cleanUpWhenOpen() {
        ArrayList<LibraryMoveInfo> moveInfos = new ArrayList<>();
        reservationDesk.cleanOverTimeBooks(moveInfos, date);
        refillBookShelf(moveInfos);

        ArrayList<LibraryMoveInfo> driftMoveInfos = circulationDesk.moveDriftBooks();
        moveInfos.addAll(driftMoveInfos);
        driftCounter.addBooks(circulationDesk.getDriftBooks());
        addBooks2BookShelf(circulationDesk.getDrift2FormalBooks());

        reservationDesk.move2FinishReservation(books, date, moveInfos);
        PRINTER.move(date, moveInfos);
    }

    @Trigger(from = "bdc", to = "bs")
    private void addBooks2BookShelf(Set<LibraryBookId> books2Add) {
        for (LibraryBookId bookId : books2Add) {
            books.put(bookId.toFormal(), 1);
        }
        books2Add.clear();
    }

    @Trigger(from = "bro", to = "bs")
    private void cleanUpWhenClose() {
        ArrayList<LibraryMoveInfo> moveInfos = new ArrayList<>();
        moveBooksFromBroToBs(moveInfos);
        PRINTER.move(date, moveInfos);
    }

    private void query(LibraryReqCmd request) {
        LibraryBookId bookId = request.getBookId();
        int numHas = 0;
        if (bookId.isFormal()) {
            if (books.containsKey(bookId)) {
                numHas = books.get(bookId);
            }
        } else {
            numHas = driftCounter.query(bookId);
        }
        PRINTER.info(request, numHas);
    }

    @Trigger(from = "user", to = "bro")
    private void returnBook(LibraryReqCmd request) {
        LibraryBookId bookId = request.getBookId();
        User student = getStudent(request.getStudentId());
        boolean ret = circulationDesk.returnBook(bookId, student, date);
        if (ret) { PRINTER.accept(request, "not overdue"); }
        else { PRINTER.accept(request, "overdue"); }
    }

    private User getStudent(String studentId) {
        User student;
        if (students.containsKey(studentId)) {
            student = students.get(studentId);
        }
        else {
            student = new User(studentId);
            students.put(studentId, student);
        }
        return student;
    }

    @Trigger(from = "bs", to = "user")
    private void borrow(LibraryReqCmd request) {
        LibraryBookId bookId = request.getBookId();
        User student = getStudent(request.getStudentId());
        if (bookId.isFormal()) {
            if (!books.containsKey(bookId) || books.get(bookId) == 0 || bookId.isTypeA()) {
                PRINTER.reject(request);
            } else {
                boolean ret = circulationDesk.borrow(student, bookId, date);
                int num = books.get(bookId) - 1;
                books.put(bookId, num);
                if (ret) { PRINTER.accept(request); }
                else { PRINTER.reject(request); }
            }
        } else {
            if (driftCounter.canBeBorrowed(bookId)) {
                boolean ret = circulationDesk.borrow(student, bookId, date);
                if (ret) {
                    driftCounter.successBorrow(bookId);
                    PRINTER.accept(request);
                } else { PRINTER.reject(request); }
            } else { PRINTER.reject(request); }
        }

    }

    @Trigger(from = "bs", to = "ao")
    private void order(LibraryReqCmd request) {
        LibraryBookId bookId = request.getBookId();
        if (!bookId.isFormal()) {
            PRINTER.reject(request);
            return;
        }
        User student = getStudent(request.getStudentId());
        boolean ret = reservationDesk.reserveOneBook(student, bookId);
        if (ret) {
            PRINTER.accept(request);
        } else {
            PRINTER.reject(request);
        }
    }
}
