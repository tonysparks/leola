/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.ast;

import java.util.ArrayList;
import java.util.List;

/**
 * A parameter listing
 * 
 * @author Tony
 *
 */
public class ParameterList {

    private List<String> parameters;
    private boolean isVarargs;
    
    

    /**
     */
    public ParameterList() {        
        this.parameters = new ArrayList<String>();
        this.isVarargs = false;
    }

    /**
     * Adds a parameter
     * @param parameter
     */
    public void addParameter(String parameter) {
        this.parameters.add(parameter);
    }
    
    /**
     * @return the parameters
     */
    public List<String> getParameters() {
        return parameters;
    }
    
    /**
     * @return the number of parameters
     */
    public int size() {
        return this.parameters.size();
    }
    
    /**
     * @param isVarargs the isVarargs to set
     */
    public void setVarargs(boolean isVarargs) {
        this.isVarargs = isVarargs;
    }
    
    /**
     * @return the isVarargs
     */
    public boolean isVarargs() {
        return isVarargs;
    }
}
