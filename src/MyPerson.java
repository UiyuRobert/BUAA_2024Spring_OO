import com.oocourse.spec3.main.Message;
import com.oocourse.spec3.main.NoticeMessage;
import com.oocourse.spec3.main.Person;
import com.oocourse.spec3.main.Tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MyPerson implements Person {
    private final int id;
    private final String name;
    private final int age;
    private int money;
    private int socialValue;
    private int bestAcquaintanceValue;
    private int bestFriendId;
    private final LinkedList<Message> messages;
    private boolean isDirty;
    private final HashMap<Integer, Person> acquaintance;
    private final HashMap<Integer, Integer> value;
    private final HashMap<Integer, Tag> tags;
    private static final int MIN = -2147483648;
    private static final int MAX = 2147483647;

    public MyPerson(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
        acquaintance = new HashMap<>();
        value = new HashMap<>();
        tags = new HashMap<>();
        isDirty = false;
        bestAcquaintanceValue = MIN;
        bestFriendId =  MAX;
        messages = new LinkedList<>();
    }

    public void addFriend(Person person, int val) {
        int friendId = person.getId();
        acquaintance.put(friendId, person);
        value.put(friendId,val);
        if (!isDirty && ((val > bestAcquaintanceValue)
                || (val == bestAcquaintanceValue && friendId < bestFriendId))) {
            bestAcquaintanceValue = val;
            bestFriendId = friendId;
        }
    }

    public void modifyFriendValue(int friendId, int val) {
        int newValue = value.get(friendId) + val;
        value.put(friendId, newValue);
        if (!isDirty && ((newValue > bestAcquaintanceValue)
                || (newValue == bestAcquaintanceValue && friendId < bestFriendId))) {
            bestAcquaintanceValue = newValue;
            bestFriendId = friendId;
        } else if (!isDirty && friendId == bestFriendId) {
            isDirty = true;
            bestAcquaintanceValue = MIN;
            bestFriendId = MAX;
        }
    }

    public void deleteFriend(int friendId) {
        Person person = acquaintance.remove(friendId);
        value.remove(friendId);
        for (Tag tag : tags.values()) {
            tag.delPerson(person);
        }

        if (!isDirty && bestFriendId == friendId) {
            isDirty = true;
            bestAcquaintanceValue = MIN;
            bestFriendId = MAX;
        }
    }

    public int getBestAcquaintance() {
        if (isDirty) {
            int maxValue = MIN;
            int minId = MAX;
            for (Map.Entry<Integer, Integer> entry : value.entrySet()) {
                int curValue = entry.getValue();
                int curId = entry.getKey();

                if (curValue > maxValue || (curValue == maxValue && curId < minId)) {
                    maxValue = curValue;
                    minId = curId;
                }
            }

            bestAcquaintanceValue = maxValue;
            bestFriendId = minId;

            isDirty = false;
        }
        return bestFriendId;
    }

    public int getAcquaintanceLength() {
        return acquaintance.size();
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getAge() {
        return age;
    }

    @Override
    public boolean containsTag(int id) {
        return tags.containsKey(id);
    }

    @Override
    public Tag getTag(int id) {
        if (containsTag(id)) {
            return tags.get(id);
        } else {
            return null;
        }
    }

    @Override
    public void addTag(Tag tag) {
        int tagId = tag.getId();
        if (!containsTag(tagId)) {
            tags.put(tagId, tag);
        }
    }

    @Override
    public void delTag(int id) {
        if (containsTag(id)) {
            tags.remove(id);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Person) {
            Person otherPerson = (Person) obj;
            return otherPerson.getId() == id;
        } else {
            return false;
        }
    }

    public boolean strictEquals(Object obj) {
        if (obj instanceof Person) {
            Person otherPerson = (Person) obj;
            return otherPerson.getId() == id && otherPerson.getName().equals(name);
        } else {
            return false;
        }
    }

    @Override
    public boolean isLinked(Person person) {
        int otherPersonId = person.getId();
        return acquaintance.containsKey(otherPersonId) || otherPersonId == id;
    }

    @Override
    public int queryValue(Person person) {
        int otherPersonId = person.getId();
        if (acquaintance.containsKey(otherPersonId)) {
            return value.get(otherPersonId);
        } else {
            return 0;
        }
    }

    @Override
    public void addSocialValue(int num) {
        socialValue = socialValue + num;
    }

    @Override
    public int getSocialValue() {
        return socialValue;
    }

    @Override
    public List<Message> getMessages() {
        return messages;
    }

    @Override
    public List<Message> getReceivedMessages() {
        List<Message> receivedMessages = new ArrayList<>();
        for (int i = 0; i < messages.size() && i < 5; i++) {
            receivedMessages.add(i, messages.get(i));
        }
        return receivedMessages;
    }

    @Override
    public void addMoney(int num) {
        money += num;
    }

    @Override
    public int getMoney() {
        return money;
    }

    public void addMessage(Message message) {
        messages.addFirst(message);
    }

    public void clearNotice() {
        Iterator<Message> iterator = messages.iterator();
        while (iterator.hasNext()) {
            Message message = iterator.next();
            if (message instanceof NoticeMessage) {
                iterator.remove();
            }
        }
    }
}
