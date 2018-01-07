/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.vm.compiler;

import leola.ast.ASTNode;
import leola.vm.Leola;

/**
 * Compiles an Abstract Syntax Tree into {@link Bytecode}
 * 
 * @author Tony
 *
 */
public class Compiler {

    private Leola runtime;
    
    /**
     * @param runtime
     */
    public Compiler(Leola runtime) {
        this.runtime = runtime;
    }

    /**
     * Compiles the supplied {@link ASTNode} into {@link Bytecode}
     * 
     * @param node
     * @return the {@link Bytecode}
     */
    public Bytecode compile(ASTNode node) {
        BytecodeGeneratorVisitor gen = new BytecodeGeneratorVisitor(this.runtime, new EmitterScopes());
        node.visit(gen);
        
        BytecodeEmitter asm = gen.getAsm();
        Bytecode bytecode = asm.compile();
        
        return bytecode;
    }
}
