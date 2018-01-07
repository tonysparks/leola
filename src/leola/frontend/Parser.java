package leola.frontend;

import static leola.frontend.tokens.TokenType.*;

import java.util.ArrayList;
import java.util.List;

import leola.ast.*;
import leola.frontend.tokens.Token;
import leola.frontend.tokens.TokenType;
import leola.vm.util.Pair;

/**
 * A {@link Parser} for the Leola programming language.
 * 
 * @author Tony
 *
 */
public class Parser {   
    private final Scanner scanner;
    private final List<Token> tokens;
    private int current;
    
    private int loopLevel;
    private int argumentsLevel;
    
    private Token startToken;
    
    /**
     * @param scanner
     *            the scanner to be used with this parser.
     */
    public Parser(Scanner scanner) {
        this.scanner = scanner;
        this.tokens = scanner.getTokens();
        
        this.current = 0;
        this.loopLevel = 0;
        this.argumentsLevel = 0;
    }

    /**
     * Parse a source program and generate the intermediate code and the symbol
     * table. To be implemented by a language-specific parser subclass.
     * 
     */
    public ProgramStmt parse() {
        
        List<Stmt> statements = new ArrayList<>();
        while(!isAtEnd()) {
            Stmt statement = statement();
            statements.add(statement);
        }
        
        return new ProgramStmt(statements);
    }
    
    
    private Stmt statement() {
        source();
        
        match(SEMICOLON); // eat any optional semi-colons
        
        if(match(CLASS))     return classDeclaration();
        if(match(NAMESPACE)) return namespaceDeclaration();        
        if(match(VAR))       return varDeclaration();
        if(match(IF))        return ifStatement();        
        if(match(WHILE))     return whileStatement();
        if(match(SWITCH))    return switchStatement();
        if(match(TRY))       return tryStatement();
        if(match(THROW))     return throwStatement();
        if(match(LEFT_BRACE))return blockStatement();
        if(match(RETURN))    return returnStatement();
        if(match(YIELD))     return yieldStatement();
        if(match(BREAK))     return breakStatement();
        if(match(CONTINUE))  return continueStatement();
                        
        return expression();
    }
    
    private ClassDeclStmt classDeclaration() {
        Token className = consume(IDENTIFIER, ErrorCode.MISSING_IDENTIFIER);
                
        ParameterList parameters = check(LEFT_PAREN) 
                ? parameters() : new ParameterList();
        
        String parentClassName = null;
        List<Expr> parentClassArguments = null;
        if(match(IS)) {
            parentClassName = className();
            parentClassArguments = arguments();
        }
        
        Stmt body = check(SEMICOLON) 
                ? emptyStatement() : statement();
        
        return node(new ClassDeclStmt(className.getText(), parameters, body, parentClassName, parentClassArguments));
    }
    
    private NamespaceStmt namespaceDeclaration() {
        Token name = consume(IDENTIFIER, ErrorCode.MISSING_IDENTIFIER);
        Stmt body = statement();
        return node(new NamespaceStmt(body, name.getText()));
    }
    
    private VarDeclStmt varDeclaration() {
        Token name = consume(IDENTIFIER, ErrorCode.MISSING_IDENTIFIER);
        
        Expr initializer = null;
        if(match(EQUALS)) {
            initializer = assignment();
        }
        
        return node(new VarDeclStmt(name.getText(), initializer));
    }
            
    private IfStmt ifStatement() {
        boolean hasLeftParen = match(LEFT_PAREN);
        Expr condition = expression();
        
        if(hasLeftParen) {
            consume(RIGHT_PAREN, ErrorCode.MISSING_RIGHT_PAREN);
        }

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(ELSE)) {
            elseBranch = statement();
        }

