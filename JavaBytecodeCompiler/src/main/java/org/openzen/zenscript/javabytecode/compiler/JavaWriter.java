package org.openzen.zenscript.javabytecode.compiler;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.LocalVariablesSorter;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.logging.IZSLogger;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.statement.VariableID;
import org.openzen.zenscript.javabytecode.JavaLocalVariableInfo;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.compiling.JavaCompilingMethod;
import org.openzen.zenscript.javashared.JavaNativeField;
import org.openzen.zenscript.javashared.JavaNativeMethod;
import org.openzen.zenscript.javashared.JavaParameterInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

public class JavaWriter {
	private static final JavaNativeMethod STRING_CONCAT = JavaNativeMethod.getNativeStatic(
			JavaClass.STRING,
			"concat",
			"(Ljava/lang/String;)Ljava/lang/String;");

	public final JavaCompilingMethod method;
	public final HighLevelDefinition forDefinition;
	public final ClassVisitor clazzVisitor;
	private final IZSLogger logger;

	private final LocalVariablesSorter visitor;
	private final List<JavaLocalVariableInfo> localVariableInfos = new ArrayList<>();
	private final Map<VariableID, JavaLocalVariableInfo> localVariables = new HashMap<>();
	private final List<Integer> lineNumberLabels = new ArrayList<>();
	private boolean debug = false;
	private final boolean nameVariables = true;
	private int labelIndex = 1;
	private Map<Label, String> labelNames = new HashMap<>();

	public JavaWriter(
			IZSLogger logger,
			CodePosition position,
			ClassVisitor visitor,
			JavaCompilingMethod method,
			HighLevelDefinition forDefinition,
			boolean isExtension,
			String[] exceptions,
			String... annotations) {
		this.logger = logger;
		this.clazzVisitor = visitor;
		this.method = method;
		this.forDefinition = forDefinition;

		final String descriptor = method.compiled.descriptor;
		final String signature = method.signature;
		final int access = isExtension ? method.compiled.modifiers | ACC_STATIC : method.compiled.modifiers;
		final MethodVisitor methodVisitor = visitor.visitMethod(access, method.compiled.name, descriptor, signature, exceptions);

		for (String annotation : annotations) {
			methodVisitor.visitAnnotation(annotation, true).visitEnd();
		}

		this.visitor = new LocalVariablesSorter(access, descriptor, methodVisitor);
		this.position(position.fromLine);
	}


	public JavaWriter(
			IZSLogger logger,
			CodePosition position,
			ClassVisitor visitor,
			JavaCompilingMethod method,
			HighLevelDefinition forDefinition,
			boolean isExtension,
			String[] exceptions) {
		this(logger, position, visitor, method, forDefinition, isExtension, exceptions, new String[0]);
	}

	public JavaWriter(
			IZSLogger logger,
			CodePosition position,
			ClassVisitor visitor,
			JavaCompilingMethod method,
			HighLevelDefinition forDefinition,
			boolean isExtension) {
		this(logger, position, visitor, method, forDefinition, isExtension, new String[0]);
	}

	public JavaWriter(
			IZSLogger logger,
			CodePosition position,
			ClassVisitor visitor,
			JavaCompilingMethod method,
			HighLevelDefinition forDefinition) {
		this(logger, position, visitor, method, forDefinition, false);
	}


	public void setLocalVariable(VariableID variable, JavaLocalVariableInfo info) {
		localVariables.put(variable, info);
	}

	public JavaLocalVariableInfo tryGetLocalVariable(VariableID variable) {
		return localVariables.get(variable);
	}

	public JavaLocalVariableInfo getLocalVariable(VariableID variable) {
		JavaLocalVariableInfo result = tryGetLocalVariable(variable);
		if (result == null)
			throw new IllegalStateException("Local variable unknown");

		return result;
	}

	public void enableDebug() {
		debug = true;
	}

	public LocalVariablesSorter getVisitor() {
		return visitor;
	}

