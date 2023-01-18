package client.gui.views;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayDeque;
import java.util.Deque;

import client.gui.Animation;
import client.gui.Animation.Animations;
import client.gui.ScreenUtils;
import client.gui.components.SimpleBox;
import client.gui.components.XboxButton;
import general.Point;
import general.Rectangle;

public class CallView extends View {

	public Deque<Double> data; //Voice data
	public int dataLen; //Length of data structure (actual deque may not always be that length)
	public Point dataBounds; //Upper and lower bound of data
	
	SimpleBox dataBox; //Bounding component when drawing data
	Animation dataAni; //Shifts data along as time goes on
	
	public CallView() {
		super(ViewType.Call, new Rectangle(0, 0, 100, 100));
		data = new ArrayDeque<Double>();
		dataLen = 200;
		dataBounds = new Point(0, 1000);
		randomiseData();
		
		components.add(new XboxButton(new Rectangle(70, 40, 10, 20), "B", new Color(126, 0, 14), new Color(191, 0, 9), this));
		dataBox = new SimpleBox(new Rectangle(0, 15, 70, 70), this);
		components.add(dataBox);
		dataAni = new Animation(data, Animations.DataMove);
	}
	
	public void randomiseData() {
		for (int i=0; i<dataLen; i++) {
			double d = Math.floor((Math.random()*(dataBounds.y-dataBounds.x))+dataBounds.x);
			data.addLast(d);
		}
		System.out.println(data.toString());
	}
	
	public void addData(int[] d) {
		for (int i : d) data.addLast((double) i);
		for (int i=0; i<d.length; i++) {
			if (!data.isEmpty()) data.pop();
		}
	}

	@Override
	public void enter() {
		System.out.println("Call entered");
		if (dataAni!=null) dataAni.start();
	}

	@Override
	public void destroy() {
		System.out.println("Call destroying");
		if (dataAni!=null&&dataAni.isRunning()) dataAni.stop();
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
