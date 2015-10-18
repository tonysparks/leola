/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import leola.vm.exceptions.LeolaRuntimeException;
import leola.vm.lib.LeolaIgnore;
import leola.vm.lib.LeolaMethod;
import leola.vm.lib.LeolaMethodVarargs;
import leola.vm.types.LeoNativeClass;
import leola.vm.types.LeoNull;
import leola.vm.types.LeoObject;
import leola.vm.types.LeoString;

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

	
	/**
	 * Re-throws the supplied exception as a {@link LeolaRuntimeException}.  This will
	 * first check to see if the supplied exception is a {@link LeolaRuntimeException}, in which
	 * case it will cast a throw.
	 *  
	 * @param e
	 * @throws LeolaRuntimeException
	 */
	private static void rethrow(Exception e) throws LeolaRuntimeException {
		Throwable cause = e.getCause();
		
		/* There are cases where 'cause' is null, which
		 * is pretty lame
		 */
		if(cause != null) {
    		if( cause instanceof LeolaRuntimeException) {
    			throw (LeolaRuntimeException)cause;
    		}
    		throw new LeolaRuntimeException(e.getCause());
		}
		else {
		    if(e instanceof LeolaRuntimeException) {
		        throw (LeolaRuntimeException)e;
		    }
		    throw new LeolaRuntimeException(e);
		}
	}
	
	
	/**
	 * Return instructions for the {@link MethodIterator}
	 * 
	 * @author Tony
	 *
	 */
	private static enum ReturnType {
	    STOP_METHOD_LOOP,
	    STOP_HIERARCHY_LOOP,	    
	    DONT_STOP
	    ;
	    
	    public boolean isStop() {
	        return !this.equals(DONT_STOP);
	    }
	}
	
	
	/**
	 * Iterate through {@link Method} callback.
	 * 
	 * @author Tony
	 *
	 */
	private static interface MethodIterator {
	    public ReturnType call(Method method) throws LeolaRuntimeException;
	}
		
	/**
	 * Iterates over a class hierarchy invoking the supplied callback function.
	 *  
	 * @param aClass the class to iterate over
	 * @param it the callback function.  If the {@link MethodIterator} returns <code>true</code>, then the
	 * iteration stops.
	 */
    private static void iterateOverHierarchy(Class<?> aClass, MethodIterator it) {
        try {
            ReturnType returnType = ReturnType.DONT_STOP;            
            for (Class<?> i = aClass; i != null && i != Object.class; i = i.getSuperclass()) {

                for (Method method : i.getDeclaredMethods()) {
                    
                    /* Only grab public and non-ignored methods */
                    if ((method.getModifiers() & Modifier.PUBLIC) > 0 && 
                        !method.isAnnotationPresent(LeolaIgnore.class)) {

                        returnType = it.call(method);
                        if(returnType.isStop()) {
                            break;
                        }
                    }
                }
                
                if(returnType.equals(ReturnType.STOP_HIERARCHY_LOOP)) {
                    break;
                }
            }
        }
        catch (Exception e) {
        }
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
	 * Retrieves all the overloaded methods by the supplied name
	 * 
	 * @param aClass
	 * @param methodName
	 * @return all methods by the given name.
	 */
	public static List<Method> getMethodsByName(final Class<?> aClass, final String methodName) {
		final List<Method> methods = new ArrayList<Method>();
	    iterateOverHierarchy(aClass, new MethodIterator() {
            
            @Override
            public ReturnType call(Method method) {
                if(method.getName().equals(methodName)) {
                    methods.add(method);
                }
                
                return methods.isEmpty() ? ReturnType.DONT_STOP : 
                    ReturnType.STOP_HIERARCHY_LOOP;
            }
        });
	    
	    return methods;
	}
	
	/**
	 * Retrieves the method names in which have the {@link LeolaMethod} annotation with the {@link LeolaMethod#alias()} of
	 * the supplied 'methodName'
	 * 
     * @param aClass
     * @param methodName
     * @return all methods that have a {@link LeolaMethod} annotation with the matching alias name
     */
    public static List<Method> getMethodsByAnnotationAlias(final Class<?> aClass, final String methodName) {
        final List<Method> methods = new ArrayList<Method>();
        iterateOverHierarchy(aClass, new MethodIterator() {
            
            @Override
            public ReturnType call(Method method) {
                if ( method.isAnnotationPresent(LeolaMethod.class) ) {
                    LeolaMethod methodAlias = method.getAnnotation(LeolaMethod.class);
                    if(methodAlias.alias().equals(methodName)) {
                        methods.add(method);
                    }
                }
                
                return methods.isEmpty() ? ReturnType.DONT_STOP : 
                    ReturnType.STOP_HIERARCHY_LOOP;
            }
        });
        
        return methods;
    }
	
    
    /**
     * Retrieves a {@link Method} by looking at the {@link LeolaMethod} annotation's {@link LeolaMethod#alias()}
     * value.
     * 
     * @param aClass
     * @param methodName
     * @return the {@link Method} if found, otherwise <code>null</code>
     */
    public static Method getMethodByAnnotationAlias(final Class<?> aClass, final String methodName) {    
        final AtomicReference<Method> methodRef = new AtomicReference<Method>();
        iterateOverHierarchy(aClass, new MethodIterator() {
            
            @Override
            public ReturnType call(Method method) {
                if ( method.isAnnotationPresent(LeolaMethod.class) ) {
                    LeolaMethod methodAlias = method.getAnnotation(LeolaMethod.class);
                    if(methodAlias.alias().equals(methodName)) {
                        methodRef.set(method);
                        return ReturnType.STOP_HIERARCHY_LOOP;
                    }
                }
                
                return ReturnType.DONT_STOP;
            }
        });
        
        return methodRef.get();        
    }
    
    /**
     * Attempts to find the best matching overloaded {@link Method}.  If one is found, it executes the
     * {@link Method}.
     * 
     * @param overloads
     * @param instance
     * @param args
     * @return the resulting Java object from the method execution.
     */
    public static Object invokeMethod(List<Method> overloads, Object instance, LeoObject[] args) {
        Method bestMatch = null;
        int bestScore = -1;
        
        /* The common case is one method, so
         * let's optimize for it
         */
        if(overloads.size() == 1) {
            bestMatch = overloads.get(0);
        }
        else {
            
            /* Look for the best possible score.  The score
             * is calculated by the following logic:
             * 
             * 1) give a +1 for parameters that match in type
             * 2) give a -1 for parameters that do not match in type
             */
            for(int i = 0; i < overloads.size(); i++) {
                Method method = overloads.get(i);
                Class<?>[] types = method.getParameterTypes();
                
                if(args!=null) {
                    int currentScore = 0;                    
                    for(int j = 0; j < types.length; j++) {
                        for(int k = 0; k < args.length; k++) {
                            Object jObject = args[k].getValue();
                            if(jObject!=null) {
                                if(jObject.getClass().isAssignableFrom(types[j])) {
                                    currentScore+=2;
                                }                                
                            }
                            currentScore--;
                        }
                    }
                    
                    if(bestScore < currentScore) {
                        bestScore = currentScore;
                        bestMatch = method;
                    }                    
                }
                else {
                    
                    /* If no arguments were passed in, the Java method
                     * with the highest score should be the one with the
                     * least amount of parameters
                     */
                    if(bestScore < 0 || types.length < bestScore) {
                        bestScore = types.length;
                        bestMatch = method;
                    }                    
                }
            }
        }
        
        Object result = invokeMethod(bestMatch, instance, args);        
        return result;
    }
    
    
    /**
     * Invokes the supplied method.
     * 
     * @param method the method to be executed
     * @param owner the instance owner of the method
     * @param params the parameters to be used
     * @return the result of executing the method
     * @throws LeolaRuntimeException
     */
	public static Object invokeMethod(Method method, Object owner, LeoObject[] params) throws LeolaRuntimeException  {
		Object result = null;
		
		try {
			method.setAccessible(true);
			result = tryMethod(owner, method, params);
		}
		catch (InvocationTargetException e) {
            rethrow(e);
        }
        catch (Exception e) {
            LeoObject.throwNativeMethodError("Error executing Java method '" + method.getName() + "'");
        }

		return result;
	}

	/**
	 * Invokes a method reflectively.
	 *
	 * @param method the method to be executed
     * @param owner the instance owner of the method
     * @param params the parameters to be used
     * @return the result of executing the method
	 * @throws LeolaRuntimeException
	 */
	public static Object invokeMethod(Method method, Object owner, Object[] params) throws LeolaRuntimeException {
		Object result = null;

		try {
			method.setAccessible(true);
			result = method.invoke(owner, params);
		}
		catch(Exception e) {
			/* This was a legitimate method invocation, so
			 * lets bomb out
			 */
			rethrow(e);
		}

		return result;
	}
	

	/**
	 * Attempts to invoke the supplied method.
	 * 
	 * @param owner the instance owner of the method (may be null)
	 * @param method the method to be executed
	 * @param params the method parameters
	 * @return the result of the method execution
	 */
	private static Object tryMethod(Object owner, Method method, LeoObject[] params)
		throws InvocationTargetException, Exception {

		Class<?>[] paramTypes = method.getParameterTypes();
		
		Object[] args = new Object[paramTypes.length];
        Object varargs = null;
		
        Class<?> arrayType = null;
        
		int startOfVarargs = -1;
		boolean hasVarArgs = false;
		
		/* Determine if this Java method has variable arguments;
		 * if it does, we need to do some special handling of the arguments
		 * (convert the parameters into an array) 
		 */
        if (method.isAnnotationPresent(LeolaMethodVarargs.class) || method.isVarArgs()) {
            startOfVarargs = paramTypes.length - 1;
            hasVarArgs = true;

            if (startOfVarargs < params.length && startOfVarargs < paramTypes.length) {
                arrayType = paramTypes[startOfVarargs].getComponentType();

                int varargSize = params.length - startOfVarargs;
                varargs = Array.newInstance(arrayType, varargSize);
                args[startOfVarargs] = varargs;
            }
        }

		/* We attempt to convert the supplied LeoObject parameters to
		 * Java parameters.
		 */
		for(int i = 0; i < paramTypes.length; i++ ) {
			 		    
		    /* If we have variable arguments, ensure we convert the
		     * array elements too
		     */
			if(hasVarArgs && i>=startOfVarargs && params!=null) {
			    int varargsIndex = 0;
			    			    
			    for(int paramIndex = startOfVarargs; paramIndex <  params.length; paramIndex++) {
			        Object javaArg = LeoObject.toJavaObject(arrayType, params[paramIndex]);
			        Array.set(varargs, varargsIndex++, javaArg);
			    }
			    break;
			}
			else {
			    /* Attempt to coerce the the LeoObject parameter
			     * into the appropriate Java object parameter
			     */
			    
			    Class<?> aCl = paramTypes[i];
			    
	            /* Leola allows for missing arguments, so
	             * if it is missing, just use null 
	             */
	            LeoObject arg = LeoNull.LEONULL;
	            if (params != null && i < params.length ) {
	                arg = params[i];
	            }
			    
			    Object javaArg = LeoObject.toJavaObject(aCl, arg);
			    args[i] = javaArg;
			}
		}

		Object result = method.invoke(owner, args);
		return (result);
	}


    /**
     * Retrieve a data members value.
     * 
     * @param instance the instance of a class
     * @param fieldName the data member name, in which to retrieve its value
     * @return the value of the field.
     */
    public static Object getFieldValue(Object instance, String fieldName) {
        Class<?> aClass = instance.getClass();
        Object result = null;
        try {
            
            Field field = aClass.getField(fieldName);
            result = field.get(instance);
        } catch(NoSuchFieldException e) {
            LeoObject.throwAttributeError(aClass, LeoString.valueOf(fieldName));
        } catch (Exception e) {
            LeoObject.throwAttributeAccessError(aClass, LeoString.valueOf(fieldName));
        }

        return result;
    }

    /**
     * Retrieve a class members value.
     * 
     * @param aClass the class in which to retrieve a field
     * @param fieldName the class member name, in which to retrieve its value
     * @return the value of the field.
     */
    public static Object getStaticFieldValue(Class<?> aClass, String fieldName) {
        Object result = null;
        try {
            Field field = aClass.getField(fieldName);
            result = field.get(null);
        } catch(NoSuchFieldException e) {
            LeoObject.throwAttributeError(aClass, LeoString.valueOf(fieldName));
        } catch (Exception e) {
            LeoObject.throwAttributeAccessError(aClass, LeoString.valueOf(fieldName));
        }

        return result;
    }   
    
    /**
     * Determines if the supplied class name is a native class.
     *
     * @param className the fully qualified Java class name
     * @return true if the supplied class name is a valid Java class; false otherwise
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
     * Determines if the supplied {@link Class} if of any of the supplied {@link Class} types.
     * 
     * @param type the type to check
     * @param classes the classes to compare type against
     * @return true if type is any of the supplied classes; false otherwise
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
	 * 
	 * @param child
	 * @param parent
	 * @return true if child inherits from parent, false otherwise
	 */
    public static boolean inheritsFrom(Class<?> child, Class<?> parent) {
        for (Class<?> i = child; i != null && i != Object.class; i = i.getSuperclass()) {

            if (i.equals(parent)) {
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
	 * Retrieves all of the methods defined in the supplied {@link Class}.  This will navigate up the class
	 * hierarchy up until the Object class is reached.
	 * 
	 * <p>
	 * This will not grab any methods annotated with {@link LeolaIgnore}.
	 * 
	 * @param aClass the class to grab all the public methods from.
	 * @return the {@link List} of public {@link Method}s.
	 */
	public static List<Method> getAllDeclaredMethods(Class<?> aClass) {
        final List<Method> methods = new ArrayList<Method>();
        iterateOverHierarchy(aClass, new MethodIterator() {
            
            @Override
            public ReturnType call(Method method) {
                methods.add(method);                
                return ReturnType.DONT_STOP; 
                 
            }
        });
        
        return methods;
	}

    /**
     * Retrieves all of the fields defined in the supplied {@link Class}.  This will navigate up the class
     * hierarchy up until the Object class is reached.
     * 
     * <p>
     * This will not grab any fields annotated with {@link LeolaIgnore}.
     * 
     * @param aClass the class to grab all the public fields from.
     * @return the {@link List} of public {@link Field}s.
     */
	public static List<Field> getAllDeclaredFields(Class<?> aClass) {
		List<Field> methods = new ArrayList<Field>();
		try {		    
		    for(Class<?> i = aClass;
	            i != null &&	         
	            i != Object.class; 
	            i = i.getSuperclass() ) {

				for(Field m : i.getDeclaredFields()) {
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
	 * Get the method by name and parameter types. It walks the class hierarchy and attempts to
	 * find the method by using {@link Class#getMethod(String, Class...)}.
	 * 
	 * 
	 * @param aClass the class to check
	 * @param methodName the methods name
	 * @param parameterTypes the parameter types.
	 * 
	 * @return the {@link Method} if found; otherwise null.
	 */
    public static Method getInheritedMethodByName(Class<?> aClass, String name, Class<?>[] parameterTypes) {
        Method inheritedMethod = null;

        for (Class<?> i = aClass; i != null && i != Object.class; i = i.getSuperclass()) {
            try {
                inheritedMethod = i.getMethod(name, parameterTypes);
                if (inheritedMethod != null) {
                    return inheritedMethod;
                }

            }
            catch (Exception ignore) {
                /* Ignore */
            }

        }

        return null;
    }

    
    /**
     * Get the data member by name.  It walks the class hierarchy and attempts to 
     * find the field.
     * 
     * @param aClass the class to check
     * @param fieldName the data member name
     * @return the {@link Field} if found; otherwise null.
     */
	public static Field getInheritedField(Class<?> aClass, String fieldName) {
        for (Class<?> i = aClass; i != null && i != Object.class; i = i.getSuperclass()) {

            try {
                Field f = i.getField(fieldName);
                if (f != null) {
                    f.setAccessible(true);
                    return f;
                }
            }
            catch (Exception ignore) {
            }

        }
        return null;
	}

	/**
	 * Retrieves the annotation from the method, if it is not on the method, the parent
	 * is checked.  This solves the "inherited annotation" problem for interfaces.
	 * 
	 * @param annotation the annotation to look for
	 * @param ownerClass the class in which this method belongs to
	 * @param method the method which should be probed for annotations
	 * @return the Annotation instance if found; otherwise null
	 */
	public static <T extends Annotation> T getAnnotation(Class<T> annotation, Class<?> ownerClass, Method method) {
		if ( method == null ) {
			return null;
		}

		if ( method.isAnnotationPresent(annotation) ) {
			return method.getAnnotation(annotation);
		}
		else {
		    
		    /* Check all the interface Classes to determine if the annotation
		     * exists
		     */
			for(Class<?> aInterface : ownerClass.getInterfaces()) {
				Method inheritedMethod = getInheritedMethodByName(aInterface, method.getName(), method.getParameterTypes());
				if ( inheritedMethod != null ) {
					T result = getAnnotation(annotation, aInterface, inheritedMethod);
					if ( result != null ) {
						return result;
					}
				}
			}

			/* Query the parent class for the annotation */
			Class<?> superClass = method.getDeclaringClass().getSuperclass();
			Method inheritedMethod = getInheritedMethodByName(superClass, method.getName(), method.getParameterTypes());

			return getAnnotation(annotation, superClass, inheritedMethod);
		}
	}


	/**
	 * Creates a new {@link LeoNativeClass} based off of the Java fully qualified name and supplied parameters.
	 *
	 * @param className the fully qualified Java class name
	 * @param params the parameters used to construct the instance
	 * @return the {@link LeoNativeClass}
	 * @throws LeolaRuntimeException
	 */
	public static LeoNativeClass newNativeInstance(String className, LeoObject ... params) throws LeolaRuntimeException {
		LeoNativeClass result = null;

		/* ensure we always have a valid array */
		if ( params == null ) {
			params = new LeoObject[0];
		}

		
		try {
		    /* first determine if this is a valid Java class type.
		     * If it isn't, we can't instantiate this, and return
		     * an error
		     */
			Class<?> nativeClass = Class.forName(className);
			
			
			/* Only allow for constructing non-abstract class types
			 * - this might change in the future if we decide to add
			 * Java bytecode creation here
			 */
			if ( Modifier.isAbstract(nativeClass.getModifiers()) ) {
			    LeoObject.throwNativeMethodError("Can't instantiate an abstract Java class '" + className + "'");
			}
			
			
			Object instance = null;
			for(Constructor<?> constructor : nativeClass.getConstructors()) {
				Class<?>[] paramTypes = constructor.getParameterTypes();
				if ( paramTypes.length == params.length) {
					instance = tryNativeConstructor(constructor, params, paramTypes);
					
					/* if we were able to successfully create the Java instance
					 * go ahead and wrap it in the LeoNativeClass; we
					 * are done here now.
					 */
					if ( instance != null ) {
					    result = new LeoNativeClass(nativeClass, instance);
						break;
					}
				}
			}
			
			
			/* if we were not able to find a matching constructor, bail out
			 * and throw an error
			 */
	        if (result == null ) {
	            LeoObject.throwClassNotFoundError("The Java class '" + className + "' was not found.");
	        }
	        
		}
		catch(LeolaRuntimeException e) {
		    throw e;
		}
		catch(ClassNotFoundException e) {
		    LeoObject.throwClassNotFoundError("The Java class '" + className + "' was not found.");
		}
		catch(Throwable e) {
		    LeoObject.throwNativeMethodError("Unable to construct Java native type: " + className + " - " + e);
		}


		return result;
	}

	/**
	 * Attempts to instantiate the object
	 * 
	 * @param constructor
	 * @param params
	 * @param paramTypes
	 * @return the resulting object from executing the constructor
	 */
	private static Object tryNativeConstructor(Constructor<?> constructor, LeoObject[] params, Class<?>[] paramTypes) {
		Object result = null;
		try {
			Object[] args = new Object[paramTypes.length];
			for(int i = 0; i < paramTypes.length; i++ ) {
				Class<?> aCl = paramTypes[i];
				args[i] = LeoObject.toJavaObject(aCl, params[i]);
			}

			result = constructor.newInstance(args);
		}
		catch(InvocationTargetException e) {
		    rethrow(e);
		}
		catch(Throwable ignore) {
		    /* allow to retry */
		}

		return (result);
	}
}

