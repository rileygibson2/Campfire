package client.gui.components;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import client.gui.Element;
import general.Point;
import general.Rectangle;

public abstract class Component extends Element {
	
	public Rectangle rO; //Original values
	public Rectangle r; //Actual values
	public int opacity;
	public boolean selected;
	
	//Visual
	public boolean hasShadow;
	
	public Component(Rectangle r, Element parent) {
		super (r, parent);
		this.r = r;
		this.rO = new Rectangle(r.x, r.y, r.width, r.height);
		opacity = 100;
		selected = false;
		hasShadow = false;
	}

	public int getOpacity() {
		return (int) ((opacity/100)*255);
	}
	
	public void draw(Graphics2D g) {
		drawComponentShadows(g);
		drawComponents(g);
	}
	
	public void doClick(Point p) {selected = true;}
	public void doDeselect() {selected = false;}
	public abstract void doHover();
	public abstract void doUnhover();
	public abstract void doKeyPress(KeyEvent k);
}
