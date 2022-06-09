package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zencode.shared.CompileError;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class InferredType {
	public static InferredType success(TypeID type, TypeID thrownType) {
		return new InferredType(type, thrownType);
	}

	@Deprecated
	public static InferredType failure(CompileExceptionCode errorCode, String errorMessage) {
		return new InferredType(new CompileError(errorCode, errorMessage));
	}

	public static InferredType failure(CompileError error) {
		return new InferredType(error);
	}

	public static Optional<InferredType> fromCandidates(List<InferredType> candidates) {
		if (candidates.isEmpty()) {
			return Optional.empty();
		} else if (candidates.size() == 1) {
			return Optional.of(candidates.get(0));
		} else {
			return Optional.of(ambiguous(candidates.stream().map(InferredType::get).collect(Collectors.toList())));
		}
	}

	public static InferredType ambiguous(List<TypeID> candidates) {
		return new InferredType(CompileErrors.ambiguousType(candidates));
	}

	public static InferredType union(ExpressionCompiler compiler, InferredType a, InferredType b) {
		if (a.isFailed())
			return a;
		if (b.isFailed())
			return b;

		Optional<TypeID> value = compiler.union(a.type, b.type);
		if (!value.isPresent())
			return failure(CompileErrors.noIntersectionBetweenTypes(a.type, b.type));

		TypeID thrown;
		if (a.thrown == null) {
			thrown = b.thrown;
		} else if (b.thrown == null) {
			thrown = a.thrown;
		} else {
			Optional<TypeID> thrownUnion = compiler.union(a.thrown, b.thrown);
			if (!thrownUnion.isPresent())
				return failure(CompileErrors.noIntersectionBetweenTypes(a.thrown, b.thrown));

			thrown = thrownUnion.get();
		}

		return new InferredType(value.get(), thrown);
	}

	private final TypeID type;
	private final TypeID thrown;
	private final CompileError error;

	private InferredType(TypeID type, TypeID thrown) {
		this.type = type;
		this.thrown = thrown;
		this.error = null;
	}

	private InferredType(CompileError error) {
		this.type = null;
		this.thrown = null;
		this.error = error;
	}

	public boolean isFailed() {
		return type == null;
	}

	public TypeID get() {
		if (type == null)
			throw new NullPointerException();

		return type;
	}

	public TypeID getThrown() {
		return thrown;
	}

	public CompileError getError() {
		if (!isFailed())
			throw new NullPointerException();

		return error;
	}
}
