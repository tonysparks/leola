package leola;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import leola.vm.types.LeoArray;
import leola.vm.types.LeoMap;
import leola.vm.types.LeoObject;

public class FromLeoObjectTest {

    public static class TestA {
        public boolean b;
        public char c;
        public byte byt;
        public short s;
        public int i;
        public float f;
        public double d;
        public long l;
        
        
        public Boolean B;
        public Character C;
        public Byte Byt;
        public Short S;
        public Integer I;
        public Float F;
        public Double D;
        public Long L;
     
        public ArrayList<String> stringArray;
        public List<TestA> testAArray;
        
        public HashMap<String, String> stringMap;
        public Map<String, TestA> testAMap;
        
        public TestA testA;

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((B == null) ? 0 : B.hashCode());
            result = prime * result + ((Byt == null) ? 0 : Byt.hashCode());
            result = prime * result + ((C == null) ? 0 : C.hashCode());
            result = prime * result + ((D == null) ? 0 : D.hashCode());
            result = prime * result + ((F == null) ? 0 : F.hashCode());
            result = prime * result + ((I == null) ? 0 : I.hashCode());
            result = prime * result + ((L == null) ? 0 : L.hashCode());
            result = prime * result + ((S == null) ? 0 : S.hashCode());
            result = prime * result + (b ? 1231 : 1237);
            result = prime * result + byt;
            result = prime * result + c;
            long temp;
            temp = Double.doubleToLongBits(d);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            result = prime * result + Float.floatToIntBits(f);
            result = prime * result + i;
            result = prime * result + (int) (l ^ (l >>> 32));
            result = prime * result + s;
            result = prime * result + ((stringArray == null) ? 0 : stringArray.hashCode());
            result = prime * result + ((stringMap == null) ? 0 : stringMap.hashCode());
            result = prime * result + ((testA == null) ? 0 : testA.hashCode());
            result = prime * result + ((testAArray == null) ? 0 : testAArray.hashCode());
            result = prime * result + ((testAMap == null) ? 0 : testAMap.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            TestA other = (TestA) obj;
            if (B == null) {
                if (other.B != null)
                    return false;
            } else if (!B.equals(other.B))
                return false;
            if (Byt == null) {
                if (other.Byt != null)
                    return false;
            } else if (!Byt.equals(other.Byt))
                return false;
            if (C == null) {
                if (other.C != null)
                    return false;
            } else if (!C.equals(other.C))
                return false;
            if (D == null) {
                if (other.D != null)
                    return false;
            } else if (!D.equals(other.D))
                return false;
            if (F == null) {
                if (other.F != null)
                    return false;
            } else if (!F.equals(other.F))
                return false;
            if (I == null) {
                if (other.I != null)
                    return false;
            } else if (!I.equals(other.I))
                return false;
            if (L == null) {
                if (other.L != null)
                    return false;
            } else if (!L.equals(other.L))
                return false;
            if (S == null) {
                if (other.S != null)
                    return false;
            } else if (!S.equals(other.S))
                return false;
            if (b != other.b)
                return false;
            if (byt != other.byt)
                return false;
            if (c != other.c)
                return false;
            if (Double.doubleToLongBits(d) != Double.doubleToLongBits(other.d))
                return false;
            if (Float.floatToIntBits(f) != Float.floatToIntBits(other.f))
                return false;
            if (i != other.i)
                return false;
            if (l != other.l)
                return false;
            if (s != other.s)
                return false;
            if (stringArray == null) {
                if (other.stringArray != null)
                    return false;
            } else if (!stringArray.equals(other.stringArray))
                return false;
            if (stringMap == null) {
                if (other.stringMap != null)
                    return false;
            } else if (!stringMap.equals(other.stringMap))
                return false;
            if (testA == null) {
                if (other.testA != null)
                    return false;
            } else if (!testA.equals(other.testA))
                return false;
            if (testAArray == null) {
                if (other.testAArray != null)
                    return false;
            } else if (!testAArray.equals(other.testAArray))
                return false;
            if (testAMap == null) {
                if (other.testAMap != null)
                    return false;
            } else if (!testAMap.equals(other.testAMap))
                return false;
            return true;
        }
        
        
    }
    
