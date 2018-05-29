/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view.editor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.openzen.drawablegui.DCanvas;
import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DDimensionPreferences;
import org.openzen.drawablegui.DDrawingContext;
import org.openzen.drawablegui.DFont;
import org.openzen.drawablegui.DFontFamily;
import org.openzen.drawablegui.DFontMetrics;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.DKeyEvent;
import org.openzen.drawablegui.DKeyEvent.KeyCode;
import org.openzen.drawablegui.DMouseEvent;
import org.openzen.drawablegui.DRectangle;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.live.LiveObject;
import org.openzen.drawablegui.live.SimpleLiveObject;
import org.openzen.zenscript.ide.host.IDESourceFile;
import org.openzen.zenscript.lexer.ReaderCharReader;
import org.openzen.zenscript.lexer.TokenParser;
import org.openzen.zenscript.lexer.ZSToken;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;

/**
 *
 * @author Hoofdgebruiker
 */
public class SourceEditor implements DComponent {
	private final SourceEditorStyle style = SourceEditorStyle.DEFAULT;
	private final DFont font = new DFont(DFontFamily.CODE, false, false, false, 24);
	private final LiveObject<DDimensionPreferences> dimensionPreferences = new SimpleLiveObject<>(new DDimensionPreferences(0, 0));
	private final String tab = "    ";
	private final IDESourceFile sourceFile;
	
	private DRectangle bounds;
	private DDrawingContext context;
	private DFontMetrics fontMetrics;
	private int textLineHeight;
	private int fullLineHeight;
	private int selectionLineHeight;
	private final List<Line> lines = new ArrayList<>();
	
	private int lineBarWidth;
	private CursorPosition cursorStart = null;
	private CursorPosition cursorEnd = null;
	
	private Timer blink = new Timer();
	private boolean cursorBlink = true;
	
	private int mouseDownX = -1;
	private int mouseDownY = -1;
	private boolean dragging = false;
	
