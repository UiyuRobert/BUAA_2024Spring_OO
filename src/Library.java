import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryCommand;
import com.oocourse.library1.LibraryMoveInfo;
import com.oocourse.library1.LibraryRequest;
import com.oocourse.library1.LibrarySystem;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Library {
    private final Map<LibraryBookId, Integer> books; //
    private final CirculationDesk circulationDesk;
    private final ReservationDesk reservationDesk;
    private final Map<String, User> students;
    private static LocalDate date;

    public Library() {
        circulationDesk = new CirculationDesk();
        reservationDesk = new ReservationDesk();
        books = LibrarySystem.SCANNER.getInventory();
        students = new HashMap<>();
    }

    public void run() {
        LibraryCommand<?> command;
        while ((command = LibrarySystem.SCANNER.nextCommand()) != null) {
            if (command.getCmd().equals("OPEN")) {
                date = command.getDate();
                cleanUpWhenOpen();
            } else if (command.getCmd().equals("CLOSE")) {
                date = command.getDate();
                cleanUpWhenClose();
            } else {
                LibraryRequest.Type type = ((LibraryRequest)command.getCmd()).getType();
                switch (type) {
                    case QUERIED:
                        query((LibraryRequest)command.getCmd());
                        break;
                    case BORROWED:
                        borrow((LibraryRequest)command.getCmd());
                        break;
                    case RETURNED:
                        returnBook((LibraryRequest)command.getCmd());
                        break;
                    case ORDERED:
                        order((LibraryRequest)command.getCmd());
                        break;
                    default:
                        pick((LibraryRequest)command.getCmd());
                }
            }
        }
    }

    private void pick(LibraryRequest request) {
        LibraryBookId bookId = request.getBookId();
        User student = getStudent(request.getStudentId());
        if (reservationDesk.pickOneBook(bookId, student)) {
            LibrarySystem.PRINTER.accept(date, request);
        } else {
            LibrarySystem.PRINTER.reject(date, request);
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
        reservationDesk.move2FinishReservation(books, date, moveInfos);
        LibrarySystem.PRINTER.move(date, moveInfos);
    }

    private void cleanUpWhenClose() {
        ArrayList<LibraryMoveInfo> moveInfos = new ArrayList<>();
        moveBooksFromBroToBs(moveInfos);
        LibrarySystem.PRINTER.move(date, moveInfos);
    }

    private void query(LibraryRequest request) {
        LibraryBookId bookId = request.getBookId();
        // User student = getStudent(request.getStudentId());
        int numHas = 0;
        // int num = 0;
        if (books.containsKey(bookId)) {
            numHas = books.get(bookId);
        }
        /* if (bookId.isTypeB()) {
            if (student.canBorrowOrOrderB()) {
                num = Math.min(numHas, 1);
            }
        } else if (bookId.isTypeC()) {
            if (student.canBorrowOrOrderC(bookId)) {
                num = Math.min(numHas, 1);
            }
        } */
        System.out.println("[" + date + "] " + bookId + " " + numHas);
    }

    private void returnBook(LibraryRequest request) {
        LibraryBookId bookId = request.getBookId();
        circulationDesk.returnBook(bookId);
        LibrarySystem.PRINTER.accept(date, request);
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

    private void borrow(LibraryRequest request) {
        LibraryBookId bookId = request.getBookId();
        if (!books.containsKey(bookId) || books.get(bookId) == 0) {
            LibrarySystem.PRINTER.reject(date, request);
        } else if (bookId.isTypeA()) {
            LibrarySystem.PRINTER.reject(date, request);
        } else {
            User student = getStudent(request.getStudentId());
            boolean ret = circulationDesk.borrow(student, bookId);
            int num = books.get(bookId) - 1;
            books.put(bookId, num);
            if (ret) {
                LibrarySystem.PRINTER.accept(date, request);
            } else {
                LibrarySystem.PRINTER.reject(date, request);
            }
        }
    }

    private void order(LibraryRequest request) {
        LibraryBookId bookId = request.getBookId();
        User student = getStudent(request.getStudentId());
        boolean ret = reservationDesk.reserveOneBook(student, bookId);
        if (ret) {
            LibrarySystem.PRINTER.accept(date, request);
        } else {
            LibrarySystem.PRINTER.reject(date, request);
        }
    }
}
