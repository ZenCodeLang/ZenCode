package org.openzen.zenscript.javabytecode.compiler.indy;

import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Type;

import java.util.Objects;
import java.util.function.UnaryOperator;

@SuppressWarnings("SpellCheckingInspection")
public final class JavaCondy {
	public static final class Builder {
		private final Constructor constructor;

		private String fieldName;
		private Type fieldDesc;
		private BsmData bsm;

		private Builder(final Constructor constructor) {
			this.constructor = constructor;
			this.fieldName = null;
			this.fieldDesc = null;
			this.bsm = null;
		}

		static Builder of(final Constructor constructor) {
			return new Builder(constructor);
		}

		public Builder field(final String name, final Type desc) {
			Objects.requireNonNull(name, "Field name cannot be null");
			Objects.requireNonNull(desc, "Field descriptor cannot be null");
			if (desc.getSort() == Type.METHOD) {
				throw new IllegalArgumentException("Field type must be a valid field descriptor");
			}
			if (this.fieldName != null) {
				throw new IllegalStateException("Field has already been set");
			}
			this.fieldName = name;
			this.fieldDesc = desc;
			return this;
		}

		public Builder bsm(final UnaryOperator<BsmData.Builder> bsmDataBuilder) {
			if (this.bsm != null) {
				throw new IllegalStateException("BSM has already been specified");
			}
			this.bsm = bsmDataBuilder.apply(BsmData.builder(IndyTarget.CONDY)).build();
			return this;
		}

		JavaCondy build() {
			if (this.fieldName == null) {
				throw new IllegalStateException("Field has not been specified");
			}
			if (this.bsm == null) {
				throw new IllegalStateException("BSM data has not been specified");
			}
			return this.constructor.of(this.fieldName, this.fieldDesc, this.bsm);
		}
	}

	@FunctionalInterface
	public interface CondyVisitor {
		void visitLdc(final ConstantDynamic dynamic);
	}

	@FunctionalInterface
	interface Constructor {
		JavaCondy of(final String fieldName, final Type fieldDesc, final BsmData bsm);
	}

	private final String fieldName;
	private final Type fieldDesc;
	private final BsmData bsm;

	private JavaCondy(final String fieldName, final Type fieldDesc, final BsmData bsm) {
		this.fieldName = fieldName;
		this.fieldDesc = fieldDesc;
		this.bsm = bsm;
	}

	public static JavaCondy build(final UnaryOperator<JavaCondy.Builder> builder) {
		return builder.apply(builder()).build();
	}

	static JavaCondy.Builder builder() {
		return Builder.of(JavaCondy::new);
	}

	public void visit(final CondyVisitor visitor) {
		visitor.visitLdc(this.asConstantDynamic());
	}

	ConstantDynamic asConstantDynamic() {
		return new ConstantDynamic(
				this.fieldName,
				this.fieldDesc.getDescriptor(),
				IndyHelpers.toBsmHandle(this.bsm.bsm()),
				IndyHelpers.toBsmArgs(this.bsm.args())
		);
	}

	@Override
	public String toString() {
		return String.format("%s:%s via %s", this.fieldName, this.fieldDesc.getDescriptor(), this.bsm);
	}
}
