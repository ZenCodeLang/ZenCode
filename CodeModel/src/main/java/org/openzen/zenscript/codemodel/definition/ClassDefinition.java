package org.openzen.zenscript.codemodel.definition;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.*;
import org.openzen.zenscript.codemodel.compilation.AnyMethod;
import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.identifiers.ModuleSymbol;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.identifiers.instances.FieldInstance;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.member.ConstructorMember;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.statement.BlockStatement;
import org.openzen.zenscript.codemodel.statement.ExpressionStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClassDefinition extends HighLevelDefinition {
	public ClassDefinition(CodePosition position, ModuleSymbol module, ZSPackage pkg, String name, Modifiers modifiers, TypeSymbol outerDefinition) {
		super(position, module, pkg, name, modifiers, outerDefinition);
	}

	@Override
	public <T> T accept(DefinitionVisitor<T> visitor) {
		return visitor.visitClass(this);
	}

	@Override
	public <C, R> R accept(C context, DefinitionVisitorWithContext<C, R> visitor) {
		return visitor.visitClass(context, this);
	}

	@Override
	public void addDefaultMembers() {
		super.addDefaultMembers();

		boolean hasNoConstructor = members.stream().noneMatch(IDefinitionMember::isConstructor);

		Optional<MethodInstance> superConstructor = Optional.ofNullable(getSuperType())
				.flatMap(t -> t.resolve().getConstructor().getSingleOverload())
				.flatMap(AnyMethod::asMethod);

		if (hasNoConstructor) {
			TypeID thisType = DefinitionTypeID.createThis(this);

			List<FieldMember> fields = getFields().stream().filter(field -> !field.isStatic()).collect(Collectors.toList());
			boolean hasSuperParameters = superConstructor.map(c -> c.getHeader().parameters.length > 0).orElse(false);
			boolean noUninitializedFields = !hasSuperParameters;
			List<Statement> defaultInitializerStatements = new ArrayList<>();
			List<Statement> initializerStatements = new ArrayList<>();
			List<FunctionParameter> parameters = new ArrayList<>();

			superConstructor.ifPresent(constructor -> {
                parameters.addAll(Arrays.asList(constructor.getHeader().parameters));

				Expression[] superArgumentExpressions = Stream.of(constructor.getHeader().parameters)
						.map(parameter -> new GetFunctionParameterExpression(position, parameter))
						.toArray(Expression[]::new);
				CallArguments superArguments = new CallArguments(superArgumentExpressions);
				ExpressionStatement superCall = new ExpressionStatement(position, new ConstructorSuperCallExpression(position, thisType, constructor, superArguments));
				defaultInitializerStatements.add(superCall);
				initializerStatements.add(superCall);
			});

			if (hasSuperParameters || !fields.isEmpty()) {
				for (int i = 0; i < fields.size(); i++) {
					FieldMember field = fields.get(i);
					noUninitializedFields &= field.initializer != null;
					FunctionParameter parameter = new FunctionParameter(field.getType(), field.name, field.initializer, false);
					parameters.add(parameter);

					initializerStatements.add(new ExpressionStatement(
							position,
							new SetFieldExpression(
									position,
									new ThisExpression(position, thisType),
									new FieldInstance(field, field.getType()),
									new GetFunctionParameterExpression(position, parameter))));
					if (field.initializer != null) {
						defaultInitializerStatements.add(new ExpressionStatement(
								position,
								new SetFieldExpression(
										position,
										new ThisExpression(position, thisType),
										new FieldInstance(field, field.getType()),
										field.initializer)));
					}
				}

				ConstructorMember constructor = new ConstructorMember(position, this, Modifiers.PUBLIC, new FunctionHeader(thisType, parameters.toArray(FunctionParameter.NONE)));
				BlockStatement block = new BlockStatement(position, initializerStatements.toArray(new Statement[0]));
				constructor.setBody(block);
				members.add(constructor);
			}

			if (noUninitializedFields) {
				ConstructorMember constructor = new ConstructorMember(position, this, Modifiers.PUBLIC, new FunctionHeader(thisType));
				BlockStatement block = new BlockStatement(position, defaultInitializerStatements.toArray(new Statement[0]));
				constructor.setBody(block);
				members.add(constructor);
			}
		}
	}
}
