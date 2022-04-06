package org.openzen.zenscript.compiler;

import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class InferredType {
	public static InferredType success(TypeID type) {
		return new InferredType(type);
	}

	public static InferredType failure(CompileExceptionCode errorCode, String errorMessage) {
		return new InferredType(errorCode, errorMessage);
	}

	public static Optional<InferredType> fromCandidates(List<TypeID> candidates) {
		if (candidates.isEmpty()) {
			return Optional.empty();
		} else if (candidates.size() == 1) {
			return Optional.of(success(candidates.get(0)));
		} else {
			return Optional.of(ambiguous(candidates));
		}
	}

	public static InferredType ambiguous(List<TypeID> candidates) {
		String possibleTypes = candidates.stream().map(Object::toString).collect(Collectors.joining(", "));
		return InferredType.failure(CompileExceptionCode.INFERENCE_AMBIGUOUS, "Type inference ambiguity, possible types: " + possibleTypes);
	}

	private final TypeID type;
	private final CompileExceptionCode errorCode;
	private final String errorMessage;

	private InferredType(TypeID type) {
		this.type = type;
		this.errorCode = null;
		this.errorMessage = null;
	}

	private InferredType(CompileExceptionCode errorCode, String errorMessage) {
		this.type = null;
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	public boolean isFailed() {
		return type == null;
	}

	public TypeID get() {
		if (type == null)
			throw new NullPointerException();

		return type;
	}

	public CompileExceptionCode getErrorCode() {
		if (!isFailed())
			throw new NullPointerException();

		return errorCode;
	}

	public String getErrorMessage() {
		if (!isFailed())
			throw new NullPointerException();

		return errorMessage;
	}
}
