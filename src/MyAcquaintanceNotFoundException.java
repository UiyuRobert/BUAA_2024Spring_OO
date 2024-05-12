import com.oocourse.spec3.exceptions.AcquaintanceNotFoundException;

import java.util.HashMap;

public class MyAcquaintanceNotFoundException extends AcquaintanceNotFoundException {
    private int personId;
    private static int sumCount = 0;
    private static HashMap<Integer, Integer> idCount = new HashMap<>(); // id -> count

    public MyAcquaintanceNotFoundException(int personId) {
        this.personId = personId;
    }

    @Override
    public void print() {
        sumCount++;
        if (idCount.containsKey(personId)) {
            int oldCount = idCount.get(personId);
            idCount.put(personId, oldCount + 1);
        } else {
            idCount.put(personId, 1);
        }
        System.out.println("anf-" + sumCount + ", " + personId + "-" + idCount.get(personId));
    }
}
