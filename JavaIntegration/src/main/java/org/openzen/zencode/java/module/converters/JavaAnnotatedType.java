package org.openzen.zencode.java.module.converters;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;

public final class JavaAnnotatedType implements AnnotatedElement, Type {
	public enum ElementType {
		ANNOTATED_PARAMETERIZED_TYPE,
		ANNOTATED_TYPE,
		CLASS,
		GENERIC_ARRAY,
		PARAMETERIZED_TYPE,
		TYPE_VARIABLE,
		WILDCARD
	}

	private final ElementType elementType;
	private final AnnotatedElement annotatedElement;
	private final Type type;

	private JavaAnnotatedType(final ElementType elementType, final AnnotatedElement element, final Type type) {
		this.elementType = elementType;
		this.annotatedElement = element;
		this.type = type;
	}

	public static JavaAnnotatedType of(final Object element) {
		if (element instanceof Type && element instanceof AnnotatedElement) {
			return checkNotNull(JavaAnnotatedType::ofBoth, (Type & AnnotatedElement) element);
		}
		if (element instanceof Type) {
			return checkNotNull(JavaAnnotatedType::of, (Type) element);
		}
		if (element instanceof AnnotatedElement) {
			return checkNotNull(JavaAnnotatedType::of, (AnnotatedElement) element);
		}
		throw invalid(element);
	}

	public static JavaAnnotatedType[] arrayOf(final Object[] element) {
		return Arrays.stream(element).map(JavaAnnotatedType::of).toArray(JavaAnnotatedType[]::new);
	}

	private static <T> JavaAnnotatedType checkNotNull(final Function<T, JavaAnnotatedType> creator, final T element) {
		final JavaAnnotatedType result = creator.apply(element);
		if (result == null) throw invalid(element);
		return result;
	}

	private static <T extends Type & AnnotatedElement> JavaAnnotatedType ofBoth(final T element) {

		if (element instanceof Class<?>) {
			return of(ElementType.CLASS, element, element);
		}
		if (element instanceof TypeVariable<?>) {
			return of(ElementType.TYPE_VARIABLE, element, element);
		}

		final JavaAnnotatedType result = of((Type) element);
		return result == null ? of((AnnotatedElement) element) : result;
	}

	private static JavaAnnotatedType of(final Type element) {
		if (element instanceof ParameterizedType) {
			return of(ElementType.PARAMETERIZED_TYPE, null, element);
		}
		if (element instanceof GenericArrayType) {
			return of(ElementType.GENERIC_ARRAY, null, element);
		}
		if (element instanceof WildcardType) {
			return of(ElementType.WILDCARD, null, element);
		}

		return null;
	}

	private static JavaAnnotatedType of(final AnnotatedElement element) {
		if (element instanceof AnnotatedParameterizedType) {
			return of(ElementType.ANNOTATED_PARAMETERIZED_TYPE, element, null);
		}
		if (element instanceof AnnotatedType) {
			return of(ElementType.ANNOTATED_TYPE, element, null);
		}

		return null;
	}

	private static JavaAnnotatedType of(final ElementType elementType, final AnnotatedElement annotatedElement, final Type type) {
		return new JavaAnnotatedType(elementType, annotatedElement, type);
	}

	private static RuntimeException invalid(final Object object) {
		final String className = object == null ? null : object.getClass().getName();
		return new IllegalArgumentException("Unable to convert " + object + " (" + className + ") to a JavaAnnotatedType");
	}

	public ElementType getElementType() {
		return this.elementType;
	}

	public AnnotatedElement getAnnotatedElement() {
		return this.annotatedElement;
	}

	public Type getType() {
		return this.type;
	}

	@Override
	public boolean isAnnotationPresent(final Class<? extends Annotation> annotationClass) {
		return this.ifAnnotatedElement(it -> it.isAnnotationPresent(annotationClass), () -> false);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
		return this.ifAnnotatedElement(it -> it.getAnnotationsByType(annotationClass), () -> (T[]) Array.newInstance(annotationClass, 0));
	}

	@Override
	public <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
		return this.ifAnnotatedElement(it -> it.getDeclaredAnnotation(annotationClass), () -> null);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Annotation> T[] getDeclaredAnnotationsByType(Class<T> annotationClass) {
		return this.ifAnnotatedElement(it -> it.getDeclaredAnnotationsByType(annotationClass), () -> (T[]) Array.newInstance(annotationClass, 0));
	}

	@Override
	public String getTypeName() {
		return this.ifType(Type::getTypeName, () -> "invalid type");
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return this.ifAnnotatedElement(it -> it.getAnnotation(annotationClass), () -> null);
	}

	@Override
	public Annotation[] getAnnotations() {
		return this.ifAnnotatedElement(AnnotatedElement::getAnnotations, () -> new Annotation[0]);
	}

	@Override
	public Annotation[] getDeclaredAnnotations() {
		return this.ifAnnotatedElement(AnnotatedElement::getDeclaredAnnotations, () -> new Annotation[0]);
	}

	private <T> T ifAnnotatedElement(final Function<AnnotatedElement, T> block, final Supplier<T> orElse) {
		return this.annotatedElement == null ? orElse.get() : block.apply(this.annotatedElement);
	}

	private <T> T ifType(final Function<Type, T> block, final Supplier<T> orElse) {
		return this.type == null ? orElse.get() : block.apply(this.type);
	}

	@Override
	public String toString() {
		return "JavaAnnotatedType{" +
				"elementType=" + this.elementType +
				", annotatedElement=" + this.annotatedElement +
				", type=" + this.type +
				'}';
	}
}
