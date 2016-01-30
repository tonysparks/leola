/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.lang.sql;

import java.sql.Connection;
import java.sql.Savepoint;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import leola.vm.types.LeoMap;
import leola.vm.types.LeoNativeClass;
import leola.vm.types.LeoObject;

/**
 * Represents a DatabaseConnection
 * @author Tony
 *
 */
public class Conn {

	/**
	 * SQL connection
	 */
	private Connection sqlConn;
	private Map<String, Savepoint> savepoints;

	private LeoObject thisObject;
	/**
	 * @param sqlConn
	 */
	public Conn(Connection sqlConn) throws Exception {
		super();
		this.sqlConn = sqlConn;
		this.thisObject = new LeoNativeClass(this);
		
		this.sqlConn.setAutoCommit(false);
		this.savepoints = new ConcurrentHashMap<String, Savepoint>();
	}
	
	/**
	 * @return the sqlConn
	 */
	public Connection jdbc() {
		return sqlConn;
	}
	
	public boolean isOpen() throws Exception {
		return ! this.sqlConn.isClosed();
	}
	
	public void close() throws Exception {
		if ( ! this.sqlConn.isClosed() ) {
			this.sqlConn.close();
		}
	}
	
	/**
	 * Executes the supplied function and will always close out the connection after executing it.
	 * 
	 * @param function
	 * @return anything returned from the supplied function
	 * @throws Exception
	 */
	public LeoObject with(LeoObject function) throws Exception {
	    try {
	        return function.call(LeoObject.valueOf(this));
	    }
	    finally {
	        close();
	    }
	}
		
	public void commit() throws Exception {
		this.sqlConn.commit();
	}
	
	public void rollback() throws Exception {
		this.sqlConn.rollback();
	}
	
	public void rollbackTo(LeoObject savepoint) throws Exception {
		String sp = savepoint.toString();
		
		if ( ! this.savepoints.containsKey(sp) ) {
			throw new IllegalArgumentException("No savepoint defined for: " + sp);
		}
		
		this.sqlConn.rollback(this.savepoints.get(sp));
	}
	
	public void savepoint(LeoObject savepoint) throws Exception {
		String sp = savepoint.toString();
		this.savepoints.put(sp, this.sqlConn.setSavepoint(sp));
	}
	
	public void releaseSavepoint(LeoObject savepoint) throws Exception {
		String sp = savepoint.toString();
		
		if ( ! this.savepoints.containsKey(sp) ) {
			throw new IllegalArgumentException("No savepoint defined for: " + sp);
		}
		
		this.sqlConn.releaseSavepoint(this.savepoints.get(sp));
	}
	
	public Query query(LeoObject sql) {
		String query = sql.toString();
		ParsedSql parsedSql = SqlParameterParser.parseSqlStatement(query);				
		return new Query(this.sqlConn, parsedSql);	
	}
	
	/**
	 * Streams the response.
	 * 
	 * @param function
	 * @param sql
	 * @param params
	 * @param pageSize
	 * @throws Exception
	 */
	public void streamExecute(LeoObject function, LeoObject sql, LeoMap params, Integer pageSize) throws Exception {
		Query aQuery = query(sql);
		aQuery.params(params);
				
		Savepoint savepoint = this.sqlConn.setSavepoint();
		try {								
			aQuery.streamExecute(function, pageSize);
			this.sqlConn.commit();
		}
		catch(Exception t) {
			this.sqlConn.rollback(savepoint);
			throw t;
		}			
		finally {
			aQuery.close();
		}
	}
	
	/**
	 * Executes a read-only query.
	 * 
	 * @param sql
	 * @param params
	 * @return the result set
	 * @throws Exception
	 */
	public LeoObject execute(LeoObject sql, LeoMap params, Integer maxResults) throws Exception {
		Query aQuery = query(sql);
		aQuery.params(params);
		
		if(maxResults != null) {
			aQuery.setMaxResults(maxResults);
		}

		LeoObject result = null;
		Savepoint savepoint = this.sqlConn.setSavepoint();
		try {								
			result = aQuery.execute();
			this.sqlConn.commit();
		}
		catch(Exception t) {
			this.sqlConn.rollback(savepoint);
			throw t;
		}
		finally {
			aQuery.close();
		}
								
		return result;	
	}
	
	/**
	 * Executes a update statement
	 * @param sql
	 * @param params
	 * @return the number of rows updated
	 * @throws Exception
	 */
	public int update(LeoObject sql, LeoMap params) throws Exception {
		Query aQuery = query(sql);
		aQuery.params(params);

		int result = 0;
		Savepoint savepoint = this.sqlConn.setSavepoint();
		try {								
			result = aQuery.update();
			this.sqlConn.commit();
		}
		catch(Exception t) {
			this.sqlConn.rollback(savepoint);
			throw t;
		}
		finally {
			aQuery.close();
		}
								
		return result;	
	}
	
	/**
	 * Executes a block of code around a transaction.
	 * 
	 * @param interpreter
	 * @param func
	 * @return
	 * @throws Exception
	 */
	public LeoObject transaction(LeoObject func) throws Exception {
		LeoObject result = null;
		Savepoint savepoint = this.sqlConn.setSavepoint();
		try {			
			result = func.xcall(this.thisObject);
			this.sqlConn.commit();
		}
		catch(Exception t) {
			this.sqlConn.rollback(savepoint);
			throw t;
		}
								
		return result;
	}
}

