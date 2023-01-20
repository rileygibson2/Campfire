package client.gui.views;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayDeque;
import java.util.Deque;

import client.gui.ScreenUtils;
import client.gui.components.SimpleBox;
import client.gui.components.XboxButton;
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
	ThreadController paintAni; //Shifts data along as time goes on

	public CallView() {
		super(ViewType.Call, new Rectangle(0, 0, 100, 100));
		initialiseData();

		components.add(new XboxButton(new Rectangle(75, 40, 10, 20), "B", new Color(126, 0, 14), new Color(191, 0, 9), this));
		dataBox = new SimpleBox(new Rectangle(0, 15, 70, 70), this);
		components.add(dataBox);
		paintAni = AnimationFactory.getAnimation(40, Animations.Paint);
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
		if (paintAni!=null) paintAni.start();
	}

	@Override
	public void destroy() {
		if (paintAni!=null&&paintAni.isRunning()) paintAni.end();
	}

	@Override
	public void doMove(Point p) {
		// TODO Auto-generated method stub

	}

	@Override
	public void draw(Graphics2D g) {
		ScreenUtils.drawDataWave(g, this, dataBox);
		super.draw(g);
	}
}
