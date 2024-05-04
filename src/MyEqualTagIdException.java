import com.oocourse.spec2.exceptions.EqualTagIdException;

import java.util.HashMap;

public class MyEqualTagIdException extends EqualTagIdException {
    private int tagId;
    private static int sumCount = 0;
    private static HashMap<Integer, Integer> idCount = new HashMap<>();

    public MyEqualTagIdException(int id) {
        this.tagId = id;
    }

    @Override
    public void print() {
        sumCount++;
        if (idCount.containsKey(tagId)) {
            int oldCount = idCount.get(tagId);
            idCount.put(tagId, oldCount + 1);
        } else {
            idCount.put(tagId, 1);
        }
        System.out.println("eti-" + sumCount + ", " + tagId + "-" + idCount.get(tagId));
    }
}
