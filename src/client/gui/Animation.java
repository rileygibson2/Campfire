package client.gui;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import client.Client;
import client.gui.components.Button;
import general.Point;
import general.Rectangle;

public class Animation {

	private Element target;
	private Thread thread;
	private Animations animation;
	private boolean stop;
	private boolean hasRun; //Stops same thread attempting to run twice and throwing an error
	
	private Set<Object> elements; //Generic list of elements an animation can use
	private int i;
	
	public static enum Animations {
		HoverButton,
		UnhoverButton,
		PulseButton
	}
	
	public Animation(Element target, Animations t) {
		this.target = target;
		this.animation = t;
		this.stop = false;
		this.hasRun = false;
		switch (t) {
		case HoverButton: this.thread = hoverButton; break;
		case UnhoverButton: this.thread = unhoverButton; break;
		case PulseButton: this.thread = pulseButton; break;
		}
	}
	
	public boolean isRunning() {return !this.stop;}
	
	public boolean hasRun() {return this.stop&&this.hasRun;}
	
	public boolean hasElements() {return elements!=null;}
	
	public Set<?> getElements() {return Collections.unmodifiableSet(elements);}
	
	public Element getTarget() {return this.target;}
	
	public void start() {
		if (!hasRun) {
			hasRun = true;
			i = 0;
			thread.start();
			System.out.println(animation+" started");
		}
	}
	
	public void stop() {this.stop = true;}
	
	private void iterate() {
		i++;
		Client.cGUI.repaint();
		try {Thread.sleep(20);}
		catch (InterruptedException e) {e.printStackTrace();}
	}
	
	private void finish() {
		System.out.println(animation+" finished");
	}
	
	final Thread unhoverButton = new Thread() {
		@Override
		public void run() {
			Button b = (Button) target;
			
			while (!stop) {
				if (b.r.width<=b.rO.width&&b.r.height<=b.rO.height) {
					stop = true;
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
	
	final Thread hoverButton = new Thread() {
		@Override
		public void run() {
			Button b = (Button) target;
			
			while (!stop) {
				if (b.r.width>=b.rO.width*1.2&&b.r.height>=b.rO.height*1.2) stop = true;
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
	
	final Thread pulseButton = new Thread() {
		@Override
		public void run() {
			elements = new HashSet<Object>(); //One point for each pulse, x is opacity, y is rad
			while (!stop) {
				if (i%200==0) elements.add(new Point(100, 1)); //Add new bubble
				
				Set<Object> toRemove = new HashSet<>();
				for (Object o : elements) {
					Point p = (Point) o;
					p.y++; //Expand bubble
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
