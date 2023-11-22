package org.openzen.zenscript.javabytecode.compiler;


import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.ArrayTypeID;
import org.openzen.zenscript.javabytecode.JavaBytecodeContext;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaNativeMethod;

import java.util.ArrayList;

class ArrayInitializerHelper {
	private static final JavaNativeMethod ARRAY_NEWINSTANCE = JavaNativeMethod.getNativeStatic(JavaClass.ARRAY, "newInstance", "(Ljava/lang/Class;I)Ljava/lang/Object;");

	private final JavaBytecodeContext context;

	public ArrayInitializerHelper(JavaBytecodeContext context) {
		this.context = context;
	}

	/**
	 * Creates an int[] with the given array size locations and writes the code that gets them in the generated file
	 * Uses the constructor arguments (sizes are expressions 0 .. dimension-1)
	 *
	 * @param dimension the array's dim
	 * @param arguments the arguments form the constructor
	 * @param visitor   the visitor visiting them
	 * @return the size locations
	 */
	static int[] getArraySizeLocationsFromConstructor(int dimension, Expression[] arguments, JavaExpressionVisitor visitor) {
		final ArrayList<Integer> list = new ArrayList<>();
		for (int i = 0; i < dimension; i++) {
			final int location = visitor.javaWriter.local(int.class);
			arguments[i].accept(visitor);
			visitor.javaWriter.storeInt(location);
			list.add(location);
		}
		int[] arraySizes = new int[list.size()];
		for (int i = 0; i < list.size(); i++) {
			arraySizes[i] = list.get(i);
		}
		return arraySizes;
	}

	/**
	 * Writes the code for visiting a multidimensional array with a default value.
	 * The variable at defaultLocation needs to be of or assignable to the resulting array element type.
	 *
	 * @param javaWriter       the writer that will write the actual opcode
	 * @param sizeLocations    the locations of the array dimension sizes sizeLocations.length == dim !!
	 * @param dim              the array's dimensions, reduced during the recursions of the loop to find the innermost loop
	 * @param currentArrayType The current type of the array, reduced during the recursions of the functions
	 * @param defaultLocation  The location of the default value. Needs to be of or assignable to elementType!
	 */
	void visitMultiDimArrayWithDefaultValue(JavaWriter javaWriter, int[] sizeLocations, int dim, ArrayHelperType currentArrayType, ArrayTypeID arrayType, int defaultLocation) {
		visitMultiDimArray(javaWriter, sizeLocations, new int[dim], dim, currentArrayType, arrayType, (elementType, counterLocations) -> javaWriter.load(elementType.getASMType(), defaultLocation));
	}

	/**
	 * The function that is actually setting up the loops and naming the counter variables.
	 * Private because the other static methods provide the new int[] sizeLocations.
	 * Recursively creates a forLoop per array dimension and in the innermost loop, applies the given function.
	 * That function needs to add one stack and may not touch the existing stacks.
	 *
	 * @param javaWriter        the writer that will write the actual opcode
	 * @param sizeLocations     the locations of the array dimension sizes sizeLocations.length == dim !!
	 * @param counterLocations  the locations of the for-Loop counter variables (will be filled by the function itself, counterLocations.length == dim !!
	 * @param dim               the array's dimensions, reduced during the recursions of the loop to find the innermost loop
	 * @param currentArrayType  The current type of the array, reduced during the recursions of the functions
	 * @param innermostFunction The function that will decide what to add to the array, needs to increase the stack size by one and may not touch the other stacks!
	 */
	void visitMultiDimArray(JavaWriter javaWriter, int[] sizeLocations, int[] counterLocations, int dim, ArrayHelperType currentArrayType, ArrayTypeID arrayType, InnermostFunction innermostFunction) {
		final Label begin = new Label();
		final Label end = new Label();
		javaWriter.label(begin);

		final int currentArraySizeLocation = sizeLocations[sizeLocations.length - dim];

		final ArrayHelperType elementType = currentArrayType.getWithOneDimensionLess();
		if (arrayType.elementType.isGeneric()) {
			arrayType.elementType.accept(javaWriter, new JavaTypeExpressionVisitor(context));
			javaWriter.loadInt(currentArraySizeLocation);
			javaWriter.invokeStatic(ARRAY_NEWINSTANCE);
			javaWriter.checkCast(context.getInternalName(arrayType));
		} else {
			javaWriter.loadInt(currentArraySizeLocation);
			elementType.newArray(javaWriter);
		}
		//javaWriter.dup();

		final int forLoopCounter = javaWriter.local(int.class);
		javaWriter.iConst0();
		javaWriter.storeInt(forLoopCounter);
		counterLocations[counterLocations.length - dim] = forLoopCounter;


		final Label loopStart = new Label();
		final Label loopEnd = new Label();

		javaWriter.label(loopStart);

		javaWriter.loadInt(forLoopCounter);
		javaWriter.loadInt(currentArraySizeLocation);
		javaWriter.ifICmpGE(loopEnd);

		//Loop content
		javaWriter.dup();
		javaWriter.loadInt(forLoopCounter);
		if (dim == 1) {
			innermostFunction.apply(elementType, counterLocations);
		} else {
			visitMultiDimArray(javaWriter, sizeLocations, counterLocations, dim - 1, elementType, arrayType, innermostFunction);
		}
		javaWriter.arrayStore(elementType.getASMType());

		//Return to the start
		javaWriter.iinc(forLoopCounter);
		javaWriter.goTo(loopStart);
		javaWriter.label(loopEnd);
		//javaWriter.pop();

		//Naming the variables
		javaWriter.nameVariable(forLoopCounter, "i" + dim, loopStart, loopEnd, Type.getType(int.class));

		javaWriter.label(end);

		for (int i = 0; i < sizeLocations.length; i++) {
			javaWriter.nameVariable(sizeLocations[i], "size" + (sizeLocations.length - i), begin, end, Type.getType(int.class));
		}
	}

	@FunctionalInterface
	public interface InnermostFunction {
		void apply(ArrayHelperType elementType, int[] counterLocations);
	}
}