    @Test
    public void test() {
        LeoMap types = new LeoMap();
        types.putByString("b", LeoObject.valueOf(true));
        types.putByString("c", LeoObject.valueOf('c'));
        types.putByString("byt", LeoObject.valueOf( (byte) 42));
        types.putByString("s", LeoObject.valueOf( (short) 34532 ));
        types.putByString("i", LeoObject.valueOf( 2345624));
        types.putByString("f", LeoObject.valueOf( 1.5f ));
        types.putByString("d", LeoObject.valueOf( 5.2d));
        types.putByString("l", LeoObject.valueOf( 1231534234242342L ));
        
        types.putByString("B", LeoObject.valueOf(true));
        types.putByString("C", LeoObject.valueOf('c'));
        types.putByString("Byt", LeoObject.valueOf( (byte) 42));
        types.putByString("S", LeoObject.valueOf( (short) 34532 ));
        types.putByString("I", LeoObject.valueOf( 2345624));
        types.putByString("F", LeoObject.valueOf( 1.5f ));
        types.putByString("D", LeoObject.valueOf( 5.2d));
        types.putByString("L", LeoObject.valueOf( 1231534234242342L ));
        
        LeoArray stringArray = new LeoArray();
        stringArray.add(LeoObject.valueOf("a"));
        stringArray.add(LeoObject.valueOf("b"));
        types.putByString("stringArray", stringArray);
        
        TestA aa = new TestA();
        aa.b = true;
        
        LeoArray testAArray = new LeoArray();
        testAArray.add(LeoObject.valueOf(aa));
        testAArray.add(types.clone());
        types.putByString("testAArray", testAArray);
        
        TestA a = LeoObject.fromLeoObject(types, TestA.class);
        assertEquals(types.getBoolean("b"), a.b);
        assertEquals(types.getString("c").charAt(0), a.c);
        assertEquals(types.getInt("byt"), a.byt);
        assertEquals(types.getInt("s"), a.s);
        assertEquals(types.getInt("i"), a.i);        
        assertEquals( (float)types.getDouble("f"), a.f, 0.001f);
        assertEquals(types.getDouble("d"), a.d, 0.001f);
        assertEquals(types.getLong("l"), a.l);
        
        
        assertEquals(types.getBoolean("B"), a.B);
        assertEquals((Character)types.getString("C").charAt(0), a.C);
        assertEquals((byte)types.getInt("Byt"), (byte)a.Byt);
        assertEquals((short)types.getInt("S"), (short)a.S);
        assertEquals((Integer)types.getInt("I"), a.I);        
        assertEquals( (float)types.getDouble("F"), a.F, 0.001f);
        assertEquals(types.getDouble("D"), a.D, 0.001f);
        assertEquals((Long)types.getLong("L"), a.L);
        
        
        assertNotNull(a.stringArray);
        assertEquals(2, a.stringArray.size());
        int i = 0;
        for(String s : a.stringArray) {
            assertEquals(stringArray.get(i++).toString(), s);
        }
        
        
        assertNotNull(a.testAArray);
        assertEquals(2, a.testAArray.size());
        assertEquals(testAArray.get(0), aa);
        //assertEquals(testAArray.get(1), );
    }

    public static enum EnumType {
        A,B
    }
    
    public static class TestEnum {
        public EnumType e;
    }
    
    @Test
    public void testEnum() {
        LeoMap map = new LeoMap();
        map.putByString("e", LeoObject.valueOf("A"));
        
        TestEnum e = LeoObject.fromLeoObject(map, TestEnum.class);
        assertEquals(EnumType.A, e.e);
    }
}
