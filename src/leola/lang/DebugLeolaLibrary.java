/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.lang;

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.util.concurrent.atomic.AtomicBoolean;

import leola.vm.Args;
import leola.vm.Leola;
import leola.vm.compiler.Assembler;
import leola.vm.compiler.Bytecode;
import leola.vm.debug.DebugEvent;
import leola.vm.debug.DebugListener;
import leola.vm.exceptions.LeolaRuntimeException;
import leola.vm.lib.LeolaIgnore;
import leola.vm.lib.LeolaLibrary;
import leola.vm.types.LeoClass;
import leola.vm.types.LeoFunction;
import leola.vm.types.LeoNamespace;
import leola.vm.types.LeoNull;
import leola.vm.types.LeoObject;

/**
 * The system core functions
 *
 * @author Tony
 *
 */
public class DebugLeolaLibrary implements LeolaLibrary {

    /**
     * The runtime
     */
    private Leola runtime;
    
    
    /* (non-Javadoc)
     * @see leola.frontend.LeolaLibrary#init(leola.frontend.Leola)
     */
    @LeolaIgnore
    public void init(Leola runtime, LeoNamespace namespace) throws LeolaRuntimeException {                        
        this.runtime = runtime;    
        this.runtime.putIntoNamespace(this, namespace);
    }

    /**
     * Retrieves the {@link Bytecode} from the supplied object.
     * @param f
     * @return the bytecode
     * @throws Exception
     */
    public final Bytecode getbytecode(LeoObject f) throws Exception {
        Bytecode result = null;
        switch(f.getType()) {
            case GENERATOR:
            case FUNCTION: {
                LeoFunction fun = f.as();
                result = fun.getBytecode();
                break;
            }
            case CLASS: {
                LeoClass cls = f.as();
                result = cls.getConstructor();
                break;
            }            
            default:
                throw new LeolaRuntimeException("Unsupported type!");
        }
        return result;
    }
    
    /**
     * Creates a {@link LeoFunction} based off of the supplied assembler code (string).
     * @param asm
     * @return the {@link LeoObject} function
     * @throws Exception
     */
    public LeoObject asm(LeoObject asm) throws Exception {
        Assembler assember = new Assembler();
        Bytecode code = assember.compile(new BufferedReader(new StringReader(asm.toString())));
        return new LeoFunction(runtime, runtime.getGlobalNamespace(), code);
    }

    /**
     * Attaches a debugger script.  The script should return
     * back a callback function that will be used to pass back
     * the {@link DebugEvent}.
     * 
     * @param filename the script file
     */
    public void debugger(final String filename) throws Exception {
        final File scriptFile = new File(filename);
        
        runtime.setDebugListener(new DebugListener() {
            Leola runtime = Args.builder()
                                .setIncludeDirectories(DebugLeolaLibrary.this.runtime.getIncludePath())
                                .setFileName(filename)
                                .newRuntime();
            LeoObject fun = runtime.eval(scriptFile);
            
            
            @Override
            public void onLineNumber(DebugEvent event) {
                fun.xcall(LeoObject.valueOf(event));            
            }
        });
    }
    
    /**
     * The assert function by default is disabled.
     */
    private AtomicBoolean assertEnabled = new AtomicBoolean(false);

    /**
     * Toggles the Assert mechanism.
     * @param enable
     */
    public final void enableAssert(boolean enable) {
        assertEnabled.set(enable);
    }

    /**
     * Fail with an optional message.  This exits the program.
     * @param message
     */
    public final void assertFail(String message) throws Exception {
        if (assertEnabled.get()) {
            throw new AssertionError(message);
        }
    }



    /**
     * Assert that obj is true
     *
     * @param obj
     * @param message
     * @throws Exception
     */
    public final void assertTrue(LeoObject obj, String message) throws Exception {
        if (assertEnabled.get()) {
            if (!LeoObject.isTrue(obj)) {
                assertFail("ASSERT FAIL: The object is not equal to true. " + message);
            }
        }
    }

    /**
     * Assert that obj is false
     *
     * @param obj
     * @param message
     * @throws Exception
     */
    public final void assertFalse(LeoObject obj, String message) throws Exception {
        if (assertEnabled.get()) {
            if (LeoObject.isTrue(obj)) {
                assertFail("ASSERT FAIL: The object is not equal to true. " + message);
            }
        }
    }

    /**
     * Assert that both objects are equal to each other.
     *
     * @param l
     * @param r
     * @param message
     * @throws Exception
     */
    public final void assertEq(LeoObject l, LeoObject r, String message) throws Exception {
        if (assertEnabled.get()) {
            if (l != null ) {
                if ( ! l.$eq(r) ) {
                    assertFail("ASSERT FAIL: Left object (" + l + ") is not equal to the Right object (" + r + ") " + message);
                }
            }
            else if ( r != null ) {
                assertFail("ASSERT FAIL: Left object (null) is not equal to the Right object " + r + ") " + message);
            }
        }
    }

    /**
     * Assert that the supplied object is not null.
     *
     * @param obj
     * @param message
     * @throws Exception
     */
    public final void assertNotNull(LeoObject obj, String message) throws Exception {
        if (assertEnabled.get()) {
            if (obj == null || LeoNull.LEONULL == obj ) {
                assertFail("ASSERT FAIL: The supplied object is null. " + message);
            }
        }
    }

    /**
     * Assert that the supplied object is null.
     *
     * @param obj
     * @param message
     * @throws Exception
     */
    public final void assertNull(LeoObject obj, String message) throws Exception {
        if (assertEnabled.get()) {
            if (obj != null && LeoNull.LEONULL != obj ) {
                assertFail("ASSERT FAIL: The supplied object is not null. " + message);
            }
        }
    }    
}

