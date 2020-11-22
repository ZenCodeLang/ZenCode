package org.openzen.zenscript.formattershared;

import java.util.List;
import org.openzen.zenscript.codemodel.statement.BlockStatement;
import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.statement.Statement;

public interface StatementFormattingTarget {
	LoopStatement getInnerLoop();
	
	String getIndent();
	
	void writeLine(String line);
	
	void writeInner(String lineBefore, Statement contents, LoopStatement loop, String lineAfter);
	
	void writeInner(String lineBefore, String[] inlineContents, Statement contents, LoopStatement loop, String lineAfter);
	
	void writeInnerMulti(String lineBefore, List<StatementFormattingSubBlock> contents, LoopStatement loop, String lineAfter);
	
	void writeBlock(String lineBefore, BlockStatement contents, String lineAfter);
}
