package org.openzen.zenscript.tree;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.lexer.ZSToken;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.tree.lexer.PositionedToken;

public interface Child {

	static Child ofToken(PositionedToken<ZSTokenType, ZSToken> token) {
		return new TokenChild(token);
	}

	static Child ofTree(Tree tree) {
		return new TreeChild(tree);
	}

	static Child ofError(CodePosition position, String message) {
		return new ErrorChild(position, message);
	}

	boolean isError();

	boolean isToken();

	boolean isTree();

	PositionedToken<ZSTokenType, ZSToken> asToken();

	Tree asTree();

	ParseError asError();

	class TokenChild implements Child {
		private final PositionedToken<ZSTokenType, ZSToken> token;

		private TokenChild(PositionedToken<ZSTokenType, ZSToken> token) {
			this.token = token;
		}

		@Override
		public boolean isError() {
			return false;
		}

		@Override
		public boolean isToken() {
			return true;
		}

		@Override
		public boolean isTree() {
			return false;
		}

		@Override
		public PositionedToken<ZSTokenType, ZSToken> asToken() {
			return this.token;
		}

		@Override
		public Tree asTree() {
			throw new IllegalStateException("Unable to convert a token to a tree!");
		}

		@Override
		public ParseError asError() {
			throw new IllegalStateException("Unable to convert a token to an error	!");
		}
	}

	class TreeChild implements Child {
		private final Tree tree;

		private TreeChild(Tree tree) {
			this.tree = tree;
		}

		@Override
		public boolean isError() {
			return false;
		}

		@Override
		public boolean isToken() {
			return false;
		}

		@Override
		public boolean isTree() {
			return true;
		}

		@Override
		public PositionedToken<ZSTokenType, ZSToken> asToken() {
			throw new IllegalStateException("Unable to convert a tree to a token!");
		}

		@Override
		public Tree asTree() {
			return tree;
		}

		@Override
		public ParseError asError() {
			throw new IllegalStateException("Unable to convert a tree to an error!");
		}
	}

	class ErrorChild implements Child {
		private final CodePosition position;
		private final String message;

		public ErrorChild(CodePosition position, String message) {
			this.position = position;
			this.message = message;
		}

		@Override
		public boolean isError() {
			return true;
		}

		@Override
		public boolean isToken() {
			return false;
		}

		@Override
		public boolean isTree() {
			return false;
		}

		@Override
		public PositionedToken<ZSTokenType, ZSToken> asToken() {
			throw new IllegalStateException("Unable to convert an error to a token!");
		}


		@Override
		public Tree asTree() {
			throw new IllegalStateException("Unable to convert an error to a tree!");
		}

		@Override
		public ParseError asError() {
			return new ParseError(this.position, this.message);
		}
	}
}
