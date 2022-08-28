package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.*;
import org.openzen.zenscript.codemodel.identifiers.MethodID;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.IteratorTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.MemberSet;

import java.util.Optional;

public class IteratorMember extends FunctionalMember {
	private final TypeID[] iteratorTypes;
	public Statement body;
	public MethodInstance overrides;

	public IteratorMember(CodePosition position, HighLevelDefinition definition, Modifiers modifiers, TypeID[] iteratorTypes) {
		super(position, definition, modifiers, MethodID.iterator(iteratorTypes.length), createIteratorHeader(iteratorTypes));

		this.iteratorTypes = iteratorTypes;
	}

	private static FunctionHeader createIteratorHeader(TypeID[] iteratorTypes) {
		return new FunctionHeader(new IteratorTypeID(iteratorTypes));
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
	public String describe() {
		return "iterator with " + iteratorTypes.length + " variables";
	}

	@Override
	public void registerTo(TypeID targetType, MemberSet.Builder members, GenericMapper mapper) {

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
	public FunctionalKind getKind() {
		return FunctionalKind.ITERATOR;
	}

	@Override
	public String getName() {
		return "iterator";
	}

	@Override
	public Optional<OperatorType> getOperator() {
		return Optional.empty();
	}

	@Override
	public Optional<MethodInstance> getOverrides() {
		return Optional.ofNullable(overrides);
	}

	public void setOverrides(MethodInstance overrides) {
		this.overrides = overrides;
	}
}
