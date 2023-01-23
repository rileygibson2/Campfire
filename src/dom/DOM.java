package dom;

import client.gui.Element;

public class DOM {

	private DOMViewer dGUI;
	private DOMNode root;

	public DOM() {
		dGUI = DOMViewer.initialise(this);
	}
	
	public void showVisualiser(boolean show) {
		DOMViewer.frame.setVisible(show);
		dGUI.isVisible = show;
	}
	
	public boolean visualiserVisible() {return dGUI.isVisible;}

	public void update(Element rootElem) {
		this.root = new DOMNode(rootElem);
		populate(root);
		dGUI.repaint();
	}
	
	public DOMNode getRoot() {return root;}

	private void populate(DOMNode node) {
		node.getElement().sortComponents();
		
		for (Element e : node.getElement().getComponents()) {
			DOMNode n = new DOMNode(e);
			node.addChild(n);
			populate(n);
		}
	}
	
	public int maxDepth(DOMNode node) {
	    if (node == null) return 0;
	    int maxDepth = 0;
	    
	    for (DOMNode child : node.getChildren()) {
	        maxDepth = Math.max(maxDepth, maxDepth(child));
	    }
	    return maxDepth + 1;
	}
	
	public int widthAtDepth(DOMNode node, int depth) {
	    if (root == null) return 0;
	    if (depth == 1) return 1;
	    
	    int width = 0;
	    for (DOMNode child : node.getChildren()) {
	        width += widthAtDepth(child, depth - 1);
	    }
	    return width;
	}

	private String getString(DOMNode node, int incr) {
		if (node==null) return null;

		String s = "";
		for (int i=0; i<incr; i++) s += " --- ";
		s += node.getLabel()+"\n";

		incr++;
		for (DOMNode n : node.getChildren()) {
			s += getString(n, incr);
		}
		return s;
	}

	@Override
	public String toString() {
		return getString(root, 0);
	}
}
