package client.gui.views;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Mixer.Info;

import cli.CLI;
import client.AudioManager;
import client.Campfire;
import client.Special.Type;
import client.gui.GUI;
import client.gui.components.Button;
import client.gui.components.CheckBox;
import client.gui.components.Component;
import client.gui.components.DropDown;
import client.gui.components.GradientButton;
import client.gui.components.Image;
import client.gui.components.Label;
import client.gui.components.MessageBox;
import client.gui.components.PopUp;
import client.gui.components.SimpleBox;
import client.gui.components.Slider;
import client.gui.components.TextBox;
import general.Getter;
import general.GetterSubmitter;
import general.Point;
import general.Rectangle;
import network.Client;
import network.managers.BroadcastManager;
import network.managers.NetworkManager;
import threads.AnimationFactory;
import threads.AnimationFactory.Animations;
import threads.ThreadController;

public class HomeView extends View {

	protected static HomeView singleton;

	SimpleBox extrasPopup;
	Slider volumeSlider;
	public Image linkImage;

	DropDown<Mixer.Info> audioIn;
	DropDown<Mixer.Info> audioOut;
	DropDown<Line.Info> remote;

	Button clients; //Client select button
	ThreadController clientsFade;

	private HomeView() {
		super(ViewType.Home, new Rectangle(0, 0, 100, 100));

		//Main label
		addComponent(new Label(new Point(86, 93), "Campfire", new Font(GUI.logoFont, Font.BOLD, 11), new Color(150, 150, 150)));
		addComponent(new Image(new Rectangle(89, 77, 5, 12), "logo.png").setOpacity(60));

		//Main button
		Button b = new GradientButton(new Rectangle(43, 36, 14, 28), new Color(89, 141, 19), new Color(112, 255, 12));
		//Button b = new GradientButton(new Rectangle(43, 36, 14, 28), new Color(250, 128, 0), new Color(240, 187, 0));
		b.addButtonComponent(new Image(new Rectangle(17.5, 25, 65, 50), "mic.png"));
		b.setClickAction(() -> Campfire.getInstance().startInitiatingRing());
		b.freezeShadow();
		addComponent(b);

		//Multiple users icon
		clients = new Button(new Rectangle(86, 7, 9, 18), MessageBox.error);
		clients.setOval(true);
		clients.setClickAction(() -> {
			PopUp p = new PopUp("Pick Your Intercom", new Point(50, 50));

			DropDown<Client> d = new DropDown<Client>(new Rectangle(15, 38, 70, 25));
			d.addComponent(new Image(new Rectangle(85, 25, 8, 50), "closedselector.png"));
			p.addPopUpComponent(d);

			//Actions
			d.setSelected(new AbstractMap.SimpleEntry<String, Client>(Campfire.getClient().getTitle(), Campfire.getClient()));
			d.setActions(new GetterSubmitter<LinkedHashMap<String, Client>, Client>() {
				public void submit(Client c) {
					Campfire.setClient(c);
				}

				public LinkedHashMap<String, Client> get() {
					LinkedHashMap<String, Client> result = new LinkedHashMap<>();
					for (Client c : BroadcastManager.getPotentialClients()) {
						result.put(c.getTitle(), c);
					}
					return result;
				}
			});

			p.increasePriority();
			addComponent(p);
		});
		clients.addComponent(new Image(new Rectangle(11, 10, 80, 80), "telephone.png"));
		addComponent(clients);
		clients.setVisible(false);

		/*b = new XboxButton(new Rectangle(60, 40, 7, 14), "X", new Color(12, 45, 241), new Color(51, 100, 253), this);
		b.freezeShadow();
		addComponent(b);*/

		//Sidebar
		int sbImgOp = 70;
		SimpleBox sB = new SimpleBox(new Rectangle(0, 0, 11, 100), GUI.fg);
		sB.setRounded(new int[] {3, 4});
		addComponent(sB);
		int y = 80;

		//Link
		final Button link = new Button(new Rectangle(0, y, 100, 20), GUI.fg);
		sB.addComponent(link);
		link.setClickAction(() -> {
			PopUp p = new PopUp("Intercom Status", new Point(50, 50));
			Label l = new Label(new Point(50, 50), "", new Font(GUI.baseFont, Font.BOLD, 16), new Color(230, 230, 230)) {
				@Override
				public void draw(Graphics2D g) { //Overriden to catch link status before being drawn
					if (NetworkManager.getLinkManager()!=null&&NetworkManager.getLinkManager().isProbablyLinked()) text = "Currently connected";
					else text = "Currently disconnected";
					super.draw(g);
				}
			};
			l.setCentered(true);
			p.addPopUpComponent(l);

			l = new Label(new Point(15, 85), "Auto Detect", new Font(GUI.baseFont, Font.BOLD, 13), new Color(180, 180, 180));
			p.addPopUpComponent(l);
			CheckBox cB = new CheckBox(new Rectangle(5, 78, 7, 14));
			cB.setActions(new GetterSubmitter<Boolean, Boolean>() {
				public void submit(Boolean b) {Campfire.setAutoDetect(b);}
				public Boolean get() {return Campfire.isAutoDetectEnabled();}
			});
			p.addPopUpComponent(cB);

			p.increasePriority();
			addComponent(p);
		});
		link.setHoverAction(() -> buttonHoverAction(link, true));
		link.setUnHoverAction(() -> buttonHoverAction(link, false));

		Image linkImage = new Image(new Rectangle(23, 20, 50, 55), "") {
			@Override
			public void draw(Graphics2D g) { //Overriden to catch link status before being drawn
				if (NetworkManager.getLinkManager()!=null&&NetworkManager.getLinkManager().isProbablyLinked()) src = "connected.png";
				else src = "disconnected.png";
				super.draw(g);
			}
		};
		//linkImage.setOpacity(sbImgOp);
		link.addButtonComponent(linkImage);

		//Mute
		y -= 20;
		final SimpleBox mute = new SimpleBox(new Rectangle(0, y, 100, 20), GUI.fg);
		sB.addComponent(mute);
		mute.setHoverAction(() -> {
			volumeSlider = new Slider(new Rectangle(100, 25, 200, 50));
			volumeSlider.setValue(AudioManager.getInstance().getVolume());
			volumeSlider.decreasePriority();
			volumeSlider.setUpdateAction(() -> {
				AudioManager aM = AudioManager.getInstance();
				aM.setVolume(volumeSlider.getValue());
				Image i = (Image) mute.getComponents(Image.class).get(0);

				if (aM.getVolume()<5) {
					aM.setVolume(0);
					mute.setColor(new Color(200, 100, 100));
					i.src = "muted.png";
					aM.mute();
				}
				else {
					mute.setColor(GUI.fg);
					i.src = "unmuted.png";
					aM.unmute();
				}
			});
			mute.addComponent(volumeSlider);

			//Image color
			List<Component> comps = mute.getComponents(Image.class);
			if (comps!=null&&!comps.isEmpty()) comps.get(0).setOpacity(100);
		});
		mute.setUnHoverAction(() -> {
			mute.removeComponent(volumeSlider);

			//Image color
			List<Component> comps = mute.getComponents(Image.class);
			if (comps!=null&&!comps.isEmpty()) comps.get(0).setOpacity(sbImgOp);
		});
		mute.addComponent(new Image(new Rectangle(27.5, 25.5, 45, 50), "unmuted.png").setOpacity(sbImgOp));

		//Extras
		y -= 20;
		final Button extras = new Button(new Rectangle(0, y, 100, 20), GUI.fg);
		sB.addComponent(extras);
		extras.setHoverAction(() -> {
			buttonHoverAction(extras, true);
			extrasPopup = new SimpleBox(new Rectangle(100, 10, 200, 80), new Color(70, 70, 70));
			extrasPopup.setRounded(new int[] {3, 4});
			extras.addComponent(extrasPopup);

			Button b1 = new Button(new Rectangle(10, 17.5, 25, 65), new Color(250, 180, 50));
			b1.setClickAction(() -> Campfire.getInstance().startInitiatingSpecial(Type.PinaColada));
			b1.addButtonComponent(new Image(new Rectangle(12, 12, 70, 70), "drink.png"));
			extrasPopup.addComponent(b1);

			b1 = new Button(new Rectangle(45, 17.5, 25, 65), new Color(50, 220, 50));
			b1.setClickAction(() -> Campfire.getInstance().startInitiatingSpecial(Type.Smoko));
			b1.addButtonComponent(new Image(new Rectangle(15, 8, 70, 80), "coffee.png"));
			extrasPopup.addComponent(b1);
		});
		extras.setUnHoverAction(() -> {
			buttonHoverAction(extras, false);
			extras.removeComponent(extrasPopup);
		});
		extras.addButtonComponent(new Image(new Rectangle(31.5, 30, 37, 40), "plus.png").setOpacity(sbImgOp));

		//Client
		y -= 20;
		final Button client = new Button(new Rectangle(0, y, 100, 20), GUI.fg);
		sB.addComponent(client);
		client.setClickAction(() -> {
			PopUp p = new PopUp("Set Client IP", new Point(50, 50));

			TextBox t = new TextBox(new Rectangle(15, 37, 70, 25), Campfire.getClient().getIP());
			t.setActions(new GetterSubmitter<String, String>() {
				public void submit(String s) {Campfire.setIP(s);}
				public String get() {return Campfire.getClient().getIP();}
			});
			t.setDescriptionAction(new Getter<String>() {
				public String get() {return "Enter an IP";}
			});
			p.addPopUpComponent(t);

			CheckBox cB = new CheckBox(new Rectangle(5, 78, 7, 14));
			cB.setActions(new GetterSubmitter<Boolean, Boolean>() {
				public void submit(Boolean b) {Campfire.setAutoDetect(b);}
				public Boolean get() {return Campfire.isAutoDetectEnabled();}
			});
			p.addPopUpComponent(cB);

			Label l = new Label(new Point(15, 85), "Auto Detect", new Font(GUI.baseFont, Font.BOLD, 13), new Color(180, 180, 180));
			p.addPopUpComponent(l);

			p.increasePriority();
			p.setCloseAction(() -> Campfire.setIP(t.getText()));
			addComponent(p);
		});
		client.setHoverAction(() -> buttonHoverAction(client, true));
		client.setUnHoverAction(() -> buttonHoverAction(client, false));
		client.addButtonComponent(new Image(new Rectangle(27.5, 25.5, 45, 50), "connection.png").setOpacity(sbImgOp));

		//Settings
		y -= 20;
		final Button settings = new Button(new Rectangle(0, y, 100, 20), GUI.fg);
		sB.addComponent(settings);
		createSettings(settings);
		//settings.setClickAction(() -> Intercom.cGUI.changeView(new SettingsView()));
		settings.setHoverAction(() -> buttonHoverAction(settings, true));
		settings.setUnHoverAction(() -> buttonHoverAction(settings, false));
		settings.addButtonComponent(new Image(new Rectangle(27.5, 25.5, 45, 50), "settings.png").setOpacity(sbImgOp));
	}

