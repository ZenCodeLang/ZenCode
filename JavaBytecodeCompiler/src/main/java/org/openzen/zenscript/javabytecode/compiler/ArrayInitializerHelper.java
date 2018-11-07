package org.openzen.zenscript.javabytecode.compiler;


import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

public class ArrayInitializerHelper {
	public static void visitProjected(JavaWriter javaWriter, int[] sizeLocations, int dim, int originArrayLocation, Type originArrayType, int functionLocation, Type functionType, Type currentArrayType) {

		visitMultiDimArray(javaWriter, sizeLocations, dim, currentArrayType, (elementType, counterLocations) -> {
			//Load origin array
			javaWriter.loadObject(originArrayLocation);

			//Use arrayGets until we are at the element type
			Type modifiedOriginArrayType = originArrayType;
			for (final int location : counterLocations) {
				javaWriter.loadInt(location);
				javaWriter.arrayLoad(modifiedOriginArrayType = Type.getType(modifiedOriginArrayType.getDescriptor().substring(1)));
			}

			//Apply function here
			//javaWriter.loadObject(functionLocation);
			//javaWriter.swap();

			//TODO invoke?
			//javaWriter.invokeVirtual(new JavaMethod(JavaClass.fromInternalName("lambda1", JavaClass.Kind.CLASS), JavaMethod.Kind.INSTANCE, "accept", true, "(Ljava/lang/String;)Ljava/lang/String;", 0, false));
		});
	}


	public static void visitMultiDimArrayWithDefaultValue(JavaWriter javaWriter, int dim, int defaultLocation, Type currentArrayType, int[] sizeLocations) {
		visitMultiDimArray(javaWriter, sizeLocations, new int[dim], dim, currentArrayType, (elementType, counterLocations) -> javaWriter.load(elementType, defaultLocation));
	}

	public static void visitMultiDimArray(JavaWriter javaWriter, int[] sizeLocations, int dim, Type currentArrayType, InnermostFunction innermostFunction){
		visitMultiDimArray(javaWriter, sizeLocations, new int[dim], dim, currentArrayType, innermostFunction);
	}


	private static void visitMultiDimArray(JavaWriter javaWriter, int[] sizeLocations, int[] counterLocations, int dim, Type currentArrayType, InnermostFunction innermostFunction) {
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

