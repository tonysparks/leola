/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.frontend.parsers;

import static leola.frontend.tokens.LeolaTokenType.BREAK;
import static leola.frontend.tokens.LeolaTokenType.CASE;
import static leola.frontend.tokens.LeolaTokenType.CLASS;
import static leola.frontend.tokens.LeolaTokenType.CONTINUE;
import static leola.frontend.tokens.LeolaTokenType.DOT;
import static leola.frontend.tokens.LeolaTokenType.ELSE;
import static leola.frontend.tokens.LeolaTokenType.IDENTIFIER;
import static leola.frontend.tokens.LeolaTokenType.IF;
import static leola.frontend.tokens.LeolaTokenType.LEFT_BRACE;
import static leola.frontend.tokens.LeolaTokenType.NAMESPACE;
import static leola.frontend.tokens.LeolaTokenType.RETURN;
import static leola.frontend.tokens.LeolaTokenType.RIGHT_BRACE;
import static leola.frontend.tokens.LeolaTokenType.SEMICOLON;
import static leola.frontend.tokens.LeolaTokenType.SWITCH;
import static leola.frontend.tokens.LeolaTokenType.THROW;
import static leola.frontend.tokens.LeolaTokenType.VAR;
import static leola.frontend.tokens.LeolaTokenType.WHILE;

import java.util.EnumSet;

import leola.ast.ASTNode;
import leola.ast.BreakStmt;
import leola.ast.ContinueStmt;
import leola.ast.Stmt;
import leola.frontend.EofToken;
import leola.frontend.LeolaParser;
import leola.frontend.Token;
import leola.frontend.tokens.LeolaErrorCode;
import leola.frontend.tokens.LeolaTokenType;

/**
 * Parsers a {@link Stmt}.
 *
 * @author Tony
 *
 */
public class StmtParser extends LeolaParser {

    // Synchronization set for starting a statement.
    protected static final EnumSet<LeolaTokenType> STMT_START_SET =
        EnumSet.of(IF, VAR, WHILE, LEFT_BRACE, SWITCH,
        		   RETURN, BREAK, CONTINUE, IDENTIFIER, SEMICOLON, CLASS, CASE, NAMESPACE, THROW);

    // Synchronization set for following a statement.
    protected static final EnumSet<LeolaTokenType> STMT_FOLLOW_SET =
        EnumSet.of(SEMICOLON, RIGHT_BRACE, ELSE, DOT);

	/**
	 * @param parser
	 */
	public StmtParser(LeolaParser parser) {
		super(parser);
	}

	/**
     * Parse a statement.
     * To be overridden by the specialized statement parser subclasses.
     * @param token the initial token.
     * @return the root node of the generated parse tree.
     * @throws Exception if an error occurred.
     */
    public ASTNode parse(Token token)
        throws Exception
    {
        ASTNode statementNode = null;

        LeolaTokenType type = token.getType();
        switch (type) {
        	case LEFT_BRACE: {
        		CompoundStmtParser parser = new CompoundStmtParser(this);
        		statementNode = parser.parse(token);
        		break;
        	}
        	case VAR: {
        		VarDeclStmtParser parser = new VarDeclStmtParser(this);
        		statementNode = parser.parse(token);

        		eatOptionalStmtEnd(currentToken());
        		break;
        	}
        	case NEW:
        	case IDENTIFIER: {
        		ExprParser parser = new ExprParser(this);
        		statementNode = parser.parse(token);

        		eatOptionalStmtEnd(currentToken());
        		break;
        	}
        	case IF: {
        		IfStmtParser parser = new IfStmtParser(this);
        		statementNode = parser.parse(token);
        		break;
        	}        	
        	case WHILE: {
        		WhileStmtParser parser = new WhileStmtParser(this);
        		statementNode = parser.parse(token);
        		break;
        	}
        	case RETURN: {
        		ReturnStmtParser parser = new ReturnStmtParser(this);
        		statementNode = parser.parse(token);

        		eatOptionalStmtEnd(currentToken());
        		break;
        	}
        	case BREAK: {
        		statementNode = new BreakStmt();
        		eatOptionalStmtEnd(nextToken());
        		break;
        	}
        	case CONTINUE: {
        		statementNode = new ContinueStmt();
        		eatOptionalStmtEnd(nextToken());
        		break;
        	}
        	case CLASS: {
        		ClassDefStmtParser parser = new ClassDefStmtParser(this);
        		statementNode = parser.parse(token);
        		break;
        	}
        	case NAMESPACE: {
        		NamespaceStmtParser parser = new NamespaceStmtParser(this);
        		statementNode = parser.parse(token);
        		break;
        	}
        	case SWITCH: {
        		SwitchStmtParser parser = new SwitchStmtParser(this);
        		statementNode = parser.parse(token);
        		break;
        	}
        	case THROW: {
        		ThrowStmtParser parser = new ThrowStmtParser(this);
        		statementNode = parser.parse(token);
        		break;
        	}
            default: {
                /*
                statementNode = new EmptyStmt();
                eatOptionalStmtEnd(currentToken());
                */
                ExprParser parser = new ExprParser(this);
                statementNode = parser.parse(token);

                eatOptionalStmtEnd(currentToken());
                break;
            }
        }

        // Set the current line number as an attribute.
        setLineNumber(statementNode, token);

        return statementNode;
    }

