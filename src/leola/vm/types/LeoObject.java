/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.types;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import leola.vm.Leola;
import leola.vm.Scope;
import leola.vm.compiler.Outer;
import leola.vm.exceptions.LeolaRuntimeException;
import leola.vm.lib.LeolaMethod;
import leola.vm.util.ClassUtil;
import leola.vm.util.LeoTypeConverter;


/**
 * Base object for {@link Leola} types.  The basic design principal for the {@link LeoObject} is to treat all
 * types the same, that is avoid the need to cast as much as possible and error if the underlying type does not
 * support an operation.
 * 
 * <p>
 * This does have the negative consequence of having the {@link LeoObject} blow up in size and responsibility.  However, I 
 * feel the trade-off is warranted.
 * 
 * <p>
 * There are a number of operators that can be overridden (both in Java and Leola code).  The operators include:
 * <table border="1">
 *  <tr>
 *      <th>Leola Operator</th>
 *      <th>Java/Leola method name</th>
 *      <th>Description</th>
 *  </tr>
 *  <tr>
 *      <td>===</td>
 *      <td>$req</td>
 *      <td>Reference equals operator, the default functionality is to check the equality of the references (identical to Java '==' operator)</td>      
 *  </tr>
 *  <tr>
 *      <td>==</td>
 *      <td>$eq</td>
 *      <td>Object equals operator, the default functionality is to check the equality of the objects (identical to Java <code>equals</code> method)</td>      
 *  </tr>
 *  <tr>
 *      <td>!=</td>
 *      <td>$neq</td>
 *      <td>Object not equals operator, the default functionality is to check the negated equality of the objects (identical to Java <code>!equals</code> method)</td>      
 *  </tr>    
 *  <tr>
 *      <td><</td>
 *      <td>$lt</td>
 *      <td>Object less than operator, the default functionality is to check if the left hand object is less than the right hand object (identical to Java <code><</code> operator)</td>      
 *  </tr>
 *  <tr>
 *      <td><=</td>
 *      <td>$lte</td>
 *      <td>Object less than or equals operator, the default functionality is to check if the left hand object is less than or equal to the right hand object (identical to Java <code>equals</code> method and/or the <code><</code> operator)</td>      
 *  </tr>
 *  <tr>
 *      <td>></td>
 *      <td>$gt</td>
 *      <td>Object greater than operator, the default functionality is to check if the left hand object is greater than the right hand object (identical to Java <code>></code> operator)</td>      
 *  </tr>
 *  <tr>
 *      <td>>=</td>
 *      <td>$gte</td>
 *      <td>Object greater than or equals operator, the default functionality is to check if the left hand object is greater than or equal to the right hand object (identical to Java <code>equals</code> method and/or the <code>></code> operator)</td>      
 *  </tr>
 *  <tr>
 *      <td>+</td>
 *      <td>$add</td>
 *      <td>Object addition operator, the default functionality is to add the right hand object to the left hand object (identical to Java <code>+</code> operator)</td>      
 *  </tr>
 *  <tr>
 *      <td>-</td>
 *      <td>$sub</td>
 *      <td>Object subtraction operator, the default functionality is to subtract the right hand object from the left hand object (identical to Java <code>-</code> operator)</td>      
 *  </tr>
 *  <tr>
 *      <td>*</td>
 *      <td>$mul</td>
 *      <td>Object multiplication operator, the default functionality is to multiply the right hand object by the left hand object (identical to Java <code>*</code> operator)</td>      
 *  </tr>
 *  <tr>
 *      <td>/</td>
 *      <td>$div</td>
 *      <td>Object division operator, the default functionality is to divide the left hand object by the right hand object (identical to Java <code>/</code> operator)</td>      
 *  </tr>
 *  <tr>
 *      <td>%</td>
 *      <td>$mod</td>
 *      <td>Object remainder (or modules) operator, the default functionality is to take the remainder of dividing the left hand object by the right hand object (identical to Java <code>%</code> operator)</td>      
 *  </tr>
 *  <tr>
 *      <td>!</td>
 *      <td>$neg</td>
 *      <td>Object negate operator, the default functionality is to take the negative of the object (identical to Java <code>!</code> operator)</td>      
 *  </tr>
 *  <tr>
 *      <td>~</td>
 *      <td>$bnot</td>
 *      <td>Object binary NOT operator, the default functionality is to negate/flip the object (identical to Java <code>~</code> operator)</td>      
 *  </tr>
 *  <tr>
 *      <td>&</td>
 *      <td>$band</td>
 *      <td>Object binary AND operator, the default functionality is to binary AND together the right hand object and the left hand object (identical to Java <code>&</code> operator)</td>      
 *  </tr>
 *  <tr>
 *      <td>|</td>
 *      <td>$bor</td>
 *      <td>Object binary OR operator, the default functionality is to binary OR together the right hand object and the left hand object (identical to Java <code>|</code> operator)</td>      
 *  </tr>
 *  <tr>
 *      <td><<</td>
 *      <td>$bsl</td>
 *      <td>Object binary shift left operator, the default functionality is to binary shift left the left hand object by the right hand object (identical to Java <code><<</code> operator)</td>      
 *  </tr>
 *  <tr>
 *      <td>>></td>
 *      <td>$bsr</td>
 *      <td>Object binary shift right operator, the default functionality is to binary shift right the left hand object by the right hand object (identical to Java <code>>></code> operator)</td>      
 *  </tr>
 *  <tr>
 *      <td>^</td>
 *      <td>$xor</td>
 *      <td>Object binary EXCLUSIVE OR operator, the default functionality is to binary exclusive or of the left hand object by the right hand object (identical to Java <code>^</code> operator)</td>      
 *  </tr>
 *  <tr>
 *      <td>[]</td>
 *      <td>$sindex</td>
 *      <td>Object set at index operator, the default functionality is to set the right hand object at the supplied index of the left hand object (identical to Java <code>left[index] = right</code> operator)</td>      
 *  </tr>
 *  <tr>
 *      <td>[]</td>
 *      <td>$index</td>
 *      <td>Object get object at index operator, the default functionality is to retrieve the object at the supplied index (identical to Java <code>left[index]</code> operator)</td>      
 *  </tr>
 *  <tr>
 *      <td>toString()</td>
 *      <td>toString</td>
 *      <td>Object toString operator, should convert the object to a {@link String} object (identical to Java <code>toString()</code> method)</td>      
 *  </tr>
 * </table>
 *
 * <p>
 * Some example Leola scripts that override the above operators:
 * <pre>
 *   class Vector(x, y) {
 *     var $add = def(other) {
 *       return new Vector(x+other.x, y+other.y)
 *     }
 *     var $sub = def(other) {
 *       return new Vector(x-other.x, y-other.y)
 *     }
 *     var $mul = def(other) {
 *       return case 
 *          when other is Vector -> new Vector(x*other.x, y*other.y)
 *          else new Vector(x*other, y*other)
 *      }
 *      
 *     var toString = def() {
 *       return "(" + x + "," + y + ")"
 *     }
 *   }
 *   
 *   var v = new Vector(10, 5)
 *   var z = new Vector(4, 10)
 *   
 *   var delta = v-z
 *   println(delta) // (6,-5) 
 *   println(delta * 2) // 12, -10
 *   
 * </pre>
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
	 * A convenience means for retrieving the {@link LeoNull} object
	 */
	public static final LeoObject NULL = LeoNull.LEONULL;
	public static final LeoObject TRUE = LeoBoolean.LEOTRUE;
	public static final LeoObject FALSE = LeoBoolean.LEOFALSE;
	
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
	protected LeoObject(LeoType type) {
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
	 * Determines if the supplied object is either a Java <code>null</code> or
	 * a {@link LeoNull} instance. 
	 * 
	 * @param obj
	 * @return true if either <code>null</code> or
	 * a {@link LeoNull} instance. 
	 */
	public static boolean isNull(LeoObject obj) {
		return obj == null || obj == LeoNull.NULL;
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
	 * Tests if the references are not the same. Java equivalent to ! '=='
	 * @param other
	 * @return true if they are not the same references
	 */
	public boolean $rneq(LeoObject other) {
        return (this != other);
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
	

	public LeoObject $add(LeoObject other) { throwNotImplementedError("$add"); return null; }
	public LeoObject $sub(LeoObject other) { throwNotImplementedError("$sub"); return null; }
	public LeoObject $mul(LeoObject other) { throwNotImplementedError("$mul"); return null; }
	public LeoObject $div(LeoObject other) { throwNotImplementedError("$div"); return null; }
	public LeoObject $mod(LeoObject other) { throwNotImplementedError("$mod"); return null; }
	
	public LeoObject $add(int other) { throwNotImplementedError("$add"); return null; }
	public LeoObject $sub(int other) { throwNotImplementedError("$sub"); return null; }
	public LeoObject $mul(int other) { throwNotImplementedError("$mul"); return null; }
	public LeoObject $div(int other) { throwNotImplementedError("$div"); return null; }
	public LeoObject $mod(int other) { throwNotImplementedError("$mod"); return null; }
	
	public LeoObject $add(double other) { throwNotImplementedError("$add"); return null; }
	public LeoObject $sub(double other) { throwNotImplementedError("$sub"); return null; }
	public LeoObject $mul(double other) { throwNotImplementedError("$mul"); return null; }
	public LeoObject $div(double other) { throwNotImplementedError("$div"); return null; }
	public LeoObject $mod(double other) { throwNotImplementedError("$mod"); return null; }
	
	
	public LeoObject $add(long other) { throwNotImplementedError("$add"); return null; }
	public LeoObject $sub(long other) { throwNotImplementedError("$sub"); return null; }
	public LeoObject $mul(long other) { throwNotImplementedError("$mul"); return null; }
	public LeoObject $div(long other) { throwNotImplementedError("$div"); return null; }
	public LeoObject $mod(long other) { throwNotImplementedError("$mod"); return null; }
	
	public LeoObject $neg() { throwNotImplementedError("$neg"); return null; }
//	public LeoObject not() { return null; }
	public LeoObject $bnot() { throwNotImplementedError("$bnot"); return null; }
	
	public LeoObject $index(LeoObject other) { throwNotImplementedError("$index"); return null; }
	public void      $sindex(LeoObject key, LeoObject other) { throwNotImplementedError("$sindex"); }
	public LeoObject $bsl(LeoObject other) { throwNotImplementedError("$bsl"); return null; }
	public LeoObject $bsr(LeoObject other) { throwNotImplementedError("$bsr"); return null; }
	public LeoObject $xor(LeoObject other) { throwNotImplementedError("$xor"); return null; }
	public LeoObject $bor(LeoObject other) { throwNotImplementedError("$bor"); return null; }
	public LeoObject $band(LeoObject other) { throwNotImplementedError("$band"); return null; }
	
	
	public LeoObject $index(int other) { throwNotImplementedError("$index"); return null; }
	public LeoObject $bsl(int other) { throwNotImplementedError("$bsl"); return null; }
	public LeoObject $bsr(int other) { throwNotImplementedError("$bsr"); return null; }
	public LeoObject $xor(int other) { throwNotImplementedError("$xor"); return null; }
	public LeoObject $bor(int other) { throwNotImplementedError("$bor"); return null; }
	public LeoObject $band(int other) { throwNotImplementedError("$band"); return null; }
	
	public LeoObject $index(double other) { throwNotImplementedError("$index"); return null; }
	public LeoObject $bsl(double other) { throwNotImplementedError("$bsl"); return null; }
	public LeoObject $bsr(double other) { throwNotImplementedError("$bsr"); return null; }
	public LeoObject $xor(double other) { throwNotImplementedError("$xor"); return null; }
	public LeoObject $bor(double other) { throwNotImplementedError("$bor"); return null; }
	public LeoObject $band(double other) { throwNotImplementedError("$band"); return null; }

	
	public LeoObject $index(long other) { throwNotImplementedError("$index"); return null; }
	public LeoObject $bsl(long other) { throwNotImplementedError("$bsl"); return null; }
	public LeoObject $bsr(long other) { throwNotImplementedError("$bsr"); return null; }
	public LeoObject $xor(long other) { throwNotImplementedError("$xor"); return null; }
	public LeoObject $bor(long other) { throwNotImplementedError("$bor"); return null; }
	public LeoObject $band(long other) { throwNotImplementedError("$band"); return null; }

		
	/**
	 * Sets a property on this object.
	 * 
	 * @param key
	 * @param value
	 */
	public void setObject(LeoObject key, LeoObject value) {
	    throw new LeolaRuntimeException("AccessError: " + this + " is not a complex object; unable to access: '" + key + "'");
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
	 * Similar to {@link LeoObject#getObject(LeoObject)} in every way, with the exception that
	 * this will throw a {@link LeolaRuntimeException} is the attribute is not found.
	 * 
	 * @param key
	 * @return the object associated with the supplied key
	 * @throws LeolaRuntimeException if the key is not bound
	 */
	public LeoObject xgetObject(LeoObject key) throws LeolaRuntimeException {
	    throw new LeolaRuntimeException("AccessError: " + this + " is not a complex object; unable to access: '" + key + "'");
	}
	
	/**
     * Similar to {@link LeoObject#getObject(String)} in every way, with the exception that
     * this will throw a {@link LeolaRuntimeException} is the attribute is not found.
     * 
     * 
     * @param key
     * @return the object associated with the supplied key
     * @throws LeolaRuntimeException if the key is not bound
     */
	public LeoObject xgetObject(String key) throws LeolaRuntimeException {
	    return xgetObject(LeoString.valueOf(key));
	}
	
	/**
	 * Retrieves a property from this object.  If this {@link LeoObject} supports associations, this
	 * will attempt to find the object with the associated key. If the key is not found, this will return {@link LeoObject#NULL}.
	 * 
	 * @see LeoObject#xgetObject(LeoObject)
	 * @param key
	 * @return the associated object if found; otherwise {@link LeoObject#NULL}
	 */
	public LeoObject getObject(LeoObject key) {
		throw new LeolaRuntimeException("AccessError: " + this + " is not a complex object; unable to access: '" + key + "'");
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
	    throw new LeolaRuntimeException("AccessError: " + this + " is not a complex object; unable to access: '" + key + "'");
	}
	
	/**
	 * This is the equivalent of:
	 * 
	 * <pre>
	 *    LeoObject result = x.call().throwIfError();
	 * </pre>
	 * 
	 * @return the result of invoking the function.
	 * @throws LeolaRuntimeException if the result of invoking the object returns a {@link LeoError}
	 */
	public LeoObject xcall() throws LeolaRuntimeException {
	    return call().throwIfError();
	}
	
	/**
     * This is the equivalent of:
     * 
     * <pre>
     *    LeoObject result = x.call(arg1).throwIfError();
     * </pre>
     * 
     * @param arg1 the first argument
     * @return the result of invoking the function.
     * @throws LeolaRuntimeException if the result of invoking the object returns a {@link LeoError}
     */
    public LeoObject xcall(LeoObject arg1) throws LeolaRuntimeException {
        return call(arg1).throwIfError();
    }
	
    /**
     * This is the equivalent of:
     * 
     * <pre>
     *    LeoObject result = x.call(arg1,arg2).throwIfError();
     * </pre>
     * 
     * @param arg1 the first argument
     * @param arg2 the second argument
     * @return the result of invoking the function.
     * @throws LeolaRuntimeException if the result of invoking the object returns a {@link LeoError}
     */
    public LeoObject xcall(LeoObject arg1, LeoObject arg2) throws LeolaRuntimeException {
        return call(arg1, arg2).throwIfError();
    }
    
    /**
     * This is the equivalent of:
     * 
     * <pre>
     *    LeoObject result = x.call(arg1,arg2,arg3).throwIfError();
     * </pre>
     * 
     * @param arg1 the first argument
     * @param arg2 the second argument
     * @param arg3 the third argument
     * @return the result of invoking the function.
     * @throws LeolaRuntimeException if the result of invoking the object returns a {@link LeoError}
     */
    public LeoObject xcall(LeoObject arg1, LeoObject arg2, LeoObject arg3) throws LeolaRuntimeException {
        return call(arg1, arg2,arg3).throwIfError();
    }
    
    /**
     * This is the equivalent of:
     * 
     * <pre>
     *    LeoObject result = x.call(arg1,arg2,arg3,arg4).throwIfError();
     * </pre>
     * 
     * @param arg1 the first argument
     * @param arg2 the second argument
     * @param arg3 the third argument
     * @param arg4 the fourth argument
     * @return the result of invoking the function.
     * @throws LeolaRuntimeException if the result of invoking the object returns a {@link LeoError}
     */
    public LeoObject xcall(LeoObject arg1, LeoObject arg2, LeoObject arg3, LeoObject arg4) throws LeolaRuntimeException {
        return call(arg1, arg2, arg3, arg4).throwIfError();
    }
    
    /**
     * This is the equivalent of:
     * 
     * <pre>
     *    LeoObject result = x.call(arg1,arg2,arg3,arg4,arg5).throwIfError();
     * </pre>
     * 
     * @param arg1 the first argument
     * @param arg2 the second argument
     * @param arg3 the third argument
     * @param arg4 the fourth argument
     * @param arg5 the fifth argument
     * @return the result of invoking the function.
     * @throws LeolaRuntimeException if the result of invoking the object returns a {@link LeoError}
     */
    public LeoObject xcall(LeoObject arg1, LeoObject arg2, LeoObject arg3, LeoObject arg4, LeoObject arg5) throws LeolaRuntimeException {
        return call(arg1, arg2, arg3, arg4, arg5).throwIfError();
    }
	
    /**
     * This is the equivalent of:
     * 
     * <pre>
     *    LeoObject result = x.call(args).throwIfError();
     * </pre>
     * 
     * @param args the argument list
     * @return the result of invoking the function.
     * @throws LeolaRuntimeException if the result of invoking the object returns a {@link LeoError}
     */
    public LeoObject xcall(LeoObject[] args) throws LeolaRuntimeException {
        return call(args).throwIfError();
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
	 * Determines if the supplied Java type can be assigned to this
	 * {@link LeoObject}.
	 * 
	 * @param javaType
	 * @return true if the supplied type can be represented (assigned) to
	 * this {@link LeoObject}
	 */
	public boolean isAssignable(Class<?> javaType) {
	    Object obj = getValue();
	    if(obj!=null) {
	        return javaType.isAssignableFrom(obj.getClass());
	    }
	    return false;
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
	 * @return the number of parameters this object takes
	 */
	public int getNumberOfArgs() {
        return 0;
    }
    
    /**
     * @return true if this function has variable arguments
     */
    public boolean hasVarargs() {
        return false;
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
	 * Throws a {@link LeolaRuntimeException} if this is a {@link LeoError} instance
	 * 
	 * @return this object for method chaining
	 * @throws LeolaRuntimeException the underlying {@link LeoError}
	 * 
	 */
	public LeoObject throwIfError() throws LeolaRuntimeException {
	    if(isError()) {
	        LeoError error = as();
	        throw new LeolaRuntimeException(error);
	    }
	    return this;
	}

    /**
     * Throws a ClassNotFoundError
     * 
     * @param message the error message
     * @throws LeolaRuntimeException
     */
    public static void throwClassNotFoundError(String message) throws LeolaRuntimeException {
        throw new LeolaRuntimeException("NoClassDefinitionError: " + message);
    }
    
    /**
     * Throws a MethodError
     * 
     * @param message the error message
     * @throws LeolaRuntimeException
     */
    public static void throwMethodError(String message) throws LeolaRuntimeException {
        throw new LeolaRuntimeException("MethodError: " + message);
    }
    
    
	/**
	 * Throws a NativeMethodError 
	 * 
	 * @param message the error message
	 * @throws LeolaRuntimeException
	 */
	public static void throwNativeMethodError(String message) throws LeolaRuntimeException {
	    throw new LeolaRuntimeException("NativeMethodError: " + message);
	}
	
	/**
	 * Throws a {@link LeolaRuntimeException} denoting a DivideByZeroError.
	 * @throws LeolaRuntimeException
	 */
	public static void throwDivideByZeroError() throws LeolaRuntimeException {
	    throw new LeolaRuntimeException("DivideByZeroError: Can't divide by zero.");
	}
	
	/**
	 * Throws a {@link LeolaRuntimeException} denoting a NotImplemetnedError.
	 * @throws LeolaRuntimeException
	 */
	public void throwNotImplementedError(String functionName) throws LeolaRuntimeException {
	    throw new LeolaRuntimeException("NotImplementedError: '" + getType() + "' does not implement the '" + functionName + "' method.");
	}
	
	/**
     * Throws a {@link LeolaRuntimeException} denoting an AttributeError, stating that a requested attribute
     * does not exist in the owned scope.
     * 
     * @param name the name of the attribute
     * @throws LeolaRuntimeException
     */
    public void throwAttributeError(LeoObject name) throws LeolaRuntimeException {
        if(isClass()) {
            if(isNativeClass()) {
                LeoNativeClass nClass = as();
                throwAttributeError(nClass.getNativeClass(), name);
            }
            else {
                LeoClass aClass = as();
                throw new LeolaRuntimeException
                    ("AttributeError: '" + aClass.getClassName() + "' has no attribute with the name '" + name + "'");
            }
        }
        else if(isNamespace()) {
            LeoNamespace namespace = as();
            throw new LeolaRuntimeException
                ("AttributeError: '" + namespace.getName() + "' has no attribute with the name '" + name + "'");
        }
        else {
            throw new LeolaRuntimeException
                ("AttributeError: No attribute found with the name '" + name + "'");
        }
    }
	
	/**
	 * Throws a {@link LeolaRuntimeException} denoting an AttributeError, stating that a requested attribute
	 * does not exist in the owned scope and/or class.
	 * 
	 * @param ownerClass the containing class in which the attribute was requested.
	 * @param name the name of the attribute
	 * @throws LeolaRuntimeException
	 */
	public static void throwAttributeError(Class<?> ownerClass, LeoObject name) throws LeolaRuntimeException {
	    throw new LeolaRuntimeException
	        ("AttributeError: '" + ownerClass.getSimpleName() + "' has no attribute with the name '" + name + "'");
	}
	
	
	/**
     * Throws a {@link LeolaRuntimeException} denoting an AttributeAccessError, stating that a requested attribute
     * could not be accessed in the owned scope and/or class.
     * 
     * @param ownerClass the containing class in which the attribute was requested.
     * @param name the name of the attribute
     * @throws LeolaRuntimeException
     */
	public static void throwAttributeAccessError(Class<?> ownerClass, LeoObject name) throws LeolaRuntimeException {
	    throw new LeolaRuntimeException
	        ("AttributeAccessError: '" + ownerClass.getSimpleName() + "' could not access attribute with the name '" + name + "'");
    }
	
	/**
	 * Rethrows the supplied {@link Throwable} as a {@link LeolaRuntimeException}
	 * 
	 * @param t
	 * @throws LeolaRuntimeException
	 */
	public static void rethrow(Throwable t) throws LeolaRuntimeException {
	    throw new LeolaRuntimeException(t);
	}
	
	/**
	 * Determines if the supplied owner has a method by the supplied method name.
	 * 
	 * @param owner
	 * @param methodName
	 * @return true if the supplied object (owner) has a method by the supplied name
	 */
	protected static boolean hasNativeMethod(Object owner, LeoObject methodName) {
	    return hasNativeMethod(owner.getClass(), methodName);
	}
	
	/**
     * Determines if the supplied class has a method by the supplied method name.
     * 
     * @param aClass
     * @param methodName
     * @return true if the supplied object (owner) has a method by the supplied name
     */
    protected static boolean hasNativeMethod(Class<?> aClass, LeoObject methodName) {
        List<Method> methods = ClassUtil.getMethodsByName(aClass, methodName.toString());
        removeInterfaceMethods(methods);
        return !methods.isEmpty();
    }
	
    /**
     * Retrieve the native methods or fields from the owner public method listings.  This will query the owner class
     * using reflection if the supplied nativeApi {@link Map} is empty.
     * 
     * @param ownerClass the class in which to inspect
     * @param owner the instance in which owns the native methods; this may be null if looking for static methods
     * @param nativeApi the cache of native methods
     * @param key the method name
     * @return the {@link LeoObject} of the native method, or null if not found
     */
    protected static LeoObject getNativeMember(Class<?> ownerClass, Object owner, Map<LeoObject, LeoObject> nativeApi, LeoObject key) {
        LeoObject func = nativeApi.get(key);
        if (!LeoObject.isTrue(func)) {
            synchronized (nativeApi) {
                /* unsure that we don't override
                 * any previous thread's attempt at creating 
                 * our native function
                 */
                func = nativeApi.get(key);
                if (!LeoObject.isTrue(func)) {
                    
                    String keyStr = key.toString();
                    
                    List<Method> methods = ClassUtil.getMethodsByName(ownerClass, keyStr);
                    removeInterfaceMethods(methods);
        
                    if (methods.isEmpty()) {
                        methods = ClassUtil.getMethodsByAnnotationAlias(ownerClass, keyStr);
                        
                        /* If there still isn't any methods by this name,
                         * check the classes field members
                         */
                        if(methods.isEmpty()) {      
                            Field field = ClassUtil.getInheritedField(ownerClass, keyStr);
                            if(field != null) {
                                try {
                                    Object value = field.get(owner);
                                    func = LeoObject.valueOf(value);
                                    /* Do not cache this value, as it 
                                     * may change by calling methods
                                     * on this native object, so we 
                                     * must grab the latest value
                                     * every time
                                     */
                                    //nativeApi.put(key, func);
                                    
                                    return (func);
                                }
                                catch(Exception e) {
                                    throwAttributeAccessError(ownerClass, key);
                                }
                            }
                            
                            throwAttributeError(ownerClass, key);
                        }
                    }
        
                    func = new LeoNativeFunction(methods, owner);
                    nativeApi.put(key, func);
                }
            }
        }
        
        return func;
    }
	
	/**
     * Retrieve the native methods from the owner public method listings.  This will query the owner class
     * using reflection if the supplied nativeApi {@link Map} is empty.
     * 
     * @param ownerClass the class in which to inspect
     * @param owner the instance in which owns the native methods; this may be null if looking for static methods
     * @param nativeApi the cache of native methods
     * @param key the method name
     * @return the {@link LeoObject} of the native method, or null if not found
     */
    protected static LeoObject getNativeMethod(Class<?> ownerClass, Object owner, Map<LeoObject, LeoObject> nativeApi, LeoObject key) {
        LeoObject func = nativeApi.get(key);
        if (!LeoObject.isTrue(func)) {
            synchronized (nativeApi) {
                /* unsure that we don't override
                 * any previous thread's attempt at creating 
                 * our native function
                 */
                func = nativeApi.get(key);
                if (!LeoObject.isTrue(func)) {
                    List<Method> methods = ClassUtil.getMethodsByName(ownerClass, key.toString());
                    removeInterfaceMethods(methods);
        
                    if (methods.isEmpty()) {
                        methods = ClassUtil.getMethodsByAnnotationAlias(ownerClass, key.toString());
                        if(methods.isEmpty()) {                    
                            throwAttributeError(ownerClass, key);
                        }
                    }
        
                    func = new LeoNativeFunction(methods, owner);
                    nativeApi.put(key, func);
                }
            }
        }
        
        return func;
    }
	
	/**
	 * Retrieve the native methods from the owner public method listings.  This will query the owner class
	 * using reflection if the supplied nativeApi {@link Map} is empty.
	 * 
	 * @param owner the instance in which owns the native methods; owner can not be null
	 * @param nativeApi the cache of native methods
	 * @param key the method name
	 * @return the {@link LeoObject} of the native method, or null if not found
	 */
    protected static LeoObject getNativeMethod(Object owner, Map<LeoObject, LeoObject> nativeApi, LeoObject key) {
        return getNativeMethod(owner.getClass(), owner, nativeApi, key);
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

