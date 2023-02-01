package client.gui.components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import client.gui.GUI;
import general.GetterSubmitter;
import general.Point;
import general.Rectangle;
import general.Utils;

public class DropDown<E> extends Component {

	private LinkedHashMap<String, E> options; //LinkedHashMap maintains an order
	Map.Entry<String, E> selectedItem; //The selected item and it's name
	private Map.Entry<String, E> noneItem =new AbstractMap.SimpleEntry<>("None", null); //A default item
	
	GetterSubmitter<LinkedHashMap<String, E>, E> actions;
	
	private SimpleBox topBox;
	public Label selectedLabel; //The gui selected item component
	public ScrollBar sB;

	public DropDown(Rectangle rectangle) {
		super(rectangle);
		options = new LinkedHashMap<String, E>();
		selectedItem = noneItem;
		
		//Top box
		topBox = new SimpleBox(new Rectangle(0, 0, 100, 100), GUI.focus);
		topBox.setRounded(true);
		addComponent(topBox);
		
		//Selected label
		selectedLabel = new Label(new Point(5, 50), selectedItem.getKey(), new Font("Geneva", Font.ITALIC, 12), new Color(200, 200, 200)); 
		topBox.addComponent(selectedLabel);
		
		//Scrollbar
		sB = new ScrollBar();
		sB.setArmed(false);
		sB.increasePriority();
		addComponent(sB);
		
	}
	
	public void setActions(GetterSubmitter<LinkedHashMap<String, E>, E> actions) {this.actions = actions;}
	
	public void setSelected(Map.Entry<String, E> selected) {
		this.selectedItem = selected;
		selectedLabel.text = selectedItem.getKey();
	}
	
	@Override
	public void doClick(Point p) {
		boolean closed = false;
		if (isSelected()) {
			close(scalePoint(p));
			closed = true;
		}
		else open();
		super.doClick(p);
		if (closed) doDeselect();
	}
	
	@Override
	public void doDeselect() {
		if (isSelected()) close(null);
		super.doDeselect();
	}

	private void open() {
		if (actions!=null) {
			options = actions.get();
			/*options.put("Extra1", null);
			options.put("Extra2", null);
			options.put("Extra3", null);*/
		}
		if (options.isEmpty()) return;
		
		int size = options.size();
		if (size>3) {
			size = 3;
			//Update and arm scrollbar
			sB.setBounds(new Point(100, 100*(size+1)));
			sB.setY(100);
			sB.setHeight(100*size);
			sB.setArmed(true);
		}
		
		setSelected(true);
		increasePriority();

		//Expanded box
		addComponent(new SimpleBox(new Rectangle(0, 100, 100, 100*size), GUI.focus2));
		
		//Curve masker box
		addComponent(new SimpleBox(new Rectangle(0, 95, 100, 10), GUI.focus2));
		
		//Top bar
		addComponent(new SimpleBox(new Rectangle(0, 95, 100, 4), new Color(50, 50, 50)));
		
		//Option elements
		int y = 155;
		
		//Extra bar to help scroll with top
		SimpleBox s = new SimpleBox(new Rectangle(0, y-55, 100, 4), new Color(60, 60, 60));
		addComponent(s);
		sB.addToScroll(s);
		
		for (Map.Entry<String, E> m : options.entrySet()) {
			//Label
			Label l = new Label(new Point(8, y), m.getKey(), new Font("Geneva", Font.ITALIC, 11), new Color(200, 200, 200));
			addComponent(l);
			sB.addToScroll(l);
			
			//Bar
			s = new SimpleBox(new Rectangle(0, y+50, 100, 4), new Color(60, 60, 60));
			addComponent(s);
			sB.addToScroll(s);
			
			y += 100;
		}
	}

	private void close(Point p) {
		if (p!=null) {
			//Get clicked element
			int i = (int) Math.floor(p.y)-1;
			if (i>=0&&i<options.size()) selectedItem = getOptionAt(i);
			if (selectedItem!=null) selectedLabel.text = selectedItem.getKey();
			
			if (actions!=null) actions.submit(selectedItem.getValue());
		}

		setSelected(false);
		decreasePriority();
		Set<Component> toRemove = new HashSet<>();
		for (Component c : getComponents()) {
			if ((c instanceof SimpleBox && c!= topBox) || (c instanceof Label && c!= selectedLabel)) {
				toRemove.add(c);
			}
		}
		removeComponents(toRemove);
		
		//Disarm scrollbar
		sB.setY(sB.getOriginalRec().y);
		sB.setHeight(sB.getOriginalRec().height);
		sB.setArmed(false);
	}
	
	@Override
	public void doHover() {
		Utils.setCursor(Cursor.HAND_CURSOR);
	}

	@Override
	public void doUnhover() {
		Utils.setCursor(Cursor.DEFAULT_CURSOR);
	}
	
	@Override
	public void doScroll(Point p, int amount) {
		sB.scroll(amount);
	}
	
	private Map.Entry<String, E> getOptionAt(int i) {
		int z = 0;
		for (Map.Entry<String, E> m : options.entrySet()) {
			if (z==i) return m;
			z++;
		}
		return null;
	}
}
