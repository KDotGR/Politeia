package gr.politeia.app;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.View;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.io.FileOutputStream;
import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class WalkthroughTest {
 @Rule public ActivityTestRule<MainActivity> activity=new ActivityTestRule<>(MainActivity.class,false,false);
 private SharedPreferences prefs(){return InstrumentationRegistry.getInstrumentation().getTargetContext().getSharedPreferences("gr.politeia.app.MainActivity",0);}
 private void launch(boolean greek,boolean dark){
  Context c=InstrumentationRegistry.getInstrumentation().getTargetContext();prefs().edit().clear().putBoolean("greek",greek).putBoolean("sounds",false).putString("appearance",dark?"dark":"light").commit();
  c.getSharedPreferences("poll-cache",0).edit().clear().putBoolean("auto",false).commit();activity.launchActivity(null);
 }
 private void tap(String tag){onView(withTagValue(is((Object)tag))).perform(click());}
 private void skip(){tap("guide-skip");}
 private void above(){final Rect card=new Rect();onView(withTagValue(is((Object)"guide-card"))).check((v,e)->{if(e!=null)throw e;assertTrue(v.getGlobalVisibleRect(card));});onView(withTagValue(is((Object)"guide-highlight"))).check((v,e)->{if(e!=null)throw e;Rect focus=new Rect();assertTrue(v.getGlobalVisibleRect(focus));assertTrue("Explanation must be above its highlighted control",card.bottom<=focus.top);});}
 private void screenshot(String name)throws Exception{Bitmap bitmap=InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();File folder=new File(InstrumentationRegistry.getArguments().getString("additionalTestOutputDir",activity.getActivity().getCacheDir().getPath()));folder.mkdirs();try(FileOutputStream out=new FileOutputStream(new File(folder,name+".png"))){bitmap.compress(Bitmap.CompressFormat.PNG,100,out);}bitmap.recycle();}
 @Test public void homeHighlightTracksRealControlAndRecreation()throws Exception{
  launch(false,false);above();
  onView(withTagValue(is((Object)"guide-highlight"))).check((v,e)->{if(e!=null)throw e;View target=((Activity)v.getContext()).findViewById(android.R.id.content).findViewWithTag("type-0");int[] actual=new int[2],highlight=new int[2];target.getLocationOnScreen(actual);v.getLocationOnScreen(highlight);assertEquals(actual[1],highlight[1]);});
  screenshot("guided-home");tap("guide-next");String title=FeatureGuideCatalog.tips("home",false).get(1).title;
  InstrumentationRegistry.getInstrumentation().runOnMainSync(()->activity.getActivity().recreate());onView(withTagValue(is((Object)"guide-title"))).check(matches(withText(title)));above();skip();assertTrue(prefs().getBoolean("guide.v1.seen.home",false));
  activity.finishActivity();activity.launchActivity(null);onView(withTagValue(is((Object)"feature-guide"))).check(doesNotExist());
 }
 @Test public void guidesFollowSourcesResultsAndCoalitionWithoutChangingInputs(){
  launch(false,false);skip();tap("next-step");skip();tap("source-election");skip();tap("dataset");above();skip();onView(withText("Parliament · June 2023")).perform(click());tap("next-step");skip();tap("next-step");above();skip();
  onView(withTagValue(is((Object)"calculate"))).perform(scrollTo(),click());skip();onView(withTagValue(is((Object)"seats-2"))).perform(scrollTo()).check(matches(withText("158")));
  onView(withTagValue(is((Object)"government-page"))).perform(scrollTo(),click());above();skip();onView(withTagValue(is((Object)"government-2"))).perform(scrollTo(),click());onView(withTagValue(is((Object)"government-total"))).perform(scrollTo()).check(matches(withText(containsString("158 / 300"))));
  tap("detail-back");onView(withTagValue(is((Object)"feature-guide"))).check(doesNotExist());
 }
 @Test public void customBonusAndPartyEditorHaveIndependentGuidance()throws Exception{
  launch(true,true);skip();tap("type-3");tap("next-step");above();skip();onView(withTagValue(is((Object)"custom-bonus"))).perform(scrollTo(),click());above();screenshot("guided-custom-bonus-dark");skip();
  onView(withTagValue(is((Object)"next-step"))).perform(scrollTo(),click());skip();tap("add");above();tap("guide-next");above();screenshot("guided-candidates-dark");skip();
  onView(withTagValue(is((Object)"party-name"))).perform(replaceText("Δοκιμή"),androidx.test.espresso.action.ViewActions.closeSoftKeyboard());onView(withTagValue(is((Object)"candidates"))).perform(replaceText("10"),androidx.test.espresso.action.ViewActions.closeSoftKeyboard());onView(withText("Αποθήκευση")).perform(click());
  onView(withTagValue(is((Object)"party-details"))).perform(scrollTo(),click());above();skip();onView(withText("Ακύρωση")).perform(click());
  assertTrue(prefs().getBoolean("guide.v1.seen.custom-party-edit",false));
  assertTrue(prefs().getBoolean("guide.v1.seen.custom-bonus",false));assertTrue(prefs().getBoolean("guide.v1.seen.custom-party-editor",false));
 }
 @Test public void everyHomeTipFitsInLandscape()throws Exception{
  launch(false,false);InstrumentationRegistry.getInstrumentation().runOnMainSync(()->activity.getActivity().setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE));
  android.os.SystemClock.sleep(500);
  for(int i=0;i<FeatureGuideCatalog.tips("home",false).size();i++){above();tap("guide-next");}
  onView(withTagValue(is((Object)"feature-guide"))).check(doesNotExist());assertTrue(prefs().getBoolean("guide.v1.seen.home",false));
 }
 @Test public void settingsCanDisableAndReplayFeatureTips(){
  launch(false,false);skip();tap("settings");above();skip();onView(withTagValue(is((Object)"tips-enabled"))).perform(scrollTo(),click());onView(withText("Close")).perform(click());tap("next-step");onView(withTagValue(is((Object)"feature-guide"))).check(doesNotExist());
  tap("settings");onView(withTagValue(is((Object)"walkthrough"))).perform(scrollTo(),click());above();assertTrue(prefs().getBoolean("featureTips",false));skip();
 }
}
