package client.gui.components;

import java.awt.Color;
import java.awt.Font;

import client.gui.GUI;
import general.Point;
import general.Rectangle;

public class PopUp extends Component {

	private Runnable onClose;
	SimpleBox mainBox;
	Button close;
	Button accept;

	public PopUp(String label, Point p) {
		super(new Rectangle((100-p.x)/2, (100-p.y)/2, p.x, p.y));

		//Smother
		SimpleBox smother = new SimpleBox(new Rectangle(0, 0, 100, 100), new Color(0, 0, 0));
		smother.setOpacity(50);
		smother.setAbsolute(true);
		addComponent(smother);

		//Main box
		mainBox = new SimpleBox(new Rectangle(0, 0, 100, 100), GUI.fg);
		mainBox.setRounded(true);
		mainBox.increasePriority();
		addComponent(mainBox);

		//Top bar
		SimpleBox tB = new SimpleBox(new Rectangle(getX(), getY(), getWidth(), 12.5), GUI.focus);
		tB.setAbsolute(true);
		tB.setRounded(new int[]{1, 4});
		mainBox.addComponent(tB);

		//Top label
		Label l = new Label(new Point(50, 50), label, new Font(GUI.baseFont, Font.BOLD, 16), new Color(230, 230, 230));
		l.setCentered(true);
		tB.addComponent(l);

		//Exit button
		close = new Button(new Rectangle(getX()+getWidth()*0.70, getY()+getHeight()*0.75, 5, 10), GUI.focus);
		close.setAbsolute(true);
		close.setClickAction(() -> close(false));
		close.addComponent(new Image(new Rectangle(10, 10, 80, 80), "exit.png"));
		mainBox.addComponent(close);

		//Accept button
		accept = new Button(new Rectangle(getX()+getWidth()*0.85, getY()+getHeight()*0.75, 5, 10), new Color(100, 200, 100));
		accept.setAbsolute(true);
		accept.setClickAction(() -> close(true));
		accept.addComponent(new Image(new Rectangle(25, 25, 50, 50), "ok.png"));
		mainBox.addComponent(accept);
	}

	public void setCloseAction(Runnable r) {this.onClose = r;}
	
	public void setCloseButtonPos(double x, double y) {
		close.setX(x);
		close.setY(y);
		close.updateOriginalRec();
	}
	
	public void setAcceptButtonPos(double x, double y) {
		accept.setX(x);
		accept.setY(y);
		accept.updateOriginalRec();
	}

	//So components get added to the main box not to the popup which is essentially a wrapper
	public void addPopUpComponent(Component c) {
		mainBox.addComponent(c);
	}

	private void close(boolean cancelled) {
		if (onClose!=null) onClose.run();
		removeFromParent();
		destroy();
	}
}
