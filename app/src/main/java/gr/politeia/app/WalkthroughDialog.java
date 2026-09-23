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
 private boolean greek;
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
 public static WalkthroughDialog create(boolean greek){WalkthroughDialog dialog=new WalkthroughDialog();Bundle args=new Bundle();args.putBoolean("greek",greek);dialog.setArguments(args);return dialog;}
 @Override public void onCreate(Bundle saved){super.onCreate(saved);greek=getArguments().getBoolean("greek");page=saved==null?0:saved.getInt("page",0);}
 @Override public Dialog onCreateDialog(Bundle saved){
  LinearLayout content=new LinearLayout(getActivity());content.setOrientation(LinearLayout.VERTICAL);int pad=(int)(24*getResources().getDisplayMetrics().density);content.setPadding(pad,pad/2,pad,pad/2);
  progress=new TextView(getActivity());progress.setTag("walkthrough-progress");progress.setTextSize(13);progress.setTextColor(getActivity().getColor(R.color.app_accent));content.addView(progress);
  description=new TextView(getActivity());description.setTextSize(16);description.setTextColor(getActivity().getColor(R.color.app_ink));description.setPadding(0,pad/2,0,0);description.setLineSpacing(4,1);content.addView(description);
  ScrollView scroll=new ScrollView(getActivity());scroll.addView(content);
  return new AlertDialog.Builder(getActivity()).setTitle(TITLES[page][greek?1:0]).setView(scroll)
   .setNegativeButton(greek?"Παράλειψη":"Skip",null).setNeutralButton(greek?"Πίσω":"Back",null).setPositiveButton(greek?"Επόμενο":"Next",null).create();
 }
 @Override public void onStart(){super.onStart();AlertDialog dialog=(AlertDialog)getDialog();dialog.setCanceledOnTouchOutside(false);
  dialog.getButton(-2).setTag("walkthrough-skip");dialog.getButton(-2).setOnClickListener(v->finish());
  dialog.getButton(-3).setTag("walkthrough-back");dialog.getButton(-3).setOnClickListener(v->{page--;update();});
  dialog.getButton(-1).setTag("walkthrough-next");dialog.getButton(-1).setOnClickListener(v->{if(page==TITLES.length-1)finish();else{page++;update();}});update();
 }
 private void update(){AlertDialog dialog=(AlertDialog)getDialog();dialog.setTitle(TITLES[page][greek?1:0]);progress.setText((page+1)+" / "+TITLES.length);description.setText(TEXT[page][greek?1:0]);((ScrollView)description.getParent().getParent()).scrollTo(0,0);dialog.getButton(-3).setVisibility(page==0?View.GONE:View.VISIBLE);dialog.getButton(-1).setText(page==TITLES.length-1?(greek?"Ξεκινήστε":"Get started"):(greek?"Επόμενο":"Next"));}
 private void rememberDismissal(){getActivity().getPreferences(0).edit().putBoolean("walkthroughSeen",true).apply();}
 private void finish(){rememberDismissal();dismiss();}
 @Override public void onCancel(DialogInterface dialog){rememberDismissal();super.onCancel(dialog);}
 @Override public void onSaveInstanceState(Bundle out){out.putInt("page",page);super.onSaveInstanceState(out);}
}
