/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view.editor;

import org.openzen.drawablegui.DPath;

/**
 * @author Hoofdgebruiker
 */
public class PathGenerator {
	private PathGenerator() {
	}

	public static class CubicBezierCurve {
		public final float p1x;
		public final float p1y;
		public final float p2x;
		public final float p2y;
		public final float p3x;
		public final float p3y;
		public final float p4x;
		public final float p4y;

		public CubicBezierCurve(float p1x, float p1y, float p2x, float p2y, float p3x, float p3y, float p4x, float p4y) {
			this.p1x = p1x;
			this.p1y = p1y;
			this.p2x = p2x;
			this.p2y = p2y;
			this.p3x = p3x;
			this.p3y = p3y;
			this.p4x = p4x;
			this.p4y = p4y;
		}

		public void cut(float t0, float t1) {
			// https://stackoverflow.com/questions/11703283/cubic-bezier-curve-segment
			// P'1 = u0u0u0 P1 + (t0u0u0 + u0t0u0 + u0u0t0) P2 + (t0t0u0 + u0t0t0 + t0u0t0) P3 + t0t0t0 P4
			// P'2 = u0u0u1 P1 + (t0u0u1 + u0t0u1 + u0u0t1) P2 + (t0t0u1 + u0t0t1 + t0u0t1) P3 + t0t0t1 P4
			// P'3 = u0u1u1 P1 + (t0u1u1 + u0t1u1 + u0u1t1) P2 + (t0t1u1 + u0t1t1 + t0u1t1) P3 + t0t1t1 P4
			// P'4 = u1u1u1 P1 + (t1u1u1 + u1t1u1 + u1u1t1) P2 + (t1t1u1 + u1t1t1 + t1u1t1) P3 + t1t1t1 P4
			// u0 = 1 − t0 and u1 = 1 − t1

		}
	}
}