	public void start() {
		if (debug)
			logger.debug("--start--");

		visitor.visitCode();
	}

	public void end() {
		if (debug)
			logger.debug("--end--");

		try {
			visitor.visitMaxs(0, 0);
		} catch (ArrayIndexOutOfBoundsException | NegativeArraySizeException ex) {
			if (debug && (clazzVisitor instanceof ClassWriter)) {
				// TODO Write to a file for debugging?
				System.out.println(Arrays.toString(((ClassWriter) clazzVisitor).toByteArray()));
			}
			throw ex;
		}

		if (nameVariables) {
			for (JavaLocalVariableInfo info : localVariableInfos) {
				nameVariable(info.local, info.name, info.start, info.end, info.type);
			}
		}
		visitor.visitEnd();
	}

	public void label(Label label) {
		if (debug)
			logger.debug("Label " + getLabelName(label));

		visitor.visitLabel(label);
	}

	public int local(Type type) {
		return visitor.newLocal(type);
	}

	public int local(Class<?> cls) {
		return visitor.newLocal(Type.getType(cls));
	}

	public void iConstM1() {
		if (debug)
			logger.debug("iconstm1");

		visitor.visitInsn(ICONST_M1);
	}

	public void iConst0() {
		if (debug)
			logger.debug("iconst0");

		visitor.visitInsn(ICONST_0);
	}

	public void iConst1() {
		if (debug)
			logger.debug("iconst1");

		visitor.visitInsn(ICONST_1);
	}

	public void iConst2() {
		if (debug)
			logger.debug("iconst2");

		visitor.visitInsn(ICONST_2);
	}

	public void iConst3() {
		if (debug)
			logger.debug("iconst3");

		visitor.visitInsn(ICONST_3);
	}

	public void iConst4() {
		if (debug)
			logger.debug("iconst4");

		visitor.visitInsn(ICONST_4);
	}

	public void iConst5() {
		if (debug)
			logger.debug("iconst5");

		visitor.visitInsn(ICONST_5);
	}

	public void lConst0() {
		if (debug)
			logger.debug("lconst0");

		visitor.visitInsn(LCONST_0);
	}

	public void lConst1() {
		if (debug)
			logger.debug("lconst1");

		visitor.visitInsn(LCONST_1);
	}

	public void fConst0() {
		if (debug)
			logger.debug("fconst0");

		visitor.visitInsn(FCONST_0);
	}

	public void fConst1() {
		if (debug)
			logger.debug("fconst1");

		visitor.visitInsn(FCONST_1);
	}

	public void dConst0() {
		if (debug)
			logger.debug("dconst0");

		visitor.visitInsn(DCONST_0);
	}

	public void dConst1() {
		if (debug)
			logger.debug("dconst1");

		visitor.visitInsn(DCONST_1);
	}

	public void biPush(byte value) {
		if (debug)
			logger.debug("bipush");

		visitor.visitIntInsn(BIPUSH, value);
	}

	public void siPush(short value) {
		if (debug)
			logger.debug("sipush");

		visitor.visitIntInsn(SIPUSH, value);
	}

	public void aConstNull() {
		if (debug)
			logger.debug("aConstNull");

		visitor.visitInsn(ACONST_NULL);
	}

	public void ldc(Object value) {
		if (value == null)
			throw new NullPointerException("Value cannot be null");

		if (debug)
			logger.debug("ldc " + value);

		visitor.visitLdcInsn(value);
	}

	public void constant(byte value) {
		switch (value) {
			case -1:
				this.iConstM1();
				break;
			case 0:
				this.iConst0();
				break;
			case 1:
				this.iConst1();
				break;
			case 2:
				this.iConst2();
				break;
			case 3:
				this.iConst3();
				break;
			case 4:
				this.iConst4();
				break;
			case 5:
				this.iConst5();
				break;
			default:
				this.biPush(value);
		}
	}

