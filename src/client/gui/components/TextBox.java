package client.gui.components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import client.gui.GUI;
import client.gui.IO;
import client.gui.ScreenUtils;
import general.Point;
import general.Rectangle;
import general.Utils;
import threads.AnimationFactory;
import threads.AnimationFactory.Animations;
import threads.ThreadController;

public class TextBox extends Component {

	private String text;
	public Label textLabel;
	String regex;

	//Cursor
	private ThreadController cursorAni;
	public String cursor;


	public TextBox(Rectangle rectangle, String initialText, String regex) {
		super(rectangle);
		this.regex = regex;
		text = initialText;
		if (text==null) text = "";
		textLabel = new Label(new Point(8, 55), text, new Font("Geneva", Font.ITALIC, 15), new Color(200, 200, 200)); 
		addComponent(textLabel);

		cursor = "";
	}
	
	public String getText() {return text;}
	
	public void setText(String t) {
		if (t==null) text = "";
		else text = t;
	}

	@Override
	public void doClick(Point p) {
		setSelected(true);
		IO.getInstance().registerKeyListener(this);
		
		cursorAni = AnimationFactory.getAnimation(this, Animations.CursorBlip);
		cursorAni.start();
		
		super.doClick(p);
	}

	@Override
	public void doDeselect() {
		IO.getInstance().deregisterKeyListener(textLabel);;
		setSelected(false);
		if (cursorAni!=null && cursorAni.isRunning()) cursorAni.end();
		if (regex!=null&&!text.matches(regex)) text = "";
		super.doDeselect();
	}
	
	@Override
	public void destroy() {
		if (cursorAni!=null) cursorAni.end();
		super.destroy();
	}
	
	@Override
	public void doHover() {
		Utils.setCursorDefault(Cursor.HAND_CURSOR);
	}

	@Override
	public void doUnhover() {
		Utils.setCursorDefault(Cursor.DEFAULT_CURSOR);
	}

	@Override
	public void doKeyPress(KeyEvent e) {
		if (e.getExtendedKeyCode()==8&&!text.isEmpty()) text = text.substring(0, text.length()-1);
		else text += e.getKeyChar();
		textLabel.text = text+cursor;
	}

	@Override
	public void draw(Graphics2D g) {
		GUI.getInstance().getScreenUtils().drawTextBox(g, this);
		super.draw(g);
	}
}
