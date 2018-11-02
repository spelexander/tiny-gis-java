package spelexander.gis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.geojson.LngLatAlt;
import org.junit.Before;
import org.junit.Test;

import spelexander.gis.GisGridUtil.RotationType;

public class MapGisGridUtilTest {

	List<GisGridAnnotation> annotations1;
	
	List<GisGridAnnotation> annotations2;
	
	List<GisGridAnnotation> annotations3;

	private MapGisGridUtil util = new MapGisGridUtil();
	
	private void neff() {
		
		LngLatAlt a = new LngLatAlt(149.3865874610993, -35.07597449607705);
		LngLatAlt b = new LngLatAlt(149.3876738387109, -35.07607456965434);
		
		// Distance in meters between two points
		Double meters = util.getDistanceBetween(a, b);
		
		// Bearing in degreses between two points
		Double bearing = util.getBearing(a, b);
		
		List<GisGridAnnotation> annotations = new ArrayList<>();
		
		// Two connected points in all of the polygons which have the longest edge
		List<LngLatAlt> largestDistanceBetween = util.getLongestLine(annotations);
		
		// Center point of all polygons/coordinates
		LngLatAlt centerPoint = util.getCenterPoint(annotations);
		
		// Calculating the best field bearing to render the most on screen
		ScaleRotation rotation = util.getBestFieldBearing(annotations);
		Double bestBearing = rotation.bearing;
		//rotation.do180();
		//rotation.do90();
		//...
		
		// Rotating all coordinates/polyongs around a center point so that best bearing is now in line with N
		List<GisGridAnnotation> rotated = util.rotateTo(annotations, rotation, RotationType.NORTH);
		
		// Calculate the best grid size for your coordinates/polygons and their bearing
		// (Width height provider is a converter from Meters to your grid X/Y and vice versa)
		Dimension gridSize = util.getFieldSizeToUse(annotations, rotation, new WidthHeightProvider() {

			@Override
			public double getTotalUnitX() {
				// TODO width in meters of a grid cell (or 1 X)
				return 0;
			}

			@Override
			public double getTotalUnitY() {
				// TODO height in meters of a grid cell (or 1 Y)
				return 0;
			}

			@Override
			public double getUnitXForMeters(double meters) {
				// TODO the number of X units for a distance
				return 0;
			}

			@Override
			public double getMetersForUnitX(double x) {
				// TODO distance in meters for a single cell's width (or 1 X) in the grid
				return 0;
			}

			@Override
			public double getUnitYForMeters(double y_distance) {
				// TODO the number of Y units for a distance
				return 0;
			}

			@Override
			public double getMetersForUnitY(double y) {
				// TODO distance in meters for a single cell's height (or 1 Y) in the grid
				return 0;
			}

			@Override
			public int getAdditionalY() {
				// TODO Extra Y spacing from edges
				return 0;
			}

			@Override
			public int getAdditionalX() {
				// TODO Extra X spacing from edges
				return 0;
			}
		});
	}
	
	private Dimension fieldSize = new Dimension(278, 304);
	
	// Mock prvoider
	private WidthHeightProvider provider = new WidthHeightProvider() {

		double borderArea = 10.0; // meters
		
		@Override
		public double getTotalUnitX() {
			return 2.5;
		}

		@Override
		public double getTotalUnitY() {
			return 2.5;
		}

		@Override
		public double getUnitXForMeters(double meters) {
			return meters / this.getTotalUnitX();
		}

		@Override
		public double getMetersForUnitX(double x) {
			return x * this.getTotalUnitX();
		}

		@Override
		public double getUnitYForMeters(double meters) {
			return meters / this.getTotalUnitY();
		}

		@Override
		public double getMetersForUnitY(double y) {
			return y * this.getTotalUnitX();
		}

		@Override
		public int getAdditionalY() {
			return (int) Math.round(borderArea / this.getTotalUnitY());
		}

		@Override
		public int getAdditionalX() {
			return (int) Math.round(borderArea / this.getTotalUnitY());
		}			
	};
	
	MapGisGridAnnotationReader reader = new MapGisGridAnnotationReader();
	
