package org.openzen.zenscript.javabytecode.compiler;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.openzen.zenscript.codemodel.CompareType;
import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.member.DefinitionMember;
import org.openzen.zenscript.codemodel.statement.ReturnStatement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.implementations.IntRange;
import org.openzen.zenscript.javabytecode.*;
import org.openzen.zenscript.shared.CompileException;
import org.openzen.zenscript.shared.CompileExceptionCode;

import java.util.Arrays;
import java.util.Map;
import java.util.StringJoiner;

public class JavaExpressionVisitor implements ExpressionVisitor<Void> {
    private static final JavaMethodInfo MAP_PUT = JavaMethodInfo.get(Opcodes.ACC_PUBLIC, Map.class, "put", Object.class, Object.class, Object.class);

    private final JavaWriter javaWriter;

    public JavaExpressionVisitor(JavaWriter javaWriter) {
        this.javaWriter = javaWriter;
    }

    private static Class<?> getForEquals(ITypeID id) {
        if (CompilerUtils.isPrimitive(id))
            return id.accept(JavaTypeClassVisitor.INSTANCE);
        return Object.class;
    }

    @Override
    public Void visitAndAnd(AndAndExpression expression) {
        Label end = new Label();
        Label onFalse = new Label();

        expression.left.accept(this);

        javaWriter.ifEQ(onFalse);
        expression.right.accept(this);

        // //these two calls are redundant but make decompiled code look better. Keep?
        // javaWriter.ifEQ(onFalse);
        // javaWriter.iConst1();

        javaWriter.goTo(end);

        javaWriter.label(onFalse);
        javaWriter.iConst0();


        javaWriter.label(end);

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
        expression.left.accept(this);
        expression.right.accept(this);
        final Type operatorType = Type.getType(CompareType.class);
        javaWriter.getStaticField(operatorType.getInternalName(), expression.operator.name(), operatorType.getDescriptor());

        // TODO: should be implemented properly
        JavaMethodInfo compareMethod = JavaMethodInfo.get(
                0,
                ZenUtils.class,
                "compare",
                boolean.class,
                getForEquals(expression.left.type),
                getForEquals(expression.right.type),
                CompareType.class);
        javaWriter.invokeStatic(compareMethod);

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
        for (Expression argument : expression.arguments.arguments) {
            argument.accept(this);
        }
        //TODO: Test with actual static method
        final JavaMethodInfo info = expression.member.getTag(JavaMethodInfo.class);
        javaWriter.invokeStatic(info);
        return null;
    }

    @Override
    public Void visitCapturedClosure(CapturedClosureExpression expression) {
        expression.value.accept(this);
        return null;
    }

    @Override
    public Void visitCapturedDirect(CapturedDirectExpression expression) {
        return null;
    }

    @Override
    public Void visitCapturedLocalVariable(CapturedLocalVariableExpression expression) {
        new GetLocalVariableExpression(expression.position, expression.variable).accept(this);
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
        final Label end = new Label();
        expression.value.accept(this);
        javaWriter.dup();
        javaWriter.ifNonNull(end);
        javaWriter.pop();
        javaWriter.newObject(NullPointerException.class);
        javaWriter.dup();
        javaWriter.constant("Tried to convert a null value to nonnull type " + expression.type.accept(JavaTypeClassVisitor.INSTANCE).getSimpleName());
        javaWriter.invokeSpecial(NullPointerException.class, "<init>", "(Ljava/lang/String;)V");
        javaWriter.aThrow();
        javaWriter.label(end);

        return null;
    }

    @Override
    public Void visitCoalesce(CoalesceExpression expression) {
        final Label end = new Label();
        expression.left.accept(this);
        javaWriter.dup();
        javaWriter.ifNonNull(end);
        javaWriter.pop();
        expression.right.accept(this);
        javaWriter.label(end);
        return null;
    }

    @Override
    public Void visitConditional(ConditionalExpression expression) {
        final Label end = new Label();
        final Label onElse = new Label();
        expression.condition.accept(this);
        javaWriter.ifEQ(onElse);
        expression.ifThen.accept(this);
        javaWriter.goTo(end);
        javaWriter.label(onElse);
        expression.ifElse.accept(this);
        javaWriter.label(end);
        return null;
    }

    @Override
    public Void visitConstantBool(ConstantBoolExpression expression) {
        if (expression.value)
            javaWriter.iConst1();
        else
            javaWriter.iConst0();
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

        if (javaWriter.method.javaClass.isEnum) {
            javaWriter.loadObject(0);
            javaWriter.loadObject(1);
            javaWriter.loadInt(2);
        } else {
            javaWriter.loadObject(0);
        }

        for (Expression argument : expression.arguments.arguments) {
            argument.accept(this);
        }
        javaWriter.invokeSpecial(type.getInternalName(), "<init>", CompilerUtils.calcDesc(expression.constructor.header, javaWriter.method.javaClass.isEnum));
        return null;
    }