	public void constant(short value) {
		if (Byte.MIN_VALUE <= value && value <= Byte.MAX_VALUE) {
			this.constant((byte) value);
		} else {
			this.siPush(value);
		}
	}

	public void constant(int value) {
		if (Short.MIN_VALUE <= value && value <= Short.MAX_VALUE) {
			this.constant((short) value);
		} else {
			this.ldc(value);
		}
	}

	public void constant(long value) {
		if (value == 0L) {
			this.lConst0();
		} else if (value == 1L) {
			this.lConst1();
		} else {
			this.ldc(value);
		}
	}

	public void constant(float value) {
		if (value == 0.0F) {
			this.fConst0();
		} else if (value == 1.0F) {
			this.fConst1();
		} else {
			this.ldc(value);
		}
	}

	public void constant(double value) {
		if (value == 0.0D) {
			this.dConst0();
		} else if (value == 1.0D) {
			this.dConst1();
		} else {
			this.ldc(value);
		}
	}

	public void constant(String value) {
		this.ldc(value);
	}

	public void constant(Class<?> clazz) {
		this.ldc(Type.getType(clazz));
	}

	public void constant(JavaClass cls) {
		this.ldc(Type.getObjectType(cls.internalName));
	}

	public void pop() {
		if (debug)
			logger.debug("pop");

		visitor.visitInsn(POP);
	}

	public void pop(Type type) {
		pop(type.getSize() == 2);
	}

	public void pop(boolean large) {
		if (large) {
			this.pop2();
		} else {
			this.pop();
		}
	}

	public void pop2() {
		if (debug)
			logger.debug("pop2");

		visitor.visitInsn(POP2);
	}

	public void dup() {
		if (debug)
			logger.debug("dup");

		visitor.visitInsn(DUP);
	}

	public void dup(Type type) {
		this.dup(type.getSize() == 2);
	}

	public void dup(boolean large) {
		if (large) {
			this.dup2();
		} else {
			this.dup();
		}
	}

	public void dup2() {
		if (debug)
			logger.debug("dup2");

		visitor.visitInsn(DUP2);
	}

	public void dupX1() {
		if (debug)
			logger.debug("dupx1");

		visitor.visitInsn(DUP_X1);
	}

	public void dupX1(boolean tosLarge, boolean large) {
		if (tosLarge) {
			if (large) {
				this.dup2X2();
			} else {
				this.dupX2();
			}
		} else {
			if (large) {
				this.dup2X1();
			} else {
				this.dupX1();
			}
		}
	}

	public void dupX2() {
		if (debug)
			logger.debug("dupx2");

		visitor.visitInsn(DUP_X2);
	}

	public void dup2X1() {
		if (debug)
			logger.debug("dup2_x1");

		visitor.visitInsn(DUP2_X1);
	}

	public void dup2X2() {
		if (debug)
			logger.debug("dup2_x2");

		visitor.visitInsn(DUP2_X2);
	}

	public void load(Type type, int local) {
		if (debug)
			logger.debug("load " + local);

		visitor.visitVarInsn(type.getOpcode(ILOAD), local);
	}

	public void load(JavaParameterInfo parameter) {
		this.load(Type.getType(parameter.typeDescriptor), parameter.index);
	}

	public void load(JavaLocalVariableInfo localVariable) {
		this.load(localVariable.type, localVariable.local);
	}

	public void store(Type type, int local) {
		if (debug)
			logger.debug("store " + local);

		visitor.visitVarInsn(type.getOpcode(ISTORE), local);
	}

	public void store(JavaParameterInfo parameter) {
		this.store(Type.getType(parameter.typeDescriptor), parameter.index);
	}

	public void store(JavaLocalVariableInfo localVariable) {
		this.store(localVariable.type, localVariable.local);
	}

	public void storeInt(int local) {
		if (debug)
			logger.debug("storeInt " + local);

		visitor.visitVarInsn(ISTORE, local);
	}

	public void loadInt(int local) {
		if (debug)
			logger.debug("loadInt " + local);

		visitor.visitVarInsn(ILOAD, local);
	}