	@Before
	public void loadFiles() {
		try {			
			annotations1 = reader.fromGeoJson(new File(this.getClass().getResource("geojson-test1.json").toURI()));
			annotations2 = reader.fromGeoJson(new File(this.getClass().getResource("geojson-test2.json").toURI()));
			annotations3 = reader.fromGeoJson(new File(this.getClass().getResource("geojson-test3.json").toURI()));	
			

		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testFieldRotation() {
		ScaleRotation rot = util.getBestFieldBearing(annotations2);
		
		List<GisGridAnnotation> anns = util.rotateToNorth(annotations2, rot);
		anns.addAll(util.revertRotation(anns, rot));

		
		anns.addAll(annotations2);
		String result = reader.toGeoJson(anns);
		System.out.println(result);
	}
	
	@Test
	public void testBestFieldSize() {
		ScaleRotation rot = util.getBestFieldBearing(annotations2);
		
		Dimension size = util.getFieldSizeToUse(annotations2, rot,  provider);
		System.out.println("Field size to use -> " + size);
	}
	
	@Test
	public void isCenterPointCorrect() {
		LngLatAlt center = util.getCenterPoint(this.annotations2);
		
		System.out.println("CenterPoint -> " + center + " m");		
		
		assertEquals("Latitude claculation was incorrect", -35, Math.round(center.getLatitude()));
		assertEquals("Longitude claculation was incorrect", 149, Math.round(center.getLongitude()));
	}
	
	@Test
	public void testDistanceCalculation() {
		List<LngLatAlt> list = this.annotations2.get(0).getExteriorRing();
		LngLatAlt a = list.get(0);
		LngLatAlt b = list.get(1);
		
		Object distance = util.getDistanceBetween(a, b);
		System.out.println("Distance -> " + distance + " m");		
		
		assertEquals("Expected distance was incorrect", 122.564546112622, distance);
	}
	
	@Test
	public void canCalculateLongestLine() {
		List<LngLatAlt> coords = util.getLongestLine(annotations2);
		
		System.out.println("LongestLine -> " + coords + " distance -> " + util.getDistanceBetween(coords.get(0), coords.get(1)));
		
		assertEquals("Should always return two coordinates", 2, coords.size());
		assertEquals("Should be the largets edge", 555, Math.round(util.getDistanceBetween(coords.get(0), coords.get(1))));
		
	}
	
	@Test
	public void canCalculateBearing() {
		ScaleRotation rot = util.getBestFieldBearing(annotations2);
		Double bearing = rot.bearing;
		
		System.out.println("Bearing -> " + bearing);

		assertTrue("Bearing should be >= 0, and bearing should be <= 360", bearing >= 0 && bearing <= 360);
		assertEquals("Bearing should match", 90.0, bearing, 0.01);
	}
	
	@Test
	public void calculateTopLeft() {
		ScaleRotation rot = util.getBestFieldBearing(annotations2);
		
		LngLatAlt topLeft = util.getTopLeftPoint(annotations2, fieldSize, rot, provider);
		
		System.out.println("TopLeftPoint -> " + topLeft);		
		
		assertNotNull(topLeft);
	}
	
	@Test
	public void testTransformToLongLat() {
		ScaleRotation rot = util.getBestFieldBearing(annotations2);
		
		LngLatAlt topLeft = util.getTopLeftPoint(annotations2, fieldSize, rot, provider);
		
		Point point = new Point(25, 65);
		
		LngLatAlt result = util.transformToLongLat(point, topLeft, fieldSize, rot, provider);
		
		System.out.println("TransformedFromXY -> " + result);
		assertNotNull(result);
		assertEquals("Was not correct value", "LngLatAlt{longitude=149.3865874610993, latitude=-35.07597449607705, altitude=NaN}", result.toString());
	}
	
	@Test
	public void testTransformToXY() {
		ScaleRotation rot = util.getBestFieldBearing(annotations2);
		
		LngLatAlt topLeft = util.getTopLeftPoint(annotations2, fieldSize, rot, provider);
		
		 //new Point(25, 65);
		
		LngLatAlt result = new LngLatAlt(149.3865874610993, -35.07597449607705);
				
		Point point = util.transformToXY(result, topLeft, fieldSize, rot, provider);
		
		System.out.println("TransformedFromLngLat -> " + point);
		assertNotNull(result);
		assertEquals("Point X Matched", point.x, 25);
		assertEquals("Point Y Matched", point.y, 65);
	}
	
	
}