	public SourceEditor(IDESourceFile sourceFile) {
		this.sourceFile = sourceFile;
		
		blink.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				blink();
			}
		}, 300, 300);
		
		try {
			parse();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void setContext(DDrawingContext context) {
		this.context = context;
		fontMetrics = context.getFontMetrics(font);
		textLineHeight = fontMetrics.getAscent() + fontMetrics.getDescent();
		fullLineHeight = textLineHeight + fontMetrics.getLeading() + style.extraLineSpacing;
		selectionLineHeight = textLineHeight + style.selectionPaddingTop + style.selectionPaddingBottom;
		
		dimensionPreferences.setValue(new DDimensionPreferences(0, fullLineHeight * lines.size()));
	}

	@Override
	public LiveObject<DDimensionPreferences> getDimensionPreferences() {
		return dimensionPreferences;
	}

	@Override
	public DRectangle getBounds() {
		return bounds;
	}

	@Override
	public void setBounds(DRectangle bounds) {
		this.bounds = bounds;
	}

	@Override
	public void paint(DCanvas canvas) {
		DIRectangle canvasBounds = canvas.getBounds();
		canvas.fillRectangle(bounds.x, bounds.y, bounds.width, bounds.height, 0xFFFFFFFF);
		
		lineBarWidth = Math.max(50, fontMetrics.getWidth(Integer.toString(lines.size()))) + 5;
		canvas.fillRectangle(bounds.x, bounds.y, lineBarWidth, bounds.height, 0xFFE9E8E2);
		canvas.strokePath(tracer -> {
			tracer.moveTo(bounds.x + lineBarWidth, bounds.y);
			tracer.lineTo(bounds.x + lineBarWidth, bounds.y + bounds.height);
		}, DTransform2D.IDENTITY, 0xFFA0A0A0, 1);
		
		int x = bounds.x + lineBarWidth + 10;
		if (cursorEnd != null)
			canvas.fillRectangle(x, lineToY(cursorEnd.line), bounds.width - x, selectionLineHeight, style.currentLineHighlight);
		
		if (cursorStart != null && !cursorStart.equals(cursorEnd)) {
			if (cursorStart.line == cursorEnd.line) {
				int y = cursorStart.getY();
				int x1 = cursorStart.getX();
				int x2 = cursorEnd.getX();
				int fromX = Math.min(x1, x2);
				int toX = Math.max(x1, x2);
				canvas.fillRectangle(fromX, y, toX - fromX, selectionLineHeight, style.selectionColor);
			} else {
				CursorPosition from = cursorStart.line < cursorEnd.line ? cursorStart : cursorEnd;
				CursorPosition to = cursorStart.line < cursorEnd.line ? cursorEnd : cursorStart;
				
				int fromX = from.getX();
				canvas.fillRectangle(fromX, from.getY(), bounds.width - fromX, selectionLineHeight, style.selectionColor);
				
				for (int i = from.line + 1; i < to.line; i++) {
					canvas.fillRectangle(x, lineToY(i), bounds.width - x, selectionLineHeight, style.selectionColor);
				}
				
				int toX = to.getX();
				canvas.fillRectangle(x, to.getY(), toX - x, selectionLineHeight, style.selectionColor);
			}
		}
		
		int y = bounds.y + style.selectionPaddingTop;
		int lineIndex = 1;
		for (Line line : lines) {
			if (y + textLineHeight  >= canvasBounds.y && y < canvasBounds.y + canvasBounds.height) {
				String lineNumber = Integer.toString(lineIndex);
				int lineNumberX = x - 15 - (int)canvas.measureTextLength(font, lineNumber);
				canvas.drawText(font, 0xFFA0A0A0, lineNumberX, y + fontMetrics.getAscent(), lineNumber);

				int lineX = x;
				for (ZSToken token : line.tokens) {
					String content = getDisplayContent(token);
					canvas.drawText(font, TokenClass.get(token.type).color, lineX, y + fontMetrics.getAscent(), content);
					lineX += canvas.measureTextLength(font, content);
				}
			}
			
			y += fullLineHeight;
			lineIndex++;
		}
		
		if (cursorEnd != null && cursorBlink) {
			int cursorX = cursorEnd.getX();
			int cursorY = cursorEnd.getY();
			canvas.fillRectangle(cursorX, cursorY, 2, selectionLineHeight, 0xFF000000);
		}
	}
	
	@Override
	public void onMouseEnter(DMouseEvent e) {
		context.setCursor(DDrawingContext.Cursor.TEXT);
	}
	
	@Override
	public void onMouseExit(DMouseEvent e) {
		context.setCursor(DDrawingContext.Cursor.NORMAL);
	}
	
	@Override
	public void onMouseClick(DMouseEvent e) {
		context.focus(this);
		
		CursorPosition position = getPositionAt(e.x, e.y);
		if (e.isDoubleClick()) {
			// select entire word
			TokenPosition token = getTokenAt(position);
			setCursor(
					new CursorPosition(position.line, position.offset - token.offset),
					new CursorPosition(position.line, position.offset + token.token.content.length() - token.offset));
		} else if (e.isTripleClick()) {
			setCursor(
					new CursorPosition(position.line, 0),
					new CursorPosition(position.line + 1, 0));
		} else {
			setCursor(position, position);
		}
	}
	
	private void setCursor(CursorPosition start, CursorPosition end) {
		if (cursorStart != null)
			repaint(cursorStart, cursorEnd);
		
		int previousLine = cursorEnd == null ? -1 : cursorEnd.line;
		
		cursorStart = start;
		cursorEnd = end;
		
		if (previousLine != cursorEnd.line) {
			if (previousLine >= 0)
				repaintLine(previousLine);
			repaintLine(cursorEnd.line);
		}
		repaint(cursorStart, cursorEnd);
		scrollTo(cursorEnd);
	}
	
	@Override
	public void onMouseDown(DMouseEvent e) {
		mouseDownX = e.x;
		mouseDownY = e.y;
	}
	
	@Override
	public void onMouseDrag(DMouseEvent e) {
		CursorPosition start = cursorStart;
		if (!dragging)
			start = getPositionAt(mouseDownX, mouseDownY);
		
		setCursor(start, getPositionAt(e.x, e.y));
	}
	
	@Override
	public void onKeyPressed(DKeyEvent e) {
		boolean shift = e.has(DKeyEvent.SHIFT);
		switch (e.keyCode) {
			case UP:
				if (cursorEnd == null || cursorEnd.line == 0)
					return;
				
				{
					int line = cursorEnd.line - 1;
					CursorPosition position = new CursorPosition(
							line,
							Math.min(lines.get(line).length, cursorEnd.offset));
					setCursor(shift ? cursorStart : position, position);
				}
				break;
			case DOWN:
				if (cursorEnd == null || cursorEnd.line >= lines.size() - 1)
					return;
				
				{
					int line = cursorEnd.line + 1;
					CursorPosition position = new CursorPosition(
							line,
							Math.min(lines.get(line).length, cursorEnd.offset));
					setCursor(shift ? cursorStart : position, position);
				}
				break;
			case LEFT:
				if (cursorEnd == null || (cursorEnd.line == 0 && cursorEnd.offset == 0))
					return;
				
				{
					CursorPosition position;
					if (cursorEnd.offset == 0) {
						int line = cursorEnd.line - 1;
						position = new CursorPosition(line, lines.get(line).length);
					} else {
						position = new CursorPosition(cursorEnd.line, cursorEnd.offset - 1);
					}
					setCursor(shift ? cursorStart : position, position);
				}
				break;
			case RIGHT:
				if (cursorEnd == null || (cursorEnd.offset == lines.get(cursorEnd.line).length && cursorEnd.line >= lines.size() - 1))
					return;
				
				{
					CursorPosition position;
					if (cursorEnd.offset == lines.get(cursorEnd.line).length) {
						position = new CursorPosition(cursorEnd.line + 1, 0);
					} else {
						position = new CursorPosition(cursorEnd.line, cursorEnd.offset + 1);
					}
					setCursor(shift ? cursorStart : position, position);
				}
				break;
			case DELETE:
				delete();
				break;
			case BACKSPACE:
				backspace();
				break;
			case ENTER:
				newline();
				break;
			case TAB:
				type(tab);
				break;
			default:
				if (e.character == DKeyEvent.CHAR_UNDEFINED)
					return;
				if (e.has(DKeyEvent.CTRL) || e.has(DKeyEvent.ALT)) {
					handleShortcut(e);
				} else {
					type(Character.toString(e.character));
				}
				break;
		}
	}
	
	private void handleShortcut(DKeyEvent e) {
		if (e.has(DKeyEvent.CTRL)) {
			if (e.keyCode == KeyCode.S) {
				save();
			}
		}
	}
	
	private void save() {
		String content = contentToString();
		sourceFile.update(content);
	}
	
	private String contentToString() {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < lines.size(); i++) {
			if (i > 0)
				result.append("\n");
			
			Line line = lines.get(i);
			for (ZSToken token : line.tokens) {
				result.append(token.content);
			}
		}
		return result.toString();
	}
	
	private void delete() {
		Line line = lines.get(cursorEnd.line);
		if (cursorEnd.offset == line.length) {
			// merge 2 lines
			if (cursorEnd.line == lines.size() - 1)
				return;
			
			line.merge(cursorEnd.line, lines.get(cursorEnd.line + 1));
			lines.remove(cursorEnd.line + 1);
			onLinesUpdated();
			return;
		}
		
		line.delete(cursorEnd.line, cursorEnd.offset);
		repaintLine(cursorEnd.line);
	}
	
	private void backspace() {
		if (cursorEnd.offset == 0) {
			if (cursorEnd.line == 0)
				return;
			
			int length = lines.get(cursorEnd.line - 1).length;
			lines.get(cursorEnd.line - 1).merge(cursorEnd.line - 1, lines.get(cursorEnd.line));
			lines.remove(cursorEnd.line);
			onLinesUpdated();
			
			CursorPosition position = new CursorPosition(cursorEnd.line - 1, length);
			setCursor(position, position);
			return;
		}
		
		lines.get(cursorEnd.line).delete(cursorEnd.line, cursorEnd.offset - 1);
		CursorPosition position = new CursorPosition(cursorEnd.line, cursorEnd.offset - 1);
		setCursor(position, position);
		repaintLine(cursorEnd.line);
	}
	
	private void type(String value) {
		lines.get(cursorEnd.line).insert(cursorEnd.line, cursorEnd.offset, value);
		CursorPosition position = new CursorPosition(cursorEnd.line, cursorEnd.offset + value.length());
		setCursor(position, position);
		repaintLine(cursorEnd.line);
	}
	
	private void newline() {
		lines.get(cursorEnd.line).split(cursorEnd.line, cursorEnd.offset);
		onLinesUpdated();
		
		CursorPosition position = new CursorPosition(cursorEnd.line + 1, 0);
		setCursor(position, position);
	}
	
	private TokenPosition getTokenAt(CursorPosition position) {
		Line line = lines.get(position.line);
		int offset = 0;
		for (ZSToken token : line.tokens) {
			if (offset + token.content.length() > position.offset)
				return new TokenPosition(token, position.offset - offset);
			offset += token.content.length();
		}
		
		return new TokenPosition(line.tokens.get(line.tokens.size() - 1), position.offset - offset);
	}
	
	private class TokenPosition {
		public final ZSToken token;
		public final int offset;
		
		public TokenPosition(ZSToken token, int offset) {
			this.token = token;
			this.offset = offset;
		}
	}
	
	private void repaint(CursorPosition from, CursorPosition to) {
		if (from.line == to.line) {
			int y = lineToY(from.line);
			int fromX = offsetToX(from.line, Math.min(from.offset, to.offset));
			int toX = offsetToX(from.line, Math.max(from.offset, to.offset)) + 2;
			context.repaint(fromX, y, toX - fromX, selectionLineHeight);
		} else {
			int fromY = lineToY(Math.min(from.line, to.line));
			int toY = lineToY(Math.max(from.line, to.line) + 1);
			context.repaint(bounds.x, fromY, bounds.width, toY - fromY);
		}
	}
	
	private void repaintLine(int line) {
		context.repaint(bounds.x, lineToY(line), bounds.width, selectionLineHeight);
	}
	
	private void scrollTo(CursorPosition position) {
		int y = lineToY(position.line);
		int x = offsetToX(position.line, position.offset);
		context.scrollInView(x, y, 2, selectionLineHeight);
	}
	
	private void blink() {
		if (cursorEnd != null) {
			cursorBlink = !cursorBlink;
			repaint(cursorEnd, cursorEnd);
		}
	}
	
	private CursorPosition getPositionAt(int x, int y) {
		int line = yToLine(y);
		int offset = xToOffset(line, x);
		
		if (line < 0)
			line = 0;
		if (line >= lines.size())
			line = lines.size() - 1;
		
		return new CursorPosition(line, offset);
	}
	
	private int xToOffset(int lineIndex, int x) {
		if (lineIndex < 0 || lineIndex >= lines.size())
			return 0;
		
		Line line = lines.get(lineIndex);
		int lineX = bounds.x + lineBarWidth + 10;
		int offset = 0;
		for (ZSToken token : line.tokens) {
			String content = getDisplayContent(token);
			int tokenWidth = fontMetrics.getWidth(content);
			if (lineX + tokenWidth > x) {
				return offset + getStringIndexForToken(token, tokenWidth, x - lineX);
			}
			lineX += tokenWidth;
			offset += token.content.length();
		}
		
		return offset;
	}
	
	private int getStringIndexForToken(ZSToken token, int strWidth, int pixels) {
		if (token.type == ZSTokenType.T_WHITESPACE_TAB)
			return pixels > strWidth / 2 ? 1 : 0;
		
		return getStringIndexForPixels(token.content, pixels);
	}
	
	private int getStringIndexForPixels(String str, int pixels) {
		int previousX = 0;
		for (int i = 1; i < str.length(); i++) {
			int currentX = fontMetrics.getWidth(str, 0, i);
			if (currentX >= pixels)
				return (pixels - previousX < currentX - pixels) ? i - 1 : i;
		}
		
		return str.length();
	}
	
	private int yToLine(int y) {
		int startY = bounds.y;
		return (y - startY) / fullLineHeight;
	}
	
	private int lineToY(int line) {
		int startY = bounds.y;
		return startY + line * fullLineHeight;
	}
	
	private int offsetToX(int line, int offset) {
		if (line >= lines.size())
			return 0;
		
		int tokensOffset = 0;
		int x = bounds.x + lineBarWidth + 10;
		Line lineData = lines.get(line);
		for (ZSToken token : lineData.tokens) {
			String content = getDisplayContent(token);
			if (tokensOffset + token.content.length() >= offset) {
				if (token.type == ZSTokenType.T_WHITESPACE_TAB)
					return offset == tokensOffset ? x : x + fontMetrics.getWidth(tab);
				
				return x + fontMetrics.getWidth(token.content, 0, offset - tokensOffset);
			}
			
			x += fontMetrics.getWidth(content);
			tokensOffset += token.content.length();
		}
		return x;
	}
	
	private String getDisplayContent(ZSToken token) {
		return token.type == ZSTokenType.T_WHITESPACE_TAB ? tab : token.content;
	}
	
	private void parse() throws IOException {
		lines.clear();
		lines.add(new Line());
		
		TokenParser<ZSToken, ZSTokenType> parser = ZSTokenParser.createRaw(sourceFile.getName(), new ReaderCharReader(sourceFile.read()), tab.length());
		insertTokens(0, 0, parser);
	}
	
	private void onLinesUpdated() {
		dimensionPreferences.setValue(new DDimensionPreferences(0, fullLineHeight * lines.size()));
		
		if (bounds != null)
			context.repaint(bounds.x, bounds.y, bounds.width, bounds.height);
	}
	
	public class Line {
		public final List<ZSToken> tokens = new ArrayList<>();
		private int length;
		
		private void calculateLength() {
			length = 0;
			for (ZSToken token : tokens)
				length += token.content.length();
		}
		
		public void merge(int lineIndex, Line other) {
			if (tokens.isEmpty()) {
				tokens.addAll(other.tokens);
				length = other.length;
				return;
			}
			
			int fromToken = tokens.size() - 1;
			int toToken = other.tokens.size() > 0 ? fromToken + 2 : fromToken + 1;
			tokens.addAll(other.tokens);
			length += other.length;
			reparse(lineIndex, fromToken, lineIndex, toToken);
		}
		
		public Line split(int lineIndex, int offset) {
			if (offset < 0)
				throw new IllegalArgumentException("offset must be >= 0");
			
			int tokenOffset = 0;
			for (int i = 0; i < tokens.size(); i++) {
				ZSToken token = tokens.get(i);
				if (tokenOffset + token.content.length() > offset) {
					ZSToken broken = token;
					Line newLine = new Line();
					if (offset - tokenOffset != token.content.length() && offset - tokenOffset != 0) {
						ZSToken.Pair split = token.split(offset - tokenOffset);
						broken = split.first;
						newLine.tokens.add(split.second);
					}
					
					tokens.set(i, broken);
					for (int j = i + 1; j < tokens.size(); j++)
						newLine.tokens.add(tokens.get(j));
					for (int j = tokens.size() - 1; j > i; j--)
						tokens.remove(j);
					
					lines.add(lineIndex + 1, newLine);
					reparse(lineIndex, i, lineIndex + 1, 1);
					return newLine;
				}
				tokenOffset += token.content.length();
			}
			
			Line result = new Line();
			lines.add(result);
			return result;
		}
		
		public void delete(int lineIndex, int offset) {
			int tokenOffset = 0;
			for (int i = 0; i < tokens.size(); i++) {
				ZSToken token = tokens.get(i);
				if (tokenOffset + token.content.length() > offset) {
					if (token.content.length() == 1) {
						tokens.remove(i);
					} else {
						token = token.delete(offset - tokenOffset, 1);
						tokens.set(i, token);
					}
					length--;
					
					reparse(lineIndex, i, lineIndex, i + 1);
					return;
				}
				tokenOffset += token.content.length();
			}
		}
		
		public void insert(int lineIndex, int offset, String value) {
			int tokenOffset = 0;
			if (tokens.isEmpty()) {
				tokens.add(new ZSToken(ZSTokenType.INVALID, value));
				reparse(lineIndex, 0, lineIndex, 1);
				return;
			}
			
			for (int i = 0; i < tokens.size(); i++) {
				ZSToken token = tokens.get(i);
				if (tokenOffset + token.content.length() > offset) {
					token = token.insert(offset - tokenOffset, value);
					tokens.set(i, token);
					length += value.length();
					reparse(lineIndex, i, lineIndex, i + 1);
					return;
				}
				tokenOffset += token.content.length();
			}
			
			ZSToken token = tokens.get(tokens.size() - 1);
			token = new ZSToken(token.type, token.content + value);
			tokens.set(tokens.size() - 1, token);
			length += value.length();
			reparse(lineIndex, tokens.size() - 1, lineIndex, tokens.size());
		}
	}
	
	private void reparse(int fromLine, int fromToken, int toLine, int toToken) {
		TokenReparser reparser = new TokenReparser(sourceFile.getName(), lines, fromLine, fromToken, toLine, toToken, tab.length());
		List<ZSToken> tokens = reparser.reparse();
		replaceTokens(fromLine, fromToken, reparser.getLine(), reparser.getToken(), tokens);
	}
	
	private void replaceTokens(int fromLine, int fromToken, int toLine, int toToken, List<ZSToken> tokens) {
		removeTokens(fromLine, fromToken, toLine, toToken);
		insertTokens(fromLine, fromToken, tokens.iterator());
	}
	
	private void removeTokens(int fromLine, int fromToken, int toLine, int toToken) {
		if (toLine > fromLine) {
			Line fromLineObject = lines.get(fromLine);
			for (int i = fromLineObject.tokens.size() - 1; i >= fromToken; i--)
				fromLineObject.tokens.remove(i);
			
			Line toLineObject = lines.get(fromLine);
			for (int i = toToken - 1; i >= 0; i--)
				toLineObject.tokens.remove(i);
			
			fromLineObject.merge(fromLine, lines.remove(toLine));
			for (int i = toLine - 1; i > fromLine; i--)
				lines.remove(i);
			
			onLinesUpdated();
		} else {
			Line line = lines.get(fromLine);
			for (int i = toToken - 1; i >= fromToken; i--)
				line.tokens.remove(i);
		}
	}
	
	private void insertTokens(int line, int tokenIndex, Iterator<ZSToken> tokens) {
		boolean linesUpdated = false;
		Line currentLine = lines.get(line);
		while (tokens.hasNext()) {
			ZSToken token = tokens.next();
			if (token.type == ZSTokenType.T_WHITESPACE_NEWLINE) {
				currentLine.calculateLength();
				
				Line newLine = new Line();
				if (tokenIndex < currentLine.tokens.size()) {
					for (int i = tokenIndex; i < currentLine.tokens.size(); i++) {
						newLine.tokens.add(currentLine.tokens.remove(tokenIndex));
					}
				}
				currentLine = newLine;
				tokenIndex = 0;
				lines.add(++line, currentLine);
				linesUpdated = true;
			} else if (token.type != ZSTokenType.T_WHITESPACE_CARRIAGE_RETURN) {
				currentLine.tokens.add(tokenIndex++, token);
			}
		}
		
		currentLine.calculateLength();
		
		if (linesUpdated) {
			onLinesUpdated();
		} else {
			// Could be further optimized to only repaint the modified characters
			repaintLine(line);
		}
	}
	
	private enum TokenClass {
		WHITESPACE(0xFF969696),
		KEYWORD(0xFF0000E6),
		OPERATOR(0xFF000000),
		TYPE(0xFF0000E6),
		IDENTIFIER(0xFF000000),
		NUMBER(0xFF444444),
		STRING(0xFFCE7B00),
		INVALID(0xFFFA0A00);
		
		public final int color;
		
		TokenClass(int color) {
			this.color = color;
		}
		
		public static TokenClass get(ZSTokenType type) {
			switch (type) {
				case T_WHITESPACE_CARRIAGE_RETURN:
				case T_WHITESPACE_NEWLINE:
				case T_WHITESPACE_SPACE:
				case T_WHITESPACE_TAB:
					return WHITESPACE;
				case T_IDENTIFIER:
				case T_DOLLAR:
					return IDENTIFIER;
				case T_FLOAT:
				case T_INT:
					return NUMBER;
				case T_STRING_SQ:
				case T_STRING_DQ:
					return STRING;
				case K_IMPORT:
				case K_ALIAS:
				case K_CLASS:
				case K_FUNCTION:
				case K_INTERFACE:
				case K_ENUM:
				case K_STRUCT:
				case K_EXPAND:
				case K_VARIANT:
				case K_ABSTRACT:
				case K_FINAL:
				case K_OVERRIDE:
				case K_CONST:
				case K_PRIVATE:
				case K_PUBLIC:
				case K_EXPORT:
				case K_STATIC:
				case K_PROTECTED:
				case K_IMPLICIT:
				case K_VIRTUAL:
				case K_EXTERN:
				case K_VAL:
				case K_VAR:
				case K_GET:
				case K_IMPLEMENTS:
				case K_SET:
				case K_IN:
				case K_IS:
				case K_AS:
				case K_MATCH:
				case K_THROWS:
				case K_SUPER:
				case K_THIS:
				case K_NULL:
				case K_TRUE:
				case K_FALSE:
				case K_NEW:
					
				case K_IF:
				case K_ELSE:
				case K_DO:
				case K_WHILE:
				case K_FOR:
				case K_THROW:
				case K_LOCK:
				case K_TRY:
				case K_CATCH:
				case K_FINALLY:
				case K_RETURN:
				case K_BREAK:
				case K_CONTINUE:
				case K_SWITCH:
				case K_CASE:
				case K_DEFAULT:
					return KEYWORD;
					
				case K_VOID:
				case K_ANY:
				case K_BOOL:
				case K_BYTE:
				case K_SBYTE:
				case K_SHORT:
				case K_USHORT:
				case K_INT:
				case K_UINT:
				case K_LONG:
				case K_ULONG:
				case K_FLOAT:
				case K_DOUBLE:
				case K_CHAR:
				case K_STRING:
					return TYPE;
				case INVALID:
					return INVALID;
				default:
					return OPERATOR;
			}
		}
	}
	
	public class CursorPosition {
		public final int line;
		public final int offset;
		
		public CursorPosition(int line, int offset) {
			this.line = line;
			this.offset = offset;
		}
		
		public int getX() {
			return offsetToX(line, offset);
		}
		
		public int getY() {
			return lineToY(line);
		}
		
		public boolean equals(CursorPosition other) {
			return line == other.line && offset == other.offset;
		}
	}
}
