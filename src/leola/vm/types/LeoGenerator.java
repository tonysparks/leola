/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.types;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import leola.vm.compiler.Bytecode;


/**
 * A {@link LeoGenerator} is an object that behaves like a function, but
 * is "resumeable" from a yield statement.  That is, after successive invocations
 * it will pick up executing the function after the yield statement.
 * 
 * <p>
 * Example:
 * <pre>
 *   var talk = gen() {
 *     var i = 0
 *     yield "Hello: " + i
 *     i += 1
 *     yield "World: " + i
 *   }
 *   
 *   println( talk() ) // prints Hello: 0
 *   println( talk() ) // prints World: 1
 *   println( talk() ) // prints null, the function is done computing
 *   
 * </pre>
 *
 * @author Tony
 *
 */
public class LeoGenerator extends LeoFunction {
	
	/**
	 * The locals that need to be saved with this
	 * generator
	 */
	private LeoObject[] locals;
	
	/**
	 * @param type
	 * @param numberOfArgs
	 * @param body
	 */
	public LeoGenerator(LeoObject env, Bytecode bytecode) {
		super(LeoType.GENERATOR, env, bytecode);				
		this.locals = new LeoObject[bytecode.numLocals];
	}
		
	/**
	 * @return the locals
	 */
	public LeoObject[] getLocals() {
		return locals;
	}
	
	/* (non-Javadoc)
	 * @see leola.vm.types.LeoObject#isGenerator()
	 */
	@Override
	public boolean isGenerator() {
		return true;
	}		
	
	@Override
	public void write(DataOutput out) throws IOException {
//		out.write(this.getType().ordinal());
//		this.bytecode.write(out);
//		int nouters = this.outers!=null?this.outers.length:0;
//		if (nouters>0) {
////			for(int i =0; i < nouters; i++) {
////				LeoObject o = this.outers[i];
////				if ( o == null ) {
////					nouters = i;
////					break;
////				}			
////			}
//			
//			out.writeInt(nouters);
//			
////			for(int i =0; i < nouters; i++) {
////				LeoObject o = this.outers[i];						
////				o.write(out);
////			}
//		}
//		else {
//			out.writeInt(nouters);
//		}
	}
	
	/**
	 * Reads from the {@link DataInput} stream, constructing a {@link LeoObject}
	 * 
	 * @param in
	 * @return the {@link LeoObject}
	 * @throws IOException
	 */
	public static LeoGenerator read(LeoObject env, DataInput in) throws IOException {
		Bytecode bytecode = Bytecode.read(env, in);
		int nouters = in.readInt();
		
		LeoObject[] outers = new LeoObject[nouters];
		for(int i =0; i < nouters; i++) {
			outers[i] = LeoObject.read(env, in);
		}
		
		LeoGenerator function = new LeoGenerator(env, bytecode);	
		return function;
	}
}

