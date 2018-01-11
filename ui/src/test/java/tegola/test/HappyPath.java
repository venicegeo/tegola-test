package tegola.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;

import tegola.page.TegolaDemoPage;
import tegola.util.Utils;

public class HappyPath {
	private WebDriver driver;
	private WebDriverWait wait;
	private Actions actions;
	private TegolaDemoPage main;

	// Strings used:
	private String baseUrl = System.getenv("baseUrl");
	private String browserPath = System.getenv("browserPath");
	private String driverPath = System.getenv("driverPath");
	private String profilePath = System.getenv("profilePath");

	// Lists to Check:
	HashMap<String, String> sources = new HashMap<String, String>() {
		{
			// Value in list, value in url
			put("Mapbox", "(.*)lib=mapbox(.*)");
			put("Open Layers", "(.*)lib=ol(.*)");
		}
	};
	HashMap<String, String> cities = new HashMap<String, String>() {
		{
			// Value in list, value in url
			put("San Diego", "(.*)lat=3[1-3].*lng=-11[6-8](.*)");
			put("Los Angeles", "(.*)lat=3[3-5].*lng=-11[7-9](.*)");
			put("San Francisco", "(.*)lat=3[6-8].*lng=-12[1-3](.*)");
			put("New York", "(.*)lat=(39|4[0-1]).*lng=-7[3-5](.*)");
			put("Washington, DC", "(.*)lat=3[7-9].*lng=-7[6-8](.*)");
			put("Boston", "(.*)lat=4[1-3].*lng=-7[0-2](.*)");
			put("Berlin", "(.*)lat=5[1-3].*lng=1[2-4](.*)");
			put("Tokyo", "(.*)lat=3[4-6].*lng=(13[8-9]|140)(.*)");
		}
	};
	
	@Before
	public void setUp() throws Exception {
		// Setup Browser:
		driver = Utils.createWebDriver(browserPath, driverPath, profilePath);
		wait = new WebDriverWait(driver, 2);
		actions = new Actions(driver);
		main = new TegolaDemoPage(driver, wait);

		// Navigate to Admin Console:
		driver.get(baseUrl);
		driver.manage().window().maximize();
		Thread.sleep(5000);
	}

	@After
	public void tearDown() throws Exception {
		driver.close();
	}

	@Test
	public void changeSource() throws Exception {
		for (String sourceKey : sources.keySet()) {
			main.selectSource(sourceKey);
			Thread.sleep(5000);
			Utils.assertURLMatches(sources.get(sourceKey), driver);
		}
	}

	@Test
	public void changeCity() throws Exception {
		for (String cityKey : cities.keySet()) {
			main.selectCity(cityKey);
			Thread.sleep(5000);
			Utils.assertURLMatches(cities.get(cityKey), driver);
		}
	}

}
