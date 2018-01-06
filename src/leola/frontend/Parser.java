package leola.frontend;

import static leola.frontend.tokens.TokenType.*;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import leola.ast.ASTNode;
import leola.ast.SubscriptGetExpr;
import leola.ast.SubscriptSetExpr;
import leola.ast.ArrayDeclExpr;
import leola.ast.AssignmentExpr;
import leola.ast.BinaryExpr;
import leola.ast.BooleanExpr;
import leola.ast.BreakStmt;
import leola.ast.CaseExpr;
import leola.ast.CatchStmt;
import leola.ast.ClassDeclStmt;
import leola.ast.BlockStmt;
import leola.ast.ContinueStmt;
import leola.ast.DecoratorExpr;
import leola.ast.Expr;
import leola.ast.FuncDefExpr;
import leola.ast.FuncInvocationExpr;
import leola.ast.GenDefExpr;
import leola.ast.GetExpr;
import leola.ast.IfStmt;
import leola.ast.IntegerExpr;
import leola.ast.IsExpr;
import leola.ast.LongExpr;
import leola.ast.MapDeclExpr;
import leola.ast.NamedParameterExpr;
import leola.ast.NamespaceStmt;
import leola.ast.NewExpr;
import leola.ast.NullExpr;
import leola.ast.ParameterList;
import leola.ast.ProgramStmt;
import leola.ast.RealExpr;
import leola.ast.ReturnStmt;
import leola.ast.SetExpr;
import leola.ast.Stmt;
import leola.ast.StringExpr;
import leola.ast.SwitchStmt;
import leola.ast.ThrowStmt;
import leola.ast.TryStmt;
import leola.ast.UnaryExpr;
import leola.ast.VarDeclStmt;
import leola.ast.VarExpr;
import leola.ast.WhileStmt;
import leola.ast.YieldStmt;
import leola.frontend.tokens.LeolaErrorCode;
import leola.frontend.tokens.TokenType;
import leola.vm.util.Pair;

/**
 * A {@link Parser} for the Leola programming language.
 * 
 * @author Tony
 *
 */
public class Parser {
   
    
    public static void main(String[] args) throws Exception {
        Source source = new Source(new FileReader("C:/Users/Tony/Desktop/scripts/leola/statements.leola"));
        Scanner scanner = new Scanner(source);
        Parser parser = new Parser(scanner);
        ASTNode node = parser.parse();
        System.out.println(node);
    }
    
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
    
    private void source() {
        this.startToken = peek();
    }
    
    private <T extends ASTNode> T node(T node) {
        if(this.startToken != null) {
            node.setSourceLine(this.startToken.getText());
            node.setLineNumber(this.startToken.getLineNumber());
        }
        return node;
    }
    
    private ClassDeclStmt classDeclaration() {
        Token className = consume(IDENTIFIER, LeolaErrorCode.MISSING_IDENTIFIER);
                
        ParameterList parameters = parameters();
        
        // Parse any parent class
        String parentClassName = null;
        List<Expr> parentClassParams = null;
        if(match(IS)) {
            // TODO
        }
        
        // Parse any interfaces
        List<String> interfaceNames = null;
        if(match(COLON)) {
            // TODO
        }
        
        Stmt body = statement();
        
        return node(new ClassDeclStmt(className.getText(), parameters, body, parentClassName, parentClassParams, interfaceNames));
    }
    
    private NamespaceStmt namespaceDeclaration() {
        Token name = consume(IDENTIFIER, LeolaErrorCode.MISSING_IDENTIFIER);
        Stmt body = statement();
        return node(new NamespaceStmt(body, name.getText()));
    }
    
    private VarDeclStmt varDeclaration() {
        Token name = consume(IDENTIFIER, LeolaErrorCode.MISSING_IDENTIFIER);
        
        Expr initializer = null;
        if(match(EQUALS)) {
            initializer = assignment();
        }
        
        return node(new VarDeclStmt(name.getText(), initializer));
    }
    
