/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.definitions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.definition.AliasDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.partial.PartialTypeExpression;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.scope.DefinitionScope;
import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.type.GenericName;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.ParsedDefinition;
import org.openzen.zenscript.parser.PrecompilationState;
import org.openzen.zenscript.parser.type.IParsedType;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedAlias extends ParsedDefinition {
	public static ParsedAlias parseAlias(ZSPackage pkg, CodePosition position, int modifiers, ParsedAnnotation[] annotations, ZSTokenParser tokens, HighLevelDefinition outerDefinition) {
		String name = tokens.required(ZSTokenType.T_IDENTIFIER, "identifier expected").content;
		List<ParsedTypeParameter> parameters = ParsedTypeParameter.parseAll(tokens);
		tokens.required(ZSTokenType.K_AS, "as expected");
		IParsedType type = IParsedType.parse(tokens);
		tokens.required(ZSTokenType.T_SEMICOLON, "; expected");
		return new ParsedAlias(pkg, position, modifiers, annotations, name, parameters, type, outerDefinition);
	}
	
	private final String name;
	private final List<ParsedTypeParameter> parameters;
	private final IParsedType type;
	
	private final AliasDefinition compiled;
	
	public ParsedAlias(ZSPackage pkg, CodePosition position, int modifiers, ParsedAnnotation[] annotations, String name, List<ParsedTypeParameter> parameters, IParsedType type, HighLevelDefinition outerDefinition) {
		super(position, modifiers, annotations);
		
		this.name = name;
		this.parameters = parameters;
		this.type = type;
		
		compiled = new AliasDefinition(position, pkg, name, modifiers, outerDefinition);
		
		if (parameters != null && parameters.size() > 0) {
			TypeParameter[] typeParameters = new TypeParameter[parameters.size()];
			for (int i = 0; i < parameters.size(); i++) {
				typeParameters[i] = parameters.get(i).compiled;
			}
			compiled.setTypeParameters(typeParameters);
		}
	}
	
	@Override
	public void compileTypes(BaseScope scope) {
		BaseScope innerScope = new CompileTypeScope(scope);
		compiled.setType(type.compile(innerScope));
	}
	
	@Override
	public HighLevelDefinition getCompiled() {
		return compiled;
	}

	@Override
	public void compileMembers(BaseScope scope) {
		DefinitionScope innerScope = new DefinitionScope(scope, compiled);
		for (int i = 0; i < compiled.genericParameters.length; i++) {
			TypeParameter output = compiled.genericParameters[i];
			ParsedTypeParameter input = this.parameters.get(i);
			for (ParsedGenericBound bound : input.bounds) {
				output.addBound(bound.compile(innerScope));
			}
		}
	}
	
	@Override
	public void listMembers(BaseScope scope, PrecompilationState state) {
		// nothing to do
	}
	
	@Override
	public void precompile(BaseScope scope, PrecompilationState state) {
		// nothing to do
	}

	@Override
	public void compileCode(BaseScope scope, PrecompilationState state) {
		// nothing to do
	}

	@Override
	public void linkInnerTypes() {
	}
	
	public class CompileTypeScope extends BaseScope {
		private final BaseScope outer;
		private final Map<String, TypeParameter> typeParameters = new HashMap<>();

		public CompileTypeScope(BaseScope outer) {
			this.outer = outer;
			
			if (parameters != null)
				for (ParsedTypeParameter parameter : parameters)
					typeParameters.put(parameter.name, parameter.compiled);
		}

		@Override
		public LocalMemberCache getMemberCache() {
			return outer.getMemberCache();
		}

		@Override
		public IPartialExpression get(CodePosition position, GenericName name) {
			if (typeParameters.containsKey(name.name) && !name.hasArguments())
				return new PartialTypeExpression(position, getTypeRegistry().getGeneric(typeParameters.get(name.name)), name.arguments);

			return outer.get(position, name);
		}

		@Override
		public ITypeID getType(CodePosition position, List<GenericName> name) {
			if (typeParameters.containsKey(name.get(0).name) && name.size() == 1 && !name.get(0).hasArguments()) {
				return getTypeRegistry().getGeneric(typeParameters.get(name.get(0).name));
			}

			return outer.getType(position, name);
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
			throw new UnsupportedOperationException("Not available at this stage");
		}

		@Override
		public Function<CodePosition, Expression> getDollar() {
			return outer.getDollar();
		}

		@Override
		public IPartialExpression getOuterInstance(CodePosition position) {
			return null;
		}

		@Override
		public AnnotationDefinition getAnnotation(String name) {
			return outer.getAnnotation(name);
		}
	}
}
