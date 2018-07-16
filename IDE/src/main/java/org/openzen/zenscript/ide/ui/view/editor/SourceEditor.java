/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view.editor;

import java.io.IOException;
import org.openzen.drawablegui.DCanvas;
import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DSizing;
import org.openzen.drawablegui.DFont;
import org.openzen.drawablegui.DFontFamily;
import org.openzen.drawablegui.DFontMetrics;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.DKeyEvent;
import org.openzen.drawablegui.DMouseEvent;
import org.openzen.drawablegui.DTimerHandle;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.listeners.ListenerHandle;
import org.openzen.drawablegui.live.LiveObject;
import org.openzen.zenscript.ide.host.IDESourceFile;
import org.openzen.zenscript.ide.ui.IDEAspectToolbar;
import org.openzen.zenscript.ide.ui.IDEWindow;
import org.openzen.zenscript.lexer.ReaderCharReader;
import org.openzen.zenscript.lexer.TokenParser;
import org.openzen.zenscript.lexer.ZSToken;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.drawablegui.DUIContext;
import org.openzen.drawablegui.draw.DDrawSurface;
import org.openzen.drawablegui.draw.DDrawnRectangle;
import org.openzen.drawablegui.live.ImmutableLiveString;
import org.openzen.drawablegui.live.InverseLiveBool;
import org.openzen.drawablegui.live.LiveBool;
import org.openzen.drawablegui.live.MutableLiveObject;
import org.openzen.drawablegui.live.SimpleLiveBool;
import org.openzen.drawablegui.style.DStyleClass;
import org.openzen.drawablegui.style.DStylePath;
import org.openzen.zenscript.ide.ui.icons.SaveIcon;
import org.openzen.zenscript.ide.ui.icons.ShadedCodeIcon;
import org.openzen.zenscript.ide.ui.icons.ShadedSaveIcon;
import org.openzen.zenscript.ide.ui.view.IconButtonControl;

/**
 *
 * @author Hoofdgebruiker
 */
public class SourceEditor implements DComponent {
	private final DStyleClass styleClass;
	private final DFont font = new DFont(DFontFamily.CODE, false, false, false, 24);
	private final MutableLiveObject<DSizing> sizing = DSizing.create();
	private final String tab = "    ";
	private final IDESourceFile sourceFile;
	private final TokenModel tokens;
	private final ListenerHandle<TokenModel.Listener> tokenListener;
	private final SimpleLiveBool unchanged = new SimpleLiveBool(true);
	
	private DIRectangle bounds;
	private int z;
	private DDrawSurface surface;
	private SourceEditorStyle style;
	private DTimerHandle blinkTimer;
	
	private DFontMetrics fontMetrics;
	private int textLineHeight;
	private int fullLineHeight;
	private int selectionLineHeight;
	
	private int lineBarWidth;
	private SourcePosition cursorStart = null;
	private SourcePosition cursorEnd = null;
	
	private boolean cursorBlink = true;
	
	private int mouseDownX = -1;
	private int mouseDownY = -1;
	private boolean dragging = false;
	
	private final IDEWindow window;
	private final IDEAspectToolbar editToolbar = new IDEAspectToolbar(0, ShadedCodeIcon.BLUE, "Edit", "Source code editor");
	
	private final LiveBool updated;
	
	private DDrawnRectangle background;
	private DDrawnRectangle lineBarBackground;
	
