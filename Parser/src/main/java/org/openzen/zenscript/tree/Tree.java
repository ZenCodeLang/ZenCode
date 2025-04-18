package org.openzen.zenscript.tree;

import org.openzen.zencode.shared.SourceFile;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.lexer.ZSToken;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.tree.lexer.DFALexer;
import org.openzen.zenscript.tree.lexer.PositionedToken;
import org.openzen.zenscript.tree.lexer.ZSPosTokenFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Tree {

	private TreeKind kind;
	private List<Child> children;

	public Tree(TreeKind kind, List<Child> children) {
		this.kind = kind;
		this.children = children;
	}

	public Tree(TreeKind kind) {
		this.kind = kind;
		this.children = new ArrayList<>();
	}

	public static Tree parse(SourceFile sourceFile) throws IOException, ParseException {
		DFALexer dfaLexer = new DFALexer(new ZSPosTokenFactory());
		List<PositionedToken<ZSTokenType, ZSToken>> tokens = dfaLexer.tokenize(sourceFile);
		return parse(tokens);
	}

	public static Tree parse(List<PositionedToken<ZSTokenType, ZSToken>> tokens) throws IOException, ParseException {
		TreeParser p = new TreeParser(tokens);
		p.file();
		return p.buildTree();
	}

	void print(StringBuilder buf, int level) {
		StringBuilder indent = new StringBuilder();
		for (int i = 0; i < level; i++) {
			indent.append("  ");
		}
		if (this.kind().isToken()) {
			buf.append(indent).append(kind);
			if (!this.children().isEmpty()) {
				buf.append(" \"").append(this.children().get(0).asToken().getContent().replaceAll("\r?\n", "\\\\n")).append("\"\n");
			}
			return;
		}
		if (this.children().size() == 1 && this.children().get(0).isToken()) {
			buf.append(indent).append(kind).append(" \"").append(this.children().get(0).asToken().getContent().replaceAll("\r?\n", "\\\\n")).append("\"\n");
			return;
		}

		buf.append(indent).append(kind).append("\n");
		for (Child child : children) {
			if (child.isToken()) {
				buf.append(" \"").append(child.asToken().getContent().replaceAll("\r?\n", "\\\\n")).append("\"\n");
			} else if (child.isTree()) {
				child.asTree().print(buf, level + 1);
			}
		}
	}

	public void reportErrors(List<ParseError> errors) {
		for (Child child : children) {
			if (child.isError()) {
				errors.add(child.asError());
			} else if (child.isTree()) {
				child.asTree().reportErrors(errors);
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		print(buf, 0);
		List<ParseError> errors = new ArrayList<>();
		reportErrors(errors);
		errors.forEach(e -> buf.append(e).append("\n"));
		return buf.toString();
	}

	public TreeKind kind() {
		return kind;
	}

	public List<Child> children() {
		return children;
	}
}
