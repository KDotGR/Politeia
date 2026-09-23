package gr.politeia.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** A read-only first-use guide. Fragment state keeps its place across rotation or theme changes. */
public final class WalkthroughDialog extends DialogFragment {
 private int page;
 private boolean greek,custom;
 private String[][] titles,text;
 private TextView progress,description;
 private static final String[][] TITLES={
  {"Welcome to Politeia","Καλώς ήρθατε στην Πολιτεία"},
  {"Choose your starting point","Επιλέξτε αφετηρία"},
  {"Choose the electoral law","Επιλέξτε εκλογικό νόμο"},
  {"Enter your scenario","Εισαγάγετε το σενάριό σας"},
  {"Explore and share results","Δείτε και μοιραστείτε τα αποτελέσματα"}
 };
 private static final String[][] TEXT={
  {"Turn election results into a seat allocation in a few simple steps.\n\nStart by choosing Parliament, European, Local or Custom elections. Use Previous and Next to move through your scenario.",
   "Μετατρέψτε εκλογικά αποτελέσματα σε κατανομή εδρών με λίγα απλά βήματα.\n\nΕπιλέξτε βουλευτικές, ευρωπαϊκές, αυτοδιοικητικές ή προσαρμοσμένες εκλογές. Με τα Προηγούμενο και Επόμενο προχωράτε στο σενάριό σας."},
  {"Choose a previous election to load its parties and results. For Parliament and European scenarios, you can also choose a poll. You can edit the imported figures later.\n\nCustom elections skip this choice: you create the parties yourself.",
   "Επιλέξτε προηγούμενες εκλογές για να φορτώσετε κόμματα και αποτελέσματα. Για βουλευτικά και ευρωπαϊκά σενάρια μπορείτε επίσης να επιλέξετε δημοσκόπηση. Τα στοιχεία αλλάζουν στη συνέχεια.\n\nΟι προσαρμοσμένες εκλογές παραλείπουν αυτή την επιλογή: δημιουργείτε εσείς τα κόμματα."},
  {"The current law is selected by default. Choose a previous law where available to compare scenarios.\n\nFor custom elections, set the total seats, threshold and optional bonus. Add the number of candidates for each party when entering its name.",
   "Ο ισχύων νόμος επιλέγεται αυτόματα. Όπου διατίθεται, επιλέξτε παλαιότερο νόμο για να συγκρίνετε σενάρια.\n\nΣτις προσαρμοσμένες εκλογές ορίζετε έδρες, εκλογικό όριο και προαιρετικό μπόνους. Για κάθε κόμμα εισάγετε όνομα και αριθμό υποψηφίων."},
  {"Edit the party percentages, or add parties from the list. Parliament and European elections use percentages; Local and Custom also support vote counts.\n\nWith a 3% threshold, any unentered share up to 100% is treated as other parties, each below 3%. Use More to save or reopen a scenario.",
   "Αλλάξτε τα ποσοστά ή προσθέστε κόμματα από τη λίστα. Βουλευτικές και ευρωπαϊκές εκλογές χρησιμοποιούν ποσοστά· αυτοδιοικητικές και προσαρμοσμένες δέχονται και αριθμό ψήφων.\n\nΜε όριο 3%, το υπόλοιπο έως το 100% θεωρείται λοιπά κόμματα, καθένα κάτω από 3%. Από τα Άλλα αποθηκεύετε ή ανοίγετε σενάρια."},
  {"Calculate seats to see the chart, party totals and changes from the comparison election. Open the allocation explanation to see how the result was calculated.\n\nFor Parliament, test coalitions against the 151-seat majority. Share a results image, or go back to edit. You can reopen this walkthrough from Settings at any time.",
   "Υπολογίστε έδρες για να δείτε το διάγραμμα, τις έδρες ανά κόμμα και τις μεταβολές από τις εκλογές σύγκρισης. Ανοίξτε την εξήγηση για τα βήματα του υπολογισμού.\n\nΣτη Βουλή δοκιμάστε συνεργασίες για πλειοψηφία 151 εδρών. Μοιραστείτε εικόνα αποτελεσμάτων ή επιστρέψτε για αλλαγές. Ο οδηγός ανοίγει ξανά από τις Ρυθμίσεις."}
 };
 private static final String[][] CUSTOM_TITLES={
  {"Your own election","Οι δικές σας εκλογές"},
  {"Optional winner bonus","Προαιρετικό μπόνους νικητή"},
  {"Parties and candidates","Κόμματα και υποψήφιοι"},
  {"Enter the results","Εισαγάγετε τα αποτελέσματα"},
  {"Seat limits and vacancies","Όρια υποψηφίων και κενές έδρες"}
 };
 private static final String[][] CUSTOM_TEXT={
  {"Custom elections let you define your own rules and parties, without importing a past election or poll.\n\nPress Next to set the total seats and entry threshold. A party below that percentage does not receive seats. Set the threshold to 0 if you do not want an entry threshold.",
   "Στις προσαρμοσμένες εκλογές ορίζετε δικούς σας κανόνες και κόμματα, χωρίς εισαγωγή προηγούμενων εκλογών ή δημοσκόπησης.\n\nΠατήστε Επόμενο για να ορίσετε συνολικές έδρες και εκλογικό όριο. Κόμμα κάτω από αυτό το ποσοστό δεν λαμβάνει έδρες. Ορίστε 0 αν δεν θέλετε εκλογικό όριο."},
  {"Leave Bonus seats at No for simple proportional allocation. Choose Yes to reveal the bonus settings for the first party: starting percentage, base bonus, percentage-point step, seats per step and maximum bonus.\n\nExample: start 20%, base 0, step 5 points, gain 1 seat, maximum 10. A first party at 35% receives 3 bonus seats within the total seats you set.",
   "Αφήστε τις Έδρες μπόνους στο Όχι για απλή αναλογική. Επιλέξτε Ναι για τις ρυθμίσεις του πρώτου κόμματος: ποσοστό έναρξης, αρχικό μπόνους, βήμα ποσοστού, έδρες ανά βήμα και μέγιστο μπόνους.\n\nΠαράδειγμα: έναρξη 20%, αρχικό 0, βήμα 5 μονάδες, 1 έδρα ανά βήμα, μέγιστο 10. Πρώτο κόμμα στο 35% λαμβάνει 3 έδρες μπόνους μέσα στο σύνολο εδρών που ορίσατε."},
  {"On the Votes page, use Add party to enter each party's name and number of candidates. One name is used in both languages.\n\nThe candidate count is the most seats that party can fill. Tap a party name later to edit its details.",
   "Στη σελίδα Ψήφοι, επιλέξτε Προσθήκη κόμματος και εισαγάγετε όνομα και αριθμό υποψηφίων. Το ίδιο όνομα χρησιμοποιείται και στις δύο γλώσσες.\n\nΟ αριθμός υποψηφίων είναι οι περισσότερες έδρες που μπορεί να καλύψει το κόμμα. Πατήστε το όνομά του για να αλλάξετε τα στοιχεία του."},
  {"Choose percentages or vote counts before entering the party results. Changing format clears the current values after confirmation.\n\nPercentages must total 100%. If you set the threshold to exactly 3%, you may leave a remainder: it represents other parties, each below 3%. Use More to save or reopen your scenario.",
   "Επιλέξτε ποσοστά ή αριθμό ψήφων πριν εισαγάγετε αποτελέσματα. Η αλλαγή μορφής μηδενίζει τις τρέχουσες τιμές μετά από επιβεβαίωση.\n\nΤα ποσοστά πρέπει να αθροίζουν 100%. Με όριο ακριβώς 3%, μπορείτε να αφήσετε υπόλοιπο: αντιστοιχεί σε λοιπά κόμματα, καθένα κάτω από 3%. Από τα Άλλα αποθηκεύετε ή ανοίγετε το σενάριο."},
  {"Calculate seats to see the allocation. If a party earns more seats than it has candidates, the surplus is redistributed proportionally among eligible parties with candidates still available.\n\nSeats that cannot be filled remain vacant and appear grey in the chart. Open the allocation explanation to inspect these transfers, or share an image of the results. Replay this guide from Settings while using Custom elections.",
   "Υπολογίστε έδρες για να δείτε την κατανομή. Αν ένα κόμμα δικαιούται περισσότερες έδρες από τους υποψηφίους του, οι επιπλέον έδρες ανακατανέμονται αναλογικά στα επιλέξιμα κόμματα με διαθέσιμους υποψηφίους.\n\nΟι έδρες που δεν μπορούν να καλυφθούν μένουν κενές και φαίνονται γκρίζες στο διάγραμμα. Ανοίξτε την εξήγηση για τις μεταφορές ή μοιραστείτε εικόνα αποτελεσμάτων. Ο οδηγός ανοίγει ξανά από τις Ρυθμίσεις στις προσαρμοσμένες εκλογές."}
 };
 public static WalkthroughDialog create(boolean greek){return create(greek,false);}
 public static WalkthroughDialog create(boolean greek,boolean custom){WalkthroughDialog dialog=new WalkthroughDialog();Bundle args=new Bundle();args.putBoolean("greek",greek);args.putBoolean("custom",custom);dialog.setArguments(args);return dialog;}
 @Override public void onCreate(Bundle saved){super.onCreate(saved);greek=getArguments().getBoolean("greek");custom=getArguments().getBoolean("custom");titles=custom?CUSTOM_TITLES:TITLES;text=custom?CUSTOM_TEXT:TEXT;page=saved==null?0:saved.getInt("page",0);}
 @Override public Dialog onCreateDialog(Bundle saved){
  LinearLayout content=new LinearLayout(getActivity());content.setOrientation(LinearLayout.VERTICAL);int pad=(int)(24*getResources().getDisplayMetrics().density);content.setPadding(pad,pad/2,pad,pad/2);
  progress=new TextView(getActivity());progress.setTag("walkthrough-progress");progress.setTextSize(13);progress.setTextColor(getActivity().getColor(R.color.app_accent));content.addView(progress);
  description=new TextView(getActivity());description.setTextSize(16);description.setTextColor(getActivity().getColor(R.color.app_ink));description.setPadding(0,pad/2,0,0);description.setLineSpacing(4,1);content.addView(description);
  ScrollView scroll=new ScrollView(getActivity());scroll.addView(content);
  return new AlertDialog.Builder(getActivity()).setTitle(titles[page][greek?1:0]).setView(scroll)
   .setNegativeButton(greek?"Παράλειψη":"Skip",null).setNeutralButton(greek?"Πίσω":"Back",null).setPositiveButton(greek?"Επόμενο":"Next",null).create();
 }
 @Override public void onStart(){super.onStart();AlertDialog dialog=(AlertDialog)getDialog();dialog.setCanceledOnTouchOutside(false);
  dialog.getButton(-2).setTag("walkthrough-skip");dialog.getButton(-2).setOnClickListener(v->finish());
  dialog.getButton(-3).setTag("walkthrough-back");dialog.getButton(-3).setOnClickListener(v->{page--;update();});
  dialog.getButton(-1).setTag("walkthrough-next");dialog.getButton(-1).setOnClickListener(v->{if(page==titles.length-1)finish();else{page++;update();}});update();
 }
 private void update(){AlertDialog dialog=(AlertDialog)getDialog();dialog.setTitle(titles[page][greek?1:0]);progress.setText((page+1)+" / "+titles.length);description.setText(text[page][greek?1:0]);((ScrollView)description.getParent().getParent()).scrollTo(0,0);dialog.getButton(-3).setVisibility(page==0?View.GONE:View.VISIBLE);dialog.getButton(-1).setText(page==titles.length-1?(greek?"Ξεκινήστε":"Get started"):(greek?"Επόμενο":"Next"));}
 private void rememberDismissal(){getActivity().getPreferences(0).edit().putBoolean(custom?"customWalkthroughSeen":"walkthroughSeen",true).apply();}
 private void finish(){rememberDismissal();dismiss();}
 @Override public void onCancel(DialogInterface dialog){rememberDismissal();super.onCancel(dialog);}
 @Override public void onSaveInstanceState(Bundle out){out.putInt("page",page);super.onSaveInstanceState(out);}
}
