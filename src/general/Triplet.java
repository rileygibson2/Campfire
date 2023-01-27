package general;

public class Triplet<A, B, C> {
	public A a;
	public B b;
	public C c;
	
	public Triplet() {}
	
	public Triplet(A a, B b, C c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}
	
	@Override
	public String toString() {
		return "["+a.toString()+", "+b.toString()+"]";
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Triplet)) return false;
		Triplet<A, B, C> p = (Triplet<A, B, C>) o;
		if (a.equals(p.a)&&b.equals(p.b)) return true;
		return false;
	}
	
	public Triplet<A, B, C> clone() {
		return new Triplet<A, B, C>(a, b, c);
	}
}

