import com.oocourse.spec2.exceptions.AcquaintanceNotFoundException;
import com.oocourse.spec2.exceptions.EqualPersonIdException;
import com.oocourse.spec2.exceptions.EqualRelationException;
import com.oocourse.spec2.exceptions.EqualTagIdException;
import com.oocourse.spec2.exceptions.PathNotFoundException;
import com.oocourse.spec2.exceptions.PersonIdNotFoundException;
import com.oocourse.spec2.exceptions.RelationNotFoundException;
import com.oocourse.spec2.exceptions.TagIdNotFoundException;
import com.oocourse.spec2.main.Network;
import com.oocourse.spec2.main.Person;
import com.oocourse.spec2.main.Tag;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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

    @Override
    public void addTag(int personId, Tag tag)
            throws PersonIdNotFoundException, EqualTagIdException {
        if (containsPerson(personId) && !getPerson(personId).containsTag(tag.getId())) {

            getPerson(personId).addTag(tag);

        } else if (!containsPerson(personId)) {
            throw new MyPersonIdNotFoundException(personId);
        } else {
            throw new MyEqualTagIdException(tag.getId());
        }
    }

    @Override
    public void addPersonToTag(int personId1, int personId2, int tagId)
            throws PersonIdNotFoundException, RelationNotFoundException,
            TagIdNotFoundException, EqualPersonIdException {
        if (containsPerson(personId1) && containsPerson(personId2) && personId1 !=  personId2
                && getPerson(personId2).isLinked(getPerson(personId1))
                && getPerson(personId2).containsTag(tagId)
                && !getPerson(personId2).getTag(tagId).hasPerson(getPerson(personId1))) {

            int personInTagLength = getPerson(personId2).getTag(tagId).getSize();
            if (personInTagLength <= 1111) {
                getPerson(personId2).getTag(tagId).addPerson(getPerson(personId1));
            }

        } else if (!containsPerson(personId1)) {
            throw new MyPersonIdNotFoundException(personId1);
        } else if (!containsPerson(personId2)) {
            throw new MyPersonIdNotFoundException(personId2);
        } else if (personId1 == personId2) {
            throw new MyEqualPersonIdException(personId1);
        } else if (!getPerson(personId2).isLinked(getPerson(personId1))) {
            throw new MyRelationNotFoundException(personId1, personId2);
        } else if (!getPerson(personId2).containsTag(tagId)) {
            throw new MyTagIdNotFoundException(tagId);
        } else {
            throw new MyEqualPersonIdException(personId1);
        }
    }

    @Override
    public int queryTagValueSum(int personId, int tagId)
            throws PersonIdNotFoundException, TagIdNotFoundException {
        if (containsPerson(personId) && getPerson(personId).containsTag(tagId)) {

            return getPerson(personId).getTag(tagId).getValueSum();

        } else if (!containsPerson(personId)) {
            throw new MyPersonIdNotFoundException(personId);
        } else {
            throw new MyTagIdNotFoundException(tagId);
        }
    }

    @Override
    public int queryTagAgeVar(int personId, int tagId)
            throws PersonIdNotFoundException, TagIdNotFoundException {
        if (containsPerson(personId) && getPerson(personId).containsTag(tagId)) {

            return getPerson(personId).getTag(tagId).getAgeVar();

        } else if (!containsPerson(personId)) {
            throw new MyPersonIdNotFoundException(personId);
        } else {
            throw new MyTagIdNotFoundException(tagId);
        }
    }

    @Override
    public void delPersonFromTag(int personId1, int personId2, int tagId)
            throws PersonIdNotFoundException, TagIdNotFoundException {
        if (containsPerson(personId1) && containsPerson(personId2)
                && getPerson(personId2).containsTag(tagId)
                && getPerson(personId2).getTag(tagId).hasPerson(getPerson(personId1))) {

            getPerson(personId2).getTag(tagId).delPerson(getPerson(personId1));

        } else if (!containsPerson(personId1)) {
            throw new MyPersonIdNotFoundException(personId1);
        } else if (!containsPerson(personId2)) {
            throw new MyPersonIdNotFoundException(personId2);
        } else if (!getPerson(personId2).containsTag(tagId)) {
            throw new MyTagIdNotFoundException(tagId);
        } else {
            throw new MyPersonIdNotFoundException(personId1);
        }
    }

    @Override
    public void delTag(int personId, int tagId)
            throws PersonIdNotFoundException, TagIdNotFoundException {
        if (containsPerson(personId) && getPerson(personId).containsTag(tagId)) {

            getPerson(personId).delTag(tagId);

        } else if (!containsPerson(personId)) {
            throw new MyPersonIdNotFoundException(personId);
        } else {
            throw new MyTagIdNotFoundException(tagId);
        }
    }

    @Override
    public int queryBestAcquaintance(int id)
            throws PersonIdNotFoundException, AcquaintanceNotFoundException {
        if (containsPerson(id) && ((MyPerson)getPerson(id)).getAcquaintanceLength() != 0) {

            return ((MyPerson)getPerson(id)).getBestAcquaintance();

        } else if (!containsPerson(id)) {
            throw new MyPersonIdNotFoundException(id);
        } else {
            throw new MyAcquaintanceNotFoundException(id);
        }
    }

    @Override
    public int queryCoupleSum() {
        int sum = 0;
        HashSet<Integer> processed = new HashSet<>();
        for (Map.Entry<Integer, Person> entry : persons.entrySet()) {
            int curId = entry.getKey(); // 目前遍历到的人
            if (processed.contains(curId)) { // 如果已经处理过
                continue; // 跳过
            } else { // 未处理
                // curId 的 best
                MyPerson person = ((MyPerson)entry.getValue());
                if (person.getAcquaintanceLength() == 0) {
                    continue;
                }
                int bestAc = person.getBestAcquaintance();

                MyPerson otherPerson = (MyPerson)persons.get(bestAc);
                if (otherPerson.getBestAcquaintance() == curId) {
                    sum++;
                    processed.add(curId);
                    processed.add(bestAc);
                }
            }
        }
        return sum;
    }

    @Override
    public int queryShortestPath(int id1, int id2)
            throws PersonIdNotFoundException, PathNotFoundException {
        if (containsPerson(id1) && containsPerson(id2) && isCircle(id1, id2)) {

            if (id1 == id2) {
                return 0;
            }
            return map.getShortestPath(id1, id2);

        } else if (!containsPerson(id1)) {
            throw new MyPersonIdNotFoundException(id1);
        } else if (!containsPerson(id2)) {
            throw new MyPersonIdNotFoundException(id2);
        } else {
            throw new MyPathNotFoundException(id1, id2);
        }
    }
}
