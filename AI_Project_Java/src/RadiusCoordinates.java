//simple object of x y coordinates for passing between radius generation functions
public class RadiusCoordinates {
	public int x;
	public int y;

	//this class is just an object for coordinates of any agent locatiosn and coordiantes to be checked within the radius
	public RadiusCoordinates(int x, int y)
	{
		this.x=x;
		this.y=y;
	}

}
