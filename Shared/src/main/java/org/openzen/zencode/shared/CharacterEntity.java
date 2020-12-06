package org.openzen.zencode.shared;

public final class CharacterEntity {
	public final char charValue;
	public final String stringValue;

	public CharacterEntity(String stringValue, char charValue) {
		this.charValue = charValue;
		this.stringValue = stringValue;
	}

	public char getCharValue() {
		return charValue;
	}

	public String getStringValue() {
		return stringValue;
	}
}
