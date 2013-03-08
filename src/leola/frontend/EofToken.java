package leola.frontend;

import leola.frontend.tokens.LeolaTokenType;

/**
 * <h1>EofToken</h1>
 *
 * <p>The generic end-of-file token.</p>
 *
 * <p>Copyright (c) 2009 by Ronald Mak</p>
 * <p>For instructional purposes only.  No warranties.</p>
 */
public class EofToken extends Token
{
    /**
     * Constructor.
     * @param source the source from where to fetch subsequent characters.
     * @throws Exception if an error occurred.
     */
    public EofToken(Source source)
        throws Exception
    {
        super(source);
        this.type = LeolaTokenType.END_OF_FILE;
    }
}
