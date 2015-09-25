/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.lang;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.List;

import leola.vm.ClassDefinitions;
import leola.vm.Leola;
import leola.vm.exceptions.LeolaRuntimeException;
import leola.vm.lib.LeolaIgnore;
import leola.vm.lib.LeolaLibrary;
import leola.vm.lib.LeolaMethod;
import leola.vm.lib.LeolaMethodVarargs;
import leola.vm.types.LeoArray;
import leola.vm.types.LeoClass;
import leola.vm.types.LeoClass.Metaclass;
import leola.vm.types.LeoNamespace;
import leola.vm.types.LeoNativeClass;
import leola.vm.types.LeoNativeFunction;
import leola.vm.types.LeoNull;
import leola.vm.types.LeoObject;
import leola.vm.types.LeoString;
import leola.vm.util.ArrayUtil;
import leola.vm.util.ClassUtil;

/**
 * The Reflection API
 *
 * @author Tony
 *
 */
public class ReflectionLeolaLibrary implements LeolaLibrary {

	/**
	 * The runtime
	 */
	private Leola runtime;
		
	/* (non-Javadoc)
	 * @see leola.frontend.LeolaLibrary#init(leola.frontend.Leola)
	 */
	@LeolaIgnore
	public void init(Leola runtime, LeoNamespace namespace) throws LeolaRuntimeException {
		this.runtime = runtime;	
		this.runtime.putIntoNamespace(this, namespace);
	}
	
	/**
	 * @param obj
	 * @return the type of {@link LeoObject} this is
	 */
	public String type(LeoObject obj) {
		return obj.getType().name();
	}
	
	/**
	 * Reflectively create a new instance of a class.
	 * 
	 * @param classname
	 * @param params
	 * @return Null if not found, the instance if instantiated
	 */
	@LeolaMethodVarargs
	public LeoObject newInstance(LeoObject classname, LeoObject ... params) {
		LeoObject result = LeoNull.LEONULL;
		ClassDefinitions defs = this.runtime.getGlobalNamespace().getScope().lookupClassDefinitions(classname);
		if( defs != null) {
			result = defs.newInstance(this.runtime, classname, params);
		}
		
		return result;
	}

	public LeoArray instrospectNames(Object obj) throws Exception {
		Class<?> aClass = obj.getClass();//obj.getValue().getClass();		
		LeoArray result = new LeoArray();
		
		List<Method> methods = ClassUtil.getAllDeclaredMethods(aClass);		
		for(Method m : methods) {			
			result.add(LeoString.valueOf(m.getName()));
		}
		
		List<Field> fields = ClassUtil.getAllDeclaredFields(aClass);
		for(Field m : fields) {
			result.add(LeoString.valueOf(m.getName()));
		}
		
		
		return result;
	}
	
	public LeoArray instrospect(Object obj) throws Exception {
		Class<?> aClass = obj.getClass();//obj.getValue().getClass();
		Object jObj = obj;
		LeoArray result = new LeoArray();
		
		List<Method> methods = ClassUtil.getAllDeclaredMethods(aClass);		
		for(Method m : methods) {
			m.setAccessible(true);
			
			boolean isStatic = (m.getModifiers() & Modifier.STATIC) != 0;
			if(isStatic) {
				result.add(new LeoNativeFunction(m, null));
			}
			else {
				result.add(new LeoNativeFunction(m, jObj));
			}
		}
		
		List<Field> fields = ClassUtil.getAllDeclaredFields(aClass);
		for(Field m : fields) {
			m.setAccessible(true);
			result.add(new LeoNativeClass(m.get(jObj)));
		}
		
		
		return result;
	}
	
	/**
	 * Retrieves the all static methods of the supplied class
	 * 
	 * @param className
	 * @param methodName
	 * @return a {@link LeoArray} or {@link LeoNativeFunction}'s that match the supplied methodName
	 * @throws Exception
	 */
	public LeoArray getStaticMethods(String className) throws Exception {
		Class<?> aClass = Class.forName(className);
		List<Method> methods = ClassUtil.getAllDeclaredMethods(aClass);
		LeoArray result = new LeoArray(methods.size());
		for(Method m : methods) {
			boolean isStatic = (m.getModifiers() & Modifier.STATIC) != 0;
			if(isStatic) {
				result.add(new LeoNativeFunction(m, null));
			}
		}
		return result;
	}
	
	/**
	 * Retrieves the all instance methods of the supplied class
	 * 
	 * @param className
	 * @param methodName
	 * @return a {@link LeoArray} or {@link LeoNativeFunction}'s that match the supplied methodName
	 * @throws Exception
	 */
	public LeoArray getStaticMethods(Object instance) throws Exception {
		Class<?> aClass = instance.getClass();
		List<Method> methods = ClassUtil.getAllDeclaredMethods(aClass);
		LeoArray result = new LeoArray(methods.size());
		for(Method m : methods) {
			boolean isStatic = (m.getModifiers() & Modifier.STATIC) != 0;
			if(!isStatic) {
				result.add(new LeoNativeFunction(m, instance));
			}
		}
		return result;
	}
	
