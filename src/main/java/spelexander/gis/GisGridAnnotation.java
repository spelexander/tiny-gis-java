package spelexander.gis;

import java.util.List;

import org.geojson.Feature;
import org.geojson.GeoJsonObject;
import org.geojson.Geometry;
import org.geojson.LngLatAlt;
import org.geojson.Point;
import org.geojson.Polygon;

public class GisGridAnnotation extends Polygon {
	
	private String name = "";
	
	public GisGridAnnotation(List<LngLatAlt> list) {
		super(list);
	}
	
	public Polygon getAsPolygon() {
		return this;
	}
	
	public boolean canUse() {
		return this.getExteriorRing().size() >= 3;
	}
	
	
	/**
	 * Return the lat long center point of this poly
	 * @return
	 */
	public Point getCenterPoint() {
		Double avg_x = 0.0;
		Double avg_y = 0.0;
		
		double count = 0.0;
		for (LngLatAlt point : this.getExteriorRing()) {
			avg_x += point.getLongitude();
			avg_y += point.getLatitude();
			count++;
		}
		
		avg_x = avg_x / count;
		avg_y = avg_y / count;
		
		return new Point(avg_x, avg_y);
	}

	public Feature toFeature() {
		Feature feature = new Feature();	
		Polygon polygon = new Polygon(this.getExteriorRing());
		feature.setGeometry(polygon);
		
		return feature;
	}

	public String getName() {
		return name;
	}

	public void setName(String value) {
		name = value;
	}

}
