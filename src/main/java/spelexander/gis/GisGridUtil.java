package spelexander.gis;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Shape;
import java.util.List;
import java.util.function.Function;

import org.geojson.LngLatAlt;
import org.geojson.Polygon;

public interface GisGridUtil<E extends Polygon> {
	
	public LngLatAlt getCenterPoint(@SuppressWarnings("unchecked") E... annotations);
	public LngLatAlt getCenterPoint(List<E> annotations);
	
	/**
	 * Defaults to meters.. can change units outside of this
	 * @param a
	 * @param b
	 * @return
	 */
	double getDistanceBetween(LngLatAlt a, LngLatAlt b);
	
	public enum RotationType {
		NORTH,
		REVERT
	}
	
	LngLatAlt transformToLongLat(Point point, LngLatAlt topLeft, Dimension fieldSize, ScaleRotation rotation, WidthHeightProvider params);
		
	Point transformToXY(LngLatAlt point, LngLatAlt topLeftPoint, Dimension fieldSize, ScaleRotation rotation,
			WidthHeightProvider params);
	
	LngLatAlt getTopLeftPoint(List<GisGridAnnotation> annotations, Dimension fieldSize, ScaleRotation rotation,
			WidthHeightProvider params);
	
	Shape getPolygonShape(GisGridAnnotation annotation, LngLatAlt topLeft, Dimension fieldSize,
			ScaleRotation rotation, WidthHeightProvider params);
	
	Boolean doesPointIntersect(Point point, GisGridAnnotation annotation, LngLatAlt topLeft, Dimension fieldSize,
			ScaleRotation rotation, WidthHeightProvider params);
	
	List<LngLatAlt> getLongestLine(List<GisGridAnnotation> annotations);
	
	double getBearing(LngLatAlt first, LngLatAlt second);
	
	LngLatAlt rotateTo(LngLatAlt point, LngLatAlt centerPoint, ScaleRotation rot, RotationType type);
	
	GisGridAnnotation rotateTo(GisGridAnnotation annotation, LngLatAlt centerPoint, ScaleRotation rot,
			RotationType type);
	
	List<GisGridAnnotation> rotateTo(List<GisGridAnnotation> annotations, ScaleRotation rot,
			RotationType type);
	
	LngLatAlt rotateToNorth(LngLatAlt point, LngLatAlt centerPoint, ScaleRotation rot);
	
	LngLatAlt revertRotation(LngLatAlt point, LngLatAlt centerPoint, ScaleRotation rot);
	
	List<GisGridAnnotation> rotateToNorth(List<GisGridAnnotation> anns, ScaleRotation rot);
	
	GisGridAnnotation rotateToNorth(GisGridAnnotation ann, LngLatAlt centerPoint, ScaleRotation rot);
	
	GisGridAnnotation revertRotation(GisGridAnnotation ann, LngLatAlt centerPoint, ScaleRotation rot);
	
	List<GisGridAnnotation> revertRotation(List<GisGridAnnotation> anns, ScaleRotation rot);
	
	Dimension getFieldSizeToUse(List<GisGridAnnotation> annotations, ScaleRotation rotation,
			WidthHeightProvider params);
	
	ScaleRotation getBestFieldBearing(List<GisGridAnnotation> annotations);

	
	Shape getPolygonShape(GisGridAnnotation annotation, LngLatAlt topLeft, Dimension fieldSize,
			ScaleRotation rotation, WidthHeightProvider params, Function<Point, Point> pointMutator);
}
