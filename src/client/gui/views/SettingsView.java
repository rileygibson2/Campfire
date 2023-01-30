package client.gui.views;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Mixer.Info;

import client.AudioManager;
import client.Intercom;
import client.gui.GUI;
import client.gui.components.Button;
import client.gui.components.DropDown;
import client.gui.components.Image;
import client.gui.components.Label;
import client.gui.components.TextBox;
import general.GetterSubmitter;
import general.Point;
import general.Rectangle;

public class SettingsView extends View {

	DropDown<Mixer.Info> audioIn;
	DropDown<Mixer.Info> audioOut;
	DropDown<Line.Info> remote;
	Color bg = new Color(50, 50, 50);

	public SettingsView() {
		super(ViewType.Settings, new Rectangle(10, 10, 80, 80));
		
	}

	@Override
	public void enter() {}

	@Override
	public void draw(Graphics2D g) {
		GUI.getInstance().getScreenUtils().fillRoundRect(g, bg, getRec());
		super.draw(g);
	}
}
