/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.types;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import leola.vm.Scope;
import leola.vm.compiler.Outer;
import leola.vm.exceptions.LeolaRuntimeException;
import leola.vm.lib.LeolaMethod;
import leola.vm.util.ClassUtil;
import leola.vm.util.LeoTypeConverter;


/**
 * Base object for Leola types.
 *
 * @author Tony
 *
 */
public abstract class LeoObject implements Comparable<LeoObject> {

	public static final LeoString REQ = LeoString.valueOf("$req");
	public static final LeoString EQ = LeoString.valueOf("$eq");
	public static final LeoString NEQ = LeoString.valueOf("$neq");
	public static final LeoString LT = LeoString.valueOf("$lt");
	public static final LeoString LTE = LeoString.valueOf("$lte");
	public static final LeoString GT = LeoString.valueOf("$gt");
	public static final LeoString GTE = LeoString.valueOf("$gte");
	
	public static final LeoString ADD = LeoString.valueOf("$add");
	public static final LeoString SUB = LeoString.valueOf("$sub");
	public static final LeoString MUL = LeoString.valueOf("$mul");
	public static final LeoString DIV = LeoString.valueOf("$div");
	public static final LeoString MOD = LeoString.valueOf("$mod");
	
	public static final LeoString NEG = LeoString.valueOf("$neg");
	
	public static final LeoString BNOT = LeoString.valueOf("$bnot");
	public static final LeoString BAND = LeoString.valueOf("$band");
	public static final LeoString BOR = LeoString.valueOf("$bor");
	public static final LeoString BSL = LeoString.valueOf("$bsl");
	public static final LeoString BSR = LeoString.valueOf("$bsr");
	public static final LeoString XOR = LeoString.valueOf("$xor");
	public static final LeoString INDEX = LeoString.valueOf("$index");
	public static final LeoString SINDEX = LeoString.valueOf("$sindex");
		
	public static final LeoString toString = LeoString.valueOf("toString");
	
	/**
	 * Converts the supplied Java object into the appropriate {@link LeoObject} type.
	 * 
	 * @param v the Java object
	 * @return the converted Java object to the respective {@link LeoObject} type.
	 */
	public static final LeoObject valueOf(Object v) {
	    return LeoTypeConverter.convertToLeolaType(v);
	}
	
	/**
	 * Attempts to convert the supplied {@link LeoObject} into the equivalent Java Object using
	 *  the supplied Class as a hint.
	 *  
	 * @param jtype the hint at which type the Java Object should be
	 * @param v the {@link LeoObject} to convert
	 * @return the Java Object version of the supplied {@link LeoObject}
	 */
	public static final Object toJavaObject(Class<?> jtype, LeoObject v) {
	    return LeoTypeConverter.convertLeoObjectToJavaObj(jtype, v);
	}
	
	/*
	 * Type
	 */
	public enum LeoType {
		  NULL
		  , BOOLEAN
		  , INTEGER
		  , LONG
		  , REAL
//		  , NUMBER
		  , STRING
		  , GENERATOR
		  , FUNCTION
		  , NATIVE_FUNCTION
		  , USER_FUNCTION
		  , ARRAY
		  , MAP
		  , CLASS
		  , NAMESPACE
		  , NATIVE_CLASS	
		  , ERROR
		;
		  
		/* Java instantiates a new array for each values() call */  
		private static final LeoType[] values = values();
		  
		public static LeoType fromOrdinal(int ordinal) {
			return values[ordinal];
		}
	}

	/**
	 * The underlying type
	 */
	private final LeoType type;

	/**
	 * @param type
	 */
	public LeoObject(LeoType type) {
		this.type = type;
	}

	public LeoString toLeoString() {
		return LeoString.valueOf(toString());
	}
	
	/**
	 * @return the type
	 */
	public LeoType getType() {
		return type;
	}
	
	/**
	 * @return the scope
	 */
	public Scope getScope() {
		return null;
	}
	
	/**
	 * @return the outers
	 */
	public Outer[] getOuters() {
		return null;
	}
	
	/**
	 * @return the locals
	 */
	public LeoObject[] getLocals() {
		return null;
	}

	public boolean isNull() {
	    return false;
	}
	
	public boolean isNumber() {
		return false;
	}
	
	public boolean isString() {
		return false;
	}
	
