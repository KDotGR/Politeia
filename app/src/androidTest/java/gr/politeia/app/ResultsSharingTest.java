package gr.politeia.app;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.View;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ResultsSharingTest {
 @Rule public ActivityTestRule<MainActivity> activity=new ActivityTestRule<MainActivity>(MainActivity.class){
  @Override protected void beforeActivityLaunched(){
   Context c=InstrumentationRegistry.getInstrumentation().getTargetContext();
   c.getSharedPreferences("gr.politeia.app.MainActivity",0).edit().clear().putBoolean("greek",false).putBoolean("sounds",false).commit();
   c.getSharedPreferences("poll-cache",0).edit().clear().putBoolean("auto",false).commit();
  }
 };
 private void tap(String tag){onView(withTagValue(is((Object)tag))).perform(scrollTo(),click());}
 @Test public void shareExportsEntireResultsAsReadablePng() throws Exception {
  Instrumentation instrumentation=InstrumentationRegistry.getInstrumentation();
  CountDownLatch shared=new CountDownLatch(1);Intent[] captured={null};
  Instrumentation.ActivityMonitor monitor=new Instrumentation.ActivityMonitor(){
   @Override public Instrumentation.ActivityResult onStartActivity(Intent intent){
    if(Intent.ACTION_CHOOSER.equals(intent.getAction())){captured[0]=intent;shared.countDown();return new Instrumentation.ActivityResult(Activity.RESULT_CANCELED,null);}return null;
   }
  };
  instrumentation.addMonitor(monitor);
  try {
   tap("type-1");tap("next-step");tap("source-election");tap("dataset");onData(anything()).atPosition(0).perform(click());tap("next-step");tap("next-step");tap("calculate");
   int[] size=new int[2];instrumentation.runOnMainSync(()->{View results=activity.getActivity().findViewById(android.R.id.content).findViewWithTag("share-results");size[0]=results.getWidth();size[1]=results.getHeight();});
   tap("share");assertTrue("Android share sheet was launched",shared.await(10,TimeUnit.SECONDS));
   Intent send=captured[0].getParcelableExtra(Intent.EXTRA_INTENT);
   assertEquals(Intent.ACTION_SEND,send.getAction());assertEquals("image/png",send.getType());assertFalse(send.hasExtra(Intent.EXTRA_TEXT));
   assertTrue((send.getFlags()&Intent.FLAG_GRANT_READ_URI_PERMISSION)!=0);
   Uri uri=send.getParcelableExtra(Intent.EXTRA_STREAM);assertEquals("content",uri.getScheme());assertEquals(uri,send.getClipData().getItemAt(0).getUri());
   try(InputStream in=activity.getActivity().getContentResolver().openInputStream(uri)){
    Bitmap image=BitmapFactory.decodeStream(in);assertNotNull(image);
    assertTrue("Image includes results below the visible screen",image.getHeight()>activity.getActivity().getResources().getDisplayMetrics().heightPixels);
    assertEquals((double)size[0]/size[1],(double)image.getWidth()/image.getHeight(),0.01);image.recycle();
   }
  } finally {instrumentation.removeMonitor(monitor);}
 }
 @Test public void tallResultsAreScaledWithoutCropping(){
  InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{
   View content=new View(activity.getActivity());content.setBackgroundColor(android.graphics.Color.RED);content.layout(0,0,1080,30000);
   Bitmap image=ResultsShare.capture(content);
   assertTrue((long)image.getWidth()*image.getHeight()<=8_000_000);assertTrue(image.getHeight()<=16384);
   assertEquals(1080.0/30000,(double)image.getWidth()/image.getHeight(),0.001);
   assertEquals(android.graphics.Color.RED,image.getPixel(image.getWidth()-1,image.getHeight()-1));image.recycle();
  });
 }
}
