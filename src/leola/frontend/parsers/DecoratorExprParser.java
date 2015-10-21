/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.frontend.parsers;

import leola.ast.ASTNode;
import leola.ast.DecoratorExpr;
import leola.ast.Expr;
import leola.frontend.LeolaParser;
import leola.frontend.Token;
import leola.frontend.tokens.LeolaErrorCode;
import leola.frontend.tokens.LeolaTokenType;

/**
 * Responsible for parsing a decorated expression:
 * 
 * <pre>
 *  @ identifier [ '(' parameterList ')' ] expr
 * </pre>
 * 
 * @author Tony
 *
 */
public class DecoratorExprParser extends ExprParser {

	
	/**
	 * @param parser
	 */
	public DecoratorExprParser(LeolaParser parser) {
		super(parser);
	}
	
	/* (non-Javadoc)
	 * @see leola.frontend.parsers.StmtParser#parse(leola.frontend.Token)
	 */
	@Override
	public ASTNode parse(Token token) throws Exception {
	    Token startingToken = token;
		token = nextToken();  // consume the @
				
		expectToken(token, LeolaTokenType.IDENTIFIER, LeolaErrorCode.MISSING_IDENTIFIER);
		
		// parse out the decorator identifier
		String decoratorName = token.getText();
		
		token = nextToken();  
		
		Expr[] parameters = null;
		
		// now parse out any decorator parameters (if there are any)
		if(token.getType().equals(LeolaTokenType.LEFT_PAREN)) {
		    parameters = ParserUtils.parseArgumentExpressions(this, token);
		}
		
		// finally, lets now parse the decorated expression
        ExprParser expressionParser = new ExprParser(this);
        Expr exprNode = (Expr)expressionParser.parse(currentToken());
        
        DecoratorExpr decoratorExpr = new DecoratorExpr(decoratorName, parameters, exprNode);
        
        setLineNumber(decoratorExpr, startingToken);
        return decoratorExpr;
	}

}

