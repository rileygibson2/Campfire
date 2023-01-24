package client.gui.views;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayDeque;
import java.util.Deque;

import client.Client;
import client.gui.GUI;
import client.gui.components.GradientButton;
import client.gui.components.Image;
import client.gui.components.SimpleBox;
import general.Point;
import general.Rectangle;
import threads.AnimationFactory;
import threads.AnimationFactory.Animations;
import threads.ThreadController;

public class CallView extends View {

	private Deque<Double> data; //Voice data
	public int dataLen; //Length of data structure (actual deque may not always be that length)
	public Point dataBounds; //Upper and lower bound of data
	
	SimpleBox dataBox; //Bounding component when drawing data
	ThreadController paint; //Shifts data along as time goes on

	public CallView() {
		super(ViewType.Call, new Rectangle(0, 0, 100, 100));
		initialiseData();
		
		GradientButton b = new GradientButton(new Rectangle(75, 38, 12, 24), new Color(126, 0, 14), new Color(191, 0, 9));
		b.setClickAction(() -> Client.getInstance().endCall(true));
		b.addComponent(new Image(new Rectangle(17.5, 17.5, 65, 65), "exit.png"));
		
		b.freezeShadow();
		addComponent(b);
		
		dataBox = new SimpleBox(new Rectangle(0, 10, 70, 80));
		addComponent(dataBox);
		paint = AnimationFactory.getAnimation(Animations.Paint);
		paint.setWait(100);
	}

	private void initialiseData() {
		dataLen = 256;
		dataBounds = new Point(0, 1000);
		data = new ArrayDeque<Double>();
		for (int i=0; i<dataLen; i++) data.addLast(0d);
	}

	public synchronized void addData(int[] d) {
		for (int i : d) {
			if (!data.isEmpty()) data.pop();
			data.addLast((double) i);
		}
	}

	public synchronized void shiftData() {
		if (!data.isEmpty()) data.pop();
		data.addLast(0d);
	}

	public synchronized Deque<Double> getData() {
		//Need to make deep copy so only access to structure is kept within sync block
		Deque<Double> copy = new ArrayDeque<>();
		for (double d : data) copy.addLast(d);
		return copy;
	}

	@Override
	public void enter() {
		if (paint!=null) paint.start();
	}

	@Override
	public void destroy() {
		if (paint!=null) paint.end();
		super.destroy();
	}

	@Override
	public void draw(Graphics2D g) {
		GUI.getInstance().getScreenUtils().drawDataWave(g, this, dataBox);
		super.draw(g);
	}
}
