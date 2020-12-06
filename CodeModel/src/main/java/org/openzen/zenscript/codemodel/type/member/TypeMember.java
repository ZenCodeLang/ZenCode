package org.openzen.zenscript.codemodel.type.member;

import org.openzen.zenscript.codemodel.member.ref.DefinitionMemberRef;

public class TypeMember<T extends DefinitionMemberRef> {
	public final TypeMemberPriority priority;
	public final T member;

	public TypeMember(TypeMemberPriority priority, T member) {
		this.priority = priority;
		this.member = member;
	}

	public TypeMember<T> resolve(TypeMember<T> other) {
		if (priority == other.priority)
			return this; // this is actually an error; but that error will be reported through the validator

		if (priority.compareTo(other.priority) < 0) {
			return other;
		} else {
			return this;
		}
	}
}
