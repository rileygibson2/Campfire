package client.gui.views;

import java.awt.Graphics2D;

import general.Point;
import general.Rectangle;

public class CallView extends View {

	public CallView() {
		super(ViewType.Call, new Rectangle(0, 0, 100, 100));
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void draw(Graphics2D g) {
		drawComponents(g);
	}

}
