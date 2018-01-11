package tegola.page;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

public class TegolaDemoPage {
	WebDriver driver;
	Actions actions;
	WebDriverWait wait;

	@FindBy(id = "liboption")					public WebElement sourceButton;
	@FindBy(id = "locoption")					public WebElement locationButton;
	@FindBy(xpath = "//ul[@id='libbar']/li")	public List<WebElement> sourceList;
	@FindBy(xpath = "//ul[@id='locbar']/li")	public List<WebElement> locationList;
	
	
	public TegolaDemoPage(WebDriver driver, WebDriverWait wait) {
		PageFactory.initElements(driver, this);
		actions = new Actions(driver);
		this.driver = driver;
		this.wait = wait;
	}
	
	public void selectSource(String selection) throws Exception {
		sourceButton.click();
		matchingElement(sourceList, selection).click();
	}
	
	public void selectCity(String selection) throws Exception {
		locationButton.click();
		matchingElement(locationList, selection).click();
	}
	
	private WebElement matchingElement(List<WebElement> options, String searchString) throws Exception {
		for (WebElement element : options) {
			if (element.getText().contains(searchString)) {
				return element;
			}
		}
		throw new Exception(String.format("No element matching %s could be found.", searchString));
	}
}
