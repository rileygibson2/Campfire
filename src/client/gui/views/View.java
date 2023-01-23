package client.gui.views;

import client.gui.Element;
import general.Rectangle;

public abstract class View extends Element {
	
	public enum ViewType {
		Home,
		Settings,
		Ring,
		Call,
		Special
	};
	private final ViewType viewType;

	protected View(ViewType v, Rectangle r) {
		super(r);
		this.viewType = v;
	}
	
	public abstract void enter();

	public ViewType getViewType() {return this.viewType;}
}
