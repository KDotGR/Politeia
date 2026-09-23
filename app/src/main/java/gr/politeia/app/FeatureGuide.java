package gr.politeia.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.List;

/** First-use coach marks anchored to real controls. Guidance never performs the highlighted action. */
final class FeatureGuide {
 private static final String PREFIX="guide.v1.";
 private final Activity activity;
 private final SharedPreferences preferences;
 private Window owner;
 private View watchedDecor;
 private final View.OnAttachStateChangeListener attachment=new View.OnAttachStateChangeListener(){public void onViewAttachedToWindow(View v){}public void onViewDetachedFromWindow(View v){close();}};
 private Dialog dialog;
 private View shiftedContent;
 private float originalTranslation;
 private String feature;
 private List<FeatureGuideCatalog.Tip> tips;
 private boolean greek;
 private int index,generation;

 FeatureGuide(Activity activity,SharedPreferences preferences){this.activity=activity;this.preferences=preferences;}
 boolean isShowing(){return dialog!=null&&dialog.isShowing();}
 void show(Window window,String feature,boolean greek){
  if(window==null||!preferences.getBoolean("featureTips",true)||preferences.getBoolean(PREFIX+"seen."+feature,false))return;
  close();int request=generation;View decor=window.getDecorView();
  // Wait for the new page/dialog to finish laying out before locating the actual controls.
  decor.post(()->{
   if(request!=generation||activity.isFinishing()||activity.isDestroyed()||!decor.isAttachedToWindow())return;
   this.owner=window;watchedDecor=decor;decor.addOnAttachStateChangeListener(attachment);this.feature=feature;this.greek=greek;tips=FeatureGuideCatalog.tips(feature,greek);
   index=Math.min(preferences.getInt(PREFIX+"step."+feature,0),Math.max(0,tips.size()-1));
   present();
  });
 }
 void reset(){close();SharedPreferences.Editor edit=preferences.edit().putBoolean("featureTips",true);for(String key:preferences.getAll().keySet())if(key.startsWith(PREFIX))edit.remove(key);edit.apply();}
 void dismiss(){if(feature!=null)preferences.edit().putBoolean(PREFIX+"seen."+feature,true).remove(PREFIX+"step."+feature).apply();close();}
 void close(){generation++;if(watchedDecor!=null){watchedDecor.removeOnAttachStateChangeListener(attachment);watchedDecor=null;}restorePosition();if(dialog!=null){Dialog old=dialog;dialog=null;old.dismiss();}owner=null;feature=null;}
 private void restorePosition(){if(shiftedContent!=null){shiftedContent.setTranslationY(originalTranslation);shiftedContent=null;}}
 private void present(){
  restorePosition();View target=null;
  while(index<tips.size()&&(target=find(owner.getDecorView(),tips.get(index).target))==null)index++;
  if(index>=tips.size()){if(dialog==null)close();else dismiss();return;}
  preferences.edit().putInt(PREFIX+"step."+feature,index).apply();
  // Reveal off-screen controls without changing input values or invoking their listeners.
  target.requestRectangleOnScreen(new Rect(0,0,target.getWidth(),Math.min(target.getHeight(),dp(72))),true);
  if(dialog!=null){dialog.dismiss();dialog=null;}
  dialog=new Dialog(activity,android.R.style.Theme_Translucent_NoTitleBar);
  dialog.setCanceledOnTouchOutside(false);dialog.setOnCancelListener(d->dismiss());
  Overlay overlay=new Overlay(target,tips.get(index));dialog.setContentView(overlay);
  Window window=dialog.getWindow();window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));window.setDimAmount(0);window.setWindowAnimations(0);
  window.setStatusBarColor(activity.getColor(R.color.app_background));window.setNavigationBarColor(activity.getColor(R.color.app_background));
  dialog.show();window.setLayout(-1,-1);
  int request=generation;String announcement=tips.get(index).title+". "+tips.get(index).body;
  overlay.post(()->{if(request!=generation||!overlay.isAttachedToWindow())return;overlay.requestLayout();overlay.announceForAccessibility(announcement);});
 }
 private View find(View view,String tag){
  Object candidate=view.getTag();if(tag.equals(view.getTag(R.id.guide_target))&&view.isShown())return view;if(candidate instanceof String&&((tag.endsWith("*")&&((String)candidate).startsWith(tag.substring(0,tag.length()-1)))||tag.equals(candidate))&&view.isShown())return view;
  if(view instanceof ViewGroup){ViewGroup group=(ViewGroup)view;for(int i=0;i<group.getChildCount();i++){View found=find(group.getChildAt(i),tag);if(found!=null)return found;}}
  return null;
 }
 private int dp(int value){return Math.round(value*activity.getResources().getDisplayMetrics().density);}
 private String t(String en,String el){return greek?el:en;}
 private GradientDrawable background(int color){GradientDrawable shape=new GradientDrawable();shape.setColor(color);shape.setCornerRadius(dp(16));return shape;}

 /** The dimmed area has a real transparent cutout; the explanation always sits above it. */
 private final class Overlay extends FrameLayout {
  private final View target;
  private final LinearLayout card;
  private final ScrollView explanation;
  private final RectF highlight=new RectF();
  private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Path shade=new Path();
  Overlay(View target,FeatureGuideCatalog.Tip tip){
   super(activity);this.target=target;setTag("feature-guide");setWillNotDraw(false);setClickable(true);
   card=new LinearLayout(activity);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(16),dp(12),dp(16),dp(8));card.setTag("guide-card");card.setBackground(background(activity.getColor(R.color.app_surface)));
   TextView title=new TextView(activity);title.setTag("guide-title");title.setText(tip.title);title.setTextSize(18);title.setTextColor(activity.getColor(R.color.app_ink));if(Build.VERSION.SDK_INT>=28)title.setAccessibilityHeading(true);card.addView(title);
   ScrollView scroll=new ScrollView(activity);explanation=scroll;TextView description=new TextView(activity);description.setTag("guide-explanation");description.setText(tip.body);description.setTextSize(15);description.setTextColor(activity.getColor(R.color.app_ink));description.setPadding(0,dp(8),0,dp(8));scroll.addView(description);card.addView(scroll,new LinearLayout.LayoutParams(-1,-2));
   LinearLayout actions=new LinearLayout(activity);
   addButton(actions,t("Skip","Παράλειψη"),"guide-skip",()->dismiss());
   int previous=index-1;while(previous>=0&&find(owner.getDecorView(),tips.get(previous).target)==null)previous--;
   final int backIndex=previous;
   if(backIndex>=0)addButton(actions,t("Back","Πίσω"),"guide-back",()->{index=backIndex;present();});
   int next=index+1;while(next<tips.size()&&find(owner.getDecorView(),tips.get(next).target)==null)next++;
   addButton(actions,next==tips.size()?t("Done","Τέλος"):t("Next","Επόμενο"),"guide-next",()->{index++;present();});card.addView(actions);addView(card);
   // Expose the highlighted control to accessibility and UI tests without duplicating its action.
   View marker=new View(activity);marker.setTag("guide-highlight");marker.setContentDescription(t("Highlighted control: ","Επισημασμένο στοιχείο: ")+tip.title);addView(marker);
  }
  private void addButton(LinearLayout row,String label,String tag,Runnable action){Button button=new Button(activity);button.setText(label);button.setAllCaps(false);button.setTextSize(13);button.setTextColor(activity.getColor(R.color.app_accent));button.setTag(tag);button.setMinHeight(dp(48));button.setPadding(dp(4),0,dp(4),0);button.setOnClickListener(v->action.run());row.addView(button,new LinearLayout.LayoutParams(0,-2,1));}
  @Override protected void onMeasure(int widthSpec,int heightSpec){
   int width=MeasureSpec.getSize(widthSpec),height=MeasureSpec.getSize(heightSpec);setMeasuredDimension(width,height);
   // A scrollable explanation keeps Next/Skip reachable on small screens and with larger fonts.
   int insetTop=getRootWindowInsets()==null?0:getRootWindowInsets().getSystemWindowInsetTop();
   int insetBottom=getRootWindowInsets()==null?0:getRootWindowInsets().getSystemWindowInsetBottom();
   int maximum=Math.min(dp(280),Math.max(dp(110),height-insetTop-insetBottom-dp(108)));
   int cardWidth=MeasureSpec.makeMeasureSpec(Math.max(1,width-dp(32)),MeasureSpec.EXACTLY);
   explanation.getLayoutParams().height=LayoutParams.WRAP_CONTENT;
   card.measure(cardWidth,MeasureSpec.makeMeasureSpec(0,MeasureSpec.UNSPECIFIED));
   if(card.getMeasuredHeight()>maximum){explanation.getLayoutParams().height=Math.max(dp(24),explanation.getMeasuredHeight()-(card.getMeasuredHeight()-maximum));card.measure(cardWidth,MeasureSpec.makeMeasureSpec(0,MeasureSpec.UNSPECIFIED));}
  }
  @Override protected void onLayout(boolean changed,int l,int t,int r,int b){
   int[] screen=new int[2],control=new int[2];getLocationOnScreen(screen);target.getLocationOnScreen(control);
   int insetTop=getRootWindowInsets()==null?0:getRootWindowInsets().getSystemWindowInsetTop();
   float top=control[1]-screen[1],minimum=insetTop+card.getMeasuredHeight()+dp(20);
   if(top<minimum&&shiftedContent==null){
    View content=owner.findViewById(android.R.id.content);
    if(content!=null){shiftedContent=content;originalTranslation=content.getTranslationY();content.setTranslationY(originalTranslation+minimum-top);target.getLocationOnScreen(control);top=control[1]-screen[1];}
   }
   float left=Math.max(dp(4),control[0]-screen[0]);
   highlight.set(left,top,Math.min(getWidth()-dp(4),left+target.getWidth()),Math.min(getHeight()-dp(4),top+Math.min(target.getHeight(),dp(80))));
   int cardTop=Math.max(insetTop+dp(8),(int)highlight.top-dp(12)-card.getMeasuredHeight());
   card.layout(dp(16),cardTop,getWidth()-dp(16),cardTop+card.getMeasuredHeight());
   getChildAt(1).layout((int)highlight.left,(int)highlight.top,(int)highlight.right,(int)highlight.bottom);invalidate();
  }
  @Override protected void onDraw(Canvas canvas){
   super.onDraw(canvas);shade.reset();shade.setFillType(Path.FillType.EVEN_ODD);shade.addRect(0,0,getWidth(),getHeight(),Path.Direction.CW);shade.addRoundRect(highlight,dp(8),dp(8),Path.Direction.CW);paint.setStyle(Paint.Style.FILL);paint.setColor(0xB8000000);canvas.drawPath(shade,paint);
   paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(dp(3));paint.setColor(activity.getColor(R.color.app_accent));canvas.drawRoundRect(highlight,dp(8),dp(8),paint);
  }
 }
}