	public void storeObject(int local) {
		if (debug)
			logger.debug("storeObject " + local);

		visitor.visitVarInsn(ASTORE, local);
	}

	public void loadObject(int local) {
		if (debug)
			logger.debug("loadObject " + local);

		visitor.visitVarInsn(ALOAD, local);
	}

	public void arrayLength() {
		if (debug)
			logger.debug("arrayLength");

		visitor.visitInsn(ARRAYLENGTH);
	}

	public void arrayLoad(Type type) {
		if (debug)
			logger.debug("arrayLoad");

		visitor.visitInsn(type.getOpcode(IALOAD));
	}

	public void arrayStore(Type type) {
		if (debug)
			logger.debug("arrayStore");

		visitor.visitInsn(type.getOpcode(IASTORE));
	}

	public void newArray(Type componentType) {
		if (debug)
			logger.debug("newArray");

		int sort = componentType.getSort();
		if (sort == Type.METHOD) {
			throw new RuntimeException("Unsupported array type: " + componentType);
		} else if (sort == Type.OBJECT || sort == Type.ARRAY) {
			visitor.visitTypeInsn(ANEWARRAY, componentType.getInternalName());
		} else {
			int type;
			switch (sort) {
				case Type.BOOLEAN:
					type = Opcodes.T_BOOLEAN;
					break;
				case Type.BYTE:
					type = Opcodes.T_BYTE;
					break;
				case Type.SHORT:
					type = Opcodes.T_SHORT;
					break;
				case Type.INT:
					type = Opcodes.T_INT;
					break;
				case Type.LONG:
					type = Opcodes.T_LONG;
					break;
				case Type.FLOAT:
					type = Opcodes.T_FLOAT;
					break;
				case Type.DOUBLE:
					type = Opcodes.T_DOUBLE;
					break;
				case Type.CHAR:
					type = Opcodes.T_CHAR;
					break;
				default:
					throw new RuntimeException("Unsupported array type: " + componentType);
			}
			visitor.visitIntInsn(NEWARRAY, type);
		}
	}

	public void checkCast(String internalName) {
		if (debug)
			logger.debug("checkCast " + internalName);

		visitor.visitTypeInsn(CHECKCAST, internalName);
	}

	public void checkCast(Type type) {
		this.checkCast(type.getInternalName());
	}

	public void iNeg() {
		if (debug)
			logger.debug("iNeg");

		visitor.visitInsn(INEG);
	}

	public void iAdd() {
		if (debug)
			logger.debug("iAdd");

		visitor.visitInsn(IADD);
	}

	public void iSub() {
		if (debug)
			logger.debug("iSub");

		visitor.visitInsn(ISUB);
	}

	public void iMul() {
		if (debug)
			logger.debug("iMul");

		visitor.visitInsn(IMUL);
	}

	public void iDiv() {
		if (debug)
			logger.debug("iDiv");

		visitor.visitInsn(IDIV);
	}

	public void iRem() {
		if (debug)
			logger.debug("iRem");

		visitor.visitInsn(IREM);
	}

	public void iAnd() {
		if (debug)
			logger.debug("iAnd");

		visitor.visitInsn(IAND);
	}

	public void iOr() {
		if (debug)
			logger.debug("iOr");

		visitor.visitInsn(IOR);
	}

	public void iXor() {
		if (debug)
			logger.debug("iXor");

		visitor.visitInsn(IXOR);
	}

	public void iNot() {
		if (debug)
			logger.debug("iNot");

		visitor.visitInsn(ICONST_M1);
		visitor.visitInsn(IXOR);
	}

	public void invertBoolean() {
		if (debug)
			logger.debug("invert bool");

		final Label l1 = new Label();
		final Label l2 = new Label();

		ifEQ(l1);
		iConst0();
		goTo(l2);
		label(l1);
		iConst1();
		label(l2);
	}

