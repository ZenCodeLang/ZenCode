package org.openzen.zenscript.codemodel.definition;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.*;
import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.codemodel.member.CallerMember;
import org.openzen.zenscript.codemodel.member.ref.FunctionalMemberRef;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.member.TypeMemberGroup;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;

public class FunctionDefinition extends HighLevelDefinition {
	public final TypeMemberGroup callerGroup;
	public FunctionHeader header;
	public CallerMember caller;

	public FunctionDefinition(CodePosition position, Module module, ZSPackage pkg, String name, int modifiers, HighLevelDefinition outerDefinition) {
		super(position, module, pkg, name, modifiers, outerDefinition);
		callerGroup = new TypeMemberGroup(true, name);
	}

	public FunctionDefinition(CodePosition position, Module module, ZSPackage pkg, String name, int modifiers, FunctionHeader header, GlobalTypeRegistry registry) {
		this(position, module, pkg, name, modifiers, (HighLevelDefinition) null);
		setHeader(registry, header);
	}

	public void setHeader(GlobalTypeRegistry registry, FunctionHeader header) {
		this.header = header;
		addMember(caller = new CallerMember(position, this, Modifiers.PUBLIC | Modifiers.STATIC, header, null));
		callerGroup.addMethod(new FunctionalMemberRef(caller, registry.getFunction(header), GenericMapper.EMPTY), TypeMemberPriority.SPECIFIED);
	}

	public void setCode(Statement statement) {
		caller.setBody(statement);
	}

	@Override
	public <T> T accept(DefinitionVisitor<T> visitor) {
		return visitor.visitFunction(this);
	}

	@Override
	public <C, R> R accept(C context, DefinitionVisitorWithContext<C, R> visitor) {
		return visitor.visitFunction(context, this);
	}
}
