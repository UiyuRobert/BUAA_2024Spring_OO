import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class DisjointSet {
    private HashMap<Integer, Integer> parentMap; // son -> parent
    private final HashMap<Integer, HashSet<Integer>> relationMap; // Network, personId->friendIds
    private HashMap<Integer, Integer> rankMap; // 秩 father -> father's rank
    private int blockSum;
    private int tripleSum;

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
        union(id1, id2, false);
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

    public void union(int x, int y, boolean flag) {
        int rootX = find(x);
        int rootY = find(y);
        if (rootX != rootY) {
            if (!flag) {
                blockSum--;
            }
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

    public void deleteRelation(int id1, int id2) {
        relationMap.get(id1).remove(id2);
        relationMap.get(id2).remove(id1);
        HashMap<Integer, Integer> newParentMap = new HashMap<>();
        HashMap<Integer, Integer> newRankMap = new HashMap<>();

        for (Integer i :relationMap.keySet()) {
            newParentMap.put(i, i);
            newRankMap.put(i, 0);
        }

        parentMap = newParentMap;
        rankMap = newRankMap;

        for (Integer i :relationMap.keySet()) {
            for (Integer j : relationMap.get(i)) {
                if (i < j) {
                    union(i, j, true);
                }
            }
        }

        if (find(id1) != find(id2)) {
            blockSum++;
        }

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

    public int getShortestPath(int start, int end) {
        return bfs(start, end) - 1;
    }

    private int bfs(int start, int target) {
        Queue<Integer> queue = new LinkedList<>();
        Set<Integer> visited = new HashSet<>();
        queue.offer(start);
        visited.add(start);
        int depth = 0;

        while (!queue.isEmpty()) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                int node = queue.poll();
                if (node == target) {
                    return depth;
                }
                for (int neighbor : relationMap.get(node)) {
                    if (!visited.contains(neighbor)) {
                        queue.offer(neighbor);
                        visited.add(neighbor);
                    }
                }
            }
            depth++;
        }

        return -1; // 如果没有找到目标节点，返回-1
    }

}
