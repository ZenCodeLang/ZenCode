package org.openzen.zenscript.javashared.compiling;

import org.openzen.zenscript.codemodel.identifiers.ModuleSymbol;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.annotations.NativeTag;
import org.openzen.zenscript.codemodel.identifiers.DefinitionSymbol;
import org.openzen.zenscript.codemodel.identifiers.FieldSymbol;
import org.openzen.zenscript.codemodel.identifiers.MethodID;
import org.openzen.zenscript.codemodel.identifiers.MethodSymbol;
import org.openzen.zenscript.codemodel.type.GenericTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.javashared.*;

import java.util.*;

public class JavaCompilingClass {
	private static final boolean DEBUG_EMPTY = true;

	public static List<JavaCompilingClass> sortTopologically(List<JavaCompilingClass> definitions) {
		List<JavaCompilingClass> result = new ArrayList<>();
		Set<DefinitionSymbol> visited = new HashSet<>();
		Map<DefinitionSymbol, JavaCompilingClass> definitionsByType = new HashMap<>();

		for (JavaCompilingClass definition : definitions) {
			definitionsByType.put(definition.symbol, definition);
		}

		for (JavaCompilingClass definition : definitions) {
			sortTopologically(result, definition, visited, definitionsByType);
		}

		return result;
	}

	private static void sortTopologically(
			List<JavaCompilingClass> result,
			JavaCompilingClass definition,
			Set<DefinitionSymbol> visited,
			Map<DefinitionSymbol, JavaCompilingClass> definitionsByType
	) {
		if (visited.contains(definition.symbol))
			return;

		visited.add(definition.symbol);
		for (DefinitionSymbol type : definition.dependencies) {
			if (definitionsByType.containsKey(type)) {
				sortTopologically(result, definitionsByType.get(type), visited, definitionsByType);
			}
		}
		result.add(definition);
	}


	public final JavaCompilingModule module;
	public final DefinitionSymbol symbol;
	public final JavaClass compiled;
	public final JavaNativeClass nativeClass;
	public boolean membersPrepared;
	public boolean empty = true;
	private final Map<MethodSymbol, JavaCompilingMethod> methods = new HashMap<>();
	private final Map<FieldSymbol, JavaNativeField> fields = new HashMap<>();
	private final List<DefinitionSymbol> dependencies = new ArrayList<>();

	public JavaCompilingClass(JavaCompilingModule module, DefinitionSymbol symbol, JavaClass compiled, JavaNativeClass nativeClass) {
		this.module = module;
		this.symbol = symbol;
		this.compiled = compiled;
		this.nativeClass = nativeClass;
	}

	public void addMethod(MethodSymbol method, JavaCompilingMethod compiling) {
		methods.put(method, compiling);
		module.module.setMethodInfo(method, compiling.compiled);

		if (DEBUG_EMPTY && empty)
			getContext().logger.trace("Class " + compiled.fullName + " not empty because of " + method.getID());
		this.empty = false;
	}

	public void addField(FieldSymbol member, JavaNativeField field) {
		fields.put(member, field);
		module.module.setFieldInfo(member, field);
		this.empty = false;
	}

	public JavaNativeField addField(FieldSymbol field, NativeTag native_) {
		JavaNativeField javaField = new JavaNativeField(
				compiled,
				field.getName(),
				getContext().getDescriptor(field.getType()),
				getContext().getSignature(field.getType()));

		addField(field, javaField);
		return javaField;
	}

	public JavaCompilingMethod addConstructor(MethodSymbol method, NativeTag native_) {
		JavaNativeMethod javaMethod;
		if (native_ != null && nativeClass != null) {
			javaMethod = (JavaNativeMethod) nativeClass.getMethod(native_.value);
		} else {
			javaMethod = new JavaNativeMethod(
					compiled,
					getKind(method),
					"<init>",
					true,
					getContext().getMethodDescriptorConstructor(method),
					JavaModifiers.getJavaModifiers(method.getModifiers()),
					false,
					method.getHeader().useTypeParameters()
			);
		}

		JavaCompilingMethod compiling = new JavaCompilingMethod(compiled, javaMethod, getContext().getMethodSignatureConstructor(method));
		addMethod(method, compiling);
		return compiling;
	}

