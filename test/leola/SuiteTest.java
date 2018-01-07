/* 
 * see license.txt
 */
package leola;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileFilter;

import org.junit.Test;

import leola.vm.Leola;
import leola.vm.types.LeoObject;

/**
 * Runs all tests
 * 
 * @author Tony
 *
 */
public class SuiteTest {

    @Test
    public void test() {        
        File testsDir = new File(System.getProperty("user.dir"), "tests");
        File[] testScripts = testsDir.listFiles(new FileFilter() {
            
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().toLowerCase().endsWith("test.leola");
            }
        });
        
        for(File testScript : testScripts) {
            runTest(testScript);
        }
    }
    
    private void runTest(File testScript) {
        System.out.print("Running test: " + testScript + "...");
        
        Leola leola = Leola.builder()
                           .setIsDebugMode(true)
                           .newRuntime();
        try {
            LeoObject result = leola.eval(testScript);
            assertFalse(result.isError());
        }
        catch(AssertionError error) {
            error.printStackTrace();
            
            throw error;
        }
        catch(Exception e) {
            e.printStackTrace();
            
            fail(e.getMessage());
        }
        
        System.out.println("passed!");
    }

}
