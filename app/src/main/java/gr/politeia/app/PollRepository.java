package gr.politeia.app;
import android.content.*;
import gr.politeia.core.PollFeed;
import org.json.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
/** Atomic validated cache. Refreshing never touches a user's active scenario. */
public final class PollRepository {
 public static final long INTERVAL=6*60*60*1000L;
 private final Context context;
 public PollRepository(Context context){this.context=context.getApplicationContext();}
 private SharedPreferences prefs(){return context.getSharedPreferences("poll-cache",0);}
 public long checked(){return prefs().getLong("checked",0);}
 public String error(){return prefs().getString("error","");}
 public boolean enabled(){return prefs().getBoolean("auto",true);}
 public void setEnabled(boolean enabled){prefs().edit().putBoolean("auto",enabled).apply();PollUpdateService.schedule(context);}
 public List<JSONObject> all(){try{String saved=prefs().getString("polls","");JSONArray data;if(saved.isEmpty()){try(InputStream in=context.getAssets().open("polls.html")){data=convert(PollFeed.parse(read(in),LocalDate.now()));}}else data=new JSONArray(saved);List<JSONObject> out=new ArrayList<>();for(int i=0;i<data.length();i++)out.add(data.getJSONObject(i));out.sort((a,b)->b.optString("date").compareTo(a.optString("date")));return out;}catch(Exception e){throw new IllegalStateException("Poll cache unavailable",e);}}
 public synchronized boolean refresh(){try{
  HttpURLConnection connection=(HttpURLConnection)new URL(PollFeed.URL).openConnection();connection.setConnectTimeout(15000);connection.setReadTimeout(15000);connection.setRequestProperty("User-Agent","Politeia/1.1 (Android election calculator)");
  String html;try{if(connection.getResponseCode()!=200)throw new IOException("HTTP "+connection.getResponseCode());if(!connection.getURL().getProtocol().equals("https"))throw new IOException("HTTPS required");try(InputStream in=connection.getInputStream()){html=read(in);}}finally{connection.disconnect();}
  JSONArray fresh=convert(PollFeed.parse(html,LocalDate.now()));Map<String,JSONObject> merged=new LinkedHashMap<>();for(JSONObject poll:all())merged.put(poll.optString("source"),poll);for(int i=0;i<fresh.length();i++){JSONObject poll=fresh.getJSONObject(i);merged.put(poll.getString("source"),poll);}
  List<JSONObject> sorted=new ArrayList<>(merged.values());sorted.sort((a,b)->b.optString("date").compareTo(a.optString("date")));if(sorted.size()>200)sorted=new ArrayList<>(sorted.subList(0,200));
  return prefs().edit().putString("polls",new JSONArray(sorted).toString()).putLong("checked",System.currentTimeMillis()).putString("error","").commit();
 }catch(Exception e){prefs().edit().putString("error","REFRESH_FAILED").apply();return false;}}
 private static String read(InputStream in)throws IOException{ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1){if(out.size()+n>2_000_000)throw new IOException("Feed too large");out.write(b,0,n);}return out.toString(StandardCharsets.UTF_8.name());}
 private static JSONArray convert(List<PollFeed.Poll> polls)throws JSONException{JSONArray array=new JSONArray();for(PollFeed.Poll p:polls){JSONObject item=new JSONObject().put("source",p.source).put("date",p.date).put("firm",p.firm).put("other",p.other.toPlainString());JSONArray parties=new JSONArray();for(PollFeed.Party party:p.parties)parties.put(new JSONObject().put("id",party.id).put("en",party.en).put("el",party.el).put("color",party.color).put("percentage",party.percent.toPlainString()).put("members",1));item.put("parties",parties);if(p.firm.equals("GPO")&&p.date.equals("2026-09-08"))item.put("primary","https://www.star.gr/eidiseis/politiki/727489/gpo-h-dhmoskophsh-gia-to-star-gia-prothesh-pshfoy-kai-deth");array.put(item);}return array;}
}
