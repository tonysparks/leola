/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.types;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import leola.vm.util.ClassUtil;



/**
 * Leola boolean value
 *
 * @author Tony
 *
 */
public class LeoBoolean extends LeoObject {

	/**
	 * True
	 */
	public static final LeoBoolean LEOTRUE = new LeoBoolean(true);

	/**
	 * False
	 */
	public static final LeoBoolean LEOFALSE = new LeoBoolean(false);

	/*
	 * Value
	 */
	private boolean value;

	/**
	 * @param value
	 */
	private LeoBoolean(boolean value) {
		super(LeoType.BOOLEAN);
		this.value = value;
	}

	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#hashCode()
	 */
	@Override
	public int hashCode() {	
		return value ? 1231 : 1237;
	}
	
	@Override
	public boolean isBoolean() {
		return true;
	}
	
   @Override
    public int asInt() {
        return (isTrue()) ? 1 : 0;
    }

    @Override
    public double asDouble() {
        return (isTrue()) ? 1.0 : 0.0;
    }
    
    @Override
    public long asLong() {
        return (isTrue()) ? 1L : 0L;
    }
	
	/**
	 * @return true if this is an instance of True
	 */
	@Override
	public boolean isTrue() {
		return this.value;
	}

	/**
	 * @return true if this is an instance of False
	 */
	public boolean isFalse() {
		return ! this.value;
	}

	/**
	 * Gets the instance of the boolean.
	 *
	 * @param value
	 * @return
	 */
	public static LeoBoolean valueOf(boolean value) {
		return value ? LEOTRUE : LEOFALSE;
	}

	/* (non-Javadoc)
	 * @see leola.types.LeoObject#toString()
	 */
	@Override
	public String toString() {
		return Boolean.toString(this.value);
	}

	/* (non-Javadoc)
	 * @see leola.types.LeoObject#eq(leola.types.LeoObject)
	 */
	@Override
	public boolean $eq(LeoObject other) {
		return (other != null) && (other.isTrue() == this.isTrue());
	}

	/* (non-Javadoc)
	 * @see leola.types.LeoObject#gt(leola.types.LeoObject)
	 */
	@Override
	public boolean $gt(LeoObject other) {
		return false;
	}

	/* (non-Javadoc)
	 * @see leola.types.LeoObject#lt(leola.types.LeoObject)
	 */
	@Override
	public boolean $lt(LeoObject other) {
		return false;
	}

	/* (non-Javadoc)
	 * @see leola.types.LeoObject#getValue()
	 */
	@Override
	public Object getValue() {
		return this.isTrue();
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#getValue(java.lang.Class)
	 */
	@Override
	public Object getValue(Class<?> type) {	
	    if(ClassUtil.isType(type, ClassUtil.BOOLEAN)) {
	        return isTrue();
	    }
	    else if ( ClassUtil.isType(type, ClassUtil.BYTE) ){
			return (byte)(isTrue() ? 1 : 0);
		}
		else if ( ClassUtil.isType(type, ClassUtil.CHAR) ){
			return (char)(isTrue() ? 1 : 0);
		}
		else if ( ClassUtil.isType(type, ClassUtil.SHORT) ){
			return (short)(isTrue() ? 1 : 0);
		}
		else if ( ClassUtil.isType(type, ClassUtil.INT) ){
			return (int)(isTrue() ? 1 : 0);
		}
		else if ( ClassUtil.isType(type, ClassUtil.FLOAT) ){
			return (float)(isTrue() ? 1 : 0);
		}
		else if ( ClassUtil.isType(type, ClassUtil.DOUBLE) ){
			return (double)(isTrue() ? 1 : 0);
		}
		else if ( ClassUtil.isType(type, ClassUtil.LONG) ){
			return (long)(isTrue() ? 1 : 0);
		}

		else if ( ClassUtil.isType(type, ClassUtil.STRING) ){
			return Boolean.toString(isTrue());
		}
		else if(ClassUtil.inheritsFrom(type, LeoObject.class) ) {
            return this;
        }
		
		return isTrue();
	}
	
	@Override
    public boolean isAssignable(Class<?> javaType) {
        if(javaType.isPrimitive()) {
            javaType = ClassUtil.primitiveToWrapper(javaType);
        }
        return Boolean.class.isAssignableFrom(javaType);
    }

	/* (non-Javadoc)
	 * @see leola.types.LeoObject#clone()
	 */
	@Override
	public LeoObject clone() {
		return this;
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#add(leola.vm.types.LeoObject)
	 */
	@Override
	public LeoObject $add(LeoObject other) {
		if ( other.isString() ) {
			return LeoString.valueOf(this.value + other.toString());
		}
		return super.$add(other);
	}
	
	/* (non-Javadoc)
	 * @see leola.types.LeoObject#bnot()
	 */
	@Override
	public LeoObject $bnot() {
		LeoObject result = this.value ? LEOFALSE : LEOTRUE;			
		return result;
	}

	
	/* (non-Javadoc)
	 * @see leola.types.LeoObject#xor(leola.types.LeoObject)
	 */
	@Override
	public LeoObject $xor(LeoObject other) {		
		LeoObject result = (this.value ^ LeoObject.isTrue(other)) ? LEOTRUE : LEOFALSE;		
		return result;
	}

	/* (non-Javadoc)
	 * @see leola.types.LeoObject#lor(leola.types.LeoObject)
	 */
	@Override
	public LeoObject $bor(LeoObject other) {
		LeoObject result = (this.value | LeoObject.isTrue(other)) ? LEOTRUE : LEOFALSE;		
		return result;
	}

	/* (non-Javadoc)
	 * @see leola.types.LeoObject#land(leola.types.LeoObject)
	 */
	@Override
	public LeoObject $band(LeoObject other) {
		LeoObject result = (this.value & LeoObject.isTrue(other)) ? LEOTRUE : LEOFALSE;		
		return result;
	}
	
	@Override
	public void write(DataOutput out) throws IOException {
		out.write(this.getType().ordinal());
		out.writeBoolean(this.value);
	}
	
	/**
	 * Reads from the {@link DataInput} stream, constructing a {@link LeoObject}
	 * 
	 * @param in
	 * @return the {@link LeoObject}
	 * @throws IOException
	 */
	public static LeoBoolean read(LeoObject env, DataInput in) throws IOException {		
		return in.readBoolean() ? LEOTRUE : LEOFALSE;
	}
}

