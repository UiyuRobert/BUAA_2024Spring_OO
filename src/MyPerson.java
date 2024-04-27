import com.oocourse.spec1.main.Person;

import java.util.HashMap;

public class MyPerson implements Person {
    private final int id;
    private final String name;
    private final int age;
    private final HashMap<Integer, Person> acquaintance;
    private final HashMap<Integer, Integer> value;

    public MyPerson(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
        acquaintance = new HashMap<>();
        value = new HashMap<>();
    }

    public void addFriend(Person person, int val) {
        int friendId = person.getId();
        acquaintance.put(friendId, person);
        value.put(friendId,val);
    }

    public void modifyFriendValue(int friendId, int val) {
        int oldValue = value.get(friendId);
        value.put(friendId, oldValue + val);
    }

    public void deleteFriend(int friendId) {
        acquaintance.remove(friendId);
        value.remove(friendId);
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
            return otherPerson.getId() == id;
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
}
