package org.openzen.zenscript.javabytecode.compiler;

import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.statement.StatementVisitor;

public class JavaExpressionVisitor implements ExpressionVisitor<Void> {

    final StatementVisitor<?> statementVisitor;
    private final JavaWriter javaWriter;

    public JavaExpressionVisitor(final JavaWriter javaWriter, StatementVisitor<?> statementVisitor) {
        this.javaWriter = javaWriter;
        this.statementVisitor = statementVisitor;
    }

    @Override
    public Void visitAndAnd(AndAndExpression expression) {
        return null;
    }

    @Override
    public Void visitArray(ArrayExpression expression) {
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
        expression.member.accept(new JavaMemberVisitor(javaWriter, this));

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
        javaWriter.constant(expression.value);
        return null;
    }

    @Override
    public Void visitConstantByte(ConstantByteExpression expression) {
        javaWriter.biPush(expression.value);
        return null;
    }

    @Override
    public Void visitConstantChar(ConstantCharExpression expression) {
        javaWriter.constant(expression.value);
        return null;
    }

    @Override
    public Void visitConstantDouble(ConstantDoubleExpression expression) {
        javaWriter.constant(expression.value);
        return null;
    }

    @Override
    public Void visitConstantFloat(ConstantFloatExpression expression) {
        javaWriter.constant(expression.value);
        return null;
    }

    @Override
    public Void visitConstantInt(ConstantIntExpression expression) {
        javaWriter.constant(expression.value);
        return null;
    }

    @Override
    public Void visitConstantLong(ConstantLongExpression expression) {
        javaWriter.constant(expression.value);
        return null;
    }

    @Override
    public Void visitConstantSByte(ConstantSByteExpression expression) {
        javaWriter.constant(expression.value);
        return null;
    }

    @Override
    public Void visitConstantShort(ConstantShortExpression expression) {
        javaWriter.siPush(expression.value);
        return null;
    }

    @Override
    public Void visitConstantString(ConstantStringExpression expression) {
        javaWriter.constant(expression.value);
        return null;
    }

    @Override
    public Void visitConstantUInt(ConstantUIntExpression expression) {
        javaWriter.constant(expression.value);
        return null;
    }

    @Override
    public Void visitConstantULong(ConstantULongExpression expression) {
        javaWriter.constant(expression.value);
        return null;
    }

    @Override
    public Void visitConstantUShort(ConstantUShortExpression expression) {
        javaWriter.constant(expression.value);
        return null;
    }

    @Override
    public Void visitConstructorCall(ConstructorCallExpression expression) {
        return null;
    }

    @Override
    public Void visitEnumConstant(EnumConstantExpression expression) {
        return null;
    }

    @Override
    public Void visitEquals(EqualsExpression expression) {
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
        return null;
    }

    @Override
    public Void visitGetFunctionParameter(GetFunctionParameterExpression expression) {
        return null;
    }

    @Override
    public Void visitGetLocalVariable(GetLocalVariableExpression expression) {
        return null;
    }

    @Override
    public Void visitGetStaticField(GetStaticFieldExpression expression) {
        expression.field.accept(new JavaMemberVisitor(javaWriter, this));
        return null;
    }

    @Override
    public Void visitGetter(GetterExpression expression) {
        return null;
    }

    @Override
    public Void visitInterfaceCast(InterfaceCastExpression expression) {
        return null;
    }

    @Override
    public Void visitIs(IsExpression expression) {
        return null;
    }

    @Override
    public Void visitMakeConst(MakeConstExpression expression) {
        return null;
    }

    @Override
    public Void visitMap(MapExpression expression) {
        return null;
    }

    @Override
    public Void visitNew(NewExpression expression) {
        return null;
    }

    @Override
    public Void visitNot(NotExpression expression) {
        return null;
    }

    @Override
    public Void visitNull(NullExpression expression) {
        return null;
    }

    @Override
    public Void visitOrOr(OrOrExpression expression) {
        return null;
    }

    @Override
    public Void visitRange(RangeExpression expression) {
        return null;
    }

    @Override
    public Void visitSetField(SetFieldExpression expression) {
        return null;
    }

    @Override
    public Void visitSetFunctionParameter(SetFunctionParameterExpression expression) {
        return null;
    }

    @Override
    public Void visitSetLocalVariable(SetLocalVariableExpression expression) {
        return null;
    }

    @Override
    public Void visitSetStaticField(SetStaticFieldExpression expression) {
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
    public Void visitStringConcat(StringConcatExpression expression) {
        return null;
    }

    @Override
    public Void visitSubstring(SubstringExpression expression) {
        return null;
    }

    @Override
    public Void visitThis(ThisExpression expression) {
        return null;
    }

    @Override
    public Void visitWrapOptional(WrapOptionalExpression expression) {
        return null;
    }
}
