import com.oocourse.library3.LibraryBookId;
import com.oocourse.library3.LibraryCloseCmd;
import com.oocourse.library3.LibraryCommand;
import com.oocourse.library3.LibraryMoveInfo;
import com.oocourse.library3.LibraryOpenCmd;
import com.oocourse.library3.LibraryQcsCmd;
import com.oocourse.library3.LibraryReqCmd;
import com.oocourse.library3.LibraryRequest;
import com.oocourse.library3.LibrarySystem;
import com.oocourse.library3.annotation.Trigger;

import static com.oocourse.library3.LibrarySystem.PRINTER;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Library {
    private final Map<LibraryBookId, Integer> books; //
    private final CirculationDesk circulationDesk;
    private final ReservationDesk reservationDesk;
    private final DriftCounter driftCounter;
    private final Map<String, User> students;
    private final Map<LibraryBookId, User> donateBooks;
    private static LocalDate date;

    public Library() {
        circulationDesk = new CirculationDesk();
        reservationDesk = new ReservationDesk();
        driftCounter = new DriftCounter();
        donateBooks = new HashMap<>();
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
            } else if (command instanceof LibraryQcsCmd) {
                LibraryQcsCmd request = (LibraryQcsCmd) command;
                query(request);
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
        User student = getStudent(request.getStudentId());
        driftCounter.donateOneBook(bookId);
        donateBooks.put(bookId, student);
        student.reward4Donate();
        PRINTER.accept(request);
    }

    private void renew(LibraryReqCmd request) {
        LibraryBookId bookId = request.getBookId();
        User student = getStudent(request.getStudentId());
        if (!bookId.isFormal() || student.getCredits() < 0) {
            PRINTER.reject(request);
            return;
        }
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

    private void processOverTimeBooks(List<Map.Entry<LibraryBookId, String>> overTimeBooks) {
        for (Map.Entry<LibraryBookId, String> entry : overTimeBooks) {
            LibraryBookId bookId = entry.getKey();
            User student = getStudent(entry.getValue());
            student.punish4NoPick();
            if (books.containsKey(bookId)) {
                int num = books.get(bookId) + 1;
                books.put(bookId, num);
            } else {
                books.put(bookId, 1);
            }

        }
    }

    public void updateStuCredit() {
        for (User stu : students.values()) {
            stu.updateCredits(date);
        }
    }

    private void cleanUpWhenOpen() {
        updateStuCredit();
        ArrayList<LibraryMoveInfo> moveInfos = new ArrayList<>();
        List<Map.Entry<LibraryBookId, String>> overTimeBooks =
                reservationDesk.cleanOverTimeBooks(moveInfos, date);
        processOverTimeBooks(overTimeBooks);

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
            User stu = donateBooks.get(bookId);
            stu.reward4HotDonate();
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

    private void query(LibraryQcsCmd request) {
        User stu = getStudent(request.getStudentId());
        int credits = stu.getCredits();
        PRINTER.info(request, credits);
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
        if (ret) {
            PRINTER.accept(request, "not overdue");
            student.reward4ReturnInTime();
        }
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
                driftCounter.borrow(bookId);
                if (ret) {
                    PRINTER.accept(request);
                } else { PRINTER.reject(request); }
            } else {
                PRINTER.reject(request);
            }
        }

    }

    @Trigger(from = "bs", to = "ao")
    private void order(LibraryReqCmd request) {
        LibraryBookId bookId = request.getBookId();
        User student = getStudent(request.getStudentId());
        if (!bookId.isFormal() || student.getCredits() < 0) {
            PRINTER.reject(request);
            return;
        }
        boolean ret = reservationDesk.reserveOneBook(student, bookId);
        if (ret) {
            PRINTER.accept(request);
        } else {
            PRINTER.reject(request);
        }
    }
}
