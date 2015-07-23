/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm;

import leola.vm.compiler.Bytecode;
import leola.vm.compiler.Outer;
import leola.vm.types.LeoClass;
import leola.vm.types.LeoObject;
import leola.vm.types.LeoOuterObject;
import leola.vm.types.LeoString;

/**
 * Represents a class definition, all of the meta data required to create a {@link LeoClass}
 * 
 * @author Tony
 *
 */
public class ClassDefinition {

	private LeoString className;
	private ClassDefinition superClass;
	private LeoString[] interfaces;
	
	private LeoString[] params;	
	private LeoObject[] superParams;
	private Bytecode body;
	
	private Outer[] outers;
	
	/**
	 * The scope in which the class
	 * was defined in
	 */
	private Scope declaredScope;
	
	/**
	 * @param className
	 * @param superClass
	 * @param declaredScope
	 * @param interfaces
	 * @param numberOfParams
	 * @param body
	 */
	public ClassDefinition(LeoString className, ClassDefinition superClass, Scope declaredScope,
			LeoString[] interfaces, LeoString[] params, LeoObject[] superParams, Bytecode body) {
		super();
		this.className = className;
		this.superClass = superClass;
		this.declaredScope = declaredScope;
		this.interfaces = interfaces;
		this.params = params;
		this.superParams = superParams;
		this.body = body;
		this.outers = body.numOuters>0 ? new Outer[body.numOuters] : LeoOuterObject.NOOUTERS;
	}
	
	/**
	 * @return the declaredScope
	 */
	public Scope getDeclaredScope() {
		return declaredScope;
	}
	
	/**
	 * @return the outers
	 */
	public Outer[] getOuters() {
		return outers;
	}
	
	/**
	 * @return the className
	 */
	public LeoString getClassName() {
		return className;
	}
	/**
	 * @return the superClass
	 */
	public ClassDefinition getSuperClassDefinition() {
		return superClass;
	}
	
	/**
	 * @return the superParams
	 */
	public LeoObject[] getSuperParameterNames() {
		return superParams;
	}
	
	/**
	 * @return the interfaces
	 */
	public LeoString[] getInterfaceNames() {
		return interfaces;
	}

	/**
	 * @return the params
	 */
	public LeoString[] getParameterNames() {
		return params;
	}
	/**
	 * @return the body
	 */
	public Bytecode getBody() {
		return body;
	}
	
	/**
	 * @return true if this definition inherits from another
	 */
	public boolean hasParentClass() {
		return this.superClass != null;
	}
}

