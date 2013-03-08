/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.frontend.parsers;

import static leola.frontend.tokens.LeolaTokenType.COMMA;
import static leola.frontend.tokens.LeolaTokenType.RIGHT_PAREN;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import leola.ast.ASTNode;
import leola.ast.Expr;
import leola.ast.FuncInvocationExpr;
import leola.frontend.LeolaParser;
import leola.frontend.Token;
import leola.frontend.tokens.LeolaErrorCode;
import leola.frontend.tokens.LeolaTokenType;



/**
 * @author Tony
 *
 */
public class FuncInvocationParser extends ExprParser {

    // Synchronization set for the , token.
    protected static final EnumSet<LeolaTokenType> COMMA_SET =
        ExprParser.EXPR_START_SET.clone();
    static {
        COMMA_SET.add(COMMA);
        COMMA_SET.add(RIGHT_PAREN);
    };
	
	/**
	 * @param parser
	 */
	public FuncInvocationParser(LeolaParser parser) {
		super(parser);
	}
	
	/* (non-Javadoc)
	 * @see leola.frontend.parsers.ExprParser#parse(leola.frontend.Token)
	 */
	@Override
	public ASTNode parse(Token token) throws Exception {
		String functionName = token.getText();
		Expr[] params = parseActualParameters(token);
		FuncInvocationExpr expr = new FuncInvocationExpr(functionName, params);
		setLineNumber(expr, currentToken());
		return expr;
	}

	/**
     * Parse the actual parameters of a procedure or function call.
     * @param token the current token.
     */
    protected Expr[] parseActualParameters(Token currentToken) throws Exception
    {
        ExprParser expressionParser = new ExprParser(this);

        Token token = this.nextToken();  // consume opening (

        List<Expr> paramsNode = new ArrayList<Expr>();
        
        // Loop to parse each actual parameter.
        while (token.getType() != RIGHT_PAREN) {
            ASTNode actualNode = expressionParser.parse(token);                       
            paramsNode.add( (Expr)actualNode);
            
            token = synchronize(COMMA_SET);
            LeolaTokenType tokenType = token.getType();

            // Look for the comma.
            if (tokenType == COMMA) {
                token = nextToken();  // consume ,
            }
            else if (ExprParser.EXPR_START_SET.contains(tokenType)) {
                getExceptionHandler().errorToken(token, this, LeolaErrorCode.MISSING_COMMA);
            }
            else if (tokenType != RIGHT_PAREN) {
                token = synchronize(ExprParser.EXPR_START_SET);
            }
        }

        token = nextToken();  // consume closing )

        return paramsNode.toArray(new Expr[0]);
    }
}