    @Override
    public Void visitConstructorSuperCall(ConstructorSuperCallExpression expression) {
        javaWriter.loadObject(0);
        for (Expression argument : expression.arguments.arguments) {
            argument.accept(this);
        }
        //No super calls in enums possible, and that's already handled in the enum constructor itself.
        javaWriter.invokeSpecial(expression.objectType.accept(JavaTypeClassVisitor.INSTANCE), "<init>", CompilerUtils.calcDesc(expression.constructor.header, false));

        CompilerUtils.writeDefaultFieldInitializers(javaWriter, javaWriter.forDefinition, false);
        return null;
    }

    @Override
    public Void visitEnumConstant(EnumConstantExpression expression) {
        final Type type = expression.type.accept(JavaTypeVisitor.INSTANCE);
        javaWriter.getStaticField(type.getInternalName(), expression.value.name, type.getDescriptor());
        return null;
    }

    @Override
    public Void visitFunction(FunctionExpression expression) {
        if (expression.header.parameters.length == 0 && expression.body instanceof ReturnStatement && expression.body.hasTag(MatchExpression.class) && expression.closure.captures.isEmpty()) {
            ((ReturnStatement) expression.body).value.accept(this);
            return null;
        }
        final String signature = calcFunctionSignature(expression.closure, expression.header.returnType);
        final String name = "lambda" + expression.hashCode();

        final JavaMethodInfo methodInfo = new JavaMethodInfo(javaWriter.method.javaClass, name, signature, Opcodes.ACC_STATIC | Opcodes.ACC_PRIVATE);
        JavaWriter functionWriter = new JavaWriter(javaWriter.clazzVisitor, methodInfo, null, signature, null);

        for (CapturedExpression capture : expression.closure.captures) {
            capture.accept(new JavaCapturedExpressionVisitor(this));
        }

        javaWriter.invokeStatic(methodInfo);

        functionWriter.start();
        expression.body.accept(new JavaStatementVisitor(new JavaExpressionVisitor(functionWriter) {
            @Override
            public Void visitGetLocalVariable(GetLocalVariableExpression varExpression) {
                final int position = calculateMemberPosition(varExpression, expression);
                if (position < 0)
                    throw new CompileException(varExpression.position, CompileExceptionCode.INTERNAL_ERROR, "Captured Statement error");
                functionWriter.load(varExpression.variable.type.accept(JavaTypeVisitor.INSTANCE), position);
                return null;
            }
        }));


        functionWriter.ret();
        functionWriter.end();

        return null;
    }

    //TODO replace with visitor?
    private static int calculateMemberPosition(GetLocalVariableExpression localVariableExpression, FunctionExpression expression) {
        int h = expression.header.parameters.length;
        for (CapturedExpression capture : expression.closure.captures) {
            if (capture instanceof CapturedLocalVariableExpression && ((CapturedLocalVariableExpression) capture).variable == localVariableExpression.variable)
                return h;
            if (capture instanceof CapturedClosureExpression && ((CapturedClosureExpression) capture).value instanceof CapturedLocalVariableExpression && ((CapturedLocalVariableExpression) ((CapturedClosureExpression) capture).value).variable == localVariableExpression.variable)
                return h;
            h++;
        }
        return -1;
    }

    private String calcFunctionSignature(LambdaClosure closure, ITypeID type) {
        StringJoiner joiner = new StringJoiner("", "(", ")");

        for (CapturedExpression capture : closure.captures) {
            String descriptor = capture.type.accept(JavaTypeVisitor.INSTANCE).getDescriptor();
            joiner.add(descriptor);
        }
        return joiner.toString() + type.accept(JavaTypeVisitor.INSTANCE).getDescriptor();
    }

    @Override
    public Void visitGenericCompare(GenericCompareExpression expression) {
        //TODO: What am I supposed to do here?
        return null;
    }

    @Override
    public Void visitGetField(GetFieldExpression expression) {
        expression.accept(this);
        if (!checkAndGetFieldInfo(expression.field, false))
            throw new IllegalStateException("Missing field info on a field member!");
        return null;
    }

    @Override
    public Void visitGetFunctionParameter(GetFunctionParameterExpression expression) {
        JavaParameterInfo parameter = expression.parameter.getTag(JavaParameterInfo.class);
        javaWriter.load(Type.getType(expression.parameter.type.accept(JavaTypeClassVisitor.INSTANCE)), parameter.index);
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
    public Void visitGlobal(GlobalExpression expression) {
        return expression.resolution.accept(this);
    }

    @Override
    public Void visitGlobalCall(GlobalCallExpression expression) {
        return expression.resolution.accept(this);
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
        //TODO: What am I supposed to do here?
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
            javaWriter.invokeInterface(MAP_PUT);
            javaWriter.pop();
        }
        return null;
    }

