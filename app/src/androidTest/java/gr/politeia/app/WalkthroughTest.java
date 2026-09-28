package gr.politeia.app;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class WalkthroughTest {
 @Rule public ActivityTestRule<MainActivity> activity=new ActivityTestRule<>(MainActivity.class,false,false);
 private Context context(){return InstrumentationRegistry.getInstrumentation().getTargetContext();}
 private SharedPreferences prefs(){return context().getSharedPreferences("gr.politeia.app.MainActivity",0);}
 private SharedPreferences onboarding(){return context().getSharedPreferences("onboarding",0);}
 private void fresh(boolean greek){
  prefs().edit().clear().putBoolean("greek",greek).putBoolean("sounds",false).putString("appearance",greek?"dark":"light").commit();
  onboarding().edit().clear().commit();
  context().getSharedPreferences("poll-cache",0).edit().clear().putBoolean("auto",false).commit();
 }
 private void tap(String tag){onView(withTagValue(is((Object)tag))).perform(click());}
 private void absent(){onView(withTagValue(is((Object)"walkthrough-progress"))).check(doesNotExist());}
 private void relaunch(){activity.finishActivity();activity.launchActivity(null);}
 @Test public void skippedIntroductionDoesNotReturnOrOfferReplay(){
  fresh(false);activity.launchActivity(null);tap("walkthrough-skip");relaunch();absent();
  tap("settings");onView(withTagValue(is((Object)"walkthrough"))).check(doesNotExist());onView(withTagValue(is((Object)"tips-enabled"))).check(doesNotExist());
 }
 @Test public void completedGreekIntroductionDoesNotReturn(){
  fresh(true);activity.launchActivity(null);onView(withText("Καλώς ήρθατε στην Πολιτεία")).check(matches(isDisplayed()));
  for(int i=0;i<5;i++)tap("walkthrough-next");absent();relaunch();absent();
  tap("type-3");tap("next-step");absent();onView(withTagValue(is((Object)"feature-guide"))).check(doesNotExist());
 }
 @Test public void interruptedIntroductionDoesNotReturnOnLaterLaunch(){
  fresh(false);activity.launchActivity(null);tap("walkthrough-next");
  assertTrue(onboarding().getBoolean("installationIntroShown",false));relaunch();absent();
 }
 @Test public void recreationKeepsCurrentPageButFreshInstallationResetsIt(){
  fresh(false);activity.launchActivity(null);tap("walkthrough-next");
  InstrumentationRegistry.getInstrumentation().runOnMainSync(()->activity.getActivity().recreate());
  onView(withTagValue(is((Object)"walkthrough-progress"))).check(matches(withText("2 / 5")));
  tap("walkthrough-skip");activity.finishActivity();fresh(false);activity.launchActivity(null);
  onView(withTagValue(is((Object)"walkthrough-progress"))).check(matches(withText("1 / 5")));
 }
 @Test public void upgradesFromOldGuidesDoNotReplayIntroduction(){
  for(String key:new String[]{"walkthroughSeen","customWalkthroughSeen","featureTips","guide.v1.seen.home"}){
   fresh(false);prefs().edit().putBoolean(key,true).commit();activity.launchActivity(null);absent();
   assertTrue(onboarding().getBoolean("installationIntroShown",false));activity.finishActivity();
  }
 }
 @Test public void existingDraftDoesNotTriggerIntroductionOnUpgrade(){
  fresh(false);prefs().edit().putString("draft","{}").commit();activity.launchActivity(null);absent();
 }
}
