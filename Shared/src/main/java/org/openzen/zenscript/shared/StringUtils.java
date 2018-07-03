/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.shared;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Hoofdgebruiker
 */
public class StringUtils {
	private static final Map<String, CharacterEntity> NAMED_CHARACTER_ENTITIES;
	
	private StringUtils() {}
	
	static
	{
		NAMED_CHARACTER_ENTITIES = new HashMap<>();
		
		Properties properties = new Properties();
		try {
			InputStream input = String.class.getResourceAsStream("/org/openzen/zenscript/shared/characterEntities.properties");
			if (input != null)
				properties.load(input);
			else
				System.out.println("Warning: could not load character entities");
		} catch (IOException ex) {
			Logger.getLogger(StringUtils.class.getName()).log(Level.SEVERE, null, ex);
		}
		
		for (Object okey : properties.keySet()) {
			String key = okey.toString();
			char value = (char) Integer.parseInt(properties.getProperty(key));
			CharacterEntity entity = new CharacterEntity(key, value);
			NAMED_CHARACTER_ENTITIES.put(entity.stringValue, entity);
		}
	}
	
	public static String capitalize(String value) {
		return value.substring(0, 1).toUpperCase() + value.substring(1);
	}

	/**
	 * Splits a string in parts, given a specified delimiter.
	 *
	 * @param value string to be split
	 * @param delimiter delimiter
	 * @return split string
	 */
	public static List<String> split(String value, char delimiter)
	{
		if (value == null)
			return null;

		List<String> result = new ArrayList<>();
		int start = 0;
		for (int i = 0; i < value.length(); i++) {
			if (value.charAt(i) == delimiter) {
				result.add(value.substring(start, i));
				start = i + 1;
			}
		}
		result.add(value.substring(start));
		return result;
	}
	
	/**
	 * Left pads (prefixes) a string with characters until it reaches the given string
	 * length. Does not do anything if the string length &gt;= given length.
	 * 
	 * @param value value to be padded
	 * @param length desired string length
	 * @param c padding character
	 * @return padded string
	 */
	public static String lpad(String value, int length, char c)
	{
		if (value.length() >= length)
			return value;
		
		return times(c, length - value.length()) + value;
	}
	
	/**
	 * Right pads (suffixes) a string with characters until it reaches the given
	 * string length. Does not do anything if the string length &gt;= given length.
	 * 
	 * @param value value to be padded
	 * @param length desired string length
	 * @param c padding character
	 * @return padded string
	 */
	public static String rpad(String value, int length, char c)
	{
		if (value.length() >= length)
			return value;
		
		return value + times(c, length - value.length());
	}
	
	/**
	 * Constructs a string with count times the given character.
	 * 
	 * @param c filling character
	 * @param count character count
	 * @return string value
	 */
	public static String times(char c, int count)
	{
		char[] value = new char[count];
		for (int i = 0; i < count; i++) {
			value[i] = c;
		}
		return new String(value);
	}
	
	/**
	 * Unescapes a string escaped in one of following ways:
	 * 
	 * <ul>
	 * <li>A string escaped with single quotes (<code>'Hello "my" world'</code>)</li>
	 * <li>A string escaped with double quotes (<code>"Hello 'my' world"</code>)</li>
	 * <li>A near-literal string (<code>@"C:\Program Files\"</code>) in which escape sequences
	 * aren't processed but the " character cannot occur</li>
	 * </ul>
	 * 
	 * The following escape sequences are recognized:
	 * <ul>
	 * <li>\\</li>
	 * <li>\'</li>
	 * <li>\"</li>
	 * <li>\&amp;namedCharacterEntity; (note that although redundant, \&amp;#ddd; and \&amp;#xXXXX; are also allowed)</li>
	 * <li>\t</li>
	 * <li>\n</li>
	 * <li>\r</li>
	 * <li>\b</li>
	 * <li>\f</li>
	 * <li>\&amp;uXXXX for unicode character points</li>
	 * </ul>
	 * 
	 * @param escapedString escaped string
	 * @return unescaped string
	 */
	public static String unescape(String escapedString)
	{
		if (escapedString.length() < 2)
			throw new IllegalArgumentException("String is not quoted");
		
		boolean isLiteral = escapedString.charAt(0) == '@';
		if (isLiteral)
			escapedString = escapedString.substring(1);
		
		if (escapedString.charAt(0) != '"' && escapedString.charAt(0) != '\'')
			throw new IllegalArgumentException("String is not quoted");
		
		char quoteCharacter = escapedString.charAt(0);
		if (escapedString.charAt(escapedString.length() - 1) != quoteCharacter)
			throw new IllegalArgumentException("Unbalanced quotes");
		
		if (isLiteral)
			return escapedString.substring(1, escapedString.length() - 1);
		
		StringBuilder result = new StringBuilder(escapedString.length() - 2);
		
		for (int i = 1; i < escapedString.length() - 1; i++) {
			if (escapedString.charAt(i) == '\\') {
				if (i >= escapedString.length() - 1)
					throw new IllegalArgumentException("Unfinished escape sequence");
				
				switch (escapedString.charAt(i + 1)) {
					case '\\': i++; result.append('\\'); break;
					case '&':
						CharacterEntity characterEntity = readCharacterEntity(escapedString, i + 1);
						i += characterEntity.stringValue.length() + 2;
						result.append(characterEntity.charValue);
						break;
					case 't': i++; result.append('\t'); break;
					case 'r': i++; result.append('\r'); break;
					case 'n': i++; result.append('\n'); break;
					case 'b': i++; result.append('\b'); break;
					case 'f': i++; result.append('\f'); break;
					case '"': i++; result.append('\"'); break;
					case '\'': i++; result.append('\''); break;
					case 'u':
						if (i >= escapedString.length() - 5)
							throw new IllegalArgumentException("Unfinished escape sequence");
						int hex0 = readHexCharacter(escapedString.charAt(i + 2));
						int hex1 = readHexCharacter(escapedString.charAt(i + 3));
						int hex2 = readHexCharacter(escapedString.charAt(i + 4));
						int hex3 = readHexCharacter(escapedString.charAt(i + 5));
						i += 5;
						result.append((hex0 << 12) | (hex1 << 8) | (hex2 << 4) | hex3);
					default:
						throw new IllegalArgumentException("Illegal escape sequence");
				}
			}
			else
				result.append(escapedString.charAt(i));
		}
		
		return result.toString();
	}
	
