/*
  WebDrive - Selenium 2 WebDriver support for play framework

  Copyright (C) 2011 Raghu Kaippully 

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package play.modules.webdrive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.android.AndroidDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.iphone.IPhoneDriver;

/**
 * Manages {@link WebDriver} instances to use for testing.
 */
public class DriverManager {

	/* Drivers can be specified with their simple names - ie, firefox, etc */
	private static final Map<String, Class<? extends WebDriver>> simpleDriverNames =
		new HashMap<String, Class<? extends WebDriver>>();
	static {
		simpleDriverNames.put("htmlunit", HtmlUnitDriver.class);
		simpleDriverNames.put("android", AndroidDriver.class);
		simpleDriverNames.put("chrome", ChromeDriver.class);
		simpleDriverNames.put("firefox", FirefoxDriver.class);
		simpleDriverNames.put("ie", InternetExplorerDriver.class);
		simpleDriverNames.put("iphone", IPhoneDriver.class);
	}
	
	public List<Driver> getRemoteDriverNames() {
		List<Driver> drivers = new ArrayList<Driver>();
		String driversProp = System.getProperty("webdrive.remote.browsers");
		if (driversProp == null || driversProp.trim().isEmpty()) {
			return drivers;
		}
		
		for (String rawDriver : driversProp.split(",")) {
			drivers.add(new Driver(rawDriver.split(":")));
		}
		return drivers;
	}

	/**
	 * Returns the list of all {@link WebDriver} classes to run tests.
	 */
	public List<Class<?>> getLocalDriverClasses() {
		List<Class<?>> drivers = new ArrayList<Class<?>>();
		String driversProp = System.getProperty("webdrive.classes");
		if (driversProp == null || driversProp.trim().isEmpty()) {
			return drivers;
		}

		for (String driver : driversProp.split(",")) {
			Class<?> clazz;
			try {
				clazz = Class.forName(driver);
			} catch (ClassNotFoundException e) {
				clazz = simpleDriverNames.get(driver);
			}

			if (clazz == null || !WebDriver.class.isAssignableFrom(clazz)) {
				System.out.println("~ " + driver +
					" is not a valid WebDriver implementation.");
				continue;
			}

			/* Skip IE if we are not on windows */
			if (InternetExplorerDriver.class.equals(clazz) &&
				!System.getProperty("os.name").startsWith("Windows")) {
				System.out.println("~ Cannot test with IE on " +
					System.getProperty("os.name"));
				continue;
			}

			drivers.add(clazz);
		}
		return drivers;
	}
}
