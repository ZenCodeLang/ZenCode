/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zenscript.codemodel.annotations.Annotation;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.annotations.DefinitionAnnotation;
import org.openzen.zenscript.codemodel.annotations.MemberAnnotation;
import org.openzen.zenscript.codemodel.annotations.StatementAnnotation;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.scope.StatementScope;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.parser.expression.ParsedCallArguments;
import org.openzen.zenscript.parser.type.IParsedType;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.shared.CompileException;
import org.openzen.zenscript.shared.CompileExceptionCode;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedAnnotation {
	public static final ParsedAnnotation[] NONE = new ParsedAnnotation[0];
	
	public static ParsedAnnotation[] parseAnnotations(ZSTokenParser parser) {
		if (!parser.isNext(ZSTokenType.T_SQOPEN))
			return NONE;
		
		List<ParsedAnnotation> results = new ArrayList<>();
		while (parser.isNext(ZSTokenType.T_SQOPEN)) {
			parser.next();
			CodePosition position = parser.getPosition();
			IParsedType type = IParsedType.parse(parser);
			ParsedCallArguments arguments = ParsedCallArguments.parseForAnnotation(parser);
			parser.required(ZSTokenType.T_SQCLOSE, "] expected");
			results.add(new ParsedAnnotation(position, type, arguments));
		}
		return results.toArray(new ParsedAnnotation[results.size()]);
	}
	
	public static MemberAnnotation[] compileForMember(ParsedAnnotation[] annotations, IDefinitionMember member, BaseScope scope) {
		if (annotations.length == 0)
			return MemberAnnotation.NONE;
		
		MemberAnnotation[] compiled = new MemberAnnotation[annotations.length];
		for (int i = 0; i < annotations.length; i++) {
			compiled[i] = annotations[i].compileForMember(member, scope);
		}
		return compiled;
	}
	
	public static DefinitionAnnotation[] compileForDefinition(ParsedAnnotation[] annotations, HighLevelDefinition definition, BaseScope scope) {
		if (annotations.length == 0)
			return DefinitionAnnotation.NONE;
		
		DefinitionAnnotation[] compiled = new DefinitionAnnotation[annotations.length];
		for (int i = 0; i < annotations.length; i++) {
			compiled[i] = annotations[i].compileForDefinition(definition, scope);
		}
		return compiled;
	}
	
	public static StatementAnnotation[] compileForStatement(ParsedAnnotation[] annotations, Statement statement, StatementScope scope) {
		if (annotations.length == 0)
			return StatementAnnotation.NONE;
		
		StatementAnnotation[] compiled = new StatementAnnotation[annotations.length];
		for (int i = 0; i < annotations.length; i++)
			compiled[i] = annotations[i].compileForStatement(statement, scope);
		return compiled;
	}
	
	public static Annotation[] compileForParameter(ParsedAnnotation[] annotations, FunctionHeader header, FunctionParameter parameter, BaseScope scope) {
		if (annotations.length == 0)
			return Annotation.NONE;
		
		Annotation[] compiled = new Annotation[annotations.length];
		for (int i = 0; i < annotations.length; i++)
			compiled[i] = annotations[i].compileForParameter(header, parameter, scope);
		return compiled;
	}
	
	public final CodePosition position;
	public final IParsedType type;
	public final ParsedCallArguments arguments;
	
	public ParsedAnnotation(CodePosition position, IParsedType type, ParsedCallArguments arguments) {
		this.position = position;
		this.type = type;
		this.arguments = arguments;
	}
	
	public MemberAnnotation compileForMember(IDefinitionMember member, BaseScope scope) {
		AnnotationDefinition annotationType = type.compileAnnotation(scope);
		ExpressionScope evalScope = annotationType.getScopeForMember(member, scope);
		ITypeID[] types = type.compileTypeArguments(scope);
		CallArguments cArguments = arguments.compileCall(position, evalScope, types, annotationType.getInitializers(scope));
		return annotationType.createForMember(position, cArguments);
	}
	
	public DefinitionAnnotation compileForDefinition(HighLevelDefinition definition, BaseScope scope) {
		AnnotationDefinition annotationType = type.compileAnnotation(scope);
		if (annotationType == null)
			throw new CompileException(position, CompileExceptionCode.UNKNOWN_ANNOTATION, "Unknown annotation type: " + type.toString());
		
		ExpressionScope evalScope = annotationType.getScopeForType(definition, scope);
		ITypeID[] types = type.compileTypeArguments(scope);
		CallArguments cArguments = arguments.compileCall(position, evalScope, types, annotationType.getInitializers(scope));
		return annotationType.createForDefinition(position, cArguments);
	}
	
	public StatementAnnotation compileForStatement(Statement statement, StatementScope scope) {
		AnnotationDefinition annotationType = type.compileAnnotation(scope);
		if (annotationType == null)
			throw new CompileException(position, CompileExceptionCode.UNKNOWN_ANNOTATION, "Unknown annotation type: " + type.toString());
		
		ExpressionScope evalScope = annotationType.getScopeForStatement(statement, scope);
		ITypeID[] types = type.compileTypeArguments(scope);
		CallArguments cArguments = arguments.compileCall(position, evalScope, types, annotationType.getInitializers(scope));
		return annotationType.createForStatement(position, cArguments);
	}
	
	public Annotation compileForParameter(FunctionHeader header, FunctionParameter parameter, BaseScope scope) {
		AnnotationDefinition annotationType = type.compileAnnotation(scope);
		if (annotationType == null)
			throw new CompileException(position, CompileExceptionCode.UNKNOWN_ANNOTATION, "Unknown annotation type: " + type.toString());
		
		ExpressionScope evalScope = annotationType.getScopeForParameter(header, parameter, scope);
		ITypeID[] types = type.compileTypeArguments(scope);
		CallArguments cArguments = arguments.compileCall(position, evalScope, types, annotationType.getInitializers(scope));
		return annotationType.createForParameter(position, cArguments);
	}
}
