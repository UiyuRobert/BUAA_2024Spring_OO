import com.oocourse.spec1.exceptions.EqualPersonIdException;
import com.oocourse.spec1.exceptions.EqualRelationException;
import com.oocourse.spec1.exceptions.PersonIdNotFoundException;
import com.oocourse.spec1.exceptions.RelationNotFoundException;
import com.oocourse.spec1.main.Network;
import com.oocourse.spec1.main.Person;
import org.junit.Test;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class MyNetworkTest {

    MyNetwork network;

    public MyNetworkTest(MyNetwork network) {
        this.network = network;
    }

    @Parameterized.Parameters
    public static Collection prepareData() throws EqualPersonIdException, RelationNotFoundException, PersonIdNotFoundException, EqualRelationException {
        long seed = System.currentTimeMillis();
        Random random = new Random(seed);
        int testNum = 5;
        Object[][] object = new Object[testNum][];
        object[0] = new Object[]{generateNoEdge()};
        object[1] = new Object[]{generateOneEdge(0)};
        object[2] = new Object[]{generateOneEdge(1)};
        object[3] = new Object[]{generateOneEdge(2)};
        object[4] = new Object[]{generateCompletedGraph()};
        
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

        int ans = network.queryTripleSum();
        assertEquals(tripleSum, ans);

        Person[] newPersons = network.getPersons();
        assertEquals(oldPersons.length, newPersons.length);

        for (int i = 0; i < oldPersons.length; i++) {
            assertEquals(((MyPerson)oldPersons[i]).strictEquals(((MyPerson)newPersons[i])), true);
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
        int personSum = 10;
        Random random = new Random();
        for (int i = 1; i <= personSum; i++) {
            MyPerson myPerson = new MyPerson(i, "name" + i, random.nextInt(18));
            myNetwork.addPerson(myPerson);
        }

        for (int i = 1; i < personSum; i++) {
            myNetwork.addRelation(i, i + 1, random.nextInt(10));
        }
        if (choice == 1) {
            return myNetwork;
        } else if (choice == 2) {
            myNetwork.addRelation(personSum, 1, random.nextInt(10));
            return myNetwork;
        } else {
            for (int i = 1; i < personSum; i += 2) {
                myNetwork.modifyRelation(i, i + 1, random.nextInt(10) * -1);
            }
            return myNetwork;
        }
    }

    public static MyNetwork generateCompletedGraph() throws EqualPersonIdException {
        MyNetwork myNetwork = new MyNetwork();
        int personSum = 10;
        Random random = new Random();
        for (int i = 1; i <= personSum; i++) {
            MyPerson myPerson = new MyPerson(i, "name" + i, random.nextInt(18));
            myNetwork.addPerson(myPerson);
        }

        for (int i = 1; i <= personSum; i++) {
            for (int j = i + 1; j <= personSum; j++) {
                int value = random.nextInt(11) + 1;
                try {
                    myNetwork.addRelation(i, j, value);
                } catch (Exception e) {
                    throw new RuntimeException("Unreachable");
                }
            }
        }

        return myNetwork;
    }
}