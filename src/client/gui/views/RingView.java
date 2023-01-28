package client.gui.views;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import client.Intercom;
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

public class RingView extends View {

	ThreadController pulse;
	ThreadController move;
	ThreadController fade;
	Button b;
	boolean recieving;
	public Pair<Color, Color> cols;
	
	public RingView(boolean recieving) {
		super(ViewType.Call, new Rectangle(0, 0, 100, 100));
		
		//Xbox button
		if (recieving) {
			cols = new Pair<>(new Color(0, 220, 50), new Color(0, 200, 100));
			b = new GradientButton(new Rectangle(43, 36, 14, 28), new Color(89, 141, 19), new Color(112, 255, 12));
			b.setClickAction(() -> Intercom.getInstance().acceptRing());
			b.addComponent(new Image(new Rectangle(17.5, 25, 65, 50), "mic.png"));
			b.pauseHover();
		}
		else {
			cols = new Pair<>(new Color(220, 0, 50), new Color(200, 0, 100));
			b = new GradientButton(new Rectangle(43, 36, 14, 28), new Color(126, 0, 14), new Color(191, 0, 9));
			b.setClickAction(() -> Intercom.getInstance().cancelRing());
			b.addComponent(new Image(new Rectangle(17.5, 17.5, 65, 65), "exit.png"));
			b.freezeShadow();
		}
		addComponent(b);
		pulse = AnimationFactory.getAnimation(b, Animations.PulseRings, cols);
		
		if (!recieving) return;
		//Label and line
		Label l = new Label(new Point(34, 72), "Incoming Call", new Font("Geneva", Font.ROMAN_BASELINE, 25), new Color(200, 200, 200));
		l.setOpacity(0);
		addComponent(l);
		SimpleBox sB = new SimpleBox(new Rectangle(35, 60, 30, 0.4), new Color(200, 200, 200));
		sB.setOpacity(0);
		addComponent(sB);
		
		move = AnimationFactory.getAnimation(b, Animations.MoveTo, new Point(b.getRec().x, b.getRec().y-12));
		move.setInitialDelay(1000);
		move.setFinishAction(() -> {
			b.changeOriginalRec(b.getRec().clone());
			b.freezeShadow();
			b.unpauseHover();
		});
		fade = AnimationFactory.getAnimation(new Component[] {l, sB}, Animations.Fade, 100);
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
		if (pulse!=null&&!pulse.hasEnded()) pulse.end();
		if (move!=null&&!move.hasEnded()) move.end();
		if (fade!=null&&!fade.hasEnded()) fade.end();
		super.destroy();
	}

	@Override
	public void draw(Graphics2D g) {
		drawComponentShadows(g);
		if (pulse!=null&&!pulse.hasEnded()) { //Draw pulse animation
			GUI.getInstance().getScreenUtils().drawPulse(g, pulse);
		}
		drawComponents(g);
	}

}
