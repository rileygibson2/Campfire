package client.gui.views;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.LinkedHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;

import client.AudioManager;
import client.Client;
import client.gui.ScreenUtils;
import client.gui.components.Button;
import client.gui.components.DropDown;
import client.gui.components.Image;
import client.gui.components.Label;
import client.gui.components.TextBox;
import general.Point;
import general.Rectangle;

public class SettingsView extends View {

	private static SettingsView singleton;

	DropDown<Mixer.Info> audioIn;
	DropDown<Mixer.Info> audioOut;
	DropDown<Line.Info> remote;
	Color bg = new Color(50, 50, 50);

	private SettingsView() {
		super(ViewType.Settings, new Rectangle(10, 10, 80, 80));
		int x = 6;	
		
		//Input dropdown
		components.add(new Label(new Point(x, 15), "Microphone", new Font("Geneva", Font.BOLD, 18), new Color(200, 200, 200), this));
		audioIn = new DropDown<>(new Rectangle(x, 18, 40, 15), this);
		audioIn.components.add(new Image(new Rectangle(85, 25, 8, 50), "closedselector.png", audioIn));
		components.add(audioIn);

		//Actions
		audioIn.setSelected(AudioManager.getInstance().getDefaultMic());
		audioIn.setSelectAction(() -> AudioManager.getInstance().setMicLineInfo(audioIn.getSelected()));
		Future<LinkedHashMap<String, Mixer.Info>> update = Client.getExecutor().submit(new Callable<LinkedHashMap<String, Mixer.Info>>() {
			public LinkedHashMap<String, Mixer.Info> call() throws Exception {
				return AudioManager.getInstance().listMicrophones();
			}
		});
		audioIn.setUpdateAction(update);

		//Output dropdown
		components.add(new Label(new Point(x, 45), "Speaker", new Font("Geneva", Font.BOLD, 18), new Color(200, 200, 200), this));
		audioOut = new DropDown<Mixer.Info>(new Rectangle(x, 48, 40, 15), this);
		audioOut.components.add(new Image(new Rectangle(85, 25, 8, 50), "closedselector.png", audioOut));
		components.add(audioOut);

		//Actions
		audioOut.setSelected(AudioManager.getInstance().getDefaultSpeaker());
		audioOut.setSelectAction(() -> AudioManager.getInstance().setSpeakerLineInfo(audioOut.getSelected()));
		update = Client.getExecutor().submit(new Callable<LinkedHashMap<String, Mixer.Info>>() {
			public LinkedHashMap<String, Mixer.Info> call() throws Exception {
				return AudioManager.getInstance().listSpeakers();
			}
		});
		audioOut.setUpdateAction(update);

		//Remote dropdown
		components.add(new Label(new Point(x, 75), "Remote", new Font("Geneva", Font.BOLD, 18), new Color(200, 200, 200), this));
		remote = new DropDown<>(new Rectangle(x, 78, 40, 15), this);
		remote.components.add(new Image(new Rectangle(85, 25, 8, 50), "closedselector.png", remote));
		components.add(remote);

		//Server textbox
		x = 55;
		components.add(new Label(new Point(x, 15), "Server", new Font("Geneva", Font.BOLD, 18), new Color(200, 200, 200), this));
		components.add(new TextBox(Client.ipRegex, new Rectangle(x, 18, 40, 15), this));

		//Exit button
		Button b = new Button(new Rectangle(92, 4, 6, 12), new Color(100, 100, 100), this);
		b.setClickAction(() -> Client.cGUI.changeView(HomeView.getInstance()));
		b.components.add(new Image(new Rectangle(10, 10, 80, 80), "exit.png", b));
		components.add(b);
	}

	public static SettingsView getInstance() {
		if (singleton==null) singleton = new SettingsView();
		return singleton;
	};

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
		super.draw(g);
	}
}
