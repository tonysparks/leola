/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.types;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import leola.vm.VM;
import leola.vm.asm.Symbols;
import leola.vm.exceptions.LeolaRuntimeException;


/**
 * Base object for Leola types.
 *
 * @author Tony
 *
 */
public abstract class LeoObject {

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
	
	public static final LeoString toString = LeoString.valueOf("toString");
	
	
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
		  , FUNCTION
		  , NATIVE_FUNCTION
		  , ARRAY
		  , MAP
		  , CLASS
		  , NAMESPACE
		  , NATIVE_CLASS	
		  , ERROR
		;
		  
		public static LeoType fromOrdinal(int ordinal) {
			return values()[ordinal];
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
	public boolean isOfType(Class<? extends LeoObject> ... leoTypes) {
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
	
	public LeoObject $bsl(LeoObject other) { return null; }
	public LeoObject $bsr(LeoObject other) { return null; }
	public LeoObject $xor(LeoObject other) { return null; }
	public LeoObject $bor(LeoObject other) { return null; }
	public LeoObject $band(LeoObject other) { return null; }
	
	public LeoObject $bsl(int other) { return null; }
	public LeoObject $bsr(int other) { return null; }
	public LeoObject $xor(int other) { return null; }
	public LeoObject $bor(int other) { return null; }
	public LeoObject $band(int other) { return null; }
	
	public LeoObject $bsl(double other) { return null; }
	public LeoObject $bsr(double other) { return null; }
	public LeoObject $xor(double other) { return null; }
	public LeoObject $bor(double other) { return null; }
	public LeoObject $band(double other) { return null; }

	
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
	 * Retrieves a property from this object.
	 * 
	 * @param key
	 * @return
	 */
	public LeoObject getObject(LeoObject key) {
		throw new LeolaRuntimeException(this + " is not a complex object");
	}
	
	
	/**
	 * Invokes the function
	 * 
	 * @param vm
	 * @return
	 */
	public LeoObject call(VM vm) {
		throw new LeolaRuntimeException(this + " is not a function.");
	}
	
	/**
	 * Invokes the function
	 * 
	 * @param vm
	 * @param arg1
	 * @return
	 */
	public LeoObject call(VM vm, LeoObject arg1) {
		throw new LeolaRuntimeException(this + " is not a function.");		
	}
	
	/**
	 * Invokes the function
	 * 
	 * @param vm
	 * @param arg1
	 * @param arg2
	 * @return
	 */
	public LeoObject call(VM vm, LeoObject arg1, LeoObject arg2) {
		throw new LeolaRuntimeException(this + " is not a function.");
	}
	
	/**
	 * Invokes the function
	 * 
	 * @param vm
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @return
	 */
	public LeoObject call(VM vm, LeoObject arg1, LeoObject arg2, LeoObject arg3) {
		throw new LeolaRuntimeException(this + " is not a function.");
	}
	
	/**
	 * Invokes the function
	 * 
	 * @param vm
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 * @return
	 */
	public LeoObject call(VM vm, LeoObject arg1, LeoObject arg2, LeoObject arg3, LeoObject arg4) {
		throw new LeolaRuntimeException(this + " is not a function.");
	}
	
	/**
	 * Invokes the function
	 * 
	 * @param vm
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 * @param arg5
	 * @return
	 */
	public LeoObject call(VM vm, LeoObject arg1, LeoObject arg2, LeoObject arg3, LeoObject arg4, LeoObject arg5) {
		throw new LeolaRuntimeException(this + " is not a function.");
	}
	
	/**
	 * Invokes the function
	 * 
	 * @param vm
	 * @param args
	 * @return
	 */
	public LeoObject call(VM vm, LeoObject[] args) {
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
	public static LeoObject read(LeoObject env, Symbols symbols, DataInput in) throws IOException {
		int type = in.readByte();
		LeoType leoType = LeoType.fromOrdinal(type);
		LeoObject result = null;
		
		switch(leoType) {
			case ARRAY: {
				result = LeoArray.read(env, symbols, in);
				break;
			}
			case BOOLEAN: {
				result = LeoBoolean.read(env, symbols, in);
				break;
			}
			case CLASS: {
			//	result = LeoClass.read(env, symbols, in); TODO
				break;
			}
			case FUNCTION: {
				result = LeoFunction.read(env, symbols, in);
				break;
			}
			case MAP: {
				result = LeoMap.read(env, symbols, in);
				break;
			}
			case NAMESPACE: {
				break;
			}
			case NATIVE_CLASS: {
				break;
			}
			case NATIVE_FUNCTION: {
				break;				
			}
			case NULL: {
				result = LeoNull.read(symbols, in);
				break;
			}
			case INTEGER: {
				result = LeoInteger.read(symbols, in);
				break;
			}
			case LONG: {
				result = LeoLong.read(symbols, in);
				break;
			}
			case REAL: {
				result = LeoDouble.read(symbols, in);
				break;
			}
			case STRING: {
				result = LeoString.read(symbols, in);
				break;
			}
		}
		
		return result;
	}
}