	public void invertInt() {
		if (debug)
			logger.debug("invert int");

		iConstM1();
		iXor();
	}

	public void iShr() {
		if (debug)
			logger.debug("iShr");

		visitor.visitInsn(ISHR);
	}

	public void iUShr() {
		if (debug)
			logger.debug("iUShr");

		visitor.visitInsn(IUSHR);
	}

	public void iShl() {
		if (debug)
			logger.debug("iShl");

		visitor.visitInsn(ISHL);
	}

	public void lNeg() {
		if (debug)
			logger.debug("lNeg");

		visitor.visitInsn(LNEG);
	}

	public void lAdd() {
		if (debug)
			logger.debug("lAdd");

		visitor.visitInsn(LADD);
	}

	public void lSub() {
		if (debug)
			logger.debug("lSub");

		visitor.visitInsn(LSUB);
	}

	public void lMul() {
		if (debug)
			logger.debug("lMul");

		visitor.visitInsn(LMUL);
	}

	public void lDiv() {
		if (debug)
			logger.debug("lDiv");

		visitor.visitInsn(LDIV);
	}

	public void lRem() {
		if (debug)
			logger.debug("lRem");

		visitor.visitInsn(LREM);
	}

	public void lAnd() {
		if (debug)
			logger.debug("lAnd");

		visitor.visitInsn(LAND);
	}

	public void lOr() {
		if (debug)
			logger.debug("lOr");

		visitor.visitInsn(LOR);
	}

	public void lXor() {
		if (debug)
			logger.debug("lXor");

		visitor.visitInsn(LXOR);
	}

	public void lNot() {
		if (debug)
			logger.debug("lNot");

		constant(-1L);
		lXor();
	}

	public void lShr() {
		if (debug)
			logger.debug("lShr");

		visitor.visitInsn(LSHR);
	}

	public void lUShr() {
		if (debug)
			logger.debug("lUShr");

		visitor.visitInsn(LUSHR);
	}

	public void lShl() {
		if (debug)
			logger.debug("lShl");

		visitor.visitInsn(LSHL);
	}

	public void fNeg() {
		if (debug)
			logger.debug("fNeg");

		visitor.visitInsn(FNEG);
	}

	public void fAdd() {
		if (debug)
			logger.debug("fAdd");

		visitor.visitInsn(FADD);
	}

	public void fSub() {
		if (debug)
			logger.debug("fSub");

		visitor.visitInsn(FSUB);
	}

	public void fMul() {
		if (debug)
			logger.debug("fMul");

		visitor.visitInsn(FMUL);
	}

	public void fDiv() {
		if (debug)
			logger.debug("fDiv");

		visitor.visitInsn(FDIV);
	}

	public void fRem() {
		if (debug)
			logger.debug("fRem");

		visitor.visitInsn(FREM);
	}

	public void dNeg() {
		if (debug)
			logger.debug("dNeg");

		visitor.visitInsn(DNEG);
	}

	public void dAdd() {
		if (debug)
			logger.debug("dAdd");

		visitor.visitInsn(DADD);
	}

	public void dSub() {
		if (debug)
			logger.debug("dSub");

		visitor.visitInsn(DSUB);
	}

	public void dMul() {
		if (debug)
			logger.debug("dMul");

		visitor.visitInsn(DMUL);
	}

	public void dDiv() {
		if (debug)
			logger.debug("dDiv");

		visitor.visitInsn(DDIV);
	}

	public void dRem() {
		if (debug)
			logger.debug("dRem");

		visitor.visitInsn(DREM);
	}

	public void iinc(int local) {
		iinc(local, 1);
	}

	public void idec(int local) {
		iinc(local, -1);
	}

	public void iinc(int local, int increment) {
		if (debug)
			logger.debug("iinc " + local + " + " + increment);

		visitor.visitIincInsn(local, increment);
	}

	public void i2b() {
		if (debug)
			logger.debug("i2b");

		visitor.visitInsn(I2B);
	}

