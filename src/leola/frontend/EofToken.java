package leola.frontend;

import leola.frontend.tokens.TokenType;

/**
 * End of File token
 * 
 * @author Tony
 *
 */
public class EofToken extends Token {
    
    public EofToken(Source source) {
        super(source);
        this.type = TokenType.END_OF_FILE;
    }
}
