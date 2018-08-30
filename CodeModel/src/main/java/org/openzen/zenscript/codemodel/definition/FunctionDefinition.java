/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.definition;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.codemodel.member.CallerMember;
import org.openzen.zenscript.codemodel.member.ref.FunctionalMemberRef;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.member.DefinitionMemberGroup;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;

/**
 *
 * @author Hoofdgebruiker
 */
public class FunctionDefinition extends HighLevelDefinition {
	public FunctionHeader header;
	public Statement statement;
	public CallerMember caller;
	public final DefinitionMemberGroup callerGroup;
	
	public FunctionDefinition(CodePosition position, Module module, ZSPackage pkg, String name, int modifiers, HighLevelDefinition outerDefinition) {
		super(position, module, pkg, name, modifiers, outerDefinition);
		callerGroup = new DefinitionMemberGroup(true, name);
	}
	
	public FunctionDefinition(CodePosition position, Module module, ZSPackage pkg, String name, int modifiers, FunctionHeader header) {
		this(position, module, pkg, name, modifiers, (HighLevelDefinition) null);
		setHeader(header);
	}
	
	public void setHeader(FunctionHeader header) {
		this.header = header;
		addMember(caller = new CallerMember(position, this, modifiers | Modifiers.STATIC, header, null));
		callerGroup.addMethod(new FunctionalMemberRef(caller, GenericMapper.EMPTY), TypeMemberPriority.SPECIFIED);
	}
	
	public void setCode(Statement statement) {
		this.statement = statement;
		caller.setBody(statement);
	}

	@Override
	public <T> T accept(DefinitionVisitor<T> visitor) {
		return visitor.visitFunction(this);
	}
}
