package client.gui.views;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import client.Intercom;
import client.Special.Type;
import client.gui.GUI;
import client.gui.components.Button;
import client.gui.components.Component;
import client.gui.components.GradientButton;
import client.gui.components.Image;
import client.gui.components.Label;
import client.gui.components.SimpleBox;
import general.Pair;
import general.Point;
import general.Rectangle;
import threads.AnimationFactory;
import threads.AnimationFactory.Animations;
import threads.ThreadController;

public class SpecialView extends View {

	ThreadController pulse;
	ThreadController move;
	ThreadController fade;
	Button b;
	boolean recieving;
	public Pair<Color, Color> cols;

	public SpecialView(Type type, boolean recieving) {
		super(ViewType.Special, new Rectangle(0, 0, 100, 100));

		Image i = null;
		String label = null;
		switch (type) {
		case PinaColada:
			label = "Playing Pina Coladas";
			if (recieving) label = "Present from the other Intercom";
			i = new Image(new Rectangle(20, 22.5, 55, 55), "drink.png");
			break;
		case Smoko:
			label = "Letting them know it's Smoko";
			if (recieving) label = "They're letting you know it's Smoko";
			i = new Image(new Rectangle(17.5, 25, 65, 50), "coffee.png");
			break;
		}
		
		cols = new Pair<>(new Color(250, 150, 0), new Color(200, 150, 0));
		b = new GradientButton(new Rectangle(43, 36, 14, 28), new Color(233, 144, 12), new Color(251, 200, 8));
		b.setClickAction(() -> Intercom.getInstance().endSpecial(true));
		b.addComponent(i);
		b.pauseHover();
		addComponent(b);
		
		Button close = new GradientButton(new Rectangle(53, 41, 7, 14), new Color(126, 0, 14), new Color(191, 0, 9));
		close.setClickAction(() -> Intercom.getInstance().endSpecial(true));
		close.addComponent(new Image(new Rectangle(17.5, 17.5, 65, 65), "exit.png"));
		close.freezeShadow();
		addComponent(close);
		close.setOpacity(0);

		//Label and line
		Label l = new Label(new Point(50, 72), label, new Font("Geneva", Font.ROMAN_BASELINE, 25), new Color(200, 200, 200));
		l.setOpacity(0);
		l.setCentered(true);
		addComponent(l);
		SimpleBox sB = new SimpleBox(new Rectangle(35, 60, 30, 0.4), new Color(200, 200, 200));
		sB.setOpacity(0);
		addComponent(sB);

		pulse = AnimationFactory.getAnimation(b, Animations.PulseRings, cols);
		move = AnimationFactory.getAnimation(b, Animations.MoveTo, new Point(b.getX(), b.getY()-12));
		move.setInitialDelay(1000);
		fade = AnimationFactory.getAnimation(new Component[] {l, sB, close}, Animations.Fade, 100);
		fade.setInitialDelay(1500);
	}

	@Override
	public void enter() {
		if (pulse!=null) pulse.start();
		if (move!=null) move.start();
		if (fade!=null) fade.start();
	}

	@Override
	public void destroy() {
		if (pulse!=null&&pulse.isRunning()) pulse.end();
		if (move!=null&&move.isRunning()) move.end();
		if (fade!=null&&fade.isRunning()) fade.end();
		super.destroy();
	}

	@Override
	public void draw(Graphics2D g) {
		drawComponentShadows(g);
		if (pulse!=null&&pulse.isRunning()) { //Draw pulse animation
			GUI.getInstance().getScreenUtils().drawPulse(g, pulse);
		}
		drawComponents(g);
	}

}
