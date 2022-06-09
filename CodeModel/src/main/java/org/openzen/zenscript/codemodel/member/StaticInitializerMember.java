package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.ConcatMap;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.member.ref.DefinitionMemberRef;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;

public class StaticInitializerMember extends DefinitionMember {
	public Statement body;

	public StaticInitializerMember(CodePosition position, HighLevelDefinition definition) {
		super(position, definition, 0);
	}

	@Override
	public BuiltinID getBuiltin() {
		return null;
	}

	@Override
	public String describe() {
		return "static initializer";
	}

	@Override
	public void registerTo(TypeMembers type, TypeMemberPriority priority, GenericMapper mapper) {

	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		return visitor.visitStaticInitializer(this);
	}

	@Override
	public <C, R> R accept(C context, MemberVisitorWithContext<C, R> visitor) {
		return visitor.visitStaticInitializer(context, this);
	}

	@Override
	public DefinitionMemberRef getOverrides() {
		return null;
	}

	@Override
	public int getEffectiveModifiers() {
		return modifiers;
	}

	@Override
	public boolean isAbstract() {
		return false;
	}

	@Override
	public DefinitionMemberRef ref(TypeID type, GenericMapper mapper) {
		throw new UnsupportedOperationException("Cannot reference a static initializer");
	}

	@Override
	public FunctionHeader getHeader() {
		return new FunctionHeader(BasicTypeID.VOID);
	}
}