	public void i2s() {
		if (debug)
			logger.debug("i2s");

		visitor.visitInsn(I2S);
	}

	public void i2l() {
		if (debug)
			logger.debug("i2l");

		visitor.visitInsn(I2L);
	}

	public void i2f() {
		if (debug)
			logger.debug("i2f");

		visitor.visitInsn(I2F);
	}

	public void i2d() {
		if (debug)
			logger.debug("i2d");

		visitor.visitInsn(I2D);
	}

	public void l2i() {
		if (debug)
			logger.debug("l2i");

		visitor.visitInsn(L2I);
	}

	public void l2f() {
		if (debug)
			logger.debug("l2f");

		visitor.visitInsn(L2F);
	}

	public void l2d() {
		if (debug)
			logger.debug("l2d");

		visitor.visitInsn(L2D);
	}

	public void f2i() {
		if (debug)
			logger.debug("f2i");

		visitor.visitInsn(F2I);
	}

	public void f2l() {
		if (debug)
			logger.debug("f2l");

		visitor.visitInsn(F2L);
	}

	public void f2d() {
		if (debug)
			logger.debug("f2d");

		visitor.visitInsn(F2D);
	}

	public void d2i() {
		if (debug)
			logger.debug("d2i");

		visitor.visitInsn(D2I);
	}

	public void d2l() {
		if (debug)
			logger.debug("d2l");

		visitor.visitInsn(D2L);
	}

	public void d2f() {
		if (debug)
			logger.debug("d2f");

		visitor.visitInsn(D2F);
	}

	public void lCmp() {
		if (debug)
			logger.debug("lCmp");

		visitor.visitInsn(LCMP);
	}

	public void fCmp() {
		if (debug)
			logger.debug("fCmp");

		visitor.visitInsn(FCMPL);
	}

	public void dCmp() {
		if (debug)
			logger.debug("dCmp");

		visitor.visitInsn(DCMPL);
	}

	public void instanceOf(String descriptor) {
		if (debug)
			logger.debug("instanceOf " + descriptor);

		visitor.visitTypeInsn(INSTANCEOF, descriptor);
	}

	public void instanceOf(Type type) {
		this.instanceOf(type.getDescriptor());
	}

	public void invokeStatic(JavaNativeMethod method) {
		visitor.visitMethodInsn(
				INVOKESTATIC,
				method.cls.internalName,
				method.name,
				method.descriptor,
				method.cls.isInterface());
	}

	public void invokeSpecial(String ownerInternalName, String name, String descriptor) {
		if (debug)
			logger.debug("invokeSpecial " + ownerInternalName + '.' + name + descriptor);

		visitor.visitMethodInsn(INVOKESPECIAL, ownerInternalName, name, descriptor, false);
	}

	public void invokeSpecial(Class owner, String name, String descriptor) {
		invokeSpecial(Type.getInternalName(owner), name, descriptor);
	}

	public void invokeSpecial(JavaNativeMethod method) {
		invokeSpecial(method.cls.internalName, method.name, method.descriptor);
	}

	public void invokeVirtual(JavaNativeMethod method) {
		if (method.kind == JavaNativeMethod.Kind.INTERFACE) {
			invokeInterface(method);
			return;
		}
		if (debug)
			logger.debug("invokeVirtual " + method.cls.internalName + '.' + method.name + method.descriptor);

		visitor.visitMethodInsn(INVOKEVIRTUAL, method.cls.internalName, method.name, method.descriptor, false);
	}

	public void invokeInterface(JavaNativeMethod method) {
		if (debug)
			logger.debug("invokeInterface " + method.cls.internalName + '.' + method.name + method.descriptor);

		visitor.visitMethodInsn(INVOKEINTERFACE, method.cls.internalName, method.name, method.descriptor, true);
	}

	public void newObject(String internalName) {
		if (debug)
			logger.debug("newObject " + internalName);

		visitor.visitTypeInsn(NEW, internalName);
	}

	public void newObject(JavaClass cls) {
		this.newObject(cls.internalName);
	}

