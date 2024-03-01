package expr;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

public class Poly { // 多项式
    private ArrayList<Mono> monoList;
    private HashMap<Integer, BigInteger> monoMap;

    public Poly() {
        this.monoList = new ArrayList<>();
        this.monoMap = new HashMap<>();
    }

    public Poly(BigInteger coe, int exp) { // 用 Mono 创建一个 Poly
        this.monoList = new ArrayList<>();
        this.monoMap = new HashMap<>();
        monoMap.put(exp, coe);
        monoList.add(new Mono(coe, exp));
    }

    public ArrayList<Mono> getMonoList() {
        return monoList;
    }

    public HashMap<Integer, BigInteger> getMonoMap() {
        return monoMap;
    }

    public Poly monoToPoly(BigInteger coe, int exp) {
        monoList.add(new Mono(coe, exp));
        monoMap.put(exp, coe);
        return this;
    }

    public Poly addPoly(Poly poly) { // 多项式相加,返回相加后的新的多项式
        Poly newPoly = new Poly();
        HashMap<Integer, BigInteger> tmpMap = poly.getMonoMap(); // 可能为 null
        for (Integer expThis : monoMap.keySet()) {
            if (tmpMap.containsKey(expThis)) {
                newPoly.addMono(new Mono(monoMap.get(expThis).add(tmpMap.get(expThis)), expThis));
                tmpMap.remove(expThis); // 另一个加数去掉已经加过的单项式
            } else {
                newPoly.addMono(new Mono(monoMap.get(expThis), expThis));
            }
        }
        for (Integer expOut : tmpMap.keySet()) {
            newPoly.addMono(new Mono(tmpMap.get(expOut), expOut));
        }
        return newPoly;
    }

    public Poly mulPoly(Poly poly) { // 多项式相乘，返回相乘后新的多项式
        Poly newPoly = new Poly();
        for (Integer expThis : monoMap.keySet()) { // 指数集合
            HashMap<Integer, BigInteger> tmpMap = poly.getMonoMap();
            for (Integer expOut : tmpMap.keySet()) {
                newPoly = newPoly.addPoly(new Poly(monoMap.get(expThis)
                        .multiply(tmpMap.get(expOut)), expThis + expOut));
            }
        }
        return newPoly;
    }

    public Poly powPoly(int exp) { // () ^ exp 展开
        Poly newPoly = new Poly(new BigInteger("1"), 0);
        for (int i = 0; i < exp; ++i) {
            newPoly = newPoly.mulPoly(this);
        }
        return newPoly;
    }

    public void addMono(Mono mono) { // 直接操作，不返回新的 Poly 对象
        this.monoList.add(mono);
        this.monoMap.put(mono.getExp(), mono.getCoe());
    }

    @Override
    public String toString() {
        if (monoList.isEmpty()) {
            return "0";
        }
        StringBuilder sb = new StringBuilder();
        for (Mono mono : monoList) {
            sb.append("+");
            sb.append(mono.toString());
        }
        return sb.toString();
    }
}
