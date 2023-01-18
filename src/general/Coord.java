package general;

public abstract class Coord {
	
	public CoordType type;
	
	public enum CoordType {
		Percentage,
		Real
	};
}
