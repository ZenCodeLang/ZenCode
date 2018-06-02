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
import org.openzen.drawablegui.DFontMetrics;
import org.openzen.drawablegui.DPath;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.listeners.DIRectangle;
import org.openzen.drawablegui.live.LiveObject;
import org.openzen.drawablegui.live.SimpleLiveObject;
import org.openzen.zenscript.ide.ui.IDEAspectBar;
import org.openzen.zenscript.ide.ui.IDEAspectToolbar;
import org.openzen.drawablegui.DUIContext;
import org.openzen.drawablegui.DUIWindow;
import org.openzen.drawablegui.listeners.ListenerHandle;
import org.openzen.drawablegui.live.LiveBool;
import org.openzen.drawablegui.live.LiveList;
import org.openzen.drawablegui.live.LiveMappedList;
import org.openzen.drawablegui.live.LivePredicateBool;
import org.openzen.drawablegui.live.SimpleLiveBool;
import org.openzen.drawablegui.style.DStyleClass;
import org.openzen.drawablegui.style.DStylePath;
import org.openzen.zenscript.ide.ui.IDEAspectBarControl;
import org.openzen.zenscript.ide.ui.icons.CloseIcon;
import org.openzen.zenscript.ide.ui.icons.ScalableCloseIcon;
import org.openzen.zenscript.ide.ui.icons.ScalableMaximizeIcon;
import org.openzen.zenscript.ide.ui.icons.ScalableMinimizeIcon;

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
	private DFontMetrics activeToolbarTitleFontMetrics;
	private boolean showWindowControls;
	
	private final LiveMappedList<IDEAspectToolbar, AspectBarSelectorButton> selectorButtons;
	public final LiveObject<IDEAspectToolbar> active = new SimpleLiveObject<>(null);
	private LiveMappedList<IDEAspectBarControl, DComponent> activeToolbarComponents;
	
	private final ListenerHandle<LiveList.Listener<IDEAspectToolbar>> listener;
	private DPath aspectBarShape;
	private DPath windowControlsShape;
	private int aspectSelectorEndX = 0;
	
	private WindowActionButton minimize;
	private WindowActionButton maximizeRestore;
	private WindowActionButton close;
	
	private final ListenerHandle<LiveObject.Listener<DDimensionPreferences>> minimizeRelayout;
	private final ListenerHandle<LiveObject.Listener<DDimensionPreferences>> maximizeRestoreRelayout;
	private final ListenerHandle<LiveObject.Listener<DDimensionPreferences>> closeRelayout;
	
	public AspectBarView(DStyleClass styleClass, IDEAspectBar aspectBar) {
		this.styleClass = styleClass;
		this.aspectBar = aspectBar;
		
		active.addListener(this::onActiveChanged);
		listener = aspectBar.aspectToolbars.addListener(new ToolbarListListener());
		
		minimize = new WindowActionButton(scale -> new ScalableMinimizeIcon(scale), e -> context.getWindow().minimize());
		minimizeRelayout = minimize.getDimensionPreferences().addListener((a, b) -> layout());
		maximizeRestore = new WindowActionButton(ScalableMaximizeIcon::new, e -> {
			if (context.getWindow().getWindowState().getValue() == DUIWindow.State.MAXIMIZED)
				context.getWindow().restore();
			else
				context.getWindow().maximize();
		});
		maximizeRestoreRelayout = maximizeRestore.getDimensionPreferences().addListener((a, b) -> layout());
		close = new WindowActionButton(scale -> new ScalableCloseIcon(scale), e -> context.getWindow().close());
		closeRelayout = close.getDimensionPreferences().addListener((a, b) -> layout());
		
		selectorButtons = new LiveMappedList<>(
				aspectBar.aspectToolbars,
				bar -> {
					LiveBool buttonActive = new LivePredicateBool<>(active, activeBar -> activeBar == bar);
					AspectBarSelectorButton button = new AspectBarSelectorButton(DStyleClass.EMPTY, bar.icon, buttonActive, e -> active.setValue(bar));
					if (context != null)
						button.setContext(path, context);
					
					return button;
				});
		
		if (aspectBar.aspectToolbars.size() > 0)
			active.setValue(aspectBar.aspectToolbars.get(0));
	}
	
	@Override
	public void setContext(DStylePath parent, DUIContext context) {
		this.context = context;
		this.path = parent.getChild("aspectbar", styleClass);
		this.style = new AspectBarStyle(context.getStylesheets().get(context, path));
		activeToolbarTitleFontMetrics = context.getFontMetrics(style.activeToolbarTitleFont);
		showWindowControls = !context.getWindow().hasTitleBar();
		
		for (DComponent selectorButton : selectorButtons)
			selectorButton.setContext(path, context);
		
		if (bounds != null) {
			layout();
			active.setValue(active.getValue());
		}
		
		if (activeToolbarComponents != null)
			for (DComponent component : activeToolbarComponents)
				component.setContext(path, context);
		
		minimize.setContext(path, context);
		maximizeRestore.setContext(path, context);
		close.setContext(path, context);
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
			layout();
			context.repaint(bounds);
		}
	}

	@Override
	public void paint(DCanvas canvas) {
		canvas.pushBounds(bounds);
		canvas.fillRectangle(bounds.x, bounds.y, bounds.width, bounds.height, style.backgroundColor);
		
		for (DComponent button : selectorButtons)
			button.paint(canvas);
		
		canvas.shadowPath(
				aspectBarShape,
				DTransform2D.IDENTITY,
				style.aspectBarShadow);
		canvas.fillPath(
				aspectBarShape,
				DTransform2D.IDENTITY,
				style.foregroundColor);
		
		if (active.getValue() != null) {
			int y = bounds.y
					+ style.aspectBarPaddingTop
					+ (int)(activeToolbarTitleFontMetrics.getAscent() * 0.35f)
					+ (bounds.height - style.aspectBarPaddingTop) / 2;
			int x = aspectSelectorEndX + style.aspectSelectorToToolbarSpacing;
			canvas.drawText(style.activeToolbarTitleFont, style.activeToolbarTitleColor, x, y, active.getValue().title);
			canvas.fillRectangle(
					x + activeToolbarTitleFontMetrics.getWidth(active.getValue().title) + 8,
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
			canvas.shadowPath(windowControlsShape, DTransform2D.IDENTITY, style.windowControlShadow);
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
		DDimensionPreferences closeSize = close.getDimensionPreferences().getValue();
		DDimensionPreferences maximizeRestoreSize = maximizeRestore.getDimensionPreferences().getValue();
		DDimensionPreferences minimizeSize = minimize.getDimensionPreferences().getValue();
		
		x -= closeSize.preferredWidth;
		close.setBounds(new DIRectangle(x, bounds.y, closeSize.preferredWidth, closeSize.preferredHeight));
		x -= maximizeRestoreSize.preferredWidth;
		maximizeRestore.setBounds(new DIRectangle(x, bounds.y, maximizeRestoreSize.preferredWidth, maximizeRestoreSize.preferredHeight));
		x -= minimizeSize.preferredWidth;
		minimize.setBounds(new DIRectangle(x, bounds.y, minimizeSize.preferredWidth, minimizeSize.preferredHeight));
		
		calculateAspectBarShape();
	}
	
	private void layoutAspectSelectorButtons() {
		if (bounds == null)
			return;
		
		int x = bounds.x + style.aspectSelectorPaddingLeft;
		
		for (DComponent component : selectorButtons) {
			int width = component.getDimensionPreferences().getValue().preferredWidth;
			int height = component.getDimensionPreferences().getValue().preferredHeight;
			int y = bounds.y + (bounds.height - style.aspectSelectorBottomSize - height) / 2;
			component.setBounds(new DIRectangle(x, y, width, height));
			
			x += width + style.aspectSelectorButtonSpacing;
		}
		
		aspectSelectorEndX = x;
	}
	
	private void layoutActiveToolbarComponents() {
		if (bounds == null)
			return;
		
		int x = aspectSelectorEndX
				+ style.aspectSelectorToToolbarSpacing
				+ activeToolbarTitleFontMetrics.getWidth(active.getValue().title)
				+ style.toolbarTitleToControlsSpacing;
		
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
			int baseY = bounds.y + bounds.height - style.aspectSelectorBottomSize;
			tracer.moveTo(bounds.x, baseY);
			tracer.lineTo(toX, baseY);
			tracer.bezierCubic(
					toX + 6 * context.getScale(), baseY,
					toX + style.aspectSelectorToToolbarSpacing - 6 * context.getScale(), bounds.y + style.aspectBarPaddingTop,
					toX + style.aspectSelectorToToolbarSpacing, bounds.y + style.aspectBarPaddingTop);
			
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
			
			tracer.lineTo(bounds.x + bounds.width, bounds.y + bounds.height);
			tracer.lineTo(bounds.x, bounds.y + bounds.height);
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
				active.setValue(value);
			
			layout();
		}

		@Override
		public void onChanged(int index, IDEAspectToolbar oldValue, IDEAspectToolbar newValue) {
			if (oldValue == active.getValue())
				active.setValue(newValue);
			
			layoutAspectSelectorButtons();
		}

		@Override
		public void onRemoved(int index, IDEAspectToolbar oldValue) {
			if (oldValue == active.getValue())
				active.setValue(aspectBar.aspectToolbars.size() == 0 ? null : aspectBar.aspectToolbars.get(0));
			
			layout();
		}
	}
}
