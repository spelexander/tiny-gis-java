package spelexander.gis;

public interface WidthHeightProvider {

	/**
	 * Provides width in any unit of mesurement for a single grid cell width
	 * @return
	 */
	public double getTotalUnitX();
	
	/**
	 * Provides height in any unit of mesurement for a single grid cell width
	 * @return
	 */
	public double getTotalUnitY();
	
	public double getUnitXForMeters(double meters);
	public double getMetersForUnitX(double x);
	
	public double getUnitYForMeters(double y_distance);
	public double getMetersForUnitY(double y);
	
	public int getAdditionalY();
	public int getAdditionalX();

	
}
