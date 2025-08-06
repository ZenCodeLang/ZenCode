package org.openzen.zenscript.tree.visitor;

import org.openzen.zenscript.lexer.ZSToken;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.tree.Child;
import org.openzen.zenscript.tree.Tree;
import org.openzen.zenscript.tree.TreeKind;
import org.openzen.zenscript.tree.lexer.PositionedToken;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class Chainsaw {
	private final Tree tree;
	private int index = 0;
	private final List<Child> children;

	public Chainsaw(Tree tree) {
		this.tree = tree;
		this.children = tree.filterWhitespaceChildren();
	}

	public Tree cut(TreeKind kind) {
		return cut(kind, tree1 -> tree1);
	}

	public PositionedToken<ZSTokenType, ZSToken> cut(ZSTokenType... type) {
		return cut(type, token -> token);
	}

	public <T> T cut(Predicate<TreeKind> kind, Function<Tree, T> transformer) {
		if (index >= children.size()) {
			return null;
		}
		Child child = children.get(index);
		if (child.isTree() && kind.test(child.asTree().kind())) {
			index++;
			return transformer.apply(child.asTree());
		}
		return null;
	}

//	public void cut(Predicate<TreeKind> kind, Consumer<Tree> consumer) {
//		if (index >= children.size()) {
//			return;
//		}
//		Child child = children.get(index);
//		if (child.isTree() && kind.test(child.asTree().kind())) {
//			index++;
//			consumer.accept(child.asTree());
//		}
//	}

	public <T> T cut(TreeKind kind, Function<Tree, T> transformer) {
		if (index >= children.size()) {
			return null;
		}
		Child child = children.get(index);
		if (child.isTree() && child.asTree().kind() == kind) {
			index++;
			return transformer.apply(child.asTree());
		}
		return null;
	}

	public <T> T cutToken(Function<PositionedToken<ZSTokenType, ZSToken>, T> transformer) {
		if (index >= children.size()) {
			return null;
		}
		Child child = children.get(index);
		if (child.isToken()) {
			index++;
			return transformer.apply(child.asToken());

		}
		return null;
	}

	public <T> T cut(ZSTokenType type, Function<PositionedToken<ZSTokenType, ZSToken>, T> transformer) {
		if (index >= children.size()) {
			return null;
		}
		Child child = children.get(index);
		if (child.isToken()) {
			if (child.asToken().getType() == type) {
				index++;
				return transformer.apply(child.asToken());
			}

		}
		return null;
	}

	public <T> T cut(ZSTokenType[] type, Function<PositionedToken<ZSTokenType, ZSToken>, T> transformer) {
		return cut(type, null, transformer);
	}

	public <T> T cut(ZSTokenType[] type, T defaultValue, Function<PositionedToken<ZSTokenType, ZSToken>, T> transformer) {
		if (index >= children.size()) {
			return defaultValue;
		}
		Child child = children.get(index);
		if (child.isToken()) {
			for (ZSTokenType tokenType : type) {
				if (child.asToken().getType() == tokenType) {
					index++;
					return transformer.apply(child.asToken());
				}
			}

		}
		return defaultValue;
	}

}