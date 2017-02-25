/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.lang.sql;

/**
 * A {@link NamedParameter} represents a named parameter and location in a SQL statement.
 * 
 * @author Tony
 *
 */
public class NamedParameter {

    /**
     * Parameter name
     */
    private String paramName;
    
    /**
     * Start index in the sql statement
     */
    private int startIndex;
    
    
    /**
     * End index in the sql statement
     */
    private int endIndex;
    
    /**
     * @param endIndex
     * @param paramName
     * @param startIndex
     */
    public NamedParameter(String paramName, int startIndex, int endIndex) {
        this.endIndex = endIndex;
        this.paramName = paramName;
        this.startIndex = startIndex;
    }
    /**
     * @return the paramName
     */
    public String getParamName() {
        return paramName;
    }
    /**
     * @return the startIndex
     */
    public int getStartIndex() {
        return startIndex;
    }
    /**
     * @return the endIndex
     */
    public int getEndIndex() {
        return endIndex;
    }        
}

