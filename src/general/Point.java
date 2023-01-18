package general;

public class Point extends Coord {
	public double x;
	public double y;
	
	public Point() {}
	
	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	@Override
	public String toString() {
		return "["+x+", "+y+"]";
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Rectangle)) return false;
		Rectangle r = (Rectangle) o;
		if (x==r.x&&y==r.y) return true;
		return false;
	}
	
	public Point clone() {
		return new Point(x, y);
	}
}