	public boolean isMap() {
		return false;
	}
	
	public boolean isArray() {
		return false;
	}
	
	public boolean isBoolean() {
		return false;
	}
	
	public boolean isClass() {
		return false;
	}
	public boolean isNativeClass() {
	    return false;
	}
	
	public boolean isGenerator() {
		return false;
	}
	public boolean isFunction() {
		return false;
	}
	public boolean isNativeFunction() {
		return false;
	}
	public boolean isOuter() {
		return false;
	}
	public boolean isError() {
		return false;
	}
	
	public boolean isNamespace() {
		return false;
	}
	
	/**
	 * @return if this object has it's own scope.
	 */
	public boolean isScopedObject() {
		return false;
	}

	/**
	 * Determines if its one of these types
	 * @param leoTypes
	 * @return
	 */
	public boolean isOfType(LeoType leoType) {
		return (this.type==leoType);
	}

	/**
	 * Determines if its one of these types
	 * @param leoTypes
	 * @return
	 */
	public boolean isOfType(LeoType ... leoTypes) {
		for(LeoType t: leoTypes) {
			if ( this.type==t ) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Determines if its one of these types
	 * @param leoTypes
	 * @return
	 */
    public boolean isOfType(Class<?> ... leoTypes) {
		for(Class<?> c: leoTypes) {
			if ( this.getClass().equals(c)) {
				return true;
			}
		}

		return false;
	}

	
	/**
	 * Determines if this type is of the supplied type.
	 * 
	 * @param rawType
	 * @return
	 */
	public boolean isOfType(String rawType) {
		boolean isa = false;
		try {
			LeoType type = LeoType.valueOf(rawType.toUpperCase());
			isa = isOfType(type);
		}
		catch(Exception e) {
			isa = false;
		}
		
		return isa;
	}
	
	/**
	 * Up Casts the supplied {@link LeoObject}
	 * @param <T>
	 * @param obj
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends LeoObject> T as(LeoObject obj) {
		return (T)obj;
	}

	/**
	 * Up Casts this {@link LeoObject}
	 *
	 * @param <T>
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends LeoObject> T as() {
		return (T)this;
	}

	/**
	 * @return this object represented as an integer
	 */
	public int asInt() {
		throw new LeolaRuntimeException("Not a valid integer");
	}
	
	/**
	 * @return this object represented as a Long
	 */
	public long asLong() {
	    throw new LeolaRuntimeException("Not a valid long");
	}
	
	/**
	 * @return this object represented as a Double
	 */
	public double asDouble() {
	    throw new LeolaRuntimeException("Not a valid double");
	}
	
	/**
	 * @return this object represented as a Float
	 */
	public float asFloat() {
	    return (float)asDouble();
    }

	/**
     * @return this object represented as a Byte
     */
    public byte asByte() {
        return (byte)asInt();
    }

    /**
     * @return this object represented as a Short
     */
    public short asShort() {
        return (short)asInt();
    }

    /**
     * @return this object represented as a Character
     */
    public char asChar() {
        return (char)asInt();
    }
	
	/**
	 * Determines if the two objects are equal.
	 *
	 * @param left
	 * @param right
	 * @return
	 */
	public static boolean $eq(LeoObject left, LeoObject right) {
		if ( left == null && right == null ) {
			return true;
		}

		if ( left == right ) {
			return true;
		}

		if ( left != null && right != null ) {
			return left.$eq(right);
		}

		return false;
	}

	/**
	 * Determines if the supplied object is of type TRUE.
	 *
	 * @param obj
	 * @return
	 */
	public static boolean isTrue(LeoObject obj) {
		boolean isTrue = (obj != null) && obj.isTrue();
		return isTrue;
	}

	/**
	 * Determines if the supplied object is of type TRUE.
	 *
	 * @param obj
	 * @return
	 */
	public boolean isTrue() {
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("[ %s @ %s ]", this.type, super.toString());
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		LeoObject other = (LeoObject) obj;
		if (type != other.type) 
			return false;
		
		return this.$eq(other);
	}

	/**
	 * Tests if the references are the same.  Java equivalent to '=='
	 * @param other
	 * @return true if they are the same reference
	 */
	public boolean $req(LeoObject other) {
		return this == other;
	}
	
	/**
	 * Determines if it equals another object.
	 *
	 * @param other
	 * @return true if it equals
	 */
	public abstract boolean $eq(LeoObject other);
	public boolean $eq(int other)  { return false; }
	public boolean $eq(double other)  { return false; }
	public boolean $eq(long other)  { return false; }
	/**
	 * If it's not equal another object.
	 *
	 * @param other
	 * @return
	 */
	public boolean $neq(LeoObject other) {
		return ! $eq(other);
	}
	
	public boolean $neq(int other) {
		return ! $eq(other);
	}
	
	public boolean $neq(double other) {
		return ! $eq(other);
	}
	
	public boolean $neq(long other) {
		return ! $eq(other);
	}

	/**
	 * If it's less than to another object
	 * @param other
	 * @return
	 */
	public abstract boolean $lt(LeoObject other);
	public boolean $lt(int other) { return false; }
	public boolean $lt(double other)  { return false; }
	public boolean $lt(long other)  { return false; }

	/**
	 * If it's less than or equal to another object.
	 * @param other
	 * @return
	 */
	public boolean $lte(LeoObject other) {
		return $eq(other) || $lt(other);
	}
	public boolean $lte(int other) {
		return $eq(other) || $lt(other);
	}
	public boolean $lte(double other) {
		return $eq(other) || $lt(other);
	}
	public boolean $lte(long other) {
		return $eq(other) || $lt(other);
	}
	

	/**
	 * If it's greater than to another object
	 * @param other
	 * @return
	 */
	public abstract boolean $gt(LeoObject other);
	public boolean $gt(int other) { return false; }
	public boolean $gt(double other) { return false; }
	public boolean $gt(long other) { return false; }

	/**
	 * If it's greater than or equal to another object.
	 * @param other
	 * @return
	 */
	public boolean $gte(LeoObject other) {
		return $eq(other) || $gt(other);
	}
	public boolean $gte(int other) {
		return $eq(other) || $gt(other);
	}
	public boolean $gte(double other) {
		return $eq(other) || $gt(other);
	}
	public boolean $gte(long other) {
		return $eq(other) || $gt(other);
	}
	

	public LeoObject $add(LeoObject other) { return null; }
	public LeoObject $sub(LeoObject other) { return null; }
	public LeoObject $mul(LeoObject other) { return null; }
	public LeoObject $div(LeoObject other) { return null; }
	public LeoObject $mod(LeoObject other) { return null; }
	
	public LeoObject $add(int other) { return null; }
	public LeoObject $sub(int other) { return null; }
	public LeoObject $mul(int other) { return null; }
	public LeoObject $div(int other) { return null; }
	public LeoObject $mod(int other) { return null; }
	
	public LeoObject $add(double other) { return null; }
	public LeoObject $sub(double other) { return null; }
	public LeoObject $mul(double other) { return null; }
	public LeoObject $div(double other) { return null; }
	public LeoObject $mod(double other) { return null; }
	
	
	public LeoObject $add(long other) { return null; }
	public LeoObject $sub(long other) { return null; }
	public LeoObject $mul(long other) { return null; }
	public LeoObject $div(long other) { return null; }
	public LeoObject $mod(long other) { return null; }
	
	public LeoObject $neg() { return null; }
//	public LeoObject not() { return null; }
	public LeoObject $bnot() { return null; }
	
	public LeoObject $index(LeoObject other) { return null; }
	public void      $sindex(LeoObject key, LeoObject other) {}
	public LeoObject $bsl(LeoObject other) { return null; }
	public LeoObject $bsr(LeoObject other) { return null; }
	public LeoObject $xor(LeoObject other) { return null; }
	public LeoObject $bor(LeoObject other) { return null; }
	public LeoObject $band(LeoObject other) { return null; }
	
	
	public LeoObject $index(int other) { return null; }
	public LeoObject $bsl(int other) { return null; }
	public LeoObject $bsr(int other) { return null; }
	public LeoObject $xor(int other) { return null; }
	public LeoObject $bor(int other) { return null; }
	public LeoObject $band(int other) { return null; }
	
	public LeoObject $index(double other) { return null; }
	public LeoObject $bsl(double other) { return null; }
	public LeoObject $bsr(double other) { return null; }
	public LeoObject $xor(double other) { return null; }
	public LeoObject $bor(double other) { return null; }
	public LeoObject $band(double other) { return null; }

	
	public LeoObject $index(long other) { return null; }
	public LeoObject $bsl(long other) { return null; }
	public LeoObject $bsr(long other) { return null; }
	public LeoObject $xor(long other) { return null; }
	public LeoObject $bor(long other) { return null; }
	public LeoObject $band(long other) { return null; }

	/**
	 * Sets a property on this object.
	 * 
	 * @param key
	 * @param value
	 */
	public void setObject(LeoObject key, LeoObject value) {
		throw new LeolaRuntimeException(this + " is not a complex object");
	}
	
	/**
	 * Sets a property on this object.
	 * 
	 * @param key -- converts to a LeoString
	 * @param value
	 */
	public void setObject(String key, LeoObject value) {
		setObject(LeoString.valueOf(key), value);
	}
	
	/**
	 * Retrieves a property from this object.
	 * 
	 * @param key
	 * @return
	 */
	public LeoObject getObject(LeoObject key) {
		throw new LeolaRuntimeException(this + " is not a complex object");
	}
	
	/**
	 * Retrieves a property from this object.
	 * 
	 * @param key - converts to a LeoString
	 * @return
	 */
	public LeoObject getObject(String key) {
		return getObject(LeoString.valueOf(key));
	}
	

    /**
     * Determines if the supplied key has an associated value 
     * 
     * @param key
     * @return true if the supplied key has an associated value, otherwise false
     */
	public boolean hasObject(String key) {
	    return hasObject(LeoString.valueOf(key));
	}
	
	/**
	 * Determines if the supplied key has an associated value 
	 * 
	 * @param key
	 * @return true if the supplied key has an associated value, otherwise false
	 */
	public boolean hasObject(LeoObject key) {
	    throw new LeolaRuntimeException(this + " is not a complex object");
	}
	
	/**
	 * Invokes the function
	 * 
	 * @return the result of the function call
	 */
	public LeoObject call() {
		throw new LeolaRuntimeException(this + " is not a function.");
	}
	
	/**
	 * Invokes the function
	 * 
	 * @param arg1
	 * @return the result of the function call
	 */
	public LeoObject call(LeoObject arg1) {
		throw new LeolaRuntimeException(this + " is not a function.");		
	}
	
	/**
	 * Invokes the function
	 * 
	 * @param arg1
	 * @param arg2
	 * @return the result of the function call
	 */
	public LeoObject call(LeoObject arg1, LeoObject arg2) {
		throw new LeolaRuntimeException(this + " is not a function.");
	}
	
	/**
	 * Invokes the function
	 * 
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @return the result of the function call
	 */
	public LeoObject call(LeoObject arg1, LeoObject arg2, LeoObject arg3) {
		throw new LeolaRuntimeException(this + " is not a function.");
	}
	
	/**
	 * Invokes the function
	 * 
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 * @return the result of the function call
	 */
	public LeoObject call(LeoObject arg1, LeoObject arg2, LeoObject arg3, LeoObject arg4) {
		throw new LeolaRuntimeException(this + " is not a function.");
	}
	
	/**
	 * Invokes the function
	 * 
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 * @param arg5
	 * @return the result of the function call
	 */
	public LeoObject call(LeoObject arg1, LeoObject arg2, LeoObject arg3, LeoObject arg4, LeoObject arg5) {
		throw new LeolaRuntimeException(this + " is not a function.");
	}
	
	/**
	 * Invokes the function
	 * 
	 * @param args
	 * @return the result of the function call
	 */
	public LeoObject call(LeoObject[] args) {
		throw new LeolaRuntimeException(this + " is not a function.");
	}

	
	/**
	 * @return this types value
	 */
	public abstract Object getValue();
	
	/**
	 * Attempts to narrow the {@link LeoObject} to a more specific Java Type
	 * @param narrowType
	 * @return the Java Object
	 */
	public Object getValue(Class<?> narrowType) {
		return this;
	}
	
	/**
	 * @return a deep copy clone of this object
	 */
	@Override
	public abstract LeoObject clone();
	
	
	@Override
	public int compareTo(LeoObject other) {
	    if(this.$lt(other)) {
	        return -1;
	    }
	    
	    if(this.$gt(other)) {
	        return 1;
	    }
	    
	    return 0;
	}
	
	/**
	 * Writes this object out
	 * 
	 * @param out
	 * @throws IOException
	 */
	public abstract void write(DataOutput out) throws IOException;
	
	/**
	 * Reads from the {@link DataInput} stream, constructing a {@link LeoObject}
	 * 
	 * @param in
	 * @return the reconstituted {@link LeoObject}
	 * @throws IOException
	 */
	public static LeoObject read(LeoObject env, DataInput in) throws IOException {
		int type = in.readByte();
		LeoType leoType = LeoType.fromOrdinal(type);
		LeoObject result = null;
		
		switch(leoType) {
			case ARRAY: {
				result = LeoArray.read(env, in);
				break;
			}
			case BOOLEAN: {
				result = LeoBoolean.read(env, in);
				break;
			}			
			case ERROR: 
			case CLASS: {
			//	result = LeoClass.read(env, symbols, in); TODO
				break;
			}
			case GENERATOR: {
				result = LeoGenerator.read(env, in);
				break;
			}
			case FUNCTION: {
				result = LeoFunction.read(env, in);
				break;
			}
			case MAP: {
				result = LeoMap.read(env, in);
				break;
			}
			case NAMESPACE: {
				break;
			}
			case NATIVE_CLASS: {
				break;
			}
			case USER_FUNCTION:
			case NATIVE_FUNCTION: {
				break;				
			}
			case NULL: {
				result = LeoNull.read(in);
				break;
			}
			case INTEGER: {
				result = LeoInteger.read(in);
				break;
			}
			case LONG: {
				result = LeoLong.read(in);
				break;
			}
			case REAL: {
				result = LeoDouble.read(in);
				break;
			}
			case STRING: {
				result = LeoString.read(in);
				break;
			}
		}
		
		return result;
	}
	
	
	/**
	 * Determines if the supplied owner has a method by the supplied method name.
	 * 
	 * @param owner
	 * @param methodName
	 * @return true if the supplied object (owner) has a method by the supplied name
	 */
	protected static boolean hasNativeMethod(Object owner, LeoObject methodName) {
	    List<Method> methods = ClassUtil.getMethodsByName(owner.getClass(), methodName.toString());
        removeInterfaceMethods(methods);
        return !methods.isEmpty();
	}
	
	/**
	 * Retrieve the native methods from the owner public method listings.  This will query the owner class
	 * using reflection if the supplied nativeApi {@link Map} is empty.
	 * 
	 * @param owner the instance in which owns the native methods
	 * @param nativeApi the cache of native methods
	 * @param key the method name
	 * @return the {@link LeoObject} of the native method, or null if not found
	 */
    protected static LeoObject getNativeMethod(Object owner, Map<LeoObject, LeoObject> nativeApi, LeoObject key) {
        LeoObject func = nativeApi.get(key);
        if (!LeoObject.isTrue(func)) {
            List<Method> methods = ClassUtil.getMethodsByName(owner.getClass(), key.toString());
            removeInterfaceMethods(methods);

            if (methods.isEmpty()) {
                methods = ClassUtil.getMethodsByAnnotationAlias(owner.getClass(), key.toString());
                if(methods.isEmpty()) {
                    throw new LeolaRuntimeException("No method exists by the name: " + key);
                }
            }

            func = new LeoNativeFunction(methods, owner);
            nativeApi.put(key, func);
        }
        
        return func;
    }
	    
    
    /**
     * HACK - removes weird interface methods from the API listing.  
     * @param methods
     */
    protected static void removeInterfaceMethods(List<Method> methods) {
        if(methods.size() > 1) {
            for (int i = 0; i < methods.size(); i++) {
                Method method = methods.get(i);
                boolean isOverride = method.isAnnotationPresent(LeolaMethod.class) ||
                                     method.isAnnotationPresent(Override.class);
                
                // if this method has the override or LeolaMethod
                // annotations, this means it is our method that we want to
                // use
                if(isOverride) {
                    continue;
                }
                
                for (int j = 0; j < method.getParameterTypes().length; j++) {
                    Class<?> pType = method.getParameterTypes()[j];
                    if (pType.isPrimitive()) {
                        continue;
                    }
    
                    if (pType.equals(Object.class)) {
                        methods.remove(i);
                        i -= 1;
                        break;
                    }
                }
    
            }
        }
    }
}

