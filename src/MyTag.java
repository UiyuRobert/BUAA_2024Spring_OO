import com.oocourse.spec2.main.Person;
import com.oocourse.spec2.main.Tag;
import java.util.HashMap;

public class MyTag implements Tag {
    private final int id;
    private final HashMap<Integer, Person> persons;

    public MyTag(int id) {
        this.id = id;
        persons = new HashMap<>();
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void addPerson(Person person) {
        persons.put(person.getId(), person);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Tag) {
            return (((Tag) obj).getId() == id);
        } else {
            return false;
        }
    }

    @Override
    public boolean hasPerson(Person person) {
        return persons.containsKey(person.getId());
    }

    @Override
    public int getValueSum() {
        int sum = 0;
        for (Person person1 : persons.values()) {
            for (Person person2 : persons.values()) {
                if (person1.isLinked(person2)) {
                    sum += person1.queryValue(person2);
                }
            }
        }
        return sum;
    }

    @Override
    public int getAgeMean() {
        if (persons.isEmpty()) {
            return 0;
        }
        int sumAge = 0;
        for (Person person : persons.values()) {
            sumAge += person.getAge();
        }
        return sumAge / persons.size();
    }

    @Override
    public int getAgeVar() {
        if (persons.isEmpty()) {
            return 0;
        }
        int ageMean = getAgeMean();
        int varSum = 0;
        for (Person person : persons.values()) {
            varSum += (person.getAge() - ageMean) * (person.getAge() - ageMean);
        }
        return varSum / persons.size();
    }

    @Override
    public void delPerson(Person person) {
        if (hasPerson(person)) {
            persons.remove(person.getId());
        }
    }

    @Override
    public int getSize() {
        return persons.size();
    }
}
