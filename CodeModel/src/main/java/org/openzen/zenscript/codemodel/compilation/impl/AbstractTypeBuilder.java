package org.openzen.zenscript.codemodel.compilation.impl;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.compilation.ExpressionCompiler;
import org.openzen.zenscript.codemodel.compilation.TypeBuilder;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.type.*;
import org.openzen.zenscript.codemodel.type.builtin.ResultTypeSymbol;

import java.util.List;
import java.util.Optional;

public abstract class AbstractTypeBuilder implements TypeBuilder {
	public AbstractTypeBuilder() {

	}

	@Override
	public TypeID definitionOf(TypeSymbol type, TypeID[] arguments) {
		return DefinitionTypeID.create(type, arguments);
	}

	@Override
	public OptionalTypeID optionalOf(TypeID type) {
		return new OptionalTypeID(type);
	}

	@Override
	public ArrayTypeID arrayOf(TypeID elementType) {
		return new ArrayTypeID(elementType);
	}

	@Override
	public ArrayTypeID arrayOf(TypeID elementType, int dimension) {
		return new ArrayTypeID(elementType, dimension);
	}

	@Override
	public AssocTypeID associativeOf(TypeID keyType, TypeID valueType) {
		return new AssocTypeID(keyType, valueType);
	}

	@Override
	public RangeTypeID rangeOf(TypeID type) {
		return new RangeTypeID(type);
	}

	@Override
	public FunctionTypeID functionOf(FunctionHeader header) {
		return new FunctionTypeID(header);
	}

	@Override
	public TypeID resultOf(TypeID type, TypeID thrownType) {
		return DefinitionTypeID.create(ResultTypeSymbol.INSTANCE, type, thrownType);
	}

	@Override
	public GenericMapTypeBuilder withGeneric(TypeParameter... parameters) {
		return new Generic() {
			@Override
			public Optional<TypeID> resolve(CodePosition position, List<GenericName> name) {
				if (name.size() == 1 && name.get(0).hasNoArguments()) {
					for (int i = 0; i < parameters.length; i++) {
						if (parameters[i].name.equals(name.get(0).name))
							return Optional.of(new GenericTypeID(parameters[i]));
					}
				}

				return AbstractTypeBuilder.this.resolve(position, name);
			}

			@Override
			public Optional<AnnotationDefinition> resolveAnnotation(List<GenericName> name) {
				return AbstractTypeBuilder.this.resolveAnnotation(name);
			}

			@Override
			public ExpressionCompiler getDefaultValueCompiler() {
				return AbstractTypeBuilder.this.getDefaultValueCompiler();
			}

			@Override
			public TypeID ofValue(TypeID valueType) {
				// TODO - multiple keys?
				return new GenericMapTypeID(valueType, parameters[0]);
			}
		};
	}

	private static abstract class Generic extends AbstractTypeBuilder implements GenericMapTypeBuilder {

	}
}
