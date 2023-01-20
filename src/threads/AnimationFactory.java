package threads;


import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import client.gui.components.Button;
import client.gui.components.TextBox;
import general.Pair;
import general.Point;
import general.Rectangle;

public class AnimationFactory {

	public static enum Animations {
		HoverButton,
		UnhoverButton,
		PulseButton,
		CursorBlip,
		Paint
	}

	public static ThreadController getAnimation(Object target, Animations t) {
		ThreadController tC = getThread(t);
		tC.setTarget(target);
		return tC;
	}

	public static ThreadController getAnimation(Animations t) {
		return getThread(t);
	}

	private static ThreadController getThread(Animations t) {
		switch (t) {
		case HoverButton: return hoverButton();
		case UnhoverButton: return unhoverButton();
		case PulseButton: return pulseButton();
		case CursorBlip: return cursorBlip();
		case Paint: return paint();
		}
		return null;
	}

	final static ThreadController unhoverButton() {
		return new ThreadController() {
			@Override
			public void run() {
				Button b = (Button) getTarget();

				while (isRunning()) {
					if (b.r.width<=b.rO.width&&b.r.height<=b.rO.height) {
						end();
						b.r = new Rectangle(b.rO.x, b.rO.y, b.rO.width, b.rO.height);
					}
					else {
						b.r.width = b.r.width-1;
						b.r.height = b.r.height-1;
					}
					iterate();
				}

				b.hoverAni = null;
				finish();
			}
		};
	}

	final static ThreadController hoverButton() {
		return new ThreadController() {
			@Override
			public void run() {
				Button b = (Button) getTarget();

				while (isRunning()) {
					if (b.r.width>=b.rO.width*1.2&&b.r.height>=b.rO.height*1.2) end();
					else {
						b.r.width = b.r.width+1;
						b.r.height = b.r.height+1;
					}
					iterate();
				}

				b.hoverAni = null;
				finish();
			}
		};
	}

	final static ThreadController pulseButton() {
		return new ThreadController() {
			@Override
			public void run() {
				elements = new HashSet<Object>(); //One point for each pulse, x is opacity, y is rad
				while (isRunning()) {
					//Add new bubble
					if (((getIncrement()-62)/70d)%1==0) elements.add(new Pair<Point, Color>(new Point(50, 1), new Color(0, 220, 50)));
					if ((getIncrement()/70d)%1==0) elements.add(new Pair<Point, Color>(new Point(50, 1), new Color(0, 200, 100)));

					Set<Object> toRemove = new HashSet<>();
					for (Object o : elements) {
						Pair<Point, Color> pa = (Pair<Point, Color>) o;
						Point p = pa.a;
						p.y += 0.1; //Expand bubble
						if (p.x>0) p.x--; //Lower opacity
						else toRemove.add(o);
					}
					elements.removeAll(toRemove); //Remove invisible bubbles

					iterate();
				}

				finish();
			}
		};
	}

	final static ThreadController cursorBlip() {
		return new ThreadController() {
			@Override
			public void run() {
				TextBox t = (TextBox) getTarget();
				while (isRunning()) {
					if (t.cursor.isEmpty()) t.cursor = "_";
					else t.cursor = "";
					t.textLabel.text = t.text+t.cursor;

					iterate();
				}

				//Reset
				t.cursor = "";
				t.textLabel.text = t.text;
				finish();
			}
		};
	}

	final static ThreadController paint() {
		return new ThreadController() {
			@Override
			public void run() {
				while (isRunning()) iterate();
				finish();
			}
		};
	}
}
