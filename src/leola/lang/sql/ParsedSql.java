/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.lang.sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Parsed SQL represents a named parameter query that is ready to be passed to JDBC.
 * 
 * @author Tony
 *
 */
public class ParsedSql {

    /**
     * The original SQL
     */
    private String originalSql;
    
    /**
     * Named Parameters
     */
    private Map<String, ParameterLocation> namedParams;
    
    /**
     * Named parameters
     */
    private List<NamedParameter> parameters;
    
    /**
     * Parameter index
     */
    private int numOfParams;
    
    /**
     * Last compiled parameter list
     */
    private int lastNumOfParms;
    
    /**
     * JDBC SQL
     */
    private StringBuilder jdbcSql;
    
    /**
     * @param originalSql
     */
    public ParsedSql(String originalSql) {
        this.originalSql = originalSql;        
        this.namedParams = new HashMap<String, ParameterLocation>();
        this.parameters = new ArrayList<NamedParameter>();
                
        this.numOfParams = 0;
        this.lastNumOfParms = -1; /* Make sure we do our lazy jdbc build the first time */
    }

    /**
     * @return a JDBC compliant SQL statement
     */
    public StringBuilder toJdbcStatement() {
        if (this.lastNumOfParms < this.numOfParams ) {
            this.jdbcSql = new StringBuilder(this.originalSql);            
            
            int lastEndIndex = 0; /* The last index that was replaced */
            
            /* Replace each parameter with a question mark */
            for(NamedParameter param : this.parameters) {
                this.jdbcSql.replace(param.getStartIndex() + lastEndIndex, param.getEndIndex() + lastEndIndex, "?");
                lastEndIndex += (param.getStartIndex() - param.getEndIndex()) + 1 /* plus one for question mark length */;
            }
                        
            this.lastNumOfParms = this.numOfParams;
        }
        
        return this.jdbcSql; 
    }
            
    
    /**
     * @return the originalSql
     */
    public String getOriginalSql() {
        return originalSql;
    }

    /**
     * @return the namedParams
     */
    public Map<String, ParameterLocation> getNamedParams() {
        return namedParams;
    }


    /**
     * @return the numOfParams
     */
    public int getNumOfParams() {
        return numOfParams;
    }

    /**
     * Adds a {@link NamedParameter}.
     * 
     * @param paramName
     * @param startIndex
     * @param endIndex
     */
    public void addNamedParameter(String paramName, int startIndex, int endIndex) {
        this.parameters.add(new NamedParameter(paramName, startIndex, endIndex));
        
        /* Increment the number of params */
        this.numOfParams++; /* Starts at 1 as in SQL param indexes do */
        
        /* Place in index map */
        if ( ! this.namedParams.containsKey(paramName)) {                
            this.namedParams.put(paramName, new ParameterLocation(paramName) );
        }
        
        ParameterLocation location = this.namedParams.get(paramName);
        location.addParameterIndex(this.numOfParams);
        location.addSqlCharPosition(startIndex);
    }
        
    /**
     * @return the parameters
     */
    public List<NamedParameter> getNamedParameters() {
        return parameters;
    }
}

