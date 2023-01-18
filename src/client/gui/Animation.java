package client.gui;


import java.awt.Color;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import client.Client;
import client.gui.components.Button;
import client.gui.views.CallView;
import general.Pair;
import general.Point;
import general.Rectangle;

public class Animation {

	private Object target;
	private Thread thread;
	private Animations animation;
	private boolean stop;
	private boolean hasRun; //Stops same thread attempting to run twice and throwing an error
	private int wait = 20;
	
	private Set<Object> elements; //Generic list of elements an animation can use
	private int i;
	
	public static enum Animations {
		HoverButton,
		UnhoverButton,
		PulseButton,
		DataMove
	}
	
	public Animation(Object target, Animations t) {
		this.target = target;
		animation = t;
		stop = false;
		hasRun = false;
		thread = getThread(t);
	}
	
	public Animation(Animations t) {
		target = null;
		animation = t;
		stop = false;
		hasRun = false;
		thread = getThread(t);
	}
	
	private Thread getThread(Animations t) {
		switch (t) {
		case HoverButton: return hoverButton;
		case UnhoverButton: return unhoverButton;
		case PulseButton: return pulseButton;
		case DataMove: return dataMove;
		}
		return null;
	}
	
	public boolean isRunning() {return !this.stop;}
	
	public boolean hasRun() {return this.stop&&this.hasRun;}
	
	public boolean hasElements() {return elements!=null;}
	
	public Set<?> getElements() {return Collections.unmodifiableSet(elements);}
	
	public Object getTarget() {return this.target;}
	
	public Animations getType() {return this.animation;}
	
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
		try {Thread.sleep(wait);}
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
				//Add new bubble
				if (((i-62)/70d)%1==0) elements.add(new Pair<Point, Color>(new Point(50, 1), new Color(0, 220, 50)));
				if ((i/70d)%1==0) elements.add(new Pair<Point, Color>(new Point(50, 1), new Color(0, 200, 100)));
				
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
	
	final Thread dataMove = new Thread() {
		@Override
		public void run() {
			wait = 30;
			while (!stop) {
				Deque<Double> data = (Deque<Double>) target;
				data.pop();
				data.addLast(0d);
				
				iterate();
			}
			
			finish();
		}
	};
}
