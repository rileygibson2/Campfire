package client.gui.components;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import client.gui.Element;
import client.gui.ScreenUtils;
import general.Point;
import general.Rectangle;

public class Image extends Component {

	public String src;
	
	public Image(Rectangle r, String src, Element parent) {
		super(r, parent);
		this.src = src;
	}

	@Override
	public void doClick(Point p) {
		System.out.println("Image clicked");
	}

	@Override
	public void doHover() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doUnhover() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void doKeyPress(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void draw(Graphics2D g) {
		super.draw(g);
		ScreenUtils.drawImage(g, this);
	}

}
