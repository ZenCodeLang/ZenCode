package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.expression.ConstantStringExpression;
import org.openzen.zenscript.codemodel.expression.EnumConstantExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.VariantValueExpression;
import org.openzen.zenscript.codemodel.expression.switchvalue.EnumConstantSwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.VariantOptionSwitchValue;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.codemodel.member.ref.VariantOptionRef;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.definitions.ParsedFunctionHeader;
import org.openzen.zenscript.parser.definitions.ParsedFunctionParameter;
import org.openzen.zenscript.parser.type.IParsedType;
import org.openzen.zenscript.parser.type.ParsedTypeBasic;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ParsedExpressionVariable extends ParsedExpression {
	public final String name;
	private final List<IParsedType> typeArguments;

	public ParsedExpressionVariable(CodePosition position, String name, List<IParsedType> typeArguments) {
		super(position);

		this.name = name;
		this.typeArguments = typeArguments;
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) throws CompileException {
		TypeID[] typeArguments = IParsedType.compileTypes(this.typeArguments, scope);
		IPartialExpression result = scope.get(position, new GenericName(name, typeArguments));
		if (result == null) {
			for (TypeID hint : scope.hints) {
				TypeMembers members = scope.getTypeMembers(hint);
				EnumConstantMember member = members.getEnumMember(name);
				if (member != null)
					return new EnumConstantExpression(position, hint, member);

				VariantOptionRef option = members.getVariantOption(name);
				if (option != null)
					return new VariantValueExpression(position, hint, option);
			}

			StringBuilder builder = new StringBuilder("No such symbol: " + name);

			Set<String> possibleImports = scope.getTypeRegistry().getDefinitions().stream().filter(definitionTypeID -> name.equals(definitionTypeID.definition.name)).map(definitionTypeID -> definitionTypeID.definition.getFullName()).collect(Collectors.toSet());
			if(!possibleImports.isEmpty()){
				builder.append("\nPossible imports:");
				possibleImports.forEach(name -> {
					builder.append("\n").append(name);
				});
			}

			throw new CompileException(position, CompileExceptionCode.UNDEFINED_VARIABLE, builder.toString());
		} else {
			return result;
		}
	}

	@Override
	public Expression compileKey(ExpressionScope scope) {
		return new ConstantStringExpression(position, name);
	}

	@Override
	public SwitchValue compileToSwitchValue(TypeID type, ExpressionScope scope) throws CompileException {
		TypeMembers members = scope.getTypeMembers(type);
		if (type.isEnum()) {
			EnumConstantMember member = members.getEnumMember(name);
			if (member == null)
				throw new CompileException(position, CompileExceptionCode.NO_SUCH_MEMBER, "Enum member does not exist: " + name);

			return new EnumConstantSwitchValue(member);
		} else if (type.isVariant()) {
			VariantOptionRef option = members.getVariantOption(name);
			if (option == null)
				throw new CompileException(position, CompileExceptionCode.NO_SUCH_MEMBER, "Variant option does not exist: " + name);
			if (option.types.length > 0)
				throw new CompileException(position, CompileExceptionCode.MISSING_VARIANT_CASEPARAMETERS, "Variant case is missing parameters");

			return new VariantOptionSwitchValue(option, new String[0]);
		} else {
			throw new CompileException(position, CompileExceptionCode.INVALID_SWITCH_CASE, "Invalid switch case");
		}
	}

	@Override
	public ParsedFunctionHeader toLambdaHeader() {
		return new ParsedFunctionHeader(position, Collections.singletonList(toLambdaParameter()), ParsedTypeBasic.UNDETERMINED);
	}

	@Override
	public ParsedFunctionParameter toLambdaParameter() {
		return new ParsedFunctionParameter(ParsedAnnotation.NONE, name, ParsedTypeBasic.UNDETERMINED, null, false);
	}

	@Override
	public boolean hasStrongType() {
		return true;
	}
}
