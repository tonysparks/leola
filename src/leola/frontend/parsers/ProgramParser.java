/*
    Leola Programming Language
    Author: Tony Sparks
    See license.txt
*/
package leola.frontend.parsers;

import leola.ast.ASTNode;
import leola.ast.ProgramStmt;
import leola.frontend.EofToken;
import leola.frontend.LeolaParser;
import leola.frontend.Token;

/**
 * @author Tony
 *
 */
public class ProgramParser extends StmtParser {

    /**
     * @param parser
     */
    public ProgramParser(LeolaParser parser) {
        super(parser);
    }

    /* (non-Javadoc)
     * @see leola.frontend.parsers.StmtParser#parse(leola.frontend.Token)
     */
    @Override
    public ASTNode parse(Token token) throws Exception {
        ProgramStmt program = new ProgramStmt();
        
        while (!(token instanceof EofToken)) {            
            ASTNode node = parseStmt(token);
            program.addChild(node);

            token = currentToken();
        }

        return program;
    }
}

