/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.frontend.parsers;

import static leola.frontend.tokens.LeolaTokenType.AT;
import static leola.frontend.tokens.LeolaTokenType.BITWISE_AND;
import static leola.frontend.tokens.LeolaTokenType.BITWISE_NOT;
import static leola.frontend.tokens.LeolaTokenType.BITWISE_OR;
import static leola.frontend.tokens.LeolaTokenType.BITWISE_XOR;
import static leola.frontend.tokens.LeolaTokenType.BIT_SHIFT_LEFT;
import static leola.frontend.tokens.LeolaTokenType.BIT_SHIFT_RIGHT;
import static leola.frontend.tokens.LeolaTokenType.CASE;
import static leola.frontend.tokens.LeolaTokenType.DEF;
import static leola.frontend.tokens.LeolaTokenType.DOT;
import static leola.frontend.tokens.LeolaTokenType.D_EQUALS;
import static leola.frontend.tokens.LeolaTokenType.FALSE;
import static leola.frontend.tokens.LeolaTokenType.GEN;
import static leola.frontend.tokens.LeolaTokenType.GREATER_EQUALS;
import static leola.frontend.tokens.LeolaTokenType.GREATER_THAN;
import static leola.frontend.tokens.LeolaTokenType.IDENTIFIER;
import static leola.frontend.tokens.LeolaTokenType.INTEGER;
import static leola.frontend.tokens.LeolaTokenType.LEFT_BRACE;
import static leola.frontend.tokens.LeolaTokenType.LEFT_BRACKET;
import static leola.frontend.tokens.LeolaTokenType.LEFT_PAREN;
import static leola.frontend.tokens.LeolaTokenType.LESS_EQUALS;
import static leola.frontend.tokens.LeolaTokenType.LESS_THAN;
import static leola.frontend.tokens.LeolaTokenType.LOGICAL_AND;
import static leola.frontend.tokens.LeolaTokenType.LOGICAL_OR;
import static leola.frontend.tokens.LeolaTokenType.LONG;
import static leola.frontend.tokens.LeolaTokenType.MINUS;
import static leola.frontend.tokens.LeolaTokenType.MOD;
import static leola.frontend.tokens.LeolaTokenType.NEW;
import static leola.frontend.tokens.LeolaTokenType.NOT;
import static leola.frontend.tokens.LeolaTokenType.NOT_EQUALS;
import static leola.frontend.tokens.LeolaTokenType.NULL;
import static leola.frontend.tokens.LeolaTokenType.PLUS;
import static leola.frontend.tokens.LeolaTokenType.REAL;
import static leola.frontend.tokens.LeolaTokenType.REF_EQUALS;
import static leola.frontend.tokens.LeolaTokenType.REF_NOT_EQUALS;
import static leola.frontend.tokens.LeolaTokenType.RIGHT_BRACKET;
import static leola.frontend.tokens.LeolaTokenType.RIGHT_PAREN;
import static leola.frontend.tokens.LeolaTokenType.SLASH;
import static leola.frontend.tokens.LeolaTokenType.STAR;
import static leola.frontend.tokens.LeolaTokenType.STRING;
import static leola.frontend.tokens.LeolaTokenType.TRUE;

import java.util.EnumSet;

import leola.ast.ASTAttributes;
import leola.ast.ASTNode;
import leola.ast.BinaryExpr;
import leola.ast.BooleanExpr;
import leola.ast.ChainedFuncInvocationExpr;
import leola.ast.CompoundExpr;
import leola.ast.Expr;
import leola.ast.IntegerExpr;
import leola.ast.IsExpr;
import leola.ast.LongExpr;
import leola.ast.NullExpr;
import leola.ast.RealExpr;
import leola.ast.StringExpr;
import leola.ast.UnaryExpr;
import leola.ast.UnaryExpr.UnaryOp;
import leola.ast.VarExpr;
import leola.frontend.LeolaParser;
import leola.frontend.Token;
import leola.frontend.tokens.LeolaErrorCode;
import leola.frontend.tokens.LeolaTokenType;

/**
 * Parses an Expression
 * 
 * @author Tony
 *
 */
public class ExprParser extends StmtParser {

