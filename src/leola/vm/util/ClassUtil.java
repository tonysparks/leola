/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import leola.frontend.EvalException;
import leola.vm.exceptions.LeolaRuntimeException;
import leola.vm.lib.LeolaIgnore;
import leola.vm.types.LeoNativeClass;
import leola.vm.types.LeoNull;
import leola.vm.types.LeoObject;

/**
 * Simple utility class for reflection operations
 *
 * @author Tony
 *
 */
public class ClassUtil {

	public static final Class<?>[] STRING = { String.class, char.class, Character.class };
	public static final Class<?>[] BOOLEAN = { boolean.class, Boolean.class };
	public static final Class<?>[] BYTE = { byte.class, Byte.class };
	public static final Class<?>[] CHAR = { char.class, Character.class };
	public static final Class<?>[] SHORT = { short.class, Short.class };
	public static final Class<?>[] INT = { int.class, Integer.class };
	public static final Class<?>[] LONG = { long.class, Long.class };
	public static final Class<?>[] FLOAT = { float.class, Float.class };
	public static final Class<?>[] DOUBLE = { double.class, Double.class };

	public static final Class<?>[] INTEGER =
	  { byte.class, Byte.class
	  , short.class, Short.class
	  , int.class, Integer.class
//	  , float.class, Float.class
//	  , double.class, Double.class
//	  , long.class, Long.class
//	  , Number.class
	  };

	public static final Class<?>[] NUMBER =
		  { byte.class, Byte.class
		  , short.class, Short.class
		  , int.class, Integer.class
		  , float.class, Float.class
		  , double.class, Double.class
		  , long.class, Long.class
		  , Number.class };

	private static void rethrow(Exception e) throws Exception {
		Throwable cause = e.getCause();
		if( cause instanceof LeolaRuntimeException) {
			throw (LeolaRuntimeException)cause;
		}

		throw new Exception(e.getCause());
	}

	/**
	 * Retrieves a method by name (grabs the first if overloaded).
	 *
	 * @param methodName
	 * @return the method if found, otherwise null
	 */
	public static Method getMethodByName(Class<?> aClass, String methodName, Class<?> ... params) {
		Method result = null;
		try {
			result = aClass.getMethod(methodName, params);
		}
		catch(Exception e) {
		}

		return (result);
	}

	/**
	 * @param aClass
	 * @param methodName
	 * @return all methods by the given name.
	 */
	public static List<Method> getMethodsByName(Class<?> aClass, String methodName) {
		List<Method> result = new ArrayList<Method>();
		try {
			List<Method> superResult = getAllDeclaredMethods(aClass);
			for(int i = 0; i < superResult.size(); i++) {
				Method m = superResult.get(i);
				if ( m.getName().equals(methodName)) {
					result.add(m);
				}
			}
		}
		catch(Exception e) {
		}

		return result;
	}

	public static Object invokeMethod(Method method, Object owner, LeoObject[] params) throws Exception {
		Object result = null;
		try {
			method.setAccessible(true);
			result = tryMethod2(owner, method, params);
		}
		catch(InvocationTargetException e) {
			/* This was a legitimate method invokation, so
			 * lets bomb out
			 */
			rethrow(e);
		}
		catch(Throwable e) {
			/* try other methods */
			// System.out.println(e);
		}

		return result;
	}

	/**
	 * Invokes a method reflectively.
	 *
	 * @param method
	 * @param owner
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static Object invokeMethod(Method method, Object owner, Object[] params) throws Exception {
		Object result = null;

		try {
			method.setAccessible(true);
			result = method.invoke(owner, params);
		}
		catch(InvocationTargetException e) {
			/* This was a legitimate method invokation, so
			 * lets bomb out
			 */
			rethrow(e);
		}
		catch(Throwable e) {
			/* try other methods */
			// System.out.println(e);
		}

		return result;
	}

	/**
	 * @param aClass
	 * @param fieldName
	 * @return
	 */
