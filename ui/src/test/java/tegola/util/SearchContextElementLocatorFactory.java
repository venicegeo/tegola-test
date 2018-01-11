package tegola.util;

import java.lang.reflect.Field;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.support.pagefactory.DefaultElementLocator;
import org.openqa.selenium.support.pagefactory.ElementLocator;
import org.openqa.selenium.support.pagefactory.ElementLocatorFactory;

public class SearchContextElementLocatorFactory
implements ElementLocatorFactory {

	private final SearchContext context;

	public SearchContextElementLocatorFactory(SearchContext context) {
		this.context = context;
	}

	@Override
	public ElementLocator createLocator(Field field) {
		return new DefaultElementLocator(context, field);
	}
}
