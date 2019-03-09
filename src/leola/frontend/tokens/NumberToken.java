package leola.frontend.tokens;

import static leola.frontend.ErrorCode.INVALID_NUMBER;
import static leola.frontend.ErrorCode.RANGE_INTEGER;
import static leola.frontend.ErrorCode.RANGE_LONG;
import static leola.frontend.ErrorCode.RANGE_REAL;
import static leola.frontend.tokens.TokenType.ERROR;
import static leola.frontend.tokens.TokenType.INTEGER;
import static leola.frontend.tokens.TokenType.LONG;
import static leola.frontend.tokens.TokenType.REAL;

import java.math.BigInteger;

import leola.frontend.Source;

/**
 * Number Token, parses number formats
 * 
 * @author Tony
 *
 */
public class NumberToken extends Token {
    private static final int MAX_EXPONENT = 37;

    public NumberToken(Source source) {
        super(source);
    }
    
    @Override
    protected void extract() {    
        StringBuilder textBuffer = new StringBuilder(); // token's characters
        extractNumber(textBuffer);
        text = textBuffer.toString();        
    }

    /**
     * Extract a Leola number token from the source.
     * 
     * @param textBuffer
     *            the buffer to append the token's characters.
     */
    protected void extractNumber(StringBuilder textBuffer) {
        String wholeDigits = null; // digits before the decimal point
        String fractionDigits = null; // digits after the decimal point
        String exponentDigits = null; // exponent digits
        char exponentSign = '+'; // exponent sign '+' or '-'
        boolean sawDotDot = false; // true if saw .. token
        char currentChar; // current character

        type = INTEGER; // assume INTEGER token type for now

        // Extract the digits of the whole part of the number.
        wholeDigits = unsignedIntegerDigits(textBuffer);
        if (type == ERROR) {
            return;
        }

        // Is there a . ?
        // It could be a decimal point or the start of a .. token.
        currentChar = this.source.currentChar();
        if (currentChar == '.') {
            if (this.source.peekChar() == '.') {
                sawDotDot = true; // it's a ".." token, so don't consume it
            }
            else {
                type = REAL; // decimal point, so token type is REAL
                textBuffer.append(currentChar);
                currentChar = this.source.nextChar(); // consume decimal point

                // Collect the digits of the fraction part of the number.
                if(Character.isDigit(currentChar)) {
                    fractionDigits = unsignedIntegerDigits(textBuffer);
                    if (type == ERROR) {
                        return;
                    }
                }
            }
        }
        else if (currentChar == 'L') {
            type = LONG;
            this.source.nextChar();

            long longValue = computeLongValue(wholeDigits);
            value = new Long(longValue);
            return;
        }

        // Is there an exponent part?
        // There cannot be an exponent if we already saw a ".." token.
        currentChar = this.source.currentChar();
        if (!sawDotDot && ((currentChar == 'E') || (currentChar == 'e'))) {
            type = REAL; // exponent, so token type is REAL
            textBuffer.append(currentChar);
            currentChar = this.source.nextChar(); // consume 'E' or 'e'

            // Exponent sign?
            if ((currentChar == '+') || (currentChar == '-')) {
                textBuffer.append(currentChar);
                exponentSign = currentChar;
                currentChar = this.source.nextChar(); // consume '+' or '-'
            }

            // Extract the digits of the exponent.
            exponentDigits = unsignedIntegerDigits(textBuffer);
        }

        // Compute the value of an integer number token.
        if (type == INTEGER) {
            int integerValue = computeIntegerValue(wholeDigits);

            if (type != ERROR) {
                value = new Integer(integerValue);
            }
            else if (value == RANGE_INTEGER) {
                type = LONG;

                long longValue = computeLongValue(wholeDigits);
                value = new Long(longValue);
            }
        }

        // Compute the value of a real number token.
        else if (type == REAL) {
            double floatValue = computeFloatValue(wholeDigits, fractionDigits, exponentDigits, exponentSign);

            if (type != ERROR) {
                value = new Double(floatValue);
            }
        }

    }

    /**
     * Extract and return the digits of an unsigned integer.
     * 
     * @param textBuffer
     *            the buffer to append the token's characters.
     * @return the string of digits.
     */
    private String unsignedIntegerDigits(StringBuilder textBuffer) {
        char currentChar = this.source.currentChar();

        // Must have at least one digit.
        if (!Character.isDigit(currentChar)) {
            type = ERROR;
            value = INVALID_NUMBER;
            return null;
        }

        boolean isHex = false;

        // Extract the digits.
        StringBuilder digits = new StringBuilder();
        while (Character.isDigit(currentChar) || 
               ('_' == currentChar) ||
               ('x' == currentChar && !isHex) || 
               (isHex && isHexDigit(currentChar))) {

            if ('x' == currentChar) {
                isHex = true;
            }            
            else if ('_' == currentChar) {
                currentChar = this.source.nextChar(); // consume _
                continue;
            }

            textBuffer.append(currentChar);
            digits.append(currentChar);
            currentChar = this.source.nextChar(); // consume digit
        }
        
        String digitsStr = digits.toString();
        if(isHex) {
            if(!digitsStr.startsWith("0x")) {
                type = ERROR;
                value = INVALID_NUMBER;
                return null;    
            }
        }

        return digitsStr;
    }

