package client.gui;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import client.gui.components.Component;
import general.Point;
import general.Rectangle;

public abstract class Element {

	private Element parent;
	protected Rectangle r; //Current values
	private Rectangle rO; //Original values
	private List<Component> components;
	private Lock componentsLock; //Primitive lock on components list
	
	/**
	 * If this is set then methods will not respect nesting.
	 * The element will treat it's rectangle as a proportion of the actual
	 * screen, not as a proportion of it's parents rectangle.
	 */
	private boolean absolute;

	public Element(Rectangle r) {
		this.r = r;
		this.rO = r.clone();
		this.components = new ArrayList<Component>();
		componentsLock = new ReentrantLock();
	}
	
	public void setX(double x) {r.x = x;}
	public void setY(double y) {r.y = y;}
	public void setWidth(double width) {r.width = width;}
	public void setHeight(double height) {r.height = height;}
	
	public double getX() {return r.x;}
	public double getY() {return r.y;}
	public double getWidth() {return r.width;}
	public double getHeight() {return r.height;}
	
	public void setRec(Rectangle r) {this.r = r;}
	public void setRecToOriginal() {r = rO.clone();}
	public Rectangle getRec() {return r;}
	public Rectangle getOriginalRec() {return rO;}
	public void changeOriginalRec(Rectangle r) {rO = r;}
	public void updateOriginalRec() {rO = r.clone();}
	
	public Element getParent() {return parent;}
	public void setParent(Element e) {parent = e;}
	
	public boolean isAbsolute() {return absolute;}
	public void setAbsolute(boolean a) {absolute = a;}
	
	public List<Component> getComponents() {
		List<Component> copy = new ArrayList<>();
		componentsLock.lock();
		for (Component c : components) copy.add(c);
		componentsLock.unlock();
		return copy;
	}
	
	public List<Component> getComponents(Class<?> clazz) {
		List<Component> results = new ArrayList<>();
		componentsLock.lock();
		for (Component c : components) {
			if (clazz.isInstance(c)) results.add(c);
		}
		componentsLock.unlock();
		if (!results.isEmpty()) return results;
		return null;
	}
	
	public void addComponent(Component c) {
		c.setParent(this);
		componentsLock.lock();
		components.add(c);
		componentsLock.unlock();
	}
	public void removeComponent(Component c) {
		componentsLock.lock();
		components.remove(c);
		componentsLock.unlock();
	}
	public void removeComponents(Collection<Component> toRemove) {
		componentsLock.lock();
		components.removeAll(toRemove);
		componentsLock.unlock();
	}

