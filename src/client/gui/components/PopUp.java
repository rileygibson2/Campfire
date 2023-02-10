package client.gui.components;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import client.gui.GUI;
import general.Point;
import general.Rectangle;

public class PopUp extends Component {

	private Runnable onClose;
	SimpleBox mainBox;
	Button close;
	Button accept;
	Set<Component> addedComponents; //Components that are not part of the core popup
	List<SimpleBox> tabs; //Tabs added to this popup;
	
	public PopUp(String label, Point p) {
		super(new Rectangle((100-p.x)/2, (100-p.y)/2, p.x, p.y));
		addedComponents = new HashSet<Component>();
		tabs = new ArrayList<SimpleBox>();
		
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
	
	public void addTab(String name, Runnable clickAction) {
		//Tabs
		Font f = new Font(GUI.baseFont, Font.BOLD, 14);
		double w = GUI.getInstance().getScreenUtils().getStringWidthAsPerc(f, name)+10;
		double h = GUI.getInstance().getScreenUtils().getStringHeightAsPerc(f, name)+5;
		double x = 0;
		if (!tabs.isEmpty()) x = tabs.get(tabs.size()-1).getX()+tabs.get(tabs.size()-1).getWidth();
		
		SimpleBox tab = new SimpleBox(new Rectangle(x, 16-h, w, h), GUI.fg);
		tab.setClickAction(() -> {
			for (SimpleBox t : tabs) t.setColor(GUI.focus2);
			tab.setColor(GUI.fg);
			clickAction.run();
		});
		tab.setRounded(new int[] {1, 4});
		Label l = new Label(new Point(50, 50), name, f, new Color(220, 220, 220));
		l.setCentered(true);
		tab.addComponent(l);
		tab.increasePriority();
		mainBox.addComponent(tab);
		
		tabs.add(tab);
	}

	//So components get added to the main box not to the popup which is essentially a wrapper
	public void addPopUpComponent(Component c) {
		addedComponents.add(c);
		mainBox.addComponent(c);
	}
	
	public void cleanPopupComponents() {
		mainBox.removeComponents(addedComponents);
		addedComponents.clear();
	}

	private void close(boolean cancelled) {
		if (onClose!=null) onClose.run();
		removeFromParent();
		destroy();
	}
}
