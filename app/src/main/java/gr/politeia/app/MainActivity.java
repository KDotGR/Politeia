package gr.politeia.app;

import android.app.*;
import android.os.*;
import android.content.*;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.*;
import android.media.*;
import android.text.*;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import org.json.*;
import gr.politeia.core.ElectionEngine;
import gr.politeia.core.Scenario;
import java.math.*;
import java.util.*;

public final class MainActivity extends Activity {
 private int BG,INK,MUTED,TEAL,LINE,SURFACE,BUTTON,SELECTED,ERROR,LOSS,ON_ACCENT;
 private String themeMode="system";
 private ElectionRepository repository;private JSONObject dataset;private String datasetId="june";
 private boolean greek,percent=true,sounds=true,expanded=false,future=false,resultScreen=false;
 private int editorStep=1; private int pendingScrollY=0;
 private boolean parliamentReinforced=true;
 private String sourceMode="";private boolean sourceReady=false;
 private boolean customBonus=false;
 private int resultPage=5;
 private int type=0;private String winner="";private final List<JSONObject> parties=new ArrayList<>();
 private final Map<String,String> values=new LinkedHashMap<>(),transfers=new LinkedHashMap<>();
 private final List<String> tieOrder=new ArrayList<>();
 private final String[] custom={"100","0","20","0","5","1","20"};
 private ElectionEngine.Result result;private LinearLayout root,body;private ScrollView scroll;private TextView totalLabel,error;
 private final Set<String> selectedParties=new LinkedHashSet<>(),government=new LinkedHashSet<>();
 private JSONObject activePoll;private boolean pollEdited=false;private PollRepository polls;private TextView pollStatus;private boolean refreshing;
 private ToneGenerator tone;private boolean building;
 private boolean animateStep; private int stepDirection=1;
 private String t(String en,String el){return greek?el:en;}
 private int dp(float n){return Math.round(n*getResources().getDisplayMetrics().density);}
 @Override public void onCreate(Bundle saved){configureAppearance();super.onCreate(saved);repository=new ElectionRepository(this);polls=new PollRepository(this);PollUpdateService.schedule(this);greek=getPreferences(0).getBoolean("greek",Locale.getDefault().getLanguage().equals("el"));sounds=getPreferences(0).getBoolean("sounds",true);loadDataset("june");restore();render();if(Build.VERSION.SDK_INT>=33)getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,this::navigateBack);
  // Existing drafts identify returning users; only a fresh installation opens the guide automatically.
  if(saved==null&&!getPreferences(0).getBoolean("walkthroughSeen",false)){
   if(getPreferences(0).contains("draft"))getPreferences(0).edit().putBoolean("walkthroughSeen",true).apply();
   else WalkthroughDialog.create(greek).show(getFragmentManager(),"walkthrough");
  }
 }
 // Override resources before Android initializes the activity theme, including native dialogs.
 @Override protected void attachBaseContext(Context base){
  super.attachBaseContext(base);
  themeMode=base.getSharedPreferences("gr.politeia.app.MainActivity",0).getString("appearance","system");
  if(themeMode.equals("light")||themeMode.equals("dark")){
   Configuration override=new Configuration();
   override.uiMode=(base.getResources().getConfiguration().uiMode&~Configuration.UI_MODE_NIGHT_MASK)|(themeMode.equals("dark")?Configuration.UI_MODE_NIGHT_YES:Configuration.UI_MODE_NIGHT_NO);
   applyOverrideConfiguration(override);
  }
 }
 // System mode leaves uiMode untouched; Android recreates the activity on device theme changes.
 private void configureAppearance(){
  setTheme(R.style.AppTheme);
  BG=getColor(R.color.app_background);INK=getColor(R.color.app_ink);MUTED=getColor(R.color.app_muted);TEAL=getColor(R.color.app_accent);LINE=getColor(R.color.app_line);
  SURFACE=getColor(R.color.app_surface);BUTTON=getColor(R.color.app_button);SELECTED=getColor(R.color.app_selected);ERROR=getColor(R.color.app_error);LOSS=getColor(R.color.app_loss);ON_ACCENT=getColor(R.color.app_on_accent);
 }
 @Override protected void onResume(){super.onResume();if(polls!=null&&polls.enabled()&&System.currentTimeMillis()-polls.checked()>PollRepository.INTERVAL)refreshPolls(false);}
 @Override protected void onPause(){save();super.onPause();}
 @Override protected void onDestroy(){if(tone!=null)tone.release();super.onDestroy();}
 private void sound(boolean success){feedback(success?"success":"tap");}
 private void feedback(String event){
  if(!sounds)return;
  AudioManager am=(AudioManager)getSystemService(AUDIO_SERVICE);
  if(am==null||am.getRingerMode()!=AudioManager.RINGER_MODE_NORMAL)return;
  int note=ToneGenerator.TONE_PROP_BEEP,duration=24;
  if(event.equals("next")){note=ToneGenerator.TONE_PROP_ACK;duration=55;}
  if(event.equals("back")){note=ToneGenerator.TONE_PROP_BEEP2;duration=35;}
  if(event.equals("success")){note=ToneGenerator.TONE_PROP_ACK;duration=110;}
  if(event.equals("error")){note=ToneGenerator.TONE_PROP_NACK;duration=70;}
  try{if(tone==null)tone=new ToneGenerator(AudioManager.STREAM_MUSIC,15);tone.startTone(note,duration);}catch(RuntimeException ignored){}
 }
 private void goToStep(int step){
  ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(root.getWindowToken(),0);
  stepDirection=step<editorStep||resultScreen?-1:1;feedback(stepDirection<0?"back":"next");
  editorStep=step;resultScreen=false;resultPage=5;pendingScrollY=0;animateStep=true;render();save();
 }
 private void loadDataset(String id){selectedParties.clear();government.clear();activePoll=null;pollEdited=false;datasetId=id;dataset=repository.get(id);parties.clear();values.clear();transfers.clear();winner="";tieOrder.clear();result=null;resultScreen=false;expanded=false;
  JSONArray a=dataset.optJSONArray("parties");for(int i=0;i<a.length();i++){JSONObject p;try{p=new JSONObject(a.optJSONObject(i).toString());}catch(JSONException e){throw new IllegalStateException(e);}parties.add(p);values.put(p.optString("id"),"0");}}
 private String name(JSONObject p){return p.optString(greek?"el":"en",p.optString("id"));}
 private String title(){if(activePoll!=null)return activePoll.optString("firm")+" · "+activePoll.optString("date")+(pollEdited?t(" · edited scenario"," · τροποποιημένο σενάριο"):t(" · poll scenario"," · δημοσκοπικό σενάριο"));return type==3?t("Custom election","Προσαρμοσμένες εκλογές"):dataset.optString(greek?"el":"en");}
 private String ruleName(){if(type==0)return parliamentReinforced?"Ν. 4654/2020":"Ν. 4406/2016";if(type==1)return "Ν. 4255/2014 · Ν. 5083/2024";if(type==2)return future?"Ν. 5314/2026 · Ν. 5321/2026":"Ν. 4804/2021";return t("Your electoral rules","Οι δικοί σας κανόνες");}
 private int seats(){if(type==3){try{return Integer.parseInt(custom[0]);}catch(Exception e){return 0;}}if(type==2&&future){return dataset.optString("kind").equals("region")?ElectionEngine.regionalSeats(dataset.optInt("population"),dataset.optInt("regionId"),true):ElectionEngine.municipalSeats(dataset.optInt("population"),dataset.optInt("units"));}return dataset.optInt("seats");}
 private GradientDrawable bg(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
 private TextView text(String s,int size,int color,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(color);v.setFontFeatureSettings("tnum");v.setTypeface(Typeface.create("sans-serif",bold?Typeface.BOLD:Typeface.NORMAL));v.setLineSpacing(dp(3),1);return v;}
 private LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
 private LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
 private void space(LinearLayout l,int height){View v=new View(this);l.addView(v,new LinearLayout.LayoutParams(1,dp(height)));}
 private void weighted(LinearLayout row,View child){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,1);p.setMargins(dp(4),dp(3),dp(4),dp(3));row.addView(child,p);}
 private Button button(String label,String tag,Runnable action,boolean primary){Button b=new Button(this);b.setText(label);b.setTag(tag);b.setAllCaps(false);b.setTextSize(14);b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);b.setTextColor(primary?ON_ACCENT:INK);b.setPadding(dp(12),dp(9),dp(12),dp(9));b.setMinHeight(dp(50));b.setMinimumHeight(dp(50));b.setStateListAnimator(null);b.setBackground(new RippleDrawable(android.content.res.ColorStateList.valueOf(0x22008578),bg(primary?TEAL:BUTTON,12),null));b.setSoundEffectsEnabled(false);b.setOnClickListener(v->{
   v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
   if(!Arrays.asList("next-step","previous-step","calculate","edit","sound").contains(tag))sound(false);
   action.run();});return b;}
 private LinearLayout card(LinearLayout parent,String heading){LinearLayout box=column();box.setPadding(dp(20),dp(18),dp(20),dp(20));box.setBackground(bg(SURFACE,20));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(8),0,dp(8));parent.addView(box,p);if(heading!=null){box.addView(text(heading,19,INK,true));space(box,12);}return box;}
 private void description(LinearLayout p,String s){p.addView(text(s,13,MUTED,false));space(p,10);}
 private void stepButton(LinearLayout parent,String label,String tag,Runnable action,boolean primary){Button b=button(label,tag,action,primary);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-2,-2);p.gravity=Gravity.START;p.setMargins(0,dp(4),0,dp(4));parent.addView(b,p);}
 private void render(){
  building=true;totalLabel=null;error=null;pollStatus=null;
  root=column();root.setTag("app-root");root.setBackgroundColor(BG);root.setPadding(dp(16),0,dp(16),0);
  root.setOnApplyWindowInsetsListener((v,in)->{v.setPadding(dp(16),in.getSystemWindowInsetTop(),dp(16),in.getSystemWindowInsetBottom());return in;});setContentView(root);
  LinearLayout header=row();TextView logo=text(t("politeia","πολιτεία"),26,INK,true);logo.setGravity(Gravity.CENTER_VERTICAL);header.addView(logo,new LinearLayout.LayoutParams(0,dp(60),1));
  Button options=button("⚙","settings",this::settings,false);options.setContentDescription(t("Settings and information","Ρυθμίσεις και πληροφορίες"));header.addView(options,new LinearLayout.LayoutParams(dp(48),dp(48)));root.addView(header);
  // Page titles and Previous/Next buttons provide navigation without a numbered header.
  scroll=new ScrollView(this);scroll.setTag("step-scroll");scroll.setFillViewport(true);body=column();body.setPadding(0,dp(12),0,dp(24));scroll.addView(body);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
  if(resultScreen&&result!=null){if(resultPage==5)renderResults();else renderDetail();}else renderEditor();building=false;updateTotal();root.requestApplyInsets();
  final int restoreY=pendingScrollY;pendingScrollY=0;scroll.post(()->scroll.scrollTo(0,Math.max(0,restoreY)));
  if(animateStep&&android.animation.ValueAnimator.areAnimatorsEnabled()){body.setAlpha(0f);body.setTranslationY(dp(10)*stepDirection);body.animate().alpha(1f).translationY(0).setDuration(180).start();}animateStep=false;
 }
 private void settings(){
  LinearLayout box=column();box.setPadding(dp(20),dp(8),dp(20),dp(12));
  ScrollView menuScroll=new ScrollView(this);menuScroll.addView(box);AlertDialog dialog=new AlertDialog.Builder(this).setTitle(t("Settings","Ρυθμίσεις")).setView(menuScroll).setPositiveButton(t("Close","Κλείσιμο"),null).create();
  box.addView(button(t("Language: English","Γλώσσα: Ελληνικά"),"language",()->{dialog.dismiss();new AlertDialog.Builder(this).setTitle("Language / Γλώσσα").setItems(new String[]{"English","Ελληνικά"},(d,i)->{greek=i==1;save();rerenderPreservingScroll();}).show();},false));
  LinearLayout appearance=column();description(appearance,t("Appearance","Εμφάνιση"));
  RadioGroup modes=new RadioGroup(this);modes.setOrientation(RadioGroup.HORIZONTAL);
  String[] keys={"light","dark","system"},labels={t("Light","Φωτεινό"),t("Dark","Σκοτεινό"),t("System","Σύστημα")};
  for(int i=0;i<keys.length;i++){RadioButton option=new RadioButton(this);option.setId(View.generateViewId());option.setTag("theme-"+keys[i]);option.setText(labels[i]);option.setTextColor(INK);option.setTextSize(13);option.setMinHeight(dp(48));option.setButtonTintList(android.content.res.ColorStateList.valueOf(TEAL));modes.addView(option,new RadioGroup.LayoutParams(0,-2,1));option.setChecked(keys[i].equals(themeMode));}
  modes.setOnCheckedChangeListener((group,id)->{RadioButton selected=group.findViewById(id);if(selected==null)return;String mode=selected.getTag().toString().substring(6);if(mode.equals(themeMode))return;getPreferences(0).edit().putString("appearance",mode).apply();save();dialog.dismiss();recreate();});
  appearance.addView(modes);box.addView(appearance);
  Button audio=button("","sound",()->{},false);Runnable label=()->{audio.setText(t("Sounds: ","Ήχοι: ")+(sounds?t("on","ενεργοί"):t("off","ανενεργοί")));audio.setContentDescription(t("Sound "+(sounds?"on":"off"),"Ήχος "+(sounds?"ενεργός":"ανενεργός")));};label.run();audio.setOnClickListener(v->{sounds=!sounds;if(sounds)feedback("success");save();label.run();});box.addView(audio);
  box.addView(button(t("Walkthrough","Οδηγός χρήσης"),"walkthrough",()->{dialog.dismiss();WalkthroughDialog.create(greek).show(getFragmentManager(),"walkthrough");},false));
  box.addView(button(t("Sources & privacy","Πηγές και απόρρητο"),"sources",()->{dialog.dismiss();sources();},false));
  if(type==0||type==1){box.addView(button(t("Refresh polls now","Ενημέρωση δημοσκοπήσεων"),"refresh-polls",()->{dialog.dismiss();refreshPolls(true);},false));CheckBox auto=new CheckBox(this);auto.setText(t("Refresh polls every 6 hours","Ενημέρωση ανά 6 ώρες"));auto.setTextColor(INK);auto.setChecked(polls.enabled());auto.setOnCheckedChangeListener((v,c)->polls.setEnabled(c));box.addView(auto);}
  spaceControls(box);dialog.show();
 }
 private void spaceControls(LinearLayout box){for(int i=0;i<box.getChildCount();i++){View child=box.getChildAt(i);LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(-1,-2);params.topMargin=i==0?0:dp(12);child.setLayoutParams(params);}}
 private void scenarioMenu(){
  LinearLayout box=column();box.setPadding(dp(20),dp(8),dp(20),dp(12));
  ScrollView menuScroll=new ScrollView(this);menuScroll.addView(box);AlertDialog dialog=new AlertDialog.Builder(this).setTitle(t("Scenario","Σενάριο")).setView(menuScroll).setNegativeButton(t("Close","Κλείσιμο"),null).create();
  box.addView(button(t("Save scenario","Αποθήκευση σεναρίου"),"save",()->{dialog.dismiss();saveScenario();},false));
  box.addView(button(t("Open scenario","Άνοιγμα σεναρίου"),"open",()->{dialog.dismiss();openScenario();},false));
  box.addView(button(t("Clear values","Μηδενισμός"),"clear",()->{dialog.dismiss();selectedParties.clear();government.clear();if(activePoll!=null)pollEdited=true;for(String id:values.keySet())values.put(id,"0");winner="";tieOrder.clear();transfers.clear();render();},false));spaceControls(box);dialog.show();
 }
 private void renderEditor(){
  if(editorStep==4){renderInputs();return;}
  if(editorStep==3){renderLaw();return;}
  if(editorStep==2){renderPartyList();return;}
  pageTitle(t("Choose your election","Επιλέξτε εκλογές"));
  description(body,t("Start with the election you want to explore.","Ξεκινήστε επιλέγοντας το είδος εκλογών."));
  LinearLayout types=column();String[] names={t("Parliament","Βουλευτικές"),t("European","Ευρωεκλογές"),t("Local","Αυτοδιοικητικές"),t("Custom","Προσαρμοσμένες")};
  for(int j=0;j<2;j++){LinearLayout r=row();for(int k=0;k<2;k++){final int index=j*2+k;Button choice=button(names[index],"type-"+index,()->switchType(index),false);choice.setSelected(type==index);if(type==index){choice.setTextColor(TEAL);choice.setBackground(bg(SELECTED,12));}weighted(r,choice);}types.addView(r);}body.addView(types);
  space(body,20);stepButton(body,t("Next →","Επόμενο →"),"next-step",()->goToStep(type==3?3:2),true);
 }
 private void pageTitle(String title){pageTitle(body,title);}
 private void pageTitle(LinearLayout parent,String title){TextView heading=text(title,28,INK,true);heading.setTag("page-title");parent.addView(heading);space(parent,8);}
 private void renderPartyList(){
  stepButton(body,t("← Previous","← Προηγούμενο"),"previous-step",()->goToStep(1),false);
  pageTitle(t("Choose your party list","Επιλέξτε λίστα κομμάτων"));
  description(body,type<=1?t("Choose a previous election or a poll, then select its results. You can edit the votes later.","Επιλέξτε προηγούμενες εκλογές ή δημοσκόπηση και στη συνέχεια τα αποτελέσματα. Θα μπορείτε να αλλάξετε τις ψήφους αργότερα."):t("Choose a previous election to load its editable results.","Επιλέξτε προηγούμενες εκλογές για να φορτώσετε και να επεξεργαστείτε τα αποτελέσματα."));
  LinearLayout setup=card(body,null);
  sourceButton(setup,t("Previous election","Προηγούμενες εκλογές"),"source-election","election");
  if(type<=1){space(setup,12);sourceButton(setup,t("Use a poll","Χρήση δημοσκόπησης"),"source-poll","poll");}
  if(!sourceMode.isEmpty())space(setup,16);
  if(sourceMode.equals("election"))setup.addView(button(sourceReady?title()+" ▾":t("Choose election ▾","Επιλογή εκλογών ▾"),"dataset",this::chooseDataset,false));
  if(sourceMode.equals("poll")){
   setup.addView(button(sourceReady?title()+" ▾":t("Choose poll ▾","Επιλογή δημοσκόπησης ▾"),"polls",this::choosePoll,false));space(setup,12);
   if(type==1)description(setup,t("Use national polls as European-election scenarios with 21 seats. These are not European-election polls.","Χρήση εθνικών δημοσκοπήσεων ως σενάρια ευρωεκλογών με 21 έδρες. Δεν πρόκειται για δημοσκοπήσεις ευρωεκλογών."));
   pollStatus=text(pollStatusText(),12,MUTED,false);pollStatus.setTag("poll-status");setup.addView(pollStatus);
  }
  stepButton(body,t("Next →","Επόμενο →"),"next-step",()->{if(sourceReady)goToStep(3);},true);
  View next=body.findViewWithTag("next-step");next.setEnabled(sourceReady);next.setAlpha(sourceReady?1f:0.4f);
 }
 private void sourceButton(LinearLayout parent,String label,String tag,String mode){
  Button choice=button(label,tag,()->{if(!sourceMode.equals(mode)){sourceMode=mode;sourceReady=false;render();save();}},false);
  choice.setSelected(sourceMode.equals(mode));if(choice.isSelected()){choice.setTextColor(TEAL);choice.setBackground(bg(SELECTED,12));}parent.addView(choice);

 }
 private void renderLaw(){
  stepButton(body,t("← Previous","← Προηγούμενο"),"previous-step",()->goToStep(type==3?1:2),false);
  pageTitle(t("Choose the electoral law","Επιλέξτε εκλογικό νόμο"));
  LinearLayout setup=card(body,null);
  if(type!=3){setup.addView(button(ruleName()+"  ▾","law",this::chooseLaw,false));space(setup,12);}
  LinearLayout metrics=row();LinearLayout seatBox=column();seatBox.addView(text(""+seats(),36,INK,true));seatBox.addView(text(t("total seats","συνολικές έδρες"),12,MUTED,false));weighted(metrics,seatBox);LinearLayout thresholdBox=column();thresholdBox.addView(text(type==3?custom[1]+"%":"3%",28,TEAL,true));thresholdBox.addView(text(t("entry threshold","εκλογικό όριο"),12,MUTED,false));weighted(metrics,thresholdBox);setup.addView(metrics);space(setup,14);
  description(setup,ruleName());
  if(type==0){description(setup,!parliamentReinforced?t("Simple proportional representation. No bonus.","Απλή αναλογική. Χωρίς μπόνους."):t("20 bonus seats at 25%, then +1 for each 0.5 percentage point. Maximum 50 at 40%.","20 έδρες μπόνους στο 25%, και +1 ανά 0,5 ποσοστιαία μονάδα. Μέγιστο 50 στο 40%."));description(setup,t("National seat entitlement. Constituency exceptions require district-level data.","Εθνική κατανομή εδρών. Εξαιρέσεις εκλογικών περιφερειών απαιτούν τοπικά δεδομένα."));}
  if(type==1)description(setup,t("Greece elects 21 MEPs. Proportional allocation with an integer quota and unused remainders; no bonus.","Η Ελλάδα εκλέγει 21 ευρωβουλευτές. Αναλογική κατανομή με ακέραιο μέτρο και αχρησιμοποίητα υπόλοιπα, χωρίς μπόνους."));
  if(type==2){
   description(setup,future?t("First-stage winner: above 42%. Otherwise alternative votes determine the winner. Seats use first preferences. 2023 lists are a scenario starting point; future ballots are not declared.","Νικητής πρώτου σταδίου: πάνω από 42%. Διαφορετικά προσμετρώνται εναλλακτικές ψήφοι. Οι έδρες βασίζονται στις αρχικές ψήφους. Οι συνδυασμοί του 2023 είναι αφετηρία σεναρίου, όχι μελλοντικό ψηφοδέλτιο."):t("Above 43% wins in round one. Otherwise choose the runoff winner. The winner receives rounded 3/5 of seats up to 60%; above 60% all seats are proportional.","Πάνω από 43%: νίκη στον α΄ γύρο. Αλλιώς επιλέξτε νικητή β΄ γύρου. Έως 60% λαμβάνει στρογγυλοποιημένα τα 3/5 των εδρών· πάνω από 60% όλες οι έδρες κατανέμονται αναλογικά."));
   setup.addView(button(winner.isEmpty()?t("Winner: automatic / choose ▾","Νικητής: αυτόματα / επιλογή ▾"):t("Winner: ","Νικητής: ")+partyName(winner),"winner",this::chooseWinner,false));
   if(future){space(setup,8);setup.addView(button(t("Alternative-vote transfers","Μεταφορά εναλλακτικών ψήφων"),"transfers",this::editTransfers,false));}
  }
  if(type==3)customFields(setup);
  space(body,10);stepButton(body,t("Next →","Επόμενο →"),"next-step",()->goToStep(4),true);
 }
 // Avoid a departing dim surface swallowing rapid taps on the newly rendered page.
 private void showSelectionDialog(AlertDialog dialog){dialog.getWindow().setWindowAnimations(0);dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);dialog.show();}
 private void chooseLaw(){
  String[] options=type==0?new String[]{t("Current · Law 4654/2020","Ισχύων · Ν. 4654/2020"),t("Historical · Law 4406/2016","Παλαιότερος · Ν. 4406/2016")}:type==2?new String[]{t("Current · Laws 5314/2026, 5321/2026","Ισχύοντες · Ν. 5314/2026, 5321/2026"),t("Historical · Law 4804/2021","Παλαιότερος · Ν. 4804/2021")}:new String[]{t("Current · Laws 4255/2014, 5083/2024","Ισχύοντες · Ν. 4255/2014, 5083/2024")};
  AlertDialog dialog=new AlertDialog.Builder(this).setTitle(t("Electoral law","Εκλογικός νόμος")).setSingleChoiceItems(options,type==0?(parliamentReinforced?0:1):type==2?(future?0:1):0,(d,i)->{if(type==0)parliamentReinforced=i==0;if(type==2)future=i==0;winner="";transfers.clear();tieOrder.clear();result=null;government.clear();d.dismiss();}).setNegativeButton(t("Cancel","Ακύρωση"),null).create();
  dialog.setOnDismissListener(d->{render();save();});showSelectionDialog(dialog);
 }
 private void renderInputs(){
  stepButton(body,t("← Previous","← Προηγούμενο"),"previous-step",()->goToStep(3),false);
  LinearLayout heading=row();TextView votesTitle=text(t("Enter votes","Εισαγωγή ψήφων"),26,INK,true);votesTitle.setTag("page-title");heading.addView(votesTitle,new LinearLayout.LayoutParams(0,-2,1));Button menu=button(t("More ▾","Άλλα ▾"),"scenario-menu",this::scenarioMenu,false);menu.setContentDescription(t("Scenario actions","Ενέργειες σεναρίου"));heading.addView(menu,new LinearLayout.LayoutParams(-2,-2));body.addView(heading);description(body,title());
  LinearLayout inputs=card(body,null);if(type>=2){LinearLayout ur=row();weighted(ur,button(t("Percentages %","Ποσοστά %"),"percent",()->changeUnits(true),false));weighted(ur,button(t("Vote counts","Ψήφοι"),"votes",()->changeUnits(false),false));for(int i=0;i<ur.getChildCount();i++){boolean chosen=percent?i==0:i==1;ur.getChildAt(i).setSelected(chosen);if(chosen){ur.getChildAt(i).setBackground(bg(SELECTED,12));((Button)ur.getChildAt(i)).setTextColor(TEAL);}}inputs.addView(ur);space(inputs,12);}
  totalLabel=text("",15,TEAL,true);totalLabel.setTag("input-total");inputs.addView(totalLabel);space(inputs,10);
  if(parties.isEmpty())description(inputs,t("Add your first party to begin.","Προσθέστε το πρώτο σας κόμμα για να ξεκινήσετε."));
  List<JSONObject> visibleParties=new ArrayList<>();for(JSONObject p:parties)if(!dropdownMode()||selectedParties.contains(p.optString("id")))visibleParties.add(p);
  if(dropdownMode())description(inputs,t("Select parties from the list below. Any unentered share up to 100% becomes rest parties, each assumed below 3% and excluded from seats.","Επιλέξτε κόμματα από τη λίστα. Το υπόλοιπο έως το 100% θεωρείται λοιπά κόμματα, καθένα κάτω από 3%, χωρίς έδρες."));
  int shown=expanded?visibleParties.size():Math.min(visibleParties.size(),8);for(int i=0;i<shown;i++)partyRow(inputs,visibleParties.get(i));
  if(visibleParties.size()>8){space(inputs,8);inputs.addView(button(expanded?t("Show fewer lists","Λιγότεροι συνδυασμοί"):t("Show all "+parties.size()+" lists","Όλοι οι "+parties.size()+" συνδυασμοί"),"expand",()->{expanded=!expanded;rerenderPreservingScroll();},false));}
  space(inputs,10);inputs.addView(button(t("＋ Add party","＋ Προσθήκη κόμματος"),"add",()->{if(dropdownMode())chooseParty();else editParty(null);},false));
  error=text("",14,ERROR,true);error.setTag("error");error.setVisibility(View.GONE);inputs.addView(error);space(inputs,14);inputs.addView(button(t("Calculate seats →","Υπολογισμός εδρών →"),"calculate",this::calculate,true));
 }
 private void rerenderPreservingScroll(){pendingScrollY=scroll==null?0:scroll.getScrollY();render();}
 private void partyRow(LinearLayout parent,JSONObject p){LinearLayout r=row();r.setPadding(0,dp(6),0,dp(6));View dot=new View(this);dot.setBackground(bg(Color.parseColor(p.optString("color","#64748B")),5));r.addView(dot,new LinearLayout.LayoutParams(dp(5),dp(34)));
  TextView name=text(name(p),14,INK,true);name.setPadding(dp(10),dp(8),dp(8),dp(8));name.setMinHeight(dp(48));name.setGravity(Gravity.CENTER_VERTICAL);name.setOnClickListener(v->editParty(p));name.setContentDescription(name(p)+t(". Party details",". Στοιχεία συνδυασμού"));r.addView(name,new LinearLayout.LayoutParams(0,-2,1));
  String partyId=p.optString("id");boolean compactPercentage=type<=1&&percent;
  EditText field=field(compactPercentage?displayPercentage(values.get(partyId)):values.get(partyId),name(p)+(percent?" %":t(" votes"," ψήφοι")),"value-"+partyId);
  // Keep exact imported shares in the model; rounding is only for the inactive field.
  boolean[] formatting={false};
  if(compactPercentage)field.setOnFocusChangeListener((v,focused)->{
   formatting[0]=true;
   field.setText(focused?values.get(partyId):displayPercentage(values.get(partyId)));
   if(focused)field.selectAll();
   formatting[0]=false;
  });field.setGravity(Gravity.END|Gravity.CENTER_VERTICAL);field.setSelectAllOnFocus(true);r.addView(field,new LinearLayout.LayoutParams(dp(getResources().getConfiguration().screenWidthDp<380?80:104),dp(52)));watch(field,s->{if(formatting[0])return;values.put(p.optString("id"),s);if(activePoll!=null)pollEdited=true;government.clear();tieOrder.clear();updateTotal();});if(dropdownMode()){Button remove=button("×","remove-"+p.optString("id"),()->{selectedParties.remove(p.optString("id"));values.put(p.optString("id"),"0");government.clear();tieOrder.clear();if(activePoll!=null)pollEdited=true;rerenderPreservingScroll();},false);remove.setContentDescription(t("Remove ","Αφαίρεση ")+name(p));r.addView(remove,new LinearLayout.LayoutParams(dp(48),dp(48)));}parent.addView(r);View line=new View(this);line.setBackgroundColor(LINE);parent.addView(line,new LinearLayout.LayoutParams(-1,dp(1)));}
 // Preserve unfinished/invalid input so the existing validation can explain it.
 private String displayPercentage(String value){
  if(value==null||value.trim().isEmpty())return value;
  try{return number(value).setScale(2,RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();}
  catch(IllegalArgumentException invalid){return value;}
 }
 private EditText field(String value,String hint,String tag){EditText e=new EditText(this);e.setSingleLine(true);e.setText(value==null?"":value);e.setHint(hint);e.setContentDescription(hint);e.setTag(tag);e.setTextSize(15);e.setTextColor(INK);e.setHintTextColor(MUTED);e.setInputType(android.text.InputType.TYPE_CLASS_NUMBER|android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);e.setPadding(dp(12),dp(8),dp(12),dp(8));GradientDrawable d=bg(BG,10);d.setStroke(dp(1),LINE);e.setBackground(d);e.setMinHeight(dp(52));return e;}
 private void watch(EditText e,java.util.function.Consumer<String> c){e.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int count,int after){}public void onTextChanged(CharSequence s,int start,int before,int count){if(!building)c.accept(s.toString());}public void afterTextChanged(Editable s){}});}
 private BigDecimal number(String s){try{return new BigDecimal(s.trim().replace(',','.'));}catch(Exception e){throw new IllegalArgumentException("NUMBER");}}
 private boolean remainderMode(){return percent&&(type!=3||number(custom[1]).compareTo(new BigDecimal("3"))==0);}
 private boolean dropdownMode(){return percent&&type!=3;}
 private void updateTotal(){if(totalLabel==null)return;try{BigDecimal sum=BigDecimal.ZERO;for(String v:values.values())sum=sum.add(number(v));BigDecimal rest=new BigDecimal("100").subtract(sum);String label=t("Entered: ","Σύνολο: ")+sum.stripTrailingZeros().toPlainString()+(percent?" / 100%":t(" valid votes"," έγκυρες ψήφοι"));if(remainderMode()&&rest.signum()>=0)label+="\n"+t("Rest parties (<3% each): ","Λοιπά κόμματα (<3% έκαστο): ")+rest.stripTrailingZeros().toPlainString()+"%";totalLabel.setText(label);totalLabel.setTextColor(percent&&rest.signum()<0?ERROR:TEAL);}catch(Exception e){totalLabel.setText(t("Check numeric inputs","Ελέγξτε τους αριθμούς"));}}
 private void chooseParty(){List<JSONObject> available=new ArrayList<>();for(JSONObject p:parties)if(!selectedParties.contains(p.optString("id")))available.add(p);LinearLayout content=column();content.setPadding(dp(16),dp(4),dp(16),0);ListView list=new ListView(this);content.addView(list,new LinearLayout.LayoutParams(-1,dp(Math.min(420,getResources().getConfiguration().screenHeightDp-180))));ArrayAdapter<JSONObject> adapter=new ArrayAdapter<JSONObject>(this,android.R.layout.simple_list_item_1,available){@Override public View getView(int position,View convert,android.view.ViewGroup parent){LinearLayout line=row();line.setPadding(dp(8),dp(4),dp(8),dp(4));View dot=new View(MainActivity.this);dot.setBackground(bg(Color.parseColor(getItem(position).optString("color","#64748B")),5));line.addView(dot,new LinearLayout.LayoutParams(dp(8),dp(38)));TextView label=text(name(getItem(position)),15,INK,true);label.setGravity(Gravity.CENTER_VERTICAL);label.setPadding(dp(12),0,0,0);line.addView(label,new LinearLayout.LayoutParams(-1,dp(52)));return line;}};list.setAdapter(adapter);AlertDialog dialog=new AlertDialog.Builder(this).setTitle(t("Choose party ▾","Επιλογή κόμματος ▾")).setView(content).setNeutralButton(t("New party","Νέο κόμμα"),(d,w)->editParty(null)).setNegativeButton(t("Close","Κλείσιμο"),null).create();dialog.getWindow().setWindowAnimations(0);dialog.setOnShowListener(x->dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND));list.setOnItemClickListener((parent,view,position,id)->{feedback("tap");int y=scroll==null?0:scroll.getScrollY();selectedParties.add(available.get(position).optString("id"));expanded=true;pendingScrollY=y;dialog.setOnDismissListener(d->render());dialog.dismiss();});dialog.show();}
 private void switchType(int index){if(index==type)return;type=index;if(type<=1)percent=true;sourceMode="";sourceReady=false;editorStep=1;parliamentReinforced=true;resultPage=5;activePoll=null;government.clear();selectedParties.clear();if(index==3){customBonus=false;parties.clear();values.clear();winner="";transfers.clear();tieOrder.clear();resultScreen=false;render();return;}future=index==2;loadDataset(index==0?"june":index==1?"eu":"municipality-9186");if(index==2&&!dataset.optString("kind").equals("municipality")){loadDataset(repository.ofType(2).get(0).optString("id"));}render();}
 private void chooseDataset(){List<JSONObject> all=repository.ofType(type);LinearLayout layout=column();layout.setPadding(dp(20),dp(8),dp(20),0);EditText search=new EditText(this);search.setSingleLine();search.setHint(type==2?t("Search municipality or region","Αναζήτηση δήμου ή περιφέρειας"):t("Search election","Αναζήτηση εκλογών"));layout.addView(search);ListView list=new ListView(this);layout.addView(list,new LinearLayout.LayoutParams(-1,dp(360)));List<JSONObject> shown=new ArrayList<>(all);ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1);Runnable refresh=()->{adapter.clear();for(JSONObject e:shown)adapter.add((e.optString("kind").equals("region")?t("Region: ","Περιφέρεια: "):"")+e.optString(greek?"el":"en"));};refresh.run();list.setAdapter(adapter);AlertDialog dialog=new AlertDialog.Builder(this).setTitle(t("Choose election","Επιλογή εκλογών")).setView(layout).setNegativeButton(t("Cancel","Ακύρωση"),null).create();watch(search,s->{shown.clear();for(JSONObject e:all)if((e.optString("en")+e.optString("el")).toLowerCase(Locale.ROOT).contains(s.toLowerCase(Locale.ROOT)))shown.add(e);refresh.run();});list.setOnItemClickListener((a,v,p,id)->{loadHistoricalResults(shown.get(p).optString("id"));dialog.dismiss();});dialog.setOnDismissListener(d->{render();save();});showSelectionDialog(dialog);}
 private String partyName(String id){for(JSONObject p:parties)if(p.optString("id").equals(id))return name(p);return id;}
 private void chooseWinner(){List<String> labels=new ArrayList<>();labels.add(t("Automatic","Αυτόματα"));for(JSONObject p:parties)labels.add(name(p));new AlertDialog.Builder(this).setTitle(t("Declared winner","Δηλωμένος νικητής")).setItems(labels.toArray(new String[0]),(d,i)->{winner=i==0?"":parties.get(i-1).optString("id");transfers.clear();render();}).show();}
 private void changeUnits(boolean target){if(type<=1||percent==target)return;boolean nonzero=values.values().stream().anyMatch(s->!s.equals("0"));if(nonzero){new AlertDialog.Builder(this).setTitle(t("Change input format?","Αλλαγή μορφής εισαγωγής;")).setMessage(t("This clears the current values. Percentages cannot recover exact vote counts. Saved scenarios are retained.","Οι τρέχουσες τιμές θα μηδενιστούν. Τα ποσοστά δεν δίνουν ακριβείς ψήφους. Τα αποθηκευμένα σενάρια διατηρούνται.")).setPositiveButton(t("Clear & switch","Μηδενισμός & αλλαγή"),(d,w)->{percent=target;for(String id:values.keySet())values.put(id,"0");selectedParties.clear();if(activePoll!=null)pollEdited=true;winner="";transfers.clear();tieOrder.clear();render();}).setNegativeButton(t("Cancel","Ακύρωση"),null).show();}else{percent=target;render();}}
 private void customFields(LinearLayout box){
  String[] labels={t("Total seats","Συνολικές έδρες"),t("Entry threshold (%)","Εκλογικό όριο (%)"),t("Bonus starts at (%)","Έναρξη μπόνους (%)"),t("Base bonus at start","Αρχικό μπόνους"),t("Percentage points per step","Ποσοστιαίες μονάδες ανά βήμα"),t("Seats gained per step","Έδρες ανά βήμα"),t("Maximum bonus","Μέγιστο μπόνους")};
  for(int i=0;i<custom.length;i++){
   if(i==2){Switch bonus=new Switch(this);bonus.setTag("custom-bonus");bonus.setText(t("Bonus seats","Έδρες μπόνους"));bonus.setTextColor(INK);bonus.setMinHeight(dp(48));bonus.setTextOn(t("Yes","Ναι"));bonus.setTextOff(t("No","Όχι"));bonus.setShowText(true);bonus.setChecked(customBonus);bonus.setOnCheckedChangeListener((v,on)->{customBonus=on;feedback("tap");rerenderPreservingScroll();save();});box.addView(bonus);space(box,12);if(!customBonus)break;}
   box.addView(text(labels[i],12,MUTED,true));space(box,5);final int index=i;EditText e=field(custom[i],labels[i],"custom-"+i);watch(e,s->custom[index]=s);box.addView(e);space(box,10);
  }
  description(box,customBonus?t("Example: start 20%, base 0, step 5, seats per step 1. At 35%, bonus = 3.","Παράδειγμα: έναρξη 20%, αρχικό 0, βήμα 5, έδρες ανά βήμα 1. Στο 35% το μπόνους είναι 3."):t("Simple proportional representation. No bonus seats.","Απλή αναλογική. Χωρίς έδρες μπόνους."));
 }
 private int candidateCount(JSONObject party){
  String raw=party.optString("candidates","").trim();
  try{if(!raw.matches("[0-9]+"))throw new NumberFormatException();return Integer.parseInt(raw);}catch(NumberFormatException invalid){throw new IllegalArgumentException("CANDIDATES");}
 }
 private void editCustomParty(JSONObject original){
  LinearLayout box=column();box.setPadding(dp(24),dp(8),dp(24),dp(12));
  EditText partyName=new EditText(this);partyName.setTag("party-name");partyName.setHint(t("Party name","Όνομα κόμματος"));partyName.setSingleLine();partyName.setText(original==null?"":name(original));box.addView(partyName);space(box,12);
  box.addView(text(t("Number of candidates","Αριθμός υποψηφίων"),13,INK,true));space(box,5);
  EditText candidates=field(original==null?"":original.optString("candidates",""),t("Number of candidates","Αριθμός υποψηφίων"),"candidates");candidates.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);box.addView(candidates);
  description(box,t("A party cannot fill more seats than it has candidates.","Ένα κόμμα δεν μπορεί να καλύψει περισσότερες έδρες από τους υποψηφίους του."));
  ScrollView content=new ScrollView(this);content.addView(box);
  AlertDialog dialog=new AlertDialog.Builder(this).setTitle(t("Party","Κόμμα")).setView(content).setPositiveButton(t("Save","Αποθήκευση"),null).setNegativeButton(t("Cancel","Ακύρωση"),null).create();
  if(original!=null)dialog.setButton(AlertDialog.BUTTON_NEUTRAL,t("Remove","Αφαίρεση"),(d,w)->{parties.remove(original);values.remove(original.optString("id"));selectedParties.remove(original.optString("id"));tieOrder.clear();rerenderPreservingScroll();save();});
  dialog.setOnShowListener(d->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
   String entered=partyName.getText().toString().trim();
   if(entered.isEmpty()){partyName.setError(t("Enter a party name","Εισαγάγετε όνομα κόμματος"));return;}
   for(JSONObject party:parties)if(party!=original&&(party.optString("en").equalsIgnoreCase(entered)||party.optString("el").equalsIgnoreCase(entered))){partyName.setError(t("Use a unique party name","Χρησιμοποιήστε μοναδικό όνομα κόμματος"));return;}
   String raw=candidates.getText().toString().trim();int count;
   try{if(!raw.matches("[0-9]+"))throw new NumberFormatException();count=Integer.parseInt(raw);}catch(NumberFormatException invalid){candidates.setError(t("Enter a non-negative whole number","Εισαγάγετε μη αρνητικό ακέραιο"));return;}
   try{JSONObject party=original==null?new JSONObject():original;party.put("en",entered).put("el",entered).put("members",1).put("candidates",count);if(original==null){String id="custom-"+UUID.randomUUID();party.put("id",id).put("color",new String[]{"#2979D0","#08A88A","#E56B6F","#9163C7","#D5A631"}[parties.size()%5]);parties.add(party);values.put(id,"0");selectedParties.add(id);}expanded=true;tieOrder.clear();dialog.dismiss();feedback("success");rerenderPreservingScroll();save();}catch(JSONException invalid){throw new IllegalStateException(invalid);}
  }));dialog.show();
 }
 private void editParty(JSONObject original){if(type==3){editCustomParty(original);return;}LinearLayout l=column();l.setPadding(dp(24),dp(8),dp(24),0);EditText en=new EditText(this),el=new EditText(this);en.setHint(original==null?t("Party name","Όνομα κόμματος"):t("English name","Αγγλικό όνομα"));if(original==null)en.setTag("party-name");el.setHint(t("Greek name","Ελληνικό όνομα"));en.setSingleLine();el.setSingleLine();en.setText(original==null?"":original.optString("en"));el.setText(original==null?"":original.optString("el"));l.addView(en);if(original!=null)l.addView(el);l.addView(text(t("Coalition member parties (1 = standalone)","Κόμματα συνασπισμού (1 = αυτοτελές)"),13,MUTED,false));EditText members=field(original==null?"1":""+original.optInt("members",1),"1","members");l.addView(members);if(original!=null)description(l,original.optString(greek?"fullEl":"fullEn"));
  AlertDialog d=new AlertDialog.Builder(this).setTitle(t("Party / electoral list","Κόμμα / συνδυασμός")).setView(l).setPositiveButton(t("Save","Αποθήκευση"),null).setNegativeButton(t("Cancel","Ακύρωση"),null).setNeutralButton(original==null?"":t("Remove","Αφαίρεση"),original==null?null:(x,w)->{parties.remove(original);values.remove(original.optString("id"));tieOrder.clear();render();}).create();d.setOnShowListener(x->d.getButton(-1).setOnClickListener(v->{try{String a=en.getText().toString().trim(),b=original==null?a:el.getText().toString().trim();if(a.isEmpty()&&b.isEmpty())throw new Exception();if(a.isEmpty())a=b;if(b.isEmpty())b=a;for(JSONObject p:parties)if(p!=original&&(p.optString("en").equalsIgnoreCase(a)||p.optString("el").equalsIgnoreCase(b)))throw new Exception();int count=Integer.parseInt(members.getText().toString());if(count<1||count>100)throw new Exception();JSONObject p=original==null?new JSONObject():original;p.put("en",a).put("el",b).put("members",count);if(original==null){String id="custom-"+UUID.randomUUID();p.put("id",id).put("color",new String[]{"#2979D0","#08A88A","#E56B6F","#9163C7","#D5A631"}[parties.size()%5]);parties.add(p);values.put(id,"0");selectedParties.add(id);}expanded=true;tieOrder.clear();d.dismiss();feedback("success");rerenderPreservingScroll();}catch(Exception bad){en.setError(t("Use a unique name and valid member count","Μοναδικό όνομα και έγκυρος αριθμός κομμάτων"));}}));d.show();}
 private void editTransfers(){LinearLayout l=column();l.setPadding(dp(20),dp(8),dp(20),dp(12));description(l,t("Enter transfers to finalists only, from lists eliminated after first preferences. Use the same units as your results. Leave every field blank to use a declared winner.","Μόνο μεταφορές προς φιναλίστ, από αποκλεισμένους συνδυασμούς. Ίδιες μονάδες με τα αποτελέσματα. Κενά πεδία για χρήση δηλωμένου νικητή."));Map<String,EditText> fields=new LinkedHashMap<>();for(JSONObject p:parties){l.addView(text(name(p),13,INK,true));EditText e=field(transfers.getOrDefault(p.optString("id"),""),t("Transferred votes / percentage","Μεταφερόμενες ψήφοι / ποσοστό"),"transfer-"+p.optString("id"));fields.put(p.optString("id"),e);l.addView(e);space(l,8);}ScrollView sv=new ScrollView(this);sv.addView(l);new AlertDialog.Builder(this).setTitle(t("Alternative votes","Εναλλακτικές ψήφοι")).setView(sv).setPositiveButton(t("Apply","Εφαρμογή"),(d,w)->{transfers.clear();for(String id:fields.keySet()){String s=fields.get(id).getText().toString();if(!s.trim().isEmpty())transfers.put(id,s);}winner="";}).setNegativeButton(t("Cancel","Ακύρωση"),null).show();}
 private ElectionEngine.Input input(){ElectionEngine.Rule rule;if(type==3)rule=ElectionEngine.Rule.custom(Integer.parseInt(custom[0]),number(custom[1]).toPlainString(),customBonus?number(custom[2]).toPlainString():"100",customBonus?Integer.parseInt(custom[3]):0,customBonus?number(custom[4]).toPlainString():"1",customBonus?Integer.parseInt(custom[5]):0,customBonus?Integer.parseInt(custom[6]):0);else if(type==2)rule=future?ElectionEngine.Rule.local2026(seats()):ElectionEngine.Rule.local(seats());else if(type==1)rule=ElectionEngine.Rule.european();else rule=ElectionEngine.Rule.parliament(parliamentReinforced);List<ElectionEngine.Party> rows=new ArrayList<>();for(JSONObject p:parties)rows.add(type==3?new ElectionEngine.Party(p.optString("id"),number(values.get(p.optString("id"))),1,candidateCount(p)):new ElectionEngine.Party(p.optString("id"),number(values.get(p.optString("id"))),p.optInt("members",1)));ElectionEngine.Input in=new ElectionEngine.Input(rule,rows,percent);Scenario.completePercentages(in);if(!winner.isEmpty())in.winnerId=winner;in.tieOrder=new ArrayList<>(tieOrder);for(String id:transfers.keySet())in.transfers.put(id,number(transfers.get(id)));return in;}
 private void calculate(){try{result=ElectionEngine.calculate(input());government.clear();resultScreen=true;resultPage=5;pendingScrollY=0;save();((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(root.getWindowToken(),0);sound(true);animateStep=true;stepDirection=1;render();}catch(ElectionEngine.TieException tie){error.setText(Explanation.error("TIE",greek));error.setVisibility(View.VISIBLE);String[] labels=tie.parties.stream().map(this::partyName).toArray(String[]::new);new AlertDialog.Builder(this).setTitle(t("Tie: declare lottery outcome","Ισοπαλία: δήλωση κλήρωσης")).setItems(labels,(d,i)->{tieOrder.add(tie.parties.get(i));calculate();}).setNegativeButton(t("Cancel","Ακύρωση"),null).show();}catch(IllegalArgumentException e){feedback("error");error.setText(Explanation.error(e instanceof NumberFormatException?"CONFIG":e.getMessage(),greek));error.setVisibility(View.VISIBLE);error.announceForAccessibility(error.getText());scroll.post(()->{android.graphics.Rect rect=new android.graphics.Rect();error.getDrawingRect(rect);error.requestRectangleOnScreen(rect,false);});}}
 private void renderResults(){stepButton(body,t("← Previous","← Προηγούμενο"),"previous-step",()->goToStep(4),false);space(body,20);LinearLayout results=column();results.setBackgroundColor(BG);results.setTag("share-results");body.addView(results);results.addView(text(t("politeia · THE SEAT PICTURE","πολιτεία · Η ΕΙΚΟΝΑ ΤΩΝ ΕΔΡΩΝ"),11,TEAL,true));space(results,6);pageTitle(results,t("Your results, explained.","Τα αποτελέσματα, με εξήγηση."));description(results,title()+" · "+ruleName());
  LinearLayout summary=card(results,null);TextView total=text(Arrays.stream(result.seats).sum()+t(" seats allocated"," έδρες κατανεμήθηκαν"),28,INK,true);total.setTag("seat-total");summary.addView(total);if(type==3){TextView vacant=text(result.vacantSeats+t(" vacant seats"," κενές έδρες"),16,MUTED,true);vacant.setTag("vacant-seats");summary.addView(vacant);}description(summary,(percent?t("Percentage estimate","Εκτίμηση ποσοστών"):t("Vote-count calculation","Υπολογισμός ψήφων"))+"  ·  "+t("Majority: ","Πλειοψηφία: ")+(seats()/2+1));
  if(type!=3)description(summary,t("Seat change vs ","Μεταβολή εδρών έναντι ")+dataset.optString(greek?"el":"en")+t(" (election result, not current membership)."," (εκλογικό αποτέλεσμα, όχι σημερινή σύνθεση)."));
  int[] colors=new int[parties.size()];for(int i=0;i<colors.length;i++)colors[i]=Color.parseColor(parties.get(i).optString("color","#64748B"));int[] chartSeats=result.seats,chartColors=colors;if(type==3&&result.vacantSeats>0){chartSeats=Arrays.copyOf(result.seats,result.seats.length+1);chartColors=Arrays.copyOf(colors,colors.length+1);chartSeats[result.seats.length]=result.vacantSeats;chartColors[colors.length]=LINE;}SeatChart chart=new SeatChart(this,chartSeats,chartColors,type==3?t("Seat chamber. Grey seats are vacant. Full accessible results below.","Διάγραμμα εδρών. Οι γκρίζες έδρες είναι κενές. Αναλυτικά αποτελέσματα παρακάτω."):t("Seat chamber. Full accessible results below.","Διάγραμμα εδρών. Αναλυτικά αποτελέσματα παρακάτω."));summary.addView(chart,new LinearLayout.LayoutParams(-1,dp(210)));space(summary,18);
  for(int i=0;i<parties.size();i++){if(result.seats[i]==0&&(type==3||historicalSeats(parties.get(i).optString("id"))==0))continue;final int index=i;LinearLayout r=row();TextView label=text(name(parties.get(i)),15,INK,true);r.addView(label,new LinearLayout.LayoutParams(0,-2,1));TextView number=text(""+result.seats[i],24,INK,true);number.setTag("seats-"+parties.get(i).optString("id"));r.addView(number);summary.addView(r);if(type!=3)seatChange(summary,parties.get(i).optString("id"),result.seats[i]);description(summary,result.percentages[i].setScale(2,RoundingMode.HALF_UP).toPlainString()+"%  ·  "+(result.seats[i]>=seats()/2+1?t("Majority","Πλειοψηφία"):t("Seats: ","Έδρες: ")+result.seats[i]));LinearLayout track=row();track.setBackground(bg(BG,3));View bar=new View(this);bar.setBackground(bg(colors[index],3));track.addView(bar,new LinearLayout.LayoutParams(0,dp(5),result.seats[i]));track.addView(new View(this),new LinearLayout.LayoutParams(0,dp(5),Math.max(0,seats()-result.seats[i])));summary.addView(track);space(summary,15);}
  // Polls can omit former seat holders; retain those losses instead of hiding them.
  if(type!=3){Set<String> represented=new HashSet<>();for(JSONObject party:parties)represented.add(historicalPartyId(party.optString("id")));
   JSONArray previous=dataset.optJSONArray("parties");for(int i=0;i<previous.length();i++){JSONObject party=previous.optJSONObject(i);String id=party.optString("id");if(party.optInt("seats")>0&&!represented.contains(id)){summary.addView(text(name(party),15,INK,true));seatChange(summary,id,0);description(summary,t("Not included in this scenario · 0 seats","Δεν περιλαμβάνεται στο σενάριο · 0 έδρες"));space(summary,12);}}
  }
  long zero=Arrays.stream(result.seats).filter(x->x==0).count();if(zero>0)description(summary,zero+t(" lists received no seats. Open the allocation explanation for details."," συνδυασμοί χωρίς έδρα. Δείτε λεπτομέρειες στην εξήγηση κατανομής."));
  if(type==0){body.addView(button(t("Simulate Government Coalition","Προσομοίωση κυβερνητικής συνεργασίας"),"government-page",()->openDetail(6),false));space(body,12);}
  body.addView(button(t("How the seats were allocated","Πώς κατανεμήθηκαν οι έδρες"),"explanation-page",()->openDetail(7),false));space(body,12);
  if(activePoll!=null){body.addView(button(t("Poll source & assumptions","Πηγή δημοσκόπησης & παραδοχές"),"poll-source-page",()->openDetail(8),false));space(body,12);}
  body.addView(button(t("Share results image","Κοινοποίηση εικόνας αποτελεσμάτων"),"share",()->shareResults(results),false));
 }
 private void shareResults(View results){
  View share=body.findViewWithTag("share");share.setEnabled(false);
  final android.graphics.Bitmap image;
  try{image=ResultsShare.capture(results);}catch(RuntimeException failure){share.setEnabled(true);shareError();return;}
  new Thread(()->{
   try{
    Intent send=ResultsShare.createIntent(this,image);
    runOnUiThread(()->{share.setEnabled(true);if(!isDestroyed()&&!isFinishing())try{startActivity(Intent.createChooser(send,t("Share results","Κοινοποίηση αποτελεσμάτων")));}catch(ActivityNotFoundException unavailable){shareError();}});
   }catch(java.io.IOException|RuntimeException failure){runOnUiThread(()->{share.setEnabled(true);if(!isDestroyed())shareError();});}
   finally{image.recycle();}
  },"share-results").start();
 }
 private void shareError(){Toast.makeText(this,t("Could not share the results image. Please try again.","Δεν ήταν δυνατή η κοινοποίηση της εικόνας. Δοκιμάστε ξανά."),Toast.LENGTH_LONG).show();}
 // Poll providers use slugs; official snapshots use numeric IDs. Never match by translated name.
 private String historicalPartyId(String id){
  switch(id){
   case "poll-nea-dimokratia":return "2";
   case "poll-syriza":return "4";
   case "poll-pasok":return "106";
   case "poll-kommounistiko":return "3";
   case "poll-elliniki-lysi":return "108";
   case "poll-niki":return "131";
   case "poll-plefsi-eleftherias":return "123";
   case "poll-spartiates":return "157";
   case "poll-foni-logikis":return "158";
   default:return id;
  }
 }
 private int historicalSeats(String id){
  JSONArray previous=dataset.optJSONArray("parties");String key=historicalPartyId(id);
  for(int i=0;i<previous.length();i++){JSONObject party=previous.optJSONObject(i);if(key.equals(party.optString("id")))return party.optInt("seats");}
  return 0; // A newly entered list has no seats in the selected election snapshot.
 }
 private void seatChange(LinearLayout parent,String id,int allocated){
  int delta=allocated-historicalSeats(id);String change=(delta>0?"+":delta<0?"−":"")+Math.abs(delta)+t(" seats"," έδρες");
  TextView badge=text(change,13,delta>0?TEAL:delta<0?ERROR:MUTED,true);
  badge.setTag("seat-change-"+id);badge.setPadding(dp(8),dp(4),dp(8),dp(4));badge.setBackground(bg(delta>0?SELECTED:delta<0?LOSS:BG,8));
  badge.setContentDescription(change+t(" compared with "," σε σύγκριση με ")+dataset.optString(greek?"el":"en"));
  parent.addView(badge,new LinearLayout.LayoutParams(-2,-2));
 }
 private void openDetail(int page){resultPage=page;pendingScrollY=0;stepDirection=1;animateStep=true;render();save();}
 private void backToSeats(){resultPage=5;pendingScrollY=0;stepDirection=-1;animateStep=true;feedback("back");render();save();}
 private void renderDetail(){
  stepButton(body,t("← Back to seats","← Επιστροφή στις έδρες"),"detail-back",this::backToSeats,false);
  if(resultPage==6){pageTitle(t("Government coalition simulator","Προσομοίωση κυβερνητικής συνεργασίας"));renderGovernment();return;}
  if(resultPage==8){pageTitle(t("Poll source & assumptions","Πηγή δημοσκόπησης & παραδοχές"));pollNote(card(body,null));return;}
  pageTitle(t("How the seats were allocated","Πώς κατανεμήθηκαν οι έδρες"));
  LinearLayout audit=card(body,null);description(audit,t("Every threshold, quota, remainder and bonus is recorded in order.","Κάθε όριο, μέτρο, υπόλοιπο και μπόνους καταγράφεται με τη σειρά."));audit.getChildAt(0).setTag("explanation");int k=0;for(ElectionEngine.Step s:result.steps){String p=s.party<0?"":name(parties.get(s.party));TextView v=text((++k)+".  "+Explanation.format(s,greek,p),14,INK,false);v.setTextIsSelectable(true);audit.addView(v);space(audit,12);}
 }
 private String pollStatusText(){if(refreshing)return t("Checking published polls…","Έλεγχος δημοσιευμένων δημοσκοπήσεων…");String checked=polls.checked()==0?t("Bundled snapshot: 10 Sep 2026","Αρχικό στιγμιότυπο: 10 Σεπ 2026"):t("Last successful check: ","Τελευταίος επιτυχής έλεγχος: ")+android.text.format.DateFormat.format("dd MMM yyyy HH:mm",polls.checked());return checked+(polls.error().isEmpty()?"":t(" · Update failed; using saved polls"," · Αποτυχία ενημέρωσης· χρήση αποθηκευμένων"));}
 private void refreshPolls(boolean manual){if(refreshing)return;refreshing=true;if(pollStatus!=null)pollStatus.setText(pollStatusText());new Thread(()->{boolean ok=polls.refresh();runOnUiThread(()->{refreshing=false;if(isDestroyed())return;if(pollStatus!=null)pollStatus.setText(pollStatusText());if(manual)Toast.makeText(this,ok?t("Poll list updated. Your scenario is unchanged.","Η λίστα ενημερώθηκε. Το σενάριό σας διατηρήθηκε."):t("Could not refresh. Saved polls remain available.","Αδυναμία ενημέρωσης. Οι αποθηκευμένες δημοσκοπήσεις παραμένουν διαθέσιμες."),Toast.LENGTH_LONG).show();});},"poll-refresh").start();}
 private void choosePoll(){
  List<JSONObject> available=polls.all();String[] labels=available.stream().map(p->p.optString("firm")+" · "+p.optString("date")).toArray(String[]::new);
  AlertDialog dialog=new AlertDialog.Builder(this).setTitle(t("Choose an individual poll","Επιλογή δημοσκόπησης")).setItems(labels,(d,i)->loadPoll(available.get(i))).setNegativeButton(t("Close","Κλείσιμο"),null).create();
  dialog.setOnDismissListener(d->{render();save();});showSelectionDialog(dialog);
 }
 private void loadHistoricalResults(String id){
  loadDataset(id);sourceMode="election";sourceReady=true;percent=false;winner=dataset.optString("winner","");
  for(JSONObject p:parties){String partyId=p.optString("id");values.put(partyId,Long.toString(p.optLong("votes")));selectedParties.add(partyId);}
  normalizeNationalPercentages();
 }
 private void loadPoll(JSONObject poll){
  loadDataset(type==1?"eu":"june");sourceMode="poll";sourceReady=true;editorStep=2;percent=true;parties.clear();values.clear();
  try{activePoll=new JSONObject(poll.toString());JSONArray rows=activePoll.getJSONArray("parties");for(int i=0;i<rows.length();i++){JSONObject p=new JSONObject(rows.getJSONObject(i).toString());parties.add(p);String id=p.getString("id");values.put(id,p.getString("percentage"));selectedParties.add(id);}pollEdited=false;expanded=true;}catch(JSONException e){throw new IllegalStateException(e);}
 }
 private void pollNote(LinearLayout box){if(type==1)description(box,t("National poll applied to a European-election seat scenario; not a European-election poll.","Εθνική δημοσκόπηση σε σενάριο κατανομής εδρών ευρωεκλογών· όχι δημοσκόπηση ευρωεκλογών."));description(box,activePoll.optString("firm")+" · "+activePoll.optString("date")+t(" (fieldwork end)"," (λήξη έρευνας)")+"\n"+t("Published party shares via PolitPro. Other: ","Δημοσιευμένα ποσοστά μέσω PolitPro. Λοιπά: ")+activePoll.optString("other")+"%. "+t("Other is assumed below 3% per party. Small rounding differences go into the remainder; named party percentages are preserved. Undecided voters are not imported. Polls have sampling uncertainty; this is a seat scenario, not a prediction.","Τα λοιπά θεωρούνται κάτω από 3% ανά κόμμα. Μικρές αποκλίσεις στρογγυλοποίησης προστίθενται στο υπόλοιπο· τα ποσοστά κομμάτων διατηρούνται. Οι αναποφάσιστοι δεν εισάγονται. Υπάρχει δειγματοληπτική αβεβαιότητα· πρόκειται για σενάριο εδρών, όχι πρόβλεψη."));if(pollEdited)description(box,t("Edited scenario — values differ from the published poll.","Τροποποιημένο σενάριο — οι τιμές διαφέρουν από τη δημοσκόπηση."));link(box,t("Open poll source","Άνοιγμα πηγής δημοσκόπησης"),activePoll.optString("source"));if(activePoll.has("primary"))link(box,t("Original GPO / Star publication · 9 Sep 2026","Αρχική δημοσίευση GPO / Star · 9 Σεπ 2026"),activePoll.optString("primary"));}
 private void renderGovernment(){LinearLayout box=card(body,null);description(box,t("Choose any parties, or a single party. 151 of 300 seats meet the majority target. This tests seat arithmetic; it does not imply political agreement.","Επιλέξτε κόμματα ή ένα μόνο κόμμα. Οι 151 από 300 έδρες καλύπτουν τον στόχο πλειοψηφίας. Ελέγχεται το άθροισμα εδρών, όχι η πολιτική συμφωνία."));TextView score=text("",24,INK,true);score.setTag("government-total");box.addView(score);ProgressBar progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);progress.setMax(300);box.addView(progress,new LinearLayout.LayoutParams(-1,dp(12)));TextView status=text("",15,TEAL,true);status.setTag("government-status");box.addView(status);Runnable update=()->{boolean[] chosen=new boolean[parties.size()];for(int i=0;i<chosen.length;i++)chosen[i]=government.contains(parties.get(i).optString("id"));int count=Scenario.coalitionSeats(result.seats,chosen);score.setText(count+" / 300 "+t("seats","έδρες"));progress.setProgress(count,android.animation.ValueAnimator.areAnimatorsEnabled());status.setText(Scenario.hasMajority(count)?t("Can form a majority government · ","Δυνατότητα κυβέρνησης πλειοψηφίας · ")+(count-151)+t(" seats above 151"," έδρες πάνω από τις 151"):t("No majority · needs ","Χωρίς πλειοψηφία · χρειάζονται ")+(151-count)+t(" more seats"," ακόμη έδρες"));status.setTextColor(Scenario.hasMajority(count)?TEAL:MUTED);};for(int i=0;i<parties.size();i++){if(result.seats[i]==0)continue;JSONObject party=parties.get(i);String id=party.optString("id");CheckBox check=new CheckBox(this);check.setText(name(party)+" · "+result.seats[i]);check.setTextColor(INK);check.setMinHeight(dp(48));check.setTag("government-"+id);check.setChecked(government.contains(id));check.setOnCheckedChangeListener((v,checked)->{feedback("tap");v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);if(checked)government.add(id);else government.remove(id);update.run();save();});box.addView(check);}update.run();}
 private void privacyPolicy(){new AlertDialog.Builder(this).setTitle(t("Privacy policy","Πολιτική απορρήτου")).setMessage(t("Politeia stores language, sound, election inputs and saved scenarios locally on your device. No account, advertising, analytics or crash-reporting SDK is used. Optional poll refresh and source links make network requests to the published source providers; their own privacy policies apply. The app does not intentionally collect or share personal data. Contact the publisher using the support address published with this app.","Η Πολιτεία αποθηκεύει τη γλώσσα, τον ήχο, τις εισαγωγές εκλογών και τα αποθηκευμένα σενάρια τοπικά στη συσκευή. Δεν χρησιμοποιούνται λογαριασμός, διαφημίσεις, analytics ή SDK αναφορών σφαλμάτων. Η προαιρετική ενημέρωση δημοσκοπήσεων και οι σύνδεσμοι πηγών κάνουν δικτυακά αιτήματα προς τους παρόχους των πηγών· ισχύουν οι δικές τους πολιτικές απορρήτου. Η εφαρμογή δεν συλλέγει ούτε κοινοποιεί σκόπιμα προσωπικά δεδομένα. Επικοινωνήστε με τον εκδότη μέσω της διεύθυνσης υποστήριξης που δημοσιεύεται μαζί με την εφαρμογή.")).setPositiveButton(t("Close","Κλείσιμο"),null).show();}
 private void sources(){LinearLayout l=column();l.setPadding(dp(22),dp(12),dp(22),dp(16));description(l,t("Offline ballot snapshots retrieved 10 September 2026. Includes every published list in May/June 2023 parliamentary, 2024 European, and 2023 elections for all 332 municipalities and 13 regions. English local list names are transliterations.","Στιγμιότυπα ψηφοδελτίων: 10 Σεπτεμβρίου 2026. Όλοι οι δημοσιευμένοι συνδυασμοί Μαΐου/Ιουνίου 2023, Ευρωεκλογών 2024 και αυτοδιοικητικών 2023, σε 332 δήμους και 13 περιφέρειες. Αγγλικά ονόματα τοπικών συνδυασμών: μεταγραφή."));description(l,t("National results are seat entitlements under Article 99. Exceptional constituency entitlements under Articles 99(5)/100 need district votes and cannot be derived from nationwide percentages alone. This app does not assign candidates or seats to constituencies.","Εθνικές έδρες βάσει άρθρου 99. Εξαιρέσεις των άρθρων 99(5)/100 απαιτούν ψήφους ανά περιφέρεια και δεν προκύπτουν μόνο από εθνικά ποσοστά. Δεν κατανέμονται υποψήφιοι ή έδρες ανά εκλογική περιφέρεια."));description(l,t("A future ballot is not yet an official party registry. Add or edit lists for scenarios. Lottery decisions are explicit and appear in the explanation. No personal data leaves the app; sound can be switched off.","Το μελλοντικό ψηφοδέλτιο δεν είναι ακόμη επίσημο μητρώο κομμάτων. Προσθέστε ή αλλάξτε συνδυασμούς για σενάρια. Κληρώσεις δηλώνονται ρητά και καταγράφονται. Προσωπικά δεδομένα δεν αποστέλλονται· ο ήχος απενεργοποιείται."));l.addView(button(t("Privacy policy","Πολιτική απορρήτου"),"privacy",this::privacyPolicy,false));space(l,6);
  link(l,t("Official results archive","Επίσημο αρχείο αποτελεσμάτων"),"https://ekloges.ypes.gr/");if(type!=3)link(l,t("Selected dataset (JSON)","Επιλεγμένα δεδομένα (JSON)"),dataset.optString("source"));link(l,"Ν. 4654/2020 · 300", "https://www.hellenicparliament.gr/UserFiles/bcc26661-143b-4f2d-8916-0e0e66ba4c50/eklog-voul-ap-all.pdf");link(l,"Ν. 4255/2014, 5083/2024 · 21","https://www.ypes.gr/wp-content/uploads/2024/05/eggr43270-egk30-20240524.pdf");link(l,"Ν. 4804/2021 · 2023","https://www.ypes.gr/wp-content/uploads/2023/08/eggr65437-egk849-20230803.pdf");link(l,"Ν. 5314/2026 · 33, 56–58, 87","https://www.hellenicparliament.gr/Nomothetiko-Ergo/Anazitisi-Nomothetikou-Ergou?law_id=357e304b-d7d7-410a-8bef-b465011c6f24");ScrollView sv=new ScrollView(this);sv.addView(l);new AlertDialog.Builder(this).setTitle(t("Sources & scope","Πηγές & πεδίο εφαρμογής")).setView(sv).setPositiveButton(t("Close","Κλείσιμο"),null).show();}
 private void link(LinearLayout l,String title,String url){l.addView(button(title,null,()->startActivity(new Intent(Intent.ACTION_VIEW,android.net.Uri.parse(url))),false));space(l,6);}
 private JSONObject state(){JSONObject s=new JSONObject();try{s.put("stateVersion",3).put("sourceMode",sourceMode).put("sourceReady",sourceReady).put("customBonus",customBonus).put("parliamentReinforced",parliamentReinforced).put("resultPage",resultPage).put("selectedParties",new JSONArray(selectedParties)).put("government",new JSONArray(government)).put("activePoll",activePoll).put("pollEdited",pollEdited).put("editorStep",editorStep).put("resultScreen",resultScreen).put("type",type).put("dataset",datasetId).put("percent",percent).put("future",future).put("winner",winner).put("parties",new JSONArray(parties)).put("values",new JSONObject(values)).put("custom",new JSONArray(Arrays.asList(custom))).put("transfers",new JSONObject(transfers)).put("tieOrder",new JSONArray(tieOrder));}catch(JSONException e){throw new IllegalStateException(e);}return s;}
 // Convert historical imports and saved count scenarios once; preserve unfinished fields for validation.
 private void normalizeNationalPercentages(){
  if(type>=2||percent)return;
  BigDecimal total=BigDecimal.ZERO;
  for(String value:values.values())try{BigDecimal count=number(value);if(count.signum()>=0)total=total.add(count);}catch(IllegalArgumentException ignored){}
  for(String id:values.keySet()){
   selectedParties.add(id);
   try{BigDecimal count=number(values.get(id));if(count.signum()>=0)values.put(id,total.signum()==0?"0":count.multiply(new BigDecimal("100")).divide(total,8,RoundingMode.DOWN).stripTrailingZeros().toPlainString());}catch(IllegalArgumentException ignored){}
  }
  percent=true;
 }
 private boolean hasLegacyInputs(){
  for(String value:values.values()){try{if(number(value).signum()!=0)return true;}catch(IllegalArgumentException edited){return true;}}return false;
 }
 private void apply(JSONObject s){type=s.optInt("type");datasetId=s.optString("dataset","june");dataset=repository.get(datasetId);percent=s.optBoolean("percent",true);future=s.optBoolean("future");parliamentReinforced=s.optBoolean("parliamentReinforced",!datasetId.equals("may"));resultPage=Math.max(5,Math.min(8,s.optInt("resultPage",5)));if((resultPage==6&&type!=0)||(resultPage==8&&s.optJSONObject("activePoll")==null))resultPage=5;winner=s.optString("winner","");editorStep=Math.max(1,Math.min(4,s.optInt("editorStep",1)));if(s.optInt("stateVersion",1)<2&&editorStep==2)editorStep=4;if(type==3&&editorStep==2)editorStep=3;parties.clear();values.clear();JSONArray ps=s.optJSONArray("parties");for(int i=0;i<ps.length();i++)parties.add(ps.optJSONObject(i));JSONObject vs=s.optJSONObject("values");for(Iterator<String> it=vs.keys();it.hasNext();){String key=it.next();values.put(key,vs.optString(key));}JSONArray c=s.optJSONArray("custom");if(c!=null)for(int i=0;i<custom.length;i++)custom[i]=c.optString(i,custom[i]);customBonus=s.has("customBonus")?s.optBoolean("customBonus"):number(custom[6]).signum()>0&&(number(custom[3]).signum()>0||number(custom[5]).signum()>0);transfers.clear();JSONObject tr=s.optJSONObject("transfers");if(tr!=null)for(Iterator<String> it=tr.keys();it.hasNext();){String key=it.next();transfers.put(key,tr.optString(key));}tieOrder.clear();JSONArray ties=s.optJSONArray("tieOrder");if(ties!=null)for(int i=0;i<ties.length();i++)tieOrder.add(ties.optString(i));selectedParties.clear();JSONArray selected=s.optJSONArray("selectedParties");if(selected!=null){for(int i=0;i<selected.length();i++)selectedParties.add(selected.optString(i));}else for(String id:values.keySet())if(number(values.get(id)).signum()>0)selectedParties.add(id);government.clear();JSONArray gov=s.optJSONArray("government");if(gov!=null)for(int i=0;i<gov.length();i++)government.add(gov.optString(i));activePoll=s.optJSONObject("activePoll");pollEdited=s.optBoolean("pollEdited");sourceMode=s.has("sourceMode")?s.optString("sourceMode"):activePoll!=null?"poll":editorStep>=3||hasLegacyInputs()?"election":"";sourceReady=s.optBoolean("sourceReady",!sourceMode.isEmpty());normalizeNationalPercentages();resultScreen=false;result=null;if(s.optBoolean("resultScreen")){try{result=ElectionEngine.calculate(input());resultScreen=true;}catch(IllegalArgumentException ignored){}}}
 private void save(){getPreferences(0).edit().putBoolean("greek",greek).putBoolean("sounds",sounds).putString("draft",state().toString()).apply();}
 private void restore(){try{String s=getPreferences(0).getString("draft","");if(!s.isEmpty())apply(new JSONObject(s));}catch(Exception ignored){loadDataset("june");}}
 private void saveScenario(){EditText name=new EditText(this);name.setHint(t("Scenario name","Όνομα σεναρίου"));name.setSingleLine();AlertDialog d=new AlertDialog.Builder(this).setTitle(t("Save scenario","Αποθήκευση σεναρίου")).setView(name).setPositiveButton(t("Save","Αποθήκευση"),null).setNegativeButton(t("Cancel","Ακύρωση"),null).create();d.setOnShowListener(x->d.getButton(-1).setOnClickListener(v->{String n=name.getText().toString().trim();if(n.isEmpty()){name.setError(t("Enter a name","Εισαγάγετε όνομα"));return;}getSharedPreferences("scenarios",0).edit().putString(n,state().toString()).apply();d.dismiss();feedback("success");Toast.makeText(this,t("Scenario saved","Το σενάριο αποθηκεύτηκε"),Toast.LENGTH_SHORT).show();}));d.show();}
 private void openScenario(){Map<String,?> saved=getSharedPreferences("scenarios",0).getAll();String[] names=saved.keySet().toArray(new String[0]);Arrays.sort(names);if(names.length==0){Toast.makeText(this,t("No saved scenarios yet","Δεν υπάρχουν αποθηκευμένα σενάρια"),Toast.LENGTH_SHORT).show();return;}AlertDialog dialog=new AlertDialog.Builder(this).setTitle(t("Open scenario","Άνοιγμα σεναρίου")).setItems(names,(d,i)->{try{apply(new JSONObject(saved.get(names[i]).toString()));((AlertDialog)d).dismiss();}catch(Exception e){Toast.makeText(this,t("Could not open scenario","Αδυναμία ανοίγματος"),Toast.LENGTH_SHORT).show();}}).setNegativeButton(t("Cancel","Ακύρωση"),null).create();dialog.setOnDismissListener(d->render());dialog.getWindow().setWindowAnimations(0);dialog.setOnShowListener(x->dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND));dialog.show();}
 private void navigateBack(){if(resultScreen){if(resultPage!=5)backToSeats();else goToStep(4);}else if(editorStep>1)goToStep(editorStep==3&&type==3?1:editorStep-1);else finish();}
 @Override public void onBackPressed(){navigateBack();}
}
