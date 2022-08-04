package org.openzen.zenscript.codemodel.definition;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.*;
import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.codemodel.compilation.TypeBuilder;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.member.CallerMember;
import org.openzen.zenscript.codemodel.member.ConstructorMember;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.member.ref.FunctionalMemberRef;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.builtin.BuiltinMethodSymbol;
import org.openzen.zenscript.codemodel.type.member.MemberSet;
import org.openzen.zenscript.codemodel.type.member.TypeMemberGroup;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;

import java.util.List;

import static org.openzen.zencode.shared.CodePosition.BUILTIN;
import static org.openzen.zenscript.codemodel.type.BasicTypeID.VOID;
import static org.openzen.zenscript.codemodel.type.member.BuiltinID.*;
import static org.openzen.zenscript.codemodel.type.member.BuiltinID.FUNCTION_NOTSAME;

public class FunctionDefinition extends HighLevelDefinition {
	public final TypeMemberGroup callerGroup;
	public FunctionHeader header;
	public CallerMember caller;

	public FunctionDefinition(CodePosition position, Module module, ZSPackage pkg, String name, Modifiers modifiers, TypeSymbol outerDefinition) {
		super(position, module, pkg, name, modifiers, outerDefinition);
		callerGroup = new TypeMemberGroup(true, name);
	}

	public FunctionDefinition(CodePosition position, Module module, ZSPackage pkg, String name, Modifiers modifiers, FunctionHeader header, TypeBuilder types) {
		this(position, module, pkg, name, modifiers, null);
		setHeader(types, header);
	}

	public void setHeader(TypeBuilder types, FunctionHeader header) {
		this.header = header;
		addMember(caller = new CallerMember(position, this, new Modifiers(Modifiers.PUBLIC | Modifiers.STATIC), header, null));
		callerGroup.addMethod(new FunctionalMemberRef(caller, types.functionOf(header), GenericMapper.EMPTY), TypeMemberPriority.SPECIFIED);
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

	@Override
	protected void resolveAdditional(TypeID type, MemberSet.Builder members, GenericMapper mapper) {
		members.operator(OperatorType.CALL, new MethodInstance(caller, mapper.map(header), type));
		members.same(new MethodInstance(BuiltinMethodSymbol.FUNCTION_SAME, new FunctionHeader(BasicTypeID.BOOL, type), type));
		members.notSame(new MethodInstance(BuiltinMethodSymbol.FUNCTION_NOTSAME, new FunctionHeader(BasicTypeID.BOOL, type), type));
	}
}
