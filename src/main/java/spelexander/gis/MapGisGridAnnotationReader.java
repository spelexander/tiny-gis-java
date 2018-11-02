package spelexander.gis;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.GeoJsonObject;
import org.geojson.Geometry;
import org.geojson.LngLatAlt;
import org.geojson.Point;
import org.geojson.Polygon;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MapGisGridAnnotationReader implements GisGridAnnotationReader<GisGridAnnotation> {

	@Override
	public List<GisGridAnnotation> fromGeoJson(File json) {

		List<GisGridAnnotation> result = new ArrayList<>();

		try {
			InputStream inputStream = new FileInputStream(json);

			GeoJsonObject object = new ObjectMapper().readValue(inputStream, GeoJsonObject.class);
			annotationFromGeoJsonObject(object, result);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	public void annotationFromGeoJsonObject(GeoJsonObject object, final List<GisGridAnnotation> list) {

		if (object instanceof Polygon) {
			// Single polygon
			Polygon poly = (Polygon) object;
			GisGridAnnotation sla = new GisGridAnnotation(poly.getExteriorRing());
			list.add(sla);

		} else if (object instanceof Feature) {
			// Feature
			Feature feature = (Feature) object;
			GeoJsonObject geo = feature.getGeometry();

			// recursive for features inside of features..
			annotationFromGeoJsonObject(geo, list);

		} else if (object instanceof FeatureCollection) {
			// Feature
			FeatureCollection feature = (FeatureCollection) object;
			List<Feature> features = feature.getFeatures();
			for (Feature feat: features) {
				// recursive for features inside of features..
				annotationFromGeoJsonObject(feat.getGeometry(), list);
			}					
		} else if (object instanceof Point) {
			// Long Lat Alt point
			// Not handling yet
			Point point = (Point) object;

			// We're ignoring these for now..
			GisGridAnnotation sla = new GisGridAnnotation(Arrays.asList(point.getCoordinates()));
			list.add(sla);

		} else if (object instanceof Geometry) {
			// geometry?
			@SuppressWarnings("unchecked")
			Geometry<LngLatAlt> geo = (Geometry<LngLatAlt>) object;

			GisGridAnnotation sla = new GisGridAnnotation(geo.getCoordinates());
			list.add(sla);
		}
	}

	@Override
	public String toGeoJson(List<GisGridAnnotation> entity) {
		String json;
		try {
			FeatureCollection featureCollection = new FeatureCollection();
			for (GisGridAnnotation annotation : entity) {
				featureCollection.add(annotation.toFeature());
			}

			json = new ObjectMapper().writeValueAsString(featureCollection);
			return json;

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return null;
	}


}
