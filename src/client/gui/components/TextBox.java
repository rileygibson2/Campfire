package client.gui.components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import client.gui.GUI;
import client.gui.IO;
import general.Getter;
import general.GetterSubmitter;
import general.Point;
import general.Rectangle;
import general.Utils;
import threads.AnimationFactory;
import threads.AnimationFactory.Animations;
import threads.ThreadController;

public class TextBox extends Component {

	private String text;
	public Label textLabel;
	public Label descriptionLabel;
	private GetterSubmitter<String, String> actions;
	private Getter<String> description;

	//Cursor
	private ThreadController cursorAni;
	public String cursor;


	public TextBox(Rectangle rectangle, String initialText) {
		super(rectangle);
		text = initialText;
		if (text==null) text = "";
		cursor = "";
		
		textLabel = new Label(new Point(8, 55), text, new Font(GUI.baseFont, Font.ITALIC, 15), new Color(200, 200, 200));
		addComponent(textLabel);
		descriptionLabel = new Label(new Point(8, 55), text, new Font(GUI.baseFont, Font.ITALIC, 15), new Color(140, 140, 140));
		descriptionLabel.setVisible(false);
		addComponent(descriptionLabel);
	}
	
	public void setActions(GetterSubmitter<String, String> actions) {this.actions = actions;}
	
	public void setDescriptionAction(Getter<String> d) {
		description = d;
		if (text.isEmpty()) {
			descriptionLabel.text = description.get();
			descriptionLabel.setVisible(true);
		}
		else descriptionLabel.setVisible(false);
	}
	
	public String getText() {return text;}

	@Override
	public void doClick(Point p) {
		setSelected(true);
		IO.getInstance().registerKeyListener(this);
		
		cursorAni = AnimationFactory.getAnimation(this, Animations.CursorBlip);
		cursorAni.start();
		descriptionLabel.setVisible(false);
		super.doClick(p);
	}

	@Override
	public void doDeselect() {
		IO.getInstance().deregisterKeyListener(textLabel);;
		setSelected(false);
		if (cursorAni!=null) cursorAni.end();
		
		//Submit input
		if (actions!=null) {
			actions.submit(text); //Submit input
			text = actions.get(); //Update text
			textLabel.text = text;
		}
		if (text.isEmpty()&&description!=null) {
			descriptionLabel.text = description.get();
			descriptionLabel.setVisible(true);
		}
		else descriptionLabel.setVisible(false);
		super.doDeselect();
	}
	
	@Override
	public void destroy() {
		if (cursorAni!=null) cursorAni.end();
		super.destroy();
	}
	
	@Override
	public void doHover() {
		Utils.setCursor(Cursor.HAND_CURSOR);
	}

	@Override
	public void doUnhover() {
		Utils.setCursor(Cursor.DEFAULT_CURSOR);
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
