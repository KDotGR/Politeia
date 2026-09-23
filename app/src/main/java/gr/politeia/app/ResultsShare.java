package gr.politeia.app;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.view.View;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/** Captures the full laid-out results, independent of the ScrollView's visible viewport. */
final class ResultsShare {
 private ResultsShare() {}
 static Bitmap capture(View results){
  int width=results.getWidth(),height=results.getHeight();
  if(width<=0||height<=0)throw new IllegalStateException("Results are not laid out");
  // Bound bitmap memory and dimensions for unusually long custom elections; never crop parties.
  double scale=Math.min(1,Math.min(Math.sqrt(8_000_000.0/((double)width*height)),16384.0/Math.max(width,height)));
  Bitmap bitmap=Bitmap.createBitmap(Math.max(1,(int)(width*scale)),Math.max(1,(int)(height*scale)),Bitmap.Config.ARGB_8888);
  Canvas canvas=new Canvas(bitmap);canvas.drawColor(results.getContext().getColor(R.color.app_background));
  canvas.scale((float)bitmap.getWidth()/width,(float)bitmap.getHeight()/height);results.draw(canvas);
  return bitmap;
 }
 // Compression and disk access run off the UI thread. Only this cache subfolder is shareable.
 static Intent createIntent(Context context,Bitmap bitmap)throws IOException {
  File folder=new File(context.getCacheDir(),"shared-results");
  if(!folder.isDirectory()&&!folder.mkdirs())throw new IOException("Cannot create image cache");
  File[] old=folder.listFiles();if(old!=null)for(File file:old)if(file.lastModified()<System.currentTimeMillis()-86400000L)file.delete();
  File image=File.createTempFile("politeia-results-",".png",folder);
  try(FileOutputStream out=new FileOutputStream(image)){
   if(!bitmap.compress(Bitmap.CompressFormat.PNG,100,out))throw new IOException("Cannot encode results");
  }catch(IOException failure){image.delete();throw failure;}
  Uri uri=FileProvider.getUriForFile(context,context.getPackageName()+".results",image);
  Intent send=new Intent(Intent.ACTION_SEND).setType("image/png").putExtra(Intent.EXTRA_STREAM,uri);
  send.setClipData(ClipData.newRawUri("Politeia results",uri));send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
  return send;
 }
}
