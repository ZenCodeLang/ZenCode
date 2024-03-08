package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.Tag;
import org.openzen.zenscript.codemodel.*;
import org.openzen.zenscript.codemodel.annotations.MemberAnnotation;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.MemberSet;

public interface IDefinitionMember {
	CodePosition getPosition();

	Modifiers getSpecifiedModifiers();

	Modifiers getEffectiveModifiers();

	MemberAnnotation[] getAnnotations();

	HighLevelDefinition getDefinition();

	String describe();

	void registerTo(TypeID targetType, MemberSet.Builder members, GenericMapper mapper);

	<T> T accept(MemberVisitor<T> visitor);

	<C, R> R accept(C context, MemberVisitorWithContext<C, R> visitor);

	<T extends Tag> T getTag(Class<T> tag);

	<T extends Tag> void setTag(Class<T> tag, T value);

	<T extends Tag> boolean hasTag(Class<T> tag);

	boolean isAbstract();

	default boolean isConstructor() {
		return false;
	}

	FunctionHeader getHeader();
}
