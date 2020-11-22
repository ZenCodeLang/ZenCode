package org.openzen.zenscript.lexer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Represents a compiled DFA. A compiled DFA has a compact representation and
 * is immediately usable for efficient processing.
 *
 * A compiled DFA can be converted to a compact integer array. This array can
 * then be hardcoded in an application.
 *
 * @param <T> final type
 */
public class CompiledDFA<T>
{
	public static <T extends TokenType & Comparable<T>> CompiledDFA<T> createLexerDFA(T[] tokenTypes, Class<T> tokenClass)
	{
		List<T> tokens = new ArrayList<>();
		List<String> regexps = new ArrayList<>();
		for (T tokenType : tokenTypes) {
			if (tokenType.getRegexp() != null) {
				tokens.add(tokenType);
				regexps.add(tokenType.getRegexp());
			}
		}
		
		return new NFA<>(regexps, tokens, tokenClass).compile();
	}
	
    public Map<Integer, Integer>[] transitions;
    public T[] finals;

    /**
     * Constructs a compiled DFA from the specified transition graph and finals
     * arrays.
     *
     * The transition array specifies all transitions for each state. The finals
     * array specifies the final class index of each state, or NOFINAL if the state
     * is not a final. There can multiple final classes, which can, for example,
     * be used to distinguish token types.
     *
     * @param transitions transitions graph
     * @param finals finals
     */
    public CompiledDFA(Map<Integer, Integer>[] transitions, T[] finals)
	{
        this.transitions = transitions;
        this.finals = finals;
    }
	
	/**
	 * Determines the token type for the given token. Returns null if the
	 * given token is not a valid token.
	 * 
	 * @param value token value
	 * @return token type
	 */
	public T eval(String value)
	{
		int state = 0;
		
		for (char c : value.toCharArray()) {
			state = transitions[state].get(c);
			if (state == Integer.MIN_VALUE)
				return null;
		}
		
		return finals[state];
	}
	
	/**
	 * Checks if this value evaluates to a valid token.
	 * 
	 * @param value value to check
	 * @return true if matching, false otherwise
	 */
	public boolean matches(String value)
	{
		return eval(value) != null;
	}

    /* Used for debugging */
    @Override
    public String toString()
	{
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < transitions.length; i++) {
            Map<Integer, Integer> map = transitions[i];

            Iterator<Integer> it = map.keySet().iterator();
            while (it.hasNext()) {
                int v = it.next();
                result.append("edge(");
                result.append(i);
                result.append(", ");
                result.append(v);
                result.append("): ");
                result.append(map.get(v));
                result.append("\r\n");
            }
        }
        for (int i = 0; i < finals.length; i++) {
            if (finals[i] != null) {
                result.append("final(");
                result.append(i);
                result.append("): ");
                result.append(finals[i]);
                result.append("\r\n");
            }
        }
        return result.toString();
    }
}
