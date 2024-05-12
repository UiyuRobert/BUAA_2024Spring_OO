import com.oocourse.spec3.main.Person;
import com.oocourse.spec3.main.Tag;
import java.util.HashMap;

public class MyTag implements Tag {
    private final int id;
    private int valueSum;
    private int ageSum;
    private final HashMap<Integer, Person> persons;

    public MyTag(int id) {
        this.id = id;
        valueSum = 0;
        ageSum = 0;
        persons = new HashMap<>();
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void addPerson(Person person) {
        persons.put(person.getId(), person);
        for (Person personIn : persons.values()) {
            if (personIn.isLinked(person)) {
                valueSum += personIn.queryValue(person) * 2;
            }
        }
        ageSum += person.getAge();

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
        return valueSum;
    }

    @Override
    public int getAgeMean() {
        if (persons.isEmpty()) {
            return 0;
        }
        return ageSum / persons.size();
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
            ageSum -= person.getAge();
            persons.remove(person.getId());
            for (Person personIn : persons.values()) {
                if (personIn.isLinked(person)) {
                    valueSum -= personIn.queryValue(person) * 2;
                }
            }
        }
    }

    @Override
    public int getSize() {
        return persons.size();
    }

    public void addRelation(int value) {
        valueSum += value * 2;
    }

    public void deleteRelation(int value) {
        valueSum -= value * 2;
    }

    public HashMap<Integer, Person> getPersons() {
        return persons;
    }
}
