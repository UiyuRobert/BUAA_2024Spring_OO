import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Order {
    private LocalDate startDate;
    private String stuId;

    public Order(LocalDate startDate, String stuId) {
        this.startDate = startDate;
        this.stuId = stuId;
    }

    public int getResidenceTime(LocalDate date) {
        long residenceTime = ChronoUnit.DAYS.between(startDate, date);
        return (int) residenceTime;
    }
}
