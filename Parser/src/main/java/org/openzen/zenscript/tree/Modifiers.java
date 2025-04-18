package org.openzen.zenscript.tree;

import org.openzen.zenscript.lexer.ZSTokenType;

import static org.openzen.zenscript.lexer.ZSTokenType.*;

class Modifiers {

	static TreeParser.MarkClosed parseDefinitionModifiers(TreeParser p) {
		ZSTokenType[] modifiers = {K_PUBLIC, K_PRIVATE, K_INTERNAL, K_EXTERN, K_ABSTRACT, K_FINAL, K_PROTECTED, K_IMPLICIT, K_VIRTUAL};
		if (p.atAny(modifiers)) {

			TreeParser.MarkOpened open = p.open();
			while (p.atAny(modifiers)) {
				p.expect(modifiers);
				p.whitespace();
			}

			return p.close(open, TreeKind.MODIFIERS);
		}
		return null;
	}

	static ModifierClose parseDefinitionMemberModifiers(TreeParser p) {
		org.openzen.zenscript.codemodel.Modifiers modifiers = org.openzen.zenscript.codemodel.Modifiers.NONE;
		ZSTokenType[] modifiersTokens = {
				K_INTERNAL,
				K_PUBLIC,
				K_PRIVATE,
				K_CONST,
				K_ABSTRACT,
				K_FINAL,
				K_STATIC,
				K_PROTECTED,
				K_IMPLICIT,
				K_EXTERN,
				K_OVERRIDE
		};
		TreeParser.MarkClosed modifierClose = null;
		if (p.atAny(modifiersTokens)) {
			TreeParser.MarkOpened modifierOpen = p.open();
			while (p.atAny(modifiersTokens) && !p.eof()) {
				switch (p.nth(0)) {
					case K_INTERNAL:
						modifiers = modifiers.withInternal();
						break;
					case K_PUBLIC:
						modifiers = modifiers.withPublic();
						break;
					case K_PRIVATE:
						modifiers = modifiers.withPrivate();
						break;
					case K_CONST:
						modifiers = modifiers.withConst();
						break;
					case K_ABSTRACT:
						modifiers = modifiers.withAbstract();
						break;
					case K_FINAL:
						modifiers = modifiers.withFinal();
						break;
					case K_STATIC:
						modifiers = modifiers.withStatic();
						break;
					case K_PROTECTED:
						modifiers = modifiers.withProtected();
						break;
					case K_IMPLICIT:
						modifiers = modifiers.withImplicit();
						break;
					case K_EXTERN:
						modifiers = modifiers.withExtern();
						break;
					case K_OVERRIDE:
						modifiers = modifiers.withOverride();
						break;

				}
				p.expect(modifiersTokens);
				p.whitespace();
			}
			modifierClose = p.close(modifierOpen, TreeKind.MODIFIERS);
		}
		return new ModifierClose(modifierClose, modifiers);
	}

	public static class ModifierClose {
		private final TreeParser.MarkClosed close;
		private final org.openzen.zenscript.codemodel.Modifiers modifiers;

		public ModifierClose(TreeParser.MarkClosed close, org.openzen.zenscript.codemodel.Modifiers modifiers) {
			this.close = close;
			this.modifiers = modifiers;
		}

		public TreeParser.MarkClosed close() {
			return close;
		}

		public org.openzen.zenscript.codemodel.Modifiers modifiers() {
			return modifiers;
		}
	}
}
