import com.oocourse.spec1.exceptions.EqualRelationException;

import java.util.HashMap;

public class MyEqualRelationException extends EqualRelationException {
    private int id1;
    private int id2;
    private static int sumCount = 0;
    private static HashMap<Integer, Integer> idCount = new HashMap<>();

    public MyEqualRelationException(int id1, int id2) {
        this.id1 = id1;
        this.id2 = id2;
    }

    @Override
    public void print() {
        sumCount++;
        if (idCount.containsKey(id1)) {
            int oldCount = idCount.get(id1);
            idCount.put(id1, oldCount + 1);
        } else {
            idCount.put(id1, 1);
        }
        if (id1 == id2) {
            System.out.println("er-" + sumCount + ", " + id1 + "-" + idCount.get(id1) +
                    ", " + id1 + "-" + idCount.get(id1));
        }
        else {
            if (idCount.containsKey(id2)) {
                int oldCount = idCount.get(id2);
                idCount.put(id2, oldCount + 1);
            } else {
                idCount.put(id2, 1);
            }

            if (id1 < id2) {
                System.out.println("er-" + sumCount + ", " + id1 + "-" + idCount.get(id1) +
                        ", " + id2 + "-" + idCount.get(id2));
            } else {
                System.out.println("er-" + sumCount + ", " + id2 + "-" + idCount.get(id2) +
                        ", " + id1 + "-" + idCount.get(id1));
            }
        }
    }
}
