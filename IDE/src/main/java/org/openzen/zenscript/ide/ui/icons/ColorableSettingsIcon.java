/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.icons;
import org.openzen.drawablegui.DCanvas;
import org.openzen.drawablegui.DPath;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.DColorableIcon;

public class ColorableSettingsIcon implements DColorableIcon {
	public static final ColorableSettingsIcon INSTANCE = new ColorableSettingsIcon();
	public static final ColoredIcon BLACK = new ColoredIcon(INSTANCE, 0xFF000000);
	
	private ColorableSettingsIcon() {}
	
	private static final DPath PATH = tracer -> {
		tracer.moveTo(15.95f, 10.78f);
		tracer.bezierCubic(15.98f, 10.53f, 16.0f, 10.2699995f, 16.0f, 10.0f);
		tracer.bezierCubic(16.0f, 9.7300005f, 15.98f, 9.47f, 15.94f, 9.22f);
		tracer.lineTo(17.63f, 7.9f);
		tracer.bezierCubic(17.779999f, 7.78f, 17.82f, 7.56f, 17.73f, 7.3900003f);
		tracer.lineTo(16.13f, 4.6200004f);
		tracer.bezierCubic(16.029999f, 4.4400005f, 15.819999f, 4.3800006f, 15.639999f, 4.4400005f);
		tracer.lineTo(13.65f, 5.2400007f);
		tracer.bezierCubic(13.23f, 4.9200006f, 12.79f, 4.660001f, 12.299999f, 4.460001f);
		tracer.lineTo(12f, 2.34f);
		tracer.bezierCubic(11.97f, 2.1399999f, 11.8f, 1.9999999f, 11.6f, 1.9999999f);
		tracer.lineTo(8.4f, 1.9999999f);
		tracer.bezierCubic(8.2f, 1.9999999f, 8.04f, 2.1399999f, 8.009999f, 2.34f);
		tracer.lineTo(7.709999f, 4.46f);
		tracer.bezierCubic(7.2199993f, 4.66f, 6.769999f, 4.93f, 6.359999f, 5.24f);
		tracer.lineTo(4.369999f, 4.4399996f);
		tracer.bezierCubic(4.189999f, 4.3699994f, 3.979999f, 4.4399996f, 3.879999f, 4.6199994f);
		tracer.lineTo(2.2799988f, 7.3899994f);
		tracer.bezierCubic(2.1799989f, 7.569999f, 2.2199988f, 7.7799993f, 2.3799987f, 7.8999996f);
		tracer.lineTo(4.0699987f, 9.219999f);
		tracer.bezierCubic(4.029999f, 9.469999f, 3.9999988f, 9.74f, 3.9999988f, 9.999999f);
		tracer.bezierCubic(3.9999988f, 10.259998f, 4.019999f, 10.529999f, 4.059999f, 10.779999f);
		tracer.lineTo(2.37f, 12.1f);
		tracer.bezierCubic(2.2199998f, 12.22f, 2.1799998f, 12.440001f, 2.27f, 12.610001f);
		tracer.lineTo(3.87f, 15.380001f);
		tracer.bezierCubic(3.9699998f, 15.560001f, 4.18f, 15.620001f, 4.3599997f, 15.560001f);
		tracer.lineTo(6.3499994f, 14.760001f);
		tracer.bezierCubic(6.7699995f, 15.080001f, 7.2099996f, 15.340001f, 7.6999993f, 15.540001f);
		tracer.lineTo(7.9999995f, 17.66f);
		tracer.bezierCubic(8.04f, 17.86f, 8.2f, 18.0f, 8.4f, 18.0f);
		tracer.lineTo(11.599999f, 18.0f);
		tracer.bezierCubic(11.799999f, 18.0f, 11.969999f, 17.86f, 11.99f, 17.66f);
		tracer.lineTo(12.29f, 15.54f);
		tracer.bezierCubic(12.78f, 15.34f, 13.23f, 15.07f, 13.64f, 14.76f);
		tracer.lineTo(15.63f, 15.56f);
		tracer.bezierCubic(15.81f, 15.63f, 16.02f, 15.56f, 16.12f, 15.38f);
		tracer.lineTo(17.720001f, 12.610001f);
		tracer.bezierCubic(17.820002f, 12.43f, 17.78f, 12.22f, 17.62f, 12.1f);
		tracer.lineTo(15.950001f, 10.780001f);
		tracer.close();
		tracer.moveTo(10f, 13f);
		tracer.bezierCubic(8.35f, 13.0f, 7.0f, 11.65f, 7.0f, 10.0f);
		tracer.bezierCubic(7.0f, 8.35f, 8.35f, 7.0f, 10.0f, 7.0f);
		tracer.bezierCubic(11.65f, 7.0f, 13.0f, 8.35f, 13.0f, 10.0f);
		tracer.bezierCubic(13.0f, 11.65f, 11.65f, 13.0f, 10.0f, 13.0f);
		tracer.close();
	};
	
	@Override
	public void draw(DCanvas canvas, DTransform2D transform, int color) {
		canvas.fillPath(PATH, transform, color);
	}

	@Override
	public float getNominalWidth() {
		return 20;
	}

	@Override
	public float getNominalHeight() {
		return 20;
	}
}
