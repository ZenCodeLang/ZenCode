/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.formattershared;

import java.util.List;
import org.openzen.zenscript.codemodel.statement.BlockStatement;
import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.statement.Statement;

/**
 *
 * @author Hoofdgebruiker
 */
public interface StatementFormattingTarget {
	LoopStatement getInnerLoop();
	
	String getIndent();
	
	void writeLine(String line);
	
	void writeInner(String lineBefore, Statement contents, LoopStatement loop, String lineAfter);
	
	void writeInner(String lineBefore, String[] inlineContents, Statement contents, LoopStatement loop, String lineAfter);
	
	void writeInnerMulti(String lineBefore, List<StatementFormattingSubBlock> contents, LoopStatement loop, String lineAfter);
	
	void writeBlock(String lineBefore, BlockStatement contents, String lineAfter);
}
