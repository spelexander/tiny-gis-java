package spelexander.gis;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.geojson.LngLatAlt;

public class MapGisGridUtil implements GisGridUtil<GisGridAnnotation> {

	@Override
	public ScaleRotation getBestFieldBearing(List<GisGridAnnotation> annotations) {
		List<LngLatAlt> coords = getLongestLine(annotations);
		double bearing = getBearing(coords.get(0), coords.get(1));

		// We always want our bearing orientated left to right
		if (bearing > 180) {
			bearing = bearing - 180;
		}

		ScaleRotation rot = new ScaleRotation(bearing);

		return rot;
	}

	@Override
	public LngLatAlt getCenterPoint(GisGridAnnotation... annotations) {
		return getCenterPoint(Arrays.asList(annotations));
	}

	@Override
	public LngLatAlt getCenterPoint(List<GisGridAnnotation> annotations) {
		Objects.requireNonNull(annotations, "Must provide an actual list of annotations");
		if (annotations.isEmpty()) {
			return null;
		}

		double avg_long = 0;
		double avg_lat = 0;
		double avg_alt = 0;

		int count = 0;
		for (GisGridAnnotation annotation: annotations) {
			if (!annotation.canUse()) {
				continue;
			}

			for (LngLatAlt point : annotation.getExteriorRing()) {
				avg_long += point.getLongitude();
				avg_lat += point.getLatitude();
				if (point.hasAltitude()) {
					avg_alt += point.getAltitude();
				}
				count++;
			}		
		}

		avg_long = avg_long / count;
		avg_lat = avg_lat / count;
		avg_alt = avg_alt / count;

		LngLatAlt result = new LngLatAlt(avg_long, avg_lat, avg_alt);

		return result;
	}

	@Override
	public double getDistanceBetween(LngLatAlt a, LngLatAlt b) {
		Objects.requireNonNull(a);
		Objects.requireNonNull(b);

		return distance(a, b);
	}

	@Override
	public LngLatAlt transformToLongLat(Point point, LngLatAlt topLeft, Dimension fieldSize, ScaleRotation rotation,
			WidthHeightProvider params) {

		double x = params.getMetersForUnitX(point.x);
		double y = params.getMetersForUnitY(point.y);

		double rightAngledBearing = rotation.do90();

		LngLatAlt xMove = this.movePoint(topLeft, x, rotation.bearing);
		LngLatAlt yMove = this.movePoint(topLeft, y, rightAngledBearing);

		return new LngLatAlt(xMove.getLongitude(), yMove.getLatitude());
	}

	@Override
	public Point transformToXY(LngLatAlt point, LngLatAlt topLeftPoint, Dimension fieldSize, ScaleRotation rotation,
			WidthHeightProvider params) {

		LngLatAlt y_change = new LngLatAlt(topLeftPoint.getLongitude(), point.getLatitude());
		LngLatAlt x_change = new LngLatAlt(point.getLongitude(), topLeftPoint.getLatitude());

		double x_distance = this.getDistanceBetween(topLeftPoint, x_change);
		double y_distance = this.getDistanceBetween(topLeftPoint, y_change);

		Double x = params.getUnitXForMeters(x_distance);
		Double y = params.getUnitYForMeters(y_distance);	

		return new Point((int) Math.round(x), (int) Math.round(y));
	}

	@Override
	public Dimension getFieldSizeToUse(List<GisGridAnnotation> annotations, ScaleRotation rotation, WidthHeightProvider params) {
		// from the center point find out which point is furthest away in the x direction and then the y direction

		LngLatAlt centerPoint = this.getCenterPoint(annotations);
		//		annotations = this.rotateToNorth(annotations, rotation);

		double max_x = 0;
		double max_y = 0;

		for (GisGridAnnotation a : annotations) {
			for (LngLatAlt p : a.getExteriorRing()) {

				LngLatAlt y_change = new LngLatAlt(centerPoint.getLongitude(), p.getLatitude());
				LngLatAlt x_change = new LngLatAlt(p.getLongitude(), centerPoint.getLatitude());

				double x = this.getDistanceBetween(x_change, centerPoint);
				if (x > max_x) {
					max_x = x;
				}

				double y = this.getDistanceBetween(y_change, centerPoint);
				if (y > max_y) {
					max_y = y;
				}
			}
		}

		Integer plotsX = (int) (params.getUnitXForMeters(max_x) * 2.0);
		Integer plotsY = (int) (params.getUnitYForMeters(max_y) * 2.0);

		return new Dimension(plotsX + params.getAdditionalX(), plotsY + params.getAdditionalY());
	}

