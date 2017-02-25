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

/**
 * Represents a class definition, all of the meta data required to create a {@link LeoClass}
 * 
 * @author Tony
 *
 */
public class ClassDefinition {

    private LeoObject className;
    private ClassDefinition superClass;
    private LeoObject[] interfaces;
    
    private LeoObject[] params;    
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
    public ClassDefinition(LeoObject className, ClassDefinition superClass, Scope declaredScope,
            LeoObject[] interfaces, LeoObject[] params, LeoObject[] superParams, Bytecode body) {
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
     * @return true if the constructor contains variable arguments
     */
    public boolean hasVarargs() {
        return this.body.hasVarargs();
    }
    
    /**
     * @return the number of parameters the constructor takes
     */
    public int getNumberOfParameters() {
        return this.body.numArgs;
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
    public LeoObject getClassName() {
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
    public LeoObject[] getInterfaceNames() {
        return interfaces;
    }

    /**
     * @return the params
     */
    public LeoObject[] getParameterNames() {
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

