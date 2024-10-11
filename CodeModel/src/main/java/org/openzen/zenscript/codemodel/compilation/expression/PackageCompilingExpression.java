package org.openzen.zenscript.codemodel.compilation.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.ssa.CodeBlockStatement;
import org.openzen.zenscript.codemodel.ssa.SSAVariableCollector;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;

public class PackageCompilingExpression implements CompilingExpression {
	private final ExpressionCompiler compiler;
	private final ZSPackage pkg;
	private final CodePosition position;

	public PackageCompilingExpression(ExpressionCompiler compiler, CodePosition position, ZSPackage pkg) {
		this.compiler = compiler;
		this.position = position;
		this.pkg = pkg;
	}

	@Override
	public Expression eval() {
		return compiler.at(position).invalid(CompileErrors.cannotUsePackageAsValue());
	}

	@Override
	public CastedExpression cast(CastedEval cast) {
		return CastedExpression.invalid(position, CompileErrors.cannotUsePackageAsValue());
	}

	@Override
	public boolean canConstructAs(TypeID type) {
		return false;
	}

	@Override
	public Optional<CompilingCallable> call() {
		return Optional.empty();
	}

	@Override
	public CompilingExpression getMember(CodePosition position, GenericName name) {
		Optional<TypeID> asType = pkg.getType(name);
		if (asType.isPresent()) {
			return new TypeCompilingExpression(compiler, position, asType.get());
		}

		Optional<ZSPackage> asPackage = pkg.getOptional(name.name);
		if (asPackage.isPresent()) {
			return new PackageCompilingExpression(compiler, position, asPackage.get());
		}

		return new InvalidCompilingExpression(compiler, position, CompileErrors.noMemberInPackage(pkg.fullName, name.name));
	}

	@Override
	public CompilingExpression assign(CompilingExpression value) {
		return new InvalidCompilingExpression(compiler, position, CompileErrors.cannotUsePackageAsValue());
	}

	@Override
	public Expression as(TypeID type) {
		return compiler.at(position).invalid(CompileErrors.cannotUsePackageAsValue(), type);
	}

	@Override
	public void collect(SSAVariableCollector collector) {

	}

	@Override
	public void linkVariables(CodeBlockStatement.VariableLinker linker) {

	}
}
