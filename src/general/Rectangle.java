package general;

public class Rectangle extends Coord {
	public double x;
	public double y;
	public double width;
	public double height;
	
	public Rectangle() {}
	
	public Rectangle(double x, double y, double width, double height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	@Override
	public String toString() {
		return "["+x+", "+y+", "+width+", "+height+"]";
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Rectangle)) return false;
		Rectangle r = (Rectangle) o;
		if (x==r.x&&y==r.y&&width==r.width&&height==r.height) return true;
		return false;
	}
	
	public Rectangle clone() {
		return new Rectangle(x, y, width, height);
	}
}
