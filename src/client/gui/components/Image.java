package client.gui.components;

import java.awt.Graphics2D;

import client.gui.GUI;
import client.gui.ScreenUtils;
import general.Rectangle;

public class Image extends Component {

	public String src;
	
	public Image(Rectangle r, String src) {
		super(r);
		this.src = src;
	}

	@Override
	public void draw(Graphics2D g) {
		super.draw(g);
		GUI.getInstance().getScreenUtils().drawImage(g, this);
	}

}
