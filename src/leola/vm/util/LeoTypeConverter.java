/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.vm.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import leola.vm.Scope;
import leola.vm.exceptions.LeolaRuntimeException;
import leola.vm.types.LeoArray;
import leola.vm.types.LeoBoolean;
import leola.vm.types.LeoDouble;
import leola.vm.types.LeoInteger;
import leola.vm.types.LeoLong;
import leola.vm.types.LeoMap;
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

    public static interface Converter {
        /**
         * Coerces the supplied Java Object into the equivalent
         * {@link LeoObject}
         * 
         * @param type
         * @param javaObj
         * @return the LeoObject
         */
        LeoObject convert(Class<?> type, Object javaObj);
        
        
        /**
         * Coerces the supplied {@link LeoObject} into the equivalent
         * Java Object
         * 
         * @param field
         * @param type
         * @param leoObj
         * @return a new Java instance
         */
        Object    fromLeoObject(Field field, Class<?> type, LeoObject leoObj);
    }
    
    
    private static final Map<Class<?>, Converter> CONVERTERS = new HashMap<Class<?>, LeoTypeConverter.Converter>();
    static {
        CONVERTERS.put(null, new Converter() {           
            @Override
            public LeoObject convert(Class<?> type, Object javaObj) {
                return LeoNull.LEONULL;
            }
            
            @Override
            public Object fromLeoObject(Field field, Class<?> type, LeoObject leoObj) {             
                return null;
            }
        });
        
        
        Converter stringConverter = new Converter() {
            
            @Override
            public LeoObject convert(Class<?> type, Object javaObj) {
                return LeoString.valueOf(javaObj.toString());
            }
            
            @Override
            public Object fromLeoObject(Field field, Class<?> type, LeoObject leoObj) {             
                return leoObj.toString();
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
            
            @Override
            public Object fromLeoObject(Field field, Class<?> type, LeoObject leoObj) {             
                return LeoObject.isTrue(leoObj);
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
            
            @Override
            public Object fromLeoObject(Field field, Class<?> type, LeoObject leoObj) {             
                return leoObj.asLong();
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
            
            @Override
            public Object fromLeoObject(Field field, Class<?> type, LeoObject leoObj) {             
                return leoObj.asInt();
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
            
            @Override
            public Object fromLeoObject(Field field, Class<?> type, LeoObject leoObj) {             
                return leoObj.asDouble();
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
            
            @Override
            public Object fromLeoObject(Field field, Class<?> type, LeoObject leoObj) {             
                return leoObj;
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
        
        @Override
        public Object fromLeoObject(Field field, Class<?> type, LeoObject leoObj) {             
            return LeoObject.fromLeoObject(leoObj, type);
        }
    };             
    
    
    private final static Converter listConverter = new Converter() {
        @Override
        public LeoObject convert(Class<?> type, Object javaObj) {        
            return null;
        }
      
        @Override
        public Object fromLeoObject(Field field, Class<?> type, LeoObject leoObj) {
            if(!leoObj.isArray()) {
                throw new LeolaRuntimeException();
            }
            
            LeoArray array = leoObj.as();
            
            Type genericType = field.getGenericType();
            ParameterizedType paramType = (ParameterizedType) genericType;
            
            Class<?> listType = ClassUtil.getRawType(paramType.getActualTypeArguments()[0]);
            
            List<Object> result = new ArrayList<>(array.size());
            for(int i = 0; i < array.size(); i++) {
                LeoObject obj = array.get(i);
                result.add(LeoObject.fromLeoObject(obj, listType));
            }
            
            return result;
        }
    };
    
    private final static Converter mapConverter = new Converter() {
        @Override
        public LeoObject convert(Class<?> type, Object javaObj) {        
            return null;
        }
      
        @Override
        public Object fromLeoObject(Field field, Class<?> type, LeoObject leoObj) {                                    
            TypeVariable<?>[] types = type.getTypeParameters();
            Map<Object, Object> result = new HashMap<>();
            
            Class<?> keyType = ClassUtil.getRawType(types[0]);
            Class<?> valueType = ClassUtil.getRawType(types[1]);
            
            LeoMap map = null;
            
            if(leoObj.isScopedObject()) {
                Scope scope = leoObj.getScope();
                if(scope.hasObjects()) {
                    map = scope.getRawObjects();
                }
                else {
                    map = new LeoMap(1);
                }
            }
            else if(leoObj.isMap()) {
                map = leoObj.as();
            }
            else {
                throw new LeolaRuntimeException();
            }
            
            for(int i = 0; i < map.bucketLength(); i++) {
                LeoObject key = map.getKey(i);
                LeoObject value = map.getValue(i);
                
                if(key != null) {
                    result.put(LeoObject.fromLeoObject(key, keyType), 
                               LeoObject.fromLeoObject(value, valueType));
                }
            }
            
            return result;
        }
    };
    
    private static final Map<Class<?>, Converter> FROM_CONVERTERS = new HashMap<Class<?>, LeoTypeConverter.Converter>(CONVERTERS);
    static {
        Converter charConverter = new Converter() {
            
            @Override
            public LeoObject convert(Class<?> type, Object javaObj) {
                return LeoString.valueOf(javaObj.toString());
            }
            
            @Override
            public Object fromLeoObject(Field field, Class<?> type, LeoObject leoObj) {             
                return leoObj.toString().charAt(0);
            }
        };
        
        FROM_CONVERTERS.put(char.class, charConverter);
        FROM_CONVERTERS.put(Character.class, charConverter);
        
        Converter byteConverter = new Converter() {
            
            @Override
            public LeoObject convert(Class<?> type, Object javaObj) {
                return LeoInteger.valueOf(((Number)javaObj).intValue());
            }
            
            @Override
            public Object fromLeoObject(Field field, Class<?> type, LeoObject leoObj) {             
                return leoObj.asByte();
            }
        };
        FROM_CONVERTERS.put(byte.class, byteConverter);
        FROM_CONVERTERS.put(Byte.class, byteConverter);
        
        Converter shortConverter = new Converter() {
            
            @Override
            public LeoObject convert(Class<?> type, Object javaObj) {
                return LeoInteger.valueOf(((Number)javaObj).intValue());
            }
            
            @Override
            public Object fromLeoObject(Field field, Class<?> type, LeoObject leoObj) {             
                return leoObj.asShort();
            }
        };
        FROM_CONVERTERS.put(short.class, shortConverter);
        FROM_CONVERTERS.put(Short.class, shortConverter);
        
        
        Converter floatConverter = new Converter() {
            
            @Override
            public LeoObject convert(Class<?> type, Object javaObj) {
                return LeoDouble.valueOf(((Number)javaObj).doubleValue());
            }
            
            @Override
            public Object fromLeoObject(Field field, Class<?> type, LeoObject leoObj) {             
                return leoObj.asFloat();
            }
        };
        FROM_CONVERTERS.put(float.class, floatConverter);
        FROM_CONVERTERS.put(Float.class, floatConverter);
        
        FROM_CONVERTERS.put(List.class, listConverter);
        FROM_CONVERTERS.put(Map.class, mapConverter);
    }
    
    /**
     * Converts the supplied java object into a {@link LeoObject}.
     *
     * @param javaObj
     * @return
     * @throws LeolaRuntimeException
     */
    public static LeoObject convertToLeolaType(Object javaObj) {
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
    
    private static Converter getConverter(Class<?> type, Map<Class<?>, Converter> converters) {
        Converter converter = converters != null 
                ? converters.get(type) : null;
        
        if(converter == null) {
            converter = FROM_CONVERTERS.get(type);
        }
        
        if(List.class.isAssignableFrom(type)) {
            converter = listConverter;
        }
        else if(Map.class.isAssignableFrom(type)) {
            converter = mapConverter;
        }  
        
        return converter;
    }
    
    /**
     * Creates a new Java object based on the supplied class type and populates it with the supplied {@link LeoObject}
     * 
     * @param obj
     * @param converters
     * @param type
     * @return the java object
     */
    @SuppressWarnings("unchecked")
    public static <T> T fromLeoObject(LeoObject obj, Map<Class<?>, Converter> converters, Class<T> type) {
        try {
            if(obj.isNativeClass()) {
                return (T) obj.getValue(type);
            }
            
            Converter rootConverter = getConverter(type, converters);
            if(rootConverter != null) {
                return (T) rootConverter.fromLeoObject(null, type, obj);
            }
            
            T result = type.newInstance();
            List<Field> fields = ClassUtil.getBeanFields(type);
            
            for(Field field : fields) {
                String fieldName = field.getName();
                Class<?> fieldType = field.getType();
                
                if(obj.hasObject(fieldName)) {     
                    Converter converter = getConverter(fieldType, converters);                    
                    if(converter == null) {
                        converter = objectConverter;
                    }
                    
                    ClassUtil.setFieldValue(result, field, converter.fromLeoObject(field, fieldType, obj.getObject(fieldName)));                    
                }
            }
            
            return result;
        }
        catch (Exception e) {
            throw new LeolaRuntimeException(e);
        }
    }
}

