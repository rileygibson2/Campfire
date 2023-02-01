package client.gui.components;

import java.awt.Color;
import java.awt.Graphics2D;

import client.gui.GUI;
import general.Rectangle;

public class GradientButton extends Button {

	public Color start;
	public Color end;
	
	public GradientButton(Rectangle r, Color start, Color end) {
		super(r, new Color(0, 0, 0, 0));
		this.start = start;
		this.end = end;
		mainBox.setOpacity(0);
		hasShadow(true);
	}
	
	@Override
	public void draw(Graphics2D g) {
		GUI.getInstance().getScreenUtils().drawXBoxButton(g, this);
		super.draw(g);
	}

}
