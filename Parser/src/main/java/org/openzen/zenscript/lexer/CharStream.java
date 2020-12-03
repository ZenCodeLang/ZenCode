package org.openzen.zenscript.lexer;

/**
 * Represents a character stream. Suitable for character by character parsing.
 *
 * Used in the regular expression parser.
 */
public class CharStream
{
    private final char[] data;
    private int index;

    /**
     * Constructs a character stream from a data string.
     *
     * @param data data string
     */
    public CharStream(String data)
	{
        this.data = data.toCharArray();
        index = 0;
    }
	
	public char peek()
	{
		if (index == data.length)
			return 0;
		
		return data[index];
	}

    /**
     * Checks if the next character in the stream equals the specified character.
     *
     * @param ch checked character
     * @return true if the next character equals the specified character
     */
    public boolean peek(char ch)
	{
        if (index == data.length)
			return false;
		
        return data[index] == ch;
    }

    /**
     * Processes an optional character. If the next character does not equal
     * the specified character, nothing will happen.
     *
     * @param ch checked character
     * @return true if the next character equals the specified character
     */
    public boolean optional(char ch)
	{
        if (index == data.length) return false;
        if (data[index] == ch) {
            index++;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Processes an optional range of characters. If the next character is not
     * within the specified bounds, nothing will happen.
     *
     * @param from lower character value bound, inclusive
     * @param to upper character value bound, inclusive
     * @return the processed character
     */
    public char optional(char from, char to)
	{
        if (index == data.length) return 0;
        if (data[index] >= from && data[index] <= to) {
            return data[index++];
        } else {
            return 0;
        }
    }

    /**
     * Processes a required character. If the next character does not equal the
     * specified character, an IllegalArgumentException is thrown.
     *
     * @param ch required character
     */
    public void required(char ch)
	{
        if (index >= data.length) {
            throw new IllegalArgumentException("Expected " + ch + " but got EOF");
		} else if (data[index] != ch) {
            throw new IllegalArgumentException("Expected " + ch + " but got " + data[index]);
        }
        index++;
    }

    /**
     * Processes a required character range. If the next character is not within
     * the specified bounds, an IllegalArgumentException is thrown.
     *
     * @param from lower character value bound, inclusive
     * @param to upper character value bound, inclusive
     * @return the processed character
     */
    public char required(char from, char to)
	{
        if (data[index] < from || data[index] > to)
            throw new IllegalArgumentException("Unexpected character: " + data[index]);
		
        return data[index++];
    }

    /**
     * Returns the next character in the stream.
     *
     * @return the next character
     */
    public char next()
	{
		if (index >= data.length)
			return 0;
		
        return data[index++];
    }

    /**
     * Checks if the end of the stream has been reached.
     *
     * @return true if there are more characters in the stream
     */
    public boolean hasMore()
	{
        return index < data.length;
    }
}
