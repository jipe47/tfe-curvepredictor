package be.tfe.android.misc.utils;

public class AppConfig {
	public static String LOCAL_SERVER_IP 		= "192.168.1.4:8080"; // Itstudents, not installed yet
	public static String LOCAL_PATH_MANAGER 	= "/tfe/";
	
	public static String SERVER_IP, PATH_MANAGER;
	
	private enum ServerType
	{
		ONLINE, LOCAL
	};

	public static ServerType SERVER_TYPE 	= ServerType.ONLINE;
	
	public static String[] SERVERS = {"139.165.144.110", "www.itstudents.be"};
	public static String[] PATHS = {"/~jipe/curvemanager/", "/~jipe/tfe/"};
	
    public static final String PREFS_NAME = "MyPrefs2";
	public static final boolean DEBUG = false;
	public static boolean FORCE_SERVERUNAVAILABLE = false;
	public static int PING_TIMEOUT = 4000;
	public static int NBR_FAIL = 0;
	public static final float ALPHA = 0.6f;
}
