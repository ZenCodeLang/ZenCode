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
import org.openzen.drawablegui.DSizing;
import org.openzen.drawablegui.DFontMetrics;
import org.openzen.drawablegui.DPath;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.live.LiveObject;
import org.openzen.zenscript.ide.ui.IDEAspectBar;
import org.openzen.zenscript.ide.ui.IDEAspectToolbar;
import org.openzen.drawablegui.DUIContext;
import org.openzen.drawablegui.DUIWindow;
import org.openzen.drawablegui.listeners.ListenerHandle;
import org.openzen.drawablegui.live.LiveBool;
import org.openzen.drawablegui.live.LiveList;
import org.openzen.drawablegui.live.LiveMappedList;
import org.openzen.drawablegui.live.LivePredicateBool;
import org.openzen.drawablegui.live.MutableLiveObject;
import org.openzen.drawablegui.style.DStyleClass;
import org.openzen.drawablegui.style.DStylePath;
import org.openzen.zenscript.ide.ui.IDEAspectBarControl;
import org.openzen.zenscript.ide.ui.icons.ScalableCloseIcon;
import org.openzen.zenscript.ide.ui.icons.ScalableMaximizeIcon;
import org.openzen.zenscript.ide.ui.icons.ScalableMinimizeIcon;

/**
 *
 * @author Hoofdgebruiker
 */
public class AspectBarView extends BaseComponentGroup {
	private final MutableLiveObject<DSizing> sizing = DSizing.create();
	private final DStyleClass styleClass;
	private final IDEAspectBar aspectBar;
	
	private DIRectangle bounds;
	private DUIContext context;
	private DStylePath path;
	private AspectBarStyle style;
	private DFontMetrics activeToolbarTitleFontMetrics;
	private boolean showWindowControls;
	
	private final LiveMappedList<IDEAspectToolbar, AspectBarSelectorButton> selectorButtons;
	private LiveMappedList<IDEAspectBarControl, DComponent> activeToolbarComponents;
	
	private final ListenerHandle<LiveList.Listener<IDEAspectToolbar>> listener;
	private DPath aspectBarShape;
	private DPath windowControlsShape;
	private int aspectSelectorEndX = 0;
	
	private WindowActionButton minimize;
	private WindowActionButton maximizeRestore;
	private WindowActionButton close;
	
	private final ListenerHandle<LiveObject.Listener<DSizing>> minimizeRelayout;
	private final ListenerHandle<LiveObject.Listener<DSizing>> maximizeRestoreRelayout;
	private final ListenerHandle<LiveObject.Listener<DSizing>> closeRelayout;
	
	public AspectBarView(DStyleClass styleClass, IDEAspectBar aspectBar) {
		this.styleClass = styleClass;
		this.aspectBar = aspectBar;
		
		aspectBar.active.addListener(this::onActiveChanged);
		listener = aspectBar.toolbars.addListener(new ToolbarListListener());
		
		minimize = new WindowActionButton(scale -> new ScalableMinimizeIcon(scale), e -> context.getWindow().minimize());
		minimizeRelayout = minimize.getSizing().addListener((a, b) -> layout());
		maximizeRestore = new WindowActionButton(ScalableMaximizeIcon::new, e -> {
			if (context.getWindow().getWindowState().getValue() == DUIWindow.State.MAXIMIZED)
				context.getWindow().restore();
			else
				context.getWindow().maximize();
		});
		maximizeRestoreRelayout = maximizeRestore.getSizing().addListener((a, b) -> layout());
		close = new WindowActionButton(scale -> new ScalableCloseIcon(scale), e -> context.getWindow().close());
		closeRelayout = close.getSizing().addListener((a, b) -> layout());
		
		selectorButtons = new LiveMappedList<>(
				aspectBar.toolbars,
				bar -> {
					LiveBool buttonActive = new LivePredicateBool<>(aspectBar.active, activeBar -> activeBar == bar);
					AspectBarSelectorButton button = new AspectBarSelectorButton(DStyleClass.EMPTY, bar.icon, buttonActive, bar.description, e -> aspectBar.active.setValue(bar));
					if (context != null)
						button.setContext(path, context);
					
					return button;
				});
		selectorButtons.addListener(new SelectorButtonListListener());
		
		if (aspectBar.toolbars.size() > 0)
			aspectBar.active.setValue(aspectBar.toolbars.get(0));
	}
	
