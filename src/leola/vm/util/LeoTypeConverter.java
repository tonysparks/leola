/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.vm.util;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

import leola.vm.exceptions.LeolaRuntimeException;
import leola.vm.types.LeoArray;
import leola.vm.types.LeoBoolean;
import leola.vm.types.LeoDouble;
import leola.vm.types.LeoInteger;
import leola.vm.types.LeoLong;
import leola.vm.types.LeoNativeClass;
import leola.vm.types.LeoNull;
import leola.vm.types.LeoObject;
import leola.vm.types.LeoString;
/**
 * Converts Java types to Leola types and vice versa.
 *
 * @author Tony
 *
 */
public class LeoTypeConverter {

    private static interface Converter {
        LeoObject convert(Class<?> type, Object javaObj);
    }
    private static final Map<Class<?>, Converter> CONVERTERS = new HashMap<Class<?>, LeoTypeConverter.Converter>();
    static {
        CONVERTERS.put(null, new Converter() {           
            @Override
            public LeoObject convert(Class<?> type, Object javaObj) {
                return LeoNull.LEONULL;
            } 
        });
        
        
        Converter stringConverter = new Converter() {
            
            @Override
            public LeoObject convert(Class<?> type, Object javaObj) {
                return LeoString.valueOf(javaObj.toString());
            }
        };
        CONVERTERS.put(String.class, stringConverter);
        CONVERTERS.put(char.class, stringConverter);
        CONVERTERS.put(Character.class, stringConverter);
        
        
        // Boolean
        Converter booleanConverter = new Converter() {
            
            @Override
            public LeoObject convert(Class<?> type, Object javaObj) {
                return ((Boolean)javaObj) ? LeoBoolean.LEOTRUE
                                          : LeoBoolean.LEOFALSE;
            }
        };
        CONVERTERS.put(boolean.class, booleanConverter);
        CONVERTERS.put(Boolean.class, booleanConverter);

        // Long
        Converter longConverter = new Converter() {
            
            @Override
            public LeoObject convert(Class<?> type, Object javaObj) {
                return LeoLong.valueOf(((Number)javaObj).longValue());
            }
        };
        CONVERTERS.put(long.class, longConverter);
        CONVERTERS.put(Long.class, longConverter);
        
        // integer
        Converter integerConverter = new Converter() {
            
            @Override
            public LeoObject convert(Class<?> type, Object javaObj) {
                return LeoInteger.valueOf(((Number)javaObj).intValue());
            }
        };
        
        CONVERTERS.put(byte.class, integerConverter);
        CONVERTERS.put(Byte.class, integerConverter);
        CONVERTERS.put(short.class, integerConverter);
        CONVERTERS.put(Short.class, integerConverter);
        CONVERTERS.put(int.class, integerConverter);
        CONVERTERS.put(Integer.class, integerConverter);        
        
        

        // Double
        Converter doubleConverter = new Converter() {
            
            @Override
            public LeoObject convert(Class<?> type, Object javaObj) {
                return LeoDouble.valueOf(((Number)javaObj).doubleValue());
            }
        };
        
        CONVERTERS.put(double.class, doubleConverter);
        CONVERTERS.put(Double.class, doubleConverter);
        CONVERTERS.put(float.class, doubleConverter);
        CONVERTERS.put(Float.class, doubleConverter);
        CONVERTERS.put(Number.class, doubleConverter);
        
        
        // LeoObject converter
        CONVERTERS.put(LeoObject.class, new Converter() {
            
            @Override
            public LeoObject convert(Class<?> type, Object javaObj) {
                return (LeoObject) javaObj;
            }
        });
    }   
    
    // Generic catch-all converter
    private final static Converter objectConverter = new Converter() {
        
        @Override
        public LeoObject convert(Class<?> type, Object javaObj) {
            LeoObject result = null;
            if ( ClassUtil.inheritsFrom(type, LeoObject.class)) {
                result = (LeoObject)javaObj;
            }
            else if ( type.isArray() ) {
                int len = Array.getLength(javaObj);
                LeoArray array = new LeoArray(len);
                for(int i = 0; i < len; i++) {
                    Object obj = Array.get(javaObj, i);
                    array.add(LeoObject.valueOf(obj));
                }

                result = array;
            }
            else {
                result = new LeoNativeClass(type, javaObj);
            }
            
            return (result);
        }
    };
        
     
    
    /**
     * Converts the supplied java object into a {@link LeoObject}.
     *
     * @param javaObj
     * @return
     * @throws LeolaRuntimeException
     */
    public static LeoObject convertToLeolaType(Object javaObj) /*throws EvalException*/ {
        LeoObject result = null;
        if(javaObj == null) {
            result = LeoNull.LEONULL;
        }
        else {
            Class<?> type = javaObj.getClass();
            Converter converter = CONVERTERS.get(type);
            result = (converter!=null) ? converter.convert(type, javaObj) 
                                       : objectConverter.convert(type, javaObj);
        }

        return result;
    }
    
    /**
     * Convert to the specified type.
     *
     * @param v
     * @param type
     * @param obj
     * @throws Exception
     */
    public static Object convertLeoObjectToJavaObj(Class<?> type
                                               , LeoObject obj) throws LeolaRuntimeException {

        Object jObj = null;
        if(obj!=null) {
            jObj = obj.getValue(type);
        }
        return jObj;
    }
}

