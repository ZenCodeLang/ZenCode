package org.openzen.zenscript.lexer;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ZSTokenSet {

	private static final ZSTokenSet EMPTY = new ZSTokenSet(Collections.emptySet());

	private final Set<ZSTokenType> tokens;

	public static ZSTokenSet empty() {
		return EMPTY;
	}

	public static ZSTokenSet of(ZSTokenType... tokens) {
		return new ZSTokenSet(new HashSet<>(Arrays.asList(tokens)));
	}

	private ZSTokenSet(Set<ZSTokenType> tokens) {
		this.tokens = tokens;
	}

	public ZSTokenSet and(ZSTokenType... newTokens) {
		Set<ZSTokenType> tokens = new HashSet<>(this.tokens());
		tokens.addAll(Arrays.asList(newTokens));
		return new ZSTokenSet(tokens);
	}

	public ZSTokenSet without(ZSTokenType... removedTokens) {
		Set<ZSTokenType> tokens = new HashSet<>(this.tokens());
		Arrays.asList(removedTokens).forEach(tokens::remove);
		return new ZSTokenSet(tokens);
	}

	public boolean contains(ZSTokenType token) {
		return this.tokens().contains(token);
	}

	public Set<ZSTokenType> tokens() {
		return tokens;
	}
}
