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
import threads.AnimationFactory;
import threads.AnimationFactory.Animations;
import threads.ThreadController;

public class TextBox extends Component {

	
	public String text;
	public Label textLabel;
	String regex;

	//Cursor
	private ThreadController cursorAni;
	public String cursor;


	public TextBox(String regex, Rectangle rectangle, Element parent) {
		super(rectangle, parent);
		this.regex = regex;
		text = "";
		selected = false;
		textLabel = new Label(new Point(8, 65), text, new Font("Geneva", Font.ITALIC, 15), new Color(200, 200, 200), this); 
		components.add(textLabel);

		cursor = "";
	}

	@Override
	public void doClick(Point p) {
		System.out.println("TextBox clicked");
		selected = true;
		ClientGUI.io.registerKeyListener(this);
		
		cursorAni = AnimationFactory.getAnimation(this, Animations.CursorBlip);
		cursorAni.start();
	}

	@Override
	public void doDeselect() {
		ClientGUI.io.deregisterKeyListener(textLabel);;
		selected = false;
		if (cursorAni!=null && cursorAni.isRunning()) cursorAni.end();
		if (regex!=null&&!text.matches(regex)) text = "";
		super.doDeselect();
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
		super.draw(g);
	}
}