    // Synchronization set for starting an expression.
    public static final EnumSet<LeolaTokenType> EXPR_START_SET =
            EnumSet.of(CASE, DEF, GEN, LEFT_BRACKET, LEFT_BRACE, NEW, IDENTIFIER,
                       AT, NULL, TRUE, FALSE, STRING, LONG, INTEGER, REAL,
                       PLUS, MINUS, NOT, BITWISE_NOT, LEFT_PAREN);    

    public static final EnumSet<LeolaTokenType> EXPR_END_SET =
        EnumSet.of(IDENTIFIER, /*INTEGER, REAL, STRING, DEF, $ME,
                   NULL, */RIGHT_PAREN, RIGHT_BRACKET);
    
    // Set of relational operators.
    private static final EnumSet<LeolaTokenType> REL_OPS =
        EnumSet.of(REF_EQUALS, REF_NOT_EQUALS, D_EQUALS, NOT_EQUALS, LESS_THAN, LESS_EQUALS,
                   GREATER_THAN, GREATER_EQUALS );

    private static final EnumSet<LeolaTokenType> LOGICAL_OPS =
        EnumSet.of(LOGICAL_OR, LOGICAL_AND);

    private static final EnumSet<LeolaTokenType> BITWISE_OPS =
        EnumSet.of(BITWISE_XOR, BITWISE_AND, BITWISE_OR);

    // Set of multiplicative operators.
    private static final EnumSet<LeolaTokenType> MULT_OPS =
        EnumSet.of(STAR, SLASH, MOD, /*LOGICAL_AND, BITWISE_AND, BITWISE_OR,*/
                   BIT_SHIFT_LEFT, BIT_SHIFT_RIGHT);

    // Set of additive operators.
    private static final EnumSet<LeolaTokenType> ADD_OPS =
        EnumSet.of(PLUS, MINUS /*, LOGICAL_OR, BITWISE_XOR*/);

    // set of tokens that represent a chained expression
    private static final EnumSet<LeolaTokenType> CHAINED_OP =
        EnumSet.of(DOT, LEFT_PAREN, LEFT_BRACKET/*, IDENTIFIER*/);

    private boolean isNamedParameter;
    
    /**
     * @param parser
     */
    public ExprParser(LeolaParser parser) {
        this(parser, false);
    }
    
    /**
     * @param parser
     * @param isNamedParameter
     */
    public ExprParser(LeolaParser parser, boolean isNamedParameter) {
        super(parser);
        this.isNamedParameter = isNamedParameter;
    }
    
    /**
     * Parse an expression.
     * To be overridden by the specialized statement parser subclasses.
     * @param token the initial token.
     * @return the root node of the generated parse tree.
     * @throws Exception if an error occurred.
     */
    public ASTNode parse(Token token) throws Exception {
        ASTNode statementNode = parseExpr(token);
        return statementNode;
    }
        
    /**
     * Parses the next {@link Expr}
     * 
     * @param token
     * @return the {@link Expr} 
     * @throws Exception
     */
    public Expr parseExpr(Token currToken) throws Exception {
        // Parse a simple expression and make the root of its tree
        // the root node.
        ASTNode rootNode = parseSimpleExpression(currToken);

        Token token = currentToken();
        LeolaTokenType tokenType = token.getType();

        // Look for a relational operator.
        // was IF
        while (REL_OPS.contains(tokenType)) {

            token = nextToken();  // consume the operator

            // Parse the second simple expression.  The operator node adopts
            // the simple expression's tree as its second child.
            ASTNode simExprNode = parseSimpleExpression(token);

            BinaryExpr bExpr = new BinaryExpr( (Expr)rootNode
                                              ,(Expr)simExprNode
                                              , tokenType.toBinaryOp() );

            // The operator node becomes the new root node.
            rootNode = bExpr;

            token = currentToken();
            tokenType = token.getType();
        }

        while (BITWISE_OPS.contains(tokenType)) {

            token = nextToken();  // consume the operator

            // Parse the second simple expression.  The operator node adopts
            // the simple expression's tree as its second child.
            ASTNode simExprNode = parseSimpleExpression(token);

            BinaryExpr bExpr = new BinaryExpr( (Expr)rootNode
                                              ,(Expr)simExprNode
                                              , tokenType.toBinaryOp() );

            // The operator node becomes the new root node.
            rootNode = bExpr;

            token = currentToken();
            tokenType = token.getType();
        }

        while (LOGICAL_OPS.contains(tokenType)) {

            token = nextToken();  // consume the operator

            // Parse the second simple expression.  The operator node adopts
            // the simple expression's tree as its second child.
            ASTNode simExprNode = parseExpr(token);

            BinaryExpr bExpr = new BinaryExpr( (Expr)rootNode
                                              ,(Expr)simExprNode
                                              , tokenType.toBinaryOp() );

            // The operator node becomes the new root node.
            rootNode = bExpr;

            token = currentToken();
            tokenType = token.getType();
        }

//        else if (CHAINED_OP.contains(tokenType) ) {
//
//            CompoundExpr compoundExpr = new CompoundExpr();
//            compoundExpr.addChild(rootNode);
//            rootNode = compoundExpr;
//
//            do {
//
//                // Parse the second simple expression.  The operator node adopts
//                // the simple expression's tree as its second child.
//                ASTNode simExprNode = parseChainedExpr(token);
//                compoundExpr.addChild(simExprNode);
//
//                token = currentToken();
//                tokenType = token.getType();
//            } while( ( CHAINED_OP.contains(tokenType) ||
//                       IDENTIFIER.equals(tokenType) ) &&
//                    !tokenType.equals(LeolaTokenType.END_OF_FILE));
//
//        }
        
        token = currentToken();
        tokenType = token.getType();
       
        return (Expr)rootNode;
    }