	@Override
	public Boolean doesPointIntersect(Point point, GisGridAnnotation annotation, LngLatAlt topLeft, Dimension fieldSize,
			ScaleRotation rotation, WidthHeightProvider params) {

		Shape shape = getPolygonShape(annotation, topLeft, fieldSize, rotation, params);
		return shape.getBounds2D().contains(point.x, point.y);
	}

	@Override
	public Shape getPolygonShape(GisGridAnnotation annotation, LngLatAlt topLeft, Dimension fieldSize, 
			ScaleRotation rotation, WidthHeightProvider params, Function<Point,Point> pointMutator) {

		int size = annotation.getExteriorRing().size();
		int[] xs = new int[size];
		int[] ys = new int[size];

		int count = 0;
		for (LngLatAlt point : annotation.getExteriorRing()) {
			Point p = this.transformToXY(point, topLeft, fieldSize, rotation, params);

			if (pointMutator != null) {
				p = pointMutator.apply(p);
			}

			xs[count] = p.x;
			ys[count] = p.y;


			count++;
		}

		return new java.awt.Polygon(xs, ys, size);
	}

	/**
	 * Calculate distance between two points in latitude and longitude taking
	 * into account height difference. If you are not interested in height
	 * difference pass 0.0. Uses Haversine method as its base.
	 * 
	 * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
	 * el2 End altitude in meters
	 * @returns Distance in Meters
	 */
	public static double distance(LngLatAlt first, LngLatAlt second) {

		double lon1 = first.getLongitude();
		double lon2 = second.getLongitude();

		double lat1 = first.getLatitude();
		double lat2 = second.getLatitude();

		final int R = 6371; // Radius of the earth

		double latDistance = Math.toRadians(lat2 - lat1);
		double lonDistance = Math.toRadians(lon2 - lon1);
		double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
				+ Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
				* Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double distance = R * c * 1000; // convert to meters

		return distance;
	}

	/**
	 * Calculate the bearing of the line formed by two coordinates
	 * @param lat1
	 * @param lon1
	 * @param lat2
	 * @param lon2
	 * @return
	 */
	@Override
	public double getBearing(LngLatAlt first, LngLatAlt second){

		double longitude1 = first.getLongitude();
		double longitude2 = second.getLongitude();

		double lat1 = first.getLatitude();
		double lat2 = second.getLatitude();

		double latitude1 = Math.toRadians(lat1);
		double latitude2 = Math.toRadians(lat2);
		double longDiff= Math.toRadians(longitude2 - longitude1);
		double y= Math.sin(longDiff)*Math.cos(latitude2);
		double x=Math.cos(latitude1)*Math.sin(latitude2)-Math.sin(latitude1)*Math.cos(latitude2)*Math.cos(longDiff);

		return (Math.toDegrees(Math.atan2(y, x))+360)%360;
	}



	@Override
	public GisGridAnnotation revertRotation(GisGridAnnotation ann, LngLatAlt centerPoint, ScaleRotation rot) {
		return this.rotateTo(ann, centerPoint, rot, RotationType.REVERT);
	}

	@Override
	public List<GisGridAnnotation> revertRotation(List<GisGridAnnotation> anns, ScaleRotation rot) {
		return this.rotateTo(anns, rot, RotationType.REVERT);
	}

	@Override
	public GisGridAnnotation rotateToNorth(GisGridAnnotation ann, LngLatAlt centerPoint, ScaleRotation rot) {
		return this.rotateTo(ann, centerPoint, rot, RotationType.NORTH);
	}

	@Override
	public List<GisGridAnnotation> rotateToNorth(List<GisGridAnnotation> anns, ScaleRotation rot) {
		return this.rotateTo(anns, rot, RotationType.NORTH);
	}

	@Override
	public LngLatAlt revertRotation(LngLatAlt point, LngLatAlt centerPoint, ScaleRotation rot) {
		return this.rotateTo(point, centerPoint, rot, RotationType.REVERT);
	}

	@Override
	public LngLatAlt rotateToNorth(LngLatAlt point, LngLatAlt centerPoint, ScaleRotation rot) {
		return this.rotateTo(point, centerPoint, rot, RotationType.NORTH);
	}

	@Override
	public List<GisGridAnnotation> rotateTo(List<GisGridAnnotation> annotations, ScaleRotation rot, RotationType type) {
		LngLatAlt centerPoint = this.getCenterPoint(annotations);
		List<GisGridAnnotation> result = new ArrayList<>();

		for (GisGridAnnotation annotation : annotations) {
			result.add(this.rotateTo(annotation, centerPoint, rot, type));
		}

		return result;
	}