	public SourceEditor(DStyleClass styleClass, IDEWindow window, IDESourceFile sourceFile) {
		this.styleClass = styleClass;
		this.window = window;
		this.sourceFile = sourceFile;
		
		tokens = new TokenModel(sourceFile.getName().getValue(), tab.length());
		tokenListener = tokens.addListener(new TokenListener());
		
		editToolbar.controls.add(() -> new IconButtonControl(DStyleClass.EMPTY, ShadedSaveIcon.PURPLE, SaveIcon.GREY, unchanged, new ImmutableLiveString("Save file"), e -> save()));
		updated = new InverseLiveBool(unchanged);
		
		try {
			TokenParser<ZSToken, ZSTokenType> parser = ZSTokenParser.createRaw(
					sourceFile.getName().getValue(),
					new ReaderCharReader(sourceFile.read()),
					tab.length());
			tokens.set(parser);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public LiveBool isUpdated() {
		return updated;
	}
	
	@Override
	public void onMounted() {
		window.aspectBar.toolbars.add(editToolbar);
		window.aspectBar.active.setValue(editToolbar);
	}
	
	@Override
	public void onUnmounted() {
		window.aspectBar.toolbars.remove(editToolbar);
	}
	
	@Override
	public void close() {
		if (blinkTimer != null)
			blinkTimer.close();
		
		tokenListener.close();
	}

	@Override
	public void setSurface(DStylePath parent, int z, DDrawSurface surface) {
		this.surface = surface;
		this.z = z;
		this.style = new SourceEditorStyle(surface.getStylesheet(parent.getChild("sourceeditor", styleClass)));
		
		fontMetrics = surface.getFontMetrics(font);
		textLineHeight = fontMetrics.getAscent() + fontMetrics.getDescent();
		fullLineHeight = textLineHeight + fontMetrics.getLeading() + style.extraLineSpacing;
		selectionLineHeight = textLineHeight + style.selectionPaddingTop + style.selectionPaddingBottom;
		
		sizing.setValue(new DSizing(0, fullLineHeight * tokens.getLineCount()));
		
		if (blinkTimer != null)
			blinkTimer.close();
		blinkTimer = surface.getContext().setTimer(300, this::blink);
	}

	@Override
	public LiveObject<DSizing> getSizing() {
		return sizing;
	}

	@Override
	public DIRectangle getBounds() {
		return bounds;
	}
	
	@Override
	public int getBaselineY() {
		return -1;
	}

	@Override
	public void setBounds(DIRectangle bounds) {
		this.bounds = bounds;
		
		lineBarWidth = Math.max(style.lineBarMinWidth, fontMetrics.getWidth(Integer.toString(tokens.getLineCount())))
				+ style.lineBarSpacingLeft
				+ style.lineBarSpacingRight;
		
		if (background != null)
			background.close();
		if (lineBarBackground != null)
			lineBarBackground.close();
		background = surface.fillRect(z, new DIRectangle(bounds.x + lineBarWidth, bounds.y, bounds.width - lineBarWidth, bounds.height), style.backgroundColor);
		lineBarBackground = surface.fillRect(z, new DIRectangle(bounds.x, bounds.y, lineBarWidth, bounds.height), style.lineBarBackgroundColor);
	}

	@Override
	public void paint(DCanvas canvas) {
		DIRectangle canvasBounds = canvas.getBounds();
		
		if (style.lineBarStrokeWidth > 0) {
			canvas.strokePath(tracer -> {
				tracer.moveTo(bounds.x + lineBarWidth, bounds.y);
				tracer.lineTo(bounds.x + lineBarWidth, bounds.y + bounds.height);
			}, DTransform2D.IDENTITY, style.lineBarStrokeColor, style.lineBarStrokeWidth);
		}
		
		int x = bounds.x + lineBarWidth + style.lineBarMargin;
		if (cursorEnd != null)
			canvas.fillRectangle(x, lineToY(cursorEnd.line), bounds.width - x, selectionLineHeight, style.currentLineHighlight);
		
		if (cursorStart != null && !cursorStart.equals(cursorEnd)) {
			if (cursorStart.line == cursorEnd.line) {
				int y = getY(cursorStart);
				int x1 = getX(cursorStart);
				int x2 = getX(cursorEnd);
				int fromX = Math.min(x1, x2);
				int toX = Math.max(x1, x2);
				canvas.fillRectangle(fromX, y, toX - fromX, selectionLineHeight, style.selectionColor);
			} else {
				SourcePosition from = SourcePosition.min(cursorStart, cursorEnd);
				SourcePosition to = SourcePosition.max(cursorStart, cursorEnd);
				
				int fromX = getX(from);
				canvas.fillRectangle(fromX, getY(from), bounds.width - fromX, selectionLineHeight, style.selectionColor);
				
				for (int i = from.line + 1; i < to.line; i++) {
					canvas.fillRectangle(x, lineToY(i), bounds.width - x, selectionLineHeight, style.selectionColor);
				}
				
				int toX = getX(to);
				canvas.fillRectangle(x, getY(to), toX - x, selectionLineHeight, style.selectionColor);
			}
		}
		
		int y = bounds.y + style.selectionPaddingTop;
		int lineIndex = 1;
		for (TokenLine line : tokens.getLines()) {
			if (y + textLineHeight  >= canvasBounds.y && y < canvasBounds.y + canvasBounds.height) {
				String lineNumber = Integer.toString(lineIndex);
				int lineNumberX = x - style.lineBarSpacingRight - style.lineBarMargin - fontMetrics.getWidth(lineNumber);
				canvas.drawText(font, style.lineBarTextColor, lineNumberX, y + fontMetrics.getAscent(), lineNumber);

				int lineX = x;
				for (ZSToken token : line.getTokens()) {
					String content = getDisplayContent(token);
					canvas.drawText(font, TokenClass.get(token.type).color, lineX, y + fontMetrics.getAscent(), content);
					lineX += fontMetrics.getWidth(content);
				}
			}
			
			y += fullLineHeight;
			lineIndex++;
		}
		
		if (cursorEnd != null && cursorBlink) {
			int cursorX = getX(cursorEnd);
			int cursorY = getY(cursorEnd);
			canvas.fillRectangle(cursorX, cursorY, style.cursorWidth, selectionLineHeight, style.cursorColor);
		}
	}
	
	@Override
	public void onMouseEnter(DMouseEvent e) {
		surface.getContext().setCursor(DUIContext.Cursor.TEXT);
	}
	
	@Override
	public void onMouseExit(DMouseEvent e) {
		surface.getContext().setCursor(DUIContext.Cursor.NORMAL);
	}
	
	@Override
	public void onMouseClick(DMouseEvent e) {
		surface.getContext().getWindow().focus(this);
		
		SourcePosition position = getPositionAt(e.x, e.y);
		if (e.isDoubleClick()) {
			// select entire word
			TokenModel.Position tokenPosition = tokens.getPosition(position.line, position.offset);
			ZSToken token = tokens.getTokenAt(tokenPosition);
			if (token != null) {
				setCursor(
						new SourcePosition(tokens, position.line, position.offset - tokenPosition.offset),
						new SourcePosition(tokens, position.line, position.offset - tokenPosition.offset + token.content.length()));
			}
		} else if (e.isTripleClick()) {
			setCursor(
					new SourcePosition(tokens, position.line, 0),
					new SourcePosition(tokens, position.line + 1, 0));
		} else {
			setCursor(position, position);
		}
	}
	
	private void setCursor(SourcePosition start, SourcePosition end) {
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
		SourcePosition start = cursorStart;
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
					SourcePosition position = new SourcePosition(
							tokens,
							line,
							Math.min(tokens.getLineLength(line), cursorEnd.offset));
					setCursor(shift ? cursorStart : position, position);
				}
				break;
			case DOWN:
				if (cursorEnd == null || cursorEnd.line >= tokens.getLineCount() - 1)
					return;
				
				{
					int line = cursorEnd.line + 1;
					SourcePosition position = new SourcePosition(
							tokens,
							line,
							Math.min(tokens.getLineLength(line), cursorEnd.offset));
					setCursor(shift ? cursorStart : position, position);
				}
				break;
			case LEFT:
				if (cursorEnd == null || (cursorEnd.line == 0 && cursorEnd.offset == 0))
					return;
				
				{
					SourcePosition position;
					if (cursorEnd.offset == 0) {
						int line = cursorEnd.line - 1;
						position = new SourcePosition(tokens, line, tokens.getLineLength(line));
					} else {
						position = new SourcePosition(tokens, cursorEnd.line, cursorEnd.offset - 1);
					}
					setCursor(shift ? cursorStart : position, position);
				}
				break;
			case RIGHT:
				if (cursorEnd == null || (cursorEnd.offset == tokens.getLineLength(cursorEnd.line) && cursorEnd.line >= tokens.getLineCount() - 1))
					return;
				
				{
					SourcePosition position;
					if (cursorEnd.offset == tokens.getLineLength(cursorEnd.line)) {
						position = new SourcePosition(tokens, cursorEnd.line + 1, 0);
					} else {
						position = new SourcePosition(tokens, cursorEnd.line, cursorEnd.offset + 1);
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
				type("\t");
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
			switch (e.keyCode) {
				case S:
					save();
					break;
				case C:
					copy();
					break;
				case X:
					cut();
					break;
				case V:
					paste();
					break;
			}
		}
	}
	
	private void copy() {
		String extract = tokens.extract(
				SourcePosition.min(cursorStart, cursorEnd),
				SourcePosition.max(cursorStart, cursorEnd));
		surface.getContext().getClipboard().copyAsString(extract);
	}
	
	private void cut() {
		copy();
		tokens.delete(cursorStart, cursorEnd);
		
		SourcePosition cursor = SourcePosition.min(cursorStart, cursorEnd);
		setCursor(cursor, cursor);
		unchanged.setValue(false);
	}
	
	private void paste() {
		String text = surface.getContext().getClipboard().getAsString();
		if (text == null)
			return;
		
		deleteSelection();
		tokens.insert(cursorEnd, text);
		
		SourcePosition cursor = cursorEnd.advance(text.length());
		setCursor(cursor, cursor);
		unchanged.setValue(false);
	}
	
	private void save() {
		String content = tokens.toString();
		sourceFile.update(content);
		unchanged.setValue(true);
	}
	
	private void delete() {
		if (deleteSelection())
			return;
		
		TokenLine line = tokens.getLine(cursorEnd.line);
		if (cursorEnd.offset == line.length()) {
			// merge 2 lines
			if (cursorEnd.line == tokens.getLineCount() - 1)
				return;
			
			tokens.deleteNewline(cursorEnd.line);
			return;
		}
		
		tokens.deleteCharacter(cursorEnd.line, cursorEnd.offset);
		unchanged.setValue(false);
	}
	
	private boolean hasSelection() {
		return !cursorEnd.equals(cursorStart);
	}
	
	private boolean deleteSelection() {
		if (hasSelection()) {
			SourcePosition min = SourcePosition.min(cursorStart, cursorEnd);
			SourcePosition max = SourcePosition.max(cursorStart, cursorEnd);
			tokens.delete(min, max);
			setCursor(min, min);
			unchanged.setValue(false);
			return true;
		}
		
		return false;
	}
	
	private void backspace() {
		if (deleteSelection())
			return;
		
		if (cursorEnd.line > 0) {
			String indent = tokens.getLine(cursorEnd.line - 1).getIndent(); // TODO: get nominal indent for current scope
			if (cursorEnd.offset == indent.length()) {
				// remove entire indent
				SourcePosition deleteFrom = new SourcePosition(tokens, cursorEnd.line - 1, tokens.getLine(cursorEnd.line - 1).length());
				tokens.delete(deleteFrom, cursorEnd);
				unchanged.setValue(false);
				setCursor(deleteFrom, deleteFrom);
				return;
			}
		}
		
		if (cursorEnd.offset == 0) {
			if (cursorEnd.line == 0)
				return;
			
			int length = tokens.getLineLength(cursorEnd.line - 1);
			tokens.deleteNewline(cursorEnd.line - 1);
			unchanged.setValue(false);
			
			SourcePosition position = new SourcePosition(tokens, cursorEnd.line - 1, length);
			setCursor(position, position);
			return;
		}
		
		try {
			tokens.deleteCharacter(cursorEnd.line, cursorEnd.offset - 1);
			SourcePosition position = new SourcePosition(tokens, cursorEnd.line, cursorEnd.offset - 1);
			setCursor(position, position);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void type(String value) {
		deleteSelection();
		
		if (value.equals("{")) {
			String indent = tokens.getLine(cursorEnd.line).getIndent();
			tokens.insert(cursorEnd, "{\n" + indent + "\t\n" + indent + "}");
			
			SourcePosition position = new SourcePosition(tokens, cursorEnd.line + 1, indent.length() + 1);
			setCursor(position, position);
		} else if (value.equals(";")) {
			String indent = tokens.getLine(cursorEnd.line).getIndent();
			tokens.insert(cursorEnd, ";\n" + indent);
			
			SourcePosition position = new SourcePosition(tokens, cursorEnd.line + 1, indent.length());
			setCursor(position, position);
		} else {
			tokens.insert(cursorEnd, value);
			
			SourcePosition position = new SourcePosition(tokens, cursorEnd.line, cursorEnd.offset + value.length());
			setCursor(position, position);
		}
		
		unchanged.setValue(false);
	}
	
	private void newline() {
		deleteSelection();
		
		String indent = tokens.getLine(cursorEnd.line).getIndent();
		tokens.insert(cursorEnd, "\n" + indent);
		SourcePosition position = new SourcePosition(tokens, cursorEnd.line + 1, indent.length());
		setCursor(position, position);
		unchanged.setValue(false);
	}
	
	private void repaint(SourcePosition from, SourcePosition to) {
		if (from.line == to.line) {
			int y = lineToY(from.line);
			int x1 = getX(from);
			int x2 = getX(to);
			int fromX = Math.min(x1, x2);
			int toX = Math.max(x1, x2) + 2;
			surface.repaint(fromX, y, toX - fromX, selectionLineHeight);
		} else {
			int fromY = lineToY(Math.min(from.line, to.line));
			int toY = lineToY(Math.max(from.line, to.line) + 1);
			surface.repaint(bounds.x, fromY, bounds.width, toY - fromY);
		}
	}
	
	private void repaintLine(int line) {
		if (bounds == null)
			return;
		
		surface.repaint(bounds.x, lineToY(line), bounds.width, selectionLineHeight);
	}
	
	public void scrollTo(SourcePosition position) {
		surface.getContext().scrollInView(getX(position), getY(position), 2, selectionLineHeight);
	}
	
	private void blink() {
		if (cursorEnd != null) {
			cursorBlink = !cursorBlink;
			repaint(cursorEnd, cursorEnd);
		}
	}
	
	public SourcePosition getPositionAt(int x, int y) {
		int line = yToLine(y);
		int offset = xToOffset(line, x);
		
		if (line < 0)
			line = 0;
		if (line >= tokens.getLineCount())
			line = tokens.getLineCount() - 1;
		
		return new SourcePosition(tokens, line, offset);
	}
	
	private int xToOffset(int lineIndex, int x) {
		if (lineIndex < 0 || lineIndex >= tokens.getLineCount())
			return 0;
		
		TokenLine line = tokens.getLine(lineIndex);
		int lineX = bounds.x + lineBarWidth + 10;
		int offset = 0;
		for (ZSToken token : line.getTokens()) {
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
		for (int i = 1; i <= str.length(); i++) {
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
	
	public int getX(SourcePosition position) {
		int x = bounds.x + lineBarWidth + 10;
		TokenModel.Position tokenPosition = position.asTokenPosition();
		TokenLine lineData = tokens.getLine(tokenPosition.line);
		
		for (int i = 0; i < tokenPosition.token; i++)
			x += fontMetrics.getWidth(getDisplayContent(lineData.getToken(i)));
		if (tokenPosition.offset > 0)
			x += fontMetrics.getWidth(getDisplayContent(lineData.getToken(tokenPosition.token)), 0, tokenPosition.offset);
		
		return x;
	}
	
	public int getY(SourcePosition position) {
		return lineToY(position.line);
	}
	
	private String getDisplayContent(ZSToken token) {
		return token.type == ZSTokenType.T_WHITESPACE_TAB ? tab : token.content;
	}
	
	private void onLinesUpdated() {
		sizing.setValue(new DSizing(0, fullLineHeight * tokens.getLineCount()));
		
		if (bounds != null)
			surface.repaint(bounds);
	}
	
	private class TokenListener implements TokenModel.Listener {

		@Override
		public void onLineInserted(int index) {
			onLinesUpdated();
		}

		@Override
		public void onLineChanged(int index) {
			repaintLine(index);
		}

		@Override
		public void onLineDeleted(int index) {
			onLinesUpdated();
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
				case T_COMMENT_SCRIPT:
				case T_COMMENT_SINGLELINE:
				case T_COMMENT_MULTILINE:
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
}
