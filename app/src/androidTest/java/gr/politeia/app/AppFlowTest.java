package gr.politeia.app;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.view.WindowManager;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.*;
import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class AppFlowTest {
 @Rule public ActivityTestRule<MainActivity> activity=new ActivityTestRule<MainActivity>(MainActivity.class){
  @Override protected void beforeActivityLaunched(){
   Context c=InstrumentationRegistry.getInstrumentation().getTargetContext();
   c.getSharedPreferences("MainActivity",0).edit().clear().putBoolean("greek",false).putBoolean("sounds",false).commit();
   c.getSharedPreferences("poll-cache",0).edit().clear().putBoolean("auto",false).commit();
   shell("input keyevent KEYCODE_WAKEUP");shell("wm dismiss-keyguard");
  }
  @Override protected void afterActivityLaunched(){
   InstrumentationRegistry.getInstrumentation().runOnMainSync(()->getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON));
  }
 };
 private static void shell(String command){try(ParcelFileDescriptor fd=InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(command);InputStream in=new ParcelFileDescriptor.AutoCloseInputStream(fd)){byte[] b=new byte[1024];while(in.read(b)!=-1){}}catch(Exception e){throw new RuntimeException(e);}}
 private static org.hamcrest.Matcher<View> tag(String s){return withTagValue(is((Object)s));}
 private void tap(String s){onView(tag(s)).perform(scrollTo(),click());}
 private void shot(String name)throws Exception{Bitmap b=InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();File dir=new File(InstrumentationRegistry.getInstrumentation().getTargetContext().getExternalFilesDir(null),"screenshots");dir.mkdirs();try(FileOutputStream out=new FileOutputStream(new File(dir,name+".png"))){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));}}
 @Test public void historicalCalculationAndExplanation() throws Exception {
  shot("01-english-home");tap("next-step");tap("example");tap("calculate");
  onView(tag("seat-total")).check(matches(withText(containsString("300"))));shot("02-parliament-results");
  onView(tag("seats-2")).perform(scrollTo()).check(matches(withText("158")));
  onView(tag("explanation")).perform(scrollTo()).check(matches(isDisplayed()));shot("03-calculation-trail");
 }
 @Test public void languageSwitch() throws Exception {
  onView(tag("language")).perform(click());onView(withText("Ελληνικά")).perform(click());shot("04-greek-home");
  onView(tag("next-step")).perform(scrollTo()).check(matches(withText("Επόμενο: εισαγωγή αποτελεσμάτων")));
  tap("next-step");
  tap("example");tap("calculate");onView(tag("explanation")).perform(scrollTo()).check(matches(withText("Πώς κατανεμήθηκαν οι έδρες")));shot("05-greek-explanation");
 }
 @Test public void invalidInputShowsError(){tap("next-step");tap("clear");tap("calculate");onView(tag("error")).check(matches(isDisplayed()));}
 @Test public void europeanHistoricalResults()throws Exception{tap("type-1");tap("next-step");tap("example");tap("calculate");onView(tag("seat-total")).check(matches(withText(containsString("21"))));shot("06-european-results");}
 @Test public void athensRunoffHistoricalResults()throws Exception{tap("type-2");tap("next-step");tap("example");tap("calculate");onView(tag("seat-total")).check(matches(withText(containsString("43"))));shot("07-athens-results");}
 @Test public void customPartyEntryAndBonus()throws Exception{
  tap("type-3");tap("next-step");
  tap("add");onView(withHint("English name")).perform(typeText("Alpha"),androidx.test.espresso.action.ViewActions.closeSoftKeyboard());onView(withText("Save")).perform(click());
  tap("add");onView(withHint("English name")).perform(typeText("Beta"),androidx.test.espresso.action.ViewActions.closeSoftKeyboard());onView(withText("Save")).perform(click());
  onView(withContentDescription("Alpha %")).perform(scrollTo(),replaceText("65"),androidx.test.espresso.action.ViewActions.closeSoftKeyboard());
  onView(withContentDescription("Beta %")).perform(scrollTo(),replaceText("35"),androidx.test.espresso.action.ViewActions.closeSoftKeyboard());tap("calculate");
  onView(tag("seat-total")).check(matches(withText(containsString("100"))));shot("08-custom-results");
  onView(tag("explanation")).perform(scrollTo()).check(matches(isDisplayed()));
 }
 @Test public void valuesSurviveRecreation(){tap("next-step");tap("example");activity.getActivity().runOnUiThread(()->activity.getActivity().recreate());InstrumentationRegistry.getInstrumentation().waitForIdleSync();tap("calculate");onView(tag("seat-total")).check(matches(withText(containsString("300"))));}
 @Test public void landscapeLayout()throws Exception{
  activity.getActivity().runOnUiThread(()->activity.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE));InstrumentationRegistry.getInstrumentation().waitForIdleSync();
  onView(tag("type-0")).perform(scrollTo()).check(matches(isDisplayed()));shot("09-landscape");tap("next-step");tap("example");tap("calculate");onView(tag("seat-total")).check(matches(withText(containsString("300"))));
 }
 @Test public void soundToggleAndSavedScenario(){
  onView(tag("sound")).perform(click()).check(matches(withContentDescription("Sound on")));onView(tag("sound")).perform(click()).check(matches(withContentDescription("Sound off")));
  tap("next-step");tap("example");tap("save");onView(withHint("Scenario name")).perform(typeText("Regression scenario"),androidx.test.espresso.action.ViewActions.closeSoftKeyboard());onView(withText("Save")).perform(click());tap("clear");tap("open");onView(withText("Regression scenario")).perform(click());tap("calculate");onView(tag("seat-total")).check(matches(withText(containsString("300"))));
 }
 @Test public void dropdownRemainderAndGovernment() {
  tap("next-step");tap("clear");tap("add");onView(withText("N.D.")).perform(click());
  onView(tag("value-2")).perform(scrollTo(),replaceText("30"),androidx.test.espresso.action.ViewActions.closeSoftKeyboard());
  tap("add");onView(withText("SYRIZA-P.S.")).perform(click());
  onView(tag("value-4")).perform(scrollTo(),replaceText("25"),androidx.test.espresso.action.ViewActions.closeSoftKeyboard());
  onView(tag("input-total")).perform(scrollTo()).check(matches(withText(containsString("45%"))));
  tap("calculate");onView(tag("seats-2")).perform(scrollTo()).check(matches(withText("177")));
  tap("government-2");onView(tag("government-total")).perform(scrollTo()).check(matches(withText(containsString("177 / 300"))));
  tap("government-4");onView(tag("government-total")).perform(scrollTo()).check(matches(withText(containsString("300 / 300"))));
 }
 @Test public void pollsLoadAndRemainAfterRecreation(){tap("polls");onView(withText(containsString("GPO · 2026-09-08"))).perform(click());onView(withText("Use poll results")).perform(click());tap("calculate");onView(tag("seat-total")).check(matches(withText(containsString("300"))));activity.getActivity().runOnUiThread(()->activity.getActivity().recreate());InstrumentationRegistry.getInstrumentation().waitForIdleSync();onView(tag("seat-total")).check(matches(withText(containsString("300"))));}
 @Test public void lockedStepsAndPartyColorPicker() {
  onView(tag("example")).check(matches(not(isEnabled())));
  tap("next-step");onView(tag("dataset")).check(matches(not(isEnabled())));tap("add");
  onView(withText("N.D.")).check(matches(isDisplayed())).perform(click());
  onView(tag("value-2")).perform(scrollTo()).check(matches(isDisplayed()));
  onView(tag("previous-step")).perform(scrollTo()).check(matches(isDisplayed()));
 }
}
