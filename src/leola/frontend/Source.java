package leola.frontend;

import java.io.BufferedReader;
import java.io.IOException;

import leola.frontend.events.SourceLineEvent;
import leola.frontend.listener.EventDispatcher;

/**
 * <h1>Source</h1>
 *
 * <p>
 * The framework class that represents the source program.
 * </p>
 *
 * <p>
 * Copyright (c) 2009 by Ronald Mak
 * </p>
 * <p>
 * For instructional purposes only. No warranties.
 * </p>
 */
public class Source {
    
    public static final char EOL = '\n'; // end-of-line character
    public static final char EOF = (char) 0; // end-of-file character

    private BufferedReader reader; // reader for the source program
    private String line; // source line
    private int lineNum; // current source line number
    private int currentPos; // current source line position

    private EventDispatcher eventDispatcher;

    /**
     * Constructor.
     * 
     * @param reader
     *            the reader for the source program
     * @throws IOException
     *             if an I/O error occurred
     */
    public Source(EventDispatcher eventDispatcher, BufferedReader reader) {
        this.lineNum = 0;
        this.currentPos = -2; // set to -2 to read the first source line
        this.reader = reader;
        this.eventDispatcher = eventDispatcher;
    }

    /**
     * @return the eventDispatcher
     */
    public EventDispatcher getEventDispatcher() {
        return eventDispatcher;
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
    public char currentChar() throws Exception {
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
     * @throws Exception
     *             if an error occurred.
     */
    public char nextChar() throws Exception {
        ++currentPos;
        return currentChar();
    }

    /**
     * Return the source character following the current character without
     * consuming the current character.
     * 
     * @return the following character.
     * @throws Exception
     *             if an error occurred.
     */
    public char peekChar() throws Exception {
        return peekAhead(1);
    }

    /**
     * Looks ahead 'pos' positions
     * 
     * @param pos
     * @return
     * @throws Excepion
     */
    public char peekAhead(int pos) throws Exception {
        currentChar();
        if (line == null) {
            return EOF;
        }

        int nextPos = currentPos + pos;
        return nextPos < line.length() ? line.charAt(nextPos) : EOL;
    }

    /**
     * @return true if at the end of the line, else return false.
     * @throws Exception
     *             if an error occurred.
     */
    public boolean atEol() throws Exception {
        return (line != null) && (currentPos == line.length());
    }

    /**
     * @return true if at the end of the file, else return false.
     * @throws Exception
     *             if an error occurred.
     */
    public boolean atEof() throws Exception {
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
     * @throws Exception
     *             if an error occurred.
     */
    public void skipToNextLine() throws Exception {
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
    private void readLine() throws IOException {
        line = reader.readLine(); // null when at the end of the source
        currentPos = -1;

        if (line != null) {
            ++lineNum;
        }

        // Send a source line message containing the line number
        // and the line text to all the listeners.
        if (line != null) {
            this.eventDispatcher.sendNow(new SourceLineEvent(this, line, lineNum));
        }
    }

    /**
     * Close the source.
     * 
     * @throws Exception
     *             if an error occurred.
     */
    public void close() throws Exception {
        if (reader != null) {
            try {
                reader.close();
            }
            catch (IOException ex) {
                throw ex;
            }
        }
    }

}
