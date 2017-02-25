/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.frontend.parsers;

import static leola.frontend.tokens.LeolaTokenType.ARROW;
import static leola.frontend.tokens.LeolaTokenType.COLON;
import static leola.frontend.tokens.LeolaTokenType.COMMA;
import static leola.frontend.tokens.LeolaTokenType.DOT;
import static leola.frontend.tokens.LeolaTokenType.IDENTIFIER;
import static leola.frontend.tokens.LeolaTokenType.LEFT_PAREN;
import static leola.frontend.tokens.LeolaTokenType.RIGHT_BRACKET;
import static leola.frontend.tokens.LeolaTokenType.RIGHT_PAREN;
import static leola.frontend.tokens.LeolaTokenType.VAR_ARGS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import leola.ast.ASTAttributes;
import leola.ast.ASTNode;
import leola.ast.Expr;
import leola.ast.NamedParameterExpr;
import leola.frontend.LeolaParser;
import leola.frontend.Token;
import leola.frontend.tokens.LeolaErrorCode;
import leola.frontend.tokens.LeolaTokenType;
import leola.vm.util.Pair;

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


    // Synchronization set for the , token.
    protected static final EnumSet<LeolaTokenType> ARRAY_DECLARATION_SET =
        ExprParser.EXPR_START_SET.clone();
    static {
        ARRAY_DECLARATION_SET.add(COMMA);
        ARRAY_DECLARATION_SET.add(LeolaTokenType.RIGHT_BRACKET);
    };
    
    
    // Synchronization set for the , token.
    protected static final EnumSet<LeolaTokenType> PARAMETER_LIST_SET =
        ExprParser.EXPR_START_SET.clone();
    static {
        PARAMETER_LIST_SET.add(COMMA);
        PARAMETER_LIST_SET.add(RIGHT_PAREN);
    };
    
    /**
     * Parses a list of expressions as denoted by
     * 
     *
     * <pre>
     *   x,y // parses the x, y expressions
     * </pre>
     *  
     * @param parser
     * @param currentToken
     * @param commaDelimeter
     * @param endToken
     * @param isParameter
     * @return the expression list
     * @throws Exception
     */
    private static Expr[] parseExpressionList(StmtParser parser
                                             , Token currentToken
                                             , EnumSet<LeolaTokenType> commaDelimeter
                                             , LeolaTokenType endToken, boolean isParameter) throws Exception
    {
        ExprParser expressionParser = new ExprParser(parser, isParameter);

        Token token = parser.nextToken();  // consume opening (

        List<Expr> paramsNode = new ArrayList<Expr>();

        boolean isArrayExpanded = false;
        
        // Loop to parse each actual parameter.
        while (token.getType() != endToken) {
            Expr actualNode = expressionParser.parseExpr(token);
            paramsNode.add(actualNode);
                        
            /* Ensure we only have one array expansion in the parameter
             * listings
             */
            if(actualNode instanceof NamedParameterExpr) {
                NamedParameterExpr nExpr = (NamedParameterExpr)actualNode;
                if(nExpr.getValueExpr().hasFlag(ASTAttributes.IS_ARG_ARRAY_EXPAND)) {                       
                    if(isArrayExpanded) {
                        parser.throwParseError(token, LeolaErrorCode.INVALID_MULTI_ARGS_EXPANSION);
                    }
                    
                    isArrayExpanded = true;    
                }
            }
            else if(actualNode.hasFlag(ASTAttributes.IS_ARG_ARRAY_EXPAND)) {
                if(isArrayExpanded) {
                    parser.throwParseError(token, LeolaErrorCode.INVALID_MULTI_ARGS_EXPANSION);
                }
                
                isArrayExpanded = true;
            }

            token = parser.expectedTokens(commaDelimeter);
            LeolaTokenType tokenType = token.getType();

            // Look for the comma.
            if (tokenType == COMMA) {
                token = parser.nextToken();  // consume ,
            }
            else if (ExprParser.EXPR_START_SET.contains(tokenType)) {
                parser.throwParseError(token, LeolaErrorCode.MISSING_COMMA);
            }
            else if (tokenType != endToken) {
                token = parser.expectedTokens(ExprParser.EXPR_START_SET);
            }
        }

        token = parser.nextToken();  // consume closing )

        return paramsNode.toArray(new Expr[paramsNode.size()]);
    }
    
    /**
     * Parses the parameters to a function call or class instantiation.
     * 
     * <pre>
     *   function(x,y); // parses the x, y expressions
     * </pre>
     * 
     * @param parser
     * @param currentToken the current token.
     * @return the expression list
     * @throws Exception
     */
    public static Expr[] parseArgumentExpressions(StmtParser parser, Token currentToken) throws Exception {
        return parseExpressionList(parser, currentToken, PARAMETER_LIST_SET, RIGHT_PAREN, true);
    }
    
    /**
     * Parses an array declaration
     * 
     * <pre>
     *   var array = [x,y]; // parses the x, y expressions
     * </pre>
     * 
     * @param parser
     * @param currentToken the current token.
     * @return the expression list
     * @throws Exception
     */
    public static Expr[] parseArrayDeclaration(StmtParser parser
                                             , Token currentToken) throws Exception {
        return parseExpressionList(parser, currentToken, ARRAY_DECLARATION_SET, RIGHT_BRACKET, false);
    }
    
    

    /**
     * Parses the body of a Map during a map declaration.
     * 
     * <pre>
     *   var map = {
     *      x -> "hello",
     *      y -> "bye"
     *   }
     *  // This function parses the x->"hello",y->"bye" part 
     *  <pre>
     * 
     * @param parser
     * @param currentToken the current token.
     * @param commaDelimeter
     * @param endToken
     * @return the expression list
     * @throws Exception
     */
    public static List<Pair<Expr, Expr>> parseMapParameters(StmtParser parser
                                                     , Token currentToken
                                                     , EnumSet<LeolaTokenType> commaDelimeter
                                                     , LeolaTokenType endToken) throws Exception
    {
        ExprParser expressionParser = new ExprParser(parser);

        Token token = parser.nextToken();  // consume opening token

        List<Pair<Expr, Expr>> paramsNode = new ArrayList<Pair<Expr, Expr>>();

        // Loop to parse each actual parameter.
        while (token.getType() != endToken) {
            Pair<Expr, Expr> element = new Pair<Expr, Expr>();


            Expr key = expressionParser.parseExpr(token);
            element.setFirst(key);
            
            token = expressionParser.expectTokenNext(expressionParser.currentToken(), ARROW, LeolaErrorCode.MISSING_ARROW);

            Expr value = expressionParser.parseExpr(token);
            element.setSecond(value);

            paramsNode.add( element );

            token = parser.expectedTokens(commaDelimeter);
            LeolaTokenType tokenType = token.getType();

            // Look for the comma.
            if (tokenType == COMMA) {
                token = parser.nextToken();  // consume ,
            }
            else if (ExprParser.EXPR_START_SET.contains(tokenType)) {
                parser.throwParseError(token, LeolaErrorCode.MISSING_COMMA);
            }
            else if (tokenType != endToken) {
                token = parser.expectedTokens(ExprParser.EXPR_START_SET);
            }
        }

        token = parser.nextToken();  // consume closing )

        return paramsNode;
    }

    /**
     * Parses a parameter listings, for classes, functions and generators.
     * 
     * <pre>
     *   class Person(name, age); // this parses out the name, age list
     * </pre>
     *
     * @param parser
     * @param next the current token.
     * @return the expression list
     * @throws Exception
     */
    public static ParameterList parseParameterListings(LeolaParser parser, Token next) throws Exception {
        LeolaTokenType type = next.getType();

        /* If the is no left brace, fail */
        if ( ! type.equals(LEFT_PAREN)) {
            parser.throwParseError(next, LeolaErrorCode.MISSING_LEFT_PAREN);
        }

        next = parser.nextToken(); // consume the (
        type = next.getType();

        ParameterList parameters = new ParameterList();

        boolean needsComma = false;
        boolean isVarargs = false;
        boolean isIdentifier = false;
        
        while(PARAM_OPS.contains(type)) {
            if ( type.equals(IDENTIFIER)) {
                String paramName = next.getText();
                parameters.addParameter(paramName);

                next = parser.nextToken();
                type = next.getType();

                needsComma = true;
                isIdentifier = true;
            }
            else if( type.equals(VAR_ARGS)) {
                if(!isIdentifier) {
                    parser.throwParseError(next, LeolaErrorCode.INVALID_VAR_ARGS_START);    
                }
                if(parameters.isVarargs()) {
                    parser.throwParseError(next, LeolaErrorCode.INVALID_MULTI_VAR_ARGS);
                }
                
                next = parser.nextToken();
                type = next.getType();
                

                parameters.setVarargs(true);
                
                isIdentifier = false;
                needsComma = false;
                isVarargs = true;
            }
            else if ( type.equals(COMMA)) {
                next = parser.nextToken();
                type = next.getType();

                needsComma = false;
                isIdentifier = false;
            }
            
            

            Token currentToken = parser.currentToken();
            if ( currentToken.getType().equals(RIGHT_PAREN)) {
                needsComma = false;
            }
            else if(isVarargs) {
                parser.throwParseError(next, LeolaErrorCode.INVALID_VAR_ARGS);
            }

            if ( ! PARAM_OPS.contains(type) && needsComma ) {
                parser.throwParseError(next, LeolaErrorCode.MISSING_COMMA);
            }
        }

        if ( !type.equals(RIGHT_PAREN)) {
            parser.throwParseError(next, LeolaErrorCode.MISSING_RIGHT_PAREN);
        }

        next = parser.nextToken(); // eat the )


        return parameters;
    }

    /**
     * Parses the class name, generally this is used when a set of class names are expected.
     *
     * @param parser
     * @param token the current token.
     * @param endTokenTypes 
     * @return the class name
     * @throws Exception
     */
    public static String parseClassName(LeolaParser parser, Token token, LeolaTokenType ... endTokenTypes) throws Exception {
        EnumSet<LeolaTokenType> quitSet = EnumSet.copyOf(Arrays.asList(endTokenTypes));


        String className = "";

        LeolaTokenType type = token.getType();
        while(! quitSet.contains(type) ) {
            if ( type.equals(IDENTIFIER) ) {
                className += token.getText();
            }
            else if ( type.equals(DOT)) {
                className += ".";
            }
            else if ( type.equals(COLON)) {
                className += ":";
            }
            else {
                parser.throwParseError(token, LeolaErrorCode.UNEXPECTED_TOKEN);
            }

            token = parser.nextToken();
            type = token.getType();
        }

        return className;
    }

    /**
     * Parses a single class name
     *  
     * @param parser
     * @param token
     * @return the class name
     * @throws Exception
     */
    public static String parseClassName(LeolaParser parser, Token token) throws Exception {
        EnumSet<LeolaTokenType> continueSet = EnumSet.copyOf(Arrays.asList(IDENTIFIER, DOT, COLON));

        String className = "";

        LeolaTokenType type = token.getType();
        while( continueSet.contains(type) ) {
            if ( type.equals(IDENTIFIER) ) {
                className += token.getText();
            }
            else if ( type.equals(DOT)) {
                className += ".";
            }
            else if ( type.equals(COLON)) {
                className += ":";
            }
            else {
                parser.throwParseError(token, LeolaErrorCode.UNEXPECTED_TOKEN);
            }

            token = parser.nextToken();
            type = token.getType();
        }

        return className;
    }
    
    
}