	public void goTo(Label lbl) {
		if (debug)
			logger.debug("goTo " + getLabelName(lbl));

		visitor.visitJumpInsn(GOTO, lbl);
	}

	/**
	 * Jump if TOS == 0.
	 *
	 * @param lbl target label
	 */
	public void ifEQ(Label lbl) {
		if (debug)
			logger.debug("ifEQ " + getLabelName(lbl));

		visitor.visitJumpInsn(IFEQ, lbl);
	}

	public void ifNE(Label lbl) {
		if (debug)
			logger.debug("ifNE " + getLabelName(lbl));

		visitor.visitJumpInsn(IFNE, lbl);
	}

	public void ifLT(Label lbl) {
		if (debug)
			logger.debug("ifLT " + getLabelName(lbl));

		visitor.visitJumpInsn(IFLT, lbl);
	}

	public void ifGT(Label lbl) {
		if (debug)
			logger.debug("ifGT " + getLabelName(lbl));

		visitor.visitJumpInsn(IFGT, lbl);
	}

	public void ifGE(Label lbl) {
		if (debug)
			logger.debug("ifGE " + getLabelName(lbl));

		visitor.visitJumpInsn(IFGE, lbl);
	}

	public void ifLE(Label lbl) {
		if (debug)
			logger.debug("ifLE " + getLabelName(lbl));

		visitor.visitJumpInsn(IFLE, lbl);
	}

	public void ifICmpLE(Label lbl) {
		if (debug)
			logger.debug("ifICmpLE " + getLabelName(lbl));

		visitor.visitJumpInsn(IF_ICMPLE, lbl);
	}

	public void ifICmpGE(Label lbl) {
		if (debug)
			logger.debug("ifICmpGE " + getLabelName(lbl));

		visitor.visitJumpInsn(IF_ICMPGE, lbl);
	}

	public void ifICmpEQ(Label lbl) {
		if (debug)
			logger.debug("ifICmpEQ " + getLabelName(lbl));

		visitor.visitJumpInsn(IF_ICMPEQ, lbl);
	}

	public void ifICmpNE(Label lbl) {
		if (debug)
			logger.debug("ifICmpNE " + getLabelName(lbl));

		visitor.visitJumpInsn(IF_ICMPNE, lbl);
	}

	public void ifICmpGT(Label lbl) {
		if (debug)
			logger.debug("ifICmpGT " + getLabelName(lbl));

		visitor.visitJumpInsn(IF_ICMPGT, lbl);
	}

	public void ifICmpLT(Label lbl) {
		if (debug)
			logger.debug("ifICmpLT " + getLabelName(lbl));

		visitor.visitJumpInsn(IF_ICMPLT, lbl);
	}

	public void ifACmpEq(Label lbl) {
		if (debug)
			logger.debug("ifICmpEQ " + getLabelName(lbl));

		visitor.visitJumpInsn(IF_ACMPEQ, lbl);
	}

	public void ifACmpNe(Label lbl) {
		if (debug)
			logger.debug("ifACmpNE " + getLabelName(lbl));

		visitor.visitJumpInsn(IF_ACMPNE, lbl);
	}

	public void ifNull(Label lbl) {
		if (debug)
			logger.debug("ifNull " + getLabelName(lbl));

		visitor.visitJumpInsn(IFNULL, lbl);
	}

	public void ifNonNull(Label lbl) {
		if (debug)
			logger.debug("ifNonNull " + getLabelName(lbl));

		visitor.visitJumpInsn(IFNONNULL, lbl);
	}

	public void ret() {
		if (debug)
			logger.debug("ret");

		visitor.visitInsn(RETURN);
	}

	public void returnType(Type type) {
		if (debug)
			logger.debug("return " + type.getDescriptor());

		visitor.visitInsn(type.getOpcode(IRETURN));
	}

	public void returnInt() {
		if (debug)
			logger.debug("ireturn");

		visitor.visitInsn(IRETURN);
	}

