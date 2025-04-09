package uk.ac.nulondon;


import java.util.*;

class A {
    double prop;
    A next;
    A(double b){
        prop=b;
    }
}

public class Testing {
    public static void main(String[] args){
        List<A> list = new ArrayList<>();
        A sample = new A(0.1);
        sample.next = new A(0.2);
        sample.next.next = new A(0.3);
        list.add(sample);

        A curr = sample;
        while (curr!=null){
            curr.prop += 3;
            curr=curr.next;
        }

        A iter = list.getFirst();
        while (iter!=null){
            System.out.println(iter.prop);
            iter=iter.next;
        }
    }
}
