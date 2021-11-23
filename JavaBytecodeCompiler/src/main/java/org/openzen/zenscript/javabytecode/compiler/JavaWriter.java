package org.openzen.zenscript.javabytecode.compiler;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.LocalVariablesSorter;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.logging.IZSLogger;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.statement.VariableID;
import org.openzen.zenscript.javabytecode.JavaLocalVariableInfo;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaField;
import org.openzen.zenscript.javashared.JavaMethod;
import org.openzen.zenscript.javashared.JavaParameterInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

public class JavaWriter {
	private static final JavaMethod STRING_CONCAT = JavaMethod.getNativeStatic(
			JavaClass.STRING,
			"concat",
			"(Ljava/lang/String;)Ljava/lang/String;");

	public final JavaMethod method;
	public final HighLevelDefinition forDefinition;
	public final ClassVisitor clazzVisitor;
	private final IZSLogger logger;

	private final CodePosition position;
	private final LocalVariablesSorter visitor;
	private final List<JavaLocalVariableInfo> localVariableInfos = new ArrayList<>();
	private final Map<VariableID, JavaLocalVariableInfo> localVariables = new HashMap<>();
	private final List<Integer> lineNumberLabels = new ArrayList<>();
	private boolean debug = false;
	private boolean nameVariables;
	private int labelIndex = 1;
	private Map<Label, String> labelNames = new HashMap<>();

	public JavaWriter(
			IZSLogger logger,
			CodePosition position,
			ClassVisitor visitor,
			boolean nameVariables,
			JavaMethod method,
			HighLevelDefinition forDefinition,
			String signature,
			String[] exceptions,
			String... annotations) {
		this(logger, position, visitor, nameVariables, method, forDefinition, false, signature, method.descriptor, exceptions, annotations);
		this.position(position.fromLine);
	}


	public JavaWriter(
			IZSLogger logger,
			CodePosition position,
			ClassVisitor visitor,
			boolean nameVariables,
			JavaMethod method,
			HighLevelDefinition forDefinition,
			boolean isExtension,
			String signature,
			String descriptor,
			String[] exceptions,
			String... annotations) {
		this.logger = logger;
		this.clazzVisitor = visitor;
		this.method = method;
		this.forDefinition = forDefinition;
		this.position = position;

		final int access = isExtension ? method.modifiers | ACC_STATIC : method.modifiers;
		final MethodVisitor methodVisitor = visitor.visitMethod(access, method.name, descriptor, signature, exceptions);

		for (String annotation : annotations) {
			methodVisitor.visitAnnotation(annotation, true).visitEnd();
		}

		this.visitor = new LocalVariablesSorter(access, descriptor, methodVisitor);
		this.nameVariables = nameVariables;
	}


