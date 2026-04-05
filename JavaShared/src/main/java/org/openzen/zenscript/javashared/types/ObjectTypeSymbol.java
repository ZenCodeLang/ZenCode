package org.openzen.zenscript.javashared.types;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.compilation.ResolvingType;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.identifiers.ExpansionSymbol;
import org.openzen.zenscript.codemodel.identifiers.ModuleSymbol;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.builtin.BuiltinMethodSymbol;
import org.openzen.zenscript.codemodel.type.member.MemberSet;
import org.openzen.zenscript.javashared.expressions.JavaObjectCastExpression;

import java.util.List;
import java.util.Optional;

public class ObjectTypeSymbol implements TypeSymbol {
	public static ObjectTypeSymbol INSTANCE = new ObjectTypeSymbol();

	private ObjectTypeSymbol() {
	}

	@Override
	public Modifiers getModifiers() {
		return Modifiers.PUBLIC;
	}

	@Override
	public boolean isStatic() {
		return false;
	}

	@Override
	public boolean isEnum() {
		return false;
	}

	@Override
	public String getName() {
		return "Object";
	}

	@Override
	public ResolvingType resolve(TypeID[] typeArguments) {
		DefinitionTypeID type = new DefinitionTypeID(this, typeArguments, null);
		MemberSet.Builder members = MemberSet.create(type);
		members.method(new MethodInstance(BuiltinMethodSymbol.OBJECT_SAME, new FunctionHeader(BasicTypeID.BOOL, type), type));
		members.method(new MethodInstance(BuiltinMethodSymbol.OBJECT_NOTSAME, new FunctionHeader(BasicTypeID.BOOL, type), type));
		members.method(new MethodInstance(BuiltinMethodSymbol.OBJECT_HASHCODE, new FunctionHeader(BasicTypeID.UINT), type));

		return members.build();
	}

	@Override
	public Optional<TypeSymbol> getOuter() {
		return Optional.empty();
	}

	@Override
	public Optional<TypeID> getSupertype(TypeID[] typeArguments) {
		return Optional.empty();
	}

	@Override
	public ModuleSymbol getModule() {
		return ModuleSymbol.BUILTIN;
	}

	@Override
	public String describe() {
		return "Object";
	}

	@Override
	public boolean isInterface() {
		return false;
	}

	@Override
	public boolean isExpansion() {
		return false;
	}

	@Override
	public TypeParameter[] getTypeParameters() {
		return TypeParameter.NONE;
	}

	@Override
	public Optional<Expression> castImplicitFrom(CodePosition position, TypeID[] typeArguments, Expression value, List<ExpansionSymbol> expansions) {
		TypeID type = DefinitionTypeID.create(this, TypeID.NONE);
		if (value.type.equals(type)) {
			return Optional.of(value);
		} else {
			return Optional.of(new JavaObjectCastExpression(position, type, value));
		}
	}

	@Override
	public boolean isObjectRoot() {
		return true;
	}
}
