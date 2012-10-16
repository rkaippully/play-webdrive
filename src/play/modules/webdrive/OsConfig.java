package play.modules.webdrive;

public enum OsConfig {
    LINUX("Linux", "/usr/bin/google-chrome"), MAC("Mac",
	    "/Applications/Google\\ Chrome.app/Contents/MacOS/Google\\ Chrome"), WINDOWS_XP(
	    "Windows XP",
	    "%HOMEPATH%\\Local Settings\\Application Data\\Google\\Chrome\\Application\\chromedriver.exe"), WINDOWS_VISTA(
	    "Windows Vista",
	    "C:\\Users\\%USERNAME%\\AppData\\Local\\Google\\Chrome\\Application\\chromedriver.exe"), WINDOWS_SEVEN(
	    "Windows 7",
	    "C:\\Users\\%USERNAME%\\AppData\\Local\\Google\\Chrome\\Application\\chromedriver.exe");

    private String osSystemName;

    private String chromeDriverDefaultPath = "";

    OsConfig(String osSystemName, String chromeDriverDefaultPath) {
	this.osSystemName = osSystemName;
	this.chromeDriverDefaultPath = chromeDriverDefaultPath;
    }

    public String getChromeDriverDefaultPath() {
	String result = this.chromeDriverDefaultPath;
	if (result.contains("%USERNAME%")) {
	    String userName = System.getProperty("user.name");
	    result = result.replaceAll("%USERNAME%", userName);
	}

	if (result.contains("%HOMEPATH%")) {
	    String userHome = System.getProperty("user.home");
	    result = result.replaceAll("%HOMEPATH%", userHome);
	}

	return result;
    }

    public static OsConfig getValueFromName(String osName) {
	if (osName == null) {
	    return null;
	}

	// look for exact value
	for (OsConfig os : OsConfig.values()) {
	    if (osName.equals(os.osSystemName)) {
		return os;
	    }
	}

	// look for exact value
	for (OsConfig os : OsConfig.values()) {
	    if (osName.startsWith(os.osSystemName)) {
		return os;
	    }
	}
	return null;
    }
}
