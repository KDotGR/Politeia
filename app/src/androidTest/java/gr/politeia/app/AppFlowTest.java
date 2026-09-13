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
 // Activity.getPreferences uses the fully qualified local class name when applicationId differs.
 private static final String ACTIVITY_PREFERENCES="gr.politeia.app.MainActivity";
 private boolean legacyDraftRequested;
 @Rule public ActivityTestRule<MainActivity> activity=new ActivityTestRule<MainActivity>(MainActivity.class){
  @Override protected void beforeActivityLaunched(){
   Context c=InstrumentationRegistry.getInstrumentation().getTargetContext();
   c.getSharedPreferences(ACTIVITY_PREFERENCES,0).edit().clear().putBoolean("greek",false).putBoolean("sounds",false).commit();
   c.getSharedPreferences("poll-cache",0).edit().clear().putBoolean("auto",false).commit();
   if(legacyDraftRequested)seedLegacyDraft(c);
   shell("input keyevent KEYCODE_WAKEUP");shell("wm dismiss-keyguard");
  }
  @Override protected void afterActivityLaunched(){
   InstrumentationRegistry.getInstrumentation().runOnMainSync(()->getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON));
  }
 };
 private void seedLegacyDraft(Context context){
  try {
   org.json.JSONArray parties=new ElectionRepository(context).get("may").getJSONArray("parties");
   org.json.JSONObject values=new org.json.JSONObject();
   for(int i=0;i<parties.length();i++){org.json.JSONObject party=parties.getJSONObject(i);values.put(party.getString("id"),Long.toString(party.getLong("votes")));}
   // Deliberately use only v0.1 fields: the legacy law was inferred from the dataset.
   org.json.JSONObject draft=new org.json.JSONObject().put("editorStep",2).put("resultScreen",false).put("type",0).put("dataset","may").put("percent",false).put("future",false).put("parties",parties).put("values",values).put("custom",new org.json.JSONArray("[\"100\",\"0\",\"20\",\"0\",\"5\",\"1\",\"20\"]"));
   assertTrue(context.getSharedPreferences(ACTIVITY_PREFERENCES,0).edit().putString("draft",draft.toString()).commit());
  } catch(org.json.JSONException error){throw new AssertionError(error);}
 }
 private static void shell(String command){try(ParcelFileDescriptor fd=InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(command);InputStream in=new ParcelFileDescriptor.AutoCloseInputStream(fd)){byte[] b=new byte[1024];while(in.read(b)!=-1){}}catch(Exception e){throw new RuntimeException(e);}}
 private static org.hamcrest.Matcher<View> tag(String s){return withTagValue(is((Object)s));}
 private void tap(String s){
  if(java.util.Arrays.asList("save","open","clear","example").contains(s))onView(tag("scenario-menu")).perform(scrollTo(),click());
  if(java.util.Arrays.asList("sound","language").contains(s))onView(tag("settings")).perform(click());
  if(java.util.Arrays.asList("settings").contains(s))onView(tag(s)).perform(click());else onView(tag(s)).perform(scrollTo(),click());
 }
 private void shot(String name)throws Exception{
  // Surface rotation and dialog-dismiss animations finish after Espresso becomes idle.
  android.os.SystemClock.sleep(500);
  Bitmap b=InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();File dir=new File(InstrumentationRegistry.getArguments().getString("additionalTestOutputDir","/sdcard/Android/media/gr.politeia.app/additional_test_output"));dir.mkdirs();try(FileOutputStream out=new FileOutputStream(new File(dir,name+".png"))){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));}}
 private void votes(){tap("next-step");tap("next-step");tap("next-step");}
 private void page(String title){onView(tag("page-title")).perform(scrollTo()).check(matches(withText(title)));}
 private void recreate(){activity.getActivity().runOnUiThread(()->activity.getActivity().recreate());InstrumentationRegistry.getInstrumentation().waitForIdleSync();}
 private void addCustomParty(String name,String candidates){
  tap("add");onView(tag("party-name")).perform(replaceText(name),androidx.test.espresso.action.ViewActions.closeSoftKeyboard());
  onView(tag("candidates")).perform(replaceText(candidates),androidx.test.espresso.action.ViewActions.closeSoftKeyboard());onView(withText("Save")).perform(click());
 }
 private void customVotes(String alphaCandidates,String betaCandidates){
  tap("type-3");tap("next-step");tap("next-step");addCustomParty("Alpha",alphaCandidates);addCustomParty("Beta",betaCandidates);
  onView(withContentDescription("Alpha %")).perform(scrollTo(),replaceText("65"),androidx.test.espresso.action.ViewActions.closeSoftKeyboard());
  onView(withContentDescription("Beta %")).perform(scrollTo(),replaceText("35"),androidx.test.espresso.action.ViewActions.closeSoftKeyboard());
 }
 @Test public void historicalCalculationAndExplanation() throws Exception {
  shot("01-english-home");votes();tap("example");tap("calculate");
  onView(tag("seat-total")).check(matches(withText(containsString("300"))));shot("02-parliament-results");
  onView(tag("seats-2")).perform(scrollTo()).check(matches(withText("158")));
  onView(tag("explanation")).check(doesNotExist());tap("explanation-page");
  onView(tag("explanation")).perform(scrollTo()).check(matches(isDisplayed()));shot("03-calculation-trail");tap("detail-back");onView(tag("seat-total")).perform(scrollTo()).check(matches(isDisplayed()));
 }
 @Test public void languageSwitch() throws Exception {
  tap("language");onView(withText("Ελληνικά")).perform(click());shot("04-greek-home");
  onView(tag("next-step")).perform(scrollTo()).check(matches(withText("Επόμενο →")));
  votes();
  tap("example");tap("calculate");tap("explanation-page");onView(tag("page-title")).perform(scrollTo()).check(matches(withText("Πώς κατανεμήθηκαν οι έδρες")));shot("05-greek-explanation");
 }
 @Test public void invalidInputShowsError(){votes();tap("clear");tap("calculate");onView(tag("error")).check(matches(isDisplayed()));}
 @Test public void europeanHistoricalResults()throws Exception{tap("type-1");votes();tap("example");tap("calculate");onView(tag("seat-total")).check(matches(withText(containsString("21"))));onView(tag("government-page")).check(doesNotExist());shot("06-european-results");}
 @Test public void athensRunoffHistoricalResults()throws Exception{tap("type-2");tap("next-step");tap("next-step");tap("law");onData(hasToString(containsString("4804/2021"))).perform(click());tap("next-step");tap("example");tap("calculate");onView(tag("seat-total")).check(matches(withText(containsString("43"))));shot("07-athens-results");}
 @Test public void customPartyEntryAndBonus()throws Exception{
  tap("type-3");tap("next-step");page("Choose the electoral law");onView(tag("dataset")).check(doesNotExist());onView(tag("custom-0")).perform(scrollTo()).check(matches(isDisplayed()));tap("custom-bonus");tap("next-step");
  addCustomParty("Alpha","100");addCustomParty("Beta","100");
  onView(withContentDescription("Alpha %")).perform(scrollTo(),replaceText("65"),androidx.test.espresso.action.ViewActions.closeSoftKeyboard());
  onView(withContentDescription("Beta %")).perform(scrollTo(),replaceText("35"),androidx.test.espresso.action.ViewActions.closeSoftKeyboard());tap("calculate");
  onView(tag("seat-total")).check(matches(withText(containsString("100"))));shot("08-custom-results");
  tap("explanation-page");onView(tag("explanation")).perform(scrollTo()).check(matches(isDisplayed()));
 }
 @Test public void valuesSurviveRecreation(){votes();tap("example");recreate();tap("calculate");onView(tag("seat-total")).check(matches(withText(containsString("300"))));}
 @Test public void landscapeLayout()throws Exception{
  activity.getActivity().runOnUiThread(()->activity.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE));InstrumentationRegistry.getInstrumentation().waitForIdleSync();
  onView(tag("type-0")).perform(scrollTo()).check(matches(isDisplayed()));shot("09-landscape");votes();tap("example");tap("calculate");onView(tag("seat-total")).check(matches(withText(containsString("300"))));tap("government-page");page("Government coalition simulator");tap("detail-back");onView(tag("seat-total")).perform(scrollTo()).check(matches(isDisplayed()));
 }
 @Test public void soundToggleAndSavedScenario(){
  tap("sound");onView(tag("sound")).check(matches(withContentDescription("Sound on"))).perform(click()).check(matches(withContentDescription("Sound off")));onView(withText("Close")).perform(click());
  votes();tap("example");tap("save");onView(withHint("Scenario name")).perform(replaceText("Regression scenario"),androidx.test.espresso.action.ViewActions.closeSoftKeyboard());onView(withText("Save")).perform(click());tap("clear");tap("open");onView(withText("Regression scenario")).perform(click());tap("calculate");onView(tag("seat-total")).check(matches(withText(containsString("300"))));
 }
 @Test public void dropdownRemainderAndGovernment() {
  votes();tap("clear");tap("add");onView(withText("N.D.")).perform(click());
  onView(tag("value-2")).perform(scrollTo(),replaceText("30"),androidx.test.espresso.action.ViewActions.closeSoftKeyboard());
  tap("add");onView(withText("SYRIZA-P.S.")).perform(click());
  onView(tag("value-4")).perform(scrollTo(),replaceText("25"),androidx.test.espresso.action.ViewActions.closeSoftKeyboard());
  onView(tag("input-total")).perform(scrollTo()).check(matches(withText(containsString("45%"))));
  tap("calculate");onView(tag("seats-2")).perform(scrollTo()).check(matches(withText("177")));
  onView(tag("government-total")).check(doesNotExist());tap("government-page");
  tap("government-2");onView(tag("government-total")).perform(scrollTo()).check(matches(withText(containsString("177 / 300"))));
  tap("government-4");onView(tag("government-total")).perform(scrollTo()).check(matches(withText(containsString("300 / 300"))));
 }
 @Test public void pollsLoadAndRemainAfterRecreation(){tap("next-step");tap("polls");onView(withText(containsString("GPO · 2026-09-08"))).perform(click());onView(withText("Party list only")).check(doesNotExist());onView(withText("Use poll results")).perform(click());page("Choose your party list");tap("next-step");tap("next-step");tap("calculate");onView(tag("seat-total")).check(matches(withText(containsString("300"))));tap("poll-source-page");page("Poll source & assumptions");onView(tag("seat-total")).check(doesNotExist());recreate();page("Poll source & assumptions");tap("detail-back");onView(tag("seat-total")).check(matches(withText(containsString("300"))));}
 @Test public void stepNavigationKeepsInputsAndShowsOnlyCurrentStep(){
  votes();tap("clear");tap("add");onView(withText("N.D.")).perform(click());
  onView(tag("value-2")).perform(scrollTo(),replaceText("30"),androidx.test.espresso.action.ViewActions.closeSoftKeyboard());
  tap("previous-step");onView(tag("value-2")).check(doesNotExist());
  tap("next-step");onView(tag("value-2")).perform(scrollTo()).check(matches(withText("30")));
  androidx.test.espresso.Espresso.pressBack();page("Choose the electoral law");androidx.test.espresso.Espresso.pressBack();onView(tag("dataset")).perform(scrollTo()).check(matches(isDisplayed()));
 }
 @Test public void lockedStepsAndPartyColorPicker() throws Exception {
  onView(tag("value-2")).check(doesNotExist());
  votes();onView(tag("dataset")).check(doesNotExist());tap("clear");tap("add");
  onView(withText("N.D.")).check(matches(isDisplayed())).perform(click());
  onView(tag("value-2")).perform(scrollTo()).check(matches(isDisplayed()));
  onView(tag("previous-step")).perform(scrollTo()).check(matches(isDisplayed()));shot("10-enter-votes");
 }
 @Test public void eachStepHasOnlyItsOwnControls(){
  page("Choose your election");onView(tag("dataset")).check(doesNotExist());onView(tag("law")).check(doesNotExist());
  tap("next-step");page("Choose your party list");onView(tag("type-0")).check(doesNotExist());onView(tag("law")).check(doesNotExist());
  tap("next-step");page("Choose the electoral law");onView(tag("law")).perform(scrollTo()).check(matches(withText(containsString("4654/2020"))));onView(tag("dataset")).check(doesNotExist());onView(tag("calculate")).check(doesNotExist());
  tap("next-step");page("Enter votes");onView(tag("law")).check(doesNotExist());onView(tag("calculate")).perform(scrollTo()).check(matches(isDisplayed()));
 }
 @Test public void historicalDatasetDoesNotSelectHistoricalLaw(){
  tap("next-step");tap("dataset");onView(withText("Parliament · May 2023")).perform(click());tap("next-step");onView(tag("law")).perform(scrollTo()).check(matches(withText(containsString("4654/2020"))));
 }
 @Test public void selectedLawSurvivesDatasetChangeAndRecreation(){
  tap("next-step");tap("next-step");tap("law");onData(hasToString(containsString("4406/2016"))).perform(click());tap("previous-step");tap("dataset");onView(withText("Parliament · June 2023")).perform(click());tap("next-step");recreate();onView(tag("law")).perform(scrollTo()).check(matches(withText(containsString("4406/2016"))));
  tap("next-step");tap("example");tap("calculate");onView(tag("seats-2")).perform(scrollTo()).check(matches(withText("129")));
 }
 @Test public void coalitionStateAndBackReturnToResults(){
  votes();tap("example");tap("calculate");tap("government-page");tap("government-2");recreate();onView(tag("government-total")).perform(scrollTo()).check(matches(withText(containsString("158 / 300"))));androidx.test.espresso.Espresso.pressBack();onView(tag("seat-total")).perform(scrollTo()).check(matches(isDisplayed()));tap("government-page");onView(tag("government-2")).perform(scrollTo()).check(matches(isChecked()));tap("detail-back");tap("explanation-page");androidx.test.espresso.Espresso.pressBack();onView(tag("seat-total")).perform(scrollTo()).check(matches(isDisplayed()));
 }
 @Test public void settingsControlsHaveVisibleSpacing(){
  tap("settings");onView(tag("sound")).check((view,error)->{if(error!=null)throw error;View language=view.getRootView().findViewWithTag("language");View sources=view.getRootView().findViewWithTag("sources");int[] a=new int[2],b=new int[2],c=new int[2];language.getLocationOnScreen(a);view.getLocationOnScreen(b);sources.getLocationOnScreen(c);int minGap=Math.round(8*view.getResources().getDisplayMetrics().density);assertTrue("Language and sound controls need at least 8dp spacing",b[1]-a[1]-language.getHeight()>=minGap);assertTrue("Sound and sources controls need at least 8dp spacing",c[1]-b[1]-view.getHeight()>=minGap);});onView(withText("Close")).perform(click());
 }
 @Test public void legacyDraftMigratesToVotesAndPreservesHistoricalLaw(){
  activity.finishActivity();legacyDraftRequested=true;activity.launchActivity(null);
  page("Enter votes");onView(tag("value-2")).perform(scrollTo()).check(matches(not(withText("0"))));
  tap("previous-step");onView(tag("law")).perform(scrollTo()).check(matches(withText(containsString("4406/2016"))));
  tap("next-step");tap("calculate");onView(tag("seats-2")).perform(scrollTo()).check(matches(withText("146")));
 }
 @Test public void selectedHistoricalLawSurvivesPollLoad(){
  tap("next-step");tap("next-step");tap("law");onData(hasToString(containsString("4406/2016"))).perform(click());tap("previous-step");
  tap("polls");onView(withText(containsString("GPO · 2026-09-08"))).perform(click());onView(withText("Use poll results")).perform(click());
  page("Choose your party list");tap("next-step");onView(tag("law")).perform(scrollTo()).check(matches(withText(containsString("4406/2016"))));
 }
 @Test public void customBonusToggleRetainsConfigurationAfterRecreation(){
  tap("type-3");tap("next-step");onView(tag("custom-bonus")).perform(scrollTo()).check(matches(isNotChecked()));
  for(int i=2;i<=6;i++)onView(tag("custom-"+i)).check(doesNotExist());
  tap("custom-bonus");onView(tag("custom-2")).perform(scrollTo(),click(),replaceText("25"),androidx.test.espresso.action.ViewActions.closeSoftKeyboard());
  onView(tag("custom-6")).perform(scrollTo(),click(),replaceText("30"),androidx.test.espresso.action.ViewActions.closeSoftKeyboard());
  tap("custom-bonus");recreate();onView(tag("custom-bonus")).perform(scrollTo()).check(matches(isNotChecked()));
  for(int i=2;i<=6;i++)onView(tag("custom-"+i)).check(doesNotExist());
  tap("custom-bonus");onView(tag("custom-2")).perform(scrollTo()).check(matches(withText("25")));onView(tag("custom-6")).perform(scrollTo()).check(matches(withText("30")));
 }
 @Test public void customPartyRequiresCandidatesAndKeepsOneNameAcrossLanguages(){
  tap("type-3");tap("next-step");tap("next-step");tap("add");
  onView(withHint("English name")).check(doesNotExist());onView(withHint("Greek name")).check(doesNotExist());
  onView(tag("party-name")).perform(replaceText("Ελπίδα"),androidx.test.espresso.action.ViewActions.closeSoftKeyboard());
  onView(tag("candidates")).check(matches(withText("")));onView(withText("Save")).perform(click());onView(tag("candidates")).check(matches(isDisplayed()));
  onView(tag("candidates")).perform(replaceText("10"),androidx.test.espresso.action.ViewActions.closeSoftKeyboard());onView(withText("Save")).perform(click());
  onView(withContentDescription("Ελπίδα %")).perform(scrollTo()).check(matches(isDisplayed()));
  tap("language");onView(withText("Ελληνικά")).perform(click());recreate();onView(withContentDescription("Ελπίδα %")).perform(scrollTo()).check(matches(isDisplayed()));
 }
 @Test public void customCandidateExcessRedistributesToAvailableCandidates(){
  customVotes("10","100");tap("calculate");onView(tag("seat-total")).perform(scrollTo()).check(matches(withText(containsString("100"))));
  onView(withText("10")).perform(scrollTo()).check(matches(isDisplayed()));onView(withText("90")).perform(scrollTo()).check(matches(isDisplayed()));
  onView(tag("vacant-seats")).perform(scrollTo()).check(matches(withText("0 vacant seats")));
  tap("explanation-page");onView(tag("explanation")).perform(scrollTo()).check(matches(isDisplayed()));
 }
 @Test public void customCandidateVacanciesSurviveSaveAndRecreation(){
  customVotes("10","20");tap("save");onView(withHint("Scenario name")).perform(replaceText("Candidate limits"),androidx.test.espresso.action.ViewActions.closeSoftKeyboard());onView(withText("Save")).perform(click());
  tap("clear");tap("open");onView(withText("Candidate limits")).perform(click());recreate();tap("calculate");
  onView(tag("seat-total")).perform(scrollTo()).check(matches(withText(containsString("30"))));onView(tag("vacant-seats")).perform(scrollTo()).check(matches(withText("70 vacant seats")));
  onView(withText("10")).perform(scrollTo()).check(matches(isDisplayed()));onView(withText("20")).perform(scrollTo()).check(matches(isDisplayed()));
  recreate();onView(tag("vacant-seats")).perform(scrollTo()).check(matches(withText("70 vacant seats")));
 }
}
