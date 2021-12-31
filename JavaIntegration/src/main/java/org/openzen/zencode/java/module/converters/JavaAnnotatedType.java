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
			return or(ofBoth(element), invalid(element));
		}
		if (element instanceof Type) {
			return or(of((Type) element), invalid(element));
		}
		if (element instanceof AnnotatedElement) {
			return or(of((AnnotatedElement) element), invalid(element));
		}
		throw invalid(element);
	}

	public static JavaAnnotatedType[] of(final Object[] element) {
		return Arrays.stream(element).map(JavaAnnotatedType::of).toArray(JavaAnnotatedType[]::new);
	}

	private static JavaAnnotatedType or(final JavaAnnotatedType result, final RuntimeException ifNull) {
		if (result == null) throw ifNull;
		return result;
	}

	private static JavaAnnotatedType ofBoth(final Object element) {
		final AnnotatedElement annotatedElement = (AnnotatedElement) element;
		final Type type = (Type) element;

		if (element instanceof Class<?>) {
			return of(ElementType.CLASS, annotatedElement, type);
		}
		if (element instanceof TypeVariable<?>) {
			return of(ElementType.TYPE_VARIABLE, annotatedElement, type);
		}

		final JavaAnnotatedType result = of(type);
		return result == null ? of(annotatedElement) : result;
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
		return this.ifAnnotatedElement(() -> false, it -> it.isAnnotationPresent(annotationClass));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
		return this.ifAnnotatedElement(() -> (T[]) Array.newInstance(annotationClass, 0), it -> it.getAnnotationsByType(annotationClass));
	}

	@Override
	public <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
		return this.ifAnnotatedElement(() -> null, it -> it.getDeclaredAnnotation(annotationClass));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Annotation> T[] getDeclaredAnnotationsByType(Class<T> annotationClass) {
		return this.ifAnnotatedElement(() -> (T[]) Array.newInstance(annotationClass, 0), it -> it.getDeclaredAnnotationsByType(annotationClass));
	}

	@Override
	public String getTypeName() {
		return this.ifType(() -> "invalid type", Type::getTypeName);
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return this.ifAnnotatedElement(() -> null, it -> it.getAnnotation(annotationClass));
	}

	@Override
	public Annotation[] getAnnotations() {
		return this.ifAnnotatedElement(() -> new Annotation[0], AnnotatedElement::getAnnotations);
	}

	@Override
	public Annotation[] getDeclaredAnnotations() {
		return this.ifAnnotatedElement(() -> new Annotation[0], AnnotatedElement::getDeclaredAnnotations);
	}

	private <T> T ifAnnotatedElement(final Supplier<T> def, final Function<AnnotatedElement, T> block) {
		return this.annotatedElement == null ? def.get() : block.apply(this.annotatedElement);
	}

	private <T> T ifType(final Supplier<T> def, final Function<Type, T> block) {
		return this.type == null ? def.get() : block.apply(this.type);
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
