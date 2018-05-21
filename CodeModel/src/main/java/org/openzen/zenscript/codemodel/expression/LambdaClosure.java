/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Hoofdgebruiker
 */
public class LambdaClosure {
	public final List<CapturedExpression> captures = new ArrayList<>();
	
	public void add(CapturedExpression capture) {
		captures.add(capture);
	}
}
