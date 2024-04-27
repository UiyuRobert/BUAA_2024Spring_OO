import com.oocourse.spec1.exceptions.EqualPersonIdException;
import com.oocourse.spec1.exceptions.EqualRelationException;
import com.oocourse.spec1.exceptions.PersonIdNotFoundException;
import com.oocourse.spec1.exceptions.RelationNotFoundException;
import com.oocourse.spec1.main.Network;
import com.oocourse.spec1.main.Person;
import java.util.HashMap;

public class MyNetwork implements Network {
    private final HashMap<Integer, Person> persons; // id->person
    private final DisjointSet map;

    public MyNetwork() {
        persons = new HashMap<>();
        map = new DisjointSet();
    }

    @Override
    public boolean containsPerson(int id) {
        return persons.containsKey(id);
    }

    @Override
    public Person getPerson(int id) {
        return containsPerson(id) ? persons.get(id) : null;
    }

    public Person[] getPersons() {
        Person[] ret = new Person[persons.size()];
        int index = 0;
        for (Person person : persons.values()) {
            ret[index++] = person;
        }
        return ret;
    }

    @Override
    public void addPerson(Person person) throws EqualPersonIdException {
        if (person != null) {
            int personId = person.getId();
            if (containsPerson(personId)) {
                throw new MyEqualPersonIdException(personId);
            } else {
                persons.put(personId, person);
                map.addPerson(personId);
            }
        }
    }

    @Override
    public void addRelation(int id1, int id2, int value) throws
            PersonIdNotFoundException, EqualRelationException {
        if (containsPerson(id1) && containsPerson(id2) &&
                !getPerson(id1).isLinked(getPerson(id2))) {
            Person person1 = getPerson(id1);
            Person person2 = getPerson(id2);
            ((MyPerson) person1).addFriend(person2, value);
            ((MyPerson) person2).addFriend(person1, value);
            map.addRelation(id1, id2);
        } else if (!containsPerson(id1)) {
            throw new MyPersonIdNotFoundException(id1);
        } else if (!containsPerson(id2)) {
            throw new MyPersonIdNotFoundException(id2);
        } else {
            throw new MyEqualRelationException(id1, id2);
        }
    }

    @Override
    public void modifyRelation(int id1, int id2, int value) throws
            PersonIdNotFoundException, EqualPersonIdException, RelationNotFoundException {
        if (containsPerson(id1) && containsPerson(id2) && id1 != id2
                && getPerson(id1).isLinked(getPerson(id2))) {
            if (getPerson(id1).queryValue(getPerson(id2)) + value > 0) {
                ((MyPerson) getPerson(id1)).modifyFriendValue(id2, value);
                ((MyPerson) getPerson(id2)).modifyFriendValue(id1, value);
            } else {
                ((MyPerson) getPerson(id1)).deleteFriend(id2);
                ((MyPerson) getPerson(id2)).deleteFriend(id1);
                map.deleteRelation(id1, id2);
            }

        } else if (!containsPerson(id1)) {
            throw new MyPersonIdNotFoundException(id1);
        } else if (!containsPerson(id2)) {
            throw new MyPersonIdNotFoundException(id2);
        } else if (id1 == id2) {
            throw new MyEqualPersonIdException(id1);
        } else {
            throw new MyRelationNotFoundException(id1, id2);
        }
    }

    @Override
    public int queryValue(int id1, int id2) throws
            PersonIdNotFoundException, RelationNotFoundException {
        if (containsPerson(id1) && containsPerson(id2) && getPerson(id1).isLinked(getPerson(id2))) {
            return getPerson(id1).queryValue(getPerson(id2));
        } else if (!containsPerson(id1)) {
            throw new MyPersonIdNotFoundException(id1);
        } else if (!containsPerson(id2)) {
            throw new MyPersonIdNotFoundException(id2);
        } else {
            throw new MyRelationNotFoundException(id1, id2);
        }
    }

    @Override
    public boolean isCircle(int id1, int id2) throws PersonIdNotFoundException {
        if (containsPerson(id1) && containsPerson(id2)) {
            int represent1 = map.find(id1);
            int represent2 = map.find(id2);
            return represent1 == represent2;

        } else if (!containsPerson(id1)) {
            throw new MyPersonIdNotFoundException(id1);
        } else {
            throw new MyPersonIdNotFoundException(id2);
        }
    }

    @Override
    public int queryBlockSum() {
        return map.getBlockSum();
    }

    @Override
    public int queryTripleSum() {
        return map.getTripleSum();
    }
}
