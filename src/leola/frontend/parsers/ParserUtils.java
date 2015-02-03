/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.frontend.parsers;

import static leola.frontend.tokens.LeolaTokenType.COMMA;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import leola.ast.ASTNode;
import leola.ast.Expr;
import leola.frontend.Parser;
import leola.frontend.Token;
import leola.frontend.tokens.LeolaErrorCode;
import leola.frontend.tokens.LeolaTokenType;
import leola.vm.asm.Pair;

/**
 * Parsing rules that do not return an {@link ASTNode}
 *
 * @author Tony
 *
 */
public class ParserUtils {

    // Set of additive operators.
    private static final EnumSet<LeolaTokenType> PARAM_OPS =
        EnumSet.of(LeolaTokenType.COMMA, LeolaTokenType.VAR_ARGS, LeolaTokenType.IDENTIFIER); // do to allow expr


    /**
     * Parse the actual parameters of a procedure or function call.
     * @param token the current token.
     */
    public static Expr[] parseActualParameters(StmtParser parser
    										 , Token currentToken
    										 , EnumSet<LeolaTokenType> commaDelimeter
    										 , LeolaTokenType endToken) throws Exception
    {
        ExprParser expressionParser = new ExprParser(parser);

        Token token = parser.nextToken();  // consume opening (

        List<Expr> paramsNode = new ArrayList<Expr>();

        // Loop to parse each actual parameter.
        while (token.getType() != endToken) {
            ASTNode actualNode = expressionParser.parse(token);
            paramsNode.add( (Expr)actualNode);

            token = parser.synchronize(commaDelimeter);
            LeolaTokenType tokenType = token.getType();

            // Look for the comma.
            if (tokenType == COMMA) {
                token = parser.nextToken();  // consume ,
            }
            else if (ExprParser.EXPR_START_SET.contains(tokenType)) {
            	parser.getExceptionHandler().errorToken(token, parser, LeolaErrorCode.MISSING_COMMA);
            }
            else if (tokenType != endToken) {
                token = parser.synchronize(ExprParser.EXPR_START_SET);
            }
        }

        token = parser.nextToken();  // consume closing )

        return paramsNode.toArray(new Expr[0]);
    }

    /**
     * Parse the actual parameters of a procedure or function call.
     * @param token the current token.
     */
    public static List<Pair<Expr, Expr>> parseMapParameters(StmtParser parser
                                                     , Token currentToken
                                                     , EnumSet<LeolaTokenType> commaDelimeter
                                                     , LeolaTokenType endToken) throws Exception
    {
        ExprParser expressionParser = new ExprParser(parser, true);

        Token token = parser.nextToken();  // consume opening token

        List<Pair<Expr, Expr>> paramsNode = new ArrayList<Pair<Expr, Expr>>();

        // Loop to parse each actual parameter.
        while (token.getType() != endToken) {
            Pair<Expr, Expr> element = new Pair<Expr, Expr>();


            ASTNode key = expressionParser.parse(token);
            element.setFirst( (Expr)key );

            token = expressionParser.currentToken();
            if ( token.getType() != LeolaTokenType.ARROW ) {
                parser.getExceptionHandler().errorToken(token, parser, LeolaErrorCode.MISSING_ARROW);
            }
            else {
                token = expressionParser.nextToken(); // eat the Arrow
            }

            ASTNode value = expressionParser.parse(token);
            element.setSecond( (Expr)value );

            paramsNode.add( element );

            token = parser.synchronize(commaDelimeter);
            LeolaTokenType tokenType = token.getType();

            // Look for the comma.
            if (tokenType == COMMA) {
                token = parser.nextToken();  // consume ,
            }
            else if (ExprParser.EXPR_START_SET.contains(tokenType)) {
                parser.getExceptionHandler().errorToken(token, parser, LeolaErrorCode.MISSING_COMMA);
            }
            else if (tokenType != endToken) {
                token = parser.synchronize(ExprParser.EXPR_START_SET);
            }
        }

        token = parser.nextToken();  // consume closing )

        return paramsNode;
    }

