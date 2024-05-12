import com.oocourse.spec3.exceptions.EmojiIdNotFoundException;

import java.util.HashMap;

public class MyEmojiIdNotFoundException extends EmojiIdNotFoundException {
    private int emojiId;
    private static int sumCount = 0;
    private static HashMap<Integer, Integer> idCount = new HashMap<>(); // id -> count

    public MyEmojiIdNotFoundException(int emojiId) {
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
        System.out.println("einf-" + sumCount + ", " + emojiId + "-" + idCount.get(emojiId));
    }
}
