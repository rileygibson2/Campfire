package client.gui.components;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import client.gui.Animation;
import client.gui.Element;
import client.gui.ScreenUtils;
import general.Point;
import general.Rectangle;

public class Button extends Component {

	boolean isHovered;
	public Animation hoverAni;
	public Color col;
	public Runnable onClick;
	
	public String iconSrc;
	public Rectangle iconR;

	public Button(Rectangle r, Color col, Element parent) {
		super(r, parent);
		this.col = col;
		isHovered = false;
		hoverAni = null;
	}
	
	public void setClick(Runnable onClick) {
		this.onClick = onClick;
	}

	public void doClick(Point p) {
		System.out.println("Button clicked");
		if (onClick!=null) onClick.run();
	};


	@Override
	public void doHover() {
//		if (!isHovered) {
//			isHovered = true;
//			if (hoverAni!=null) {
//				hoverAni.stop();
//				hoverAni = null;
//			}
//			hoverAni = new Animation(this, Threads.HoverButton);
//			hoverAni.start();
//		}
	}

	@Override
	public void doUnhover() {
//		if (isHovered) {
//			isHovered = false;
//			if (hoverAni!=null) {
//				hoverAni.stop();
//				hoverAni = null;
//			}
//			if (size.x>sizeO.x&&size.y>sizeO.y) {
//				hoverAni = new Animation(this, Threads.UnhoverButton);
//				hoverAni.start();
//			}
//		}
	}
	
	@Override
	public void doKeyPress(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void draw(Graphics2D g) {
		ScreenUtils.drawButton(g, this);
		drawComponents(g);
	}
}
