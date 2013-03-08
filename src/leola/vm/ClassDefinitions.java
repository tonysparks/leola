/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import leola.vm.asm.Scope;
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

	private Map<LeoString, ClassDefinition> classDefinitions;
	
	public boolean hasDefinitions() {
		return this.classDefinitions != null && ! this.classDefinitions.isEmpty();
	}
	
	/**
	 * @return the classDefinitions
	 */
	public Map<LeoString, ClassDefinition> getClassDefinitions() {
		if ( this.classDefinitions == null ) {
			this.classDefinitions = new ConcurrentHashMap<LeoString, ClassDefinition>();
		}
		
		return classDefinitions;
	}
	
	public void storeClass(LeoString className, ClassDefinition klass) {
		getClassDefinitions().put(className, klass);
	}
	
	public void removeClass(LeoString className) {
		getClassDefinitions().remove(className);
	}
	
	public boolean containsClass(LeoString className) {
		return getClassDefinitions().containsKey(className);
	}
	
	public ClassDefinition getDefinition(LeoString className) {
		return getClassDefinitions().get(className);
	}
	
	/**
	 * Resolves the passing of parameters to the super class.
	 * @param paramNames
	 * @param params
	 * @param superParams
	 */
	private void resolveSuperArguments(LeoString[] paramNames, LeoObject[] params, LeoObject[] superParams) {
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
	 * Creates a new instance of an object.
	 * 
	 * @param runtime
	 * @param className
	 * @param params
	 * @return
	 */
	public LeoObject newInstance(Leola runtime, LeoString className, LeoObject[] params) {
		if ( ! getClassDefinitions().containsKey(className)) {
			throw new LeolaRuntimeException("No class found for: " + className);
		}
		
		ClassDefinition definition = getClassDefinitions().get(className);
		
		LeoObject parentClass = LeoNull.LEONULL;
		if ( definition.hasParentClass() ) {
			
			LeoString[] paramNames = definition.getParams();
			LeoObject[] superParams = definition.getSuperParams(); 
			if ( superParams != null ) {
				LeoObject[] clone = new LeoObject[superParams.length];
				System.arraycopy(superParams, 0, clone, 0, superParams.length);
				superParams = clone;
			}
			
			resolveSuperArguments(paramNames, params, superParams);
			
			parentClass = newInstance(runtime, definition.getSuperClass().getClassName(), superParams);
		}
		
		Scope scope = runtime.getSymbols().newObjectScope();
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

