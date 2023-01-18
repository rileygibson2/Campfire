package client.gui.components;

import java.awt.event.KeyEvent;

import client.gui.Element;
import general.Point;
import general.Rectangle;

public class SimpleBox extends Component {

	public SimpleBox(Rectangle r, Element parent) {
		super(r, parent);
	}

	public void doClick(Point p) {
		System.out.println("SimpleBox clicked");
	};


	@Override
	public void doHover() {}

	@Override
	public void doUnhover() {}
	
	@Override
	public void doKeyPress(KeyEvent e) {}
}
