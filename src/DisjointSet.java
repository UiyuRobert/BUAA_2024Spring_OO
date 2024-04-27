import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class DisjointSet {
    private final HashMap<Integer, Integer> parentMap; // son -> parent
    private final HashMap<Integer, HashSet<Integer>> relationMap; // Network, personId->friendIds
    private final HashMap<Integer, Integer> rankMap; // 秩 father -> father's rank
    private int blockSum;
    private int tripleSum;
    private static boolean flag;

    public DisjointSet() {
        parentMap = new HashMap<>();
        relationMap = new HashMap<>();
        rankMap = new HashMap<>();
        blockSum = 0;
        tripleSum = 0;
    }

    public void addPerson(int personId) {
        if (!parentMap.containsKey(personId)) {
            parentMap.put(personId, personId);
            relationMap.put(personId, new HashSet<>());
            rankMap.put(personId, 0);
            blockSum++;
        }
    }

    public void addRelation(int id1, int id2) { //
        union(id1, id2);
        relationMap.get(id1).add(id2);
        relationMap.get(id2).add(id1);
        addTriple(id1, id2);
    }

    public int getBlockSum() {
        return blockSum;
    }

    public int getTripleSum() {
        return tripleSum;
    }

    public int find(int id) {
        int rep = id; //representation element
        while (rep != parentMap.get(rep)) {
            rep = parentMap.get(rep);
        }

        int now = id;
        while (now != rep) {
            int father = parentMap.get(now);
            parentMap.put(now, rep);
            now = father;
        }

        return rep;
    }

    public void union(int x, int y) {
        int rootX = find(x);
        int rootY = find(y);
        if (rootX != rootY) {
            blockSum--;
            int rankX = rankMap.get(rootX);
            int rankY = rankMap.get(rootY);
            if (rankX < rankY) {
                parentMap.put(rootX, rootY);
            } else if (rankX > rankY) {
                parentMap.put(rootY, rootX);
            } else {
                parentMap.put(rootY, rootX);
                rankMap.put(rootX, rankX + 1); // 更新秩
            }
        }
    }

    private void dfs(int node, Set<Integer> visited, int startNode, int targetNode) {
        visited.add(node);
        if (node == targetNode) {
            flag = true;
        }
        if (flag) {
            return;
        }
        for (int neighbor : relationMap.get(node)) {
            if (!visited.contains(neighbor)) {
                dfs(neighbor, visited, startNode, targetNode);
                if (flag) {
                    return;
                }
            }
        }
        parentMap.put(node, startNode);
        rankMap.put(node, 0);
        visited.remove(node);
    }

    public void deleteRelation(int id1, int id2) {
        relationMap.get(id1).remove(id2);
        relationMap.get(id2).remove(id1);
        flag = false; // is B in the way
        Set<Integer> visited = new HashSet<>();
        if (relationMap.get(id1).size() < relationMap.get(id2).size()) {
            dfs(id1, visited, id1, id2);
            if (!flag) {
                blockSum++;
                parentMap.put(id2, id2);
            }
        } else {
            dfs(id2, visited, id2, id1);
            if (!flag) {
                blockSum++;
                parentMap.put(id1, id1);
            }
        }
        rankMap.put(id1, 1);
        subTriple(id1, id2);
    }

    private void addTriple(int x, int y) {
        for (int neighbor : relationMap.get(x)) {
            if (relationMap.get(neighbor).contains(y)) {
                tripleSum++;
            }
        }
    }

    private void subTriple(int x, int y) {
        for (int neighbor : relationMap.get(x)) {
            if (relationMap.get(neighbor).contains(y)) {
                tripleSum--;
            }
        }
    }

}
