package client.gui.components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;

import general.Point;
import general.Rectangle;
import general.Utils;
import threads.AnimationFactory;
import threads.AnimationFactory.Animations;
import threads.ThreadController;

public class Button extends Component {

	private ThreadController hover;
	private ThreadController unHover;
	public SimpleBox mainBox;

	public Button(Rectangle r, Color col) {
		super(r);
		mainBox = new SimpleBox(new Rectangle(0, 0, 100, 100), col);
		mainBox.setRounded(true);
		addComponent(mainBox);
	}
	
	public void addButtonComponent(Component c) {
		mainBox.addComponent(c);
	}
	
	public Color getColor() {return mainBox.getColor();}
	
	public void setColor(Color col) {mainBox.setColor(col);}
	
	public void setOval(boolean o) {mainBox.setOval(o);}

	@Override
	public void doClick(Point p) {
		Utils.setCursor(Cursor.DEFAULT_CURSOR);
		super.doClick(p);
	};


	@Override
	public void doHover() {
		if (isHoverPaused()) return;
		if (!isHovered()) {
			if (unHover!=null) unHover.end();
			hover = AnimationFactory.getAnimation(this, Animations.Transform, new Rectangle(r.x-(r.width*0.1), r.y-(r.height*0.1), r.width*1.2, r.height*1.2), 15);
			hover.start();
			Utils.setCursor(Cursor.HAND_CURSOR);
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
			Utils.setCursor(Cursor.DEFAULT_CURSOR);
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
		super.draw(g);
	}
}