    @Override
    public Void visitMatch(MatchExpression expression) {

        final Label start = new Label();
        final Label end = new Label();


        javaWriter.label(start);
        expression.value.accept(this);
        if (expression.value.type == BasicTypeID.STRING)
            javaWriter.invokeVirtual(new JavaMethodInfo(new JavaClassInfo("java/lang/Object"), "hashCode", "()I", 0));

        final boolean hasNoDefault = hasNoDefault(expression);

        final MatchExpression.Case[] cases = expression.cases;
        final JavaSwitchLabel[] switchLabels = new JavaSwitchLabel[hasNoDefault ? cases.length : cases.length - 1];
        final Label defaultLabel = new Label();

        int i = 0;
        for (final MatchExpression.Case matchCase : cases) {
            if (matchCase.key != null) {
                switchLabels[i++] = new JavaSwitchLabel(CompilerUtils.getKeyForSwitch(matchCase.key), new Label());
            }
        }

        JavaSwitchLabel[] sortedSwitchLabels = Arrays.copyOf(switchLabels, switchLabels.length);
        Arrays.sort(sortedSwitchLabels, (a, b) -> a.key - b.key);

        javaWriter.lookupSwitch(defaultLabel, sortedSwitchLabels);

        i = 0;
        for (final MatchExpression.Case switchCase : cases) {
            if (hasNoDefault || switchCase.key != null) {
                javaWriter.label(switchLabels[i++].label);
            } else {
                javaWriter.label(defaultLabel);
            }
            switchCase.value.body.setTag(MatchExpression.class, expression);
            switchCase.value.accept(this);
            javaWriter.goTo(end);
        }

        if (hasNoDefault) {
            javaWriter.label(defaultLabel);
        }

        javaWriter.label(end);


        //throw new UnsupportedOperationException("Not yet implemented!");
        return null;
    }

    private boolean hasNoDefault(MatchExpression switchStatement) {
        for (MatchExpression.Case switchCase : switchStatement.cases)
            if (switchCase.key == null) return false;
        return true;
    }

    @Override
    public Void visitNew(NewExpression expression) {
        final String type;
        if (expression.type instanceof DefinitionTypeID)
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
        Label end = new Label();
        Label onTrue = new Label();

        expression.left.accept(this);

        javaWriter.ifNE(onTrue);
        expression.right.accept(this);

        // //these two calls are redundant but make decompiled code look better. Keep?
        // javaWriter.ifNE(onTrue);
        // javaWriter.iConst0();

        javaWriter.goTo(end);

        javaWriter.label(onTrue);
        javaWriter.iConst1();


        javaWriter.label(end);

        return null;
    }

    @Override
    public Void visitPostCall(PostCallExpression expression) {
        expression.target.accept(this);
        javaWriter.dup(expression.type.accept(new JavaTypeVisitor()));
        if (!checkAndExecuteByteCodeImplementation(expression.member) && !checkAndExecuteMethodInfo(expression.member))
            throw new IllegalStateException("Call target has no method info!");

        return null;
    }

    @Override
    public Void visitRange(RangeExpression expression) {
        // TODO: there are other kinds of ranges also; there should be a Range<T, T> type with creation of synthetic types
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
    public Void visitSameObject(SameObjectExpression expression) {
        expression.left.accept(this);
        expression.right.accept(this);

        Label end = new Label();
        Label equal = new Label();

        if (expression.inverted)
            javaWriter.ifACmpNe(equal);
        else
            javaWriter.ifACmpEq(equal);
        javaWriter.iConst0();
        javaWriter.goTo(end);
        javaWriter.label(equal);
        javaWriter.iConst1();
        javaWriter.label(end);
        return null;
    }

    @Override
    public Void visitSetField(SetFieldExpression expression) {
        expression.target.accept(this);
        expression.value.accept(this);
        if (!checkAndPutFieldInfo(expression.field, false))
            throw new IllegalStateException("Missing field info on a field member!");
        return null;
    }

    @Override
    public Void visitSetFunctionParameter(SetFunctionParameterExpression expression) {
        expression.value.accept(this);
        JavaParameterInfo parameter = expression.parameter.getTag(JavaParameterInfo.class);
        javaWriter.store(expression.type.accept(JavaTypeVisitor.INSTANCE), parameter.index);
        return null;
    }

    @Override
    public Void visitSetLocalVariable(SetLocalVariableExpression expression) {
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
    public Void visitSupertypeCast(SupertypeCastExpression expression) {
        return null; // nothing to do
    }

    @Override
    public Void visitThis(ThisExpression expression) {
        javaWriter.loadObject(0);
        return null;
    }

    @Override
    public Void visitTryConvert(TryConvertExpression expression) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Void visitTryRethrowAsException(TryRethrowAsExceptionExpression expression) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Void visitTryRethrowAsResult(TryRethrowAsResultExpression expression) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Void visitVariantValue(VariantValueExpression expression) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Void visitWrapOptional(WrapOptionalExpression expression) {
        // TODO: convert basic types (char, int, float, ...) to their boxed (Character, Integer, Float, ...) counterparts
        // -- any object type values can just be passed as-is
        expression.value.accept(this);
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
        if (methodInfo.isStatic()) {
            getJavaWriter().invokeStatic(methodInfo);
        } else {
            getJavaWriter().invokeVirtual(methodInfo);
        }
        return true;
    }


    //Will return true if a JavaFieldInfo.class tag exists, and will compile that tag
    public boolean checkAndPutFieldInfo(DefinitionMember field, boolean isStatic) {
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

    public boolean checkAndGetFieldInfo(DefinitionMember field, boolean isStatic) {
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
