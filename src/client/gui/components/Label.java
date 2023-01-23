package client.gui.components;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import client.gui.GUI;
import client.gui.ScreenUtils;
import general.Point;
import general.Rectangle;

public class Label extends Component {

	public String text;
	public Font font;
	public Color col;
	/**
	 * If set then label will be drawn centered on point, else
	 * will be drawn left centered to point
	 */
	private boolean centered; 
	
	public Label(Point point, String text, Font font, Color col) {
		super(new Rectangle(point.x, point.y, 0, 0));
		this.text = text;
		this.font = font;
		this.col = col;
		centered = false;
	}
	
	public boolean isCentered() {return centered;}
	public void setCentered(boolean c) {centered = c;} 

	@Override
	public void draw(Graphics2D g) {
		GUI.getInstance().getScreenUtils().drawLabel(g, this);
		super.draw(g);
	}
}
