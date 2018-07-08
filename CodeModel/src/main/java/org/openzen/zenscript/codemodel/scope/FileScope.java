/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.scope;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.AccessScope;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.PackageDefinitions;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.partial.PartialGlobalExpression;
import org.openzen.zenscript.codemodel.partial.PartialTypeExpression;
import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.GenericName;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ISymbol;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;

/**
 *
 * @author Hoofdgebruiker
 */
public class FileScope extends BaseScope {
	private final LocalMemberCache memberCache;
	private final ZSPackage rootPackage;
	private final PackageDefinitions packageDefinitions;
	private final GlobalTypeRegistry globalRegistry;
	private final Map<String, HighLevelDefinition> importedTypes = new HashMap<>();
	private final Map<String, ISymbol> globalSymbols;
	private final Map<String, AnnotationDefinition> annotations = new HashMap<>();
	
	public FileScope(
			AccessScope access,
			ZSPackage rootPackage,
			PackageDefinitions packageDefinitions,
			GlobalTypeRegistry globalRegistry,
			List<ExpansionDefinition> expansions,
			Map<String, ISymbol> globalSymbols,
			List<AnnotationDefinition> annotations) {
		this.rootPackage = rootPackage;
		this.packageDefinitions = packageDefinitions;
		this.globalRegistry = globalRegistry;
		this.globalSymbols = globalSymbols;
		
		memberCache = new LocalMemberCache(access, globalRegistry, expansions);
		
		for (AnnotationDefinition annotation : annotations) {
			this.annotations.put(annotation.getAnnotationName(), annotation);
		}
	}
	
	@Override
	public LocalMemberCache getMemberCache() {
		return memberCache;
	}
	
	public void register(String name, HighLevelDefinition type) {
		importedTypes.put(name, type);
	}

	@Override
	public IPartialExpression get(CodePosition position, GenericName name) {
		if (importedTypes.containsKey(name.name))
			return new PartialTypeExpression(position, getTypeRegistry().getForDefinition(importedTypes.get(name.name), name.arguments), name.arguments);
		
		HighLevelDefinition localDefinition = packageDefinitions.getDefinition(name.name);
		if (localDefinition != null)
			return new PartialTypeExpression(position, globalRegistry.getForDefinition(localDefinition, name.arguments), name.arguments);
		
		if (globalSymbols.containsKey(name.name)) {
			IPartialExpression resolution = globalSymbols.get(name.name).getExpression(position, globalRegistry, name.arguments);
			return new PartialGlobalExpression(position, name.name, resolution, name.arguments);
		}
		
		return rootPackage.getMember(position, globalRegistry, name);
	}

	@Override
	public ITypeID getType(CodePosition position, List<GenericName> name) {
		if (importedTypes.containsKey(name.get(0).name)) {
			HighLevelDefinition definition = importedTypes.get(name.get(0).name);
			ITypeID type = getTypeRegistry().getForDefinition(definition, name.get(0).arguments);
			for (int i = 1; i < name.size(); i++)
				type = getTypeMembers(type).getInnerType(position, name.get(i));
			return type;
		}
		
		HighLevelDefinition localDefinition = packageDefinitions.getDefinition(name.get(0).name);
		if (localDefinition != null) {
			DefinitionTypeID type = globalRegistry.getForDefinition(localDefinition, name.get(0).arguments);
			for (int i = 1; i < name.size(); i++) {
				type = getTypeMembers(type).getInnerType(position, name.get(i));
				if (type == null)
					break;
			}
			
			// TODO: take care of non-static inner classes in generic classes!
			if (type != null && name.get(name.size() - 1).getNumberOfArguments() != type.definition.getNumberOfGenericParameters())
				throw new CompileException(position, CompileExceptionCode.TYPE_ARGUMENTS_INVALID_NUMBER, "Invalid number of type arguments");
			
			if (type != null)
				return type;
		} else if (globalSymbols.containsKey(name.get(0).name)) {
			ITypeID type = globalSymbols.get(name.get(0).name).getType(position, globalRegistry, name.get(0).arguments);
			for (int i = 1; i < name.size(); i++) {
				type = getTypeMembers(type).getInnerType(position, name.get(i));
				if (type == null)
					break;
			}
			
			if (type != null)
				return type;
		}
		
		return rootPackage.getType(position, this, name);
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
	public ITypeID getThisType() {
		return null;
	}

	@Override
	public Function<CodePosition, Expression> getDollar() {
		return null;
	}
	
	@Override
	public IPartialExpression getOuterInstance(CodePosition position) {
		throw new CompileException(position, CompileExceptionCode.NO_OUTER_BECAUSE_OUTSIDE_TYPE, "Not in an inner type");
	}

	@Override
	public AnnotationDefinition getAnnotation(String name) {
		return annotations.get(name);
	}
}
