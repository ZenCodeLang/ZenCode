/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view.aspectbar;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.openzen.drawablegui.BaseComponentGroup;
import org.openzen.drawablegui.DCanvas;
import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DDimensionPreferences;
import org.openzen.drawablegui.DPath;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.listeners.DIRectangle;
import org.openzen.drawablegui.live.LiveObject;
import org.openzen.drawablegui.live.SimpleLiveObject;
import org.openzen.zenscript.ide.ui.IDEAspectBar;
import org.openzen.zenscript.ide.ui.IDEAspectToolbar;
import org.openzen.drawablegui.DUIContext;
import org.openzen.drawablegui.listeners.ListenerHandle;
import org.openzen.drawablegui.live.LiveList;
import org.openzen.drawablegui.live.LiveMappedList;
import org.openzen.drawablegui.style.DStyleClass;
import org.openzen.drawablegui.style.DStylePath;
import org.openzen.zenscript.ide.ui.IDEAspectBarControl;

/**
 *
 * @author Hoofdgebruiker
 */
public class AspectBarView extends BaseComponentGroup {
	private final SimpleLiveObject<DDimensionPreferences> dimensionPreferences = new SimpleLiveObject<>(new DDimensionPreferences(0, 64));
	private final DStyleClass styleClass;
	private final IDEAspectBar aspectBar;
	private final List<IDEAspectToolbar> contextBars = new ArrayList<>();
	
	private DIRectangle bounds;
	private DUIContext context;
	private DStylePath path;
	private AspectBarStyle style;
	public final LiveObject<IDEAspectToolbar> active = new SimpleLiveObject<>(null);
	private LiveMappedList<IDEAspectBarControl, DComponent> activeToolbarComponents;
	
	private final ListenerHandle<LiveList.Listener<IDEAspectToolbar>> listener;
	private DPath aspectBarShape;
	
	public AspectBarView(DStyleClass styleClass, IDEAspectBar aspectBar) {
		this.styleClass = styleClass;
		this.aspectBar = aspectBar;
		
		active.addListener(this::onActiveChanged);
		listener = aspectBar.aspectToolbars.addListener(new ToolbarListListener());
		
		if (aspectBar.aspectToolbars.size() > 0)
			active.setValue(aspectBar.aspectToolbars.get(0));
	}
	
	@Override
	public void setContext(DStylePath parent, DUIContext context) {
		this.context = context;
		this.path = parent.getChild("aspectbar", styleClass);
		this.style = new AspectBarStyle(context.getStylesheets().get(context, path));
		
		if (bounds != null) {
			calculateAspectBarShape();
			active.setValue(active.getValue());
		}
	}
	
	@Override
	public LiveObject<DDimensionPreferences> getDimensionPreferences() {
		return dimensionPreferences;
	}
	
	@Override
	public DIRectangle getBounds() {
		return bounds;
	}

	@Override
	public void setBounds(DIRectangle bounds) {
		this.bounds = bounds;
		
		if (context != null) {
			calculateAspectBarShape();
			context.repaint(bounds);
		}
	}

	@Override
	public void paint(DCanvas canvas) {
		canvas.pushBounds(bounds);
		canvas.fillRectangle(bounds.x, bounds.y, bounds.width, bounds.height, style.backgroundColor);
		
		//DPath circle = DPath.circle(0, 0, style.aspectSelectorButtonRadius);
		DPath circle = DPath.roundedRectangle(
				-style.aspectSelectorButtonRadius,
				-style.aspectSelectorButtonRadius,
				2 * style.aspectSelectorButtonRadius,
				2 * style.aspectSelectorButtonRadius,
				8);
		int x = bounds.x + style.aspectSelectorPaddingLeft;
		int y = bounds.y;
		for (IDEAspectToolbar toolbar : aspectBar.aspectToolbars) {
			canvas.shadowPath(
					circle,
					DTransform2D.translate(x + style.aspectSelectorButtonRadius, y + style.height / 2),
					style.aspectSelectorButtonShadowColor,
					style.aspectSelectorButtonShadowOffsetX,
					style.aspectSelectorButtonShadowOffsetY,
					style.aspectSelectorButtonShadowRadius);
			canvas.fillPath(
					circle,
					DTransform2D.translate(x + style.aspectSelectorButtonRadius, y + style.height / 2),
					style.foregroundColor);
			toolbar.icon.draw(canvas, DTransform2D.scaleAndTranslate(
					x + (2 * style.aspectSelectorButtonRadius - toolbar.icon.getNominalWidth() * context.getScale()) / 2,
					y + (style.height - toolbar.icon.getNominalHeight() * context.getScale()) / 2,
					context.getScale()), 0xFF888888);
			x += style.aspectSelectorButtonRadius * 2 + style.aspectSelectorButtonSpacing;
		}
		for (IDEAspectToolbar toolbar : contextBars) {
			canvas.shadowPath(
					circle,
					DTransform2D.translate(x + style.aspectSelectorButtonRadius, y + style.height / 2),
					style.aspectSelectorButtonShadowColor,
					style.aspectSelectorButtonShadowOffsetX,
					style.aspectSelectorButtonShadowOffsetY,
					style.aspectSelectorButtonShadowRadius);
			canvas.fillPath(
					circle,
					DTransform2D.translate(x + style.aspectSelectorButtonRadius, y + style.height / 2),
					style.foregroundColor);
			toolbar.icon.draw(canvas, DTransform2D.scaleAndTranslate(
					x + (2 * style.aspectSelectorButtonRadius - toolbar.icon.getNominalWidth() * context.getScale()) / 2,
					y + (style.height - toolbar.icon.getNominalHeight() * context.getScale()) / 2,
					context.getScale()), 0xFF000000);
			x += style.aspectSelectorButtonRadius * 2 + style.aspectSelectorButtonSpacing;
		}
		
		
		canvas.shadowPath(
				aspectBarShape,
				DTransform2D.IDENTITY,
				style.aspectBarShadowColor,
				style.aspectBarShadowOffsetX,
				style.aspectBarShadowOffsetY,
				style.aspectBarShadowRadius);
		canvas.fillPath(
				aspectBarShape,
				DTransform2D.IDENTITY,
				style.foregroundColor);
		
		if (activeToolbarComponents != null) {
			for (DComponent component : activeToolbarComponents)
				component.paint(canvas);
		}
		
		canvas.popBounds();
	}

