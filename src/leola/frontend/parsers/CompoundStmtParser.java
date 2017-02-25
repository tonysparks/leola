/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.frontend.parsers;

import leola.ast.ASTNode;
import leola.ast.CompoundStmt;
import leola.frontend.LeolaParser;
import leola.frontend.Token;
import leola.frontend.tokens.LeolaErrorCode;
import leola.frontend.tokens.LeolaTokenType;

/**
 * @author Tony
 *
 */
public class CompoundStmtParser extends StmtParser {

    /**
     * @param parser
     */
    public CompoundStmtParser(LeolaParser parser) {
        super(parser);
    }
    
    /* (non-Javadoc)
     * @see leola.frontend.parsers.StmtParser#parse(leola.frontend.Token)
     */
    @Override
    public ASTNode parse(Token token) throws Exception {
        Token startingToken = token;
        token = nextToken();  // consume the {

        // Create the COMPOUND node.
        CompoundStmt compoundNode = new CompoundStmt();

        // Parse the statement list terminated by the } token.        
        parseList(token, compoundNode, LeolaTokenType.RIGHT_BRACE, LeolaErrorCode.MISSING_RIGHT_BRACE);

        setLineNumber(compoundNode, startingToken);
        return compoundNode;
    }

}

