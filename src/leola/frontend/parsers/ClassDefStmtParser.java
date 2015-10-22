/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.frontend.parsers;

import java.util.ArrayList;
import java.util.List;

import leola.ast.ASTNode;
import leola.ast.ClassDeclStmt;
import leola.ast.Expr;
import leola.ast.Stmt;
import leola.frontend.LeolaParser;
import leola.frontend.Token;
import leola.frontend.tokens.LeolaErrorCode;
import leola.frontend.tokens.LeolaTokenType;

/**
 * @author Tony
 *
 */
public class ClassDefStmtParser extends StmtParser {
	
	/**
	 * @param parser
	 */
	public ClassDefStmtParser(LeolaParser parser) {
		super(parser);
	}

	/* (non-Javadoc)
	 * @see leola.frontend.parsers.StmtParser#parse(leola.frontend.Token)
	 */
	@Override
	public ASTNode parse(Token token) throws Exception {
	    Token startingToken = token;
		token = this.nextToken(); // eat the CLASS token
		
		LeolaTokenType type = token.getType();		
		expectToken(token, LeolaTokenType.IDENTIFIER, LeolaErrorCode.MISSING_IDENTIFIER);
		
		String className = token.getText();
		token = this.nextToken();
		type = token.getType();
				
		/* parse the parameter listings */
		ParameterList classParams = ParserUtils.parseParameterListings(this, token);
		
		String parentClassName = null;
		Expr[] parentClassParams = null;
		
		token = currentToken();
		type = token.getType();
		
		/* This is extending another class */
		if ( type.equals(LeolaTokenType.IS)) {
			token = nextToken(); // eat the IS
			 
			parentClassName = ParserUtils.parseClassName(this, token, LeolaTokenType.LEFT_PAREN);
			parentClassParams = ParserUtils.parseArgumentExpressions(this, currentToken());	

			token = currentToken();
			type = token.getType();
		}
		
		String[] interfaceNames = null;
		
		/* This is implementing some interfaces */ 
		if ( type.equals(LeolaTokenType.COLON) ) {
			token = nextToken(); // eat the colon
			type = token.getType();
			
			List<String> interfaceNameList = new ArrayList<String>(3);
			while(!type.equals(LeolaTokenType.LEFT_BRACE) || 
				   type.equals(LeolaTokenType.END_OF_FILE)) {				
				
				String interfaceName = ParserUtils.parseClassName(this, token
																 , LeolaTokenType.COMMA
																 , LeolaTokenType.LEFT_BRACE);
				if ( interfaceName == null ||
					 interfaceName.equals("")) {					
					throwParseError(token, LeolaErrorCode.MISSING_INTERFACE);
				}
				
				interfaceNameList.add(interfaceName);
				
				token = currentToken();
				type = token.getType();
			}
			
			interfaceNames = interfaceNameList.toArray(new String[0]);
		}
				
		
		/* now parse the body */
		StmtParser parser = new StmtParser(this);
		Stmt classBodyStmt = (Stmt)parser.parse(currentToken());
		
		ClassDeclStmt classDefStmt = new ClassDeclStmt(className, classParams, classBodyStmt
													 , parentClassName, parentClassParams, interfaceNames);
		
		setLineNumber(classDefStmt, startingToken);
		
		return classDefStmt;
	}
}

