package org.openzen.zenscript.javabytecode.compiler;

import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.member.DefinitionMember;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.implementations.IntRange;
import org.openzen.zenscript.javabytecode.*;
import org.openzen.zenscript.shared.CompileException;
import org.openzen.zenscript.shared.CompileExceptionCode;


import java.util.Map;

public class JavaExpressionVisitor implements ExpressionVisitor<Void> {
    private final JavaWriter javaWriter;
    private final boolean isInit;


    public JavaExpressionVisitor(final JavaWriter javaWriter) {
        this(javaWriter, false);
    }

    public JavaExpressionVisitor(JavaWriter javaWriter, boolean isInit) {
        this.javaWriter = javaWriter;
        this.isInit = isInit;
    }

    @Override
    public Void visitAndAnd(AndAndExpression expression) {
        return null;
    }

    @Override
    public Void visitArray(ArrayExpression expression) {
        javaWriter.constant(expression.expressions.length);
        Type type = Type.getType(expression.type.accept(JavaTypeClassVisitor.INSTANCE).getComponentType());
        javaWriter.newArray(type);
        for (int i = 0; i < expression.expressions.length; i++) {
            javaWriter.dup();
            javaWriter.constant(i);
            expression.expressions[i].accept(this);
            javaWriter.arrayStore(type);
        }
        return null;
    }

    @Override
    public Void visitCompare(BasicCompareExpression expression) {
        return null;
    }

    @Override
    public Void visitCall(CallExpression expression) {
        expression.target.accept(this);
        for (Expression argument : expression.arguments.arguments) {
            argument.accept(this);
        }
        if (!checkAndExecuteByteCodeImplementation(expression.member) && !checkAndExecuteMethodInfo(expression.member))
            throw new IllegalStateException("Call target has no method info!");

        return null;
    }

    @Override
    public Void visitCallStatic(CallStaticExpression expression) {
        return null;
    }

    @Override
    public Void visitCapturedClosure(CapturedClosureExpression expression) {
        return null;
    }

    @Override
    public Void visitCapturedDirect(CapturedDirectExpression expression) {
        return null;
    }

    @Override
    public Void visitCapturedLocalVariable(CapturedLocalVariableExpression expression) {
        return null;
    }

    @Override
    public Void visitCapturedParameter(CapturedParameterExpression expression) {

        return null;
    }

    @Override
    public Void visitCapturedThis(CapturedThisExpression expression) {
        return null;
    }
	
	@Override
    public Void visitCast(CastExpression expression) {
		expression.target.accept(this);
        if (!checkAndExecuteByteCodeImplementation(expression.member) && !checkAndExecuteMethodInfo(expression.member))
            throw new IllegalStateException("Call target has no method info!");
		
        return null;
	}

    @Override
    public Void visitCheckNull(CheckNullExpression expression) {
        return null;
    }

    @Override
    public Void visitCoalesce(CoalesceExpression expression) {
        return null;
    }

    @Override
    public Void visitConditional(ConditionalExpression expression) {
        return null;
    }

    @Override
    public Void visitConstantBool(ConstantBoolExpression expression) {
        getJavaWriter().constant(expression.value);
        return null;
    }

    @Override
    public Void visitConstantByte(ConstantByteExpression expression) {
        getJavaWriter().biPush(expression.value);
        return null;
    }

    @Override
    public Void visitConstantChar(ConstantCharExpression expression) {
        getJavaWriter().constant(expression.value);
        return null;
    }

    @Override
    public Void visitConstantDouble(ConstantDoubleExpression expression) {
        getJavaWriter().constant(expression.value);
        return null;
    }

    @Override
    public Void visitConstantFloat(ConstantFloatExpression expression) {
        getJavaWriter().constant(expression.value);
        return null;
    }

    @Override
    public Void visitConstantInt(ConstantIntExpression expression) {
        getJavaWriter().constant(expression.value);
        return null;
    }

    @Override
    public Void visitConstantLong(ConstantLongExpression expression) {
        getJavaWriter().constant(expression.value);
        return null;
    }

    @Override
    public Void visitConstantSByte(ConstantSByteExpression expression) {
        getJavaWriter().constant(expression.value);
        return null;
    }

    @Override
    public Void visitConstantShort(ConstantShortExpression expression) {
        getJavaWriter().siPush(expression.value);
        return null;
    }

    @Override
    public Void visitConstantString(ConstantStringExpression expression) {
        getJavaWriter().constant(expression.value);
        return null;
    }

    @Override
    public Void visitConstantUInt(ConstantUIntExpression expression) {
        getJavaWriter().constant(expression.value);
        return null;
    }

    @Override
    public Void visitConstantULong(ConstantULongExpression expression) {
        getJavaWriter().constant(expression.value);
        return null;
    }