	public JavaWriter(IZSLogger logger, CodePosition position, ClassVisitor visitor, JavaMethod method, HighLevelDefinition forDefinition, String signature, String[] exceptions, String... annotations) {
		this(logger, position, visitor, true, method, forDefinition, signature, exceptions, annotations);
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
			//ex.printStackTrace();
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

	public int local(Class cls) {
		return visitor.newLocal(Type.getType(cls));
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

	public void constant(Object value) {
		if (value == null)
			throw new NullPointerException("Value cannot be null");

		if (debug)
			logger.debug("ldc " + value);

		visitor.visitLdcInsn(value);
	}

	public void constantClass(JavaClass cls) {
		visitor.visitLdcInsn(Type.getObjectType(cls.internalName));
	}

	public void pop() {
		if (debug)
			logger.debug("pop");

		visitor.visitInsn(POP);
	}

	public void pop(boolean large) {
		if (debug)
			logger.debug("pop");

		visitor.visitInsn(large ? POP2 : POP);
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
		if (debug)
			logger.debug("dup");

		visitor.visitInsn(type.getSize() == 2 ? DUP2 : DUP);
	}

	public void dup(boolean large) {
		if (debug)
			logger.debug("dup");

		visitor.visitInsn(large ? DUP2 : DUP);
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
		if (debug)
			logger.debug("dupx1");

		if (tosLarge) {
			visitor.visitInsn(large ? DUP2_X2 : DUP_X2);
		} else {
			visitor.visitInsn(large ? DUP2_X1 : DUP_X1);
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

	public void store(Type type, int local) {
		if (debug)
			logger.debug("store " + local);

		visitor.visitVarInsn(type.getOpcode(ISTORE), local);
	}

	public void load(Type type, int local) {
		if (debug)
			logger.debug("load " + local);

		visitor.visitVarInsn(type.getOpcode(ILOAD), local);
	}

	public void load(JavaParameterInfo parameter) {
		if (debug)
			logger.debug("load " + parameter.index);

		visitor.visitVarInsn(Type.getType(parameter.typeDescriptor).getOpcode(ILOAD), parameter.index);
	}

	public void load(JavaLocalVariableInfo localVariable) {
		if (debug)
			logger.debug("load " + localVariable.local);

		visitor.visitVarInsn(localVariable.type.getOpcode(ILOAD), localVariable.local);
	}

	public void store(JavaParameterInfo parameter) {
		if (debug)
			logger.debug("store " + parameter.index);

		visitor.visitVarInsn(Type.getType(parameter.typeDescriptor).getOpcode(ISTORE), parameter.index);
	}

	public void store(JavaLocalVariableInfo localVariable) {
		if (debug)
			logger.debug("store " + localVariable.local);

		visitor.visitVarInsn(localVariable.type.getOpcode(ISTORE), localVariable.local);
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
		if (debug)
			logger.debug("checkCast " + type.getDescriptor());

		visitor.visitTypeInsn(CHECKCAST, type.getInternalName());
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

		constant((long) -1);
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
		if (debug)
			logger.debug("instanceOf " + type.getDescriptor());

		visitor.visitTypeInsn(INSTANCEOF, type.getDescriptor());
	}

	public void invokeStatic(JavaMethod method) {
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

	public void invokeSpecial(JavaMethod method) {
		invokeSpecial(method.cls.internalName, method.name, method.descriptor);
	}

	public void invokeVirtual(JavaMethod method) {
		if (method.kind == JavaMethod.Kind.INTERFACE) {
			invokeInterface(method);
			return;
		}
		if (debug)
			logger.debug("invokeVirtual " + method.cls.internalName + '.' + method.name + method.descriptor);

		visitor.visitMethodInsn(INVOKEVIRTUAL, method.cls.internalName, method.name, method.descriptor, false);
	}

	public void invokeInterface(JavaMethod method) {
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
		if (debug)
			logger.debug("newObject " + cls.internalName);

		visitor.visitTypeInsn(NEW, cls.internalName);
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

	public void getField(JavaField field) {
		if (debug)
			logger.debug("getField " + field.cls.internalName + '.' + field.name + ":" + field.descriptor);

		visitor.visitFieldInsn(GETFIELD, field.cls.internalName, field.name, field.descriptor);
	}

	public void putField(String owner, String name, String descriptor) {
		if (debug)
			logger.debug("putField " + owner + '.' + name + ":" + descriptor);

		visitor.visitFieldInsn(PUTFIELD, owner, name, descriptor);
	}

	public void putField(JavaField field) {
		if (debug)
			logger.debug("putField " + field.cls.internalName + '.' + field.name + ":" + field.descriptor);

		visitor.visitFieldInsn(PUTFIELD, field.cls.internalName, field.name, field.descriptor);
	}

	public void getStaticField(String owner, String name, String descriptor) {
		if (debug)
			logger.debug("getStatic " + owner + '.' + name + ":" + descriptor);

		visitor.visitFieldInsn(GETSTATIC, owner, name, descriptor);
	}

	public void getStaticField(JavaField field) {
		if (debug)
			logger.debug("getStaticField " + field.cls.internalName + '.' + field.name + ":" + field.descriptor);

		visitor.visitFieldInsn(GETSTATIC, field.cls.internalName, field.name, field.descriptor);
	}

	public void putStaticField(String owner, String name, String descriptor) {
		if (debug)
			logger.debug("putStatic " + owner + '.' + name + ":" + descriptor);

		visitor.visitFieldInsn(PUTSTATIC, owner, name, descriptor);
	}

	public void putStaticField(JavaField field) {
		if (debug)
			logger.debug("putStaticField " + field.cls.internalName + '.' + field.name + ":" + field.descriptor);

		visitor.visitFieldInsn(PUTSTATIC, field.cls.internalName, field.name, field.descriptor);
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

	public String createLabelName() {
		return "L" + labelIndex++;
	}

	public void putNamedLabel(Label lbl, String name) {
		if (labelNames == null)
			labelNames = new HashMap<>();
		labelNames.put(lbl, name);
	}

	public void stringAdd() {
		invokeVirtual(STRING_CONCAT);
	}

	public Label getNamedLabel(String label) {
		for (Map.Entry<Label, String> entry : labelNames.entrySet()) {
			if (entry.getValue().matches(label))
				return entry.getKey();
		}
		throw new RuntimeException("Label " + label + " not found!");
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
