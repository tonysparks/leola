/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.frontend.parsers;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import leola.ast.ASTNode;
import leola.ast.ClassDeclStmt;
import leola.ast.Expr;
import leola.ast.Stmt;
import leola.frontend.EvalException;
import leola.frontend.LeolaParser;
import leola.frontend.Token;
import leola.frontend.tokens.LeolaTokenType;

/**
 * @author Tony
 *
 */
public class ClassDefStmtParser extends StmtParser {

    // Set of additive operators.
//    private static final EnumSet<LeolaTokenType> CLASS_HEADER =
//        EnumSet.of(LeolaTokenType.COLON, LeolaTokenType.COMMA, LeolaTokenType.IDENTIFIER);
	
    // Synchronization set for the , token.
    protected static final EnumSet<LeolaTokenType> COMMA_SET = ArrayDeclExprParser.COMMA_SET.clone();        
    static {        
        COMMA_SET.add(LeolaTokenType.RIGHT_PAREN);
    };
	
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
		token = this.nextToken(); // eat the CLASS token
		
		LeolaTokenType type = token.getType();
		if ( ! type.equals(LeolaTokenType.IDENTIFIER) ) {
			throw new EvalException("No class name defined!");
		}
		
		String className = token.getText();
		token = this.nextToken();
		type = token.getType();
				
		/* parse the parameter listings */
		String[] classParams = ParserUtils.parseParameterListings(this, token);
		
		String parentClassName = null;
		Expr[] parentClassParams = null;
		
		token = currentToken();
		type = token.getType();
		
		/* This is extending another class */
		if ( type.equals(LeolaTokenType.IS)) {
			token = nextToken(); // eat the IS
			 
			parentClassName = ParserUtils.parseClassName(this, token, LeolaTokenType.LEFT_PAREN);
			parentClassParams = ParserUtils.parseActualParameters(this, currentToken()
												, COMMA_SET, LeolaTokenType.RIGHT_PAREN);	

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
					throw new EvalException("Illegal use of interface token ':'.  You must specify an interface to implement from.");
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
		
		setLineNumber(classDefStmt, currentToken());
		
		return classDefStmt;
	}
}