	public void addMethod(MethodSymbol method, NativeTag native_) {

		if (native_ != null && nativeClass != null) {
			final String signature = getContext().getMethodSignature(method.getHeader());
			JavaMethod method1 = nativeClass.getMethod(native_.value);
			addMethod(method, method1.asCompilingMethod(compiled, signature));
		} else {
			final JavaNativeMethod.Kind kind = getKind(method);
			final String descriptor;
			final String signature;
			if (kind == JavaNativeMethod.Kind.EXPANSION) {
				descriptor = getContext().getMethodDescriptorExpansion(method.getHeader(), method.getTargetType());
				signature = getContext().getMethodSignatureExpansion(method.getHeader(), method.getTargetType());
			} else {
				descriptor = getContext().getMethodDescriptor(method.getHeader());
				signature = getContext().getMethodSignature(method.getHeader());
			}
			JavaNativeMethod javaMethod = new JavaNativeMethod(
					compiled,
					kind,
					method.getID().accept(MethodNamer.INSTANCE),
					true,
					descriptor,
					JavaModifiers.getJavaModifiers(method.getModifiers()),
					getContext().isGenericReturn(method.getHeader().getReturnType()),
					method.getHeader().useTypeParameters());
			addMethod(method, new JavaCompilingMethod(compiled, javaMethod, signature));
		}
	}

	public void addDependency(DefinitionSymbol symbol) {
		dependencies.add(symbol);
	}

	public JavaNativeField getField(FieldSymbol field) {
		return fields.get(field);
	}

	public JavaCompilingMethod getMethod(MethodSymbol method) {
		return methods.get(method);
	}

	public JavaContext getContext() {
		return module.context;
	}

	public ModuleSymbol getModule() {
		return module.module.module;
	}

	public String getInternalName() {
		return compiled.internalName;
	}

	private JavaNativeMethod.Kind getKind(MethodSymbol method) {
		if (method.getModifiers().isStatic())
			return JavaNativeMethod.Kind.STATIC;
		else if (method.getDefiningType().isExpansion())
			return JavaNativeMethod.Kind.EXPANSION;
		else
			return JavaNativeMethod.Kind.INSTANCE;
	}

	private static class MethodNamer implements MethodID.Visitor<String>  {
		private static final MethodNamer INSTANCE = new MethodNamer();

		@Override
		public String visitInstanceMethod(String name) {
			return name;
		}

		@Override
		public String visitStaticMethod(String name) {
			return name;
		}

		@Override
		public String visitOperator(OperatorType operator) {
			return getOperatorName(operator);
		}

		@Override
		public String visitStaticOperator(OperatorType operator) {
			return getOperatorName(operator);
		}

		@Override
		public String visitGetter(String name) {
			return "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
		}

		@Override
		public String visitSetter(String name) {
			return "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
		}

		@Override
		public String visitStaticGetter(String name) {
			return "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
		}

		@Override
		public String visitStaticSetter(String name) {
			return "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
		}

		@Override
		public String visitCaster(TypeID type) {
			return "to" + JavaTypeNameVisitor.INSTANCE.process(type);
		}

		@Override
		public String visitIterator(int variables) {
			return variables == 1 ? "iterator" : "iterator" + variables;
		}

		private String getOperatorName(OperatorType operator) {
			switch (operator) {
				case NEG:
					return "negate";
				case NOT:
					return "invert";
				case ADD:
					return "add";
				case ADDASSIGN:
					return "addAssign";
				case SUB:
					return "subtract";
				case SUBASSIGN:
					return "subAssign";
				case CAT:
					return "concat";
				case CATASSIGN:
					return "append";
				case MUL:
					return "mul";
				case MULASSIGN:
					return "mulAssign";
				case DIV:
					return "div";
				case DIVASSIGN:
					return "divAssign";
				case MOD:
					return "mod";
				case MODASSIGN:
					return "modAssign";
				case AND:
					return "and";
				case ANDASSIGN:
					return "andAssign";
				case OR:
					return "or";
				case ORASSIGN:
					return "orAssign";
				case XOR:
					return "xor";
				case XORASSIGN:
					return "xorAssign";
				case SHL:
					return "shl";
				case SHLASSIGN:
					return "shlAssign";
				case SHR:
					return "shr";
				case SHRASSIGN:
					return "shrAssign";
				case USHR:
					return "ushr";
				case USHRASSIGN:
					return "ushrAssign";
				case INDEXGET:
					return "getAt";
				case INDEXSET:
					return "setAt";
				case INCREMENT:
					return "increment";
				case DECREMENT:
					return "decrement";
				case CONTAINS:
					return "contains";
				case EQUALS:
					return "equals_";
				case COMPARE:
					return "compareTo";
				case RANGE:
					return "until";
				case CAST:
					return "cast";
				case CALL:
					return "call";
				case MEMBERGETTER:
					return "getMember";
				case MEMBERSETTER:
					return "setMember";
				default:
					throw new IllegalArgumentException("Invalid operator: " + operator);
			}
		}
	}
}
