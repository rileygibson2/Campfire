package client.gui.components;

import java.awt.Color;
import java.awt.Font;

import general.Point;
import general.Rectangle;

public class PopUp extends Component {

	private Runnable onClose;
	SimpleBox mainBox;

	public PopUp(String label, Point p) {
		super(new Rectangle((100-p.x)/2, (100-p.y)/2, p.x, p.y));

		//Smother
		SimpleBox smother = new SimpleBox(new Rectangle(0, 0, 100, 100), new Color(0, 0, 0));
		smother.setOpacity(50);
		smother.setAbsolute(true);
		addComponent(smother);

		//Main box
		mainBox = new SimpleBox(new Rectangle(0, 0, 100, 100), new Color(80, 80, 80));
		mainBox.setRounded(true);
		mainBox.increasePriority();
		addComponent(mainBox);

		//Top bar
		SimpleBox tB = new SimpleBox(new Rectangle(0, 0, 100, 25), new Color(100, 100, 100));
		tB.setRounded(new int[]{1, 4});
		mainBox.addComponent(tB);

		Label l = new Label(new Point(50, 50), label, new Font("Geneva", Font.BOLD, 16), new Color(230, 230, 230));
		l.setCentered(true);
		tB.addComponent(l);

		Button b = new Button(new Rectangle(70, 75, 10, 20), new Color(100, 100, 100));
		b.setClickAction(() -> close(false));
		b.addComponent(new Image(new Rectangle(10, 10, 80, 80), "exit.png"));
		mainBox.addComponent(b);

		b = new Button(new Rectangle(85, 75, 10, 20), new Color(100, 200, 100));
		b.setClickAction(() -> close(true));
		b.addComponent(new Image(new Rectangle(25, 25, 50, 50), "ok.png"));
		mainBox.addComponent(b);
	}

	public void setCloseAction(Runnable r) {this.onClose = r;}

	//So components get added to the main box not to the popup which is essentially a wrapper
	public void addPopUpComponent(Component c) {
		mainBox.addComponent(c);
	}

	private void close(boolean cancelled) {
		if (onClose!=null) onClose.run();
		getParent().removeComponent(this);
		destroy();
	}
}
