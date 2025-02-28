package org.openzen.zenscript.javabytecode.compiler.indy;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;

import java.util.Objects;
import java.util.function.UnaryOperator;

public final class JavaIndy {
	public static final class Builder {
		private final Constructor constructor;

		private String callSiteName;
		private Type callSiteDesc;
		private BsmData bsm;

		private Builder(final Constructor constructor) {
			this.constructor = constructor;
			this.callSiteName = null;
			this.callSiteDesc = null;
			this.bsm = null;
		}

		static Builder of(final Constructor constructor) {
			return new Builder(constructor);
		}

		public Builder callSite(final String name, final Type desc) {
			Objects.requireNonNull(name, "Call site name cannot be null");
			Objects.requireNonNull(desc, "Call site descriptor cannot be null");
			if (desc.getSort() != Type.METHOD) {
				throw new IllegalArgumentException("Call site type must be a valid method descriptor");
			}
			if (this.callSiteName != null) {
				throw new IllegalStateException("Call site has already been set");
			}
			this.callSiteName = name;
			this.callSiteDesc = desc;
			return this;
		}

		public Builder bsm(final UnaryOperator<BsmData.Builder> bsmDataBuilder) {
			if (this.bsm != null) {
				throw new IllegalStateException("BSM has already been specified");
			}
			this.bsm = bsmDataBuilder.apply(BsmData.builder(IndyTarget.INDY)).build();
			return this;
		}

		JavaIndy build() {
			if (this.callSiteName == null) {
				throw new IllegalStateException("Call site has not been specified");
			}
			if (this.bsm == null) {
				throw new IllegalStateException("BSM data has not been specified");
			}
			return this.constructor.of(this.callSiteName, this.callSiteDesc, this.bsm);
		}
	}

	@FunctionalInterface
	public interface IndyVisitor {
		void visitInvokeDynamic(final String methodName, final String methodDesc, final Handle bsmMethod, final Object... bsmArgs);
	}

	@FunctionalInterface
	interface Constructor {
		JavaIndy of(final String callSiteName, final Type callSiteDesc, final BsmData bsm);
	}

	private final String callSiteName;
	private final Type callSiteDesc;
	private final BsmData bsm;

	private JavaIndy(final String callSiteName, final Type callSiteDesc, final BsmData bsm) {
		this.callSiteName = callSiteName;
		this.callSiteDesc = callSiteDesc;
		this.bsm = bsm;
	}

	public static JavaIndy build(final UnaryOperator<JavaIndy.Builder> builder) {
		return builder.apply(builder()).build();
	}

	static JavaIndy.Builder builder() {
		return Builder.of(JavaIndy::new);
	}

	public void visit(final IndyVisitor visitor) {
		visitor.visitInvokeDynamic(
				this.callSiteName,
				this.callSiteDesc.getDescriptor(),
				IndyHelpers.toBsmHandle(this.bsm.bsm()),
				IndyHelpers.toBsmArgs(this.bsm.args())
		);
	}

	@Override
	public String toString() {
		return String.format("%s%s via %s", this.callSiteName, this.callSiteDesc.getDescriptor(), this.bsm);
	}
}
