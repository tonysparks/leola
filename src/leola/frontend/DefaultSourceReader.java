/*
 * see license.txt
 */
package leola.frontend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Delegates to the supplied {@link Reader}
 *  
 * @author Tony
 *
 */
public class DefaultSourceReader implements SourceReader {

    private BufferedReader reader;
    
    public DefaultSourceReader(Reader reader) {
        this.reader = (reader instanceof BufferedReader) ? (BufferedReader)reader : new BufferedReader(reader);
    }

    @Override
    public String readLine() throws IOException {    
        return this.reader.readLine();
    }

    @Override
    public void close() throws IOException {
        this.reader.close();
    }

}
