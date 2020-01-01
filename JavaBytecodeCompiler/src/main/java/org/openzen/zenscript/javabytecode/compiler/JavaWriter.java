package org.openzen.zenscript.javabytecode.compiler;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.LocalVariablesSorter;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.statement.VariableID;
import org.openzen.zenscript.javabytecode.JavaLocalVariableInfo;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaField;
import org.openzen.zenscript.javashared.JavaMethod;
import org.openzen.zenscript.javashared.JavaParameterInfo;

import javax.lang.model.element.VariableElement;
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
			CodePosition position,
			ClassVisitor visitor,
			boolean nameVariables,
			JavaMethod method,
			HighLevelDefinition forDefinition,
			String signature,
			String[] exceptions,
			String... annotations) {
		this(position, visitor, nameVariables, method, forDefinition, false, signature, method.descriptor, exceptions, annotations);
	}
	
	
	public JavaWriter(
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
		this.clazzVisitor = visitor;
		this.method = method;
		this.forDefinition = forDefinition;
		this.position = position;
		
		final MethodVisitor methodVisitor = visitor.visitMethod(isExtension ? method.modifiers | Opcodes.ACC_STATIC : method.modifiers, method.name, descriptor, signature, exceptions);
		
		for (String annotation : annotations) {
			methodVisitor.visitAnnotation(annotation, true).visitEnd();
		}
		
		this.visitor = new LocalVariablesSorter(isExtension ? method.modifiers | Opcodes.ACC_STATIC : method.modifiers, descriptor, methodVisitor);
		this.nameVariables = nameVariables;
	}
	
	
	public JavaWriter(CodePosition position, ClassVisitor visitor, JavaMethod method, HighLevelDefinition forDefinition, String signature, String[] exceptions, String... annotations) {
		this(position, visitor, true, method, forDefinition, signature, exceptions, annotations);
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
			System.out.println("--start--");
		
		visitor.visitCode();
	}
	
	public void end() {
		if (debug)
			System.out.println("--end--");
		
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
			System.out.println("Label " + getLabelName(label));
		
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
			System.out.println("iconst0");
		
		visitor.visitInsn(ICONST_0);
	}
	
	public void iConst1() {
		if (debug)
			System.out.println("iconst1");
		
		visitor.visitInsn(ICONST_1);
	}
	
	public void biPush(byte value) {
		if (debug)
			System.out.println("bipush");
		
		visitor.visitIntInsn(BIPUSH, value);
	}
	
	public void siPush(short value) {
		if (debug)
			System.out.println("sipush");
		
		visitor.visitIntInsn(SIPUSH, value);
	}
	
	public void aConstNull() {
		if (debug)
			System.out.println("aConstNull");
		
		visitor.visitInsn(ACONST_NULL);
	}
	
	public void constant(Object value) {
		if (value == null)
			throw new NullPointerException("Value cannot be null");
		
		if (debug)
			System.out.println("ldc " + value);
		
		visitor.visitLdcInsn(value);
	}
	
	public void constantClass(JavaClass cls) {
		visitor.visitLdcInsn(Type.getType(cls.internalName));
	}
	
	public void pop() {
		if (debug)
			System.out.println("pop");
		
		visitor.visitInsn(POP);
	}
	
	public void pop(boolean large) {
		if (debug)
			System.out.println("pop");
		
		visitor.visitInsn(large ? POP2 : POP);
	}
	
	public void dup() {
		if (debug)
			System.out.println("dup");
		
		visitor.visitInsn(DUP);
	}
	
	public void dup(Type type) {
		if (debug)
			System.out.println("dup");
		
		visitor.visitInsn(type.getSize() == 2 ? DUP2 : DUP);
	}
	
	public void dup(boolean large) {
		if (debug)
			System.out.println("dup");
		
		visitor.visitInsn(large ? DUP2 : DUP);
	}
	
	public void dup2() {
		if (debug)
			System.out.println("dup2");
		
		visitor.visitInsn(DUP2);
	}
	
	public void dupX1() {
		if (debug)
			System.out.println("dupx1");
		
		visitor.visitInsn(DUP_X1);
	}
	
	public void dupX1(boolean tosLarge, boolean large) {
		if (debug)
			System.out.println("dupx1");
		
		if (tosLarge) {
			visitor.visitInsn(large ? DUP2_X2 : DUP_X2);
		} else {
			visitor.visitInsn(large ? DUP2_X1 : DUP_X1);
		}
	}
	
	public void dupX2() {
		if (debug)
			System.out.println("dupx2");
		
		visitor.visitInsn(DUP_X2);
	}
	
	public void dup2X1() {
		if (debug)
			System.out.println("dup2_x1");
		
		visitor.visitInsn(DUP2_X1);
	}
	
	public void dup2X2() {
		if (debug)
			System.out.println("dup2_x2");
		
		visitor.visitInsn(DUP2_X2);
	}
	
	public void store(Type type, int local) {
		if (debug)
			System.out.println("store " + local);
		
		visitor.visitVarInsn(type.getOpcode(ISTORE), local);
	}
	
	public void load(Type type, int local) {
		if (debug)
			System.out.println("load " + local);
		
		visitor.visitVarInsn(type.getOpcode(ILOAD), local);
	}
	
	public void load(JavaParameterInfo parameter) {
		if (debug)
			System.out.println("load " + parameter.index);
		
		visitor.visitVarInsn(Type.getType(parameter.typeDescriptor).getOpcode(ILOAD), parameter.index);
	}
	
	public void load(JavaLocalVariableInfo localVariable) {
		if (debug)
			System.out.println("load " + localVariable.local);
		
		visitor.visitVarInsn(localVariable.type.getOpcode(ILOAD), localVariable.local);
	}
	
	public void store(JavaParameterInfo parameter) {
		if (debug)
			System.out.println("store " + parameter.index);
		
		visitor.visitVarInsn(Type.getType(parameter.typeDescriptor).getOpcode(ISTORE), parameter.index);
	}
	
	public void store(JavaLocalVariableInfo localVariable) {
		if (debug)
			System.out.println("store " + localVariable.local);
		
		visitor.visitVarInsn(localVariable.type.getOpcode(ISTORE), localVariable.local);
	}
	
	public void storeInt(int local) {
		if (debug)
			System.out.println("storeInt " + local);
		
		visitor.visitVarInsn(ISTORE, local);
	}
	
	public void loadInt(int local) {
		if (debug)
			System.out.println("loadInt " + local);
		
		visitor.visitVarInsn(ILOAD, local);
	}
	
	public void storeObject(int local) {
		if (debug)
			System.out.println("storeObject " + local);
		
		visitor.visitVarInsn(ASTORE, local);
	}
	
	public void loadObject(int local) {
		if (debug)
			System.out.println("loadObject " + local);
		
		visitor.visitVarInsn(ALOAD, local);
	}
	
	public void arrayLength() {
		if (debug)
			System.out.println("arrayLength");
		
		visitor.visitInsn(ARRAYLENGTH);
	}
	
	public void arrayLoad(Type type) {
		if (debug)
			System.out.println("arrayLoad");
		
		visitor.visitInsn(type.getOpcode(IALOAD));
	}
	
	public void arrayStore(Type type) {
		if (debug)
			System.out.println("arrayStore");
		
		visitor.visitInsn(type.getOpcode(IASTORE));
	}
	
	public void newArray(Type componentType) {
		if (debug)
			System.out.println("newArray");
		
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
			System.out.println("checkCast " + internalName);
		
		visitor.visitTypeInsn(CHECKCAST, internalName);
	}
	
	public void checkCast(Type type) {
		if (debug)
			System.out.println("checkCast " + type.getDescriptor());
		
		visitor.visitTypeInsn(CHECKCAST, type.getInternalName());
	}
	
	public void iNeg() {
		if (debug)
			System.out.println("iNeg");
		
		visitor.visitInsn(INEG);
	}
	
	public void iAdd() {
		if (debug)
			System.out.println("iAdd");
		
		visitor.visitInsn(IADD);
	}
	
	public void iSub() {
		if (debug)
			System.out.println("iSub");
		
		visitor.visitInsn(ISUB);
	}
	
	public void iMul() {
		if (debug)
			System.out.println("iMul");
		
		visitor.visitInsn(IMUL);
	}
	
	public void iDiv() {
		if (debug)
			System.out.println("iDiv");
		
		visitor.visitInsn(IDIV);
	}
	
	public void iRem() {
		if (debug)
			System.out.println("iRem");
		
		visitor.visitInsn(IREM);
	}
	
	public void iAnd() {
		if (debug)
			System.out.println("iAnd");
		
		visitor.visitInsn(IAND);
	}
	
	public void iOr() {
		if (debug)
			System.out.println("iOr");
		
		visitor.visitInsn(IOR);
	}
	
	public void iXor() {
		if (debug)
			System.out.println("iXor");
		
		visitor.visitInsn(IXOR);
	}
	
	public void iNot() {
		if (debug)
			System.out.println("iNot");
		
		visitor.visitInsn(ICONST_M1);
		visitor.visitInsn(IXOR);
	}
	
	public void iXorVs1() {
		if (debug)
			System.out.println("iXor against '1'");
		
		visitor.visitInsn(ICONST_1);
		visitor.visitInsn(IXOR);
	}
	
	public void iShr() {
		if (debug)
			System.out.println("iShr");
		
		visitor.visitInsn(ISHR);
	}
	
	public void iUShr() {
		if (debug)
			System.out.println("iUShr");
		
		visitor.visitInsn(IUSHR);
	}
	
	public void iShl() {
		if (debug)
			System.out.println("iShl");
		
		visitor.visitInsn(ISHL);
	}
	
	public void lNeg() {
		if (debug)
			System.out.println("lNeg");
		
		visitor.visitInsn(LNEG);
	}
	
	public void lAdd() {
		if (debug)
			System.out.println("lAdd");
		
		visitor.visitInsn(LADD);
	}
	
	public void lSub() {
		if (debug)
			System.out.println("lSub");
		
		visitor.visitInsn(LSUB);
	}
	
	public void lMul() {
		if (debug)
			System.out.println("lMul");
		
		visitor.visitInsn(LMUL);
	}
	
	public void lDiv() {
		if (debug)
			System.out.println("lDiv");
		
		visitor.visitInsn(LDIV);
	}
	
	public void lRem() {
		if (debug)
			System.out.println("lRem");
		
		visitor.visitInsn(LREM);
	}
	
	public void lAnd() {
		if (debug)
			System.out.println("lAnd");
		
		visitor.visitInsn(LAND);
	}
	
	public void lOr() {
		if (debug)
			System.out.println("lOr");
		
		visitor.visitInsn(LOR);
	}
	
	public void lXor() {
		if (debug)
			System.out.println("lXor");
		
		visitor.visitInsn(LXOR);
	}
	
	public void lNot() {
		if (debug)
			System.out.println("lNot");
		
		constant((long) -1);
		lXor();
	}
	
	public void lShr() {
		if (debug)
			System.out.println("lShr");
		
		visitor.visitInsn(LSHR);
	}
	
	public void lUShr() {
		if (debug)
			System.out.println("lUShr");
		
		visitor.visitInsn(LUSHR);
	}
	
	public void lShl() {
		if (debug)
			System.out.println("lShl");
		
		visitor.visitInsn(LSHL);
	}
	
	public void fNeg() {
		if (debug)
			System.out.println("fNeg");
		
		visitor.visitInsn(FNEG);
	}
	
	public void fAdd() {
		if (debug)
			System.out.println("fAdd");
		
		visitor.visitInsn(FADD);
	}
	
	public void fSub() {
		if (debug)
			System.out.println("fSub");
		
		visitor.visitInsn(FSUB);
	}
	
	public void fMul() {
		if (debug)
			System.out.println("fMul");
		
		visitor.visitInsn(FMUL);
	}
	
	public void fDiv() {
		if (debug)
			System.out.println("fDiv");
		
		visitor.visitInsn(FDIV);
	}
	
	public void fRem() {
		if (debug)
			System.out.println("fRem");
		
		visitor.visitInsn(FREM);
	}
	
	public void dNeg() {
		if (debug)
			System.out.println("dNeg");
		
		visitor.visitInsn(DNEG);
	}
	
	public void dAdd() {
		if (debug)
			System.out.println("dAdd");
		
		visitor.visitInsn(DADD);
	}
	
	public void dSub() {
		if (debug)
			System.out.println("dSub");
		
		visitor.visitInsn(DSUB);
	}
	
	public void dMul() {
		if (debug)
			System.out.println("dMul");
		
		visitor.visitInsn(DMUL);
	}
	
	public void dDiv() {
		if (debug)
			System.out.println("dDiv");
		
		visitor.visitInsn(DDIV);
	}
	
	public void dRem() {
		if (debug)
			System.out.println("dRem");
		
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
			System.out.println("iinc " + local + " + " + increment);
		
		visitor.visitIincInsn(local, increment);
	}
	
	public void i2b() {
		if (debug)
			System.out.println("i2b");
		
		visitor.visitInsn(I2B);
	}
	
	public void i2s() {
		if (debug)
			System.out.println("i2s");
		
		visitor.visitInsn(I2S);
	}
	
	public void i2l() {
		if (debug)
			System.out.println("i2l");
		
		visitor.visitInsn(I2L);
	}
	
	public void i2f() {
		if (debug)
			System.out.println("i2f");
		
		visitor.visitInsn(I2F);
	}
	
	public void i2d() {
		if (debug)
			System.out.println("i2d");
		
		visitor.visitInsn(I2D);
	}
	
	public void l2i() {
		if (debug)
			System.out.println("l2i");
		
		visitor.visitInsn(L2I);
	}
	
	public void l2f() {
		if (debug)
			System.out.println("l2f");
		
		visitor.visitInsn(L2F);
	}
	
	public void l2d() {
		if (debug)
			System.out.println("l2d");
		
		visitor.visitInsn(L2D);
	}
	
	public void f2i() {
		if (debug)
			System.out.println("f2i");
		
		visitor.visitInsn(F2I);
	}
	
	public void f2l() {
		if (debug)
			System.out.println("f2l");
		
		visitor.visitInsn(F2L);
	}
	
	public void f2d() {
		if (debug)
			System.out.println("f2d");
		
		visitor.visitInsn(F2D);
	}
	
	public void d2i() {
		if (debug)
			System.out.println("d2i");
		
		visitor.visitInsn(D2I);
	}
	
	public void d2l() {
		if (debug)
			System.out.println("d2l");
		
		visitor.visitInsn(D2L);
	}
	
	public void d2f() {
		if (debug)
			System.out.println("d2f");
		
		visitor.visitInsn(D2F);
	}
	
	public void lCmp() {
		if (debug)
			System.out.println("lCmp");
		
		visitor.visitInsn(LCMP);
	}
	
	public void fCmp() {
		if (debug)
			System.out.println("fCmp");
		
		visitor.visitInsn(FCMPL);
	}
	
	public void dCmp() {
		if (debug)
			System.out.println("dCmp");
		
		visitor.visitInsn(DCMPL);
	}
	
	public void instanceOf(String descriptor) {
		if (debug)
			System.out.println("instanceOf " + descriptor);
		
		visitor.visitTypeInsn(INSTANCEOF, descriptor);
	}
	
	public void instanceOf(Type type) {
		if (debug)
			System.out.println("instanceOf " + type.getDescriptor());
		
		visitor.visitTypeInsn(INSTANCEOF, type.getDescriptor());
	}
	
	public void invokeStatic(JavaMethod method) {
		visitor.visitMethodInsn(
				INVOKESTATIC,
				method.cls.internalName,
				method.name,
				method.descriptor,
				false);
	}
	
	public void invokeSpecial(String ownerInternalName, String name, String descriptor) {
		if (debug)
			System.out.println("invokeSpecial " + ownerInternalName + '.' + name + descriptor);
		
		visitor.visitMethodInsn(INVOKESPECIAL, ownerInternalName, name, descriptor, false);
	}
	
	public void invokeSpecial(Class owner, String name, String descriptor) {
		invokeSpecial(Type.getInternalName(owner), name, descriptor);
	}
	
	public void invokeSpecial(JavaMethod method) {
		invokeSpecial(method.cls.internalName, method.name, method.descriptor);
	}
	
	public void invokeVirtual(JavaMethod method) {
        if(method.kind == JavaMethod.Kind.INTERFACE) {
            invokeInterface(method);
            return;
        }
		if (debug)
			System.out.println("invokeVirtual " + method.cls.internalName + '.' + method.name + method.descriptor);
		
		visitor.visitMethodInsn(INVOKEVIRTUAL, method.cls.internalName, method.name, method.descriptor, false);
	}
	
	public void invokeInterface(JavaMethod method) {
		if (debug)
			System.out.println("invokeInterface " + method.cls.internalName + '.' + method.name + method.descriptor);
		
		visitor.visitMethodInsn(INVOKEINTERFACE, method.cls.internalName, method.name, method.descriptor, true);
	}
	
	public void newObject(String internalName) {
		if (debug)
			System.out.println("newObject " + internalName);
		
		visitor.visitTypeInsn(NEW, internalName);
	}
	
	public void newObject(JavaClass cls) {
		if (debug)
			System.out.println("newObject " + cls.internalName);
		
		visitor.visitTypeInsn(NEW, cls.internalName);
	}
	
	public void goTo(Label lbl) {
		if (debug)
			System.out.println("goTo " + getLabelName(lbl));
		
		visitor.visitJumpInsn(GOTO, lbl);
	}
	
	/**
	 * Jump if TOS == 0.
	 *
	 * @param lbl target label
	 */
	public void ifEQ(Label lbl) {
		if (debug)
			System.out.println("ifEQ " + getLabelName(lbl));
		
		visitor.visitJumpInsn(IFEQ, lbl);
	}
	
	public void ifNE(Label lbl) {
		if (debug)
			System.out.println("ifNE " + getLabelName(lbl));
		
		visitor.visitJumpInsn(IFNE, lbl);
	}
	
	public void ifLT(Label lbl) {
		if (debug)
			System.out.println("ifLT " + getLabelName(lbl));
		
		visitor.visitJumpInsn(IFLT, lbl);
	}
	
	public void ifGT(Label lbl) {
		if (debug)
			System.out.println("ifGT " + getLabelName(lbl));
		
		visitor.visitJumpInsn(IFGT, lbl);
	}
	
	public void ifGE(Label lbl) {
		if (debug)
			System.out.println("ifGE " + getLabelName(lbl));
		
		visitor.visitJumpInsn(IFGE, lbl);
	}
	
	public void ifLE(Label lbl) {
		if (debug)
			System.out.println("ifLE " + getLabelName(lbl));
		
		visitor.visitJumpInsn(IFLE, lbl);
	}
	
	public void ifICmpLE(Label lbl) {
		if (debug)
			System.out.println("ifICmpLE " + getLabelName(lbl));
		
		visitor.visitJumpInsn(IF_ICMPLE, lbl);
	}
	
	public void ifICmpGE(Label lbl) {
		if (debug)
			System.out.println("ifICmpGE " + getLabelName(lbl));
		
		visitor.visitJumpInsn(IF_ICMPGE, lbl);
	}
	
	public void ifICmpEQ(Label lbl) {
		if (debug)
			System.out.println("ifICmpEQ " + getLabelName(lbl));
		
		visitor.visitJumpInsn(IF_ICMPEQ, lbl);
	}
	
	public void ifICmpNE(Label lbl) {
		if (debug)
			System.out.println("ifICmpNE " + getLabelName(lbl));
		
		visitor.visitJumpInsn(IF_ICMPNE, lbl);
	}
	
	public void ifICmpGT(Label lbl) {
		if (debug)
			System.out.println("ifICmpGT " + getLabelName(lbl));
		
		visitor.visitJumpInsn(IF_ICMPGT, lbl);
	}
	
	public void ifICmpLT(Label lbl) {
		if (debug)
			System.out.println("ifICmpLT " + getLabelName(lbl));
		
		visitor.visitJumpInsn(IF_ICMPLT, lbl);
	}
	
	public void ifACmpEq(Label lbl) {
		if (debug)
			System.out.println("ifICmpEQ " + getLabelName(lbl));
		
		visitor.visitJumpInsn(IF_ACMPEQ, lbl);
	}
	
	public void ifACmpNe(Label lbl) {
		if (debug)
			System.out.println("ifACmpNE " + getLabelName(lbl));
		
		visitor.visitJumpInsn(IF_ACMPNE, lbl);
	}
	
	public void ifNull(Label lbl) {
		if (debug)
			System.out.println("ifNull " + getLabelName(lbl));
		
		visitor.visitJumpInsn(IFNULL, lbl);
	}
	
	public void ifNonNull(Label lbl) {
		if (debug)
			System.out.println("ifNonNull " + getLabelName(lbl));
		
		visitor.visitJumpInsn(IFNONNULL, lbl);
	}
	
	public void ret() {
		if (debug)
			System.out.println("ret");
		
		visitor.visitInsn(RETURN);
	}
	
	public void returnType(Type type) {
		if (debug)
			System.out.println("return " + type.getDescriptor());
		
		visitor.visitInsn(type.getOpcode(IRETURN));
	}
	
	public void returnInt() {
		if (debug)
			System.out.println("ireturn");
		
		visitor.visitInsn(IRETURN);
	}
	
	public void returnObject() {
		if (debug)
			System.out.println("areturn");
		
		visitor.visitInsn(ARETURN);
	}
	
	public void getField(String owner, String name, String descriptor) {
		if (debug)
			System.out.println("getField " + owner + '.' + name + ":" + descriptor);
		
		visitor.visitFieldInsn(GETFIELD, owner, name, descriptor);
	}
	
	public void getField(JavaField field) {
		if (debug)
			System.out.println("getField " + field.cls.internalName + '.' + field.name + ":" + field.descriptor);
		
		visitor.visitFieldInsn(GETFIELD, field.cls.internalName, field.name, field.descriptor);
	}
	
	public void putField(String owner, String name, String descriptor) {
		if (debug)
			System.out.println("putField " + owner + '.' + name + ":" + descriptor);
		
		visitor.visitFieldInsn(PUTFIELD, owner, name, descriptor);
	}
	
	public void putField(JavaField field) {
		if (debug)
			System.out.println("putField " + field.cls.internalName + '.' + field.name + ":" + field.descriptor);
		
		visitor.visitFieldInsn(PUTFIELD, field.cls.internalName, field.name, field.descriptor);
	}
	
	public void getStaticField(String owner, String name, String descriptor) {
		if (debug)
			System.out.println("getStatic " + owner + '.' + name + ":" + descriptor);
		
		visitor.visitFieldInsn(GETSTATIC, owner, name, descriptor);
	}
	
	public void getStaticField(JavaField field) {
		if (debug)
			System.out.println("getStaticField " + field.cls.internalName + '.' + field.name + ":" + field.descriptor);
		
		visitor.visitFieldInsn(GETSTATIC, field.cls.internalName, field.name, field.descriptor);
	}
	
	public void putStaticField(String owner, String name, String descriptor) {
		if (debug)
			System.out.println("putStatic " + owner + '.' + name + ":" + descriptor);
		
		visitor.visitFieldInsn(PUTSTATIC, owner, name, descriptor);
	}
	
	public void putStaticField(JavaField field) {
		if (debug)
			System.out.println("putStaticField " + field.cls.internalName + '.' + field.name + ":" + field.descriptor);
		
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
			System.out.println("swap");
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
			System.out.println("TryCatch " + getLabelName(start) + ", " + getLabelName(end) + ", " + getLabelName(handler) + ", TYPE: " + type);
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
			System.out.println("lookupSwitch");
			for (int i = 0; i < switchLabels.length; i++) {
				System.out.println("  " + keys[i] + " -> " + getLabelName(labels[i]));
			}
			System.out.println("  default -> " + getLabelName(defaultLabel));
		}
		
		visitor.visitLookupSwitchInsn(defaultLabel, keys, labels);
	}
	
	public void addVariableInfo(JavaLocalVariableInfo info) {
		localVariableInfos.add(info);
	}
}
