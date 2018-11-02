package spelexander.gis;

import java.io.File;
import java.util.List;

import org.geojson.Polygon;

public interface GisGridAnnotationReader<E extends Polygon> {
	
	/**
	 * to string from object
	 * @param entity
	 */

	/**
	 * read geo object from file 
	 * @param json
	 * @return
	 */
	public List<E> fromGeoJson(File json);

	String toGeoJson(List<E> entity);
	
}