	/**
	 * Retrieves the static methods of the supplied class
	 * 
	 * @param className
	 * @param methodName
	 * @return a {@link LeoArray} or {@link LeoNativeFunction}'s that match the supplied methodName
	 * @throws Exception
	 */
	public LeoArray getStaticMethodsByName(String className, String methodName) throws Exception {
		Class<?> aClass = Class.forName(className);
		List<Method> methods = ClassUtil.getMethodsByName(aClass, methodName);
		LeoArray result = new LeoArray(methods.size());
		for(Method m : methods) {
			boolean isStatic = (m.getModifiers() & Modifier.STATIC) != 0;
			if(isStatic) {
				result.add(new LeoNativeFunction(m, null));
			}
		}
		return result;
	}
	
	/**
	 * Retrieves the instance methods of the supplied object
	 * 
	 * @param className
	 * @param methodName
	 * @return a {@link LeoArray} or {@link LeoNativeFunction}'s that match the supplied methodName
	 * @throws Exception
	 */
	public LeoArray getInstanceMethodsByName(Object instance, String methodName) throws Exception {		
		List<Method> methods = ClassUtil.getMethodsByName(instance.getClass(), methodName);
		LeoArray result = new LeoArray(methods.size());				
		for(Method m : methods) {
			boolean isStatic = (m.getModifiers() & Modifier.STATIC) != 0;
			if(!isStatic) {
				result.add(new LeoNativeFunction(m, instance));
			}
		}
		return result;
	}
	
	/**
	 * Implements a Java Interface
	 * 
	 * @param aClass
	 * @param interfaceName
	 * @return
	 * @throws Exception
	 */
	@LeolaMethod(alias="implements")
	public LeoObject _implements(final String interfaceName, final LeoObject leoMethods) throws Exception {
		Class<?> jClass = Class.forName(interfaceName);
		Object obj = Proxy.newProxyInstance(ReflectionLeolaLibrary.class.getClassLoader(), new Class[] {jClass}, new InvocationHandler() {
			
			@Override
			public Object invoke(Object proxy, Method method, Object[] args)
					throws Throwable {
				
				LeoObject[] largs = ArrayUtil.EMPTY_LEOOBJECTS;
				if(args!=null && args.length > 0) {
					largs = new LeoObject[args.length];
					for(int i = 0; i < args.length; i++) {
						largs[i] = LeoObject.valueOf(args[i]);
					}
				}
				
				String methodName = method.getName();
				
				LeoObject leoMethod = leoMethods;
				try {
					leoMethod = leoMethods.getObject(LeoString.valueOf(methodName));
				}
				catch(Exception e) {}
				
				if(leoMethod == LeoNull.LEONULL) {
					if(methodName.equals("toString")) {
						return "Proxy for: " + interfaceName;
					}
					if(methodName.equals("equals")) {
						return proxy.equals(args[0]);
					}
					if(methodName.equals("hashCode")) {
						return proxy.hashCode();
					}
				}
				
				LeoObject res = runtime.execute(leoMethod, largs);
				return LeoObject.toJavaObject(method.getReturnType(), res);
			}
		});
		
		LeoNativeClass aClass = new LeoNativeClass(obj);
		/*
		List<Method> methods = ClassUtil.getAllDeclaredMethods(jClass);
		for(Method m : methods) {
			boolean isPublic= (m.getModifiers() & Modifier.PUBLIC) != 0;
			if(isPublic) {
				aClass.setMember(LeoString.valueOf(m.getName()), new LeoNativeFunction(m, obj));
			}
		}*/
		
		return aClass;
	}
	
	
	/**
	 * Clones the object
	 * @param obj
	 * @return
	 */
	public final LeoObject clone(LeoObject obj) {
		if(obj==null) {
			return LeoNull.LEONULL;
		}
		
		return obj.clone();
	}

	/**
	 * Calls the function and applies the array as function arguments to this function.
	 * 
	 * @param interpreter
	 * @param func
	 * @param params
	 * @return
	 */
	@LeolaMethodVarargs
	public final LeoObject call(LeoObject func, LeoObject ... params) {
		LeoObject result = func.call(params);		
		return result;
	}	
	
	/**
	 * Retrieves the {@link Metaclass} information from the supplied {@link LeoClass}
	 * 
	 * @param aClass
	 * @return the {@link Metaclass}
	 */
	public Metaclass getMetaclass(LeoObject aClass) {
	    if(aClass==null) return null;
	    
	    if(aClass.isClass()) {
	        LeoClass leoClass = aClass.as();
	        return new Metaclass(leoClass);
	    }
	    
	    throw new LeolaRuntimeException(aClass + " is not of LeoClass type.");
	}
}