    @Override
    public Void visitConstantUShort(ConstantUShortExpression expression) {
        getJavaWriter().constant(expression.value);
        return null;
    }

    @Override
    public Void visitConstructorThisCall(ConstructorThisCallExpression expression) {
        Type type = expression.objectType.accept(JavaTypeVisitor.INSTANCE);
        //javaWriter.loadObject(0);
        for (Expression argument : expression.arguments.arguments) {
            argument.accept(this);
        }
        javaWriter.invokeSpecial(type.getInternalName(), "<init>", CompilerUtils.calcDesc(expression.constructor.header, expression.constructor.hasTag(JavaEnumInfo.class)));
        return null;
    }

    @Override
    public Void visitConstructorSuperCall(ConstructorSuperCallExpression expression) {
        return null;
    }

    @Override
    public Void visitEnumConstant(EnumConstantExpression expression) {
        return null;
    }

    @Override
    public Void visitFunction(FunctionExpression expression) {
        return null;
    }

    @Override
    public Void visitGenericCompare(GenericCompareExpression expression) {
        return null;
    }

    @Override
    public Void visitGetField(GetFieldExpression expression) {
        javaWriter.loadObject(0);
        if (!checkAndGetFieldInfo(expression.field, false))
            throw new IllegalStateException("Missing field info on a field member!");
        return null;
    }

    @Override
    public Void visitGetFunctionParameter(GetFunctionParameterExpression expression) {
        javaWriter.load(Type.getType(expression.parameter.type.accept(JavaTypeClassVisitor.INSTANCE)), expression.parameter.index + 1);
        return null;
    }

    @Override
    public Void visitGetLocalVariable(GetLocalVariableExpression expression) {
        final Label label = new Label();
        final JavaLocalVariableInfo tag = expression.variable.getTag(JavaLocalVariableInfo.class);
        tag.end = label;
        javaWriter.load(tag.type, tag.local);
        javaWriter.label(label);
        return null;
    }

    @Override
    public Void visitGetStaticField(GetStaticFieldExpression expression) {
        if (!checkAndGetFieldInfo(expression.field, true))
            throw new IllegalStateException("Missing field info on a field member!");
        return null;
    }

    @Override
    public Void visitGetter(GetterExpression expression) {
        return null;
    }

    @Override
    public Void visitInterfaceCast(InterfaceCastExpression expression) {
        expression.value.accept(this);
        javaWriter.checkCast(expression.type.accept(JavaTypeClassVisitor.INSTANCE));
        return null;
    }

    @Override
    public Void visitIs(IsExpression expression) {
        expression.value.accept(this);
        javaWriter.instanceOf(Type.getDescriptor(expression.isType.accept(JavaTypeClassVisitor.INSTANCE)));
        return null;
    }

    @Override
    public Void visitMakeConst(MakeConstExpression expression) {

        return null;
    }

    @Override
    public Void visitMap(MapExpression expression) {
        javaWriter.newObject(expression.type.accept(JavaTypeClassVisitor.INSTANCE));
        javaWriter.dup();
        javaWriter.invokeSpecial("java/util/Map", "<init>", "()V");
        for (int i = 0; i < expression.keys.length; i++) {
            javaWriter.dup();
            expression.keys[i].accept(this);
            expression.values[i].accept(this);
            javaWriter.invokeInterface(Map.class, "put", Object.class, Object.class, Object.class);
            javaWriter.pop();
        }
        return null;
    }

    @Override
    public Void visitNew(NewExpression expression) {
        final String type;
        if(expression.type instanceof DefinitionTypeID)
            type = ((DefinitionTypeID) expression.type).definition.name;
        else
            type = Type.getDescriptor(expression.type.accept(JavaTypeClassVisitor.INSTANCE));

        javaWriter.newObject(type);
        javaWriter.dup();
        StringBuilder signatureBuilder = new StringBuilder("(");
        for (Expression argument : expression.arguments.arguments) {
            argument.accept(this);
            signatureBuilder.append(Type.getDescriptor(argument.type.accept(JavaTypeClassVisitor.INSTANCE)));
        }
        signatureBuilder.append(")V");
        javaWriter.invokeSpecial(type, "<init>", signatureBuilder.toString());

        return null;
    }

    @Override
    public Void visitNull(NullExpression expression) {
        javaWriter.aConstNull();
        return null;
    }

    @Override
    public Void visitOrOr(OrOrExpression expression) {
        return null;
    }

    @Override
    public Void visitRange(RangeExpression expression) {
        if (expression.from.type.accept(JavaTypeClassVisitor.INSTANCE) != int.class)
            throw new CompileException(expression.position, CompileExceptionCode.INTERNAL_ERROR, "Only integer ranges supported");
        javaWriter.newObject(IntRange.class);
        javaWriter.dup();
        expression.from.accept(this);
        expression.to.accept(this);
        System.out.println(IntRange.class.getName());
        javaWriter.invokeSpecial("org/openzen/zenscript/implementations/IntRange", "<init>", "(II)V");

        return null;
    }

