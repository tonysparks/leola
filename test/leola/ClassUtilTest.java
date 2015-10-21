/*
 * see license.txt
 */
package leola;


import leola.vm.types.LeoDouble;
import leola.vm.types.LeoInteger;
import leola.vm.types.LeoLong;
import leola.vm.types.LeoNativeClass;
import leola.vm.types.LeoObject;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Tony
 *
 */
public class ClassUtilTest {

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    static class TestClass {
        public void none() {            
        }
        
        public boolean oneBoolean(boolean b) {
            System.out.println(b);
            return b;
        }
        public Boolean oneBoolean(Boolean b) {
            System.out.println(b);
            return b;
        }
        public void oneByte(byte i, String s) {
            Assert.fail();            
        }
        public byte oneByte(byte b) {
            System.out.println(b);
            return b;
        }
        public Byte oneByte(Byte b) {
            System.out.println(b);
            return b;
        }
        
        public void oneByte(String b) {
            Assert.fail();            
        }
        public void oneByte(int i, String s) {
            Assert.fail();            
        }
        
        public char oneCharacter(char b) {
            System.out.println(b);
            return b;
        }
        public Character oneCharacter(Character b) {
            System.out.println(b);
            return b;
        }
        public short oneShort(short b) {
            System.out.println(b);
            return b;
        }
        public Short oneShort(Short b) {
            System.out.println(b);
            return b;
        }
        
        public int oneInteger(int b) {
            System.out.println(b);
            return b;
        }
        public Integer oneInteger(Integer b) {
            System.out.println(b);
            return b;
        }
        
        public long oneLong(long b) {
            System.out.println(b);
            return b;
        }
        public Long oneLong(Long b) {
            System.out.println(b);
            return b;
        }
        
        public double oneDouble(double b) {
            System.out.println(b);
            return b;
        }
        public Double oneDouble(Double b) {
            System.out.println(b);
            return b;
        }
        
        public float oneFloat(float b) {
            System.out.println(b);
            return b;
        }
        public Float oneFloat(Float b) {
            System.out.println(b);
            return b;
        }
        
        public Float three(Float b, Double d, String str) {
            System.out.println(b + ":" + d + ":" + str);
            return b;
        }
        public Float three(Double d, Float b, String str) {
            System.out.println(b + ":" + d + ":" + str);
            return b;
        }
        public Float threeScale(Double d, Float b, String str) {
            System.out.println(b + ":" + d + ":" + str);
            return b;
        }
        public Float threeScale(Double d, Float b) {
            System.out.println(b + ":" + d);
            return b;
        }
        public Double threeScale(Double d) {
            System.out.println(d);
            return d;
        }
        public void threeScale() {
            System.out.println("void");            
        }
        
        public Double failType(Double d) {
            Assert.fail();
            return d;
        }
    }
    
    @Test
    public void test() {
        LeoNativeClass nClass = new LeoNativeClass(new TestClass());
        final String[] types = {"Byte", "Short", "Integer", "Long", "Double", "Float"};
        for(String type : types) {
            LeoObject method = nClass.getObject("one" + type);
            
            LeoInteger i = LeoInteger.valueOf(120);
            Assert.assertEquals(i.asLong(), method.xcall(i).asLong());
            
            LeoLong l = LeoLong.valueOf(120);
            Assert.assertEquals(l.asLong(), method.xcall(l).asLong());
            
            LeoDouble d = LeoDouble.valueOf(120);
            Assert.assertEquals(d.asLong(), method.xcall(d).asLong());
            
            LeoDouble d2 = LeoDouble.valueOf(120.1);
            Assert.assertEquals(d2.asLong(), method.xcall(d2).asLong());
            
            LeoObject o = LeoObject.valueOf(120.0);
            Assert.assertEquals(o.asLong(), method.xcall(o).asLong());
            
        }
        
        LeoObject method = nClass.getObject("three");
        method.xcall(LeoObject.valueOf(10.3), LeoObject.valueOf(4), LeoObject.valueOf("hello"));
        method.xcall(LeoObject.valueOf(10.3), LeoObject.valueOf(4));
        method.xcall(LeoObject.valueOf(10.3));
        method.xcall();
        
        
        method = nClass.getObject("threeScale");
        method.xcall(LeoObject.valueOf(10.3), LeoObject.valueOf(4), LeoObject.valueOf("hello"));
        method.xcall(LeoObject.valueOf(10.3), LeoObject.valueOf(4));
        method.xcall(LeoObject.valueOf(10.3));
        method.xcall();
        //Assert.assertTrue(nClass.getObject("failType").call(LeoObject.valueOf("blowup")).isError());
        Assert.assertTrue(nClass.getObject("failType").call(LeoObject.valueOf("blowup")).isError());
    }

}