	public static HomeView getInstance() {
		if (singleton==null) singleton = new HomeView();
		return singleton;
	};

	public void showUsersButton() {
		if (!clients.isVisible()) {
			clients.setVisible(true);
			clients.setOpacity(0);
			if (clientsFade!=null) clientsFade.end();
			clientsFade = AnimationFactory.getAnimation(clients, Animations.Fade, 100);
			clientsFade.start();
		}
	}

	public void hideUsersButton() {
		if (clients.isVisible()) {
			if (clientsFade!=null) clientsFade.end();
			clientsFade = AnimationFactory.getAnimation(clients, Animations.Fade, 0);
			clientsFade.setFinishAction(() -> clients.setVisible(false));
			clientsFade.start();
		}
	}

	public void createSettings(Button settings) {
		settings.setClickAction(() -> {
			PopUp p = new PopUp("Settings", new Point(80, 80));
			p.addTab("Intercom", () -> openIntercomTab(p));
			p.addTab("Network", () -> openNetworkTab(p));

			openIntercomTab(p);
		});
	}

	public void openNetworkTab(PopUp p) {
		p.cleanPopupComponents();
		double x = 6;
		double y = 25;

		//Connect port textbox
		p.addPopUpComponent(new Label(new Point(x, y), "Connect Port", new Font(GUI.baseFont, Font.BOLD, 14), new Color(220, 220, 220)));
		TextBox t = new TextBox(new Rectangle(x, y+6, 40, 15), ""+Campfire.getConnectPort());
		t.setActions(new GetterSubmitter<String, String>() {
			public void submit(String s) {Campfire.setConnectPort(s);}
			public String get() {return ""+Campfire.getConnectPort();}
		});
		p.addPopUpComponent(t);

		//Listen port textbox
		y += 30;
		p.addPopUpComponent(new Label(new Point(x, y), "Listen Port", new Font(GUI.baseFont, Font.BOLD, 14), new Color(220, 220, 220)));
		t = new TextBox(new Rectangle(x, y+6, 40, 15), ""+Campfire.getListenPort());
		t.setActions(new GetterSubmitter<String, String>() {
			public void submit(String s) {Campfire.setListenPort(s, true);}
			public String get() {return ""+Campfire.getListenPort();}
		});
		p.addPopUpComponent(t);

		//Auto detect checkbox
		x += 2;
		y += 28;
		p.addPopUpComponent(new Label(new Point(x+5.5, y+4.5), "Auto Detect", new Font(GUI.baseFont, Font.BOLD, 13), new Color(180, 180, 180)));
		CheckBox cB = new CheckBox(new Rectangle(x, y, 4, 8));
		cB.setActions(new GetterSubmitter<Boolean, Boolean>() {
			public void submit(Boolean b) {Campfire.setAutoDetect(b);}
			public Boolean get() {return Campfire.isAutoDetectEnabled();}
		});
		p.addPopUpComponent(cB);

		//Name textbox
		x = 50;
		y = 25;
		p.addPopUpComponent(new Label(new Point(x, y), "Station Name", new Font(GUI.baseFont, Font.BOLD, 14), new Color(220, 220, 220)));
		t = new TextBox(new Rectangle(x, y+6, 40, 15), ""+Campfire.getIntercomName());
		t.setActions(new GetterSubmitter<String, String>() {
			public void submit(String s) {Campfire.setIntercomName(s);}
			public String get() {return ""+Campfire.getIntercomName();}
		});
		p.addPopUpComponent(t);
	}

