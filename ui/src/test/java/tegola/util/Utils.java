package tegola.util;

import static org.junit.Assert.*;

import java.awt.Robot;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Assume;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Utils {
	
	public static void scrollToElement(WebElement element, Actions actions) {
		actions.moveToElement(element).build().perform();
	}

	// Check that an element is present on the page,
	// without throwing an exception if it is not present.
	public static boolean isElementPresent(WebDriver driver, By by) {
		try {
			driver.findElement(by);
			return true;
		}
		catch (NoSuchElementException e) {
			return false;
		}
	}

	// Wait for an element to exist, failing if it does not.
	private static WebElement assertElementLoads_GENERIC(String msg, Object o, WebDriverWait wait, By by) {
		try {
			wait.until(ExpectedConditions.presenceOfElementLocated(by));
		} catch (TimeoutException e) {
			throw new AssertionError(msg, e);
		}
		if (o instanceof WebDriver) {
			return ((WebDriver) o).findElement(by);
		} else if (o instanceof WebElement) {
			return ((WebElement) o).findElement(by);
		} else {
			return null;
		}
	}
	public static WebElement assertElementLoads(String msg, WebElement element, WebDriverWait wait, By by) {
		return assertElementLoads_GENERIC(msg, element, wait, by);
	}
	public static WebElement assertElementLoads(String msg, WebDriver driver, WebDriverWait wait, By by) {
		return assertElementLoads_GENERIC(msg, driver, wait, by);
	}
	
	// Wait for an element to become visible, failing if it does not.
	public static void assertBecomesVisible(String msg, WebElement element, WebDriverWait wait) {
		try {
			waitUntilVisible(element, wait);
		} catch (TimeoutException e) {
			throw new AssertionError(msg, e);
		}
	}
	public static void assertBecomesVisible(WebElement element, WebDriverWait wait) {
		assertBecomesVisible("", element, wait);
	}
	
	public static void waitUntilVisible(WebElement element, WebDriverWait wait) {
		wait.until(ExpectedConditions.and(
			ExpectedConditions.visibilityOf(element),
			ExpectedConditions.not(ExpectedConditions.stalenessOf(element))
			));
	}
	
	// Wait for an element to become invisible (or disappear), failing if it still exists.
	public static void assertBecomesInvisible(String msg, WebElement element, WebDriverWait wait) {
		try {
			wait.until(
					ExpectedConditions.or(
							ExpectedConditions.not(ExpectedConditions.visibilityOf(element)), 
							ExpectedConditions.stalenessOf(element)
					)
			);
		} catch (TimeoutException e) {
			throw new AssertionError(msg, e);
		}
	}
	
	// Wait until (something), failing if it does not happen.
	public static void assertThatAfterWait(String msg, ExpectedCondition<?> expected, WebDriverWait wait) {
		try {
			wait.until(expected);
		} catch (TimeoutException e) {
			throw new AssertionError(msg, e);
		}
	}
	
	// wait until the element does not exist, failing if it is still there.
	public static void assertNotFound(String msg, WebElement element, WebDriverWait wait) {
		try {
			waitUntilNotFound(element, wait);
		} catch (TimeoutException e) {
			throw new AssertionError(msg, e);
		}
	}
	
	public static void waitUntilNotFound(WebElement element, WebDriverWait wait) {
		wait.until((WebDriver test) -> checkNotExists(element));
	}
	
	// Try to prove an element exists with .getText(), returning false if it fails.
	public static boolean checkExists(WebElement element) {
		try {
			element.getText();
			return true;
		} catch (NoSuchElementException | StaleElementReferenceException e) {
			return false;
		}
	}
	
	// Try to prove an element does not exist with .getText(), returning true if it fails.
	public static boolean checkNotExists(WebElement element) {
		return !checkExists(element);
	}

	// Send keys to the active element.
	public static void typeToFocus(WebDriver driver, CharSequence k) {
		getFocusedField(driver).sendKeys(k);
	}
	
	// Get the active element.
	public static WebElement getFocusedField(WebDriver driver) {
		return driver.switchTo().activeElement();
	}
	
	// Check that both coordinates of a point are within range of another point.
	public static void assertPointInRange(Point2D.Double actual, Point2D.Double target, double range) {
		assertPointInRange("", actual, target, range);
	}
	public static void assertPointInRange(String msg, Point2D.Double actual, Point2D.Double target, double range) {
		assertLonInRange(msg, actual.x, target.x, range);
		assertLatInRange(msg, actual.y, target.y, range);
	}

	// Check that a latitude is within range.
	public static void assertLatInRange(double actual, double target, double range) {
		assertLatInRange("", actual, target, range);
	}
	public static void assertLatInRange(String msg, double actual, double target, double range) {
		assertTrue("Latitude should be within [-90,90]", Math.abs(actual) <= 90);
		if (msg.isEmpty()) {
			msg = "Latitude should be within %f degrees of the target.  Expected <%f>, Actual <%f>";
		} else {
			msg += ": Latitude should be within %f degrees of the target.  Expected <%f>, Actual <%f>";
		}
		assertTrue(String.format(msg, range, target, actual), Math.abs(actual - target) < range);
	}

	// Check that a longitude is within a range, accounting for wrap-around.
	public static void assertLonInRange(double actual, double target, double range) {
		assertLonInRange("", actual, target, range);
	}
	public static void assertLonInRange(String msg, double actual, double target, double range) {
		assertTrue("Longitude should be within [-180,180]", Math.abs(actual) <= 180);
		if (msg.isEmpty()) {
			msg = "Longitude should be within %f degrees of the target.  Expected <%f>, Actual <%f>";
		} else {
			msg += ": Longitude should be within %f degrees of the target.  Expected <%f>, Actual <%f>";
		}
		assertTrue(String.format(msg, range, target, actual), Math.abs(actual - target) < range || 360 - Math.abs(actual - target) < range);
	}
	
	// Return a new WebDriver.  Follow a process based on a chrome or firefox driver.
	public static WebDriver createWebDriver(String browserPath, String driverPath, String profilePath) throws Exception {
		if (browserPath.contains("fox")) {
			System.setProperty("webdriver.gecko.driver", driverPath);
			FirefoxBinary binary =new FirefoxBinary(new File(browserPath));
			FirefoxProfile profile = new FirefoxProfile(new File(profilePath));
			FirefoxOptions options = new FirefoxOptions();
			options.setBinary(binary);
			options.setProfile(profile);
			return new FirefoxDriver(options);
		} else if (browserPath.contains("chrom")) {
			Logger logger = Logger.getLogger("");
			logger.setLevel(Level.OFF);
			ChromeOptions options = new ChromeOptions();
			options.addArguments("user-data-dir=" + profilePath);
			System.setProperty("webdriver.chrome.driver", driverPath);
			options.setCapability("chrome.verbose", false);
			options.setBinary(new File(browserPath));
			return new ChromeDriver(options);
		} else {
			throw new Exception("Could not identify browser from path: " + browserPath);
		}
	}
	
	// Try to click an element, returning true if it is successful, false if it throws an error.
	public static boolean tryToClick(WebElement element) {
		try {
			element.click();
			return true;
		} catch (WebDriverException e) {
			return false;
		}
	}

	// Move the mouse a back-and-forth a couple pixels.
	public static void jostleMouse(Actions actions, WebElement element) {
		actions.moveToElement(element, 500, 100).moveByOffset(1, 1).moveByOffset(-1, -1).build().perform();
	}
	public static void jostleMouse(Robot robot, WebElement element) {
		robot.mouseMove(element.getSize().width/2, element.getSize().height/2);
		robot.mouseMove(element.getSize().width/2 + 1, element.getSize().height/2 + 1);
	}
	
	// Send a GET request to the URL, and return the integer status code.
	public static int getStatusCode(String path) {
		try {
			URL url = new URL(path);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.connect();
			return connection.getResponseCode();
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}

	}
	
	// Get the web element in the second column in the row where the first column has string.
	public static WebElement getTableData(WebElement table, String name) {
		int i = 0;
		for (WebElement header : table.findElements(By.tagName("dt"))) {
			if (header.getText().equals(name)) {
				return table.findElements(By.tagName("dd")).get(i);
			}
			i++;
		}
		return null;
	}
	
	public static WebElement selectElementWithCaption(List<WebElement> elements, String caption) {
		return selectElementBySubelementText(elements, By.className("v-button-caption"), caption);
	}
	
	public static WebElement selectElementBySubelementText(List<WebElement> elements, By subelementSelector, String text) {
		for (WebElement element : elements) {
			if (text.equals(element.findElement(subelementSelector).getText())) {
				return element;
			}
		}
		throw (new NoSuchElementException("No elements with the caption, '" + text + "', found."));
	}
	
	//
	public static boolean checkElementBySubelementTextExists(List<WebElement> elements, By subelementSelector, String text) {
		try {
			selectElementBySubelementText(elements, subelementSelector, text);
			return true;
		} catch (NoSuchElementException|StaleElementReferenceException e) {
			return false;
		}
	}
	
	// Wait until the element cannot be found within a list, failing if it is still there.
	public static void assertInList(List<WebElement> elements, By subelementSelector, String text, WebDriverWait wait) {
		assertInList("'" + text + "' should be found in element list", elements, subelementSelector, text, wait);
	}
	public static void assertInList(String msg, List<WebElement> elements, By subelementSelector, String text, WebDriverWait wait) {
		try {
			wait.until((WebDriver test) -> checkElementBySubelementTextExists(elements, subelementSelector, text));
		} catch (TimeoutException e) {
			throw new AssertionError(msg, e);
		}
	}
	
	// Wait until the element cannot be found within a list, failing if it is still there.
	public static void assertNotInList(List<WebElement> elements, By subelementSelector, String text, WebDriverWait wait) {
		assertNotInList("'" + text + "' should not be found in element list", elements, subelementSelector, text, wait);
	}
	
	public static void assertNotInList(String msg, List<WebElement> elements, By subelementSelector, String text, WebDriverWait wait) {
		try {
			wait.until((WebDriver test) -> !checkElementBySubelementTextExists(elements, subelementSelector, text));
		} catch (TimeoutException e) {
			throw new AssertionError(msg, e);
		}
	}
	
	// Switch to a browser tab, throwing an exception if it does not exist:
	public static WebDriver switchToTab(int tabNo, WebDriver driver) {
		ArrayList<String> tabs = new ArrayList<String> (driver.getWindowHandles());
		System.out.println("CURRENT:" + driver.getWindowHandle());
		for (String tab : tabs) {
			System.out.println(tab);
		}
		if (driver.getWindowHandle() == tabs.get(tabNo)) {
			System.out.println("not switching, already in " + tabs.get(tabNo));
			return driver;
		} else {
			System.out.println("SWITCHED to " + tabs.get(tabNo));
			return driver.switchTo().window(tabs.get(tabNo));
		}
	}
	
	// assert that the given regex matches the current URL.  NOTE: this must be a FULL match.
	public static void assertURLMatches(String regexString, WebDriver driver) {
		String formatString = String.format("URL should match the regex `%s`.", regexString);
		boolean test = driver.getCurrentUrl().matches(regexString);
		assertTrue(formatString, test);
	}
}