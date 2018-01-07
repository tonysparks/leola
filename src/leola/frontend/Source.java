package leola.frontend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles reading a source code file
 * 
 * @author Tony
 *
 */
public class Source implements AutoCloseable {
    
    public static final char EOL = '\n'; // end-of-line character
    public static final char EOF = (char) 0; // end-of-file character

    private BufferedReader reader; // reader for the source program
    private String line; // source line
    private int lineNum; // current source line number
    private int currentPos; // current source line position
    
    private List<String> lines;

    /**
     * 
     * @param reader
     *            the reader for the source program
     * @throws IOException
     *             if an I/O error occurred
     */
    public Source(Reader reader) {
        this.lineNum = 0;
        this.currentPos = -2; // set to -2 to read the first source line
        this.lines = new ArrayList<>();
        this.reader = (reader instanceof BufferedReader) ? 
                (BufferedReader)reader : new BufferedReader(reader);
    }

    public String getLine(int lineNumber) {
        if(lineNumber < 1 || lineNumber > this.lines.size()) {
            throw new ParseException("Invalid line number: " + lineNumber);
        }
        
        return this.lines.get(lineNumber - 1);
    }

    /**
     * Getter.
     * 
     * @return the current source line number.
     */
    public int getLineNum() {
        return lineNum;
    }

    /**
     * Getter.
     * 
     * @return the position of the next source character in the current source
     *         line.
     */
    public int getPosition() {
        return currentPos;
    }

    /**
     * Return the source character at the current position.
     * 
     * @return the source character at the current position.
     * @throws Exception
     *             if an error occurred.
     */
    public char currentChar() {
        // First time?
        if (currentPos == -2) {
            readLine();
            return nextChar();
        }

        // At end of file?
        else if (line == null) {
            return EOF;
        }

        // At end of line?
        else if ((currentPos == -1) || (currentPos == line.length())) {
            return EOL;
        }

        // Need to read the next line?
        else if (currentPos > line.length()) {
            readLine();
            return nextChar();
        }

        // Return the character at the current position.
        else {
            return line.charAt(currentPos);
        }
    }

    /**
     * Consume the current source character and return the next character.
     * 
     * @return the next source character.
     */
    public char nextChar() {
        ++currentPos;
        return currentChar();
    }

    /**
     * Return the source character following the current character without
     * consuming the current character.
     * 
     * @return the following character.
     */
    public char peekChar() {
        return peekAhead(1);
    }

    /**
     * Looks ahead 'pos' positions
     * 
     * @param pos
     * @return the peeked character
     */
    public char peekAhead(int pos) {
        currentChar();
        if (line == null) {
            return EOF;
        }

        int nextPos = currentPos + pos;
        return nextPos < line.length() ? line.charAt(nextPos) : EOL;
    }

    /**
     * @return true if at the end of the line, else return false.
     */
    public boolean atEol() {
        return (line != null) && (currentPos == line.length());
    }

    /**
     * @return true if at the end of the file, else return false.
     */
    public boolean atEof() {
        // First time?
        if (currentPos == -2) {
            readLine();
        }

        return line == null;
    }

    /**
     * Skip the rest of the current input line by forcing the next read to read
     * a new line.
     * 
     */
    public void skipToNextLine() {
        if (line != null) {
            currentPos = line.length() + 1;
        }
    }

    public String getCurrentLine() {
        return this.line;
    }

    /**
     * Read the next source line.
     * 
     * @throws IOException
     *             if an I/O error occurred.
     */
    private void readLine() {
        try {
            line = reader.readLine(); // null when at the end of the source
            currentPos = -1;
    
            if (line != null) {
                ++lineNum;
            }
            
            lines.add(line);
        }
        catch(IOException e) {
            throw new ParseException(e);
        }
    }

    /**
     * Close the source.
     */
    @Override
    public void close() {
        this.lines.clear();
        
        if (reader != null) {
            try {
                reader.close();
            }
            catch(IOException e) {
                throw new ParseException(e);    
            }
        }
    }

}
