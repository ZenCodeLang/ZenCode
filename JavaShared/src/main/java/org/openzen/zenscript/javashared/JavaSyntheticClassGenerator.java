/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared;

/**
 * @author Hoofdgebruiker
 */
public interface JavaSyntheticClassGenerator {
	JavaMethod synthesizeFunction(JavaSynthesizedFunction function);

	void synthesizeRange(JavaSynthesizedRange range);
}
