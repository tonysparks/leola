package leola.frontend.tokens;

import static leola.frontend.tokens.TokenType.ERROR;

import leola.frontend.ErrorCode;
import leola.frontend.Source;


/**
 * Leola error token
 * 
 * @author Tony
 *
 */
public class ErrorToken extends Token {
    
    /**
     * @param source
     *            the source from where to fetch subsequent characters.
     * @param errorCode
     *            the error code.
     * @param tokenText
     *            the text of the erroneous token.
     */
    public ErrorToken(Source source, ErrorCode errorCode, String tokenText) {
        super(source);

        this.text = tokenText;
        this.type = ERROR;
        this.value = errorCode;
    }

    @Override
    protected void extract() {
    }
}
