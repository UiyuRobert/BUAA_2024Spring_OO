import com.oocourse.spec3.exceptions.AcquaintanceNotFoundException;
import com.oocourse.spec3.exceptions.EmojiIdNotFoundException;
import com.oocourse.spec3.exceptions.EqualEmojiIdException;
import com.oocourse.spec3.exceptions.EqualMessageIdException;
import com.oocourse.spec3.exceptions.EqualPersonIdException;
import com.oocourse.spec3.exceptions.EqualRelationException;
import com.oocourse.spec3.exceptions.EqualTagIdException;
import com.oocourse.spec3.exceptions.MessageIdNotFoundException;
import com.oocourse.spec3.exceptions.PathNotFoundException;
import com.oocourse.spec3.exceptions.PersonIdNotFoundException;
import com.oocourse.spec3.exceptions.RelationNotFoundException;
import com.oocourse.spec3.exceptions.TagIdNotFoundException;
import com.oocourse.spec3.main.EmojiMessage;
import com.oocourse.spec3.main.Message;
import com.oocourse.spec3.main.Network;
import com.oocourse.spec3.main.Person;
import com.oocourse.spec3.main.RedEnvelopeMessage;
import com.oocourse.spec3.main.Tag;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MyNetwork implements Network {
    private final HashMap<Integer, Person> persons; // id->person
    private final HashMap<Integer, Message> messages;
    private final HashMap<Integer, Integer> emojiId2Heat;
    private final DisjointSet map;
    private final HashMap<Integer, HashSet<Integer>> tags;
    private final HashMap<Integer, Tag> tagMap = new HashMap<>();

    public MyNetwork() {
        persons = new HashMap<>();
        map = new DisjointSet();
        tags = new HashMap<>();
        messages = new HashMap<>();
        emojiId2Heat = new HashMap<>();
    }

    @Override
    public boolean containsPerson(int id) { return persons.containsKey(id); }

    @Override
    public Person getPerson(int id) { return containsPerson(id) ? persons.get(id) : null; }

    public Person[] getPersons() {
        Person[] ret = new Person[persons.size()];
        int index = 0;
        for (Person person : persons.values()) { ret[index++] = person; }
        return ret; }

    @Override
    public void addPerson(Person person) throws EqualPersonIdException {
        if (person != null) {
            int personId = person.getId();
            if (containsPerson(personId)) { throw new MyEqualPersonIdException(personId); }
            else {
                persons.put(personId, person);
                map.addPerson(personId); } }
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
            for (Map.Entry<Integer, HashSet<Integer>> entry : tags.entrySet()) {
                if (entry.getValue().contains(id1) && entry.getValue().contains(id2)) {
                    ((MyTag)(tagMap.get(entry.getKey()))).addRelation(value); } }
        } else if (!containsPerson(id1)) { throw new MyPersonIdNotFoundException(id1); }
        else if (!containsPerson(id2)) { throw new MyPersonIdNotFoundException(id2); }
        else { throw new MyEqualRelationException(id1, id2); }
    }

    @Override
    public void modifyRelation(int id1, int id2, int value) throws
            PersonIdNotFoundException, EqualPersonIdException, RelationNotFoundException {
        if (containsPerson(id1) && containsPerson(id2) && id1 != id2
                && getPerson(id1).isLinked(getPerson(id2))) {
            int oldValue = getPerson(id1).queryValue(getPerson(id2));
            if (oldValue + value > 0) {
                ((MyPerson) getPerson(id1)).modifyFriendValue(id2, value);
                ((MyPerson) getPerson(id2)).modifyFriendValue(id1, value);
                for (Map.Entry<Integer, HashSet<Integer>> entry : tags.entrySet()) {
                    if (entry.getValue().contains(id1) && entry.getValue().contains(id2)) {
                        ((MyTag)(tagMap.get(entry.getKey()))).addRelation(value); } }
            } else {
                ((MyPerson) getPerson(id1)).deleteFriend(id2);
                ((MyPerson) getPerson(id2)).deleteFriend(id1);
                map.deleteRelation(id1, id2);
                for (Map.Entry<Integer, HashSet<Integer>> entry : tags.entrySet()) {
                    if (entry.getValue().contains(id1) && entry.getValue().contains(id2)) {
                        ((MyTag)(tagMap.get(entry.getKey()))).deleteRelation(oldValue); } }
            }
        } else if (!containsPerson(id1)) { throw new MyPersonIdNotFoundException(id1); }
        else if (!containsPerson(id2)) { throw new MyPersonIdNotFoundException(id2); }
        else if (id1 == id2) { throw new MyEqualPersonIdException(id1); }
        else { throw new MyRelationNotFoundException(id1, id2); }
    }

    @Override
    public int queryValue(int id1, int id2) throws
            PersonIdNotFoundException, RelationNotFoundException {
        if (containsPerson(id1) && containsPerson(id2) && getPerson(id1).isLinked(getPerson(id2))) {
            return getPerson(id1).queryValue(getPerson(id2));
        } else if (!containsPerson(id1)) { throw new MyPersonIdNotFoundException(id1); }
        else if (!containsPerson(id2)) { throw new MyPersonIdNotFoundException(id2); }
        else { throw new MyRelationNotFoundException(id1, id2); }
    }

    @Override
    public boolean isCircle(int id1, int id2) throws PersonIdNotFoundException {
        if (containsPerson(id1) && containsPerson(id2)) {
            int represent1 = map.find(id1);
            int represent2 = map.find(id2);
            return represent1 == represent2; }
        else if (!containsPerson(id1)) { throw new MyPersonIdNotFoundException(id1); }
        else { throw new MyPersonIdNotFoundException(id2); }
    }

    @Override
    public int queryBlockSum() { return map.getBlockSum(); }

    @Override
    public int queryTripleSum() { return map.getTripleSum(); }

    @Override
    public void addTag(int personId, Tag tag)
            throws PersonIdNotFoundException, EqualTagIdException {
        if (containsPerson(personId) && !getPerson(personId).containsTag(tag.getId())) {
            if (!tags.containsKey(tag.getId())) {
                tagMap.put(tag.getId(), tag);
                tags.put(tag.getId(), new HashSet<>()); }
            getPerson(personId).addTag(tag); }
        else if (!containsPerson(personId)) { throw new MyPersonIdNotFoundException(personId); }
        else { throw new MyEqualTagIdException(tag.getId()); }
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
                HashSet<Integer> people = tags.get(tagId);
                people.add(personId1);
                tags.put(tagId, people); }
        } else if (!containsPerson(personId1)) { throw new MyPersonIdNotFoundException(personId1); }
        else if (!containsPerson(personId2)) { throw new MyPersonIdNotFoundException(personId2); }
        else if (personId1 == personId2) { throw new MyEqualPersonIdException(personId1); }
        else if (!getPerson(personId2).isLinked(getPerson(personId1))) {
            throw new MyRelationNotFoundException(personId1, personId2);
        } else if (!getPerson(personId2).containsTag(tagId)) {
            throw new MyTagIdNotFoundException(tagId);
        } else { throw new MyEqualPersonIdException(personId1); }
    }

    @Override
    public int queryTagValueSum(int personId, int tagId)
            throws PersonIdNotFoundException, TagIdNotFoundException {
        if (containsPerson(personId) && getPerson(personId).containsTag(tagId)) {
            return getPerson(personId).getTag(tagId).getValueSum();
        } else if (!containsPerson(personId)) { throw new MyPersonIdNotFoundException(personId); }
        else { throw new MyTagIdNotFoundException(tagId); }
    }

    @Override
    public int queryTagAgeVar(int personId, int tagId)
            throws PersonIdNotFoundException, TagIdNotFoundException {
        if (containsPerson(personId) && getPerson(personId).containsTag(tagId)) {
            return getPerson(personId).getTag(tagId).getAgeVar();
        } else if (!containsPerson(personId)) { throw new MyPersonIdNotFoundException(personId); }
        else { throw new MyTagIdNotFoundException(tagId); }
    }

    @Override
    public void delPersonFromTag(int personId1, int personId2, int tagId)
            throws PersonIdNotFoundException, TagIdNotFoundException {
        if (containsPerson(personId1) && containsPerson(personId2)
                && getPerson(personId2).containsTag(tagId)
                && getPerson(personId2).getTag(tagId).hasPerson(getPerson(personId1))) {
            getPerson(personId2).getTag(tagId).delPerson(getPerson(personId1));
            HashSet<Integer> people = tags.get(tagId);
            people.remove(personId1);
            tags.put(tagId, people);
        } else if (!containsPerson(personId1)) { throw new MyPersonIdNotFoundException(personId1); }
        else if (!containsPerson(personId2)) { throw new MyPersonIdNotFoundException(personId2); }
        else if (!getPerson(personId2).containsTag(tagId)) {
            throw new MyTagIdNotFoundException(tagId);
        } else { throw new MyPersonIdNotFoundException(personId1); }
    }

    @Override
    public void delTag(int personId, int tagId)
            throws PersonIdNotFoundException, TagIdNotFoundException {
        if (containsPerson(personId) && getPerson(personId).containsTag(tagId)) {
            getPerson(personId).delTag(tagId);
        } else if (!containsPerson(personId)) { throw new MyPersonIdNotFoundException(personId); }
        else { throw new MyTagIdNotFoundException(tagId); }
    }

    @Override
    public boolean containsMessage(int id) { return messages.containsKey(id); }

    private boolean emojiMessageCheck(Message message) {
        if (message instanceof EmojiMessage) {
            return containsEmojiId(((EmojiMessage) message).getEmojiId());
        } else { return true; }
    }

    private boolean messageTypeCheck(Message message) {
        if (message.getType() == 0) {
            return !message.getPerson1().equals(message.getPerson2());
        } else { return true; }
    }

    @Override
    public void addMessage(Message message) throws EqualMessageIdException,
            EmojiIdNotFoundException, EqualPersonIdException {
        if (!containsMessage(message.getId()) && emojiMessageCheck(message)
                && messageTypeCheck(message)) {
            messages.put(message.getId(), message);
        } else if (containsMessage(message.getId())) {
            throw new MyEqualMessageIdException(message.getId());
        } else if (!emojiMessageCheck(message)) {
            throw new MyEmojiIdNotFoundException(((EmojiMessage) message).getEmojiId());
        } else { throw new MyEqualPersonIdException(message.getPerson1().getId()); }
    }

    @Override
    public Message getMessage(int id) {
        if (messages.containsKey(id)) { return messages.get(id); }
        return null;
    }

    private void processEmojiMessage(Message message) {
        if (message instanceof EmojiMessage) {
            int emojiId = ((EmojiMessage) message).getEmojiId();
            emojiId2Heat.put(emojiId, emojiId2Heat.get(emojiId) + 1);
        }
    }

    @Override
    public void sendMessage(int id) throws RelationNotFoundException,
            MessageIdNotFoundException, TagIdNotFoundException {
        if (containsMessage(id)) {
            Message message = getMessage(id);
            messages.remove(id);
            if (message.getType() == 0) {
                if (message.getPerson1().isLinked(message.getPerson2())
                        && message.getPerson1() != message.getPerson2()) {
                    Person person1 = message.getPerson1();
                    Person person2 = message.getPerson2();
                    person1.addSocialValue(message.getSocialValue());
                    person2.addSocialValue(message.getSocialValue());
                    if (message instanceof RedEnvelopeMessage) {
                        person1.addMoney(((RedEnvelopeMessage) message).getMoney() * -1);
                        person2.addMoney(((RedEnvelopeMessage) message).getMoney());
                    }
                    processEmojiMessage(message);
                    ((MyPerson) person2).addMessage(message);

                } else if (!message.getPerson1().isLinked(message.getPerson2())) {
                    int id1 = message.getPerson1().getId();
                    throw new MyRelationNotFoundException(id1, message.getPerson2().getId());
                }
            } else if (message.getType() == 1) {
                if (message.getPerson1().containsTag(message.getTag().getId())) {
                    message.getPerson1().addSocialValue(message.getSocialValue());
                    HashMap<Integer, Person> tagPersons = ((MyTag) message.getTag()).getPersons();
                    for (Person person : tagPersons.values()) {
                        person.addSocialValue(message.getSocialValue());
                    }
                    if (message instanceof RedEnvelopeMessage && !tagPersons.isEmpty()) {
                        int num = message.getTag().getSize();
                        int moneyPer = ((RedEnvelopeMessage) message).getMoney() / num;
                        message.getPerson1().addMoney(moneyPer * num * -1);
                        for (Person person : tagPersons.values()) {
                            person.addMoney(moneyPer);
                        }
                    }
                    processEmojiMessage(message);
                } else {
                    throw new MyTagIdNotFoundException(message.getTag().getId());
                }
            }
        } else {
            throw new MyMessageIdNotFoundException(id);
        }
    }

    @Override
    public int querySocialValue(int id) throws PersonIdNotFoundException {
        if (containsPerson(id)) {
            return getPerson(id).getSocialValue();
        } else {
            throw new MyPersonIdNotFoundException(id);
        }
    }

    @Override
    public List<Message> queryReceivedMessages(int id) throws PersonIdNotFoundException {
        if (containsPerson(id)) {
            return getPerson(id).getReceivedMessages();
        } else {
            throw new MyPersonIdNotFoundException(id);
        }
    }

    @Override
    public boolean containsEmojiId(int id) {
        return emojiId2Heat.keySet().contains(id);
    }

    @Override
    public void storeEmojiId(int id) throws EqualEmojiIdException {
        if (!containsEmojiId(id)) {
            emojiId2Heat.put(id, 0);
        } else {
            throw new MyEqualEmojiIdException(id);
        }
    }

    @Override
    public int queryMoney(int id) throws PersonIdNotFoundException {
        if (containsPerson(id)) {
            return getPerson(id).getMoney();
        } else {
            throw new MyPersonIdNotFoundException(id);
        }
    }

    @Override
    public int queryPopularity(int id) throws EmojiIdNotFoundException {
        if (emojiId2Heat.containsKey(id)) {
            return emojiId2Heat.get(id);
        } else {
            throw new MyEmojiIdNotFoundException(id);
        }
    }

    @Override
    public int deleteColdEmoji(int limit) {
        Iterator<Map.Entry<Integer, Integer>> iterator = emojiId2Heat.entrySet().iterator();
        HashSet<Integer> deleted = new HashSet<>();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Integer> entry = iterator.next();
            if (entry.getValue() < limit) {
                iterator.remove();
                deleted.add(entry.getKey());
            }
        }
        Iterator<Map.Entry<Integer, Message>> iteratorM = messages.entrySet().iterator();
        while (iteratorM.hasNext()) {
            Map.Entry<Integer, Message> entry = iteratorM.next();
            if (entry.getValue() instanceof EmojiMessage &&
                    deleted.contains(((EmojiMessage) entry.getValue()).getEmojiId())) {
                iteratorM.remove();
            }
        }
        return emojiId2Heat.size();
    }

    @Override
    public void clearNotices(int personId) throws PersonIdNotFoundException {
        if (containsPerson(personId)) { ((MyPerson) getPerson(personId)).clearNotice(); }
        else { throw new MyPersonIdNotFoundException(personId); }
    }

    @Override
    public int queryBestAcquaintance(int id)
            throws PersonIdNotFoundException, AcquaintanceNotFoundException {
        if (containsPerson(id) && ((MyPerson)getPerson(id)).getAcquaintanceLength() != 0) {
            return ((MyPerson)getPerson(id)).getBestAcquaintance();
        } else if (!containsPerson(id)) { throw new MyPersonIdNotFoundException(id); }
        else { throw new MyAcquaintanceNotFoundException(id); }
    }

    @Override
    public int queryCoupleSum() {
        int sum = 0;
        HashSet<Integer> processed = new HashSet<>();
        for (Map.Entry<Integer, Person> entry : persons.entrySet()) {
            int curId = entry.getKey(); // 目前遍历到的人
            if (processed.contains(curId)) { continue; }
            else { // 未处理
                MyPerson person = ((MyPerson)entry.getValue());
                if (person.getAcquaintanceLength() == 0) { continue; }
                int bestAc = person.getBestAcquaintance();
                MyPerson otherPerson = (MyPerson)persons.get(bestAc);
                if (otherPerson.getBestAcquaintance() == curId) {
                    sum++;
                    processed.add(curId);
                    processed.add(bestAc); } } }
        return sum;
    }

    @Override
    public int queryShortestPath(int id1, int id2)
            throws PersonIdNotFoundException, PathNotFoundException {
        if (containsPerson(id1) && containsPerson(id2) && isCircle(id1, id2)) {
            if (id1 == id2) { return 0; }
            return map.getShortestPath(id1, id2);
        } else if (!containsPerson(id1)) { throw new MyPersonIdNotFoundException(id1); }
        else if (!containsPerson(id2)) { throw new MyPersonIdNotFoundException(id2); }
        else { throw new MyPathNotFoundException(id1, id2); }
    }

    public Message[] getMessages() { return null; }

    public int[] getEmojiIdList() { return null; }

    public int[] getEmojiHeatList() { return null; }
}
