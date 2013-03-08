/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.lang.sql;

import java.util.ArrayList;
import java.util.List;

/**
 * The location of a parameter within a SQL statement
 * 
 * @author Tony
 *
 */
public class ParameterLocation {

	/**
	 * The position within the sql statement
	 */
	private List<Integer> sqlCharPositions;
	
	/**
	 * The parameter index as it relates to
	 * JDBC parameters
	 */
	private List<Integer> parameterIndexes;
	
	/**
	 * The parameter name
	 */
	private String parameterName;

	/**
	 * @param sqlCharPosition
	 * @param parameterIndex
	 * @param parameterName
	 */
	public ParameterLocation(String parameterName) {		
		this.parameterName = parameterName;
		
		this.sqlCharPositions = new ArrayList<Integer>();
		this.parameterIndexes = new ArrayList<Integer>();
	}

	/**
	 * @return the sqlCharPositions
	 */
	public List<Integer> getSqlCharPositions() {
		return sqlCharPositions;
	}

	/**
	 * @return the parameterIndexes
	 */
	public List<Integer> getParameterIndexes() {
		return parameterIndexes;
	}

	/**
	 * @return the parameterName
	 */
	public String getParameterName() {
		return parameterName;
	}
	
	/**
	 * @param position
	 */
	public void addSqlCharPosition(int position) {
		this.sqlCharPositions.add(position);
	}
	
	/**
	 * @param index
	 */
	public void addParameterIndex(int index) {
		this.parameterIndexes.add(index);
	}
	
	/**
	 * Adds all the indexes and sql positions from the other {@link ParameterLocation}.
	 * 
	 * @param other
	 */
	public void addParameterLocation(ParameterLocation other) {
		if ( ! this.parameterName.equals(other.parameterName)) {
			throw new IllegalArgumentException
				("The supplied ParameterLocation doesn't have the same parameterName: " + other.parameterName + " vs. " + this.parameterName);
		}
		
		this.sqlCharPositions.addAll(other.sqlCharPositions);
		this.parameterIndexes.addAll(other.parameterIndexes);
	}
}

