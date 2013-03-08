/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.util;

import static leola.vm.util.ClassUtil.BOOLEAN;
import static leola.vm.util.ClassUtil.INTEGER;
import static leola.vm.util.ClassUtil.LONG;
import static leola.vm.util.ClassUtil.NUMBER;
import static leola.vm.util.ClassUtil.STRING;

import java.lang.reflect.Array;

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

	/**
	 * Converts the supplied java object into a {@link LeoObject}.
	 *
	 * @param javaObj
	 * @return
	 * @throws LeolaRuntimeException
	 */
	public static LeoObject convertToLeolaType(Object javaObj) /*throws EvalException*/ {
		LeoObject result = null;
		if ( javaObj == null ) {
			result = LeoNull.LEONULL;
		}
		else {
			Class<?> type = javaObj.getClass();
			if ( ClassUtil.isType(type, STRING) ) {
				result = new LeoString(javaObj.toString());
			}
			else if ( ClassUtil.isType(type, BOOLEAN) ){
				result = ((Boolean)javaObj) ? LeoBoolean.LEOTRUE
											: LeoBoolean.LEOFALSE;
			}
			else if ( ClassUtil.isType(type, LONG)) {
				result = new LeoLong(((Number)javaObj).longValue());
			}
			else if ( ClassUtil.isType(type, INTEGER)) {
				result = LeoInteger.valueOf(((Number)javaObj).intValue());
			}
			else if ( ClassUtil.isType(type, NUMBER) ){

				Double number = new Double( ((Number)javaObj).doubleValue() );
				result = LeoDouble.valueOf(number);
			}
			else if ( ClassUtil.inheritsFrom(type, LeoObject.class)) {
				result = (LeoObject)javaObj;
			}
			else if ( type.isArray() ) {
				int len = Array.getLength(javaObj);
				LeoArray array = new LeoArray(len);
				for(int i = 0; i < len; i++) {
					Object obj = Array.get(javaObj, i);
					array.$add(convertToLeolaType(obj));
				}

				result = array;
			}
			else {
				result = new LeoNativeClass(type, javaObj);
			}
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

