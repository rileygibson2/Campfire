package client.gui;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import client.gui.components.Component;
import general.Point;
import general.Rectangle;

public abstract class Element {

	public Element parent;
	public Rectangle r;
	public int priority;
	public List<Component> components;

	public Element(Rectangle r, Element parent) {
		this.r = r;
		this.parent = parent;
		this.components = new ArrayList<Component>();
		this.priority = 1;
	}

	public void increasePriority() {this.priority += 1;}
	public void decreasePriority() {this.priority -= 1;}

	public void sortComponents() {
		Collections.sort(components, new Comparator<Component>() {
			public int compare(Component c1, Component c2) {
				if (c1.priority>c2.priority) return -1;
				if (c1.priority<c2.priority) return 1;
				return 0;
			}
		});
	}

	/**
	 * Scales this elements rec to actual size on the screen.
	 * E.g if this element has a height of 100 but it is nested inside
	 * one or more other components, then will get actual size of this
	 * component.
	 * 
	 * @return
	 */
	public Rectangle getRealRec() {
		if (parent==null) return r;
		Rectangle pR = parent.getRealRec();
		Rectangle rNew = new Rectangle();

		rNew.x = pR.x+(r.x/100d)*pR.width;
		rNew.y = pR.y+(r.y/100d)*pR.height;
		rNew.width = (r.width/100d)*pR.width;
		rNew.height = (r.height/100d)*pR.height;

		return rNew;
	}

	/**
	 * Allows you to scale any rectangle as if it were being processed like this
	 * element. E.g you can give it a rectangle with 2x this elements height and get
	 * the real value for that.
	 * 
	 * @param r
	 * @return
	 */
	public Rectangle getRealRec(Rectangle r) {
		if (parent==null) return r;
		Rectangle pR = parent.getRealRec();
		Rectangle rNew = new Rectangle();

		rNew.x = pR.x+(r.x/100d)*pR.width;
		rNew.y = pR.y+(r.y/100d)*pR.height;
		rNew.width = (r.width/100d)*pR.width;
		rNew.height = (r.height/100d)*pR.height;

		return rNew;
	}

	/**
	 * Scales a point real point on the screen and turns it into a percentage
	 * position in this element. This method respects nested components.
	 * @param p
	 * @return
	 */
	public Point scalePoint(Point p) {
		//Scale real point to percentage point
		p.x = ScreenUtils.cWR(p.x);
		p.y = ScreenUtils.cHR(p.y);

		Rectangle r = getRealRec();
		Point pNew = new Point();
		//Scale to percentage of this element
		pNew.x = (p.x-r.x)/r.width;
		pNew.y = (p.y-r.y)/r.height;
		return pNew;
	}

	public void drawComponents(Graphics2D g) {
		sortComponents();
		Collections.reverse(components);
		for (Component c : components) c.draw(g);
	}
	
	public void drawComponentShadows(Graphics2D g) {
		sortComponents();
		Collections.reverse(components);
		for (Component c : components) {
			if (c.hasShadow) ScreenUtils.drawShadow(g, c);
		}
	}

	public boolean isOver(Point p) {
		Rectangle rS = getRealRec();
		if (p.x>=ScreenUtils.cW(rS.x)
				&& p.x<=ScreenUtils.cW(rS.x+rS.width)
				&& p.y>=ScreenUtils.cH(rS.y)
				&& p.y<=ScreenUtils.cH(rS.y+rS.height)) {
			return true;
		}
		return false;
	}

	/**
	 * Calculates whether point p is over rectangle rec when rec has been
	 * scaled to this elements actual size;
	 * @param p
	 * @param rec
	 * @return
	 */
	public boolean isOver(Point p, Rectangle rec) {
		Rectangle rS = getRealRec(rec);
		if (p.x>=ScreenUtils.cW(rS.x)
				&& p.x<=ScreenUtils.cW(rS.x+rS.width)
				&& p.y>=ScreenUtils.cH(rS.y)
				&& p.y<=ScreenUtils.cH(rS.y+rS.height)) {
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return this.getClass()+" - p "+priority;
	}
}