	@Override
	public GisGridAnnotation rotateTo(GisGridAnnotation annotation, LngLatAlt centerPoint, ScaleRotation rot, RotationType type) {
		List<LngLatAlt> points = new ArrayList<>();

		for (LngLatAlt point : annotation.getExteriorRing()) {
			points.add(rotateTo(point, centerPoint, rot, type));
		}

		GisGridAnnotation poly = new GisGridAnnotation(points);
		return poly;
	}

	@Override
	public LngLatAlt rotateTo(LngLatAlt point, LngLatAlt centerPoint, ScaleRotation rot, RotationType type) {

		double angle = 0.0;

		switch (type) {
		case NORTH:
			if (rot.bearing == 0.0) {
				return point;
			} else {
				// Negative bearing for clockwise rotation
				angle = -rot.bearing;
			}
			break;
		case REVERT:			
			if (rot.bearing == 0.0) {
				return point;
			} else {
				angle = rot.bearing - 360.0;
			}
			break;
		default:
			break;	
		}

		double newX = centerPoint.getLongitude() + (point.getLongitude() - centerPoint.getLongitude()) 
				* Math.cos(angle) - (point.getLatitude() - centerPoint.getLatitude()) * Math.sin(angle);

		double newY = centerPoint.getLatitude() + (point.getLongitude() - centerPoint.getLongitude()) 
				* Math.sin(angle) + (point.getLatitude() - centerPoint.getLatitude()) * Math.cos(angle);

		return new LngLatAlt(newX, newY);
	}


	/**
	 * Move point a specified distance according to some bearing
	 * @param point
	 * @param distanceInMetres
	 * @param bearing
	 * @return
	 */
	public LngLatAlt movePoint(LngLatAlt point, double distanceInMetres, double bearing) {

		double longitude = point.getLongitude();
		double latitude = point.getLatitude();

		double brngRad = Math.toRadians(bearing);
		double latRad = Math.toRadians(latitude);
		double lonRad = Math.toRadians(longitude);
		int earthRadiusInMetres = 6371000;
		double distFrac = distanceInMetres / earthRadiusInMetres;

		double latitudeResult = Math.asin(Math.sin(latRad) * Math.cos(distFrac) + Math.cos(latRad) * Math.sin(distFrac) * Math.cos(brngRad));
		double a = Math.atan2(Math.sin(brngRad) * Math.sin(distFrac) * Math.cos(latRad), Math.cos(distFrac) - Math.sin(latRad) * Math.sin(latitudeResult));
		double longitudeResult = (lonRad + a + 3 * Math.PI) % (2 * Math.PI) - Math.PI;

		return new LngLatAlt(Math.toDegrees(longitudeResult), Math.toDegrees(latitudeResult));
	}

	@Override
	public LngLatAlt getTopLeftPoint(List<GisGridAnnotation> annotations, Dimension fieldSize,
			ScaleRotation rotation, WidthHeightProvider params) {

		LngLatAlt center = this.getCenterPoint(annotations);
		//		annotations = this.rotateToNorth(annotations, rotation);

		double width = params.getMetersForUnitX(fieldSize.getWidth() / 2.0);
		double height = params.getMetersForUnitY(fieldSize.getHeight() / 2.0);

		double upBearing = rotation.doMinus90();
		double backBearing = rotation.do180();

		LngLatAlt x_move = this.movePoint(center, width, backBearing);
		LngLatAlt y_move = this.movePoint(center, height, upBearing);

		//		return this.revertRotation(new LngLatAlt(x_move.getLongitude(), y_move.getLatitude()), center, rotation);
		return new LngLatAlt(x_move.getLongitude(), y_move.getLatitude());
	}

	@Override
	public List<LngLatAlt> getLongestLine(List<GisGridAnnotation> annotations) {

		LngLatAlt a = null;
		LngLatAlt b = null;

		double max_distance = 0.0;

		for (GisGridAnnotation annotation : annotations) {

			List<LngLatAlt> points = annotation.getExteriorRing();

			int count = 1;
			for (LngLatAlt point : points) {
				if (count >= points.size()) {
					count = 0;
				}

				LngLatAlt point2 = points.get(count);

				double distance = this.getDistanceBetween(point, point2);
				if (distance > max_distance) {
					max_distance = distance;
					a = point;
					b = point2;
				}
				count++;
			}
		}

		return Arrays.asList(a, b);
	}

	@Override
	public Shape getPolygonShape(GisGridAnnotation annotation, LngLatAlt topLeft, Dimension fieldSize,
			ScaleRotation rotation, WidthHeightProvider params) {
		return this.getPolygonShape(annotation, topLeft, fieldSize, rotation, params, null);
	}

}
