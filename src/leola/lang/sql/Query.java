/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.lang.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import leola.vm.Leola;
import leola.vm.types.LeoArray;
import leola.vm.types.LeoMap;
import leola.vm.types.LeoObject;
import leola.vm.types.LeoObject.LeoType;
import leola.vm.types.LeoString;
import leola.vm.util.LeoTypeConverter;

/**
 * @author Tony
 *
 */
public class Query {

	private Connection conn;
	private PreparedStatement stmt;
	private ParsedSql parsedSql;
	private boolean isPrepared;

	private Leola runtime;
	/**
	 * @param conn
	 * @param parsedSql
	 */
	public Query(Leola runtime, Connection conn, ParsedSql parsedSql) {
		super();
		this.runtime = runtime;
		this.conn = conn;
		this.parsedSql = parsedSql;
		
		this.isPrepared = ! this.parsedSql.getNamedParameters().isEmpty();
	}
	
	public PreparedStatement statement() throws Exception {
		if ( this.stmt == null ) {
			String jdbcStatement = this.parsedSql.toJdbcStatement().toString();		
			this.stmt = this.conn.prepareStatement(jdbcStatement);
		}
		return this.stmt;
	}
	
	public Query params(LeoMap leoparams) throws Exception {
		if(LeoObject.isTrue(leoparams)) {
			Map<LeoObject,LeoObject> map = leoparams.getMap();
			
			Map<String, LeoObject> params = new HashMap<String, LeoObject>(map.size());
			for(Map.Entry<LeoObject, LeoObject> entry : map.entrySet()) {
				String paramName = entry.getKey().toString();
				params.put(paramName, entry.getValue());
			}
							
			setParameters(statement(), params);
		}
		return this;
	}
	
	private void setParameters(PreparedStatement stmt, Map<String, LeoObject> params) throws Exception {
		if ( this.isPrepared ) {							
			Map<String, ParameterLocation> paramLocations = this.parsedSql.getNamedParams();
			for(Map.Entry<String, ParameterLocation> entry : paramLocations.entrySet()) {
				String paramName = entry.getKey();
				ParameterLocation loc= entry.getValue();
				
				if (! params.containsKey(paramName)) {
					throw new IllegalArgumentException("No parameter defined for: " + paramName);
				}
				
				LeoObject value = params.get(paramName);
				for(int i : loc.getParameterIndexes()) {
					if ( value.isOfType(LeoType.NULL)) {
						stmt.setNull(i, Types.NULL);
					}
					else {
						stmt.setObject(i, value.getValue());
					}
				}
			}						
		}		
	}
	
	public void close() throws Exception {
		if ( this.stmt != null ) {
			this.stmt.close();
		}
	}
	
	public void setMaxResults(int maxResults) throws SQLException {		
		this.stmt.setMaxRows(maxResults);		
	}
	
	/**
	 * Executes a Read Only Search
	 * @return
	 * @throws Exception
	 */
	public LeoArray execute() throws Exception {				
		LeoArray result = null;	
		ResultSet set = null;
		
		PreparedStatement stmt = statement();
		try {			
			set = stmt.executeQuery();		
			result = convertResultSet(set);
		}
		finally {
			if ( set != null ) {
				set.close();
			}
		}
		
		return result;		
	}
	
	/**
	 * Streams the result set invoking the supplied function every fetchSize.
	 * @param function
	 * @param fetchSize
	 * @throws Exception
	 */
	public void streamExecute(LeoObject function, Integer fetchSize) throws Exception {
			
		ResultSet set = null;
		
		PreparedStatement stmt = statement();
		try {			
			set = stmt.executeQuery();		
			streamResultSet(function, set, fetchSize);
		}
		finally {
			if ( set != null ) {
				set.close();
			}
		}							
	}
	
	/**
	 * Executes an update
	 * @return
	 * @throws Exception
	 */
	public int update() throws Exception {
		int result = 0;	
		ResultSet set = null;
		
		PreparedStatement stmt = statement();
		try {			
			result = stmt.executeUpdate();		
		}
		finally {
			if ( set != null ) {
				set.close();
			}
		}
		
		return result;
	}

	private void safeExecuteFunction(LeoObject function, LeoObject arg1) {
		try {
			this.runtime.execute(function, arg1);
		}
		catch (Exception e) {
			System.err.println(e);
		}
	}
	
	/**
	 * Converts the result set
	 * 
	 * @param set
	 * @return
	 * @throws Exception
	 */
	private void streamResultSet(LeoObject function, ResultSet set, Integer fetchSize) throws Exception {
		ResultSetMetaData meta = set.getMetaData();
		int numOfColumns = meta.getColumnCount();
		
		LeoArray result = new LeoArray();
		
		final int pageSize = (fetchSize != null) ? fetchSize : 100;
		int currentSize = 0;
		while(set.next()) {
			LeoMap row = new LeoMap();
			
			for(int i = 1; i <= numOfColumns; i++) {
				Object obj = set.getObject(i);
				row.put( LeoString.valueOf( meta.getColumnName(i)/*.toLowerCase()*/ ),
						 LeoTypeConverter.convertToLeolaType(obj));
			}
			
			result.$add(row);
			currentSize++;
			
			if(currentSize >= pageSize) {
				safeExecuteFunction(function, result);
				currentSize = 0;
				result = new LeoArray();
			}
		}				
		
		if(currentSize > 0) {
			safeExecuteFunction(function, result);
		}
	}
	
	/**
	 * Converts the result set
	 * 
	 * @param set
	 * @return
	 * @throws Exception
	 */
	private LeoArray convertResultSet(ResultSet set) throws Exception {
		ResultSetMetaData meta = set.getMetaData();
		int numOfColumns = meta.getColumnCount();
		
		LeoArray result = new LeoArray();
		while(set.next()) {
			LeoMap row = new LeoMap();
			
			for(int i = 1; i <= numOfColumns; i++) {
				Object obj = set.getObject(i);
				row.put( LeoString.valueOf( meta.getColumnName(i)/*.toLowerCase()*/ ),
						 LeoTypeConverter.convertToLeolaType(obj));
			}
			
			result.$add(row);
		}
		
		return result;
	}
	
}

