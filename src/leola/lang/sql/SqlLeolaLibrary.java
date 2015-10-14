/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.lang.sql;

import java.sql.DriverManager;

import leola.vm.Leola;
import leola.vm.exceptions.LeolaRuntimeException;
import leola.vm.lib.LeolaIgnore;
import leola.vm.lib.LeolaLibrary;
import leola.vm.types.LeoNamespace;
import leola.vm.types.LeoObject;

/**
 * Handles all of the SQL/Persistence stuff
 * 
 * @author Tony
 *
 */
public class SqlLeolaLibrary implements LeolaLibrary {

	private Leola runtime;
	
	/* (non-Javadoc)
	 * @see leola.frontend.LeolaLibrary#init(leola.frontend.Leola)
	 */
	@Override
	@LeolaIgnore
	public void init(Leola leola, LeoNamespace namespace) throws LeolaRuntimeException {
		this.runtime = leola;
		this.runtime.putIntoNamespace(this, namespace);
	}

	/**
	 * Sets the driver
	 * 
	 * @param driver
	 * @throws Exception
	 */
	public void driver(LeoObject driver) throws Exception {
		Class.forName(driver.toString());
	}
	
	public void usedb2() throws Exception {
//		runtime.getResourceLoader().include ("db2jcc.jar");
//		runtime.getResourceLoader().include ("db2jcc_license_cu.jar");
		Class.forName("com.ibm.db2.jcc.DB2Driver");
	}
	
	/**
	 * Connects to a database.
	 * 
	 * @param url
	 * @param username
	 * @param pw
	 * @return the {@link Conn} object which represents the database connection
	 * @throws Exception
	 */
	public Conn connect(LeoObject url, LeoObject username, LeoObject pw) throws Exception {		
		return new Conn(DriverManager.getConnection(url.toString(), username.toString(), pw.toString()));		
	}
}

