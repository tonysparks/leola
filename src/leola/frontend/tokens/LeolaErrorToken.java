package leola.frontend.tokens;

import static leola.frontend.tokens.TokenType.ERROR;

import leola.frontend.Source;
import leola.frontend.Token;


/**
 * Leola error token
 * 
 * @author Tony
 *
 */
public class LeolaErrorToken extends Token {
    
    /**
     * @param source
     *            the source from where to fetch subsequent characters.
     * @param errorCode
     *            the error code.
     * @param tokenText
     *            the text of the erroneous token.
     */
    public LeolaErrorToken(Source source, LeolaErrorCode errorCode, String tokenText) {
        super(source);

        this.text = tokenText;
        this.type = ERROR;
        this.value = errorCode;
    }

    @Override
    protected void extract() {
    }
}
