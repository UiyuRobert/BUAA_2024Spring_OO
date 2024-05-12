import com.oocourse.spec3.exceptions.MessageIdNotFoundException;

import java.util.HashMap;

public class MyMessageIdNotFoundException extends MessageIdNotFoundException {
    private int messageId;
    private static int sumCount = 0;
    private static HashMap<Integer, Integer> idCount = new HashMap<>(); // id -> count

    public MyMessageIdNotFoundException(int messageId) {
        this.messageId = messageId;
    }

    @Override
    public void print() {
        sumCount++;
        if (idCount.containsKey(messageId)) {
            int oldCount = idCount.get(messageId);
            idCount.put(messageId, oldCount + 1);
        } else {
            idCount.put(messageId, 1);
        }
        System.out.println("minf-" + sumCount + ", " + messageId + "-" + idCount.get(messageId));
    }
}
