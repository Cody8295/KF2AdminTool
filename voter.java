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
import java.util.Map;
import java.util.HashMap;

public class voter
{
    private static final String BASE_URL = "http://deliveryboys.game.nfoservers.com:8080/";
    private static final String SETTINGS = "ServerAdmin/settings/general";
    private static final String UA = "Mozilla/5.0";
    private static String loginParams = "token=*&password_hash=&username=admin&password=cody_test_pass&remember=-1";
    private static CookieManager cookieManager;
    private static String settings = "settings_bUsedForTakeover=0&settings_ServerName=Delivery+Boys+-+Long%2FSuicidal%2FHoE&settings_MaxIdleTime=0.0000&settings_MaxPlayers=6&settings_bAntiCheatProtected=1&settings_GameDifficulty=*.0000&settings_GameDifficulty_raw=0.000000&settings_GameLength=2&settings_bDisableTeamCollision=1&settings_bAdminCanPause=0&settings_bSilentAdminLogin=1&settings_bDisableMapVote=0&settings_MapVoteDuration=60.0000&settings_MapVotePercentage=0.0000&settings_bDisableKickVote=0&settings_KickVotePercentage=0.5100&settings_bDisablePublicTextChat=0&settings_bPartitionSpectators=0&settings_bDisableVOIP=0&liveAdjust=1&action=save";
    private static Map<String,Integer> vote;

    public static void main(String[] args) throws Exception
    {
	vote = new HashMap<String,Integer>();
	cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
	CookieHandler.setDefault(cookieManager);
	String csrfToken = getCSRFToken();
	if(login(csrfToken)){
	    System.out.println("Logged in...");
	}else{ System.out.println("Couldn't log in..."); return; }
	System.out.println(playerCount() + " players online!");
	System.out.println("Starting voting poll");
    	updateVote();
    }

    public static int playerCount() throws Exception
    {
	String gameSummary = get(BASE_URL + "ServerAdmin/current+gamesummary");
	String[] tokens = gameSummary.split("\"");
	for(int i=0;i<tokens.length;i++)
	{
	    if("gs_players".equals(tokens[i])){ return Integer.valueOf(tokens[i+3].split("/")[0].replace(">","")); }
	}
	return -1; // couldn't find
    }

    public static void updateVote() throws Exception
    {
	//if(playerCount()<1){ updateVote(); } // dont vote
	String nextChat = null;
	while((nextChat=getChat()).equals(""))
	{
	    // do nothing until we get a chat msg
	}
	String[] tokens = nextChat.split("\"");
	String voteStr = null;
	String user = null;
	for(int i=0;i<tokens.length;i++)
	{
	    if("message".equals(tokens[i])) {
		voteStr=tokens[i+1].replace(">","").replace("</span</div","");
	    }
	    if(tokens[i].contains("username")) {
		user=tokens[i+1].replace(">","").split("<")[0];
	    }
	}
	System.out.println(user + ": " + voteStr);
	if(voteStr.startsWith("!diff"))
	{
	    if(!voteStr.contains(" "))
	    {
		sendChat(user + " doesn't know how to vote! Point and laugh!");
	    }else{
		Integer diff = Integer.valueOf(voteStr.split(" ")[1]);
		vote.put(user,diff);
		sendChat(user +  " voted for difficulty " + Integer.toString(diff));
	    }
	}
	Integer winner = -1;
	if((winner=tallyVote())!=-1)
	{
	    setDifficulty(winner);
	}
	updateVote();
    }

    public static Integer tallyVote()
    {
	Map<Integer, Integer> tallies  = new HashMap<Integer, Integer>();
	for(Map.Entry<String,Integer> e : vote.entrySet())
	{
	    if(tallies.containsKey(e.getValue()))
	    {
		tallies.put(e.getValue(), tallies.get(e.getValue()));
	    }else{
		tallies.put(e.getValue(), 1);
	    }
	}
	Integer winningDiff = -1;
	Integer mostVotes = 0;
	for(Map.Entry<Integer,Integer> e : tallies.entrySet())
	{
	    if(e.getValue()>mostVotes)
	    {
		winningDiff = e.getKey();
		mostVotes = e.getValue();
	    }
	}
	return winningDiff;
    }

    public static String getChat() throws Exception
    {
	String chatStr = post(BASE_URL + "ServerAdmin/current/chat+frame+data", "ajax=1");
	if(chatStr==null || chatStr.isEmpty()){ return ""; }
	return chatStr;
    }

    public static void sendChat(String msg) throws Exception
    {
	post(BASE_URL + "ServerAdmin/current/chat+frame+data", "ajax=1&message=" + msg + "&teamsay=-1");
    }

    public static void setDifficulty(int d) throws Exception
    {
	String changeSettings = post(BASE_URL + SETTINGS, settings.replace("*", Integer.toString(d)));
	//System.out.println(changeSettings);
	sendChat("Difficulty changed to " + Integer.toString(d));
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
