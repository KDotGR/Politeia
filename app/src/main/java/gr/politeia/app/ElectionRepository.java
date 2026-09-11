package gr.politeia.app;
import android.content.Context;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Immutable bundled catalog; replace via a versioned release after source validation. */
public final class ElectionRepository {
 public final List<JSONObject> elections=new ArrayList<>();
 public ElectionRepository(Context context){
  try(InputStream in=context.getAssets().open("elections.json")){
   ByteArrayOutputStream bytes=new ByteArrayOutputStream();byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)bytes.write(b,0,n);
   JSONArray array=new JSONArray(bytes.toString(StandardCharsets.UTF_8.name()));for(int i=0;i<array.length();i++)elections.add(array.getJSONObject(i));
  }catch(Exception e){throw new IllegalStateException("Bundled election data unavailable",e);}
 }
 public JSONObject get(String id){for(JSONObject e:elections)if(e.optString("id").equals(id))return e;return elections.get(0);}
 public List<JSONObject> ofType(int type){List<JSONObject> list=new ArrayList<>();for(JSONObject e:elections)if(type==0&&(e.optString("rule").equals("PARLIAMENT")||e.optString("rule").equals("SIMPLE"))||type==1&&e.optString("rule").equals("EU")||type==2&&e.optString("rule").equals("LOCAL"))list.add(e);return list;}
}
