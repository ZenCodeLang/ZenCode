/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.type.FunctionTypeID;
import org.openzen.zenscript.codemodel.type.RangeTypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public interface JavaSyntheticClassGenerator {
	JavaSynthesizedClass synthesizeFunction(FunctionTypeID type);
	
	JavaSynthesizedClass synthesizeRange(RangeTypeID type);
}
