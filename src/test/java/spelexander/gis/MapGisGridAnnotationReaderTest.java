package spelexander.gis;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

import org.geojson.LngLatAlt;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MapGisGridAnnotationReaderTest {

	private File testFileSinglePolygon = null;

	private File testFileMultiPolygon = null;

	private File testFileExtras = null;

	private File testFileEmpty = null;

	private File testFileError = null;

	@Before
	public void loadFiles() {
		try {
			testFileSinglePolygon = new File(this.getClass().getResource("geojson-test1.json").toURI());
			testFileMultiPolygon = new File(this.getClass().getResource("geojson-test2.json").toURI());
			testFileExtras = new File(this.getClass().getResource("geojson-test3.json").toURI());			
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	/**
 	* helper method
 	* @param file
 	* @param expected
 	*/
	public void fileCorrect(File file, int expected) {
		MapGisGridAnnotationReader reader = new MapGisGridAnnotationReader();
		List<GisGridAnnotation> result = reader.fromGeoJson(file);

		assertEquals("Incorrect number of Features imported", result.size(), expected);
	}
	
	/**
	 * Helper method
	 * @param file
	 * @param checkCanUse
	 * @param canUseExpected
	 */
	public void pointsCorrect(File file, boolean checkCanUse, Boolean... canUseExpected) {
		MapGisGridAnnotationReader reader = new MapGisGridAnnotationReader();
		List<GisGridAnnotation> result = reader.fromGeoJson(file);

		int count = 0;
		for (GisGridAnnotation ann : result) {
			if (checkCanUse) {
				assertEquals("Polygon usability did not match expected", ann.canUse() , canUseExpected[count]);
			}

			for (LngLatAlt point : ann.getExteriorRing()) {
				Assert.assertNotNull("Geo points should not be imported as null!", point);
			}
			count++;
		}
	}

	/**
	 * helper method
	 * @param file
	 * @param expected
	 */
	public void pointSizeCorrect(File file, int... expected) {
		MapGisGridAnnotationReader reader = new MapGisGridAnnotationReader();
		List<GisGridAnnotation> result = reader.fromGeoJson(file);

		int count = 0;
		for (GisGridAnnotation ann : result) {
			int ringSize = ann.getExteriorRing().size();
			assertEquals("Polygon points were not correct", ringSize, expected[count]);
			count++;
		}
	}
	
	//\\ TESTING //\\
	
	@Test
	public void readSinglePolygonFile_true() {
		fileCorrect(testFileSinglePolygon, 1);
	}
	
	@Test
	public void readSinglePolygonFile_pointSizeCorrect() {
		pointSizeCorrect(testFileSinglePolygon, 6);
	}

	@Test
	public void readSinglePolygonFile_pointCorrect() {
		pointsCorrect(testFileSinglePolygon, false);
	}


	@Test
	public void readsMultiPolygonFile_true() {
		fileCorrect(testFileMultiPolygon, 3);
	}

	@Test
	public void readMultiPolygonFile_pointSizeCorrect() {
		pointSizeCorrect(testFileMultiPolygon, 6, 6, 5);
	}

	@Test
	public void readMultiPolygonFile_pointCorrect() {
		pointsCorrect(testFileMultiPolygon, false);
	}
	
	@Test
	public void readsExtrasPolygonFile_true() {
		fileCorrect(testFileExtras, 7);
	}
	
	@Test
	public void readExtrasPolygonFile_pointSizeCorrect() {
		pointSizeCorrect(testFileExtras, 6, 6, 5, 1, 1, 5, 5);
	}

	@Test
	public void readExtrasPolygonFile_pointCorrect() {
		pointsCorrect(testFileExtras, false);
	}
	
	@Test
	public void readExtrasPolygonFile_extrasExcluded() {
		pointsCorrect(testFileExtras, true, true, true, true, false, false, true, true);
	}

}
