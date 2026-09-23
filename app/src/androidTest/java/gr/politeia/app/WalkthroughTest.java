package gr.politeia.app;

import android.content.Context;
import android.content.res.Configuration;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class WalkthroughTest {
 @Rule public ActivityTestRule<MainActivity> activity=new ActivityTestRule<>(MainActivity.class,false,false);
 private void launch(boolean greek,boolean dark){
  Context c=InstrumentationRegistry.getInstrumentation().getTargetContext();
  c.getSharedPreferences("gr.politeia.app.MainActivity",0).edit().clear().putBoolean("greek",greek).putBoolean("sounds",false).putString("appearance",dark?"dark":"light").commit();
  c.getSharedPreferences("poll-cache",0).edit().clear().putBoolean("auto",false).commit();activity.launchActivity(null);
 }
 private void tap(String tag){onView(withTagValue(is((Object)tag))).perform(click());}
 private void page(String text){onView(withTagValue(is((Object)"walkthrough-progress"))).check(matches(withText(text)));}
 @Test public void firstLaunchCanSkipAndReopenWithoutChangingElection(){
  launch(false,false);page("1 / 5");tap("walkthrough-skip");tap("type-1");tap("next-step");
  tap("settings");onView(withTagValue(is((Object)"walkthrough"))).perform(scrollTo(),click());page("1 / 5");tap("walkthrough-skip");
  onView(withTagValue(is((Object)"page-title"))).check(matches(withText("Choose your party list")));
  activity.finishActivity();activity.launchActivity(null);onView(withTagValue(is((Object)"walkthrough-progress"))).check(doesNotExist());
  onView(withTagValue(is((Object)"page-title"))).check(matches(withText("Choose your party list")));
 }
 @Test public void completionAndRecreationRememberProgress(){
  launch(false,false);tap("walkthrough-next");tap("walkthrough-next");page("3 / 5");
  InstrumentationRegistry.getInstrumentation().runOnMainSync(()->activity.getActivity().recreate());
  page("3 / 5");tap("walkthrough-back");page("2 / 5");tap("walkthrough-next");tap("walkthrough-next");tap("walkthrough-next");page("5 / 5");tap("walkthrough-next");
  onView(withTagValue(is((Object)"walkthrough-progress"))).check(doesNotExist());activity.finishActivity();activity.launchActivity(null);onView(withTagValue(is((Object)"walkthrough-progress"))).check(doesNotExist());
 }
 @Test public void existingDraftDoesNotTriggerFirstUseGuide(){
  launch(false,false);tap("walkthrough-skip");tap("next-step");activity.finishActivity();
  Context c=InstrumentationRegistry.getInstrumentation().getTargetContext();c.getSharedPreferences("gr.politeia.app.MainActivity",0).edit().remove("walkthroughSeen").commit();
  activity.launchActivity(null);onView(withTagValue(is((Object)"walkthrough-progress"))).check(doesNotExist());
  onView(withTagValue(is((Object)"page-title"))).check(matches(withText("Choose your party list")));
 }
 @Test public void greekDarkWalkthroughSupportsSystemBack(){
  launch(true,true);onView(withText("Καλώς ήρθατε στην Πολιτεία")).check(matches(isDisplayed()));
  onView(withTagValue(is((Object)"walkthrough-progress"))).check((v,e)->{if(e!=null)throw e;assertEquals(Configuration.UI_MODE_NIGHT_YES,v.getResources().getConfiguration().uiMode&Configuration.UI_MODE_NIGHT_MASK);});
  androidx.test.espresso.Espresso.pressBack();onView(withTagValue(is((Object)"walkthrough-progress"))).check(doesNotExist());
  activity.finishActivity();activity.launchActivity(null);onView(withTagValue(is((Object)"walkthrough-progress"))).check(doesNotExist());
 }
}