    /**
     * Eats the optional line end
     * @param token
     * @return
     * @throws Exception
     */
    protected Token eatOptionalStmtEnd(Token token) throws Exception {
    	Token nextToken = token;

    	LeolaTokenType type = token.getType();
    	if ( type.equals(LeolaTokenType.SEMICOLON) ) {
    		nextToken = nextToken();
    	}

    	return nextToken;
    }

    /**
     * Parse a statement list.
     * @param token the curent token.
     * @param parentNode the parent node of the statement list.
     * @param terminator the token type of the node that terminates the list.
     * @param errorCode the error code if the terminator token is missing.
     * @throws Exception if an error occurred.
     */
    protected void parseList(Token tok, ASTNode parentNode,
                             LeolaTokenType terminator,
                             LeolaErrorCode errorCode)
        throws Exception
    {
    	Token token = tok;

        // Synchronization set for the terminator.
        EnumSet<LeolaTokenType> terminatorSet = STMT_START_SET.clone();
        terminatorSet.add(terminator);

        // Loop to parse each statement until the END token
        // or the end of the source file.
        while (!(token instanceof EofToken) &&
               (token.getType() != terminator)) {

            // Parse a statement.  The parent node adopts the statement node.
            ASTNode statementNode = parse(token);
            parentNode.addChild(statementNode);

            token = currentToken();
            LeolaTokenType tokenType = token.getType();

            // Look for the semicolon between statements.
            if (tokenType == SEMICOLON) {
                token = nextToken();  // consume the ;
            }

            // If at the start of the next statement, then missing a semicolon.
//            else if (STMT_START_SET.contains(tokenType)) {
//                getExceptionHandler().errorToken(token, this, LeolaErrorCode.MISSING_SEMICOLON);
//            }

            // Synchronize at the start of the next statement
            // or at the terminator.
            token = synchronize(terminatorSet);
        }

        // Look for the terminator token.
        if (token.getType() == terminator) {
            token = nextToken();  // consume the terminator token
        }
        else {
            getExceptionHandler().errorToken(token, this, errorCode);
        }
    }


    /**
     * Synchronize the parser.
     * @param syncSet the set of token types for synchronizing the parser.
     * @return the token where the parser has synchronized.
     * @throws Exception if an error occurred.
     */
    public Token synchronize(EnumSet<LeolaTokenType> syncSet)
        throws Exception
    {
        Token token = currentToken();

        // If the current token is not in the synchronization set,
        // then it is unexpected and the parser must recover.
        if (!syncSet.contains(token.getType())) {

            // Flag the unexpected token.
            getExceptionHandler().errorToken(token, this, LeolaErrorCode.UNEXPECTED_TOKEN);

            // Recover by skipping tokens that are not
            // in the synchronization set.
            do {
                token = nextToken();
            } while (!(token instanceof EofToken) &&
                     !syncSet.contains(token.getType()));
       }

       return token;
    }
}