//	public static LeoObject getFieldValue(LeoNativeClass aClass, String fieldName) {
//		LeoObject result = null;
//		try {
//			Class<?> ownerClass = aClass.getNativeClass();
//			Field field = ownerClass.getField(fieldName);
//			Object javaObj = field.get(aClass.getInstance());
//			result = LeoTypeConverter.convertToLeolaType(javaObj);
//		} catch (Exception e) {
//		}
//
//		return result;
//	}

	/**
	 * @param aClass
	 * @param fieldName
	 * @return
	 */
	public static Object getFieldValue(Object instance, String fieldName) {
		Object result = null;
		try {
			Class<?> aClass = instance.getClass();
			Field field = aClass.getField(fieldName);
			result = field.get(instance);
		} catch (Exception e) {
		}

		return result;
	}

	/**
	 * Gets a static member
	 * @param aClass
	 * @param fieldName
	 * @return
	 */
	public static LeoObject getStaticFieldValue(Class<?> aClass, String fieldName) {
		LeoObject result = null;
		try {
			Field field = aClass.getField(fieldName);
			Object javaObj = field.get(null);

			result = LeoTypeConverter.convertToLeolaType(javaObj);
		} catch (Exception e) {
		}

		return result;
	}

	/**
	 * Finds the best possible method. If none is found suitable an exception is thrown.
	 *
	 * @param aClass
	 * @param methodName
	 * @param paramTypes
	 * @return a {@link Pair}
	 * @throws Exception
	 */
	public static Pair<Method, Object> invokeMethod(Class<?> aClass, String methodName, Object owner, Object[] params) throws Exception {
		Pair<Method, Object> result = null;
		boolean methodFound = false;

		List<Method> methods = getAllDeclaredMethods(aClass);

		for(Method m: methods) {
			if(m.getName().equals(methodName)) {
				try {
					m.setAccessible(true);
					Object methodResult = m.invoke(owner, params);
					result = new Pair<Method, Object>(m, methodResult);
					methodFound = true;
					break;
				}
				catch(InvocationTargetException e) {
					/* This was a legitimate method invokation, so
					 * lets bomb out
					 */
					rethrow(e);
				}
				catch(Throwable e) {
					/* try other methods */
					// System.out.println(e);
				}
			}
		}

		if ( ! methodFound ) {
			throw new LeolaRuntimeException("No method defined for: " + owner + " methodName: " + methodName);
		}

		return result;

	}

	/**
	 * Determines if the supplied class name is a native class.
	 *
	 * @param className
	 * @return
	 */
	public static boolean isNativeClass(String className) {
		boolean result = false;
		try {
			Class.forName(className);
			result = true;
		}
		catch(Throwable e) {}

		return result;
	}

	/**
	 * Finds the best possible method. If none is found suitable an exception is thrown.
	 *
	 * @param aClass
	 * @param methodName
	 * @param paramTypes
	 * @return a {@link Pair}
	 * @throws Exception
	 */
	public static Pair<Method, Object> invokeMethod(Class<?> aClass, String methodName, Object owner, LeoObject[] params) throws Exception {
		Pair<Method, Object> result = null;
		boolean methodFound = false;

		List<Method> methods = getAllDeclaredMethods(aClass);
		for(Method m: methods) {
			if(m.getName().equals(methodName)) {
				try {
					m.setAccessible(true);
					Object methodResult = tryMethod(owner, m, params);
					result = new Pair<Method, Object>(m, methodResult);
					methodFound = true;
					break;
				}
				catch(InvocationTargetException e) {
					/* This was a legitimate method invokation, so
					 * lets bomb out
					 */
					rethrow(e);
				}
				catch(Throwable e) {
					/* try other methods */
				}
			}
		}

		if ( ! methodFound ) {
			throw new LeolaRuntimeException("No method defined for: " + owner + " methodName: " + methodName);
		}

		return result;

	}

	/**
	 * Attempts to instantiate the object
	 * @param constructor
	 * @param params
	 * @param paramTypes
	 * @return
	 */
	private static Object tryMethod2(Object owner, Method method, LeoObject[] params)
		throws InvocationTargetException, Exception {

		Class<?>[] paramTypes = method.getParameterTypes();
		Object[] args = new Object[paramTypes.length];
		for(int i = 0; i < paramTypes.length; i++ ) {
			Class<?> aCl = paramTypes[i];

			/* Leola allows for missing arguments */
			LeoObject arg = LeoNull.LEONULL;
			if ( params != null && i < params.length ) {
				arg = params[i];
			}

			args[i] = LeoTypeConverter.convertLeoObjectToJavaObj(aCl, arg);
		}

		Object result = method.invoke(owner, args);


		return (result);
	}

	/**
	 * Attempts to instantiate the object
	 * @param constructor
	 * @param params
	 * @param paramTypes
	 * @return
	 */
	private static Object tryMethod(Object owner, Method method, LeoObject[] params)
		throws InvocationTargetException, Exception {

		Class<?>[] paramTypes = method.getParameterTypes();
//		if ( (params==null&&paramTypes.length!=0)
//			|| (params!=null && paramTypes.length != params.length) ) {
//			throw new Exception();
//		}

		Object[] args = new Object[paramTypes.length];
		for(int i = 0; i < paramTypes.length; i++ ) {
			Class<?> aCl = paramTypes[i];

			/* Leola allows for missing arguments */
			LeoObject arg = LeoNull.LEONULL;
			if (params != null && i < params.length ) {
				arg = params[i];
			}

			args[i] = LeoTypeConverter.convertLeoObjectToJavaObj(aCl, arg);
		}

		Object result = method.invoke(owner, args);


		return (result);
	}

	/**
	 * Is of type
	 */
	public static boolean isType(Class<?> type, Class<?> ...classes ) {
		boolean result = false;
		for(Class<?> c: classes) {
			result = result || type.equals(c);
			if ( result ) {
				break;
			}
		}
		return result;
	}

	/**
	 * Determines if the supplied child class inherits from the supplied parent class.
	 * @param child
	 * @param parent
	 * @return true if child inherits from parent, false otherwise
	 */
	public static boolean inheritsFrom(Class<?> child, Class<?> parent) {
		for(Class<?> a = child; a != null; a = a.getSuperclass()) {
			if ( a.equals(parent) ) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Determines if the supplied testMe class implements the supplied interface.
	 *
	 * @param testMe
	 * @param aInterface
	 * @return true if the testMe class implements aInterface, false otherwise.
	 */
	public static boolean doesImplement(Class<?> testMe, Class<?> aInterface) {
		for(Class<?> inter : testMe.getInterfaces()) {
			if ( inter.equals(aInterface) ) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Gets all the methods even from the parent class.
	 * @param aClass
	 * @return
	 */
	public static List<Method> getAllDeclaredMethods(Class<?> aClass) {
		List<Method> methods = new ArrayList<Method>();
		try {
			for(Class<?> a = aClass; a != null && !a.equals(Object.class);
			    a = a.getSuperclass()) {

				for(Method m : a.getDeclaredMethods()) {
					/* Only grab public and non-ignored methods */
					if( (m.getModifiers() & Modifier.PUBLIC) > 0 &&
						 ! m.isAnnotationPresent(LeolaIgnore.class) ) {
						methods.add(m);
					}
				}
			}
		}
		catch(Exception e) {
			/* ignore */
		}

		return (methods);
	}

	/**
	 * Gets all the fields even from the parent class.
	 * @param aClass
	 * @return
	 */
	public static List<Field> getAllDeclaredFields(Class<?> aClass) {
		List<Field> methods = new ArrayList<Field>();
		try {
			for(Class<?> a = aClass; a != null && !a.getClass().equals(Object.class);
			    a = a.getSuperclass()) {

				for(Field m : a.getDeclaredFields()) {
					/* Don't grab private members */
					if( (m.getModifiers() & Modifier.PUBLIC) > 0 &&
					    ! m.isAnnotationPresent(LeolaIgnore.class) ) {
						methods.add(m);
					}
				}
			}
		}
		catch(Exception e) {
			/* ignore */
		}

		return (methods);
	}

	/**
	 * Get the inherited method.
	 * @param aClass
	 * @param method
	 * @return
	 */
	public static Method getInheritedMethod(Class<?> aClass, Method method) {
		Method inheritedMethod = null;
		try {
			for(Class<?> a = aClass; a != null; a = a.getSuperclass()) {
				inheritedMethod = aClass.getMethod(method.getName(), method.getParameterTypes());
				if ( inheritedMethod != null ) {
					return inheritedMethod;
				}
			}
		}
		catch (Exception e) {
			/* Ignore */
		}

		return null;
	}

	public static Field getInheritedField(Class<?> aClass, String fieldName) {
		for(Class<?> i = aClass; i != Object.class; i = aClass.getSuperclass()) {
			try {
				Field f = i.getField(fieldName);
				if(f!=null) {
					f.setAccessible(true);
					return f;
				}
			} catch (Exception e) {}
		}
		return null;
	}

	/**
	 * Retrieves the annotation from the method, if it is not on the method, the parent
	 * is checked.  This solves the "inherited annotation" problem for interfaces.
	 * @param ownerClass
	 * @param method
	 * @return
	 */
	public static <T extends Annotation> T getAnnotation(Class<T> annotation, Class<?> ownerClass, Method method) {
		if ( method == null ) {
			return null;
		}

		if ( method.isAnnotationPresent(annotation) ) {
			return method.getAnnotation(annotation);
		}
		else {
			for(Class<?> aInterface : ownerClass.getInterfaces()) {
				Method inheritedMethod = getInheritedMethod(aInterface, method);
				if ( inheritedMethod != null ) {
					T result = getAnnotation(annotation, aInterface, inheritedMethod);
					if ( result != null ) {
						return result;
					}
				}
			}

			/* Query the parent class for the annotation */
			Class<?> superClass = method.getDeclaringClass().getSuperclass();
			Method inheritedMethod = getInheritedMethod(superClass, method);

			return getAnnotation(annotation, superClass, inheritedMethod);
		}
	}


	/**
	 * Attempts to retrieve the best possible constructor match.
	 * @param aClass
	 * @param args
	 * @return
	 */
	public static Constructor<?> getBestConstructor(Class<?> aClass, Object[] constructorArgs) {
		int score = 0;
		Constructor<?> bestMatch = null;
		Object[] args = (constructorArgs == null) ? new Object[0] : constructorArgs;

		Constructor<?>[] consts = aClass.getConstructors();
		for(Constructor<?> c: consts) {
			Class<?>[] cargs = c.getParameterTypes();
			if ( cargs.length == args.length ) {
				int currentScore = 1;
				for(int i = 0; i < cargs.length; i++ ) {
					Object value = args[i];
					Class<?> aCl = cargs[i];

					if ( value != null ) {
						if ( value instanceof LeoObject ) {
							try {
								Object jObject = LeoTypeConverter.convertLeoObjectToJavaObj(aCl, (LeoObject)value);
								if ( jObject.getClass().equals(aCl) ) {
									currentScore++;
								}
							}
							catch(Exception e) {
							}
						}
						else if ( value.getClass().equals(cargs[i]) ) {
							currentScore++;
						}
						else {
							currentScore--;
						}
					}
				}

				if (currentScore > score) {
					bestMatch = c;
					score = currentScore;
				}
			}
		}

		return bestMatch;
	}


	/**
	 * Creates a new Native class.
	 *
	 * @param className
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static LeoNativeClass newNativeInstance(String className, LeoObject ... params) {
		LeoNativeClass result = null;

		if ( params == null ) {
			params = new LeoObject[0];
		}

		try {

			Class<?> nativeClass = Class.forName(className);
			if ( Modifier.isAbstract(nativeClass.getModifiers()) ) {

			}
			else {
				Object instance = null;
				for(Constructor<?> constructor : nativeClass.getConstructors()) {
					Class<?>[] paramTypes = constructor.getParameterTypes();
					if ( paramTypes.length == params.length) {
						instance = tryNativeConstructor(constructor, params, paramTypes);
						if ( instance != null ) {
							break;
						}
					}
				}

				if ( instance != null ) {
					result = new LeoNativeClass(nativeClass, instance);
				}
			}

		}
		catch(Throwable e) {
			throw new EvalException("Unable to construct native type: " + className);
		}

		if (result == null ) {
			throw new EvalException("Unable to construct native type: " + className);
		}

		return result;
	}

	/**
	 * Attempts to instantiate the object
	 * @param constructor
	 * @param params
	 * @param paramTypes
	 * @return
	 */
	private static Object tryNativeConstructor(Constructor<?> constructor, LeoObject[] params, Class<?>[] paramTypes) {
		Object result = null;
		try {
			Object[] args = new Object[paramTypes.length];
			for(int i = 0; i < paramTypes.length; i++ ) {
				Class<?> aCl = paramTypes[i];
				args[i] = LeoTypeConverter.convertLeoObjectToJavaObj(aCl, params[i]);
			}

			result = constructor.newInstance(args);
		}
		catch(Throwable e) {
		}

		return (result);
	}
}