    /**
	 * Parses a parameter listings
	 *
	 * @param next
	 * @return
	 * @throws Exception
	 */
	public static ParameterList parseParameterListings(Parser parser, Token next) throws Exception {
		LeolaTokenType type = next.getType();

		/* If the is no left brace, fail */
		if ( ! type.equals(LeolaTokenType.LEFT_PAREN)) {
			parser.getExceptionHandler().errorToken(next, parser, LeolaErrorCode.MISSING_LEFT_PAREN);
		}

		next = parser.nextToken(); // consume the (
		type = next.getType();

		ParameterList parameters = new ParameterList();

		boolean needsComma = false;
		boolean isVarargs = false;
		boolean isIdentifier = false;
		
		while(PARAM_OPS.contains(type)) {
			if ( type.equals(LeolaTokenType.IDENTIFIER)) {
				String paramName = next.getText();
				parameters.addParameter(paramName);

				next = parser.nextToken();
				type = next.getType();

				needsComma = true;
				isIdentifier = true;
			}
			else if( type.equals(LeolaTokenType.VAR_ARGS)) {
				if(!isIdentifier) {
					parser.getExceptionHandler().errorToken(next, parser, LeolaErrorCode.INVALID_VAR_ARGS_START);	
				}
				
				next = parser.nextToken();
				type = next.getType();

				parameters.setVarargs(true);
				
				isIdentifier = false;
				needsComma = false;
				isVarargs = true;
			}
			else if ( type.equals(LeolaTokenType.COMMA)) {
				next = parser.nextToken();
				type = next.getType();

				needsComma = false;
				isIdentifier = false;
			}
			
			

			Token currentToken = parser.currentToken();
			if ( currentToken.getType().equals(LeolaTokenType.RIGHT_PAREN)) {
				needsComma = false;
			}
			else if(isVarargs) {
				parser.getExceptionHandler().errorToken(next, parser, LeolaErrorCode.INVALID_VAR_ARGS);
			}

			if ( ! PARAM_OPS.contains(type) && needsComma ) {
				parser.getExceptionHandler().errorToken(next, parser, LeolaErrorCode.MISSING_COMMA);
			}
		}

		if ( !type.equals(LeolaTokenType.RIGHT_PAREN)) {
			parser.getExceptionHandler().errorToken(next, parser, LeolaErrorCode.MISSING_RIGHT_PAREN);
		}

		next = parser.nextToken(); // eat the )


		return parameters;
	}

	/**
	 * Parses the class name.
	 *
	 * @param token
	 * @return
	 * @throws Exception
	 */
	public static String parseClassName(Parser parser, Token token, LeolaTokenType ... endTokenTypes) throws Exception {
		EnumSet<LeolaTokenType> quitSet = EnumSet.copyOf(Arrays.asList(endTokenTypes));


		String className = "";

		LeolaTokenType type = token.getType();
		while(! quitSet.contains(type) ) {
			if ( type.equals(LeolaTokenType.IDENTIFIER) ) {
				className += token.getText();
			}
			else if ( type.equals(LeolaTokenType.DOT)) {
				className += ".";
			}
			else if ( type.equals(LeolaTokenType.COLON)) {
				className += ":";
			}
			else {
				parser.getExceptionHandler().errorToken(token, parser, LeolaErrorCode.UNEXPECTED_TOKEN);
			}

			token = parser.nextToken();
			type = token.getType();
		}

		return className;
	}

	public static String parseClassName(Parser parser, Token token) throws Exception {
		EnumSet<LeolaTokenType> quitSet = EnumSet.copyOf(Arrays.asList(LeolaTokenType.IDENTIFIER
																	 , LeolaTokenType.DOT
																	 , LeolaTokenType.COLON));


		String className = "";

		LeolaTokenType type = token.getType();
		while( quitSet.contains(type) ) {
			if ( type.equals(LeolaTokenType.IDENTIFIER) ) {
				className += token.getText();
			}
			else if ( type.equals(LeolaTokenType.DOT)) {
				className += ".";
			}
			else if ( type.equals(LeolaTokenType.COLON)) {
				className += ":";
			}
			else {
				parser.getExceptionHandler().errorToken(token, parser, LeolaErrorCode.UNEXPECTED_TOKEN);
			}

			token = parser.nextToken();
			type = token.getType();
		}

		return className;
	}
	
	
}

