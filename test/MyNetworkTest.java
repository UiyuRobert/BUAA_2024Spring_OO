import com.oocourse.spec3.exceptions.EqualPersonIdException;
import com.oocourse.spec3.exceptions.EqualRelationException;
import com.oocourse.spec3.exceptions.PersonIdNotFoundException;
import com.oocourse.spec3.exceptions.RelationNotFoundException;
import com.oocourse.spec3.main.*;
import com.oocourse.spec3.main.Person;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.*;
import java.util.*;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class MyNetworkTest {

    MyNetwork network;
    int limit;

    public MyNetworkTest(MyNetwork network) {
        this.network = network;
    }

    @Parameterized.Parameters
    public static Collection prepareData() throws EqualPersonIdException, RelationNotFoundException, PersonIdNotFoundException, EqualRelationException {
        int testNum = 80;
        Object[][] object = new Object[testNum][];
        object[0] = new Object[]{generateNoEdge()};
        object[1] = new Object[]{generateOneEdge(0)};
        object[2] = new Object[]{generateOneEdge(1)};
        object[3] = new Object[]{generateOneEdge(2)};
        object[4] = new Object[]{generateCompletedGraph(20)};
        object[5] = new Object[]{generateStrongData(50)};
        for (int i = 6; i < testNum; i++) {
            object[i] = new Object[]{generateStrongData(30)};
        }
        
        return Arrays.asList(object);
    }

    @Test
    public void queryTripleSumTest() throws IOException, ClassNotFoundException {
        Person[] oldPersons = deepCop(network.getPersons());

        int tripleSum = 0;
        for (int i = 0; i < network.getPersons().length; i++) {
            for (int j = i + 1; j < network.getPersons().length; j++) {
                for (int k = j + 1; k < network.getPersons().length; k++) {
                    if (((MyPerson)(network.getPersons()[i])).isLinked(((MyPerson)(network.getPersons()[j])))
                            && ((MyPerson)(network.getPersons()[j])).isLinked(((MyPerson)(network.getPersons()[k])))
                            && ((MyPerson)(network.getPersons()[k])).isLinked(((MyPerson)(network.getPersons()[i])))) {
                        tripleSum++;
                    }
                }
            }
        }

        int ans = -1;
        boolean exp = false;

        try {
            ans = network.queryTripleSum();
        } catch (Exception e) {
            exp = true;
        }
        assertEquals(false, exp);

        assertEquals(tripleSum, ans);

        Person[] newPersons = network.getPersons();
        assertEquals(oldPersons.length, newPersons.length);

        for (int i = 0; i < oldPersons.length; i++) {
            MyPerson oldPerson = ((MyPerson)oldPersons[i]);
            MyPerson newPerson = ((MyPerson)newPersons[i]);
            assertEquals(true, oldPerson.getId() == newPerson.getId());
            assertEquals(true, oldPerson.getAge() == newPerson.getAge());
            assertEquals(true, oldPerson.getName().equals(newPerson.getName()));

            for (int j = 0; j < oldPersons.length; j++) {
                MyPerson oldPer = ((MyPerson)oldPersons[j]);
                MyPerson newPer = ((MyPerson)newPersons[j]);
                if (oldPerson.isLinked(oldPer)) {
                    assertEquals(true, newPerson.isLinked(newPer));
                    assertEquals(true, oldPerson.queryValue(oldPer) == newPerson.queryValue(newPer));
                }
            }
        }

    }

    @Test
    public void queryCoupleSumTest() throws IOException, ClassNotFoundException {
        Person[] oldPersons = deepCop(network.getPersons());

        int length = network.getPersons().length;
        HashSet<Person> processed = new HashSet<>();
        Person[] curPersons = network.getPersons();
        int sum = 0;

        for (int i = 0; i < length; i++) {
            Person curPerson = curPersons[i];
            if (processed.contains(curPerson)) {
                continue;
            }
            Person curPerBest = getBestFriend(curPerson, curPersons);
            if (curPerBest == null || curPerBest.equals(curPerson)) {
                continue;
            }
            if (getBestFriend(curPerBest, curPersons) != null &&
                    getBestFriend(curPerBest, curPersons).equals(curPerson)) {
                sum++;
                processed.add(curPerson);
                processed.add(curPerBest);
            }
        }
        int ans = -1;
        boolean flag = false;

        try {
            ans = network.queryCoupleSum();
        } catch (Exception e) {
            flag = true;
        }

        assertEquals(false, flag);
        assertEquals(sum, ans);

        Person[] newPersons = network.getPersons();
        assertEquals(length, newPersons.length);

        for (int i = 0; i < oldPersons.length; i++) {
            MyPerson oldPerson = ((MyPerson)oldPersons[i]);
            MyPerson newPerson = ((MyPerson)newPersons[i]);
            assertEquals(true, oldPerson.getId() == newPerson.getId());
            assertEquals(true, oldPerson.getAge() == newPerson.getAge());
            assertEquals(true, oldPerson.getName().equals(newPerson.getName()));

            for (int j = 0; j < oldPersons.length; j++) {
                MyPerson oldPer = ((MyPerson)oldPersons[j]);
                MyPerson newPer = ((MyPerson)newPersons[j]);
                if (oldPerson.isLinked(oldPer)) {
                    assertEquals(true, newPerson.isLinked(newPer));
                    assertEquals(true, oldPerson.queryValue(oldPer) == newPerson.queryValue(newPer));
                }
            }
        }

    }

    @Test
    public void deleteColdEmojiTest() throws IOException, ClassNotFoundException {
        int[] oldEmojiIdList = listClone(network.getEmojiIdList());
        int[] oldEmojiHeatList = listClone(network.getEmojiHeatList());
        Message[] oldMessages = messageClone(network.getMessages());
        Person[] oldPersons = deepCop(network.getPersons());

        int length = network.getPersons().length;
        Person[] curPersons = network.getPersons();


        HashSet<Integer> emojiIdIndexDeleted = new HashSet<>(); // store index
        HashSet<Integer> emojiIdDeleted = new HashSet<>();
        for (int i = 0; i < oldEmojiHeatList.length; i++) {
            if (oldEmojiHeatList[i] < limit) {
                emojiIdIndexDeleted.add(i);
                emojiIdDeleted.add(oldEmojiIdList[i]);
            }
        }
        HashSet<Integer> messageIndexDeleted = new HashSet<>();
        for (int i = 0; i < oldMessages.length; i++) {
            if (oldMessages[i] instanceof EmojiMessage &&
                    emojiIdDeleted.contains(((EmojiMessage)oldMessages[i]).getEmojiId())) {
                messageIndexDeleted.add(i);
            }
        }
        boolean exp = false;
        int emojiLength = 0;
        try {
            emojiLength = network.deleteColdEmoji(limit);
        } catch (Exception e) {
            exp = true;
        }
        assertEquals(false, exp);

        assertEquals(oldEmojiIdList.length - emojiIdDeleted.size(), emojiLength);
        assertEquals(oldMessages.length - messageIndexDeleted.size(), network.getMessages().length);
        assertEquals(oldEmojiHeatList.length - emojiIdDeleted.size(), network.getEmojiHeatList().length);

        int[] newEmojiIdList = network.getEmojiIdList();
        int[] newEmojiHeatList = network.getEmojiHeatList();
        Message[] newMessages = network.getMessages();

        for (int i = 0; i < newEmojiIdList.length; i++) {
            assertEquals(false, emojiIdDeleted.contains(newEmojiIdList[i]));
            boolean flag = false;
            for (int j = 0; j < oldEmojiIdList.length; j++) {
                if (newEmojiIdList[i] == oldEmojiIdList[j] && newEmojiHeatList[i] == oldEmojiHeatList[j]) {
                    flag = true;
                    break;
                }
            }
            assertEquals(true, flag);
        }




        Person[] newPersons = network.getPersons();
        assertEquals(length, newPersons.length);



        for (int i = 0; i < oldPersons.length; i++) {
            MyPerson oldPerson = ((MyPerson)oldPersons[i]);
            MyPerson newPerson = ((MyPerson)newPersons[i]);
            assertEquals(true, oldPerson.getId() == newPerson.getId());
            assertEquals(true, oldPerson.getAge() == newPerson.getAge());
            assertEquals(true, oldPerson.getName().equals(newPerson.getName()));

            for (int j = 0; j < oldPersons.length; j++) {
                MyPerson oldPer = ((MyPerson)oldPersons[j]);
                MyPerson newPer = ((MyPerson)newPersons[j]);
                if (oldPerson.isLinked(oldPer)) {
                    assertEquals(true, newPerson.isLinked(newPer));
                    assertEquals(true, oldPerson.queryValue(oldPer) == newPerson.queryValue(newPer));
                }
            }
        }

    }

    private static int[] listClone(int[] list) {
        int[] ret = new int[list.length];
        for (int i = 0; i < list.length; i++) {
            ret[i] = list[i];
        }
        return ret;
    }

    private static Message[] messageClone(Message[] messages) {
        Message[] ret = new Message[messages.length];
        for (int i = 0; i < messages.length; i++) {
            if (messages[i] instanceof EmojiMessage) {
                MyEmojiMessage nm = (MyEmojiMessage)messages[i];
                if (nm.getType() == 0) {
                    ret[i] = new MyEmojiMessage(nm.getId(), nm.getEmojiId(), nm.getPerson1(), nm.getPerson2());
                } else {
                    ret[i] = new MyEmojiMessage(nm.getId(), nm.getEmojiId(), nm.getPerson1(), nm.getTag());
                }
            } else if (messages[i] instanceof RedEnvelopeMessage) {
                MyRedEnvelopeMessage rem = (MyRedEnvelopeMessage)messages[i];
                if (rem.getType() == 0) {
                    ret[i] = new MyRedEnvelopeMessage(rem.getId(), rem.getMoney(), rem.getPerson1(), rem.getPerson2());
                } else {
                    ret[i] = new MyRedEnvelopeMessage(rem.getId(), rem.getMoney(), rem.getPerson1(), rem.getTag());
                }
            } else if (messages[i] instanceof NoticeMessage) {
                MyNoticeMessage nm = (MyNoticeMessage)messages[i];
                if (nm.getType() == 0) {
                    ret[i] = new MyNoticeMessage(nm.getId(), nm.getString(), nm.getPerson1(), nm.getPerson2());
                } else {
                    ret[i] = new MyNoticeMessage(nm.getId(), nm.getString(), nm.getPerson1(), nm.getTag());
                }
            } else {
                if (messages[i].getType() == 0) {
                    ret[i] = new MyMessage(messages[i].getId(), messages[i].getSocialValue(), messages[i].getPerson1(), messages[i].getPerson2());
                } else {
                    ret[i] = new MyMessage(messages[i].getId(), messages[i].getSocialValue(), messages[i].getPerson1(), messages[i].getTag());
                }
            }
        }
        return ret;
    }

    public Person getBestFriend(Person person, Person[] people) {
        int maxValue = -100;
        int minId = 2147483647;
        Person bestPerson = null;

        for (int i = 0; i < people.length; i++) {
            if (person.isLinked(people[i])) {
                if (maxValue < person.queryValue(people[i]) ||
                        (maxValue == person.queryValue(people[i]) && minId > people[i].getId())) {
                    maxValue = person.queryValue(people[i]);
                    minId = people[i].getId();
                    bestPerson = people[i];
                }
            }
        }

        if (maxValue == -100) {
            return null;
        } else {
            return bestPerson;
        }

    }

    public static Person[] deepCop(Person[] des) throws IOException, ClassNotFoundException { // 深克隆
        Person[] newPersons = new Person[des.length];
        for (int i = 0; i < des.length; i++) {
            Person person = new MyPerson(des[i].getId(), des[i].getName(), des[i].getAge());

            newPersons[i] = person;
        }
        return newPersons;
    }

    public static MyNetwork generateNoEdge() throws EqualPersonIdException {
        MyNetwork myNetwork = new MyNetwork();
        int personSum = 10;
        Random random = new Random();
        for (int i = 1; i <= personSum; i++) {
            MyPerson myPerson = new MyPerson(i, "name" + i, random.nextInt(18));
            myNetwork.addPerson(myPerson);
        }
        return myNetwork;
    }

    public static MyNetwork generateOneEdge(int choice) throws EqualPersonIdException, PersonIdNotFoundException, EqualRelationException, RelationNotFoundException {
        MyNetwork myNetwork = new MyNetwork();
        int personSum = 20;
        Random random = new Random();
        for (int i = 1; i <= personSum; i++) {
            MyPerson myPerson = new MyPerson(i, "name" + i, random.nextInt(18));
            myNetwork.addPerson(myPerson);
        }

        for (int i = 1; i < personSum; i++) {
            myNetwork.addRelation(i, i + 1, random.nextInt(100));
        }
        if (choice == 1) {
            return myNetwork;
        } else if (choice == 2) {
            myNetwork.addRelation(personSum, 1, random.nextInt(100));
            return myNetwork;
        } else {
            for (int i = 1; i < personSum; i += 2) {
                myNetwork.modifyRelation(i, i + 1, random.nextInt(100) * -1);
            }
            return myNetwork;
        }
    }

    public static MyNetwork generateCompletedGraph(int num) throws EqualPersonIdException {
        MyNetwork myNetwork = new MyNetwork();
        Random random = new Random();
        for (int i = 1; i <= num; i++) {
            MyPerson myPerson = new MyPerson(i, "name" + i, random.nextInt(18));
            myNetwork.addPerson(myPerson);
        }

        for (int i = 1; i <= num; i++) {
            for (int j = i + 1; j <= num; j++) {
                int value = random.nextInt(num) + 1;
                try {
                    myNetwork.addRelation(i, j, value);
                } catch (Exception e) {
                    throw new RuntimeException("Unreachable");
                }
            }
        }

        return myNetwork;
    }

    public static MyNetwork generateStrongData(int degree) throws EqualPersonIdException {
        MyNetwork myNetwork = generateCompletedGraph(degree);
        Random random = new Random();

        for (int i = 1; i <= degree; i++) {
            for (int j = i + 1; j <= degree; j++) {
                int value = (random.nextInt(degree) + 1) * -1;
                try {
                    myNetwork.modifyRelation(i, j, value);
                } catch (Exception e) {
                    throw new RuntimeException("Unreachable");
                }
            }
        }
        return myNetwork;

    }
}