/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.vm.types;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import leola.vm.Leola;
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
    public LeoGenerator(Leola runtime, LeoObject env, Bytecode bytecode) {
        super(runtime, LeoType.GENERATOR, env, bytecode);                
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
    
    /**
     * Returns a sequence consisting of those items from the generator for which function(item) is true
     * 
     * <pre>
     *   var count = def(to) {
     *      return gen() {
     *          var i = 0
     *          while i < to {
     *              yield i
     *              i += 1
     *          }
     *      }
     *   }
     *   
     *   filter(count(5), def(e) return e%2==0)
     *    .foreach(println)
     *    
     *   // prints: 
     *   // 0
     *   // 2
     *   // 4
     *   
     * </pre>
     * 
     * @param function
     * @return the resulting {@link LeoArray}
     */
    public LeoArray filter(LeoObject function) {
        LeoArray result = new LeoArray();
        while(true) {
            LeoObject generatorResult = xcall();
            if(generatorResult == LeoNull.LEONULL) {
                break;
            }
            
            if( LeoObject.isTrue(function.xcall(generatorResult))) {
                result.add(generatorResult);
            }
        }
        
        return result;
    }
    
    /**
     * Iterates through the array, invoking the supplied 
     * function object for each element
     * 
     * <pre>
     *   var count = def(to) {
     *      return gen() {
     *          var i = 0
     *          while i < to {
     *              yield i
     *              i += 1
     *          }
     *      }
     *   }
     *   
     *   foreach(count(5), println)
     *   // prints: 
     *   // 0
     *   // 1
     *   // 2
     *   // 3
     *   // 4
     *   
     * </pre>
     * 
     * @param function
     * @return the {@link LeoObject} returned from the supplied function if returned <code>true</code>
     */
    public LeoObject foreach(LeoObject function) {
        while(true) {
            LeoObject generatorResult = xcall();
            if(generatorResult == LeoNull.LEONULL) {
                break;
            }
            
            LeoObject result = function.xcall(generatorResult);
            if ( LeoObject.isTrue(result) ) {
                return result;
            }
        }
        
        return LeoObject.NULL;
    }
    
    
    /**
     * Applies a function to each generator iteration.
     * 
     * <pre>
     *   var count = def(to) {
     *      return gen() {
     *          var i = 0
     *          while i < to {
     *              yield i
     *              i += 1
     *          }
     *      }
     *   }
     *   
     *   map(count(5), def(e) return e + 10)
     *     .foreach(println)
     *   // prints: 
     *   // 10
     *   // 11
     *   // 12
     *   // 13
     *   // 14
     * </pre>
     * 
     * @param function
     * @return a {@link LeoArray} of results
     */
    public LeoArray map(LeoObject function) {
        LeoArray result = new LeoArray();
        while(true) {
            LeoObject generatorResult = xcall();
            if(generatorResult == LeoNull.LEONULL) {
                break;
            }
            
            result.add(function.xcall(generatorResult));                   
        }
        return result;
    }
    
    /**
     * Reduces all of the elements in this generator into one value.
     * 
     * <pre>
     *   var count = def(to) {
     *      return gen() {
     *          var i = 0
     *          while i < to {
     *              yield i
     *              i += 1
     *          }
     *      }
     *   }
     *   var sum = reduce(count(5), def(p,n) return p+n)
     *   println(sum) // 10
     * </pre>
     * 
     * 
     * @param function
     * @return
     */
    public LeoObject reduce(LeoObject function) {
        LeoObject result = xcall();
        if(result != LeoObject.NULL) {
            while(true) {                        
                LeoObject generatorResult = xcall();
                if(generatorResult == LeoNull.LEONULL) {
                    break;
                }
                
                result = function.xcall(result, generatorResult);            
            } 
        }
        
        return result;
    }
    
    /* (non-Javadoc)
     * @see leola.vm.types.LeoFunction#clone()
     */
    @Override
    public LeoObject clone() {
        LeoGenerator clone = new LeoGenerator(this.runtime, this.env, getBytecode());
        if(clone.outers!=null) {
            for(int i = 0; i < clone.outers.length; i++) {
                clone.outers[i] = this.outers[i];
            }
        }
        return clone;
    }
    
    @Override
    public void write(DataOutput out) throws IOException {
    }
    
    /**
     * Reads from the {@link DataInput} stream, constructing a {@link LeoObject}
     * 
     * @param in
     * @return the {@link LeoObject}
     * @throws IOException
     */
    public static LeoGenerator read(Leola runtime, LeoObject env, DataInput in) throws IOException {
        Bytecode bytecode = Bytecode.read(env, in);
        int nouters = in.readInt();
        
        LeoObject[] outers = new LeoObject[nouters];
        for(int i =0; i < nouters; i++) {
            outers[i] = LeoObject.read(env, in);
        }
        
        LeoGenerator function = new LeoGenerator(runtime, env, bytecode);    
        return function;
    }
}

