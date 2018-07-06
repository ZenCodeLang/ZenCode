/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.member.ref.FunctionalMemberRef;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public abstract class FunctionalMember extends DefinitionMember {
	public final FunctionHeader header;
	public final String name;
	public final BuiltinID builtin;
	public Statement body = null;
	public FunctionalMember overrides = null;
	
	public FunctionalMember(
			CodePosition position,
			HighLevelDefinition definition,
			int modifiers,
			String name,
			FunctionHeader header,
			BuiltinID builtin) {
		super(position, definition, modifiers);
		
		this.name = name;
		this.header = header;
		this.builtin = builtin;
	}
	
	public void setBody(Statement body) {
		this.body = body;
	}
	
	public abstract String getCanonicalName();
	
	public abstract FunctionalKind getKind();
	
	public FunctionalMemberRef ref(GenericMapper mapper) {
		return new FunctionalMemberRef(this, mapper.map(header));
	}
	
	@Override
	public BuiltinID getBuiltin() {
		return builtin;
	}
}
