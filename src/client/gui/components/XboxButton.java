package client.gui.components;

import java.awt.Color;
import java.awt.Graphics2D;

import client.gui.Element;
import client.gui.ScreenUtils;
import general.Rectangle;

public class XboxButton extends Button {

	public Color start;
	public Color end;
	public String label;
	
	public XboxButton(Rectangle r, String label, Color start, Color end, Element parent) {
		super(r, null, parent);
		this.label = label;
		this.start = start;
		this.end = end;
		hasShadow = true;
	}
	
	@Override
	public void draw(Graphics2D g) {
		ScreenUtils.drawXBoxButton(g, this);
	}

}
