package gr.politeia.app;

import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.ParcelFileDescriptor;
import android.view.View;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.InputStream;
import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ThemeSettingsTest {
 private static final String PREFS="gr.politeia.app.MainActivity";
 @Rule public ActivityTestRule<MainActivity> activity=new ActivityTestRule<MainActivity>(MainActivity.class){
  @Override protected void beforeActivityLaunched(){
   Context c=InstrumentationRegistry.getInstrumentation().getTargetContext();
   c.getSharedPreferences(PREFS,0).edit().clear().putBoolean("greek",false).putBoolean("sounds",false).putBoolean("walkthroughSeen",true).commit();
   c.getSharedPreferences("poll-cache",0).edit().clear().putBoolean("auto",false).commit();
  }
 };
 private void tap(String tag){if(tag.equals("settings"))onView(withTagValue(is((Object)tag))).perform(click());else onView(withTagValue(is((Object)tag))).perform(scrollTo(),click());}
 private void theme(String mode){tap("settings");tap("theme-"+mode);}
 private void assertTheme(boolean dark){onView(withTagValue(is((Object)"app-root"))).check((view,error)->{
  if(error!=null)throw error;
  assertEquals(dark?0xFF101820:0xFFF5F7FA,((ColorDrawable)view.getBackground()).getColor());
  assertEquals(dark?Configuration.UI_MODE_NIGHT_YES:Configuration.UI_MODE_NIGHT_NO,view.getResources().getConfiguration().uiMode&Configuration.UI_MODE_NIGHT_MASK);
 });}
 private void systemNight(boolean dark)throws Exception{
  try(ParcelFileDescriptor fd=InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand("cmd uimode night "+(dark?"yes":"no"));InputStream in=new ParcelFileDescriptor.AutoCloseInputStream(fd)){byte[] b=new byte[1024];while(in.read(b)!=-1){}}
  // Device-wide uiMode changes trigger asynchronous activity recreation and window transitions.
  android.os.SystemClock.sleep(1000);
  InstrumentationRegistry.getInstrumentation().waitForIdleSync();
 }
 @Test public void systemIsDefaultAndExplicitChoicesOverrideIt()throws Exception{
  UiModeManager manager=(UiModeManager)InstrumentationRegistry.getInstrumentation().getTargetContext().getSystemService(Context.UI_MODE_SERVICE);
  int original=manager.getNightMode();
  try{
   tap("settings");onView(withTagValue(is((Object)"theme-system"))).check(matches(isChecked()));onView(withText("Close")).perform(click());
   systemNight(true);assertTheme(true);theme("light");assertTheme(false);
   systemNight(false);assertTheme(false);theme("dark");assertTheme(true);
   systemNight(true);assertTheme(true);theme("system");assertTheme(true);systemNight(false);assertTheme(false);
  }finally{
   String mode=original==UiModeManager.MODE_NIGHT_YES?"yes":original==UiModeManager.MODE_NIGHT_NO?"no":"auto";
   try(ParcelFileDescriptor fd=InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand("cmd uimode night "+mode);InputStream in=new ParcelFileDescriptor.AutoCloseInputStream(fd)){while(in.read()!=-1){}}
  }
 }
 @Test public void themePersistsAndPreservesEditedVotes(){
  tap("type-1");tap("next-step");tap("source-election");tap("dataset");onData(anything()).atPosition(0).perform(click());tap("next-step");tap("next-step");
  onView(withTagValue(is((Object)"value-2"))).perform(scrollTo(),replaceText("20"),androidx.test.espresso.action.ViewActions.closeSoftKeyboard());theme("dark");assertTheme(true);
  onView(withTagValue(is((Object)"value-2"))).perform(scrollTo()).check(matches(withText("20")));
  onView(withTagValue(is((Object)"app-root"))).check((v,e)->{if(e!=null)throw e;((MainActivity)v.getContext()).recreate();});
  assertTheme(true);tap("settings");onView(withTagValue(is((Object)"theme-dark"))).check(matches(isChecked()));onView(withText("Close")).perform(click());
  onView(withTagValue(is((Object)"value-2"))).perform(scrollTo()).check(matches(withText("20")));
 }
 @Test public void darkResultsImageAndGreekThemeOptions()throws Exception{
  theme("dark");tap("type-1");tap("next-step");tap("source-election");tap("dataset");onData(anything()).atPosition(0).perform(click());tap("next-step");tap("next-step");tap("calculate");
  onView(withTagValue(is((Object)"share-results"))).check((view,error)->{if(error!=null)throw error;Bitmap image=ResultsShare.capture(view);assertEquals(0xFF101820,image.getPixel(0,0));image.recycle();});
  tap("settings");tap("language");onView(withText("Ελληνικά")).perform(click());
  // Wait for the language dialog to release its window before opening Settings again.
  android.os.SystemClock.sleep(500);tap("settings");
  onView(withTagValue(is((Object)"theme-light"))).check(matches(withText("Φωτεινό")));
  onView(withTagValue(is((Object)"theme-dark"))).check(matches(withText("Σκοτεινό")));
  onView(withTagValue(is((Object)"theme-system"))).check(matches(withText("Σύστημα")));
  Bitmap screenshot=InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();
  java.io.File folder=new java.io.File(InstrumentationRegistry.getArguments().getString("additionalTestOutputDir",activity.getActivity().getCacheDir().getPath()));folder.mkdirs();
  try(java.io.FileOutputStream out=new java.io.FileOutputStream(new java.io.File(folder,"dark-settings-greek.png"))){screenshot.compress(Bitmap.CompressFormat.PNG,100,out);}screenshot.recycle();
 }
}
