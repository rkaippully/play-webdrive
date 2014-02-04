package play.modules.webdrive;

import org.openqa.selenium.Platform;

public class Driver {

	public static String ANY = "ANY";
	public String name;
	public String version;
	public Platform platform;
	
	public Driver(String[] driverParts) {
		switch (driverParts.length) {
		case 0:
			System.out.println("wtf?");
			break;
		case 1:
			this.name = driverParts[0];
			this.version = ANY;
			this.platform = Platform.ANY;
			break;
		case 2:
			this.name = driverParts[0];
			this.version = driverParts[1];
			this.platform = Platform.ANY;
			break;
		case 3:
			this.name = driverParts[0];
			this.version = driverParts[1];
			this.platform = Platform.valueOf(driverParts[2]);
			break;
		default:
			this.name = driverParts[0];
			this.version = driverParts[1];
			this.platform = Platform.valueOf(driverParts[2]);
			System.out.println("wtf? why haven't you updated this?");
			break;
		}
	}
	
	public Driver(String name) {
		this(name, ANY);
	}
	
	public Driver(String name, String version) {
		this(name, version, Platform.ANY);
	}
	
	public Driver(String name, String version, Platform platform) {
		this.name = name;
		this.version = version;
		this.platform = platform;
	}
	
}
