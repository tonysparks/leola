/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.types;

import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import leola.vm.exceptions.LeolaRuntimeException;
import leola.vm.util.ClassUtil;




/**
 * Refers to a Java Class
 *
 * @author Tony
 *
 */
public class LeoNativeClass extends LeoObject {

	/**
	 * Class name
	 */
	private Class<?> nativeClass;

	/**
	 * The instance of the native class
	 */
	private Object instance;
	
	   /**
     * Adds ability to reference the public API of this class
     */
    private Map<LeoObject, LeoObject> arrayApi; 
    private LeoObject getNativeMember(LeoObject key) {
        if(this.arrayApi == null) {
            this.arrayApi = new LeoMap();
        }
        return getNativeMember(this.nativeClass, this.instance, this.arrayApi, key);
    }
	
	/**
	 */
	public LeoNativeClass() {
		this(null, null);
	}

	/**
	 * @param instance
	 */
	public LeoNativeClass(Object instance) {
		this(instance.getClass(), instance);
	}

	/**
	 * @param nativeClass
	 * @param instance
	 */
	public LeoNativeClass( Class<?> nativeClass, Object instance) {
		super(LeoType.NATIVE_CLASS);
		this.nativeClass = nativeClass;
		this.instance = instance;
	}

	/* (non-Javadoc)
	 * @see leola.types.LeoObject#toString()
	 */
	@Override
	public String toString() {
		return this.instance.toString();
	}

	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#isOfType(java.lang.String)
	 */
	@Override
	public boolean isOfType(String rawType) {
		return is(rawType);
	}
	/**
	 * @param className
	 * @return true if the supplied className is of this type
	 */
	public boolean is(String className) {
		boolean result = false;
		try {
			Class<?> cls = Class.forName(className);
			Class<?> currentClass = this.nativeClass;
			while(!result && currentClass != null) {
				result = cls.getName().equals(currentClass.getName());
				currentClass = currentClass.getSuperclass();
			}
		}
		catch(Throwable t) {
		}

		return result;
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#isClass()
	 */
	@Override
	public boolean isClass() {
	    return true;
	}

	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#isNativeClass()
	 */
	@Override
	public boolean isNativeClass() {
	    return true;
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#setObject(leola.vm.types.LeoObject, leola.vm.types.LeoObject)
	 */
	@Override
	public void setObject(LeoObject key, LeoObject value) {
		setMember(key, value);
	}

	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#getObject(leola.vm.types.LeoObject)
	 */
	@Override
	public LeoObject getObject(LeoObject key) {
		return getMember(key);
	}

	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#hasObject(leola.vm.types.LeoObject)
	 */
	@Override
	public boolean hasObject(LeoObject key) {	 
	    return getMember(key) != null;
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#$sindex(leola.vm.types.LeoObject, leola.vm.types.LeoObject)
	 */
	@Override
	public void $sindex(LeoObject key, LeoObject other) {
	    Method method = ClassUtil.getMethodByAnnotationAlias(nativeClass, "$sindex");
	    if(method!=null) {
	        try {
                ClassUtil.invokeMethod(method, instance, new Object[] {key.getValue(), other.getValue()});
            }
            catch (Exception e) {
                throw new LeolaRuntimeException(e);
            }
	    }
	    else {
	        super.$sindex(key, other);
	    }
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#$index(leola.vm.types.LeoObject)
	 */
	@Override
	public LeoObject $index(LeoObject other) {
	    Method method = ClassUtil.getMethodByAnnotationAlias(nativeClass, "$index");
        if(method!=null) {
            try {
                return LeoObject.valueOf( ClassUtil.invokeMethod(method, instance, new Object[] {other.getValue()}) );
            }
            catch (Exception e) {
                throw new LeolaRuntimeException(e);
            }
        }        
        return super.$index(other);        
	}
	
	/**
	 * Attempt to retrieve a native member of the supplied Java class.  This will first check
	 * the public methods, short of that, it will then check the public data members.
	 * 
	 * @param member
	 * @return the member if found.
	 */
	public LeoObject getMember(LeoObject member) {
		LeoObject result = getNativeMember(member);
		return result;
	}

	/**
	 * Attempts to set the Java objects field.  If it isn't found or fails to set
	 * the field, an exception is thrown.
	 * 
	 * @param member
	 * @param value
	 */
	public void setMember(LeoObject member, LeoObject value) {
		String memberName = member.toString();
		
		Field field = getField(memberName);
		if ( field != null ) {
			try {
				field.set(getInstance(), value.getValue(field.getType()));
			}
			catch(Exception e) {
			    throwAttributeAccessError(nativeClass, member);
			}
		}
		else {
		    throwAttributeError(nativeClass, member);
		}
	}

	/**
	 * @return the nativeClass
	 */
	public Class<?> getNativeClass() {
		return nativeClass;
	}


	/**
	 * @param fieldName
	 * @return returns the field if found, if not found null
	 */
	public Field getField(String fieldName) {
		Field field = ClassUtil.getInheritedField(nativeClass, fieldName);
		return field;
	}

	/**
	 * @param methodName
	 * @return all methods defined by the supplied methodName
	 */
	public List<Method> getMethods(String methodName) {
		List<Method> meth = ClassUtil.getMethodsByName(nativeClass, methodName);
		return meth;
	}

	/**
	 * @return the instance
	 */
	public Object getInstance() {
		return instance;
	}

	/**
	 * @param instance the instance to set
	 */
	public void setInstance(Object instance) {
		this.instance = instance;
	}


	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#add(leola.vm.types.LeoObject)
	 */
	@Override
	public LeoObject $add(LeoObject other) {
		if (other.isString()) {
			return LeoString.valueOf(toString() + other.toString());
		}
		return super.$add(other);
	}

	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#$req(leola.vm.types.LeoObject)
	 */
	@Override
	public boolean $req(LeoObject other) {	
		return this.instance == other.getValue();
	}
	
	
	/* (non-Javadoc)
	 * @see leola.types.LeoObject#eq(leola.types.LeoObject)
	 */
	@Override
	public boolean $eq(LeoObject other) {
		if ( other != null && other.isOfType(LeoType.NATIVE_CLASS)) {
			LeoNativeClass otherClass = other.as();
			return this.instance.equals(otherClass.instance);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
	    return this.instance.hashCode();
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
	    if(obj instanceof LeoObject) {
	        return $eq((LeoObject)obj);
	    }
	    
	    return this.instance.equals(obj);
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
		return this.instance;
	}

	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#getValue(java.lang.Class)
	 */
	@Override
	public Object getValue(Class<?> narrowType) {
		if(LeoObject.class.equals(narrowType)) {
			return this;
		}
		
		return narrowType.isInstance(this.instance) ? narrowType.cast(this.instance) : this.instance;
	}

	/* (non-Javadoc)
	 * @see leola.types.LeoObject#clone()
	 */
	@Override
	public LeoObject clone() {
		LeoNativeClass nClass = new LeoNativeClass(this.nativeClass, this.instance);
		return nClass;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.write(this.getType().ordinal());
		out.writeChars(this.nativeClass.getName());
	}
}