	public void sortComponents() {
		Collections.sort(components, new Comparator<Component>() {
			public int compare(Component c1, Component c2) {
				if (c1.getPriority()>c2.getPriority()) return -1;
				if (c1.getPriority()<c2.getPriority()) return 1;
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
		if (absolute||parent==null) return r;
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
		if (absolute||parent==null) return r;
		Rectangle pR = parent.getRealRec();
		Rectangle rNew = new Rectangle();

		rNew.x = pR.x+(r.x/100d)*pR.width;
		rNew.y = pR.y+(r.y/100d)*pR.height;
		rNew.width = (r.width/100d)*pR.width;
		rNew.height = (r.height/100d)*pR.height;

		return rNew;
	}
	
	public boolean isOver(Point p) {
		Rectangle rS = getRealRec();
		if (p.x>=GUI.getInstance().getScreenUtils().cW(rS.x)
				&& p.x<=GUI.getInstance().getScreenUtils().cW(rS.x+rS.width)
				&& p.y>=GUI.getInstance().getScreenUtils().cH(rS.y)
				&& p.y<=GUI.getInstance().getScreenUtils().cH(rS.y+rS.height)) {
			return true;
		}
		
		/*
		 * At this point look at all components recursivly incase
		 * a component is setup outside the bounds of this box,
		 * but isOver should still trigger so events can reach that component.
		 */
		for (Component c : components) {
			if (c.isOver(p)) return true;
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
	@Deprecated
	public boolean isOver(Point p, Rectangle rec) {
		Rectangle rS = getRealRec(rec);
		if (p.x>=GUI.getInstance().getScreenUtils().cW(rS.x)
				&& p.x<=GUI.getInstance().getScreenUtils().cW(rS.x+rS.width)
				&& p.y>=GUI.getInstance().getScreenUtils().cH(rS.y)
				&& p.y<=GUI.getInstance().getScreenUtils().cH(rS.y+rS.height)) {
			return true;
		}
		return false;
	}

	/**
	 * Scales a point real point on the screen and turns it into a percentage
	 * position in this element. This method respects nested components.
	 * @param p
	 * @return
	 */
	public Point scalePoint(Point p) {
		//Scale real point to percentage point
		Point pNew = p.clone();
		pNew.x = GUI.getInstance().getScreenUtils().cWR(pNew.x);
		pNew.y = GUI.getInstance().getScreenUtils().cHR(pNew.y);

		Rectangle r = getRealRec();
		//Scale to percentage of this element
		pNew.x = (pNew.x-r.x)/r.width;
		pNew.y = (pNew.y-r.y)/r.height;
		return pNew;
	}

	public void drawComponents(Graphics2D g) {
		componentsLock.lock();
		sortComponents();
		Collections.reverse(components);
		for (Component c : components) {
			if (c.isVisible()) c.draw(g);
		}
		componentsLock.unlock();
	}
	
	public void drawComponentShadows(Graphics2D g) {
		componentsLock.lock();
		sortComponents();
		Collections.reverse(components);
		for (Component c : components) {
			if (c.isVisible()&&c.hasShadow()) GUI.getInstance().getScreenUtils().drawShadow(g, c);
		}
		componentsLock.unlock();
	}
	
	public void draw(Graphics2D g) {
		drawComponentShadows(g);
		drawComponents(g);
	}
	
	public void destroy() {
		componentsLock.lock();
		for (Component c : getComponents()) c.destroy();
		componentsLock.unlock();
	}
	
	public void doClick(Point p) {
		componentsLock.lock();
		/*
		 * Components with higher priority may have overrided their isOver method,
		 * allowing them to take up more space for example when a selector is open.
		 * In this case we don't want an element potentially under an expanded element
		 * to register a click. Thats why we sort components first and only allow one 
		 * element to register a click at any one time.
		 */
		 
		sortComponents();
		Component clicked = null;
		for (Component c : getComponents()) {
			if (c.isVisible()&&c.isOver(p)) {
				c.doClick(p); //Will recur down
				clicked = c;
				break;
			}
		}
		
		//Deselect all non clicked components
		for (Component c : getComponents()) {
			if (clicked==null||c!=clicked) {
				if (c.isVisible()&&c.isSelected()) c.doDeselect();
			}
		}
		componentsLock.unlock();
	}
	
	public void doMove(Point p) {
		componentsLock.lock();
		sortComponents();
		
		for (Component c : getComponents()) {
			if (!c.isVisible()) continue;
			if (c.isOver(p)) {
				c.doHover();
				c.doMove(p); //Will recur down
			}
			else c.doUnhover();
		}
		componentsLock.unlock();
	}
	
	public void doDrag(Point entry, Point current) {
		componentsLock.lock();
		sortComponents();
		
		for (Component c : getComponents()) {
			if (!c.isVisible()) continue;
			if (c.isOver(current)) {
				c.doDrag(entry, current); //Will recur down
				break;
			}
		}
		componentsLock.unlock();
	}
	
	public void doScroll(Point p, int amount) {
		componentsLock.lock();
		sortComponents();
		
		for (Component c : getComponents()) {
			if (c.isVisible()&&c.isOver(p)) {
				c.doScroll(p, amount); //Will recur down
				break;
			}
		}
		componentsLock.unlock();
	}
	
	public void doKeyPress(KeyEvent k) {}; //Doesn't need to recur due to key listener registration in IO
	
	@Override
	public String toString() {
		return this.getClass().toString();
	}
}
