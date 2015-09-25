/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.types;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import leola.vm.exceptions.LeolaRuntimeException;
import leola.vm.util.ClassUtil;


/**
 * Represents a double float precision number
 * 
 * @author Tony
 *
 */
public class LeoLong extends LeoObject {
		
	
	/**
	 * Number
	 */
	private long number;

	/**
     * Converts the java long into a {@link LeoLong}
     * 
     * @param number
     * @return
     */
    public static LeoLong valueOf(Long number) {
        return new LeoLong(number);
    }
	
	/**
	 * Converts the java long into a {@link LeoLong}
	 * 
	 * @param number
	 * @return
	 */
	public static LeoLong valueOf(long number) {
	    return new LeoLong(number);
	}
	
	/**
	 * @param value
	 */
	public LeoLong(long number) {
		super(LeoType.LONG);
		this.number = number;
	}

	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int)(number ^ (number >>> 32));
	}
	
	/**
	 * @return the number
	 */
	public long getNumber() {
		return number;
	}


	@Override
	public int asInt() {
	    return (int)this.number;
	}

	@Override
    public double asDouble() {
        return (double)this.number;
    }
	
	@Override
    public long asLong() {
        return this.number;
    }
	
	@Override
	public float asFloat() {
	    return (float)this.number;
	}

	@Override
	public byte asByte() {
	    return (byte)this.number;
	}

	@Override
	public short asShort() {
	    return (short)this.number;
	}	

	@Override
	public char asChar() {
		return (char)this.number;
	}

	/* (non-Javadoc)
	 * @see leola.types.LeoObject#isNumber()
	 */
	@Override
	public boolean isNumber() {	
		return true;
	}
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#isTrue()
	 */
	@Override
	public boolean isTrue() {
		return this.number != 0;
	}
	
	/* (non-Javadoc)
	 * @see leola.types.LeoObject#toString()
	 */
	@Override
	public String toString() {
		return Long.toString(number);
	}
	
	/* (non-Javadoc)
	 * @see leola.types.LeoObject#getValue()
	 */
	@Override
	public Object getValue() {
		return this.number;
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#getValue(java.lang.Class)
	 */
	@Override
	public Object getValue(Class<?> type) {
	    if ( ClassUtil.isType(type, ClassUtil.LONG) ){
            return this.number;
        }
	    else if ( ClassUtil.isType(type, ClassUtil.INT) ){
            return (int)this.number;
        }
	    else if(ClassUtil.inheritsFrom(type, LeoObject.class) ) {
			return this;
		}
		else if ( ClassUtil.isType(type, ClassUtil.BYTE) ){
			return (byte)this.number;
		}
		else if ( ClassUtil.isType(type, ClassUtil.CHAR) ){
			return (char)this.number;
		}
		else if ( ClassUtil.isType(type, ClassUtil.SHORT) ){
			return (short)this.number;
		}		
		else if ( ClassUtil.isType(type, ClassUtil.FLOAT) ){
			return (float)this.number;
		}
		else if ( ClassUtil.isType(type, ClassUtil.DOUBLE) ){
			return (double)this.number;
		}		
		else if ( ClassUtil.isType(type, ClassUtil.STRING) ){
			return this.number + "";
		}
		return this.number;
	}


	/* (non-Javadoc)
	 * @see leola.types.LeoObject#clone()
	 */
	@Override
	public LeoObject clone() {
		return this;
	}

	/* (non-Javadoc)
	 * @see leola.types.LeoObject#eq(leola.types.LeoObject)
	 */
	@Override
	public boolean $eq(LeoObject other) {
		return other.$eq(this.number);
	}	
	@Override
	public boolean $eq(double other) {	
		return other == this.number;
	}	
	@Override
	public boolean $eq(int other) {
		return other == this.number;
	}
	@Override
	public boolean $eq(long other) {
		return other == this.number;
	}

	/* (non-Javadoc)
	 * @see leola.types.LeoObject#gt(leola.types.LeoObject)
	 */

	@Override
	public boolean $gt(LeoObject other) {
		return other.$gt(this.number);
	}
	@Override
	public boolean $gt(double other) {
		return other > this.number;
	}
	@Override
	public boolean $gt(int other) {
		return other > this.number;
	}
	@Override
	public boolean $gt(long other) {
		return other > this.number;
	}

	/* (non-Javadoc)
	 * @see leola.types.LeoObject#lt(leola.types.LeoObject)
	 */
	@Override
	public boolean $lt(LeoObject other) {
		return other.$lt(this.number);
	}
	@Override
	public boolean $lt(double other) {
		return other < this.number;
	}
	@Override
	public boolean $lt(int other) {
		return other < this.number;
	}
	@Override
	public boolean $lt(long other) {
		return other < this.number;
	}

	/* (non-Javadoc)
	 * @see leola.types.LeoObject#gte(leola.types.LeoObject)
	 */
	@Override
	public boolean $gte(LeoObject other) {
		return other.$gte(this.number);
	}
	@Override
	public boolean $gte(double other) {
		return other >= this.number;
	}
	@Override
	public boolean $gte(int other) {
		return other >= this.number;
	}
	@Override
	public boolean $gte(long other) {
		return other >= this.number;
	}

	/* (non-Javadoc)
	 * @see leola.types.LeoObject#lte(leola.types.LeoObject)
	 */
	@Override
	public boolean $lte(LeoObject other) {
		return other.$lte(this.number);
	}
	@Override
	public boolean $lte(double other) {
		return other <= this.number;
	}
	@Override
	public boolean $lte(int other) {
		return other <= this.number;
	}
	@Override
	public boolean $lte(long other) {
		return other <= this.number;
	}
	
	
	/* (non-Javadoc)
	 * @see leola.types.LeoObject#add(leola.types.LeoObject)
	 */
	@Override
	public LeoObject $add(LeoObject other) {
		return other.$add(this.number);		
	}	
	@Override
	public LeoObject $add(double other) {
		return LeoDouble.valueOf(other + this.number);
	}	
	@Override
	public LeoObject $add(int other) {
		return new LeoLong(other + this.number);
	}
	@Override
	public LeoObject $add(long other) {
		return new LeoLong(other + this.number);
	}

	/* (non-Javadoc)
	 * @see leola.types.LeoObject#sub(leola.types.LeoObject)
	 */
	@Override
	public LeoObject $sub(LeoObject other) {
		return other.$sub(this.number);
	}	
	@Override
	public LeoObject $sub(double other) {
		return LeoDouble.valueOf(other - this.number);
	}	
	@Override
	public LeoObject $sub(int other) {
		return new LeoLong(other - this.number);
	}
	@Override
	public LeoObject $sub(long other) {
		return new LeoLong(other - this.number);
	}
	
	/* (non-Javadoc)
	 * @see leola.types.LeoObject#mul(leola.types.LeoObject)
	 */
	@Override
	public LeoObject $mul(LeoObject other) {
		return other.$mul(this.number);
	}	
	@Override
	public LeoObject $mul(double other) {
		return LeoDouble.valueOf(other * this.number);
	}
	@Override
	public LeoObject $mul(int other) {
		return new LeoLong(other * this.number);
	}
	@Override
	public LeoObject $mul(long other) {
		return new LeoLong(other * this.number);
	}
	/* (non-Javadoc)
	 * @see leola.types.LeoObject#div(leola.types.LeoObject)
	 */
	@Override
	public LeoObject $div(LeoObject other) {
		return other.$div(this.number);
	}	
	@Override
	public LeoObject $div(double other) {
		if ( number == 0 ) {
			throw new LeolaRuntimeException("Divide by zero error");
		}
		return LeoDouble.valueOf(other / this.number);
	}
	@Override
	public LeoObject $div(int other) {
		if ( number == 0 ) {
			throw new LeolaRuntimeException("Divide by zero error");
		}
		return new LeoLong(other / this.number);
	}
	@Override
	public LeoObject $div(long other) {
		if ( number == 0 ) {
			throw new LeolaRuntimeException("Divide by zero error");
		}
		return new LeoLong(other / this.number);
	}
	
	/* (non-Javadoc)
	 * @see leola.types.LeoObject#mod(leola.types.LeoObject)
	 */
	@Override
	public LeoObject $mod(LeoObject other) {
		return other.$mod(this.number);
	}	
	@Override
	public LeoObject $mod(double other) {
		if ( number == 0 ) {
			throw new LeolaRuntimeException("Divide by zero error");
		}
		return LeoDouble.valueOf(other % this.number);
	}
	@Override
	public LeoObject $mod(int other) {
		if ( number == 0 ) {
			throw new LeolaRuntimeException("Divide by zero error");
		}
		return new LeoLong(other % this.number);
	}
	@Override
	public LeoObject $mod(long other) {
		if ( number == 0 ) {
			throw new LeolaRuntimeException("Divide by zero error");
		}
		return new LeoLong(other % this.number);
	}	

	/* (non-Javadoc)
	 * @see leola.types.LeoObject#neg()
	 */
	@Override
	public LeoObject $neg() {
		return new LeoLong(-this.number);
	}
	@Override
	public LeoObject $bnot() {
		LeoObject result = new LeoLong( ~this.number );				
		return result;
	}
	
	@Override
	public LeoObject $bsl(LeoObject other) {
		return other.$bsl(this.number);
	}
	@Override
	public LeoObject $bsl(double other) {
		return new LeoLong((int)other << this.number);
	}
	@Override
	public LeoObject $bsl(int other) {
		return new LeoLong(other << this.number);
	}
	@Override
	public LeoObject $bsl(long other) {
		return new LeoLong(other << this.number);
	}

	/* (non-Javadoc)
	 * @see leola.types.LeoObject#bsr(leola.types.LeoObject)
	 */
	@Override
	public LeoObject $bsr(LeoObject other) {
		return other.$bsr(this.number);
	}
	@Override
	public LeoObject $bsr(double other) {
		return new LeoLong((int)other >> this.number);
	}
	@Override
	public LeoObject $bsr(int other) {
		return new LeoLong(other >> this.number);
	}
	@Override
	public LeoObject $bsr(long other) {
		return new LeoLong(other >> this.number);
	}
	
	/* (non-Javadoc)
	 * @see leola.types.LeoObject#xor(leola.types.LeoObject)
	 */
	@Override
	public LeoObject $xor(LeoObject other) {	
		return other.$xor(this.number);
	}
	@Override
	public LeoObject $xor(double other) {
		return new LeoLong((int)other ^ this.number);
	}
	@Override
	public LeoObject $xor(int other) {
		return new LeoLong(other ^ this.number);
	}
	@Override
	public LeoObject $xor(long other) {
		return new LeoLong(other ^ this.number);
	}

	/* (non-Javadoc)
	 * @see leola.types.LeoObject#lor(leola.types.LeoObject)
	 */
	@Override
	public LeoObject $bor(LeoObject other) {
		return other.$bor(this.number);
	}
	@Override
	public LeoObject $bor(double other) {
		return new LeoLong((int)other | this.number);
	}
	@Override
	public LeoObject $bor(int other) {
		return new LeoLong(other | this.number);
	}
	@Override
	public LeoObject $bor(long other) {
		return new LeoLong(other | this.number);
	}

	/* (non-Javadoc)
	 * @see leola.types.LeoObject#land(leola.types.LeoObject)
	 */
	@Override
	public LeoObject $band(LeoObject other) {	
		return other.$band(this.number);
	}
	@Override
	public LeoObject $band(double other) {
		return new LeoLong((int)other & this.number);
	}
	@Override
	public LeoObject $band(int other) {
		return new LeoLong(other & this.number);
	}
	@Override
	public LeoObject $band(long other) {
		return new LeoLong(other & this.number);
	}
	
	@Override
	public void write(DataOutput out) throws IOException {
		out.write(this.getType().ordinal());
		out.writeLong(this.number);
	}
	
	/**
	 * Reads from the {@link DataInput} stream, constructing a {@link LeoObject}
	 * 
	 * @param in
	 * @return the {@link LeoObject}
	 * @throws IOException
	 */
	public static LeoLong read(DataInput in) throws IOException {
		long number = in.readLong();
		return new LeoLong(number);
	}
}

