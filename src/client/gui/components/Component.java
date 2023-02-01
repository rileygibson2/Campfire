package client.gui.components;

import client.gui.Element;
import general.Point;
import general.Rectangle;

public abstract class Component extends Element {

	private boolean selected;
	private boolean hovered;
	private boolean pauseHover; //Hover effects are pause when this is true

	public Runnable onClick;
	public Runnable onHover;
	public Runnable onUnHover;

	//Visual
	private boolean visible;
	private int priority;
	private double opacity;

	private boolean hasShadow;
	private boolean freezeShadow; //Stops shadow being updated with position changes
	private Rectangle shadowR;

	public Component(Rectangle r) {
		super (r);
		selected = false;
		hovered = false;
		pauseHover = false;
		hasShadow = false;
		freezeShadow = false;
		shadowR = r.clone();
		visible = true;
		priority = 1;
		opacity = 100;
	}

	public void setClickAction(Runnable r) {this.onClick = r;}
	public void setHoverAction(Runnable r) {this.onHover = r;}
	public void setUnHoverAction(Runnable r) {this.onUnHover = r;}

	public void increasePriority() {this.priority += 1;}
	public void decreasePriority() {this.priority -= 1;}
	public int getPriority() {return priority;}

	public double getOpacity() {return opacity;}
	
	public Component setOpacity(double d) {
		opacity = d;
		for (Component c : getComponents()) c.setOpacity(d); //Recur down
		return this; //To allow for chaining
	}

	public boolean isSelected() {return selected;}
	public void setSelected(boolean s) {selected = s;}

	public boolean isHovered() {return hovered;}
	public boolean isHoverPaused() {return pauseHover;}
	public void pauseHover() {pauseHover = true;}
	public void unpauseHover() {pauseHover = false;}

	public boolean hasShadow() {return hasShadow;}
	public void hasShadow(boolean s) {hasShadow = s;} 
	public Rectangle getShadowRec() {return shadowR;}
	public void freezeShadow() {freezeShadow = true;}
	public void unfreezeShadow() {freezeShadow = false;}

	public void setVisible(boolean v) {visible = v;}
	public boolean isVisible() {return visible;}
	
	public void removeFromParent() {
		if (getParent()!=null) getParent().removeComponent(this);
	}

	@Override
	public void doClick(Point p) {
		selected = true;
		if (onClick!=null) onClick.run();
		super.doClick(p);
	}

	public void doDeselect() {selected = false;}

	public void doHover() {
		if (pauseHover) return;
		if (!hovered) {
			hovered = true;
			if (onHover!=null) {
				onHover.run();
			}
		}
	}

	public void doUnhover() {
		if (pauseHover) return;
		if (hovered) {
			hovered = false;
			if (onUnHover!=null) onUnHover.run();
		}
		
		for (Component c : getComponents()) c.doUnhover();
	}

	@Override
	public void setX(double x) {
		super.setX(x);
		if (!freezeShadow) shadowR.x = x;
	}

	@Override
	public void setY(double y) {
		super.setY(y);
		if (!freezeShadow) shadowR.y = y;
	}

	@Override
	public void setWidth(double width) {
		super.setWidth(width);
		if (!freezeShadow) shadowR.width = width;
	}

	@Override
	public void setHeight(double height) {
		super.setHeight(height);
		if (!freezeShadow) shadowR.height = height;
	}
}