	@Override
	public void setContext(DStylePath parent, DUIContext context) {
		this.context = context;
		this.path = parent.getChild("aspectbar", styleClass);
		this.style = new AspectBarStyle(context.getStylesheets().get(context, path));
		activeToolbarTitleFontMetrics = context.getFontMetrics(style.activeToolbarTitleFont);
		showWindowControls = !context.getWindow().hasTitleBar();
		
		sizing.setValue(new DSizing(0, style.height));
		
		for (DComponent selectorButton : selectorButtons)
			selectorButton.setContext(path, context);
		
		if (activeToolbarComponents != null)
			for (DComponent component : activeToolbarComponents)
				component.setContext(path, context);
		
		minimize.setContext(path, context);
		maximizeRestore.setContext(path, context);
		close.setContext(path, context);
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
		
		if (context != null) {
			layout();
			context.repaint(bounds);
		}
	}

	@Override
	public void paint(DCanvas canvas) {
		canvas.pushBounds(bounds);
		canvas.fillRectangle(bounds.x, bounds.y, bounds.width, bounds.height, style.backgroundColor);
		canvas.fillRectangle(bounds.x, bounds.y + bounds.height - style.marginBottom, bounds.width, style.marginBottom, style.backgroundColorBottom);
		
		for (DComponent button : selectorButtons) {
			if (button.getBounds() == null)
				continue;
			
			button.paint(canvas);
		}
		
		canvas.shadowPath(
				aspectBarShape,
				DTransform2D.IDENTITY,
				style.foregroundColor,
				style.aspectBarShadow);
		
		if (aspectBar.active.getValue() != null) {
			int y = bounds.y
					+ style.aspectBarPaddingTop
					+ (int)(activeToolbarTitleFontMetrics.getAscent() * 0.35f)
					+ (bounds.height - style.aspectBarPaddingTop) / 2;
			int x = aspectSelectorEndX + style.aspectSelectorToToolbarSpacing;
			canvas.drawText(style.activeToolbarTitleFont, style.activeToolbarTitleColor, x, y, aspectBar.active.getValue().title);
			canvas.fillRectangle(
					x + activeToolbarTitleFontMetrics.getWidth(aspectBar.active.getValue().title) + 8,
					bounds.y + style.aspectBarPaddingTop + 8,
					2,
					bounds.height - style.aspectBarPaddingTop - 16,
					0xFFCCCCCC);
		}
		
		if (activeToolbarComponents != null) {
			for (DComponent component : activeToolbarComponents)
				component.paint(canvas);
		}
		
		if (showWindowControls) {
			canvas.shadowPath(windowControlsShape, DTransform2D.IDENTITY, style.backgroundColor, style.windowControlShadow);
			minimize.paint(canvas);
			maximizeRestore.paint(canvas);
			close.paint(canvas);
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
	
	private void layout() {
		if (bounds == null)
			return;
		
		layoutAspectSelectorButtons();
		layoutActiveToolbarComponents();
		
		int x = bounds.x + bounds.width;
		DSizing closeSize = close.getSizing().getValue();
		DSizing maximizeRestoreSize = maximizeRestore.getSizing().getValue();
		DSizing minimizeSize = minimize.getSizing().getValue();
		
		x -= closeSize.preferredWidth;
		close.setBounds(new DIRectangle(x, bounds.y, closeSize.preferredWidth, closeSize.preferredHeight));
		x -= maximizeRestoreSize.preferredWidth;
		maximizeRestore.setBounds(new DIRectangle(x, bounds.y, maximizeRestoreSize.preferredWidth, maximizeRestoreSize.preferredHeight));
		x -= minimizeSize.preferredWidth;
		minimize.setBounds(new DIRectangle(x, bounds.y, minimizeSize.preferredWidth, minimizeSize.preferredHeight));
		
		calculateAspectBarShape();
		context.repaint(bounds);
	}
	
	private void layoutAspectSelectorButtons() {
		if (bounds == null)
			return;
		
		int x = bounds.x + style.aspectSelectorPaddingLeft;
		
		for (DComponent component : selectorButtons) {
			int width = component.getSizing().getValue().preferredWidth;
			int height = component.getSizing().getValue().preferredHeight;
			int y = bounds.y + (bounds.height - style.marginBottom - style.aspectSelectorBottomSize - height) / 2;
			component.setBounds(new DIRectangle(x, y, width, height));
			
			x += width + style.aspectSelectorButtonSpacing;
		}
		
		aspectSelectorEndX = x;
	}
	
	private void layoutActiveToolbarComponents() {
		if (bounds == null || bounds.height < (style.aspectBarPaddingTop + style.controlPaddingTop + style.controlPaddingBottom))
			return;
		
		int x = aspectSelectorEndX
				+ style.aspectSelectorToToolbarSpacing
				+ activeToolbarTitleFontMetrics.getWidth(aspectBar.active.getValue().title)
				+ style.toolbarTitleToControlsSpacing;
		
		int y = style.aspectBarPaddingTop + style.controlPaddingTop;
		int height = bounds.height - y - style.controlPaddingBottom;
		
		for (DComponent toolbarComponent : activeToolbarComponents) {
			int width = toolbarComponent.getSizing().getValue().preferredWidth;
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
		for (DComponent component : selectorButtons)
			children.accept(component);
		
		if (showWindowControls) {
			children.accept(minimize);
			children.accept(maximizeRestore);
			children.accept(close);
		}
	}

	@Override
	protected DComponent findChild(Predicate<DComponent> predicate) {
		if (activeToolbarComponents == null)
			return null;
		
		for (DComponent component : activeToolbarComponents)
			if (predicate.test(component))
				return component;
		for (DComponent component : selectorButtons)
			if (predicate.test(component))
				return component;
		
		if (showWindowControls) {
			if (predicate.test(minimize))
				return minimize;
			if (predicate.test(maximizeRestore))
				return maximizeRestore;
			if (predicate.test(close))
				return close;
		}
		
		return null;
	}
	
	private void calculateAspectBarShape() {
		int toX = aspectSelectorEndX;
		
		aspectBarShape = tracer -> {
			int height = bounds.height - style.marginBottom;
			int baseY = bounds.y + height - style.aspectSelectorBottomSize;
			int barBaseX = toX + style.aspectSelectorToToolbarSpacing;
			int barBaseY = bounds.y + style.aspectBarPaddingTop;
			
			tracer.moveTo(bounds.x, baseY);
			tracer.lineTo(toX - 3 * context.getScale(), baseY);
			tracer.bezierCubic(
					toX + 0 * context.getScale(), baseY,
					toX + 2 * context.getScale(), baseY,
					toX + 3.5f * context.getScale(), baseY - 4 * context.getScale());
			tracer.lineTo(barBaseX - 3.5f * context.getScale(), barBaseY + 4 * context.getScale());
			tracer.bezierCubic(
					barBaseX - 2 * context.getScale(), barBaseY,
					barBaseX - 0 * context.getScale(), barBaseY,
					barBaseX + 3 * context.getScale(), barBaseY);
			
			if (showWindowControls) {
				int spacingLeft = style.windowControlSpacingLeft;
				int spacingBottom = style.windowControlSpacingBottom;
				int cornerX = minimize.getBounds().x - spacingLeft;
				int cornerY = minimize.getBounds().y + minimize.getBounds().height + spacingBottom;
				tracer.lineTo(cornerX, bounds.y + style.aspectBarPaddingTop);
				tracer.lineTo(cornerX, cornerY);
				tracer.lineTo(bounds.x + bounds.width, cornerY);
			} else {
				tracer.lineTo(bounds.x + bounds.width, bounds.y + style.aspectBarPaddingTop);
			}
			
			tracer.lineTo(bounds.x + bounds.width, bounds.y + height);
			tracer.lineTo(bounds.x, bounds.y + height);
			tracer.close();
		};
		
		if (showWindowControls) {
			DIRectangle minimizeBounds = minimize.getBounds();
			DIRectangle closeBounds = close.getBounds();
			windowControlsShape = DPath.rectangle(
					minimizeBounds.x,
					minimizeBounds.y,
					closeBounds.x + closeBounds.width - minimizeBounds.x,
					closeBounds.height);
		}
	}
	
	private class ToolbarListListener implements LiveList.Listener<IDEAspectToolbar> {

		@Override
		public void onInserted(int index, IDEAspectToolbar value) {
			if (index == 0 && activeToolbarComponents == null)
				aspectBar.active.setValue(value);
		}

		@Override
		public void onChanged(int index, IDEAspectToolbar oldValue, IDEAspectToolbar newValue) {
			if (oldValue == aspectBar.active.getValue())
				aspectBar.active.setValue(newValue);
		}

		@Override
		public void onRemoved(int index, IDEAspectToolbar oldValue) {
			if (oldValue == aspectBar.active.getValue())
				aspectBar.active.setValue(aspectBar.toolbars.size() == 0 ? null : aspectBar.toolbars.get(0));
		}
	}
	
	private class SelectorButtonListListener implements LiveList.Listener<AspectBarSelectorButton> {
		@Override
		public void onInserted(int index, AspectBarSelectorButton value) {
			layout();
		}

		@Override
		public void onChanged(int index, AspectBarSelectorButton oldValue, AspectBarSelectorButton newValue) {
			layoutAspectSelectorButtons();
		}

		@Override
		public void onRemoved(int index, AspectBarSelectorButton oldValue) {
			layout();
		}
	}
}
