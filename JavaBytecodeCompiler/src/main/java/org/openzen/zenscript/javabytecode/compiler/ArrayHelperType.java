package org.openzen.zenscript.javabytecode.compiler;

import org.objectweb.asm.Type;
import org.openzen.zenscript.codemodel.type.ArrayTypeID;
import org.openzen.zenscript.codemodel.type.GenericTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.javabytecode.JavaBytecodeContext;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaNativeMethod;

public class ArrayHelperType {

	private final TypeID elementType;
	private final JavaBytecodeContext context;

	public ArrayHelperType(TypeID elementType, JavaBytecodeContext context) {
		this.elementType = elementType;
		this.context = context;
	}


	public ArrayHelperType getWithOneDimensionLess() {
		ArrayTypeID arrayTypeID = ((ArrayTypeID) elementType);
		return new ArrayHelperType(arrayTypeID.removeOneDimension(), context);
	}

	public void newArray(JavaWriter javaWriter) {
		if(elementType instanceof GenericTypeID) {

			elementType.accept(javaWriter, new JavaTypeExpressionVisitor(context, false));
			javaWriter.swap();
			final JavaClass arrayClass = JavaClass.fromInternalName("java/lang/reflect/Array", JavaClass.Kind.CLASS);
			javaWriter.invokeStatic(JavaNativeMethod.getStatic(arrayClass, "newInstance", "(Ljava/lang/Class;I)Ljava/lang/Object;", 0));
			javaWriter.checkCast("[Ljava/lang/Object;");
		} else {
			javaWriter.newArray(context.getType(elementType));
		}
	}

	public Type getASMElementType() {
		return context.getType(elementType);
	}
}
