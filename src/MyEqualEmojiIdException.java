import com.oocourse.spec3.exceptions.EqualEmojiIdException;

import java.util.HashMap;

public class MyEqualEmojiIdException extends EqualEmojiIdException {
    private int emojiId;
    private static int sumCount = 0;
    private static HashMap<Integer, Integer> idCount = new HashMap<>(); // id -> count

    public MyEqualEmojiIdException(int emojiId) {
        this.emojiId = emojiId;
    }

    @Override
    public void print() {
        sumCount++;
        if (idCount.containsKey(emojiId)) {
            int oldCount = idCount.get(emojiId);
            idCount.put(emojiId, oldCount + 1);
        } else {
            idCount.put(emojiId, 1);
        }
        System.out.println("eei-" + sumCount + ", " + emojiId + "-" + idCount.get(emojiId));
    }
}
