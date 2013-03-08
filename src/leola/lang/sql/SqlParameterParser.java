/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.lang.sql;

import java.util.HashSet;
import java.util.Set;

/**
 * Parses sql parameters to and from JDBC style parameters.
 * 
 * 
 * @author spring framework
 * 
 */
public class SqlParameterParser {

	/** Flag for Parameterized token */
	private final static char PARAMETER_TOKEN = ':';
	/** JDBC Parameter type token */
	//private final static char JDBC_TOKEN = '?';

	/**
	 * Set of characters that qualify as parameter separators,
	 * indicating that a parameter name in a SQL String has ended.
	 */
	private static final char[] PARAMETER_SEPARATORS =
			{'"', '\'', ':', '&', ',', ';', '(', ')', '|', '=', '+', '-', '*', '%', '/', '\\', '<', '>', '^'};

	/**
	 * Parse the SQL statement and locate any placeholders or named parameters.
	 * Named parameters are substituted for a JDBC placeholder.
	 * @param sql the SQL statement
	 * @return the parsed statement, represented as ParsedSql instance
	 */
	public static ParsedSql parseSqlStatement(String sql) {
			Set<String> namedParameters = new HashSet<String>();
			ParsedSql parsedSql = new ParsedSql(sql);
						
			char[] statement = sql.toCharArray();
			boolean withinQuotes = false;
			char currentQuote = '-';
			
			int i = 0;
			while (i < statement.length) {
				char c = statement[i];
				if (withinQuotes) {
					if (c == currentQuote) {
						withinQuotes = false;
						currentQuote = '-';
					}
				}
				else {
					if (c == '"' || c == '\'') {
						withinQuotes = true;
						currentQuote = c;
					}
					else {
						if (c == PARAMETER_TOKEN || c == '&') {
							int j = i + 1;
							if (j < statement.length && statement[j] == PARAMETER_TOKEN && c == PARAMETER_TOKEN) {
								// Postgres-style "::" casting operator - to be skipped.
								i = i + 2;
								continue;
							}
							while (j < statement.length && !isParameterSeparator(statement[j])) {
								j++;
							}
							if (j - i > 1) {
								String parameter = sql.substring(i + 1, j);
								if (!namedParameters.contains(parameter)) {
									namedParameters.add(parameter);
									
								}
								parsedSql.addNamedParameter(parameter, i, j);
								
								
							}
							i = j - 1;
						}
					}
				}
				i++;
			}
			return parsedSql;
	}

	/**
	 * @param c
	 * @return
	 */
	private static boolean isParameterSeparator(char c) {
		if (Character.isWhitespace(c)) {
			return true;
		}
		for (int i = 0; i < PARAMETER_SEPARATORS.length; i++) {
			if (c == PARAMETER_SEPARATORS[i]) {
				return true;
			}
		}
		return false;
	}

}

