package client.gui.views;

import java.awt.Color;
import java.awt.Graphics2D;

import client.gui.Animation;
import client.gui.Animation.Animations;
import client.gui.ScreenUtils;
import client.gui.components.Button;
import client.gui.components.XboxButton;
import general.Point;
import general.Rectangle;

public class RingView extends View {

	Animation pulse;
	
	public RingView() {
		super(ViewType.Call, new Rectangle(0, 0, 100, 100));
		Button b = new XboxButton(new Rectangle(45, 45, 10, 10), "A", new Color(89, 141, 19), new Color(112, 255, 12), this);
		components.add(b);
		pulse = new Animation(b, Animations.PulseButton);
	}

	@Override
	public void enter() {
		System.out.println("CALL VIEW ENTERING");
		if (pulse!=null) pulse.start();
	}

	@Override
	public void destroy() {
		System.out.println("CALL VIEW DESTROYING");
		if (pulse!=null&&pulse.isRunning()) pulse.stop();
	}

	@Override
	public void doMove(Point p) {
		// TODO Auto-generated method stub

	}

	@Override
	public void draw(Graphics2D g) {
		if (pulse!=null&&pulse.isRunning()) { //Draw pulse animation
			ScreenUtils.drawPulse(g, pulse);
		}
		drawComponents(g);
	}

}