    /**
     * Parses an Expression
     * @param token
     * @return
     * @throws Exception
     */
    private ASTNode parseSimpleExpression(Token token)
        throws Exception {

        LeolaTokenType signType = null;  // type of leading sign (if any)

        Token leadingToken = token;
        // Look for a leading + or - sign.
        LeolaTokenType tokenType = token.getType();
        if ( (tokenType == STAR) ||
             (tokenType == PLUS) ||             
             (tokenType == MINUS)) {            
            signType = tokenType;
            token = nextToken();  // consume the + or - or *            
        }

        // Parse a term and make the root of its tree the root node.
        ASTNode rootNode = parseTerm(token);

        // Was there a leading - sign?
        if(signType!=null) {
            if (signType == MINUS) {
    
                // Create a NEGATE node and adopt the current tree
                // as its child. The NEGATE node becomes the new root node.
                UnaryExpr negateNode = new UnaryExpr((Expr)rootNode, UnaryOp.NEGATE);
                rootNode = negateNode;
            }
            else if(signType == STAR) {
                if(!this.isNamedParameter) {
                    throwParseError(leadingToken, LeolaErrorCode.INVALID_ARGS_EXPANSION);
                }
                rootNode.appendFlag(ASTAttributes.IS_ARG_ARRAY_EXPAND);
            }
        }

        token = currentToken();
        tokenType = token.getType();

        if ( tokenType.equals(LeolaTokenType.IS)) {
            IsExpr isExpr = (IsExpr)(new IsExprParser((Expr)rootNode, this).parse(token));
            rootNode = isExpr;

            token = currentToken();
            tokenType = token.getType();
        } 

        // Loop over additive operators.
        while (ADD_OPS.contains(tokenType)) {
            LeolaTokenType operator = tokenType;

            token = nextToken();  // consume the operator

            // Parse another term.  The operator node adopts
            // the term's tree as its second child.
            ASTNode termNode = parseTerm(token);

            BinaryExpr bExpr = new BinaryExpr( (Expr)rootNode
                                             , (Expr)termNode
                                             , operator.toBinaryOp());

            // The operator node becomes the new root node.
            rootNode = bExpr;

            token = currentToken();
            tokenType = token.getType();
        }

        return rootNode;
    }

