/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm;

import leola.vm.asm.Bytecode;
import leola.vm.asm.Outer;
import leola.vm.types.LeoObject;
import leola.vm.types.LeoOuterObject;
import leola.vm.types.LeoString;

/**
 * Represents a class definition
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
	 * @param className
	 * @param superClass
	 * @param interfaces
	 * @param numberOfParams
	 * @param body
	 */
	public ClassDefinition(LeoString className, ClassDefinition superClass,
			LeoString[] interfaces, LeoString[] params, LeoObject[] superParams, Bytecode body) {
		super();
		this.className = className;
		this.superClass = superClass;
		this.interfaces = interfaces;
		this.params = params;
		this.superParams = superParams;
		this.body = body;
		this.outers = body.numOuters>0 ? new Outer[body.numOuters] : LeoOuterObject.NOOUTERS;
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
	public ClassDefinition getSuperClass() {
		return superClass;
	}
	
	/**
	 * @return the superParams
	 */
	public LeoObject[] getSuperParams() {
		return superParams;
	}
	
	/**
	 * @return the interfaces
	 */
	public LeoString[] getInterfaces() {
		return interfaces;
	}

	/**
	 * @return the params
	 */
	public LeoString[] getParams() {
		return params;
	}
	/**
	 * @return the body
	 */
	public Bytecode getBody() {
		return body;
	}
	
	public boolean hasParentClass() {
		return this.superClass != null;
	}
}

