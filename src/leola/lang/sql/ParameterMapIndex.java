/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.lang.sql;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parameter listing, maps the variable names to the actual JDBC parameter index.
 * 
 * @author Tony
 *
 */
public class ParameterMapIndex {

    /**
     * Parameter map
     */
    private Map<String, ParameterLocation> params;

    /**
     * Index
     */
    private int index;

    
    /**    
     */
    public ParameterMapIndex() {
        this.params = new HashMap<String, ParameterLocation>();
        this.index = 1;
    }
    
    /**
     * Adds a parameter, adding to the current index.
     * 
     * @param parameterKey
     * @param index
     */
    public void addParameter(String parameterKey, int charPosition) {
        /* Store the parameter information */
        if ( ! params.containsKey(parameterKey) ) {
            params.put(parameterKey, new ParameterLocation(parameterKey));
        }
        
        ParameterLocation location = this.params.get(parameterKey);
        location.addParameterIndex(this.index++);
        location.addSqlCharPosition(charPosition);
    }
    
    /**
     * Adds a parameter and a list of its indexes
     * @param paramKey
     * @param indexes
     */
    public void addParameters(String parameterKey, ParameterLocation location) {
        /* Store the parameter information */
        if ( ! params.containsKey(parameterKey) ) {
            params.put(parameterKey, new ParameterLocation(parameterKey));
        }
        
        params.get(parameterKey).addParameterLocation(location);
    }
    
    /**
     * @param paramKey
     * @return the parameter indexes associated with the parameter Key
     */
    public List<Integer> getParameterIndexes(String paramKey) {
        if ( ! params.containsKey(paramKey)) {
            throw new IllegalArgumentException("No indexes found for: " + paramKey);
        }
        
        return params.get(paramKey).getParameterIndexes();
    }
    
    /**
     * @param paramKey
     * @return the sql character position of the parameters associated with the parameter key
     */
    public List<Integer> getSqlCharPositions(String paramKey) {
        if ( ! params.containsKey(paramKey)) {
            throw new IllegalArgumentException("No indexes found for: " + paramKey);
        }
        
        return params.get(paramKey).getSqlCharPositions();
    }
    
    /**
     * Get the {@link ParameterLocation}.
     * 
     * @param paramKey
     * @return the {@link ParameterLocation}
     */
    public ParameterLocation getParameterLocation(String paramKey) {
        if ( ! params.containsKey(paramKey)) {
            throw new IllegalArgumentException("No indexes found for: " + paramKey);
        }
        
        return params.get(paramKey);
    }
    
    /**
     * The current parameter index
     * @return
     */
    public int getIndex() {
        return this.index;
    }
    
    /**
     * Set the index
     * @param index
     */
    public void setIndex(int index) {
        this.index = index;
    }
}

