package dom;

import java.util.ArrayList;
import java.util.List;

import client.gui.Element;

public class DOMNode {
	
	private Element e;
	private String label;
	
	private List<DOMNode> children;
	
	public DOMNode(Element e) {
		this.e = e;
		this.label = e.getClass().getSimpleName();
		children = new ArrayList<DOMNode>();
	}
	
	public Element getElement() {return e;}
	public String getLabel() {return label;}
	
	public void addChild(DOMNode n) {children.add(n);}
	public List<DOMNode> getChildren() {return children;}
}
