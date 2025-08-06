package org.openzen.zenscript.tree.ast;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.SourceFile;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.lexer.ZSToken;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.tree.Child;
import org.openzen.zenscript.tree.Tree;
import org.openzen.zenscript.tree.TreeKind;
import org.openzen.zenscript.tree.ast.definition.*;
import org.openzen.zenscript.tree.ast.definition.member.*;
import org.openzen.zenscript.tree.ast.definition.variant.VariantOptionNode;
import org.openzen.zenscript.tree.ast.definition.variant.VariantOptionTypesNode;
import org.openzen.zenscript.tree.ast.definition.variant.VariantOptionsNode;
import org.openzen.zenscript.tree.ast.expression.*;
import org.openzen.zenscript.tree.ast.function.*;
import org.openzen.zenscript.tree.ast.generic.GenericBoundNode;
import org.openzen.zenscript.tree.ast.generic.TypeArgumentsNode;
import org.openzen.zenscript.tree.ast.generic.TypeParameterNode;
import org.openzen.zenscript.tree.ast.generic.TypeParametersNode;
import org.openzen.zenscript.tree.ast.misc.*;
import org.openzen.zenscript.tree.ast.statement.*;
import org.openzen.zenscript.tree.ast.type.*;
import org.openzen.zenscript.tree.lexer.PositionedToken;
import org.openzen.zenscript.tree.visitor.Chainsaw;
import org.openzen.zenscript.tree.visitor.TreeVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class CSTToASTVisitor implements TreeVisitor<Void, ASTNode> {

	private final SourceFile source;

	public CSTToASTVisitor(SourceFile source) {
		this.source = source;
	}

	@Override
	public FileNode visitFile(Tree tree, Void context) {
		List<ASTNode> children = new ArrayList<>();
		for (Tree child : tree.treeChildren()) {
			children.add(child.accept(this, context));
		}
		return new FileNode(tree.position(this.source), children);
	}

	@Override
	public ASTNode visitError(Tree tree, Void context) {
		return null;
	}

	@Override
	public NameNode visitName(Tree tree, Void context) {
		return new Chainsaw(tree).cut(ZSTokenType.T_IDENTIFIER, NameNode::new);
	}

	@Override
	public ASTNode visitAnnotation(Tree tree, Void context) {
		return null;
	}

	public ASTNode visitFunctionBody(Tree tree, Chainsaw chainsaw) {
		boolean isLambda = chainsaw.cut(new ZSTokenType[]{ZSTokenType.T_LAMBDA}, false, token -> true);
		if (isLambda) {
			return chainsaw.cut(TreeKind.LAMBDA_BODY, this::visit);
		} else if (chainsaw.cut(new ZSTokenType[]{ZSTokenType.T_SEMICOLON}, false, token -> true)) {
			return new EmptyFunctionBodyNode(tree.position(this.source));
		} else {
			return chainsaw.cut(treeKind -> true, this::visit);
		}

	}

	@Override
	public ASTNode visitImport(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		chainsaw.cut(ZSTokenType.K_IMPORT);
		boolean relative = chainsaw.cut(ZSTokenType.T_DOT) != null;
		boolean parsed = true;
		List<NameNode> names = new ArrayList<>();
		while (parsed) {
			NameNode name = chainsaw.cut(TreeKind.NAME, this::visit);
			if (name == null) {
				parsed = false;
			} else {
				names.add(name);
				chainsaw.cut(ZSTokenType.T_DOT);
			}
		}
		boolean aliased = chainsaw.cut(ZSTokenType.K_AS) != null;
		NameNode alias = null;
		if (aliased) {
			alias = chainsaw.cut(TreeKind.NAME, this::visit);
		}
		return new ImportNode(tree.position(this.source), relative, names, alias);
	}

	@Override
	public ASTNode visitLambdaBody(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		//TODO confirm
		return chainsaw.cut(tre -> true, this::visit);
	}

	@Override
	public FunctionHeaderNode visitFunctionHeader(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		TypeParametersNode typeParams = chainsaw.cut(TreeKind.TYPE_PARAMS, this::visit);
		ParametersNode parameters = chainsaw.cut(TreeKind.PARAMS, this::visit);
		TypeNode returnType = chainsaw.cut(TreeKind.TYPE_DECLARATION, this::visit);

		//TODO do!
		return new FunctionHeaderNode(tree.position(this.source), typeParams, parameters, returnType, null);
	}

	@Override
	public BlockStatementNode visitBlock(Tree tree, Void context) {
		List<ASTNode> nodes = new ArrayList<>();
		for (Tree treeChild : tree.treeChildren()) {
			nodes.add(visit(treeChild));
		}
		return new BlockStatementNode(tree.position(this.source), nodes);
	}

	@Override
	public BasicTypeNode visitBasicType(Tree tree, Void context) {
		return new BasicTypeNode(tree.position(this.source), tree.child(0).asToken());
	}

	@Override
	public RangeTypeNode visitRangeType(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		TypeNode fromType = chainsaw.cut(TreeKind::isType, this::visit);
		chainsaw.cut(ZSTokenType.T_DOT2);
		TypeNode toType = chainsaw.cut(TreeKind::isType, this::visit);
		return new RangeTypeNode(tree.position(this.source), fromType, toType);
	}

	@Override
	public ArrayTypeNode visitArrayType(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		TypeNode type = chainsaw.cut(TreeKind::isType, this::visit);
		chainsaw.cut(ZSTokenType.T_SQOPEN);
		int dimensions = 1;
		while (chainsaw.cut(ZSTokenType.T_COMMA) != null) {
			dimensions++;
		}
		return new ArrayTypeNode(tree.position(this.source), type, dimensions);
	}

	@Override
	public GenericMapTypeNode visitGenericMapType(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		TypeNode value = chainsaw.cut(TreeKind::isType, this::visit);

		chainsaw.cut(ZSTokenType.T_SQOPEN);
		chainsaw.cut(ZSTokenType.T_LESS);
		TypeParameterNode key = chainsaw.cut(TreeKind.TYPE_PARAM, this::visit);

		return new GenericMapTypeNode(tree.position(this.source), key, value);
	}

	@Override
	public MapTypeNode visitMapType(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		TypeNode value = chainsaw.cut(TreeKind::isType, this::visit);

		chainsaw.cut(ZSTokenType.T_SQOPEN);
		chainsaw.cut(ZSTokenType.T_LESS);
		TypeNode key = chainsaw.cut(TreeKind::isType, this::visit);

		return new MapTypeNode(tree.position(this.source), key, value);
	}

	@Override
	public ASTNode visitOptionalType(Tree tree, Void context) {
		return new OptionalTypeNode(tree.position(this.source), visit(tree.child(0).asTree()));
	}

	@Override
	public ASTNode visitBracedType(Tree tree, Void context) {
		//TODO do we want this to do more?
		return visit(tree.child(0).asTree());
	}

	@Override
	public FunctionTypeNode visitFunctionType(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		chainsaw.cut(ZSTokenType.K_FUNCTION);
		FunctionHeaderNode header = chainsaw.cut(TreeKind.FUNCTION_HEADER, this::visit);
		return new FunctionTypeNode(tree.position(this.source), header);
	}

	@Override
	public NamedTypeNode visitNamedTyped(Tree tree, Void context) {
		List<NamedTypeNode.TypeName> names = new ArrayList<>();

		Chainsaw chainsaw = new Chainsaw(tree);
		NameNode name = chainsaw.cut(TreeKind.NAME, this::visit);
		while (name != null) {
			TypeArgumentsNode typeArgs = chainsaw.cut(TreeKind.TYPE_ARGS, this::visit);
			names.add(new NamedTypeNode.TypeName(name, typeArgs));
			chainsaw.cut(ZSTokenType.T_DOT);
			name = chainsaw.cut(TreeKind.NAME, this::visit);
		}
		return new NamedTypeNode(tree.position(this.source), names);
	}

	@Override
	public LabelNode visitLabel(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		chainsaw.cut(ZSTokenType.T_COLON);
		NameNode name = chainsaw.cut(TreeKind.NAME, this::visit);
		return new LabelNode(tree.position(this.source), name);
	}

	@Override
	public ForKeysNode visitForNames(Tree tree, Void context) {
		List<NameNode> names = new ArrayList<>();

		Chainsaw chainsaw = new Chainsaw(tree);
		NameNode name = chainsaw.cut(TreeKind.NAME, this::visit);
		while (name != null) {
			names.add(name);
			chainsaw.cut(ZSTokenType.T_COMMA);
			name = chainsaw.cut(TreeKind.NAME, this::visit);
		}
		return new ForKeysNode(tree.position(this.source), names);
	}

	@Override
	public ParametersNode visitParams(Tree tree, Void context) {

		List<ParameterNode> params = new ArrayList<>();

		Chainsaw chainsaw = new Chainsaw(tree);
		chainsaw.cut(ZSTokenType.T_BROPEN);
		ParameterNode param = chainsaw.cut(TreeKind.PARAM, this::visit);
		while (param != null) {
			params.add(param);
			chainsaw.cut(ZSTokenType.T_COMMA);
			param = chainsaw.cut(TreeKind.PARAM, this::visit);
		}
		return new ParametersNode(tree.position(this.source), params);
	}

	@Override
	public ParameterNode visitParam(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);

		NameNode name = chainsaw.cut(TreeKind.NAME, this::visit);
		boolean variadic = chainsaw.cut(new ZSTokenType[]{ZSTokenType.T_DOT3}, false, token -> true);
		TypeNode type = chainsaw.cut(TreeKind.TYPE_DECLARATION, this::visit);

		boolean hasDefaultValue = chainsaw.cut(new ZSTokenType[]{ZSTokenType.T_ASSIGN}, false, token -> true);
		ExpressionNode defaultValue = null;
		if (hasDefaultValue) {
			defaultValue = chainsaw.cut(TreeKind::isExpression, this::visit);
		}

		return new ParameterNode(tree.position(this.source), name, type, defaultValue, variadic);
	}

	@Override
	public ASTNode visitThrows(Tree tree, Void context) {
		return null;
	}

	@Override
	public ClassNode visitClassDefinition(Tree tree, Void context) {

		Chainsaw chainsaw = new Chainsaw(tree);
		ModifiersNode modifiers = chainsaw.cut(TreeKind.MODIFIERS, this::visit);
		chainsaw.cut(ZSTokenType.K_CLASS);
		NameNode name = chainsaw.cut(TreeKind.NAME, this::visit);
		TypeParametersNode typeParams = chainsaw.cut(TreeKind.TYPE_PARAMS, this::visit);
		SuperTypeNode superType = chainsaw.cut(TreeKind.SUPER_TYPE, this::visit);
		MembersNode members = chainsaw.cut(TreeKind.MEMBERS, this::visit);
		return new ClassNode(modifiers, tree.position(this.source), name, members, typeParams, superType);
	}

	@Override
	public ASTNode visitTypeArgs(Tree tree, Void context) {
		List<TypeNode> types = new ArrayList<>();
		Chainsaw chainsaw = new Chainsaw(tree);

		chainsaw.cut(ZSTokenType.T_LESS);

		TypeNode type = chainsaw.cut(TreeKind::isType, this::visit);

		while (type != null) {
			types.add(type);
			chainsaw.cut(ZSTokenType.T_COMMA);
			type = chainsaw.cut(TreeKind::isType, this::visit);
		}
		return new TypeArgumentsNode(tree.position(this.source), types);
	}

	@Override
	public TypeParametersNode visitTypeParams(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		chainsaw.cut(ZSTokenType.T_LESS);
		List<TypeParameterNode> typeParams = new ArrayList<>();
		TypeParameterNode param = chainsaw.cut(TreeKind.TYPE_PARAM, this::visit);
		while (param != null) {
			typeParams.add(param);
			param = chainsaw.cut(TreeKind.TYPE_PARAM, this::visit);
		}
		return new TypeParametersNode(tree.position(this.source), typeParams);
	}

	@Override
	public TypeParameterNode visitTypeParam(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		NameNode name = chainsaw.cut(TreeKind.NAME, this::visit);

		List<GenericBoundNode> bounds = new ArrayList<>();

		GenericBoundNode bound = chainsaw.cut(treeKind -> treeKind == TreeKind.TYPE_BOUND || treeKind == TreeKind.SUPER_BOUND, this::visit);
		while (bound != null) {
			bounds.add(bound);
			bound = chainsaw.cut(treeKind -> treeKind == TreeKind.TYPE_BOUND || treeKind == TreeKind.SUPER_BOUND, this::visit);
		}
		return new TypeParameterNode(tree.position(this.source), name, bounds);
	}

	@Override
	public GenericBoundNode visitTypeBound(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		chainsaw.cut(ZSTokenType.T_COLON);
		TypeNode bound = chainsaw.cut(TreeKind::isType, this::visit);
		return new GenericBoundNode(tree.position(this.source), bound, false);
	}

	@Override
	public GenericBoundNode visitSuperBound(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		chainsaw.cut(ZSTokenType.T_COLON);
		chainsaw.cut(ZSTokenType.K_SUPER);
		TypeNode bound = chainsaw.cut(TreeKind::isType, this::visit);
		return new GenericBoundNode(tree.position(this.source), bound, true);
	}

	@Override
	public SuperTypeNode visitSuperType(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		return new SuperTypeNode(tree.position(this.source), chainsaw.cut(TreeKind::isType, this::visit));
	}

	@Override
	public SuperTypesNode visitSuperTypes(Tree tree, Void context) {
		List<SuperTypeNode> superTypes = new ArrayList<>();
		Chainsaw chainsaw = new Chainsaw(tree);

		chainsaw.cut(ZSTokenType.T_COLON);
		SuperTypeNode superType = chainsaw.cut(TreeKind.SUPER_TYPE, this::visit);
		while (superType != null) {
			superTypes.add(superType);
			superType = chainsaw.cut(TreeKind.SUPER_TYPE, this::visit);
		}

		return new SuperTypesNode(tree.position(this.source), superTypes);
	}

	@Override
	public EnumNode visitEnumDefinition(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		ModifiersNode modifiers = chainsaw.cut(TreeKind.MODIFIERS, this::visit);
		chainsaw.cut(ZSTokenType.K_ENUM);
		NameNode name = chainsaw.cut(TreeKind.NAME, this::visit);
		chainsaw.cut(ZSTokenType.T_AOPEN);
		List<EnumNode.EnumConstantNode> constants = new ArrayList<>();
		EnumNode.EnumConstantNode constant = chainsaw.cut(TreeKind.ENUM_CONSTANT, this::visit);
		while (constant != null) {
			chainsaw.cut(ZSTokenType.T_COMMA);
			constants.add(constant);
			constant = chainsaw.cut(TreeKind.ENUM_CONSTANT, this::visit);
		}

		chainsaw.cut(ZSTokenType.T_SEMICOLON);
		MembersNode membersNode = chainsaw.cut(TreeKind.MEMBERS, this::visit);

		return new EnumNode(modifiers, tree.position(this.source), name, constants, null, membersNode);
	}

	@Override
	public EnumNode.EnumConstantNode visitEnumConstant(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		NameNode name = chainsaw.cut(TreeKind.NAME, this::visit);
		CallArgumentsNode arguments = chainsaw.cut(TreeKind.CALL_ARGUMENTS, this::visit);

		return new EnumNode.EnumConstantNode(name, arguments, null);
	}

	@Override
	public ASTNode visitEnumConstantValue(Tree tree, Void context) {
		return null;
	}

	@Override
	public InterfaceNode visitInterfaceDefinition(Tree tree, Void context) {

		Chainsaw chainsaw = new Chainsaw(tree);
		ModifiersNode modifiers = chainsaw.cut(TreeKind.MODIFIERS, this::visit);
		chainsaw.cut(ZSTokenType.K_INTERFACE);
		NameNode name = chainsaw.cut(TreeKind.NAME, this::visit);
		TypeParametersNode typeParams = chainsaw.cut(TreeKind.TYPE_PARAMS, this::visit);
		SuperTypesNode superType = chainsaw.cut(TreeKind.SUPER_TYPES, this::visit);
		MembersNode members = chainsaw.cut(TreeKind.MEMBERS, this::visit);
		return new InterfaceNode(modifiers, tree.position(this.source), name, members, typeParams, superType);
	}

	@Override
	public FunctionNode visitFunctionDefinition(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		ModifiersNode modifiers = chainsaw.cut(TreeKind.MODIFIERS, this::visit);
		chainsaw.cut(ZSTokenType.K_FUNCTION);
		NameNode name = chainsaw.cut(TreeKind.NAME, this::visit);
		FunctionHeaderNode header = chainsaw.cut(TreeKind.FUNCTION_HEADER, this::visit);
		ASTNode body = visitFunctionBody(tree, chainsaw);
		return new FunctionNode(modifiers, tree.position(this.source), header, body, name);
	}

	@Override
	public StructNode visitStructDefinition(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		ModifiersNode modifiers = chainsaw.cut(TreeKind.MODIFIERS, this::visit);
		chainsaw.cut(ZSTokenType.K_STRUCT);
		NameNode name = chainsaw.cut(TreeKind.NAME, this::visit);
		TypeParametersNode typeParams = chainsaw.cut(TreeKind.TYPE_PARAMS, this::visit);
		MembersNode members = chainsaw.cut(TreeKind.MEMBERS, this::visit);
		return new StructNode(modifiers, tree.position(this.source), name, typeParams, members);
	}

	@Override
	public AliasNode visitAliasDefinition(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		ModifiersNode modifiers = chainsaw.cut(TreeKind.MODIFIERS, this::visit);
		chainsaw.cut(ZSTokenType.K_ALIAS);
		NameNode name = chainsaw.cut(TreeKind.NAME, this::visit);
		TypeParametersNode typeParams = chainsaw.cut(TreeKind.TYPE_PARAMS, this::visit);
		TypeNode type = chainsaw.cut(TreeKind.TYPE_DECLARATION, this::visit);
		return new AliasNode(modifiers, tree.position(this.source), name, typeParams, type);
	}

	@Override
	public ExpansionNode visitExpansionDefinition(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		ModifiersNode modifiers = chainsaw.cut(TreeKind.MODIFIERS, this::visit);
		chainsaw.cut(ZSTokenType.K_EXPAND);
		TypeParametersNode typeParams = chainsaw.cut(TreeKind.TYPE_PARAMS, this::visit);
		TypeNode type = chainsaw.cut(TreeKind::isType, this::visit);
		MembersNode members = chainsaw.cut(TreeKind.MEMBERS, this::visit);
		return new ExpansionNode(modifiers, tree.position(this.source), type, members, typeParams);
	}

	@Override
	public VariantNode visitVariantDefinition(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		ModifiersNode modifiers = chainsaw.cut(TreeKind.MODIFIERS, this::visit);
		chainsaw.cut(ZSTokenType.K_VARIANT);
		NameNode name = chainsaw.cut(TreeKind.NAME, this::visit);
		TypeParametersNode typeParams = chainsaw.cut(TreeKind.TYPE_PARAMS, this::visit);
		VariantOptionsNode variants = chainsaw.cut(TreeKind.VARIANTS, this::visit);
		MembersNode members = chainsaw.cut(TreeKind.MEMBERS, this::visit);
		return new VariantNode(modifiers, tree.position(this.source), name, members, typeParams, variants);
	}

	@Override
	public VariantOptionNode visitVariantOption(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		NameNode name = chainsaw.cut(TreeKind.NAME, this::visit);
		VariantOptionTypesNode types = chainsaw.cut(TreeKind.VARIANT_OPTION_TYPES, this::visit);
		return new VariantOptionNode(tree.position(this.source), name, types);
	}

	@Override
	public VariantOptionTypesNode visitVariantOptionTypes(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		chainsaw.cut(ZSTokenType.T_BROPEN);

		List<TypeNode> types = new ArrayList<>();
		TypeNode type = chainsaw.cut(TreeKind::isType, this::visit);
		while (type != null) {
			types.add(type);
			chainsaw.cut(ZSTokenType.T_COMMA);
			type = chainsaw.cut(TreeKind::isType, this::visit);
		}
		return new VariantOptionTypesNode(tree.position(this.source), types);
	}

	@Override
	public SwitchNode.SwitchCasesNode visitSwitchCases(Tree tree, Void context) {
		List<SwitchNode.SwitchCaseNode> cases = new ArrayList<>();
		Chainsaw chainsaw = new Chainsaw(tree);
		chainsaw.cut(ZSTokenType.T_AOPEN);
		SwitchNode.SwitchCaseNode caseNode = chainsaw.cut(TreeKind.SWITCH_CASE, this::visit);
		while (caseNode != null) {
			cases.add(caseNode);
			caseNode = chainsaw.cut(TreeKind.SWITCH_CASE, this::visit);
		}
		return new SwitchNode.SwitchCasesNode(cases);
	}

	@Override
	public SwitchNode.SwitchCaseNode visitSwitchCase(Tree tree, Void context) {

		Chainsaw chainsaw = new Chainsaw(tree);
		boolean isCase = chainsaw.cut(new ZSTokenType[]{ZSTokenType.K_CASE}, false, token -> true);
		ExpressionNode expression = null;
		if (isCase) {
			expression = chainsaw.cut(TreeKind::isExpression, this::visit);
		} else {
			chainsaw.cut(ZSTokenType.K_DEFAULT);
		}
		chainsaw.cut(ZSTokenType.T_COLON);
		List<ASTNode> statements = new ArrayList<>();
		ASTNode statement = chainsaw.cut(treeKind -> true, this::visit);
		while (statement != null) {
			statements.add(statement);
			statement = chainsaw.cut(treeKind -> true, this::visit);
		}

		return new SwitchNode.SwitchCaseNode(expression, statements);
	}

	@Override
	public ModifiersNode visitModifiers(Tree tree, Void context) {
		List<ModifierNode> modifiers = new ArrayList<>();
		Chainsaw chainsaw = new Chainsaw(tree);

		boolean cut = true;
		while (cut) {
			ModifierNode modifierNode = chainsaw.cutToken(ModifierNode::new);
			if (modifierNode != null) {
				modifiers.add(modifierNode);
			} else {
				cut = false;
			}
		}
		return new ModifiersNode(tree.position(this.source), modifiers);
	}

	@Override
	public MembersNode visitMembers(Tree tree, Void context) {
		List<DefinitionMemberNode> members = new ArrayList<>();
		Chainsaw chainsaw = new Chainsaw(tree);
		chainsaw.cut(ZSTokenType.T_AOPEN);
		DefinitionMemberNode lastNode;
		do {
			lastNode = chainsaw.cut(TreeKind::isDefinitionMember, this::visit);
			if (lastNode != null) {
				members.add(lastNode);
			}
		} while (lastNode != null);
		return new MembersNode(tree.position(this.source), members);
	}

	@Override
	public ConstructorNode visitConstructorMember(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		ModifiersNode modifiers = chainsaw.cut(TreeKind.MODIFIERS, this::visit);
		chainsaw.cut(ZSTokenType.K_THIS);
		FunctionHeaderNode header = chainsaw.cut(TreeKind.FUNCTION_HEADER, this::visit);
		ASTNode body = visitFunctionBody(tree, chainsaw);
		return new ConstructorNode(tree.position(this.source), null, modifiers, header, body);
	}

	@Override
	public FieldNode visitFieldMember(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		ModifiersNode modifiers = chainsaw.cut(TreeKind.MODIFIERS, this::visit);
		boolean isFinal = chainsaw.cut(new ZSTokenType[]{ZSTokenType.K_VAR, ZSTokenType.K_VAL}, false, token -> token.getType() == ZSTokenType.K_VAL);
		NameNode name = chainsaw.cut(TreeKind.NAME, this::visit);
		TypeNode type = chainsaw.cut(TreeKind.TYPE_DECLARATION, this::visit);
		AutosNode autos = chainsaw.cut(TreeKind.FIELD_AUTO, this::visit);
		chainsaw.cut(ZSTokenType.T_ASSIGN);
		ExpressionNode expression = chainsaw.cut(TreeKind::isExpression, this::visit);
		return new FieldNode(tree.position(this.source), modifiers, name, type, isFinal, autos, expression);
	}

	@Override
	public TypeNode visitTypeDeclaration(Tree tree, Void context) {
		//TODO do I want this to return a TypeDeclaration node or a type node?
		Chainsaw chainsaw = new Chainsaw(tree);
		chainsaw.cut(ZSTokenType.T_COLON, ZSTokenType.K_AS);
		return chainsaw.cut(TreeKind::isType, this::visit);
	}

	@Override
	public AutosNode visitFieldAuto(Tree tree, Void context) {

		AutoGetterNode autoGet = null;
		AutoSetterNode autoSet = null;
		Tree getTree = tree.childOfKind(TreeKind.AUTO_GET);
		Tree setTree = tree.childOfKind(TreeKind.AUTO_SET);

		if (getTree != null) {
			autoGet = this.visit(getTree);
		}
		if (setTree != null) {
			autoSet = this.visit(setTree);
		}
		return new AutosNode(tree.position(this.source), autoGet, autoSet);
	}

	@Override
	public AutoGetterNode visitAutoGet(Tree tree, Void context) {
		Tree modifiersTree = tree.childOfKind(TreeKind.MODIFIERS);
		ModifiersNode modifiers = null;
		if (modifiersTree != null) {
			modifiers = this.visit(modifiersTree);
		}
		return new AutoGetterNode(tree.position(this.source), modifiers);
	}

	@Override
	public AutoSetterNode visitAutoSet(Tree tree, Void context) {
		Tree modifiersTree = tree.childOfKind(TreeKind.MODIFIERS);
		ModifiersNode modifiers = null;
		if (modifiersTree != null) {
			modifiers = this.visit(modifiersTree);
		}
		return new AutoSetterNode(tree.position(this.source), modifiers);
	}

	@Override
	public CasterNode visitCasterMember(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		ModifiersNode modifiers = chainsaw.cut(TreeKind.MODIFIERS, this::visit);
		chainsaw.cut(ZSTokenType.K_AS);

		TypeNode type = chainsaw.cut(TreeKind::isType, this::visit);
		//TODO this is a bit of a hack, but it works for now
		chainsaw.cut(ZSTokenType.T_LAMBDA);
		ASTNode body = visitFunctionBody(tree, chainsaw);
		return new CasterNode(tree.position(this.source), null, modifiers, type, body);
	}

	@Override
	public MethodNode visitMethodMember(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		ModifiersNode modifiers = chainsaw.cut(TreeKind.MODIFIERS, this::visit);
		NameNode name = chainsaw.cut(TreeKind.NAME, this::visit);
		FunctionHeaderNode header = chainsaw.cut(TreeKind.FUNCTION_HEADER, this::visit);
		ASTNode body = visitFunctionBody(tree, chainsaw);
		return new MethodNode(tree.position(this.source), null, modifiers, name, header, body);
	}

	@Override
	public SetterNode visitSetterMember(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		ModifiersNode modifiers = chainsaw.cut(TreeKind.MODIFIERS, this::visit);
		chainsaw.cut(ZSTokenType.K_SET);
		NameNode name = chainsaw.cut(TreeKind.NAME, this::visit);
		TypeNode type = chainsaw.cut(TreeKind.TYPE_DECLARATION, this::visit);
		ASTNode body = chainsaw.cut(treeKind -> true, this::visit);
		return new SetterNode(tree.position(this.source), null, modifiers, name, type, body);
	}

	@Override
	public GetterNode visitGetterMember(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		ModifiersNode modifiers = chainsaw.cut(TreeKind.MODIFIERS, this::visit);
		chainsaw.cut(ZSTokenType.K_GET);
		NameNode name = chainsaw.cut(TreeKind.NAME, this::visit);
		TypeNode type = chainsaw.cut(TreeKind.TYPE_DECLARATION, this::visit);
		ASTNode body = visitFunctionBody(tree, chainsaw);
		return new GetterNode(tree.position(this.source), null, modifiers, name, type, body);
	}

	@Override
	public ImplementsNode visitImplementsMember(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		ModifiersNode modifiers = chainsaw.cut(TreeKind.MODIFIERS, this::visit);
		chainsaw.cut(ZSTokenType.K_IMPLEMENTS);
		TypeNode type = chainsaw.cut(TreeKind::isType, this::visit);
		List<DefinitionMemberNode> members = new ArrayList<>();
		DefinitionMemberNode member = chainsaw.cut(TreeKind::isDefinitionMember, this::visit);
		while (member != null) {
			members.add(member);
			member = chainsaw.cut(TreeKind::isDefinitionMember, this::visit);
		}
		return new ImplementsNode(tree.position(this.source), null, modifiers, type, members);
	}

	@Override
	public OperatorNode visitOperatorMember(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		ModifiersNode modifiers = chainsaw.cut(TreeKind.MODIFIERS, this::visit);
		//TODO this needs rewriting to just not use this code at all, it needs to handle things like [], []=, ()
		OperatorType operator = chainsaw.cutToken(token -> getOperator(token.getType()));
		FunctionHeaderNode header = chainsaw.cut(TreeKind.FUNCTION_HEADER, this::visit);
		ASTNode body = visitFunctionBody(tree, chainsaw);
		return new OperatorNode(tree.position(this.source), null, modifiers, operator, header, body);
	}

	@Override
	public DestructorNode visitDestructorMember(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		ModifiersNode modifiers = chainsaw.cut(TreeKind.MODIFIERS, this::visit);
		OperatorType operator = chainsaw.cutToken(token -> getOperator(token.getType()));
		FunctionHeaderNode header = chainsaw.cut(TreeKind.FUNCTION_HEADER, this::visit);
		ASTNode body = visitFunctionBody(tree, chainsaw);
		return new DestructorNode(tree.position(this.source), null, modifiers, operator, header, body);
	}

	@Override
	public IteratorNode visitIteratorMember(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		ModifiersNode modifiers = chainsaw.cut(TreeKind.MODIFIERS, this::visit);
		List<TypeNode> variableTypes = new ArrayList<>();
//TODO fill in variable types
		ASTNode body = visitFunctionBody(tree, chainsaw);
		return new IteratorNode(tree.position(this.source), null, modifiers, variableTypes, body);
	}

	@Override
	public ASTNode visitStaticInitializerMember(Tree tree, Void context) {
		return null;
	}

	@Override
	public ReturnNode visitReturnStatement(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		chainsaw.cut(ZSTokenType.K_RETURN);
		ExpressionNode expression = chainsaw.cut(TreeKind::isExpression, this::visit);
		return new ReturnNode(tree.position(this.source), expression);
	}

	private List<AnnotationNode> collectAnnotations(Tree tree) {
		List<AnnotationNode> annotations = new ArrayList<>();
		for (Child child : tree.children()) {
			if (child.isTree() && child.asTree().kind() == TreeKind.ANNOTATION) {
				annotations.add(visit(child.asTree()));
			}
		}
		return annotations;
	}

	private List<AnnotationNode> collectAnnotations(Chainsaw chainsaw) {
		List<AnnotationNode> annotations = new ArrayList<>();

		AnnotationNode ann;
		do {
			ann = chainsaw.cut(TreeKind.ANNOTATION, this::visit);
			if (ann != null) {
				annotations.add(ann);
			}
		} while (ann != null);
		return annotations;
	}

	private <T extends ASTNode> T visit(Tree tree) {
		ASTNode accept = tree.accept(this, null);
		if (accept != null) {
			return accept.cast();
		}
		return null;
	}

	@Override
	public VarStatementNode visitVarStatement(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		List<AnnotationNode> annotations = collectAnnotations(chainsaw);
		chainsaw.cut(ZSTokenType.K_VAR);
		NameNode name = chainsaw.cut(TreeKind.NAME, this::visit);
		TypeNode type = chainsaw.cut(TreeKind.TYPE_DECLARATION, this::visit);
		chainsaw.cut(ZSTokenType.T_ASSIGN);
		ExpressionNode expression = chainsaw.cut(TreeKind::isExpression, this::visit);
		return new VarStatementNode(tree.position(this.source), annotations, name, type, expression, false);
	}

	@Override
	public VarStatementNode visitValStatement(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		List<AnnotationNode> annotations = collectAnnotations(chainsaw);
		chainsaw.cut(ZSTokenType.K_VAL);
		NameNode name = chainsaw.cut(TreeKind.NAME, this::visit);
		TypeNode type = chainsaw.cut(TreeKind.TYPE_DECLARATION, this::visit);
		chainsaw.cut(ZSTokenType.T_ASSIGN);
		ExpressionNode expression = chainsaw.cut(TreeKind::isExpression, this::visit);
		return new VarStatementNode(tree.position(this.source), annotations, name, type, expression, true);
	}

	@Override
	public IfNode visitIfStatement(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);

		chainsaw.cut(ZSTokenType.K_IF);
		ExpressionNode condition = chainsaw.cut(TreeKind::isExpression, this::visit);
		ASTNode body = chainsaw.cut(treeKind -> true, this::visit);
		ElseNode elseNode = chainsaw.cut(TreeKind.STMT_ELSE, this::visit);
		return new IfNode(tree.position(this.source), condition, body, elseNode);
	}

	@Override
	public ASTNode visitElseStatement(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		chainsaw.cut(ZSTokenType.K_ELSE);
		ASTNode body = chainsaw.cut(treeKind -> true, this::visit);
		return new ElseNode(tree.position(this.source), body);
	}

	@Override
	public ForNode visitForStatement(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);

		chainsaw.cut(ZSTokenType.K_FOR);
		ForKeysNode keys = chainsaw.cut(TreeKind.FOR_NAMES, this::visit);
		chainsaw.cut(ZSTokenType.K_IN);
		ExpressionNode value = chainsaw.cut(TreeKind::isExpression, this::visit);
		ASTNode body = chainsaw.cut(treeKind -> true, this::visit);
		return new ForNode(tree.position(this.source), keys, value, body);
	}

	@Override
	public DoWhileNode visitDoWhileStatement(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		chainsaw.cut(ZSTokenType.K_DO);

		chainsaw.cut(ZSTokenType.T_COLON);
		LabelNode label = chainsaw.cut(TreeKind.LABEL, this::visit);
		ASTNode body = chainsaw.cut(treeKind -> true, this::visit);
		chainsaw.cut(ZSTokenType.K_WHILE);
		ExpressionNode condition = chainsaw.cut(TreeKind::isExpression, this::visit);

		return new DoWhileNode(tree.position(this.source), label, body, condition);
	}

	@Override
	public WhileNode visitWhileStatement(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		chainsaw.cut(ZSTokenType.K_WHILE);

		chainsaw.cut(ZSTokenType.T_COLON);
		LabelNode label = chainsaw.cut(TreeKind.LABEL, this::visit);
		ExpressionNode condition = chainsaw.cut(TreeKind::isExpression, this::visit);
		ASTNode body = chainsaw.cut(treeKind -> true, this::visit);
		return new WhileNode(tree.position(this.source), label, condition, body);
	}

	@Override
	public LockNode visitLockStatement(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);

		chainsaw.cut(ZSTokenType.K_LOCK);
		ExpressionNode object = chainsaw.cut(TreeKind::isExpression, this::visit);
		ASTNode body = chainsaw.cut(treeKind -> true, this::visit);
		return new LockNode(tree.position(this.source), object, body);
	}

	@Override
	public ThrowStatementNode visitThrowStatement(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);

		chainsaw.cut(ZSTokenType.K_THROW);

		ExpressionNode expression = chainsaw.cut(TreeKind::isExpression, this::visit);
		return new ThrowStatementNode(tree.position(this.source), expression);
	}

	@Override
	public TryCatchNode visitTryStatement(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		chainsaw.cut(ZSTokenType.K_TRY);
		NameNode resourceName = chainsaw.cut(TreeKind.NAME, this::visit);
		ExpressionNode resourceInit = null;
		if (resourceName != null) {
			chainsaw.cut(ZSTokenType.T_ASSIGN);
			resourceInit = chainsaw.cut(TreeKind::isExpression, this::visit);
		}
		ASTNode body = chainsaw.cut(treeKind -> true, this::visit);
		List<TryCatchNode.CatchClauseNode> catches = new ArrayList<>();

		ASTNode finallyNode = null;
		while (true) {
			ZSTokenType type = chainsaw.cut(new ZSTokenType[]{ZSTokenType.K_CATCH, ZSTokenType.K_FINALLY}, null, token -> token.getType());
			if (type == ZSTokenType.K_CATCH) {
				NameNode name = chainsaw.cut(TreeKind.NAME, this::visit);
				ASTNode catchBody = chainsaw.cut(t -> true, this::visit);
				catches.add(new TryCatchNode.CatchClauseNode(name, catchBody));
			} else if (type == ZSTokenType.K_FINALLY) {
				finallyNode = chainsaw.cut(treeKind -> true, this::visit);
			} else {
				break;
			}
		}

		return new TryCatchNode(tree.position(this.source), resourceName, resourceInit, body, catches, finallyNode);
	}

	@Override
	public ContinueNode visitContinueStatement(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		chainsaw.cut(ZSTokenType.K_CONTINUE);
		NameNode name = chainsaw.cut(TreeKind.NAME, this::visit);
		return new ContinueNode(tree.position(this.source), name);
	}

	@Override
	public BreakNode visitBreakStatement(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		chainsaw.cut(ZSTokenType.K_BREAK);
		NameNode name = chainsaw.cut(TreeKind.NAME, this::visit);
		return new BreakNode(tree.position(this.source), name);
	}

	@Override
	public SwitchNode visitSwitchStatement(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		chainsaw.cut(ZSTokenType.K_SWITCH);
		LabelNode label = chainsaw.cut(TreeKind.LABEL, this::visit);
		ExpressionNode value = chainsaw.cut(TreeKind::isExpression, this::visit);
		SwitchNode.SwitchCasesNode cases = chainsaw.cut(TreeKind.SWITCH_CASES, this::visit);
		return new SwitchNode(tree.position(this.source), label, value, cases);
	}

	@Override
	public AssignNode visitAssignExpression(Tree tree, Void context) {
		return visitBinaryExpression(tree, ZSTokenType.T_ASSIGN, AssignNode::new);
	}

	@Override
	public AddAssignNode visitAddAssignExpression(Tree tree, Void context) {
		return visitBinaryExpression(tree, ZSTokenType.T_ADDASSIGN, AddAssignNode::new);
	}

	@Override
	public SubAssignNode visitSubAssignExpression(Tree tree, Void context) {
		return visitBinaryExpression(tree, ZSTokenType.T_SUBASSIGN, SubAssignNode::new);
	}

	@Override
	public CatAssignNode visitCatAssignExpression(Tree tree, Void context) {
		return visitBinaryExpression(tree, ZSTokenType.T_CATASSIGN, CatAssignNode::new);
	}

	@Override
	public MulAssignNode visitMulAssignExpression(Tree tree, Void context) {
		return visitBinaryExpression(tree, ZSTokenType.T_MULASSIGN, MulAssignNode::new);
	}

	@Override
	public DivAssignNode visitDivAssignExpression(Tree tree, Void context) {
		return visitBinaryExpression(tree, ZSTokenType.T_DIVASSIGN, DivAssignNode::new);
	}

	@Override
	public ModAssignNode visitModAssignExpression(Tree tree, Void context) {
		return visitBinaryExpression(tree, ZSTokenType.T_MODASSIGN, ModAssignNode::new);
	}

	@Override
	public OrAssignNode visitOrAssignExpression(Tree tree, Void context) {
		return visitBinaryExpression(tree, ZSTokenType.T_ORASSIGN, OrAssignNode::new);
	}

	@Override
	public AndAssignNode visitAndAssignExpression(Tree tree, Void context) {
		return visitBinaryExpression(tree, ZSTokenType.T_ANDASSIGN, AndAssignNode::new);
	}

	@Override
	public XorAssignNode visitXorAssignExpression(Tree tree, Void context) {
		return visitBinaryExpression(tree, ZSTokenType.T_XORASSIGN, XorAssignNode::new);
	}

	@Override
	public SHLAssignNode visitSHLAssignExpression(Tree tree, Void context) {
		return visitBinaryExpression(tree, ZSTokenType.T_SHLASSIGN, SHLAssignNode::new);
	}

	@Override
	public SHRAssignNode visitSHRAssignExpression(Tree tree, Void context) {
		return visitBinaryExpression(tree, ZSTokenType.T_SHRASSIGN, SHRAssignNode::new);
	}

	@Override
	public USHRAssignNode visitUSHRAssignExpression(Tree tree, Void context) {
		return visitBinaryExpression(tree, ZSTokenType.T_USHRASSIGN, USHRAssignNode::new);
	}

	@Override
	public ConditionalNode visitConditionalExpression(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		ExpressionNode condition = chainsaw.cut(TreeKind::isExpression, this::visit);
		chainsaw.cut(ZSTokenType.T_QUEST);
		ExpressionNode ifThen = chainsaw.cut(TreeKind::isExpression, this::visit);
		chainsaw.cut(ZSTokenType.T_COLON);
		ExpressionNode ifElse = chainsaw.cut(TreeKind::isExpression, this::visit);
		return new ConditionalNode(tree.position(this.source), condition, ifThen, ifElse);
	}

	@Override
	public OrOrNode visitOrOrExpression(Tree tree, Void context) {
		return visitBinaryExpression(tree, ZSTokenType.T_OROR, OrOrNode::new);
	}

	@Override
	public CoalesceNode visitCoalesceExpression(Tree tree, Void context) {
		return visitBinaryExpression(tree, ZSTokenType.T_COALESCE, CoalesceNode::new);
	}

	@Override
	public AndAndNode visitAndAndExpression(Tree tree, Void context) {
		return visitBinaryExpression(tree, ZSTokenType.T_ANDAND, AndAndNode::new);
	}

	@Override
	public OrNode visitOrExpression(Tree tree, Void context) {
		return visitBinaryExpression(tree, ZSTokenType.T_OR, OrNode::new);
	}

	@Override
	public XorNode visitXorExpression(Tree tree, Void context) {
		return visitBinaryExpression(tree, ZSTokenType.T_XOR, XorNode::new);
	}

	@Override
	public AndNode visitAndExpression(Tree tree, Void context) {
		return visitBinaryExpression(tree, ZSTokenType.T_AND, AndNode::new);
	}

	@Override
	public EqualNode visitEqualExpression(Tree tree, Void context) {
		return visitBinaryExpression(tree, ZSTokenType.T_EQUAL2, EqualNode::new);
	}

	@Override
	public SameNode visitSameExpression(Tree tree, Void context) {
		return visitBinaryExpression(tree, ZSTokenType.T_EQUAL3, SameNode::new);
	}

	@Override
	public NotEqualNode visitNotEqualExpression(Tree tree, Void context) {
		return visitBinaryExpression(tree, ZSTokenType.T_NOTEQUAL, NotEqualNode::new);
	}

	@Override
	public NotSameNode visitNotSameExpression(Tree tree, Void context) {
		return visitBinaryExpression(tree, ZSTokenType.T_NOTEQUAL2, NotSameNode::new);
	}

	@Override
	public LessThanNode visitLessThanExpression(Tree tree, Void context) {
		return visitBinaryExpression(tree, ZSTokenType.T_LESS, LessThanNode::new);
	}

	@Override
	public LessThanEqualToNode visitLessThanEqualToExpression(Tree tree, Void context) {
		return visitBinaryExpression(tree, ZSTokenType.T_LESSEQ, LessThanEqualToNode::new);
	}

	@Override
	public GreaterNode visitGreaterThanExpression(Tree tree, Void context) {
		return visitBinaryExpression(tree, ZSTokenType.T_GREATER, GreaterNode::new);
	}

	@Override
	public GreaterThanEqualToNode visitGreaterThanEqualToExpression(Tree tree, Void context) {
		return visitBinaryExpression(tree, ZSTokenType.T_GREATEREQ, GreaterThanEqualToNode::new);
	}

	@Override
	public ContainsNode visitContainsExpression(Tree tree, Void context) {
		return visitBinaryExpression(tree, ZSTokenType.K_IN, ContainsNode::new);
	}

	@Override
	public IsNode visitIsExpression(Tree tree, Void context) {

		Chainsaw chainsaw = new Chainsaw(tree);

		ExpressionNode expression = chainsaw.cut(TreeKind::isExpression, this::visit);
		chainsaw.cut(ZSTokenType.K_IS);
		TypeNode type = chainsaw.cut(TreeKind::isType, this::visit);
		return new IsNode(tree.position(this.source), expression, type);
	}

	@Override
	public NotContainsNode visitNotInExpression(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		ExpressionNode left = chainsaw.cut(TreeKind::isExpression, this::visit);
		chainsaw.cut(ZSTokenType.T_NOT);
		chainsaw.cut(ZSTokenType.K_IN);
		ExpressionNode right = chainsaw.cut(TreeKind::isExpression, this::visit);
		return new NotContainsNode(tree.position(this.source), left, right);
	}

	@Override
	public NotIsNode visitNotIsExpression(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		ExpressionNode left = chainsaw.cut(TreeKind::isExpression, this::visit);
		chainsaw.cut(ZSTokenType.T_NOT);
		chainsaw.cut(ZSTokenType.K_IS);
		TypeNode right = chainsaw.cut(TreeKind::isType, this::visit);
		return new NotIsNode(tree.position(this.source), left, right);
	}

	@Override
	public SHLNode visitSHLExpression(Tree tree, Void context) {
		return visitBinaryExpression(tree, ZSTokenType.T_SHL, SHLNode::new);
	}

	@Override
	public SHRNode visitSHRExpression(Tree tree, Void context) {
		return visitBinaryExpression(tree, ZSTokenType.T_SHR, SHRNode::new);
	}

	@Override
	public USHRNode visitUSHRExpression(Tree tree, Void context) {
		return visitBinaryExpression(tree, ZSTokenType.T_USHR, USHRNode::new);
	}

	public <T extends ExpressionNode> T visitUnaryExpression(Tree tree, Function<ExpressionNode, T> generator) {
		Chainsaw chainsaw = new Chainsaw(tree);
		ExpressionNode value = chainsaw.cut(TreeKind::isExpression, this::visit);
		return generator.apply(value);
	}

	@FunctionalInterface
	private interface BinaryFactory<T extends ExpressionNode> {
		T create(CodePosition position, ExpressionNode left, ExpressionNode right);
	}

	private <T extends ExpressionNode> T visitBinaryExpression(Tree tree, ZSTokenType operator, BinaryFactory<T> generator) {
		Chainsaw chainsaw = new Chainsaw(tree);
		ExpressionNode left = chainsaw.cut(TreeKind::isExpression, this::visit);
		chainsaw.cut(operator);
		ExpressionNode right = chainsaw.cut(TreeKind::isExpression, this::visit);
		return generator.create(tree.position(this.source), left, right);
	}

	@Override
	public AddNode visitAddExpression(Tree tree, Void context) {
		return visitBinaryExpression(tree, ZSTokenType.T_ADD, AddNode::new);
	}

	@Override
	public SubNode visitSubExpression(Tree tree, Void context) {
		return visitBinaryExpression(tree, ZSTokenType.T_SUB, SubNode::new);
	}

	@Override
	public CatNode visitCatExpression(Tree tree, Void context) {
		return visitBinaryExpression(tree, ZSTokenType.T_CAT, CatNode::new);
	}

	@Override
	public MulNode visitMulExpression(Tree tree, Void context) {
		return visitBinaryExpression(tree, ZSTokenType.T_MUL, MulNode::new);
	}

	@Override
	public DivNode visitDivExpression(Tree tree, Void context) {
		return visitBinaryExpression(tree, ZSTokenType.T_DIV, DivNode::new);
	}

	@Override
	public ModNode visitModExpression(Tree tree, Void context) {
		return visitBinaryExpression(tree, ZSTokenType.T_MOD, ModNode::new);
	}

	@Override
	public NotNode visitNotExpression(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		chainsaw.cut(ZSTokenType.T_NOT);
		ExpressionNode expr = chainsaw.cut(TreeKind::isExpression, this::visit);
		return new NotNode(tree.position(this.source), expr);
	}

	@Override
	public ExpressionNode visitNegExpression(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		chainsaw.cut(ZSTokenType.T_SUB);
		ExpressionNode expr = chainsaw.cut(TreeKind::isExpression, this::visit);
		return new NegNode(tree.position(this.source), expr);
	}

	@Override
	public InvertNode visitInvertExpression(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		chainsaw.cut(ZSTokenType.T_CAT);

		ExpressionNode cut = chainsaw.cut(TreeKind::isExpression, this::visit);
		return new InvertNode(tree.position(this.source), cut);
	}

	@Override
	public TryConvertNode visitTryConvertExpression(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		chainsaw.cut(ZSTokenType.K_TRY);
		chainsaw.cut(ZSTokenType.T_QUEST);
		ExpressionNode expr = chainsaw.cut(TreeKind::isExpression, this::visit);
		return new TryConvertNode(tree.position(this.source), expr);
	}

	@Override
	public TryRethrowNode visitTryRethrowExpression(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		chainsaw.cut(ZSTokenType.K_TRY);
		chainsaw.cut(ZSTokenType.T_NOT);
		ExpressionNode expr = chainsaw.cut(TreeKind::isExpression, this::visit);
		return new TryRethrowNode(tree.position(this.source), expr);
	}

	@Override
	public MemberNode visitMemberExpression(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		ExpressionNode expression = chainsaw.cut(TreeKind::isExpression, this::visit);
		chainsaw.cut(ZSTokenType.T_DOT);
		NameNode member = chainsaw.cut(new ZSTokenType[]{ZSTokenType.T_STRING_DQ, ZSTokenType.T_STRING_SQ}, NameNode::new);
		if (member == null) {
			member = chainsaw.cut(TreeKind.NAME, this::visit);
		}
		TypeArgumentsNode generics = chainsaw.cut(TreeKind.TYPE_ARGS, this::visit);
		return new MemberNode(tree.position(this.source), expression, member, generics);
	}

	@Override
	public OuterNode visitOuterExpression(Tree tree, Void context) {
		return new OuterNode(tree.position(this.source), new Chainsaw(tree).cut(TreeKind::isExpression, this::visit));
	}

	@Override
	public RangeNode visitRangeExpression(Tree tree, Void context) {
		return visitBinaryExpression(tree, ZSTokenType.T_DOT2, RangeNode::new);
	}

	@Override
	public IndexNode visitIndexExpression(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);

		ExpressionNode value = chainsaw.cut(TreeKind::isExpression, this::visit);

		List<ExpressionNode> indices = new ArrayList<>();
		chainsaw.cut(ZSTokenType.T_SQOPEN);
		ExpressionNode key = chainsaw.cut(TreeKind.INDEX_KEY, this::visit);

		while (key != null) {
			indices.add(key);
			chainsaw.cut(ZSTokenType.T_COMMA);
			key = chainsaw.cut(TreeKind.INDEX_KEY, this::visit);
		}
		return new IndexNode(tree.position(this.source), value, indices);
	}

	@Override
	public ExpressionNode visitIndexKey(Tree tree, Void context) {
		return new Chainsaw(tree).cut(TreeKind::isExpression, this::visit);
	}

	@Override
	public CallNode visitCallExpression(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		ExpressionNode receiver = chainsaw.cut(TreeKind::isExpression, this::visit);
		CallArgumentsNode args = chainsaw.cut(TreeKind.CALL_ARGUMENTS, this::visit);
		return new CallNode(tree.position(this.source), receiver, args);
	}

	@Override
	public ASTNode visitCastExpression(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		ExpressionNode left = chainsaw.cut(TreeKind::isExpression, this::visit);
		chainsaw.cut(ZSTokenType.K_AS);
		boolean optional = chainsaw.cut(new ZSTokenType[]{ZSTokenType.T_QUEST}, false, token -> true);
		TypeNode type = chainsaw.cut(TreeKind::isType, this::visit);
		return new CastNode(tree.position(this.source), left, type, optional);
	}

	@Override
	public IncrementNode visitIncrementExpression(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		boolean pre = chainsaw.cut(new ZSTokenType[]{ZSTokenType.T_INCREMENT}, false, token -> true);
		ExpressionNode expression = chainsaw.cut(TreeKind::isExpression, this::visit);
		return new IncrementNode(tree.position(this.source), expression, pre);
	}

	@Override
	public DecrementNode visitDecrementExpression(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		boolean pre = chainsaw.cut(new ZSTokenType[]{ZSTokenType.T_DECREMENT}, false, token -> true);
		ExpressionNode expression = chainsaw.cut(TreeKind::isExpression, this::visit);
		return new DecrementNode(tree.position(this.source), expression, pre);
	}

	@Override
	public FunctionExpressionNode visitFunctionExpression(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		List<ParameterNode> parameters = new ArrayList<>();
		ExpressionNode expressionNode = chainsaw.cut(TreeKind::isExpression, this::visit);

		if (expressionNode instanceof VariableNode) {
			parameters.add(new ParameterNode(expressionNode.position(), ((VariableNode) expressionNode).name(), null, null, false));
		}
		if (expressionNode instanceof BracketNode) {
			List<ExpressionNode> expressions = ((BracketNode) expressionNode).expressions();
			for (ExpressionNode expression : expressions) {
				if (expression instanceof VariableNode) {
					parameters.add(new ParameterNode(expressionNode.position(), ((VariableNode) expression).name(), null, null, false));
				} else {
					// TODO this should error somehow
					return null;
				}
			}
		}

		chainsaw.cut(ZSTokenType.T_LAMBDA);
		ASTNode body = chainsaw.cut(treeKind -> true, this::visit);
		CodePosition position = tree.position(this.source);
		return new FunctionExpressionNode(position, new LambdaHeaderNode(position, null, new ParametersNode(position, parameters)), body);
	}

	@Override
	public IntNode visitIntExpression(Tree tree, Void context) {
		PositionedToken<ZSTokenType, ZSToken> token = tree.child(0).asToken();
		String content = token.getContent();

		int split = content.length();
		char c = content.charAt(split - 1);
		while (Character.isLetter(c) || c == '_') {
			split--;
		}

		boolean negative = content.charAt(0) == '-';
		long value = Long.parseLong(content.substring(0, split));
		String suffix = content.substring(split);
		return new IntNode(tree.position(this.source), negative, value, suffix);
	}

	@Override
	public IntNode visitPrefixedIntExpression(Tree tree, Void context) {
		PositionedToken<ZSTokenType, ZSToken> token = tree.child(0).asToken();
		String value = token.getContent();

		boolean negative = value.startsWith("-");
		if (negative)
			value = value.substring(1);

		String suffix = "";
		if (value.endsWith("u") || value.endsWith("l") || value.endsWith("U") || value.endsWith("L")) {
			suffix = value.substring(value.length() - 1);
			value = value.substring(0, value.length() - 1);
		} else if (value.endsWith("ul") || value.endsWith("UL")) {
			suffix = value.substring(value.length() - 2);
			value = value.substring(0, value.length() - 2);
		}

		value = value.toLowerCase();

		long parsed = 0;
		if (value.startsWith("0x")) {
			for (char c : value.substring(2).toCharArray()) {
				if (c >= '0' && c <= '9')
					parsed = parsed * 16 + (c - '0');
				else if (c >= 'a' && c <= 'f')
					parsed = parsed * 16 + 10 + (c - 'a');
				else if (c != '_')
					throw new NumberFormatException("Invalid number: " + value);
			}
		} else if (value.startsWith("0b")) {
			for (char c : value.substring(2).toCharArray()) {
				if (c == '0')
					parsed = parsed * 2;
				else if (c == '1')
					parsed = parsed * 2 + 1;
				else if (c != '_')
					throw new NumberFormatException("Invalid number: " + value);
			}
		} else if (value.startsWith("0o")) {
			for (char c : value.substring(2).toCharArray()) {
				if (c >= '0' && c <= '7')
					parsed = parsed * 8 + c - '0';
				else if (c != '_')
					throw new NumberFormatException("Invalid number: " + value);
			}
		} else {
			throw new NumberFormatException("Invalid number: " + value);
		}

		return new IntNode(tree.position(this.source), negative, negative ? -parsed : parsed, suffix);
	}

	@Override
	public FloatNode visitFloatExpression(Tree tree, Void context) {
		PositionedToken<ZSTokenType, ZSToken> token = tree.child(0).asToken();
		String content = token.getContent();

		int split = content.length();
		char c = content.charAt(split - 1);
		while (Character.isLetter(c) || c == '_') {
			split--;
		}

		double value = Double.parseDouble(content.substring(0, split));
		String suffix = content.substring(split);
		return new FloatNode(tree.position(this.source), value, suffix);
	}

	@Override
	public ASTNode visitStringExpression(Tree tree, Void context) {
		return new StringExpressionNode(tree.position(this.source), tree.child(0).asToken());
	}

	@Override
	public VariableNode visitVariableExpression(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		NameNode name = chainsaw.cut(TreeKind.NAME, this::visit);
		TypeArgumentsNode typeArgs = chainsaw.cut(TreeKind.TYPE_PARAMS, this::visit);
		return new VariableNode(tree.position(this.source), name, typeArgs);
	}

	@Override
	public LocalVariableNode visitLocalVariableExpression(Tree tree, Void context) {

		return new LocalVariableNode(tree.position(this.source), tree.child(0).asToken());
	}

	@Override
	public ThisNode visitThisExpression(Tree tree, Void context) {
		return new ThisNode(tree.position(this.source));
	}

	@Override
	public SuperNode visitSuperExpression(Tree tree, Void context) {
		return new SuperNode(tree.position(this.source));
	}

	@Override
	public DollarNode visitDollarExpression(Tree tree, Void context) {
		return new DollarNode(tree.position(this.source));
	}

	@Override
	public ArrayNode visitArrayExpression(Tree tree, Void context) {
		List<ExpressionNode> expressions = new ArrayList<>();
		Chainsaw chainsaw = new Chainsaw(tree);

		chainsaw.cut(ZSTokenType.T_SQOPEN);
		ExpressionNode last = chainsaw.cut(TreeKind::isExpression, this::visit);

		while (last != null) {
			expressions.add(last);
			if (chainsaw.cut(new ZSTokenType[]{ZSTokenType.T_COMMA}, false, token -> true)) {
				last = chainsaw.cut(TreeKind::isExpression, this::visit);
			} else {
				break;
			}
		}

		return new ArrayNode(tree.position(this.source), expressions);
	}

	@Override
	public MapNode visitMapExpression(Tree tree, Void context) {
		List<ExpressionNode> keys = new ArrayList<>();
		List<ExpressionNode> values = new ArrayList<>();
		Chainsaw chainsaw = new Chainsaw(tree);
		chainsaw.cut(ZSTokenType.T_AOPEN);

		do {
			ExpressionNode key = chainsaw.cut(TreeKind.MAP_KEY, this::visit);
			ExpressionNode value = chainsaw.cut(TreeKind.MAP_VALUE, this::visit);
			if (key != null && value != null) {
				keys.add(key);
				values.add(value);
			} else {
				break;
			}
		} while (chainsaw.cut(new ZSTokenType[]{ZSTokenType.T_COMMA}, false, token -> true));
		return new MapNode(tree.position(this.source), keys, values);
	}

	@Override
	public ExpressionNode visitMapKey(Tree tree, Void context) {
		return new Chainsaw(tree).cut(TreeKind::isExpression, this::visit);
	}

	@Override
	public ExpressionNode visitMapValue(Tree tree, Void context) {
		return new Chainsaw(tree).cut(TreeKind::isExpression, this::visit);
	}

	@Override
	public BoolNode visitBoolExpression(Tree tree, Void context) {
		return new BoolNode(tree.position(this.source), tree.child(0).asToken().getType() == ZSTokenType.K_TRUE);
	}

	@Override
	public NullNode visitNullExpression(Tree tree, Void context) {
		return new NullNode(tree.position(this.source));
	}

	@Override
	public BracketNode visitBracketExpression(Tree tree, Void context) {
		List<ExpressionNode> expressions = new ArrayList<>();
		Chainsaw chainsaw = new Chainsaw(tree);

		chainsaw.cut(ZSTokenType.T_BROPEN);
		ExpressionNode last = chainsaw.cut(TreeKind::isExpression, this::visit);

		while (last != null) {
			expressions.add(last);
			if (chainsaw.cut(new ZSTokenType[]{ZSTokenType.T_COMMA}, false, token -> true)) {
				last = chainsaw.cut(TreeKind::isExpression, this::visit);
			} else {
				break;
			}
		}

		return new BracketNode(tree.position(this.source), expressions);
	}

	@Override
	public NewNode visitNewExpression(Tree tree, Void context) {

		Chainsaw chainsaw = new Chainsaw(tree);

		chainsaw.cut(ZSTokenType.K_NEW);
		TypeNode type = chainsaw.cut(TreeKind::isType, this::visit);
		CallArgumentsNode args = chainsaw.cut(TreeKind.CALL_ARGUMENTS, this::visit);
		return new NewNode(tree.position(this.source), type, args);
	}

	@Override
	public ThrowNode visitThrowExpression(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		chainsaw.cut(ZSTokenType.K_THROW);
		ExpressionNode expression = chainsaw.cut(TreeKind::isExpression, this::visit);
		return new ThrowNode(tree.position(this.source), expression);
	}

	@Override
	public PanicNode visitPanicExpression(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		chainsaw.cut(ZSTokenType.K_PANIC);
		ExpressionNode expr = chainsaw.cut(TreeKind::isExpression, this::visit);
		return new PanicNode(tree.position(this.source), expr);
	}

	@Override
	public MatchNode visitMatchExpression(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		chainsaw.cut(ZSTokenType.K_MATCH);
		ExpressionNode value = chainsaw.cut(TreeKind::isExpression, this::visit);

		chainsaw.cut(ZSTokenType.T_AOPEN);

		List<MatchNode.CaseNode> cases = new ArrayList<>();

		ExpressionNode key = chainsaw.cut(TreeKind.MATCH_KEY, this::visit);
		while (key != null) {

			chainsaw.cut(ZSTokenType.T_LAMBDA);
			ExpressionNode caseValue = chainsaw.cut(TreeKind::isExpression, this::visit);

			chainsaw.cut(ZSTokenType.T_COMMA);
			cases.add(new MatchNode.CaseNode(key, caseValue));
			key = chainsaw.cut(TreeKind.MATCH_KEY, this::visit);
		}
		return new MatchNode(tree.position(this.source), value, cases);
	}

	@Override
	public ExpressionNode visitMatchKey(Tree tree, Void context) {

		Chainsaw chainsaw = new Chainsaw(tree);
		boolean isDefault = chainsaw.cut(new ZSTokenType[]{ZSTokenType.K_DEFAULT}, false, token -> true);
		if (isDefault) {
			//TODO
			return null;
		}
		return chainsaw.cut(TreeKind::isExpression, this::visit);
	}

	@Override
	public BEPNode visitBEP(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		List<NameNode> names = new ArrayList<>();
		chainsaw.cut(ZSTokenType.T_LESS);
		//TODO this currently does nothing, since we use identifier tokens, not name nodes
		do {
			names.add(chainsaw.cut(TreeKind.NAME, this::visit));
		} while (chainsaw.cut(new ZSTokenType[]{ZSTokenType.T_COLON}, false, token -> true));

		return new BEPNode(tree.position(this.source), names);
	}

	@Override
	public TypeExpressionNode visitTypeExpression(Tree tree, Void context) {

		return new TypeExpressionNode(tree.position(this.source), new Chainsaw(tree).cut(TreeKind::isType, this::visit));
	}

	@Override
	public CallArgumentsNode visitCallArguments(Tree tree, Void context) {
		Chainsaw chainsaw = new Chainsaw(tree);
		chainsaw.cut(ZSTokenType.T_BROPEN);
		List<ExpressionNode> args = new ArrayList<>();

		ExpressionNode last = chainsaw.cut(TreeKind.CALL_ARGUMENT, this::visit);

		while (last != null) {
			args.add(last);
			if (chainsaw.cut(new ZSTokenType[]{ZSTokenType.T_COMMA}, false, token -> true)) {
				last = chainsaw.cut(TreeKind.CALL_ARGUMENT, this::visit);
			} else {
				break;
			}
		}

		return new CallArgumentsNode(tree.position(this.source), args);
	}

	@Override
	public ExpressionNode visitCallArgument(Tree tree, Void context) {
		return new Chainsaw(tree).cut(TreeKind::isExpression, this::visit);
	}

	@Override
	public ASTNode visitComment(Tree tree, Void context) {
		return null;
	}

	@Override
	public ASTNode visitMultilineComment(Tree tree, Void context) {
		return null;
	}

	@Override
	public ASTNode visitScriptComment(Tree tree, Void context) {
		return null;
	}

	@Override
	public ASTNode visitWhitespace(Tree tree, Void context) {
		return null;
	}

	@Override
	public VariantOptionsNode visitVariants(Tree tree, Void context) {
		List<VariantOptionNode> variants = new ArrayList<>();
		Chainsaw chainsaw = new Chainsaw(tree);
		chainsaw.cut(ZSTokenType.T_AOPEN);
		VariantOptionNode option = chainsaw.cut(TreeKind.VARIANT_OPTION, this::visit);
		while (option != null) {
			chainsaw.cut(ZSTokenType.T_COMMA);
			variants.add(option);
			option = chainsaw.cut(TreeKind.VARIANT_OPTION, this::visit);
		}
		return new VariantOptionsNode(tree.position(this.source), variants);
	}

	//TODO move this somewhere more accessible
	private static OperatorType getOperator(ZSTokenType type) {
		switch (type) {
			case T_BROPEN:
				return OperatorType.CALL;
			case T_ADD:
				return OperatorType.ADD;
			case T_SUB:
				return OperatorType.SUB;
			case T_CAT:
				return OperatorType.CAT;
			case T_MUL:
				return OperatorType.MUL;
			case T_DIV:
				return OperatorType.DIV;
			case T_MOD:
				return OperatorType.MOD;
			case T_AND:
				return OperatorType.AND;
			case T_OR:
				return OperatorType.OR;
			case T_XOR:
				return OperatorType.XOR;
			case T_NOT:
				return OperatorType.NOT;
			case T_ADDASSIGN:
				return OperatorType.ADDASSIGN;
			case T_SUBASSIGN:
				return OperatorType.SUBASSIGN;
			case T_CATASSIGN:
				return OperatorType.CATASSIGN;
			case T_MULASSIGN:
				return OperatorType.MULASSIGN;
			case T_DIVASSIGN:
				return OperatorType.DIVASSIGN;
			case T_MODASSIGN:
				return OperatorType.MODASSIGN;
			case T_ANDASSIGN:
				return OperatorType.ANDASSIGN;
			case T_ORASSIGN:
				return OperatorType.ORASSIGN;
			case T_XORASSIGN:
				return OperatorType.XORASSIGN;
			case T_INCREMENT:
				return OperatorType.INCREMENT;
			case T_DECREMENT:
				return OperatorType.DECREMENT;
			case T_DOT2:
				return OperatorType.RANGE;
			case T_SHL:
				return OperatorType.SHL;
			case T_SHR:
				return OperatorType.SHR;
			case T_USHR:
				return OperatorType.USHR;
			case T_SHLASSIGN:
				return OperatorType.SHLASSIGN;
			case T_SHRASSIGN:
				return OperatorType.SHRASSIGN;
			case T_USHRASSIGN:
				return OperatorType.USHRASSIGN;
			default:
				throw new AssertionError("Missing switch case in getOperator");
		}
	}
}