    /**
     * If the character is a Hex digit
     * 
     * @param c
     * @return
     */
    private boolean isHexDigit(char c) {
        return ((c >= 48 && c <= 57) || // 0 - 9
                (c >= 65 && c <= 70) || // A - F
                (c >= 97 && c <= 102)   // a - f
        );
    }

    /**
     * Compute and return the integer value of a string of digits. Check for
     * overflow.
     * 
     * @param digits
     *            the string of digits.
     * @return the integer value.
     */
    private int computeIntegerValue(String digits) {
        // Return 0 if no digits.
        if (digits == null) {
            return 0;
        }

        /* If it's a HEX number, parse it out */
        if (digits.contains("0x")) {
            if (digits.length() > "0xFFFFFFFF".length()) {
                // Overflow: Set the integer out of range error.
                type = ERROR;
                value = RANGE_INTEGER;
                return 0;
            }

            return new BigInteger(digits.replace("0x", ""), 16).intValue();
        }

        int integerValue = 0;
        int prevValue = -1; // overflow occurred if prevValue > integerValue
        int index = 0;

        // Loop over the digits to compute the integer value
        // as long as there is no overflow.
        while ((index < digits.length()) && (integerValue >= prevValue)) {
            prevValue = integerValue;
            integerValue = 10 * integerValue + Character.getNumericValue(digits.charAt(index++));
        }

        // No overflow: Return the integer value.
        if (integerValue >= prevValue) {
            return integerValue;
        }

        // Overflow: Set the integer out of range error.
        else {
            type = ERROR;
            value = RANGE_INTEGER;
            return 0;
        }
    }

    /**
     * Compute and return the long value of a string of digits. Check for
     * overflow.
     * 
     * @param digits
     *            the string of digits.
     * @return the integer value.
     */
    private long computeLongValue(String digits) {
        // Return 0 if no digits.
        if (digits == null) {
            return 0L;
        }

        /* If it's a HEX number, parse it out */
        if (digits.contains("0x")) {
            if (digits.length() > "0xFFFFFFFFFFFFFFFF".length()) {
                // Overflow: Set the integer out of range error.
                type = ERROR;
                value = RANGE_LONG;
                return 0L;
            }

            return Long.parseLong(digits.replace("0x", ""), 16);
        }

        long longValue = 0L;
        long prevValue = -1L; // overflow occurred if prevValue > integerValue
        int index = 0;

        // Loop over the digits to compute the integer value
        // as long as there is no overflow.
        while ((index < digits.length()) && (longValue >= prevValue)) {
            prevValue = longValue;
            longValue = 10 * longValue + Character.getNumericValue(digits.charAt(index++));
        }

        // No overflow: Return the integer value.
        if (longValue >= prevValue) {
            return longValue;
        }

        // Overflow: Set the integer out of range error.
        else {
            type = ERROR;
            value = RANGE_LONG;
            return 0L;
        }
    }

    /**
     * Compute and return the float value of a real number.
     * 
     * @param wholeDigits
     *            the string of digits before the decimal point.
     * @param fractionDigits
     *            the string of digits after the decimal point.
     * @param exponentDigits
     *            the string of exponent digits.
     * @param exponentSign
     *            the exponent sign.
     * @return the float value.
     */
    private double computeFloatValue(String wholeDigits, String fractionDigits, String exponentDigits, char exponentSign) {
        double floatValue = 0.0;
        int exponentValue = computeIntegerValue(exponentDigits);
        String digits = wholeDigits; // whole and fraction digits

        // Negate the exponent if the exponent sign is '-'.
        if (exponentSign == '-') {
            exponentValue = -exponentValue;
        }

        // If there are any fraction digits, adjust the exponent value
        // and append the fraction digits.
        if (fractionDigits != null) {
            exponentValue -= fractionDigits.length();
            digits += fractionDigits;
        }

        // Check for a real number out of range error.
        if (Math.abs(exponentValue + wholeDigits.length()) > MAX_EXPONENT) {
            type = ERROR;
            value = RANGE_REAL;
            return 0.0f;
        }

        // Loop over the digits to compute the float value.
        int index = 0;
        while (index < digits.length()) {
            floatValue = 10 * floatValue + Character.getNumericValue(digits.charAt(index++));
        }

        // Adjust the float value based on the exponent value.
        if (exponentValue != 0) {
            floatValue *= Math.pow(10, exponentValue);
        }

        return floatValue;
    }
}
