package spelexander.gis;

public class ScaleRotation {
	public double bearing;
	
	public ScaleRotation(double bearing) {
		this.bearing = bearing;
	}
	
	public double doMinus90() {
		double value = - 90.0 + bearing;
		if (value < 0) {
			value = 360 + value;
		}
		return value;
	}
	
	public double do90() {
		double value = 90.0 + bearing;
		if (value > 360) {
			value = value - 360;
		}
		return value;
	}
	
	public double do180() {
		double value = 180.0 + bearing;
		if (value > 360) {
			value = value - 360;
		}
		return value;
	}

	public double doMinus270() {
		double value = - 270.0 + bearing;
		if (value > 360) {
			value = value - 360;
		}
		return value;
	}
	
	public double do270() {
		double value = 270.0 + bearing;
		if (value > 360) {
			value = value - 360;
		}
		return value;
	}
}