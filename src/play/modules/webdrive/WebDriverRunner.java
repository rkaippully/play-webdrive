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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

public class WebDriverRunner {

    /**
	 * Default URL of the play app.
	 */
	private static final String DEFAULT_APP_URL = "http://localhost:9000";

	/**
	 * Default URL of the Selenium Remote; assuming a local hub but user could just
	 * point directly at a remote.
	 */
	private static final String DEFAULT_REMOTE_URL = "http://localhost:4444/wd/hub";

	/**
	 * Default timeout value for each test.
	 */
	private static final String DEFAULT_TEST_TIMEOUT = "120";

	public static void main(String[] args) throws Exception {
    	if (new WebDriverRunner().run())
    		System.exit(0);
    	else
    		System.exit(1);
    }

    /**
     * The "test-result" directory.
     */
    private File testResultRoot;

    /**
     * Set to true if any of the tests failed.
     */
    private boolean failed;

	/**
	 * URL of the play application to test
	 */
	private String appUrlBase;

	/**
	 * The maximum time we give for each test to complete.
	 */
	private int testTimeoutInSeconds;

	/**
	 * URL part for selenium test runner
	 */
	private String seleniumUrlPart;

	/**
	 * All selenium tests.
	 */
	private List<String> seleniumTests = new ArrayList<String>();

	/**
	 * All non-selenium tests
	 */
	private List<String> nonSeleniumTests = new ArrayList<String>();
	private int maxTestNameLength;

	public WebDriverRunner() {
		this.appUrlBase = System.getProperty("application.url", DEFAULT_APP_URL);
		String timeoutStr = System.getProperty("webdrive.timeout", DEFAULT_TEST_TIMEOUT);
		try {
			if (timeoutStr == null || timeoutStr.trim().equals(""))
				timeoutStr = DEFAULT_TEST_TIMEOUT;
			this.testTimeoutInSeconds = Integer.parseInt(timeoutStr);
			System.out.println("~ Using a timeout value of "
					+ this.testTimeoutInSeconds + " seconds");
		} catch (NumberFormatException e) {
			System.out.println("~ The timeout value " + timeoutStr
					+ " is not a " + "number. Setting to default value "
					+ DEFAULT_TEST_TIMEOUT + " seconds");
            this.testTimeoutInSeconds = Integer.parseInt(DEFAULT_TEST_TIMEOUT);
		}
		retrieveTestsList();
	}

    /**
	 * Retrieve the list of tests to run
	 */
	private void retrieveTestsList() {
        try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					new URL(appUrlBase + "/@tests.list").openStream(), "utf-8"));
            String marker = in.readLine();
            if (!marker.equals("---"))
                throw new RuntimeException("Error retrieving list of tests");

            this.testResultRoot = new File(in.readLine());
            this.seleniumUrlPart = in.readLine();
            String line;
            while ((line = in.readLine()) != null) {
                String test = line;
				String testName = test.replace(".class", "")
						.replace(".test.html", "").replace(".", "/")
						.replace("$", "/");
                if (testName.length() > maxTestNameLength) {
                    maxTestNameLength = testName.length();
                }
                if (test.contains(".test.html")) {
                	seleniumTests.add(test);
                } else {
                	nonSeleniumTests.add(test);
                }
            }
            in.close();

			System.out.println("~ " + seleniumTests.size() + " selenium test"
					+ (seleniumTests.size() != 1 ? "s" : "") + " to run:");
			System.out.println("~ " + nonSeleniumTests.size() + " other test"
					+ (nonSeleniumTests.size() != 1 ? "s" : "") + " to run:");
            System.out.println("~");
        } catch(Exception e) {
            System.out.println("~ The application does not start. There are errors: " + e);
            System.exit(-1);
        }
	}

	private boolean run() throws Exception {
	    DriverManager manager = new DriverManager();
        List<Class<?>> driverClasses = manager.getDriverClasses();

        /* Run non-selenium tests */
    	runTestsWithDriver(HtmlUnitDriver.class, nonSeleniumTests);

    	/* Run selenium tests on all browsers */
    	for (Class<?> driverClass : driverClasses) {
        	runTestsWithDriver(driverClass, seleniumTests);
        }

		File resultFile = new File(testResultRoot, "result."
				+ (failed ? "failed" : "passed"));
        resultFile.createNewFile();

        return !failed;
    }

	private void runTestsWithDriver(Class<?> webDriverClass, List<String> tests)
			throws Exception {
        System.out.println("~ Starting tests with " + webDriverClass);
        WebDriver webDriver;
        if (webDriverClass == RemoteWebDriver.class) {
            DesiredCapabilities capabilities = new DesiredCapabilities();
            capabilities.setBrowserName("internet explorer");
            capabilities.setJavascriptEnabled(true);
            webDriver = new RemoteWebDriver(new URL(System.getProperty("webdrive.remote", DEFAULT_REMOTE_URL)), capabilities);
        }
        else {
            webDriver = (WebDriver) webDriverClass.newInstance();
        }
        webDriver.get(appUrlBase + "/@tests/init");
        boolean ok = true;
        for (String test : tests) {
            long start = System.currentTimeMillis();
			String testName = test.replace(".class", "")
					.replace(".test.html", "").replace(".", "/")
					.replace("$", "/");
            System.out.print("~ " + testName + "... ");
            for (int i = 0; i < maxTestNameLength - testName.length(); i++) {
                System.out.print(" ");
            }
            System.out.print("    ");
            String url;
            if (test.endsWith(".class")) {
                url = appUrlBase + "/@tests/" + test;
            } else {
				url = appUrlBase + seleniumUrlPart + "?baseUrl="
						+ appUrlBase + "&test=/@tests/" + test
						+ ".suite&auto=true&resultsUrl=/@tests/" + test;
            }
            webDriver.get(url);
            int retry = 0;
            while(retry < testTimeoutInSeconds) {
				if (new File(testResultRoot, test.replace("/", ".")
						+ ".passed.html").exists()) {
                    System.out.print("PASSED      ");
                    break;
				} else if (new File(testResultRoot, test.replace("/", ".")
						+ ".failed.html").exists()) {
                    System.out.print("FAILED   !  ");
                    ok = false;
                    break;
                } else {
                	retry++;
                    if(retry == testTimeoutInSeconds) {
                        System.out.print("TIMEOUT  ?  ");
                        ok = false;
                        break;
                    } else {
                        Thread.sleep(1000);
                    }
                }
            }

            //
            int duration = (int) (System.currentTimeMillis() - start);
            int seconds = (duration / 1000) % 60;
            int minutes = (duration / (1000 * 60)) % 60;

            if (minutes > 0) {
                System.out.println(minutes + " min " + seconds + "s");
            } else {
                System.out.println(seconds + "s");
            }
        }
        webDriver.get(appUrlBase + "/@tests/end?result=" + (ok ? "passed" : "failed"));
        webDriver.quit();
        
        saveTestResults(webDriver.getClass().getSimpleName());
        if (!ok)
        	failed = true;
	}

	/**
	 * Play stores test results under {app.path}/test-result directory.
	 * We will move it under {app.path}/test-result/{webDriverName}.
	 */
	private void saveTestResults(String webDriverName) {
		File destDir = new File(testResultRoot, webDriverName);
		destDir.mkdir();

		for (File file : testResultRoot.listFiles()) {
			String fileName = file.getName();
			if (!"application.log".equals(fileName) && file.isFile()) {
				File newFile = new File(destDir, fileName);
				if (!file.renameTo(newFile)) {
					System.out.println("~ Could not create " + newFile);
				}
			}
		}
	}
}