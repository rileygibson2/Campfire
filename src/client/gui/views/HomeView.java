package client.gui.views;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import client.Client;
import client.gui.components.Button;
import client.gui.components.Component;
import client.gui.components.Image;
import client.gui.components.Label;
import client.gui.components.XboxButton;
import general.Point;
import general.Rectangle;

public class HomeView extends View {
	
	public HomeView() {
		super(ViewType.Home, new Rectangle(0, 0, 100, 100));
		components.add(new Label(new Point(8, 53), "RileyCom", new Font("Geneva", Font.ROMAN_BASELINE, 40), new Color(230, 230, 230), this));
		components.add(new XboxButton(new Rectangle(70, 60, 7, 7), "A", new Color(89, 141, 19), new Color(112, 255, 12), this));
		components.add(new XboxButton(new Rectangle(80, 40, 7, 7), "B", new Color(126, 0, 14), new Color(191, 0, 9), this));
		components.add(new XboxButton(new Rectangle(60, 40, 7, 7), "X", new Color(12, 45, 241), new Color(51, 100, 253), this));
		components.add(new XboxButton(new Rectangle(70, 20, 7, 7), "Y", new Color(233, 144, 12), new Color(251, 200, 8), this));
		
		Button b = new Button(new Rectangle(10, 60, 5, 10), new Color(100, 100, 100), this);
		b.setClick(() -> Client.cGUI.changeView(ViewType.Settings));
		b.components.add(new Image(new Rectangle(10, 10, 80, 80), "settings.png", b));
		components.add(b);
	}

	@Override
	public void enter() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doMove(Point p) {
		boolean i = false;
		for (Component c : components) {
			if (c instanceof Button) {
				Button b = (Button) c;
				if (b.isOver(new Point(p.x, p.y))) {
					//c.resetHover(b);
					//b.hover();
					i = true;
				}
			}
		}
		//if (!i) c.resetHover(null);
		
	}

	@Override
	public void draw(Graphics2D g) {
		drawComponents(g);
	}

}
