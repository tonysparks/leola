package leola.frontend.tokens;

import leola.frontend.Source;

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
