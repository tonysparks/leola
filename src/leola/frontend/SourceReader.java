/*
 * see license.txt 
 */
package leola.frontend;

import java.io.IOException;

/**
 * Reads bytes
 * 
 * @author Tony
 *
 */
public interface SourceReader extends AutoCloseable {

    /**
     * @return the current character
     */
    public String readLine() throws IOException;
    
    
    @Override    
    public void close() throws IOException;
}
