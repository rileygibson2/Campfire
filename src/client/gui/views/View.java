package client.gui.views;

import java.awt.Graphics2D;

import client.gui.Element;
import client.gui.components.Component;
import general.Point;
import general.Rectangle;

public abstract class View extends Element {

	public enum ViewType {
		Home,
		Settings,
		Ring,
		Call
	};
	private final ViewType viewType;

	public View(ViewType v, Rectangle r) {
		super(r, null);
		this.viewType = v;
	}


	public abstract void enter();
	public abstract void destroy();
	public abstract void doMove(Point p);
	
	public void doClick(Point p) {
		/*
		 * Components with higher priority may have overrided their isOver method,
		 * allowing them to take up more space for example when a selector is open.
		 * In this case we don't want an element potentially under an expanded element
		 * to register a click. Thats why we sort components first and only allow one 
		 * element to register a click at any one time.
		 */
		sortComponents();
		Component clicked = null;
		for (Component c : components) {
			if (c.isOver(p)) {
				//Scale click to element percentage
				c.doClick(c.scalePoint(p));
				clicked = c;
				break;
			}
		}
		
		//Deselect all non clicked components
		for (Component c : components) {
			if (clicked==null||c!=clicked) {
				if (c.selected) c.doDeselect();
			}
		}
	}

	public ViewType getViewType() {return this.viewType;}

	public abstract void draw(Graphics2D g);
}
