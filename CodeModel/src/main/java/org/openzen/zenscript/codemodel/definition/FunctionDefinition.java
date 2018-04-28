/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.definition;

import java.util.List;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.member.OperatorMember;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.member.DefinitionMemberGroup;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class FunctionDefinition extends HighLevelDefinition {
	public FunctionHeader header;
	public List<Statement> statements;
	public OperatorMember caller;
	public DefinitionMemberGroup callerGroup = new DefinitionMemberGroup();
	
	public FunctionDefinition(CodePosition position, ZSPackage pkg, String name, int modifiers, HighLevelDefinition outerDefinition) {
		super(position, pkg, name, modifiers, outerDefinition);
	}
	
	public FunctionDefinition(CodePosition position, ZSPackage pkg, String name, int modifiers, FunctionHeader header) {
		this(position, pkg, name, modifiers, (HighLevelDefinition) null);
		setHeader(header);
	}
	
	public void setHeader(FunctionHeader header) {
		this.header = header;
		addMember(caller = new OperatorMember(position, modifiers | Modifiers.MODIFIER_STATIC, OperatorType.CALL, header));
		callerGroup.addMethod(caller, TypeMemberPriority.SPECIFIED);
	}
	
	public void setCode(List<Statement> statements) {
		this.statements = statements;
		caller.setBody(statements);
	}

	@Override
	public <T> T accept(DefinitionVisitor<T> visitor) {
		return visitor.visitFunction(this);
	}
}
