package client.gui.views;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import client.AudioManager;
import client.Intercom;
import client.Special.Type;
import client.gui.components.Button;
import client.gui.components.CheckBox;
import client.gui.components.GradientButton;
import client.gui.components.Image;
import client.gui.components.Label;
import client.gui.components.PopUp;
import client.gui.components.SimpleBox;
import client.gui.components.Slider;
import client.gui.components.TextBox;
import general.Getter;
import general.GetterSubmitter;
import general.Point;
import general.Rectangle;
import network.managers.NetworkManager;

public class HomeView extends View {

	protected static HomeView singleton;
	
	SimpleBox extrasPopup;
	Slider volumeSlider;
	public Image linkImage;

	private HomeView() {
		super(ViewType.Home, new Rectangle(0, 0, 100, 100));

		//Main label
		addComponent(new Label(new Point(79, 88.5), "RileyCom", new Font("Geneva", Font.ROMAN_BASELINE, 20), new Color(230, 230, 230)));
		addComponent(new Image(new Rectangle(69, 81, 10, 15), "icon.png"));

		//Xbox buttons
		Button b = new GradientButton(new Rectangle(43, 36, 14, 28), new Color(89, 141, 19), new Color(112, 255, 12));
		b.addComponent(new Image(new Rectangle(17.5, 25, 65, 50), "mic.png"));
		b.setClickAction(() -> Intercom.getInstance().startInitiatingRing());
		b.freezeShadow();
		addComponent(b);

		/*b = new XboxButton(new Rectangle(80, 40, 10, 20), "", new Color(126, 0, 14), new Color(191, 0, 9), this);
		b.addComponent(new Image(new Rectangle(15, 15, 70, 70), "endcall.png", b));
		b.freezeShadow();
		addComponent(b);*/

		/*b = new XboxButton(new Rectangle(60, 40, 7, 14), "X", new Color(12, 45, 241), new Color(51, 100, 253), this);
		b.freezeShadow();
		addComponent(b);

		b = new XboxButton(new Rectangle(70, 20, 7, 14), "Y", new Color(233, 144, 12), new Color(251, 200, 8), this);
		b.freezeShadow();
		addComponent(new XboxButton(new Rectangle(70, 20, 7, 14), "Y", new Color(233, 144, 12), new Color(251, 200, 8), this));
		 */

		//Sidebar
		SimpleBox sB = new SimpleBox(new Rectangle(0, 0, 11, 100), new Color(100, 100, 100));
		sB.setRounded(new int[] {3, 4});
		addComponent(sB);
		int y = 80;

		//Link
		final Button link = new Button(new Rectangle(0, y, 100, 20), new Color(100, 100, 100));
		sB.addComponent(link);
		link.setClickAction(() -> {
			PopUp p = new PopUp("Intercom Status", new Point(50, 50));
			Label l = new Label(new Point(50, 50), "", new Font("Geneva", Font.BOLD, 16), new Color(230, 230, 230)) {
				@Override
				public void draw(Graphics2D g) { //Overriden to catch link status before being drawn
					if (NetworkManager.getLinkManager().isProbablyLinked()) text = "Currently connected";
					else text = "Currently disconnected";
					super.draw(g);
				}
			};
			l.setCentered(true);
			p.addPopUpComponent(l);
			p.increasePriority();
			addComponent(p);
		});
		link.setHoverAction(() -> adjustColorHover(link, true));
		link.setUnHoverAction(() -> adjustColorHover(link, false));
		
		Image linkImage = new Image(new Rectangle(23, 20, 50, 55), "") {
			@Override
			public void draw(Graphics2D g) { //Overriden to catch link status before being drawn
				if (NetworkManager.getLinkManager().isProbablyLinked()) src = "connected.png";
				else src = "disconnected.png";
				super.draw(g);
			}
		};
		link.addComponent(linkImage);

		//Mute
		y -= 20;
		final SimpleBox mute = new SimpleBox(new Rectangle(0, y, 100, 20), new Color(100, 100, 100, 0));
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
					mute.col = new Color(200, 100, 100);
					i.src = "muted.png";
					aM.mute();
				}
				else {
					mute.col = new Color(100, 100, 100);
					i.src = "unmuted.png";
					aM.unmute();
				}
			});
			mute.addComponent(volumeSlider);
		});
		mute.setUnHoverAction(() -> {
			mute.removeComponent(volumeSlider);
		});
		mute.addComponent(new Image(new Rectangle(27.5, 25.5, 45, 50), "unmuted.png"));

		//Extras
		y -= 20;
		final Button extras = new Button(new Rectangle(0, y, 100, 20), new Color(100, 100, 100));
		sB.addComponent(extras);
		extras.setHoverAction(() -> {
			adjustColorHover(extras, true);
			extrasPopup = new SimpleBox(new Rectangle(100, 10, 200, 80), new Color(70, 70, 70));
			extrasPopup.setRounded(new int[] {3, 4});
			extras.addComponent(extrasPopup);
			
			Button b1 = new Button(new Rectangle(10, 17.5, 25, 65), new Color(250, 180, 50));
			b1.setClickAction(() -> Intercom.getInstance().startInitiatingSpecial(Type.PinaColada));
			b1.addComponent(new Image(new Rectangle(12, 12, 70, 70), "drink.png"));
			extrasPopup.addComponent(b1);
			
			b1 = new Button(new Rectangle(45, 17.5, 25, 65), new Color(50, 220, 50));
			b1.setClickAction(() -> Intercom.getInstance().startInitiatingSpecial(Type.Smoko));
			b1.addComponent(new Image(new Rectangle(15, 8, 70, 80), "coffee.png"));
			extrasPopup.addComponent(b1);
		});
		extras.setUnHoverAction(() -> {
			adjustColorHover(extras, false);
			extras.removeComponent(extrasPopup);
		});
		extras.addComponent(new Image(new Rectangle(31.5, 30, 37, 40), "plus.png"));

		//Client
		y -= 20;
		final Button client = new Button(new Rectangle(0, y, 100, 20), new Color(100, 100, 100));
		sB.addComponent(client);
		client.setClickAction(() -> {
			PopUp p = new PopUp("Set Client IP", new Point(50, 50));
			
			TextBox t = new TextBox(new Rectangle(15, 37, 70, 25), Intercom.getClient().getIP());
			t.setActions(new GetterSubmitter<String, String>() {
				public void submit(String s) {Intercom.setIP(s);}
				public String get() {return Intercom.getClient().getIP();}
			});
			t.setDescriptionAction(new Getter<String>() {
				public String get() {return "Enter an IP";}
			});
			p.addPopUpComponent(t);
			
			CheckBox cB = new CheckBox(new Rectangle(5, 78, 7, 14));
			cB.setActions(new GetterSubmitter<Boolean, Boolean>() {
				public void submit(Boolean b) {Intercom.setAutoDetect(b);}
				public Boolean get() {return Intercom.isAutoDetectEnabled();}
			});
			p.addPopUpComponent(cB);
			
			Label l = new Label(new Point(15, 85), "Auto Detect", new Font("Geneva", Font.BOLD, 13), new Color(180, 180, 180));
			p.addPopUpComponent(l);
			
			p.increasePriority();
			p.setCloseAction(() -> Intercom.setIP(t.getText()));
			addComponent(p);
		});
		client.setHoverAction(() -> adjustColorHover(client, true));
		client.setUnHoverAction(() -> adjustColorHover(client, false));
		client.addComponent(new Image(new Rectangle(27.5, 25.5, 45, 50), "connection.png"));

		//Settings
		y -= 20;
		final Button settings = new Button(new Rectangle(0, y, 100, 20), new Color(100, 100, 100));
		sB.addComponent(settings);
		settings.setClickAction(() -> Intercom.cGUI.changeView(new SettingsView()));
		settings.setHoverAction(() -> adjustColorHover(settings, true));
		settings.setUnHoverAction(() -> adjustColorHover(settings, false));
		settings.addComponent(new Image(new Rectangle(27.5, 25.5, 45, 50), "settings.png"));
	}

	public static HomeView getInstance() {
		if (singleton==null) singleton = new HomeView();
		return singleton;
	};

	public void adjustColorHover(Button b, boolean hoverOn) {
		Color c = b.col;
		if (hoverOn) {
			b.col = new Color(c.getRed()+20, c.getGreen()+20, c.getBlue()+20);
			b.increasePriority();
		}
		else {
			b.col = new Color(c.getRed()-20, c.getGreen()-20, c.getBlue()-20);
			b.decreasePriority();
		}
	}

	@Override
	public void enter() {}
}
