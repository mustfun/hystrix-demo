package com.dzy.learn.other;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author dengzhiyuan
 * @version 1.0
 * @date 2017/8/10
 * @since 1.0
 */
public class DecimalTest {

    @Test
    public void testDecimal(){
        BigDecimal a1=new BigDecimal(10);
        BigDecimal a2=new BigDecimal(20);
        BigDecimal a3=new BigDecimal(30);

        BigDecimal add = a1.add(a2).add(a3);

        System.out.println("a1===="+a1);
        System.out.println("add===="+add);

        Map<Integer,BigDecimal> map=new HashMap<>();
        map.put(1,a1);
        map.put(2,a2);
        map.put(3,a3);

        map.put(2,map.get(2).add(a1));  // 30
        System.out.println(map.get(2));

        map.put(null,a1.add(a3));


        System.out.println(map.size());

        System.out.println(map.get(null));

        Integer a=1;
        Integer b=new Integer(1);
        Integer c=1;

        Integer c1=-128;
        Integer b1=-128;



        System.out.println(a==b);
        System.out.println(a.equals(b));
        System.out.println(a==c);

        System.out.println(c1==b1);

    }
}