    /**
     * Parse a term.
     * @param token the initial token.
     * @return the root node of the generated parse tree.
     * @throws Exception if an error occurred.
     */
    private ASTNode parseTerm(Token token) throws Exception
    {
        // Parse a factor and make its node the root node.
        ASTNode rootNode = parseFactor(token);

        token = currentToken();
        LeolaTokenType tokenType = token.getType();

        if (CHAINED_OP.contains(tokenType) ) {

            CompoundExpr compoundExpr = new CompoundExpr();
            compoundExpr.addChild(rootNode);
            rootNode = compoundExpr;


            LeolaTokenType prevToken = null;
            do {                                
                
                // Parse the second simple expression.  The operator node adopts
                // the simple expression's tree as its second child.
                ASTNode simExprNode = parseChainedExpr(token);
                compoundExpr.addChild(simExprNode);

                token = currentToken();
                tokenType = token.getType();
                
                prevToken = previousToken().getType();
                
            } while( ( CHAINED_OP.contains(tokenType) || ( IDENTIFIER.equals(tokenType) && ! EXPR_END_SET.contains(prevToken)) ) &&
                    !tokenType.equals(LeolaTokenType.END_OF_FILE));
        }


        // Loop over multiplicative operators.
        while (MULT_OPS.contains(tokenType)) {
            LeolaTokenType operator = tokenType;

            token = nextToken();  // consume the operator

            // Parse another factor.  The operator node adopts
            // the term's tree as its second child.
            ASTNode factorNode = parseFactor(token);

            BinaryExpr bExpr = new BinaryExpr( (Expr)rootNode
                                             , (Expr)factorNode
                                             , operator.toBinaryOp());

            // The operator node becomes the new root node.
            rootNode = bExpr;

            token = currentToken();
            tokenType = token.getType();
        }

        return rootNode;
    }


    /**
     * Parse a factor.
     * @param token the initial token.
     * @return the root node of the generated parse tree.
     * @throws Exception if an error occurred.
     */
    private ASTNode parseFactor(Token token)
        throws Exception
    {
        LeolaTokenType tokenType = token.getType();
        ASTNode rootNode = null;

        switch (tokenType) {
            case AT: {
                DecoratorExprParser parser = new DecoratorExprParser(this);
                rootNode = parser.parse(token);
                break;
            }
            case IDENTIFIER: {
                return parseIdentifier(token);
            }

            case STRING: {
                rootNode = new StringExpr((String)token.getValue());
                token = nextToken();  // consume the string
                break;
            }
            case TRUE:
            case FALSE: {
                rootNode = new BooleanExpr(tokenType.equals(LeolaTokenType.TRUE));
                token = nextToken(); // consume the true or false
                break;
            }
            
            case LONG:
            case INTEGER:
            case REAL: {
                try {
                    Object value = token.getValue();
                    if ( value instanceof Integer) {
                        rootNode = new IntegerExpr( (Integer)value );
                    }
                    else if ( value instanceof Long ) {
                        rootNode = new LongExpr( (Long)value );
                    }
                    else {                        
                        rootNode = new RealExpr( (Double) value );
                    }

                    
                    token = nextToken();  // consume the number
                }
                catch(Exception e) {
                    throwParseError(token, LeolaErrorCode.INVALID_NUMBER);
                }
                break;
            }
            case DEF: {
                FuncDefExprParser parser = new FuncDefExprParser(this);
                rootNode = parser.parse(token);
                break;
            }
            case GEN: {
                GenDefExprParser parser = new GenDefExprParser(this);
                rootNode = parser.parse(token);
                break;
            }
            case CASE: {
                CaseExprParser parser = new CaseExprParser(this);
                rootNode = parser.parse(token);
                break;
            }
            case NEW: {
                NewExprParser parser = new NewExprParser(this);
                rootNode = parser.parse(token);
                break;
            }
            case NULL: {
                NullExprParser parser = new NullExprParser(this);
                rootNode = parser.parse(token);
                break;
            }
            case NOT: {
                token = nextToken();  // consume the NOT

                // Parse the factor.  The NOT node adopts the
                // factor node as its child.
                ASTNode factorNode = parseFactor(token);

                // Create a NOT node as the root node.
                rootNode = new UnaryExpr( (Expr)factorNode, UnaryOp.NOT);
                break;
            }
            case BITWISE_NOT: {
                token = nextToken();  // consume the ~

                // Parse the factor.  The NOT node adopts the
                // factor node as its child.
                ASTNode factorNode = parseFactor(token);

                // Create a NOT node as the root node.
                rootNode = new UnaryExpr( (Expr)factorNode, UnaryOp.BIT_NOT);
                break;
            }
            case LEFT_PAREN: {
                token = nextToken();      // consume the (

                // Parse an expression and make its node the root node.
                rootNode = parseExpr(token);

                // Look for the matching ) token.                
                token = expectTokenNext(currentToken(), LeolaTokenType.RIGHT_PAREN, LeolaErrorCode.MISSING_RIGHT_PAREN);
                
                break;
            }
            case LEFT_BRACE: {
                MapDeclExprParser parser = new MapDeclExprParser(this);
                rootNode = parser.parse(token);
                break;
            }
            case LEFT_BRACKET: {
                ArrayDeclExprParser parser = new ArrayDeclExprParser(this);
                rootNode = parser.parse(token);
                break;
            }
            case SEMICOLON: {
                rootNode = new NullExpr();
                break;
            }
            default: {
                throwParseError(token, LeolaErrorCode.UNEXPECTED_TOKEN);
            }
        }

        return rootNode;
    }

