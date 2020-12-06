package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.member.ref.DefinitionMemberRef;
import org.openzen.zenscript.codemodel.member.ref.IteratorMemberRef;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;

public class IteratorMember extends FunctionalMember {
	private final TypeID[] iteratorTypes;
	public Statement body;
	public IteratorMemberRef overrides;

	public IteratorMember(CodePosition position, HighLevelDefinition definition, int modifiers, TypeID[] iteratorTypes, GlobalTypeRegistry registry, BuiltinID builtin) {
		super(position, definition, modifiers, createIteratorHeader(registry, iteratorTypes), builtin);

		this.iteratorTypes = iteratorTypes;
	}

	private static FunctionHeader createIteratorHeader(GlobalTypeRegistry registry, TypeID[] iteratorTypes) {
		return new FunctionHeader(registry.getIterator(iteratorTypes));
	}

	public void setContent(Statement body) {
		this.body = body;
	}

	@Override
	public String getCanonicalName() {
		return definition.getFullName() + ":iterator:" + iteratorTypes.length;
	}

	public int getLoopVariableCount() {
		return iteratorTypes.length;
	}

	public TypeID[] getLoopVariableTypes() {
		return iteratorTypes;
	}

	@Override
	public void registerTo(TypeMembers type, TypeMemberPriority priority, GenericMapper mapper) {
		type.addIterator(new IteratorMemberRef(this, type.type, mapper == null ? iteratorTypes : mapper.map(iteratorTypes)), priority);
	}

	@Override
	public String describe() {
		return "iterator with " + iteratorTypes.length + " variables";
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		return visitor.visitCustomIterator(this);
	}

	@Override
	public <C, R> R accept(C context, MemberVisitorWithContext<C, R> visitor) {
		return visitor.visitIterator(context, this);
	}

	@Override
	public DefinitionMemberRef getOverrides() {
		return overrides;
	}

	public void setOverrides(IteratorMemberRef overrides) {
		this.overrides = overrides;
	}

	@Override
	public FunctionalKind getKind() {
		return FunctionalKind.ITERATOR;
	}
}
