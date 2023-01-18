package client.gui.components;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import client.gui.Element;
import client.gui.ScreenUtils;
import general.Point;
import general.Rectangle;

public class Label extends Component {

	public String text;
	public Font font;
	public Color col;
	
	public Label(Point point, String text, Font font, Color col, Element parent) {
		super(new Rectangle(point.x, point.y, 0, 0), parent);
		this.text = text;
		this.font = font;
		this.col = col;
	}

	@Override
	public void doClick(Point p) {
		// TODO Auto-generated method stub
		
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
		ScreenUtils.drawLabel(g, this);
		super.draw(g);
	}
}
