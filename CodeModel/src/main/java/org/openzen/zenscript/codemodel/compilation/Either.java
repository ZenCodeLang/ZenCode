package org.openzen.zenscript.codemodel.compilation;

public class Either<L, R> {
	private final L left;
	private final R right;

	private Either(L left, R right) {
		this.left = left;
		this.right = right;
	}

	public static <L, R> Either<L, R> left(L left) {
		return new Either<>(left, null);
	}

	public static <L, R> Either<L, R> right(R right) {
		return new Either<>(null, right);
	}

	public boolean isLeft() {
		return left != null;
	}

	public boolean isRight() {
		return right != null;
	}

	public L getLeft() {
		if(!isLeft()) {
			throw new IllegalStateException("Must be left");
		}
		return left;
	}

	public R getRight() {
		if(!isRight()) {
			throw new IllegalStateException("Must be right");
		}
		return right;
	}
}
