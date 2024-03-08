package expr;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;

public class Poly implements Serializable { // 多项式
    private ArrayList<Mono> monoList;

    public Poly() {
        this.monoList = new ArrayList<>();
    }

    public ArrayList<Mono> getMonoList() {
        return monoList;
    }

    public Poly monoToPoly(BigInteger coe, int exp) { // ax^n 到多项式
        monoList.add(new Mono(coe, exp));
        return this;
    }

    public Poly monoToPoly(Mono mono) {
        monoList.add(mono);
        return this;
    }

    public Poly addPoly(Poly poly) throws IOException, ClassNotFoundException { // 多项式相加,返回相加后的新的多项式
        Poly newPoly = new Poly();
        Poly poly1 = deepCop(this);
        Poly poly2 = deepCop(poly);
        ArrayList<Mono> tmpMonoList = poly2.getMonoList();
        Iterator<Mono> iteratorIn = poly1.getMonoList().iterator();
        while (iteratorIn.hasNext()) {
            Mono monoIn = iteratorIn.next();
            Iterator<Mono> iteratorOut = tmpMonoList.iterator(); // 有删除操作，用迭代器
            while (iteratorOut.hasNext()) {
                Mono monoOut = iteratorOut.next();
                if (monoIn.canAdd(monoOut)) { // 如果可以相加
                    newPoly.addMono(monoIn.add(monoOut)); // 得到的 和 加入新 Poly
                    iteratorOut.remove(); // 删除被加数
                    iteratorIn.remove(); // 删除加数
                    break;
                }
                if (poly1.getMonoList().isEmpty()) {
                    break;
                }
            }
        }
        // 剩余未合并 Mono 补入 newPoly
        for (Mono mono : poly1.getMonoList()) { // 合并后的 in多项式的 单项式集合，可能为 null，若为 null 则退出循环
            newPoly.addMono(mono);
        }
        for (Mono mono : tmpMonoList) {
            newPoly.addMono(mono);
        }
        return newPoly;
    }

    public Poly mulPoly(Poly poly) throws IOException, ClassNotFoundException { // 多项式相乘，返回相乘后新的多项式
        Poly newPoly = new Poly();
        for (Mono monoIn : this.getMonoList()) {
            for (Mono monoOut : poly.getMonoList()) {
                Poly tmpPoly = new Poly();
                tmpPoly = tmpPoly.monoToPoly(monoIn.mul(monoOut));
                newPoly = newPoly.addPoly(tmpPoly);
            }
        }
        return newPoly;
    }

    public Poly powPoly(int exp) throws IOException, ClassNotFoundException { // () ^ exp 展开
        Poly newPoly = new Poly();
        newPoly = newPoly.monoToPoly(BigInteger.ONE,0);
        for (int i = 0; i < exp; ++i) {
            newPoly = newPoly.mulPoly(this);
        }
        return newPoly;
    }

    public boolean isPolyNull() {
        return monoList.isEmpty();
    }

    public void addMono(Mono mono) { // 直接操作，不返回新的 Poly 对象
        this.monoList.add(mono);
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

    @Override
    public boolean equals(Object object) {
        if (object instanceof Poly) {
            Poly poly = (Poly) object;
            if (monoList.isEmpty() && poly.getMonoList().isEmpty()) {
                return true;
            } else if ((!monoList.isEmpty()) && (!poly.getMonoList().isEmpty())
                    && monoList.size() == poly.getMonoList().size()) {
                return monoList.containsAll(poly.getMonoList())
                        && poly.getMonoList().containsAll(monoList);
            }
            return false;
        }
        return false;
    }

    public Poly deepCop(Poly des) throws IOException, ClassNotFoundException { // 深克隆
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(des);
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        Poly poly = (Poly) ois.readObject();//从流中把数据读出来
        bos.close();
        bis.close();
        return poly;
    }
}
