package client.gui.components;

import java.awt.Color;
import java.awt.Graphics2D;

import client.gui.GUI;
import client.gui.ScreenUtils;
import general.Rectangle;

public class SimpleBox extends Component {

	public Color col;
	private boolean rounded;
	private boolean oval;
	private int[] roundedCorners;
	
	public SimpleBox(Rectangle r) {
		super(r);
		rounded = false;
		oval = false;
		setVisible(false);
	}
	
	public SimpleBox(Rectangle r, Color col) {
		super(r);
		this.col = col;
		rounded = false;
		oval = false;
	}
	
	public void setRounded(boolean r) {rounded = r;}
	public void setRounded(int[] r) {
		rounded = true;
		roundedCorners = r;
	}
	public boolean isRounded() {return rounded;}
	public int[] getRoundedCorners() {return roundedCorners;}
	
	public void setOval(boolean o) {oval = o;}
	public boolean isOval() {return oval;}
	
	@Override
	public void draw(Graphics2D g) {
		GUI.getInstance().getScreenUtils().drawSimpleBox(g, this);
		super.draw(g);
	}
}
