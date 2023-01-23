package client.gui.components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;

import client.gui.GUI;
import client.gui.ScreenUtils;
import general.Point;
import general.Rectangle;
import general.Utils;
import threads.AnimationFactory;
import threads.AnimationFactory.Animations;
import threads.ThreadController;

public class Button extends Component {

	public Color col;
	public ThreadController hoverAni;
	public ThreadController unHoverAni;
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
			if (unHoverAni!=null) unHoverAni.end();
			hoverAni = AnimationFactory.getAnimation(this, Animations.Transform, new Rectangle(r.x-(r.width*0.1), r.y-(r.height*0.1), r.width*1.2, r.height*1.2), 15);
			hoverAni.start();
			Utils.setCursorDefault(Cursor.HAND_CURSOR);
		}
		super.doHover();
	}

	@Override
	public void doUnhover() {
		if (isHoverPaused()) return;
		if (isHovered()) {
			if (hoverAni!=null) hoverAni.end();
			unHoverAni = AnimationFactory.getAnimation(this, Animations.Transform, getOriginalRec().clone(), 15);
			unHoverAni.start();
			Utils.setCursorDefault(Cursor.DEFAULT_CURSOR);
		}
		super.doUnhover();
	}

	@Override
	public void destroy() {
		if (hoverAni!=null) hoverAni.end();
		if (unHoverAni!=null) unHoverAni.end();
		super.destroy();
	}

	@Override
	public void draw(Graphics2D g) {
		if (drawBox) GUI.getInstance().getScreenUtils().drawButton(g, this);
		super.draw(g);
	}
}