	@Override
	public void close() {
		listener.close();
	}
	
	private void onActiveChanged(IDEAspectToolbar previous, IDEAspectToolbar aspectBar) {
		if (activeToolbarComponents != null)
			activeToolbarComponents.close();
		
		activeToolbarComponents = new LiveMappedList<>(aspectBar.controls, control -> {
			DComponent result = control.instantiate();
			if (context != null)
				result.setContext(path, context);
			return result;
		});
		
		layoutActiveToolbarComponents();
	}
	
	private void layoutActiveToolbarComponents() {
		if (bounds == null)
			return;
		
		int x = bounds.x
				+ style.aspectSelectorPaddingLeft
				+ (style.aspectSelectorButtonRadius * 2 + style.aspectSelectorButtonSpacing) * (aspectBar.aspectToolbars.size() + contextBars.size())
				+ style.aspectSelectorToToolbarSpacing;
		
		int y = style.aspectBarPaddingTop + style.controlPaddingTop;
		int height = bounds.height - y - style.controlPaddingBottom;
		for (DComponent toolbarComponent : activeToolbarComponents) {
			int width = toolbarComponent.getDimensionPreferences().getValue().preferredWidth;
			toolbarComponent.setBounds(
					new DIRectangle(x, bounds.y + y, width, height));
			x += width;
		}
		
		context.repaint(bounds);
	}

	@Override
	protected void forEachChild(Consumer<DComponent> children) {
		if (activeToolbarComponents == null)
			return;
		
		for (DComponent component : activeToolbarComponents)
			children.accept(component);
	}

	@Override
	protected DComponent findChild(Predicate<DComponent> predicate) {
		if (activeToolbarComponents == null)
			return null;
		
		for (DComponent component : activeToolbarComponents)
			if (predicate.test(component))
				return component;
		
		return null;
	}
	
	private void calculateAspectBarShape() {
		int toX = bounds.x
				+ style.aspectSelectorPaddingLeft
				+ (style.aspectSelectorButtonRadius * 2 + style.aspectSelectorButtonSpacing) * (aspectBar.aspectToolbars.size() + contextBars.size());
		
		aspectBarShape = tracer -> {
			int baseY = bounds.y + bounds.height - style.aspectSelectorBottomSize;
			tracer.moveTo(bounds.x, baseY);
			tracer.lineTo(toX, baseY);
			tracer.bezierCubic(
					toX + 6 * context.getScale(), baseY,
					toX + style.aspectSelectorToToolbarSpacing - 6 * context.getScale(), bounds.y + style.aspectBarPaddingTop,
					toX + style.aspectSelectorToToolbarSpacing, bounds.y + style.aspectBarPaddingTop);
			tracer.lineTo(bounds.x + bounds.width, bounds.y + style.aspectBarPaddingTop);
			tracer.lineTo(bounds.x + bounds.width, bounds.y + bounds.height);
			tracer.lineTo(bounds.x, bounds.y + bounds.height);
			tracer.close();
		};
	}
	
	private class ToolbarListListener implements LiveList.Listener<IDEAspectToolbar> {

		@Override
		public void onInserted(int index, IDEAspectToolbar value) {
			if (index == 0 && activeToolbarComponents == null)
				active.setValue(value);
			
			layoutActiveToolbarComponents();
			calculateAspectBarShape();
		}

		@Override
		public void onChanged(int index, IDEAspectToolbar oldValue, IDEAspectToolbar newValue) {
			if (oldValue == active.getValue())
				active.setValue(newValue);
		}

		@Override
		public void onRemoved(int index, IDEAspectToolbar oldValue) {
			if (oldValue == active.getValue())
				active.setValue(aspectBar.aspectToolbars.size() == 0 ? null : aspectBar.aspectToolbars.get(0));
			
			layoutActiveToolbarComponents();
			calculateAspectBarShape();
		}
	}
}
