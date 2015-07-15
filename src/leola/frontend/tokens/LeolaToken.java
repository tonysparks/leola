package leola.frontend.tokens;

import leola.frontend.Source;
import leola.frontend.Token;

/**
 * The LeolaToken
 * 
 * @author Tony
 *
 */
public class LeolaToken extends Token
{
    /**
     * @param source the source from where to fetch the token's characters.
     * @throws Exception if an error occurred.
     */
    protected LeolaToken(Source source)
        throws Exception {
        super(source);
    }
}