	public void openIntercomTab(PopUp p) {
		p.cleanPopupComponents();
		double x = 6;
		double y = 25;

		//Audio input dropdown
		p.addPopUpComponent(new Label(new Point(x, y), "Microphone", new Font(GUI.baseFont, Font.BOLD, 14), new Color(220, 220, 220)));
		audioIn = new DropDown<>(new Rectangle(x, y+6, 40, 15));
		audioIn.addComponent(new Image(new Rectangle(85, 25, 8, 50), "closedselector.png"));
		p.addPopUpComponent(audioIn);

		//Actions
		audioIn.setSelected(AudioManager.getInstance().getDefaultMic());
		audioIn.setActions(new GetterSubmitter<LinkedHashMap<String, Mixer.Info>, Mixer.Info>() {
			public void submit(Mixer.Info s) {
				AudioManager.getInstance().setMicLineInfo(s);
			}

			public LinkedHashMap<String, Info> get() {
				return AudioManager.getInstance().listMicrophones();
			}
		});

		//Audio output dropdown
		y += 30;
		p.addPopUpComponent(new Label(new Point(x, y), "Speaker", new Font(GUI.baseFont, Font.BOLD, 14), new Color(220, 220, 220)));
		audioOut = new DropDown<Mixer.Info>(new Rectangle(x, y+6, 40, 15));
		audioOut.addComponent(new Image(new Rectangle(85, 25, 8, 50), "closedselector.png"));
		p.addPopUpComponent(audioOut);

		//Actions
		audioOut.setSelected(AudioManager.getInstance().getDefaultSpeaker());
		audioOut.setActions(new GetterSubmitter<LinkedHashMap<String, Mixer.Info>, Mixer.Info>() {
			public void submit(Mixer.Info s) {
				AudioManager.getInstance().setSpeakerLineInfo(s);
			}

			public LinkedHashMap<String, Info> get() {
				return AudioManager.getInstance().listSpeakers();
			}
		});

		//Anti aliasing checkbox
		x = 55;
		y = 30;
		p.addPopUpComponent(new Label(new Point(x+5.5, y+4.5), "Anti Aliasing", new Font(GUI.baseFont, Font.BOLD, 13), new Color(180, 180, 180)));
		CheckBox cB = new CheckBox(new Rectangle(x, y, 4, 8));
		cB.setActions(new GetterSubmitter<Boolean, Boolean>() {
			public void submit(Boolean b) {GUI.getInstance().setAntiAliasing(b);}
			public Boolean get() {return GUI.getInstance().getAntiAliasing();}
		});
		p.addPopUpComponent(cB);

		//Finish up
		p.setCloseButtonPos(p.getX()+p.getWidth()*0.81, p.getY()+p.getHeight()*0.83);
		p.setAcceptButtonPos(p.getX()+p.getWidth()*0.90, p.getY()+p.getHeight()*0.83);
		p.increasePriority();
		addComponent(p);
	}

	public void buttonHoverAction(Button b, boolean hoverOn) {
		Color c = b.getColor();
		if (hoverOn) {
			b.setColor(new Color(c.getRed()+20, c.getGreen()+20, c.getBlue()+20));
			b.increasePriority();
			List<Component> comps = b.mainBox.getComponents(Image.class);
			if (comps!=null&&!comps.isEmpty()) comps.get(0).setOpacity(100);
		}
		else {
			b.setColor(new Color(c.getRed()-20, c.getGreen()-20, c.getBlue()-20));
			b.decreasePriority();
			List<Component> comps = b.mainBox.getComponents(Image.class);
			if (comps!=null&&!comps.isEmpty()) comps.get(0).setOpacity(70);
		}
	}

	@Override
	public void enter() {}
}
