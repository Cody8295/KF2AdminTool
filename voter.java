import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.CookieManager;
import java.net.CookieHandler;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.util.List;

public class voter
{
    private static final String BASE_URL = "http://deliveryboys.game.nfoservers.com:8080/";
    private static final String SETTINGS = "ServerAdmin/settings/general";
    private static final String UA = "Mozilla/5.0";
    private static String loginParams = "token=*&password_hash=&username=admin&password=cody_test_pass&remember=-1";
    private static CookieManager cookieManager;
    private static String settings = "settings_bUsedForTakeover=0&settings_ServerName=Delivery+Boys+-+Long%2FSuicidal%2FHoE&settings_MaxIdleTime=0.0000&settings_MaxPlayers=6&settings_bAntiCheatProtected=1&settings_GameDifficulty=*.0000&settings_GameDifficulty_raw=0.000000&settings_GameLength=2&settings_bDisableTeamCollision=1&settings_bAdminCanPause=0&settings_bSilentAdminLogin=1&settings_bDisableMapVote=0&settings_MapVoteDuration=60.0000&settings_MapVotePercentage=0.0000&settings_bDisableKickVote=0&settings_KickVotePercentage=0.5100&settings_bDisablePublicTextChat=0&settings_bPartitionSpectators=0&settings_bDisableVOIP=0&liveAdjust=1&action=save";

    public static void main(String[] args) throws Exception
    {
	cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
	CookieHandler.setDefault(cookieManager);
	String csrfToken = getCSRFToken();
	if(login(csrfToken)){
	    System.out.println("Logged in...");
	}else{ System.out.println("Couldn't log in..."); return; }
	setDifficulty(3);
    }

    public static void setDifficulty(int d) throws Exception
    {
	String changeSettings = post(BASE_URL + SETTINGS, settings.replace("*", Integer.toString(d)));
	System.out.println(changeSettings);
    }

    public static Boolean login(String csrft) throws Exception
    {
	String loginTry = post(BASE_URL + "ServerAdmin/", loginParams.replace("*", csrft));
	String loginStep2 = get(BASE_URL + "ServerAdmin/current");
	String loginStep3 = get(BASE_URL + "ServerAdmin/current/info");
	//System.out.println(loginStep3);
	return !loginStep3.contains("login");
    }

    public static String getCSRFToken() throws Exception
    {
	String tokenSrc = get(BASE_URL);
	String[] tokens = tokenSrc.split("\"");
	for(int i=0;i<tokens.length;i++)
	{
	    if("token".equals(tokens[i])){ return tokens[i+2]; }
	}
	return "Not found!";
    }

    public static String post(String url, String params) throws Exception
    {
	URL uri = new URL(url);
	HttpURLConnection http = (HttpURLConnection)uri.openConnection();
	http.setRequestMethod("POST");
	http.setRequestProperty("User-Agent", UA);
	http.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
	http.setDoOutput(true);
	DataOutputStream ds = new DataOutputStream(http.getOutputStream());
	ds.writeBytes(params);
	ds.flush();
	ds.close();
	BufferedReader br = new BufferedReader(new InputStreamReader(http.getInputStream()));
	String line;
	StringBuffer rb = new StringBuffer();
	while((line=br.readLine())!=null)
	{
	    rb.append(line);
	}
	br.close();
	return rb.toString();
    }

    public static String get(String url) throws Exception
    {
	URL uri = new URL(url);
	HttpURLConnection http = (HttpURLConnection)uri.openConnection();
	http.setRequestMethod("GET");
	http.setRequestProperty("User-Agent", UA);
	BufferedReader br = new BufferedReader(new InputStreamReader(http.getInputStream()));
	String line;
	StringBuffer rb = new StringBuffer();
	while((line=br.readLine())!=null)
	{
	    rb.append(line);
	}
	br.close();
	return rb.toString();
    }
}