        return node(new IfStmt(condition, thenBranch, elseBranch));
    }
    
    private WhileStmt whileStatement() {
        boolean hasLeftParen = match(LEFT_PAREN);
        Expr condition = expression();
        
        if(hasLeftParen) {
            consume(RIGHT_PAREN, ErrorCode.MISSING_RIGHT_PAREN);
        }
        
        try {
            this.loopLevel++;
            Stmt body = statement();
            
            return node(new WhileStmt(condition, body));
        }
        finally {
            this.loopLevel--;
        }
    }
    

    
    private SwitchStmt switchStatement() {
        Expr condition = null;
        if(!check(WHEN) && !check(LEFT_BRACE)) {
            condition = expression();
        }
        else {
            condition = new BooleanExpr(true);
        }
        
        List<Pair<Expr, Stmt>> whenStmts = new ArrayList<>();
        
        boolean hasBraces = match(LEFT_BRACE);
        
        do {
            consume(WHEN, ErrorCode.MISSING_WHEN);
            Expr whenCond = expression();
            consume(ARROW, ErrorCode.MISSING_ARROW);
            Stmt stmt = statement();
            
            whenStmts.add(new Pair<>(whenCond, stmt));            
        }
        while(check(WHEN));
        
        Stmt elseStmt = null;
        if(match(ELSE)) {
            elseStmt = statement();
        }
        
        if(hasBraces) {
            consume(RIGHT_BRACE, ErrorCode.MISSING_RIGHT_BRACE);
        }
        
        return node(new SwitchStmt(condition, whenStmts, elseStmt));
    }
    
    private TryStmt tryStatement() {
        Stmt body = statement();
        CatchStmt catchBody = null;
        Stmt finallyBody = null;
        if(match(CATCH)) {
            catchBody = catchStatement();
        }
        
        if(match(FINALLY)) {
            finallyBody = statement();
        }
        
        return node(new TryStmt(body, catchBody, finallyBody));
    }
    
    private CatchStmt catchStatement() {
        Token exceptionName = consume(IDENTIFIER, ErrorCode.MISSING_IDENTIFIER);
        Stmt body = statement();
        return node(new CatchStmt(exceptionName.getText(), body));
    }
    
    private ThrowStmt throwStatement() {
        Expr value = expression();
        return node(new ThrowStmt(value));
    }
    
    private EmptyStmt emptyStatement() {
        return node(new EmptyStmt());
    }
    
    private ReturnStmt returnStatement() {        
        Expr value = null;
        if(!check(SEMICOLON)) {
            value = expression();
        }
        
        if(value == null) {
            consume(SEMICOLON, ErrorCode.MISSING_SEMICOLON);
        }
        
        return node(new ReturnStmt(value));
    }
    
    private YieldStmt yieldStatement() {
        Expr value = null;
        if(!check(SEMICOLON)) {
            value = expression();
        }
        
        if(value == null) {
            consume(SEMICOLON, ErrorCode.MISSING_SEMICOLON);
        }
        
        return node(new YieldStmt(value));
    }
    
    private BreakStmt breakStatement() {
        if(this.loopLevel < 1) {
            throw error(previous(), ErrorCode.INVALID_BREAK_STMT);
        }
        
        match(SEMICOLON);
        return node(new BreakStmt());
    }
    
    private ContinueStmt continueStatement() {
        if(this.loopLevel < 1) {
            throw error(previous(), ErrorCode.INVALID_CONTINUE_STMT);
        }
        
        match(SEMICOLON);
        return node(new ContinueStmt());
    }
    
    private BlockStmt blockStatement() {
        List<Stmt> statements = new ArrayList<>();
        while(!check(RIGHT_BRACE) && !isAtEnd()) {
            Stmt statement = statement();
            statements.add(statement);
            
            match(SEMICOLON); // eat any optional semi-colons
        }
        
        consume(RIGHT_BRACE, ErrorCode.MISSING_RIGHT_BRACE);
        
        return node(new BlockStmt(statements));
    }
    
    
    /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     *                      Expression parsing
     *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
    
    
    private Expr expression() {
        return assignment();
    }
    
    private Expr assignment()  {
        Expr expr = or();
        
        if(match(EQUALS, PLUS_EQ, MINUS_EQ, STAR_EQ, SLASH_EQ, 
                MOD_EQ, BSL_EQ, BSR_EQ, BOR_EQ, BAND_EQ, BXOR_EQ)) {
            
            Token operatorEquals = previous();
            Expr value = assignment();
            
            if(expr instanceof VarExpr) {
                VarExpr varExpr = (VarExpr)expr;
                return node(new AssignmentExpr(varExpr, value, operatorEquals)); 
            }
            else if(expr instanceof GetExpr) {
                GetExpr getExpr = (GetExpr)expr;
                return node(new SetExpr(getExpr.getObject(), getExpr.getIdentifier(), value, operatorEquals));
            }            
            else if(expr instanceof SubscriptGetExpr) {
                SubscriptGetExpr subscriptExpr = (SubscriptGetExpr)expr;
                return node(new SubscriptSetExpr(subscriptExpr.getObject(), subscriptExpr.getElementIndex(), value, operatorEquals));
            }
            
            throw error(operatorEquals, ErrorCode.INVALID_ASSIGNMENT);
        }
        
        return expr;
    }
    
    private Expr or() {
        Expr expr = and();
        
        while(match(LOGICAL_OR)) {
            Token operator = previous();
            Expr right = and();
            expr = node(new BinaryExpr(expr, right, operator));
        }
        
        return expr;
    }
    
    private Expr and() {
        Expr expr = equality();
        
        while(match(LOGICAL_AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = node(new BinaryExpr(expr, right, operator));
        }
        
        return expr;
    }
    
    private Expr equality() {
        Expr expr = comparison();
        
        while(match(NOT_EQUALS, D_EQUALS, REF_EQUALS, REF_NOT_EQUALS)) {
            Token operator = previous();
            Expr right = comparison();
            expr = node(new BinaryExpr(expr, right, operator));
        }
        
        return expr;
    }
    
    private Expr comparison() {
        Expr expr = term();
        
        while(match(GREATER_THAN, GREATER_EQUALS, LESS_THAN, LESS_EQUALS)) {
            Token operator = previous();
            Expr right = term();
            expr = node(new BinaryExpr(expr, right, operator));
        }
        
        return expr;
    }
    
    private Expr term() {
        Expr expr = factor();
        
        while(match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = node(new BinaryExpr(expr, right, operator));
        }
        
        return expr;
    }
    
    private Expr factor() {
        Expr expr = unary();
        
        while(match(SLASH, STAR, MOD)) {
            Token operator = previous();
            Expr right = unary();
            expr = node(new BinaryExpr(expr, right, operator));
        }
        
        return expr;
    }
    
    private Expr unary() {
        if(match(NOT, MINUS, BITWISE_NOT) || (this.argumentsLevel > 0 && match(STAR)) ) {
            Token operator = previous();
            Expr right = unary();
            return node(new UnaryExpr(right, operator)); 
        }
        return functionCall();
    }
    
    private Expr functionCall() {
        Expr expr = primary();
        while(true) {
            if(match(LEFT_PAREN)) {
                expr = finishFunctionCall(expr);
            }
            else if(match(LEFT_BRACKET)) {
                Expr indexExpr = expression();
                consume(RIGHT_BRACKET, ErrorCode.MISSING_RIGHT_BRACKET);                
                expr = node(new SubscriptGetExpr(expr, indexExpr));
            }
            else if(match(DOT)) {
                Token name = consume(IDENTIFIER, ErrorCode.MISSING_IDENTIFIER);
                expr = node(new GetExpr(expr, name.getText()));
            }
            else if(match(QUESTION_MARK)) {
                Token name = consume(IDENTIFIER, ErrorCode.MISSING_IDENTIFIER);
                expr = node(new ElvisGetExpr(expr, name.getText()));
            }
            else if(match(COLON)) {
                if(!(expr instanceof VarExpr)) {
                    throw error(previous(), ErrorCode.INVALID_NAMESPACE_ACCESS);
                }
                VarExpr varExpr = (VarExpr)expr;
                Token name = consume(IDENTIFIER, ErrorCode.MISSING_IDENTIFIER);
                expr = node(new NamespaceGetExpr(varExpr, name.getText()));
            }
            else if(match(IS)) {
                Token name = consume(IDENTIFIER, ErrorCode.MISSING_IDENTIFIER);
                expr = node(new IsExpr(expr, name.getText()));
            }
            else if(this.argumentsLevel > 0 && match(FAT_ARROW)) {
                if(!(expr instanceof VarExpr)) {
                    throw error(previous(), ErrorCode.INVALID_NAMED_PARAMETER);
                }
                VarExpr var = (VarExpr)expr;
                Expr valueExpr = expression();
                expr = node(new NamedParameterExpr(var.getVarName(), valueExpr));
            }
            else {
                break;
            }
        }
        
        return expr;
    }
    
    private Expr primary() {
        source();
        
        if(match(TRUE))  return node(new BooleanExpr(true));
        if(match(FALSE)) return node(new BooleanExpr(false));
        if(match(NULL))  return node(new NullExpr());
        
        if(match(INTEGER)) return node(new IntegerExpr((int)previous().getValue()));
        if(match(LONG))    return node(new LongExpr((long)previous().getValue()));
        if(match(REAL))    return node(new RealExpr((double)previous().getValue()));
        if(match(STRING))  return node(new StringExpr(previous().getValue().toString()));
        
        if(match(IDENTIFIER)) return node(new VarExpr(previous().getText()));
        
        if(match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, ErrorCode.MISSING_RIGHT_PAREN);
            return expr;
        }
        
        if(match(CASE)) return caseExpression();
        if(match(DEF))  return function();
        if(match(GEN))  return generator();
        
        if(match(AT)) return decorator();
        
        if(match(LEFT_BRACKET)) return array();
        if(match(LEFT_BRACE))   return map();
        
        if(match(NEW)) return newInstance();
        
        throw error(peek(), ErrorCode.UNEXPECTED_TOKEN);
    }
    
    private DecoratorExpr decorator() {
        Token name = consume(IDENTIFIER, ErrorCode.MISSING_IDENTIFIER);
        
        List<Expr> arguments = new ArrayList<>();
        if(match(LEFT_PAREN)) {
            arguments.addAll(arguments());
        }
        
        Expr decoratedExpr = expression();
        
        return node(new DecoratorExpr(new VarExpr(name.getText()), arguments, decoratedExpr));
    }
    
    private FuncDefExpr function() {                
        ParameterList parameters = parameters();        
        Stmt body = statement();        
        return node(new FuncDefExpr(body, parameters));
    }
    
    private GenDefExpr generator() {        
        ParameterList parameters = parameters();
        Stmt body = statement();        
        return node(new GenDefExpr(body, parameters));
    }

    
    private Expr finishFunctionCall(Expr callee) {
        List<Expr> arguments = arguments();        
        return node(new FuncInvocationExpr(callee, arguments));
    }
    
    private CaseExpr caseExpression() {
        Expr condition = null;
        if(!check(WHEN) && !check(LEFT_BRACE)) {
            condition = expression();
        }
        else {
            condition = new BooleanExpr(true);
        }
        
        List<Pair<Expr, Expr>> whenStmts = new ArrayList<>();
        
        boolean hasBraces = match(LEFT_BRACE);
        
        do {
            consume(WHEN, ErrorCode.MISSING_WHEN);
            Expr whenCond = expression();
            consume(ARROW, ErrorCode.MISSING_ARROW);
            Expr expr = expression();
            
            whenStmts.add(new Pair<>(whenCond, expr));            
        }
        while(check(WHEN));
        
        Expr elseExpr = null;
        if(match(ELSE)) {
            elseExpr = expression();
        }
        
        if(hasBraces) {
            consume(RIGHT_BRACE, ErrorCode.MISSING_RIGHT_BRACE);
        }
        
        return node(new CaseExpr(condition, whenStmts, elseExpr));
    }
    
    private NewExpr newInstance() {
        
        String className = className();
        
        List<Expr> arguments = arguments();        
        return node(new NewExpr(className, arguments));
    }
    
    private ArrayDeclExpr array() {
        List<Expr> elements = new ArrayList<>();
        do {
            if(check(RIGHT_BRACKET)) {
                break;
            }
            Expr element = expression();
            elements.add(element);
        }
        while(match(COMMA));
        
        consume(RIGHT_BRACKET, ErrorCode.MISSING_RIGHT_BRACKET);
        
        return new ArrayDeclExpr(elements);
    }
    
    private MapDeclExpr map() {
        List<Pair<Expr, Expr>> elements = new ArrayList<>();
        do {
            if(check(RIGHT_BRACE)) {
                break;
            }
            
            Expr key = expression();
            consume(ARROW, ErrorCode.MISSING_ARROW);
            Expr value = expression();
            
            elements.add(new Pair<>(key, value));
        }
        while(match(COMMA));
        
        consume(RIGHT_BRACE, ErrorCode.MISSING_RIGHT_BRACE);
        
        return new MapDeclExpr(elements);
    }

    
    /**
     * Parses parameters:
     * 
     * def(x,y,z) {
     * }
     * 
     * The (x,y,z) part
     * 
     * @return the parsed {@link ParameterList}
     */
    private ParameterList parameters() {
        consume(LEFT_PAREN, ErrorCode.MISSING_LEFT_PAREN);
        
        ParameterList parameters = new ParameterList();        
        if(!check(RIGHT_PAREN)) {
            do {                
                parameters.addParameter(consume(IDENTIFIER, ErrorCode.MISSING_IDENTIFIER).getText());
                if(check(VAR_ARGS)) {
                    if(parameters.isVarargs()) {
                        throw error(previous(), ErrorCode.INVALID_MULTI_VAR_ARGS);
                    }
                    
                    advance();
                    parameters.setVarargs(true);
                    
                }
            }
            while(match(COMMA));
        }
        
        consume(RIGHT_PAREN, ErrorCode.MISSING_RIGHT_PAREN);
        return parameters;
    }
    
    /**
     * Parses arguments:
     * 
     * someFunction( 1.0, x );
     * 
     * Parses the ( 1.0, x ) into a {@link List} of {@link Expr}
     * 
     * @return the {@link List} of {@link Expr}
     */
    private List<Expr> arguments() {
        List<Expr> arguments = new ArrayList<>();
        if(!check(RIGHT_PAREN)) {
            try {
                this.argumentsLevel++;
            
                do {
                    arguments.add(assignment());
                } 
                while(match(COMMA));
            }
            finally {
                this.argumentsLevel--;
            }
        }
        
        consume(RIGHT_PAREN, ErrorCode.MISSING_RIGHT_PAREN);
        
        return arguments;
    }
    
    /**
     * Parses a class name, in leola a class name may be:
     * 
     * identifier [(.|:) identifier]*
     * 
     * @return the class name
     */
    private String className() {
        StringBuilder className = new StringBuilder();
        do {
            Token classNamePart = consume(IDENTIFIER, ErrorCode.MISSING_IDENTIFIER);
            className.append(classNamePart.getText());
            if(check(DOT)) {
                advance();
                className.append(".");
            }
            else if(check(COLON)) {
                advance();
                className.append(":");
            }            
        }
        while(!match(LEFT_PAREN));
        
        return className.toString();
    }
    

    
    /**
     * Mark the start of parsing a statement
     * so that we can properly mark the AST node
     * source line and number information
     */
    private void source() {
        this.startToken = peek();
    }
    
    /**
     * Updates the AST node parsing information
     * 
     * @param node
     * @return the supplied node
     */
    private <T extends ASTNode> T node(T node) {
        if(this.startToken != null) {
            node.setSourceLine(this.startToken.getText());
            node.setLineNumber(this.startToken.getLineNumber());
        }
        return node;
    }
    
    /**
     * Determines if the supplied {@link TokenType} is
     * the current {@link Token}, if it is it will advance
     * over it.
     * 
     * @param type
     * @return true if we've advanced (i.e., the supplied token type was
     * the current one).
     */
    private boolean match(TokenType type) {        
        if(check(type)) {
            advance();
            return true;
        }        
        
        return false;
    }

    /**
     * Determines if any of the supplied {@link TokenType}'s are
     * the current {@link Token}, if it is it will advance.
     * 
     * @param type
     * @return true if we've advanced (i.e., the supplied token type was
     * the current one).
     */
    private boolean match(TokenType ...types) {
        for(TokenType type : types) {
            if(check(type)) {
                advance();
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Ensures the supplied {@link TokenType} is the current one and 
     * advances.  If the {@link TokenType} does not match, this will
     * throw a {@link ParseException}
     * 
     * @param type
     * @param errorCode
     * @return the skipped {@link Token}
     */
    private Token consume(TokenType type, ErrorCode errorCode) {
        if(check(type)) {
            return advance();
        }
        
        throw error(peek(), errorCode);
    }
    
    
    /**
     * Checks to see if the current {@link Token} is of the supplied
     * {@link TokenType}
     * 
     * @param type
     * @return true if it is
     */
    private boolean check(TokenType type) {
        if(isAtEnd()) {
            return false;
        }
        
        return peek().getType() == type;
    }
  
    /**
     * Advances to the next Token.  If we've reached
     * the END_OF_FILE token, this stop advancing.
     * 
     * @return the previous token.
     */
    private Token advance() {
        if(!isAtEnd()) {
            this.current++;
        }
        return previous();
    }
    
    
    /**
     * The previous token
     * @return The previous token
     */
    private Token previous() {
        return this.tokens.get(current - 1);
    }
        
    /**
     * The current token
     * @return The current token
     */
    private Token peek() {
        return this.tokens.get(current);
    }
    
    /**
     * If we've reached the end of the file
     * 
     * @return true if we're at the end
     */
    private boolean isAtEnd() {
        return peek().getType() == END_OF_FILE;
    }
    
    
    /**
     * Constructs an error message into a {@link ParseException}
     * 
     * @param token
     * @param errorCode
     * @return the {@link ParseException} to be thrown
     */
    private ParseException error(Token token, ErrorCode errorCode) {
        int lineNumber = token.getLineNumber();
        int position = token.getPosition();
        String tokenText = token.getType() != TokenType.END_OF_FILE ? token.getText() : null;
        String errorMessage = errorCode.toString(); 
        
        int spaceCount = position + 1;
        String currentLine = this.scanner.getSourceLine(lineNumber);
        StringBuilder flagBuffer = new StringBuilder(currentLine != null ? currentLine : "");
        flagBuffer.append("\n");

        // Spaces up to the error position.
        for (int i = 1; i < spaceCount; ++i) {
            flagBuffer.append(' ');
        }

        // A pointer to the error followed by the error message.
        flagBuffer.append("^\n*** ").append(errorMessage);

        flagBuffer.append(" [at line: ").append(lineNumber);
        
        // Text, if any, of the bad token.
        if (tokenText != null) {
            flagBuffer.append(" '").append(tokenText).append("'");
        }
        
        flagBuffer.append("]");

        return new ParseException(errorCode, flagBuffer.toString());
    }
}
