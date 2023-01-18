package client.gui.components;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import client.gui.ClientGUI;
import client.gui.Element;
import client.gui.ScreenUtils;
import general.Point;
import general.Rectangle;

public class TextBox extends Component {

	ClientGUI c;
	private String text;
	Label textLabel;
	String regex;

	//Cursor
	private String cursor;
	private boolean cursorRun;


	public TextBox(String regex, Rectangle rectangle, Element parent, ClientGUI c) {
		super(rectangle, parent);
		this.c = c;
		this.regex = regex;
		text = "";
		selected = false;
		textLabel = new Label(new Point(8, 65), text, new Font("Geneva", Font.ITALIC, 15), new Color(200, 200, 200), this); 
		components.add(textLabel);

		cursor = "";
		cursorRun = false;
	}

	@Override
	public void doClick(Point p) {
		System.out.println("TextBox clicked");
		selected = true;
		ClientGUI.io.registerKeyListener(this);
		getCursorThread().start();
	}

	@Override
	public void doDeselect() {
		ClientGUI.io.deregisterKeyListener(textLabel);;
		selected = false;
		cursorRun = false;
		if (regex!=null&&!text.matches(regex)) text = "";
		super.doDeselect();
	}

	public Thread getCursorThread() {
		return new Thread() {
			@Override
			public void run() {
				cursorRun = true;

				while (cursorRun) {
					if (cursor.isEmpty()) cursor = "_";
					else cursor = "";
					textLabel.text = text+cursor;

					c.repaint();
					try {Thread.sleep(500);}
					catch (InterruptedException e) {e.printStackTrace();}
				}

				//Reset
				cursor = "";
				textLabel.text = text;
				c.repaint();
			}
		};
	}

	@Override
	public void doKeyPress(KeyEvent e) {
		if (e.getExtendedKeyCode()==8&&!text.isEmpty()) text = text.substring(0, text.length()-1);
		else text += e.getKeyChar();
		textLabel.text = text+cursor;
	}

	@Override
	public void doHover() {
		// TODO Auto-generated method stub

	}

	@Override
	public void doUnhover() {
		// TODO Auto-generated method stub

	}

	@Override
	public void draw(Graphics2D g) {
		ScreenUtils.drawTextBox(g, this);
		drawComponents(g);
	}
}