    @Override
    public Void visitSetField(SetFieldExpression expression) {
        javaWriter.loadObject(0);
        if (expression.field.isFinal() && !isInit)
            throw new CompileException(expression.position, CompileExceptionCode.CANNOT_SET_FINAL_VARIABLE, "Cannot set a final field!");
        expression.value.accept(this);
        if (!checkAndPutFieldInfo(expression.field, false))
            throw new IllegalStateException("Missing field info on a field member!");
        return null;
    }

    @Override
    public Void visitSetFunctionParameter(SetFunctionParameterExpression expression) {
        return null;
    }

    @Override
    public Void visitSetLocalVariable(SetLocalVariableExpression expression) {
        if (expression.variable.isFinal)
            throw new CompileException(expression.position, CompileExceptionCode.CANNOT_SET_FINAL_VARIABLE, "Cannot set a final variable!");
        expression.value.accept(this);
        Label label = new Label();
        javaWriter.label(label);
        final JavaLocalVariableInfo tag = expression.variable.getTag(JavaLocalVariableInfo.class);
        tag.end = label;

        javaWriter.store(tag.type, tag.local);

        return null;
    }

    @Override
    public Void visitSetStaticField(SetStaticFieldExpression expression) {
        if (expression.field.isFinal())
            throw new CompileException(expression.position, CompileExceptionCode.CANNOT_SET_FINAL_VARIABLE, "Cannot set a final field!");
        expression.value.accept(this);
        if (!checkAndPutFieldInfo(expression.field, true))
            throw new IllegalStateException("Missing field info on a field member!");
        return null;
    }

    @Override
    public Void visitSetter(SetterExpression expression) {
        return null;
    }

    @Override
    public Void visitStaticGetter(StaticGetterExpression expression) {
        return null;
    }

    @Override
    public Void visitStaticSetter(StaticSetterExpression expression) {
        return null;
    }

    @Override
    public Void visitThis(ThisExpression expression) {
        javaWriter.loadObject(0);
        return null;
    }

    @Override
    public Void visitWrapOptional(WrapOptionalExpression expression) {
        return null;
    }

    public JavaWriter getJavaWriter() {
        return javaWriter;
    }


    //Will return true if a JavaBytecodeImplementation.class tag exists, and will compile that tag
    private boolean checkAndExecuteByteCodeImplementation(DefinitionMember member) {
        JavaBytecodeImplementation implementation = member.getTag(JavaBytecodeImplementation.class);
        if (implementation != null) {
            implementation.compile(getJavaWriter());
            return true;
        }
        return false;
    }

    //Will return true if a JavaMethodInfo.class tag exists, and will compile that tag
    private boolean checkAndExecuteMethodInfo(DefinitionMember member) {
        JavaMethodInfo methodInfo = member.getTag(JavaMethodInfo.class);
        if (methodInfo == null)
            return false;
        if (methodInfo.isStatic) {
            getJavaWriter().invokeStatic(
                    methodInfo.javaClass.internalClassName,
                    methodInfo.name,
                    methodInfo.signature);
        } else {
            getJavaWriter().invokeVirtual(
                    methodInfo.javaClass.internalClassName,
                    methodInfo.name,
                    methodInfo.signature);
        }
        return true;
    }


    //Will return true if a JavaFieldInfo.class tag exists, and will compile that tag
    private boolean checkAndPutFieldInfo(DefinitionMember field, boolean isStatic) {
        JavaFieldInfo fieldInfo = field.getTag(JavaFieldInfo.class);
        if (fieldInfo == null)
            return false;
        //TODO Remove isStatic
        if (field.isStatic() || isStatic) {
            getJavaWriter().putStaticField(fieldInfo.javaClass.internalClassName, fieldInfo.name, fieldInfo.signature);
        } else {
            getJavaWriter().putField(fieldInfo.javaClass.internalClassName, fieldInfo.name, fieldInfo.signature);
        }
        return true;
    }

    private boolean checkAndGetFieldInfo(DefinitionMember field, boolean isStatic) {
        JavaFieldInfo fieldInfo = field.getTag(JavaFieldInfo.class);
        if (fieldInfo == null)
            return false;
        //TODO Remove isStatic
        if (field.isStatic() || isStatic) {
            getJavaWriter().getStaticField(fieldInfo.javaClass.internalClassName, fieldInfo.name, fieldInfo.signature);
        } else {
            getJavaWriter().getField(fieldInfo.javaClass.internalClassName, fieldInfo.name, fieldInfo.signature);
        }
        return true;
    }
}
