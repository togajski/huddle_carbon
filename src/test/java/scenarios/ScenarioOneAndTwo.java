package scenarios;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.response.ResponseOptions;

public class ScenarioOneAndTwo {
	ResponseOptions<Response> response;

	@BeforeTest
	public void getResponse() {
		RestAssured.baseURI = "https://api.carbonintensity.org.uk";
		response = RestAssured.given().when().get("/regional").then().extract().response();

		if (response.statusCode() != 200) {
			System.out.println("Request failed with status code: " + response.statusCode());
		}
	}

	@Test(priority = 1)
	public void getCarbonIntensity() {
		System.out.println("**** First Test ****\n");

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		try {
			ResponseClass responseClass = objectMapper.readValue(response.getBody().asString(), ResponseClass.class);
			List<Region> regions = responseClass.getData().get(0).getRegions();

			Collections.sort(regions, Comparator.comparingInt(Region::getIntensityForecast).reversed());

			FileOutputStream outputStream = new FileOutputStream("log.txt");
			for (Region region : regions) {
				String str = region.getIntensityForecast() + " " + region.getName() + "\n";
				byte[] strToBytes = str.getBytes();
				outputStream.write(strToBytes);
				System.out.println(str);
			}
			outputStream.close();

		} catch (Exception e) {
			System.out.println("Failed to deserialize the response: " + e.getMessage());
		}

	}

	@Test(priority = 2)
	public void genMix() {
		System.out.println("**** Second Test ****\n");
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		try {
			ResponseClass responseClass = objectMapper.readValue(response.getBody().asString(), ResponseClass.class);
			List<Region> regions = responseClass.getData().get(0).getRegions();

			for (Region region : regions) {
				List<GenerationMix> generationMixList = region.getGenerationMix();

				float sum = 0;
				for (GenerationMix mix : generationMixList) {
					sum += mix.getPerc();
				}

				assertEquals(sum, 100);
			}

		} catch (Exception e) {
			System.out.println("Failed to deserialize the response: " + e.getMessage());
		}

	}

	static class ResponseClass {
		private List<Data> data;

		public List<Data> getData() {
			return data;
		}

		public void setData(List<Data> data) {
			this.data = data;
		}
	}

	static class Data {
		private List<Region> regions;

		public List<Region> getRegions() {
			return regions;
		}

		public void setRegions(List<Region> regions) {
			this.regions = regions;
		}
	}

	static class Region {
		public String shortname;
		private Intensity intensity;
		public List<GenerationMix> generationmix;

		public String getName() {
			return shortname;
		}

		public void setName(String shortname) {
			this.shortname = shortname;
		}

		public Intensity getIntensity() {
			return intensity;
		}

		public void setIntensity(Intensity intensity) {
			this.intensity = intensity;
		}

		public int getIntensityForecast() {
			return intensity.getForecast();
		}

		public void setIntensityForecast(int intensityForecast) {
			this.intensity.setForecast(intensityForecast);
		}

		public List<GenerationMix> getGenerationMix() {
			return generationmix;
		}

		public void setGenerationMix(List<GenerationMix> generationmix) {
			this.generationmix = generationmix;
		}
	}

	static class Intensity {
		private int forecast;

		public int getForecast() {
			return forecast;
		}

		public void setForecast(int forecast) {
			this.forecast = forecast;
		}
	}

	static class GenerationMix {
		private float perc;

		public float getPerc() {
			return perc;
		}

		public void setPerc(float perc) {
			this.perc = perc;
		}
	}
}
