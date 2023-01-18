package client.gui.views;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.LinkedHashMap;
import java.util.concurrent.Callable;

import javax.sound.sampled.Line;

import client.Client;
import client.gui.ClientGUI;
import client.gui.ScreenUtils;
import client.gui.components.Button;
import client.gui.components.DropDown;
import client.gui.components.Image;
import client.gui.components.Label;
import client.gui.components.TextBox;
import general.Point;
import general.Rectangle;

public class SettingsView extends View {

	ClientGUI c;
	DropDown<Line.Info> audioIn;
	DropDown<Line.Info> audioOut;
	DropDown<Line.Info> remote;
	Color bg = new Color(50, 50, 50);
	
	public SettingsView(ClientGUI c) {
		super(ViewType.Settings, new Rectangle(10, 10, 80, 80));

		//Selectors
		int x = 6;		
		components.add(new Label(new Point(x, 15), "Microphone", new Font("Geneva", Font.BOLD, 18), new Color(200, 200, 200), this));
		DropDown<Line.Info> s = new DropDown<>(Client.aM.getDefaultMic(), new Rectangle(x, 18, 40, 15), this);
		s.components.add(new Image(new Rectangle(85, 25, 8, 50), "closedselector.png", s));
		s.updateOptions = Client.getExecutor().submit(new Callable<LinkedHashMap<String, Line.Info>>() {
	        public LinkedHashMap<String, Line.Info> call() throws Exception {
	            return Client.aM.listMicrophones();
	        }
	    });
		components.add(s);
		
		components.add(new Label(new Point(x, 45), "Speaker", new Font("Geneva", Font.BOLD, 18), new Color(200, 200, 200), this));
		s = new DropDown<Line.Info>(Client.aM.getDefaultSpeaker(), new Rectangle(x, 48, 40, 15), this);
		s.components.add(new Image(new Rectangle(85, 25, 8, 50), "closedselector.png", s));
		s.updateOptions = Client.getExecutor().submit(new Callable<LinkedHashMap<String, Line.Info>>() {
	        public LinkedHashMap<String, Line.Info> call() throws Exception {
	            return Client.aM.listSpeakers();
	        }
	    });
		components.add(s);
		
		components.add(new Label(new Point(x, 75), "Remote", new Font("Geneva", Font.BOLD, 18), new Color(200, 200, 200), this));
		s = new DropDown<>(null, new Rectangle(x, 78, 40, 15), this);
		s.components.add(new Image(new Rectangle(85, 25, 8, 50), "closedselector.png", s));
		components.add(s);
		
		//Server
		x = 55;
		components.add(new Label(new Point(x, 15), "Server", new Font("Geneva", Font.BOLD, 18), new Color(200, 200, 200), this));
		components.add(new TextBox(Client.ipRegex, new Rectangle(x, 18, 40, 15), this, c));
		
		//Exit button
		Button b = new Button(new Rectangle(92, 4, 6, 12), new Color(100, 100, 100), this);
		b.setClick(() -> Client.cGUI.changeView(ViewType.Home));
		b.components.add(new Image(new Rectangle(10, 10, 80, 80), "exit.png", b));
		components.add(b);
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
		ScreenUtils.fillRoundRect(g, bg, r);
		drawComponents(g);
	}
}
