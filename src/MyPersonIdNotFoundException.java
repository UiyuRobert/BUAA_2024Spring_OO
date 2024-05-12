import com.oocourse.spec3.exceptions.PersonIdNotFoundException;

import java.util.HashMap;

public class MyPersonIdNotFoundException extends PersonIdNotFoundException {
    private int id;
    private static int sumCount = 0;
    private static HashMap<Integer, Integer> idCount = new HashMap<>();

    public MyPersonIdNotFoundException(int id) {
        this.id = id;
    }

    @Override
    public void print() {
        sumCount++;
        if (idCount.containsKey(id)) {
            int oldCount = idCount.get(id);
            idCount.put(id, oldCount + 1);
        } else {
            idCount.put(id, 1);
        }
        System.out.println("pinf-" + sumCount + ", " + id + "-" + idCount.get(id));
    }
}
