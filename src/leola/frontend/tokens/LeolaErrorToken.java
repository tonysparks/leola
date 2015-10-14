package leola.frontend.tokens;

import static leola.frontend.tokens.LeolaTokenType.ERROR;

import java.io.IOException;

import leola.frontend.Source;


/**
 * Leola error token
 * 
 * @author Tony
 *
 */
public class LeolaErrorToken extends LeolaToken {
    /**
     * Constructor.
     * 
     * @param source
     *            the source from where to fetch subsequent characters.
     * @param errorCode
     *            the error code.
     * @param tokenText
     *            the text of the erroneous token.
     * @throws Exception
     *             if an error occurred.
     */
    public LeolaErrorToken(Source source, LeolaErrorCode errorCode, String tokenText) throws IOException {
        super(source);

        this.text = tokenText;
        this.type = ERROR;
        this.value = errorCode;
    }

    /**
     * Do nothing. Do not consume any source characters.
     * 
     * @throws Exception
     *             if an error occurred.
     */
    @Override
    protected void extract() throws IOException {
    }
}