	public void returnObject() {
		if (debug)
			logger.debug("areturn");

		visitor.visitInsn(ARETURN);
	}

	public void getField(String owner, String name, String descriptor) {
		if (debug)
			logger.debug("getField " + owner + '.' + name + ":" + descriptor);

		visitor.visitFieldInsn(GETFIELD, owner, name, descriptor);
	}

	public void getField(JavaNativeField field) {
		this.getField(field.cls.internalName, field.name, field.descriptor);
	}

	public void putField(String owner, String name, String descriptor) {
		if (debug)
			logger.debug("putField " + owner + '.' + name + ":" + descriptor);

		visitor.visitFieldInsn(PUTFIELD, owner, name, descriptor);
	}

	public void putField(JavaNativeField field) {
		this.putField(field.cls.internalName, field.name, field.descriptor);
	}

	public void getStaticField(String owner, String name, String descriptor) {
		if (debug)
			logger.debug("getStatic " + owner + '.' + name + ":" + descriptor);

		visitor.visitFieldInsn(GETSTATIC, owner, name, descriptor);
	}

	public void getStaticField(JavaNativeField field) {
		getStaticField(field.cls.internalName, field.name, field.descriptor);
	}

	public void putStaticField(String owner, String name, String descriptor) {
		if (debug)
			logger.debug("putStatic " + owner + '.' + name + ":" + descriptor);

		visitor.visitFieldInsn(PUTSTATIC, owner, name, descriptor);
	}

	public void putStaticField(JavaNativeField field) {
		this.putStaticField(field.cls.internalName, field.name, field.descriptor);
	}

	public void aThrow() {
		visitor.visitInsn(ATHROW);
	}

	public void position(int position) {
		if (lineNumberLabels.contains(position)) {
			return;
		}
		Label label = new Label();
		visitor.visitLabel(label);
		visitor.visitLineNumber(position, label);
		lineNumberLabels.add(position);

	}

	public void swap() {
		if (debug)
			logger.debug("swap");
		visitor.visitInsn(SWAP);
	}

	private String getLabelName(Label lbl) {
		if (labelNames == null)
			labelNames = new HashMap<>();

		if (!labelNames.containsKey(lbl)) {
			labelNames.put(lbl, "L" + labelIndex++);
		}

		return labelNames.get(lbl);
	}

	public void stringAdd() {
		invokeVirtual(STRING_CONCAT);
	}

	public void tryCatch(Label start, Label end, Label handler, String type) {
		if (debug)
			logger.debug("TryCatch " + getLabelName(start) + ", " + getLabelName(end) + ", " + getLabelName(handler) + ", TYPE: " + type);
		visitor.visitTryCatchBlock(start, end, handler, type);
	}

	public void nameVariable(int local, String name, Label start, Label end, Type type) {
		if (nameVariables && name != null && type != null && end != null && end != start)
			visitor.visitLocalVariable(name, type.getDescriptor(), null, start, end, local);
	}

	public void nameParameter(int modifier, String name) {
		if (nameVariables)
			visitor.visitParameter(name, modifier);
	}

	public void lookupSwitch(Label defaultLabel, JavaSwitchLabel[] switchLabels) {
		int[] keys = new int[switchLabels.length];
		Label[] labels = new Label[switchLabels.length];
		for (int i = 0; i < switchLabels.length; i++) {
			keys[i] = switchLabels[i].key;
			labels[i] = switchLabels[i].label;
		}

		if (debug) {
			logger.debug("lookupSwitch");
			for (int i = 0; i < switchLabels.length; i++) {
				logger.debug("  " + keys[i] + " -> " + getLabelName(labels[i]));
			}
			logger.debug("  default -> " + getLabelName(defaultLabel));
		}

		visitor.visitLookupSwitchInsn(defaultLabel, keys, labels);
	}

	public void addVariableInfo(JavaLocalVariableInfo info) {
		localVariableInfos.add(info);
	}
}
