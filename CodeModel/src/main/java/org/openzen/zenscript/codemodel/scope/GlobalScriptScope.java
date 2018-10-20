/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.scope;

import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPreparer;
import org.openzen.zenscript.codemodel.type.storage.StorageTag;

/**
 *
 * @author Hoofdgebruiker
 */
public class GlobalScriptScope extends StatementScope {
	private final BaseScope file;
	
	public GlobalScriptScope(BaseScope file) {
		this.file = file;
	}

	@Override
	public LocalMemberCache getMemberCache() {
		return file.getMemberCache();
	}
	
	@Override
	public IPartialExpression get(CodePosition position, GenericName name) throws CompileException {
		IPartialExpression result = super.get(position, name);
		if (result != null)
			return result;
		
		return file.get(position, name);
	}

	@Override
	public TypeID getType(CodePosition position, List<GenericName> name) {
		return file.getType(position, name);
	}

	@Override
	public StorageTag getStorageTag(CodePosition position, String name, String[] parameters) {
		return file.getStorageTag(position, name, parameters);
	}

	@Override
	public LoopStatement getLoop(String name) {
		return null;
	}

	@Override
	public FunctionHeader getFunctionHeader() {
		return null;
	}

	@Override
	public StoredType getThisType() {
		return null;
	}

	@Override
	public DollarEvaluator getDollar() {
		return null; // script arguments?
	}

	@Override
	public IPartialExpression getOuterInstance(CodePosition position) {
		return null;
	}

	@Override
	public AnnotationDefinition getAnnotation(String name) {
		return file.getAnnotation(name);
	}

	@Override
	public TypeMemberPreparer getPreparer() {
		return file.getPreparer();
	}

	@Override
	public GenericMapper getLocalTypeParameters() {
		return GenericMapper.EMPTY;
	}
}
