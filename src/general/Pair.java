package general;

public class Pair {
	public Object a;
	public Object b;
	
	public Pair() {}
	
	public Pair(Object a, Object b) {
		this.a = a;
		this.b = b;
	}
	
	@Override
	public String toString() {
		return "["+a.toString()+", "+b.toString()+"]";
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Pair)) return false;
		Pair p = (Pair) o;
		if (a.equals(p.a)&&b.equals(p.b)) return true;
		return false;
	}
	
	public Pair clone() {
		return new Pair(a, b);
	}
}

