package org.openzen.zenscript.javabytecode.compiler;


import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.FunctionExpression;
import org.openzen.zenscript.codemodel.statement.ReturnStatement;

import java.util.ArrayList;

class ArrayInitializerHelper {

	/**
	 * creates an int[] with the given array size locations and writes the code that gets them in the generated file.
	 * Uses an already present origin array (rectangular!)
	 *
	 * @param dimension           the array's dim
	 * @param originArrayType     the type of the original array
	 * @param originArrayLocation the location of the original array
	 * @param javaWriter          the writer
	 * @return the size locations
	 */
	static int[] getArraySizeLocationsProjected(int dimension, Type originArrayType, int originArrayLocation, JavaWriter javaWriter) {
		int[] arraySizes;
		final ArrayList<Integer> list = new ArrayList<>();
		javaWriter.loadObject(originArrayLocation);
		Type currentElementType = originArrayType;
		for (int i = 0; i < dimension; i++) {
			currentElementType = Type.getType(currentElementType.getDescriptor().substring(1));
			final int location = javaWriter.local(int.class);
			javaWriter.dup();
			javaWriter.arrayLength();
			javaWriter.storeInt(location);
			list.add(location);

			if (i < dimension - 1) {
				javaWriter.iConst0();
				javaWriter.arrayLoad(currentElementType);
			}
		}
		javaWriter.pop();
		arraySizes = new int[list.size()];
		for (int i = 0; i < list.size(); i++) {
			arraySizes[i] = list.get(i);
		}
		return arraySizes;
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
	 * Writes the code for visiting a multidimensional array that is projected from an origin array.
	 * Accepts a function that will decide what to do with each value from the origin array
	 * When the function is executed, the value from the origin array will be on top of the stack and when it is finished the value on top should be what will be inserted into the array.
	 * After the function is completed the stack size must be the same as when it started (effectively replacing the item on top of the stack with the item that should be inserted.
	 * The function should not modify the other stacks.
	 *
	 * @param javaWriter          the writer that will write the actual opcode
	 * @param sizeLocations       the locations of the array dimension sizes sizeLocations.length == dim !!
	 * @param dim                 the array's dimensions, reduced during the recursions of the loop to find the innermost loop
	 * @param originArrayLocation The location of the origin array.
	 * @param originArrayType     the type of the origin array. The element type needs to be assignable to the new array element type!
	 * @param currentArrayType    The current type of the array, reduced during the recursions of the functions
	 * @param innermostFunction   The function that will decide what to add to the array, needs to increase the stack size by one and may not touch the other stacks!
	 */
	static void visitProjected(JavaWriter javaWriter, int[] sizeLocations, int dim, int originArrayLocation, Type originArrayType, Type currentArrayType, InnermostFunction innermostFunction) {

		visitMultiDimArray(javaWriter, sizeLocations, dim, currentArrayType, (elementType, counterLocations) -> {
			//Load origin array
			javaWriter.loadObject(originArrayLocation);

			//Use arrayGets until we are at the element type
			Type modifiedOriginArrayType = originArrayType;
			for (final int location : counterLocations) {
				javaWriter.loadInt(location);
				javaWriter.arrayLoad(modifiedOriginArrayType = Type.getType(modifiedOriginArrayType.getDescriptor().substring(1)));
			}

			innermostFunction.apply(elementType, counterLocations);


		});
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
	static void visitMultiDimArrayWithDefaultValue(JavaWriter javaWriter, int[] sizeLocations, int dim, Type currentArrayType, int defaultLocation) {
		visitMultiDimArray(javaWriter, sizeLocations, new int[dim], dim, currentArrayType, (elementType, counterLocations) -> javaWriter.load(elementType, defaultLocation));
	}

	/**
	 * Writes the code for visiting a multidimensional array.
	 * Accepts a function that will decide what value to insert into the array slots.
	 * When the function is finished, one additional item needs to have been added to the stack.
	 * The already present stacks may not be touched!
	 *
	 * @param javaWriter        the writer that will write the actual opcode
	 * @param sizeLocations     the locations of the array dimension sizes sizeLocations.length == dim !!
	 * @param dim               the array's dimensions, reduced during the recursions of the loop to find the innermost loop
	 * @param currentArrayType  The current type of the array, reduced during the recursions of the functions
	 * @param innermostFunction The function that will decide what to add to the array, needs to increase the stack size by one and may not touch the other stacks!
	 */
	private static void visitMultiDimArray(JavaWriter javaWriter, int[] sizeLocations, int dim, Type currentArrayType, InnermostFunction innermostFunction) {
		visitMultiDimArray(javaWriter, sizeLocations, new int[dim], dim, currentArrayType, innermostFunction);
	}

	/**
	 * Checks if an expression can be inLined
	 *
	 * @param expression Expression to check for.
	 * @return can expression be inLined
	 */
	static boolean canBeInLined(Expression expression) {
		return expression instanceof FunctionExpression && ((FunctionExpression) expression).body instanceof ReturnStatement;
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
	static void visitMultiDimArray(JavaWriter javaWriter, int[] sizeLocations, int[] counterLocations, int dim, Type currentArrayType, InnermostFunction innermostFunction) {
		final Label begin = new Label();
		final Label end = new Label();
		javaWriter.label(begin);

		final int currentArraySizeLocation = sizeLocations[sizeLocations.length - dim];
		javaWriter.loadInt(currentArraySizeLocation);
		final Type elementType = Type.getType(currentArrayType.getDescriptor().substring(1));
		javaWriter.newArray(elementType);
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
			visitMultiDimArray(javaWriter, sizeLocations, counterLocations, dim - 1, elementType, innermostFunction);
		}
		javaWriter.arrayStore(elementType);

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
		void apply(Type elementType, int[] counterLocations);
	}
}

