package client.gui.components;

import java.awt.Color;
import java.awt.Graphics2D;

import client.gui.GUI;
import client.gui.ScreenUtils;
import general.Rectangle;

public class GradientButton extends Button {

	public Color start;
	public Color end;
	
	public GradientButton(Rectangle r, Color start, Color end) {
		super(r, null);
		this.start = start;
		this.end = end;
		drawBox = false;
		hasShadow(true);
	}
	
	@Override
	public void draw(Graphics2D g) {
		GUI.getInstance().getScreenUtils().drawXBoxButton(g, this);
		super.draw(g);
	}

}
