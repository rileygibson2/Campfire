package client.gui.components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;

import client.gui.GUI;
import general.Point;
import general.Rectangle;
import general.Utils;
import threads.AnimationFactory;
import threads.AnimationFactory.Animations;
import threads.ThreadController;

public class Button extends Component {

	public Color col;
	public ThreadController hover;
	public ThreadController unHover;
	public boolean drawBox;

	public Button(Rectangle r, Color col) {
		super(r);
		this.col = col;
		drawBox = true;
	}

	@Override
	public void doClick(Point p) {
		Utils.setCursorDefault(Cursor.DEFAULT_CURSOR);
		super.doClick(p);
	};


	@Override
	public void doHover() {
		if (isHoverPaused()) return;
		if (!isHovered()) {
			if (unHover!=null) unHover.end();
			hover = AnimationFactory.getAnimation(this, Animations.Transform, new Rectangle(r.x-(r.width*0.1), r.y-(r.height*0.1), r.width*1.2, r.height*1.2), 15);
			hover.start();
			Utils.setCursorDefault(Cursor.HAND_CURSOR);
		}
		super.doHover();
	}

	@Override
	public void doUnhover() {
		if (isHoverPaused()) return;
		if (isHovered()) {
			if (hover!=null) hover.end();
			unHover = AnimationFactory.getAnimation(this, Animations.Transform, getOriginalRec().clone(), 15);
			unHover.start();
			Utils.setCursorDefault(Cursor.DEFAULT_CURSOR);
		}
		super.doUnhover();
	}

	@Override
	public void destroy() {
		if (hover!=null) hover.end();
		if (unHover!=null) unHover.end();
		super.destroy();
	}

	@Override
	public void draw(Graphics2D g) {
		if (drawBox) GUI.getInstance().getScreenUtils().drawButton(g, this);
		super.draw(g);
	}
}