    /**
     * Parse an identifier.
     * @param token the current token.
     * @return the root node of the generated parse tree.
     * @throws Exception if an error occurred.
     */
    protected ASTNode parseIdentifier(Token token)
        throws Exception
    {
        ASTNode result = null;

        Token next = this.nextToken(); // eat the identifier name

        LeolaTokenType type = next.getType();
        switch(type) {
        case LEFT_PAREN: {
            /* method invocation */
            FuncInvocationParser parser = new FuncInvocationParser(this);
            result = parser.parse(token);
            break;
        }
        case LEFT_BRACKET: {
            /* array index */
            ArrayAccessExprParser parser = new ArrayAccessExprParser(this);
            result = parser.parse(token);
            break;
        }
        case DOT: {
            MemberAccessParser parser = new MemberAccessParser(this);
            result = parser.parse(token);
            break;
        }
        case COLON: {
            NamespaceAccessParser parser = new NamespaceAccessParser(this);
            result = parser.parse(token);
            break;
        }
        case EQUALS: {
            /* assignment */
            AssignmentExprParser parser = new AssignmentExprParser(this);
            result = parser.parse(token);
            break;
        }
        case ARROW: {
            
            /* If this is a named parameter, parse it as
             * such, otherwise it is just a Variable Expression
             */
            if(this.isNamedParameter) {
                NamedParameterExprParser parser = new NamedParameterExprParser(this);
                result = parser.parse(token);
            }
            else {
                result = new VarExpr(token.getText());
            }
            break;
        }
        case PLUS_EQ:
        case MINUS_EQ:
        case MOD_EQ:
        case STAR_EQ:
        case SLASH_EQ:
        case BAND_EQ:
        case BOR_EQ:
        case BSL_EQ:
        case BSR_EQ:
        case BXOR_EQ: {
            BinaryAssignmentExprParser parser = new BinaryAssignmentExprParser(this);
            result = parser.parse(token);
            break;
        }
        case IS: {
            IsExprParser parser = new IsExprParser(null, this);
            result = parser.parse(token);
            break;
        }
        default:
            result = new VarExpr(token.getText());
            break;
        }

        setLineNumber(result, token);
        return (result);
    }


    /**
     * Parse an identifier.
     * @param token the current token.
     * @return the root node of the generated parse tree.
     * @throws Exception if an error occurred.
     */
    protected ASTNode parseChainedExpr(Token token)
        throws Exception
    {
        ASTNode result = null;

        LeolaTokenType type = token.getType();
        switch(type) {
        case LEFT_PAREN: {
            /* method invocation */
            Expr[] params = ParserUtils.parseArgumentExpressions(this, token);
            result = new ChainedFuncInvocationExpr(params);
            break;
        }
        case LEFT_BRACKET: {
            /* array index */
            ChainedArrayAccessExprParser parser = new ChainedArrayAccessExprParser(this);
            result = parser.parse(token);
            break;
        }
        case DOT: {
            ChainedMemberAccessParser parser = new ChainedMemberAccessParser(this);
            result = parser.parse(token);
            break;
        }
        case COLON: {            
            throwParseError(token, LeolaErrorCode.UNIMPLEMENTED);
            break;
        }
        case EQUALS: {
            /* assignment */
            ChainedAssignmentExprParser parser = new ChainedAssignmentExprParser(this);
            result = parser.parse(token);
            break;
        }
        case PLUS_EQ:
        case MINUS_EQ:
        case MOD_EQ:
        case STAR_EQ:
        case SLASH_EQ:
        case BAND_EQ:
        case BOR_EQ:
        case BSL_EQ:
        case BSR_EQ:
        case BXOR_EQ: {
            ChainedBinaryAssignmentExprParser parser = new ChainedBinaryAssignmentExprParser(this);
            result = parser.parse(token);
            break;
        }
        default:
            result = new VarExpr(token.getText());
            nextToken(); /* eat the identifier */
            break;
        }

        setLineNumber(result, token);
        return (result);
    }

}