	/**
	 * Escapes special characters in the given string, including ". (but not ').
	 * Adds opening and closing quotes.
	 * 
	 * @param value value to be escaped
	 * @param quote character (' or ")
	 * @param escapeUnicode true to escape any non-ascii value, false to leave them be
	 * @return escaped value
	 */
	public static String escape(String value, char quote, boolean escapeUnicode)
	{
		StringBuilder output = new StringBuilder();
		output.append(quote);
		for (char c : value.toCharArray()) {
			switch (c) {
				case '"':
					if (quote == '"')
						output.append("\\\"");
					break;
				case '\'':
					if (quote == '\'')
						output.append("\\\'");
					break;
				case '\n': output.append("\\n"); break;
				case '\r': output.append("\\r"); break;
				case '\t': output.append("\\t"); break;
				case '\\': output.append("\\\\"); break;
				default:
					if (escapeUnicode && c > 127) {
						output.append("\\u");
						output.append(lpad(Integer.toHexString(c), 4, '0'));
					} else {
						output.append(c);
					}
			}
		}
		
		output.append(quote);
		return output.toString();
	}
	
	/**
	 * Reads a single hex digit and converts it to a value 0-15.
	 * 
	 * @param hex hex digit
	 * @return converted value
	 */
	private static int readHexCharacter(char hex)
	{
		if (hex >= '0' && hex <= '9')
			return hex - '0';
		
		if (hex >= 'A' && hex <= 'F')
			return hex - 'A' + 10;
		
		if (hex >= 'a' && hex <= 'f')
			return hex - 'a' + 10;
		
		throw new IllegalArgumentException("Illegal hex character: " + hex);
	}
	
	/**
	 * Retrieves all official named character entities.
	 * 
	 * @return named character entities
	 */
	public static Collection<CharacterEntity> getNamedCharacterEntities()
	{
		return NAMED_CHARACTER_ENTITIES.values();
	}
	
	/**
	 * Reads a single character entity (formatted as &amp;characterEntity;) at the
	 * given string offset.
	 * 
	 * The following formats are supported:
	 * <ul>
	 * <li>&amp;namedCharacterEntity;</li>
	 * <li>&amp;#ddd</li>
	 * <li>&amp;#xXXXX</li>
	 * </ul>
	 * 
	 * The returned value includes the character entity, without the enclosing
	 * &amp; and ; characters.
	 * 
	 * @param str string value to search in
	 * @param offset offset to look at
	 * @return character entity
	 * @throws IllegalArgumentException if the given string does not contain a
	 *	valid character entity at the given position
	 */
	private static CharacterEntity readCharacterEntity(String str, int offset)
	{
		if (offset + 3 >= str.length())
			throw new IllegalArgumentException("Not a proper character entity");
		if (str.charAt(offset) != '&')
			throw new IllegalArgumentException("Not a proper character entity");
		
		int semi = str.indexOf(';', offset);
		if (semi < 0)
			throw new IllegalArgumentException("Not a proper character entity");
		
		String entity = str.substring(offset + 1, semi);
		if (entity.isEmpty())
			throw new IllegalArgumentException("Not a proper character entity");
		
		if (NAMED_CHARACTER_ENTITIES.containsKey(entity))
			return NAMED_CHARACTER_ENTITIES.get(entity);
		
		if (entity.charAt(0) == '#') {
			if (entity.length() < 2)
				throw new IllegalArgumentException("Not a proper character entity");
			
			if (str.charAt(1) == 'x') {
				// hex character entity
				if (entity.length() != 7)
					throw new IllegalArgumentException("Not a proper character entity");
				
				int ivalue = Integer.parseInt(entity.substring(2), 16);
				return new CharacterEntity(entity, (char) ivalue);
			} else {
				// decimal character entity
				int ivalue = Integer.parseInt(entity.substring(1));
				return new CharacterEntity(entity, (char) ivalue);
			}
		}
		
		throw new IllegalArgumentException("Not a valid named character entity");
	}
	
	public static class CharacterEntity
	{
		public char charValue;
		public String stringValue;
		
		public CharacterEntity(String stringValue, char charValue)
		{
			this.charValue = charValue;
			this.stringValue = stringValue;
		}
	}
}