    private Stmt statement() {
        source();
        
        match(SEMICOLON); // eat any optional semi-colons
                
        if(match(CLASS)) return classDeclaration();
        if(match(NAMESPACE)) return namespaceDeclaration();        
        if(match(VAR)) return varDeclaration();
        if(match(IF)) return ifStatement();        
        if(match(WHILE)) return whileStatement();
        if(match(SWITCH)) return switchStatement();
        if(match(TRY)) return tryStatement();
        if(match(THROW)) return throwStatement();
        if(match(LEFT_BRACE)) return compoundStatement();
        if(match(RETURN)) return returnStatement();
        if(match(YIELD)) return yieldStatement();
        if(match(BREAK)) return breakStatement();
        if(match(CONTINUE)) return continueStatement();
                
        return expression();
    }
        
    private IfStmt ifStatement() {
        boolean hasLeftParen = match(LEFT_PAREN);
        Expr condition = expression();
        
        if(hasLeftParen) {
            consume(RIGHT_PAREN, LeolaErrorCode.MISSING_RIGHT_PAREN);
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
            consume(RIGHT_PAREN, LeolaErrorCode.MISSING_RIGHT_PAREN);
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
        if(!match(WHEN)) {
            condition = expression();
        }
        
        List<Pair<Expr, Stmt>> whenStmts = new ArrayList<>();
        
        boolean hasBraces = match(LEFT_BRACE);
        
        do {
            consume(WHEN, LeolaErrorCode.MISSING_WHEN);
            Expr whenCond = expression();
            consume(ARROW, LeolaErrorCode.MISSING_ARROW);
            Stmt stmt = statement();
            
            whenStmts.add(new Pair<>(whenCond, stmt));            
        }
        while(match(WHEN));
        
        Stmt elseStmt = null;
        if(match(ELSE)) {
            elseStmt = statement();
        }
        
        if(hasBraces) {
            consume(RIGHT_BRACE, LeolaErrorCode.MISSING_RIGHT_BRACE);
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
        Token exceptionName = consume(IDENTIFIER, LeolaErrorCode.MISSING_IDENTIFIER);
        Stmt body = statement();
        return node(new CatchStmt(exceptionName.getText(), body));
    }
    
    private ThrowStmt throwStatement() {
        Expr value = expression();
        return node(new ThrowStmt(value));
    }
    
//    private EmptyStmt emptyStatement() {
//        return node(new EmptyStmt());
//    }
    
    private ReturnStmt returnStatement() {        
        Expr value = null;
        if(!check(SEMICOLON)) {
            value = expression();
        }
        
        if(value == null) {
            consume(SEMICOLON, LeolaErrorCode.MISSING_SEMICOLON);
        }
        
        return node(new ReturnStmt(value));
    }
    
    private YieldStmt yieldStatement() {
        Expr value = null;
        if(!check(SEMICOLON)) {
            value = expression();
        }
        
        if(value == null) {
            consume(SEMICOLON, LeolaErrorCode.MISSING_SEMICOLON);
        }
        
        return node(new YieldStmt(value));
    }
    
    private BreakStmt breakStatement() {
        if(this.loopLevel < 1) {
            throw error(previous(), LeolaErrorCode.INVALID_BREAK_STMT);
        }
        
        match(SEMICOLON);
        return node(new BreakStmt());
    }
    
    private ContinueStmt continueStatement() {
        if(this.loopLevel < 1) {
            throw error(previous(), LeolaErrorCode.INVALID_CONTINUE_STMT);
        }
        
        match(SEMICOLON);
        return node(new ContinueStmt());
    }
    
    private BlockStmt compoundStatement() {
        List<Stmt> statements = new ArrayList<>();
        while(!check(RIGHT_BRACE) && !isAtEnd()) {
            Stmt statement = statement();
            statements.add(statement);            
        }
        
        consume(RIGHT_BRACE, LeolaErrorCode.MISSING_RIGHT_BRACE);
        
        return node(new BlockStmt(statements));
    }
    
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
            
            error(operatorEquals, LeolaErrorCode.INVALID_ASSIGNMENT);
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
        
        while(match(SLASH, STAR)) {
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
                consume(RIGHT_BRACKET, LeolaErrorCode.MISSING_RIGHT_BRACKET);                
                expr = node(new SubscriptGetExpr(expr, indexExpr));
            }
            else if(match(DOT, COLON)) {
                Token name = consume(IDENTIFIER, LeolaErrorCode.MISSING_IDENTIFIER);
                expr = node(new GetExpr(expr, name.getText()));
            }
            else if(match(IS)) {
                Token name = consume(IDENTIFIER, LeolaErrorCode.MISSING_IDENTIFIER);
                expr = node(new IsExpr(expr, name.getText()));
            }
            else if(this.argumentsLevel > 0 && match(ARROW)) {
                if(!(expr instanceof VarExpr)) {
                    throw error(previous(), LeolaErrorCode.INVALID_NAMED_PARAMETER);
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
        
        if(match(TRUE)) return node(new BooleanExpr(true));
        if(match(FALSE)) return node(new BooleanExpr(false));
        if(match(NULL)) return node(new NullExpr());
        
        if(match(INTEGER)) return node(new IntegerExpr((int)previous().getValue()));
        if(match(LONG)) return node(new LongExpr((long)previous().getValue()));
        if(match(REAL)) return node(new RealExpr((double)previous().getValue()));
        if(match(STRING)) return node(new StringExpr(previous().getValue().toString()));
        
        if(match(IDENTIFIER)) return node(new VarExpr(previous().getText()));
        
        if(match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, LeolaErrorCode.MISSING_RIGHT_PAREN);
            return expr;
        }
        
        if(match(CASE)) return caseExpression();
        if(match(DEF)) return function();
        if(match(GEN)) return generator();
        
        if(match(AT)) return decorator();
        
        if(match(LEFT_BRACKET)) return array();
        if(match(LEFT_BRACE)) return map();
        if(match(NEW)) return newInstance();
        
        throw error(peek(), LeolaErrorCode.UNEXPECTED_TOKEN);
    }
    
    private ParameterList parameters() {
        consume(LEFT_PAREN, LeolaErrorCode.MISSING_LEFT_PAREN);
        
        ParameterList parameters = new ParameterList();        
        if(!check(RIGHT_PAREN)) {
            do {                
                parameters.addParameter(consume(IDENTIFIER, LeolaErrorCode.MISSING_IDENTIFIER).getText());
                if(check(VAR_ARGS)) {
                    if(parameters.isVarargs()) {
                        throw error(previous(), LeolaErrorCode.INVALID_MULTI_VAR_ARGS);
                    }
                    
                    advance();
                    parameters.setVarargs(true);
                    
                }
            }
            while(match(COMMA));
        }
        
        consume(RIGHT_PAREN, LeolaErrorCode.MISSING_RIGHT_PAREN);
        return parameters;
    }
    
    private DecoratorExpr decorator() {
        Token name = consume(IDENTIFIER, LeolaErrorCode.MISSING_IDENTIFIER);
        
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
        
        consume(RIGHT_PAREN, LeolaErrorCode.MISSING_RIGHT_PAREN);
        
        return arguments;
    }
    
    private Expr finishFunctionCall(Expr callee) {
        List<Expr> arguments = arguments();        
        return node(new FuncInvocationExpr(callee, arguments));
    }
    
    private CaseExpr caseExpression() {
        Expr condition = null;
        if(!match(WHEN)) {
            condition = expression();
        }
        
        List<Pair<Expr, Expr>> whenStmts = new ArrayList<>();
        
        boolean hasBraces = match(LEFT_BRACE);
        
        do {
            consume(WHEN, LeolaErrorCode.MISSING_WHEN);
            Expr whenCond = expression();
            consume(ARROW, LeolaErrorCode.MISSING_ARROW);
            Expr expr = expression();
            
            whenStmts.add(new Pair<>(whenCond, expr));            
        }
        while(match(WHEN));
        
        Expr elseExpr = null;
        if(match(ELSE)) {
            elseExpr = expression();
        }
        
        if(hasBraces) {
            consume(RIGHT_BRACE, LeolaErrorCode.MISSING_RIGHT_BRACE);
        }
        
        return node(new CaseExpr(condition, whenStmts, elseExpr));
    }
    
    private NewExpr newInstance() {
        
        StringBuilder className = new StringBuilder();
        do {
            Token classNamePart = consume(IDENTIFIER, LeolaErrorCode.MISSING_IDENTIFIER);
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
        
        List<Expr> arguments = arguments();        
        return node(new NewExpr(className.toString(), arguments));
    }
    
    private ArrayDeclExpr array() {
        List<Expr> elements = new ArrayList<>();
        if(!check(RIGHT_BRACKET)) {
            do {
                Expr element = expression();
                elements.add(element);
            }
            while(match(COMMA));
        }
        
        consume(RIGHT_BRACKET, LeolaErrorCode.MISSING_RIGHT_BRACKET);
        
        return new ArrayDeclExpr(elements);
    }
    
    private MapDeclExpr map() {
        List<Pair<Expr, Expr>> elements = new ArrayList<>();
        if(!check(RIGHT_BRACE)) {
            do {
                Expr key = expression();
                consume(ARROW, LeolaErrorCode.MISSING_ARROW);
                Expr value = expression();
                
                elements.add(new Pair<>(key, value));
            }
            while(match(COMMA));
        }
        
        consume(RIGHT_BRACE, LeolaErrorCode.MISSING_RIGHT_BRACE);
        
        return new MapDeclExpr(elements);
    }

    private boolean match(TokenType type) {        
        if(check(type)) {
            advance();
            return true;
        }        
        
        return false;
    }
    
    private boolean match(TokenType ...types) {
        for(TokenType type : types) {
            if(check(type)) {
                advance();
                return true;
            }
        }
        
        return false;
    }
    
    private Token consume(TokenType type, LeolaErrorCode errorCode) {
        if(check(type)) {
            return advance();
        }
        
        throw error(peek(), errorCode);
    }
    
    private boolean check(TokenType type) {
        if(isAtEnd()) {
            return false;
        }
        
        return peek().type == type;
    }
  
    
    private Token advance() {
        if(!isAtEnd()) {
            this.current++;
        }
        return previous();
    }
    
    private Token previous() {
        return this.tokens.get(current - 1);
    }
    
    private Token peek() {
        return this.tokens.get(current);
    }
    
    /**
     * If we've reached the end of the file
     * 
     * @return true if we're at the end
     */
    private boolean isAtEnd() {
        return peek().type == END_OF_FILE;
    }
    
    private ParseException error(Token token, LeolaErrorCode errorCode) {
        int lineNumber = token.getLineNumber();
        int position = token.getPosition();
        String tokenText = token.getText();
        String errorMessage = errorCode.toString(); 
        
        int spaceCount = position;
        String currentLine = this.scanner.getSourceLine(lineNumber);
        StringBuilder flagBuffer = new StringBuilder(currentLine != null ? currentLine : "");
        flagBuffer.append("\n");

        // Spaces up to the error position.
        for (int i = 1; i < spaceCount; ++i) {
            flagBuffer.append(' ');
        }

        // A pointer to the error followed by the error message.
        flagBuffer.append("^\n*** ").append(errorMessage);

        // Text, if any, of the bad token.
        if (tokenText != null) {
            flagBuffer.append(" [at line: ")
                      .append(lineNumber)
                      .append(" '").append(tokenText).append("']");
        }

        return new ParseException(errorCode, flagBuffer.toString());
    }
}
