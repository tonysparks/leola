/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import leola.vm.exceptions.LeolaRuntimeException;
import leola.vm.types.LeoClass;
import leola.vm.types.LeoNull;
import leola.vm.types.LeoObject;
import leola.vm.types.LeoString;

/**
 * Stores the available class definitions
 * 
 * @author Tony
 *
 */
public class ClassDefinitions {

	private Map<LeoObject, ClassDefinition> classDefinitions;
	
	
	/**
	 * Determine if there are any {@link ClassDefinition}'s defined
	 * 
	 * @return true if there are {@link ClassDefinition}'s defined
	 */
	public boolean hasDefinitions() {
		return this.classDefinitions != null && ! this.classDefinitions.isEmpty();
	}
	
	/**
	 * @return the classDefinitions
	 */
	public Map<LeoObject, ClassDefinition> getClassDefinitions() {
		if ( this.classDefinitions == null ) {
			this.classDefinitions = new ConcurrentHashMap<LeoObject, ClassDefinition>();
		}
		
		return classDefinitions;
	}
	
	/**
	 * Stores a {@link ClassDefinition}
	 * 
	 * @param className
	 * @param klass
	 */
	public void storeClass(LeoObject className, ClassDefinition klass) {
		getClassDefinitions().put(className, klass);
	}
	
	
	/**
	 * Removes the {@link ClassDefinition} associated with the supplied class name.
	 * 
	 * @param className
	 */
	public void removeClass(LeoObject className) {
		getClassDefinitions().remove(className);
	}
	
	
	/**
	 * Determines if there is a {@link ClassDefinition} defined by the supplied
	 * class name.
	 * 
	 * @param className
	 * @return true if there exists a {@link ClassDefinition} associated with the supplied class name.
	 */
	public boolean containsClass(LeoObject className) {
		return getClassDefinitions().containsKey(className);
	}
	
	/**
	 * Retrieves the {@link ClassDefinition} associated with the supplied class name.
	 * 
	 * @param className
	 * @return the {@link ClassDefinition} associated with the class name.  If there is not 
	 * a {@link ClassDefinition} associated with the class name, a {@link LeolaRuntimeException} is 
	 * thrown.
	 */
	public ClassDefinition getDefinition(LeoObject className) {
	    if ( ! getClassDefinitions().containsKey(className)) {
            throw new LeolaRuntimeException("No class found for: " + className);
        }               
	    
		return getClassDefinitions().get(className);
	}
	
	/**
	 * Resolves the passing of parameters to the super class.
	 * 
	 * @param paramNames
	 * @param params
	 * @param superParams
	 */
	private void resolveSuperArguments(LeoObject[] paramNames, LeoObject[] params, LeoObject[] superParams) {
		if(superParams != null && paramNames != null) {
			for(int i = 0; i < superParams.length; i++) {
				LeoObject sp = superParams[i];
				if(sp.isString()) {
					for(int j=0;j<paramNames.length;j++) {
						if(sp.$eq(paramNames[j])) {
							if ( params != null && j < params.length ) {
								superParams[i] = params[j];
							}
							else {
								superParams[i] = LeoNull.LEONULL;
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Creates a new instance of a class defined by the {@link ClassDefinition} for the supplied
	 * class name.
	 * 
	 * @param runtime
	 * @param className
	 * @param params
	 * @return the new {@link LeoObject} instance.
	 */
	public LeoObject newInstance(Leola runtime, LeoObject className, LeoObject[] params) {		
		ClassDefinition definition = getDefinition(className);		
		return newInstance(runtime, definition, params);
	}
	
	/**
     * Creates a new instance of a class defined by the {@link ClassDefinition} for the supplied
     * class name.
     * 
     * @param runtime
     * @param definition
     * @param params
     * @return the new {@link LeoObject} instance.
     */
    public LeoObject newInstance(Leola runtime, ClassDefinition definition, LeoObject[] params) {        
        LeoObject parentClass = LeoNull.LEONULL;
        if ( definition.hasParentClass() ) {
            
            LeoString[] paramNames = definition.getParameterNames();
            LeoObject[] superParams = definition.getSuperParameterNames(); 
            if ( superParams != null ) {
                LeoObject[] clone = new LeoObject[superParams.length];
                System.arraycopy(superParams, 0, clone, 0, superParams.length);
                superParams = clone;
            }
            
            resolveSuperArguments(paramNames, params, superParams);
            
            parentClass = newInstance(runtime, definition.getSuperClassDefinition().getClassName(), superParams);
        }
        
        Scope scope = new Scope(definition.getDeclaredScope());
        if ( parentClass != LeoNull.LEONULL ) {
            LeoClass p = parentClass.as();
            scope.setParent(p.getScope());
        }       
        
        LeoClass klass = new LeoClass(runtime
                                    , scope
                                    , definition
                                    , parentClass                                   
                                    , params); 
        
        return klass;
    }
}

