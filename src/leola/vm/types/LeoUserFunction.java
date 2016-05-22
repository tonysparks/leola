/*
 * see license.txt
 */
package leola.vm.types;

import java.io.DataOutput;
import java.io.IOException;

/**
 * A user defined function.  This allows one to write Java code:
 * 
 * <pre>
 *    Leola runtime = ...
 *    LeoArray array = ...
 *    array.foreach( new LeoUserFunction() {
 *        public LeoObject call(LeoObject element) {
 *          System.out.println(element);
 *        }
 *    });
 * </pre>
 * 
 * @author Tony
 *
 */
public class LeoUserFunction extends LeoObject {

    /**
     */
    public LeoUserFunction() {
        super(LeoType.USER_FUNCTION);
    }
    
    /* (non-Javadoc)
     * @see leola.vm.types.LeoObject#isFunction()
     */
    @Override
    public boolean isFunction() {     
        return true;
    }

    /* (non-Javadoc)
     * @see leola.vm.types.LeoObject#$eq(leola.vm.types.LeoObject)
     */
    @Override
    public boolean $eq(LeoObject other) {       
        return other==this;
    }

    /* (non-Javadoc)
     * @see leola.vm.types.LeoObject#$lt(leola.vm.types.LeoObject)
     */
    @Override
    public boolean $lt(LeoObject other) {
        return false;
    }

    /* (non-Javadoc)
     * @see leola.vm.types.LeoObject#$gt(leola.vm.types.LeoObject)
     */
    @Override
    public boolean $gt(LeoObject other) {
        return false;
    }

    /* (non-Javadoc)
     * @see leola.vm.types.LeoObject#getValue()
     */
    @Override
    public Object getValue() {
        return this;
    }

    /* (non-Javadoc)
     * @see leola.vm.types.LeoObject#clone()
     */
    @Override
    public LeoObject clone() {
        return this;
    }

    /* (non-Javadoc)
     * @see leola.vm.types.LeoObject#write(java.io.DataOutput)
     */
    @Override
    public void write(DataOutput out) throws IOException {
    }

    /* (non-Javadoc)
     * @see leola.vm.types.LeoObject#call()
     */
    @Override
    public LeoObject call() {
        return call(new LeoObject[] {});
    }
    
    /* (non-Javadoc)
     * @see leola.vm.types.LeoObject#call(leola.vm.types.LeoObject)
     */
    @Override
    public LeoObject call(LeoObject arg1) {
        return call(new LeoObject[] {arg1});
    }
    
    /* (non-Javadoc)
     * @see leola.vm.types.LeoObject#call(leola.vm.types.LeoObject, leola.vm.types.LeoObject)
     */
    @Override
    public LeoObject call(LeoObject arg1, LeoObject arg2) {
        return call(new LeoObject[] {arg1, arg2});
    }
    
    /* (non-Javadoc)
     * @see leola.vm.types.LeoObject#call(leola.vm.types.LeoObject, leola.vm.types.LeoObject, leola.vm.types.LeoObject)
     */
    @Override
    public LeoObject call(LeoObject arg1, LeoObject arg2, LeoObject arg3) {
        return call(new LeoObject[] {arg1, arg2, arg3});
    }
    
    /* (non-Javadoc)
     * @see leola.vm.types.LeoObject#call(leola.vm.types.LeoObject, leola.vm.types.LeoObject, leola.vm.types.LeoObject, leola.vm.types.LeoObject)
     */
    @Override
    public LeoObject call(LeoObject arg1, LeoObject arg2, LeoObject arg3, LeoObject arg4) {
        return call(new LeoObject[] {arg1, arg2, arg3, arg4});
    }
    
    /* (non-Javadoc)
     * @see leola.vm.types.LeoObject#call(leola.vm.types.LeoObject, leola.vm.types.LeoObject, leola.vm.types.LeoObject, leola.vm.types.LeoObject, leola.vm.types.LeoObject)
     */
    @Override
    public LeoObject call(LeoObject arg1, LeoObject arg2, LeoObject arg3, LeoObject arg4, LeoObject arg5) {
        return call(new LeoObject[] {arg1, arg2, arg3, arg4, arg5});
    }
    
    /* (non-Javadoc)
     * @see leola.vm.types.LeoObject#call(leola.vm.types.LeoObject[])
     */
    @Override
    public LeoObject call(LeoObject[] args) {
        return LeoObject.NULL;
    }
}
