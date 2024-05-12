import com.oocourse.spec3.exceptions.TagIdNotFoundException;

import java.util.HashMap;

public class MyTagIdNotFoundException extends TagIdNotFoundException {
    private int tagId;
    private static int sumCount = 0;
    private static HashMap<Integer, Integer> idCount = new HashMap<>();

    public MyTagIdNotFoundException(int id) {
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
        System.out.println("tinf-" + sumCount + ", " + tagId + "-" + idCount.get(tagId));
    }
}
