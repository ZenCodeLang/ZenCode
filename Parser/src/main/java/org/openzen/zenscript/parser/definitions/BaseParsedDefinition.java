package org.openzen.zenscript.parser.definitions;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.ParsedDefinition;
import org.openzen.zenscript.parser.member.ParsedDefinitionMember;

import java.util.*;

public abstract class BaseParsedDefinition extends ParsedDefinition {
	protected final List<ParsedDefinitionMember> members = new ArrayList<>();

	public BaseParsedDefinition(CodePosition position, Modifiers modifiers, ParsedAnnotation[] annotations) {
		super(position, modifiers, annotations);
	}

	public void addMember(ParsedDefinitionMember member) {
		members.add(member);
	}
}
