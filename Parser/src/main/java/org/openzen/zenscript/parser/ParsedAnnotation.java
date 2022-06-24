package org.openzen.zenscript.parser;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.annotations.*;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.parser.expression.ParsedCallArguments;
import org.openzen.zenscript.parser.type.IParsedType;

import java.util.ArrayList;
import java.util.List;

public class ParsedAnnotation {
	public static final ParsedAnnotation[] NONE = new ParsedAnnotation[0];
	public final CodePosition position;
	public final IParsedType type;
	public final ParsedCallArguments arguments;

	public ParsedAnnotation(CodePosition position, IParsedType type, ParsedCallArguments arguments) {
		this.position = position;
		this.type = type;
		this.arguments = arguments;
	}

	public static ParsedAnnotation[] parseAnnotations(ZSTokenParser parser) throws ParseException {
		if (!parser.isNext(ZSTokenType.T_SQOPEN))
			return NONE;

		List<ParsedAnnotation> results = new ArrayList<>();
		while (parser.isNext(ZSTokenType.T_SQOPEN)) {
			parser.pushMark();
			try {
				parser.next();
			} catch (ParseException ex) {
				parser.popMark();
				parser.recoverUntilTokenOrNewline(ZSTokenType.T_SQCLOSE);
				continue;
			}

			if (!parser.isNext(ZSTokenType.T_IDENTIFIER)) {
				parser.reset();
				break;
			} else {
				parser.popMark();
			}

			CodePosition position = parser.getPosition();
			IParsedType type = IParsedType.parse(parser);
			try {
				ParsedCallArguments arguments = ParsedCallArguments.parseForAnnotation(parser);
				parser.required(ZSTokenType.T_SQCLOSE, "] expected");
				results.add(new ParsedAnnotation(position, type, arguments));
			} catch (ParseException ex) {
				parser.recoverUntilTokenOrNewline(ZSTokenType.T_SQCLOSE);
			}
		}
		return results.toArray(NONE);
	}

	public static MemberAnnotation[] compileForMember(ParsedAnnotation[] annotations, IDefinitionMember member, MemberCompiler compiler) {
		if (annotations.length == 0)
			return MemberAnnotation.NONE;

		MemberAnnotation[] compiled = new MemberAnnotation[annotations.length];
		for (int i = 0; i < annotations.length; i++) {
			compiled[i] = annotations[i].compileForMember(member, compiler);
		}
		return compiled;
	}

	public static DefinitionAnnotation[] compileForDefinition(ParsedAnnotation[] annotations, HighLevelDefinition definition, DefinitionCompiler compiler) {
		if (annotations.length == 0)
			return DefinitionAnnotation.NONE;

		DefinitionAnnotation[] compiled = new DefinitionAnnotation[annotations.length];
		for (int i = 0; i < annotations.length; i++) {
			compiled[i] = annotations[i].compileForDefinition(definition, compiler);
		}
		return compiled;
	}

	public static StatementAnnotation[] compileForStatement(ParsedAnnotation[] annotations, Statement statement, StatementCompiler compiler) {
		if (annotations.length == 0)
			return StatementAnnotation.NONE;

		StatementAnnotation[] compiled = new StatementAnnotation[annotations.length];
		for (int i = 0; i < annotations.length; i++)
			compiled[i] = annotations[i].compileForStatement(statement, compiler);
		return compiled;
	}

	public static ParameterAnnotation[] compileForParameter(ParsedAnnotation[] annotations, FunctionHeader header, FunctionParameter parameter, ExpressionCompiler compiler) {
		if (annotations.length == 0)
			return ParameterAnnotation.NONE;

		ParameterAnnotation[] compiled = new ParameterAnnotation[annotations.length];
		for (int i = 0; i < annotations.length; i++)
			compiled[i] = annotations[i].compileForParameter(header, parameter, compiler);
		return compiled;
	}

	public MemberAnnotation compileForMember(IDefinitionMember member, MemberCompiler compiler) {
		return type.compileAnnotation(compiler.types()).map(annotationType -> {
			ExpressionCompiler evalScope = annotationType.getScopeForMember(member, compiler);
			TypeID[] types = type.compileTypeArguments(compiler.types());
			CompilingExpression[] arguments = this.arguments.compile(evalScope);

			MatchedCallArguments<StaticCallableMethod> matched
					= annotationType.getInitializers().match(evalScope, position, types, arguments);
			return matched.getArguments()
					.map(args -> annotationType.createForMember(position, args))
					.orElseGet(() -> new InvalidMemberAnnotation(position, matched.getError().orElse(null)));
		}).orElseGet(() -> new InvalidMemberAnnotation(position, CompileErrors.annotationNotFound(type.toString())));
	}

	public DefinitionAnnotation compileForDefinition(HighLevelDefinition definition, DefinitionCompiler compiler) {
		return type.compileAnnotation(compiler.types()).map(annotationType -> {
			ExpressionCompiler evalScope = annotationType.getScopeForType(definition, compiler);
			TypeID[] types = type.compileTypeArguments(compiler.types());
			CompilingExpression[] arguments = this.arguments.compile(evalScope);

			MatchedCallArguments<StaticCallableMethod> matched
					= annotationType.getInitializers().match(evalScope, position, types, arguments);
			return matched.getArguments()
					.map(args -> annotationType.createForDefinition(position, args))
					.orElseGet(() -> new InvalidDefinitionAnnotation(position, matched.getError().orElse(null)));
		}).orElseGet(() -> new InvalidDefinitionAnnotation(position, CompileErrors.annotationNotFound(type.toString())));
	}

	public StatementAnnotation compileForStatement(Statement statement, StatementCompiler compiler) {
		return type.compileAnnotation(compiler.types()).map(annotationType -> {
			ExpressionCompiler evalScope = annotationType.getScopeForStatement(statement, compiler);
			TypeID[] types = type.compileTypeArguments(compiler.types());
			CompilingExpression[] arguments = this.arguments.compile(evalScope);

			MatchedCallArguments<StaticCallableMethod> matched
					= annotationType.getInitializers().match(evalScope, position, types, arguments);
			return matched.getArguments()
					.map(args -> annotationType.createForStatement(position, args))
					.orElseGet(() -> new InvalidStatementAnnotation(position, matched.getError().orElse(null)));
		}).orElseGet(() -> new InvalidStatementAnnotation(position, CompileErrors.annotationNotFound(type.toString())));
	}

	public ParameterAnnotation compileForParameter(FunctionHeader header, FunctionParameter parameter, ExpressionCompiler compiler) {
		return type.compileAnnotation(compiler.types()).map(annotationType -> {
			ExpressionCompiler evalScope = annotationType.getScopeForParameter(header, parameter, compiler);
			TypeID[] types = type.compileTypeArguments(compiler.types());
			CompilingExpression[] arguments = this.arguments.compile(evalScope);

			MatchedCallArguments<StaticCallableMethod> matched
					= annotationType.getInitializers().match(evalScope, position, types, arguments);
			return matched.getArguments()
					.map(args -> annotationType.createForParameter(position, args))
					.orElseGet(() -> new InvalidParameterAnnotation(position, matched.getError().orElse(null)));
		}).orElseGet(() -> new InvalidParameterAnnotation(position, CompileErrors.annotationNotFound(type.toString())));
	}
}
