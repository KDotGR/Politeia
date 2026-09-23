package gr.politeia.app;
import java.util.ArrayList;
import java.util.List;
/** Bilingual help for real tagged controls. Optional controls are skipped when absent. */
final class FeatureGuideCatalog {
 static final class Tip {final String target,title,body;Tip(String target,String title,String body){this.target=target;this.title=title;this.body=body;}}
 static List<Tip> tips(String feature,boolean greek){List<Tip> tips=new ArrayList<>();switch(feature){
 case "home":
  tips.add(new Tip("type-0",greek?"Βουλή":"Parliament",greek?"Επιλέξτε σενάριο βουλευτικών εκλογών με 300 έδρες.":"Choose a 300-seat parliamentary scenario."));
  tips.add(new Tip("type-1",greek?"Ευρωεκλογές":"European elections",greek?"Υπολογίστε τις 21 ελληνικές έδρες του Ευρωκοινοβουλίου.":"Calculate Greece’s 21 European Parliament seats."));
  tips.add(new Tip("type-2",greek?"Αυτοδιοικητικές εκλογές":"Local elections",greek?"Επιλέξτε δήμο ή περιφέρεια στην επόμενη σελίδα.":"Choose a municipality or region on the next page."));
  tips.add(new Tip("type-3",greek?"Προσαρμοσμένες εκλογές":"Custom elections",greek?"Ορίστε κόμματα, συνολικές έδρες, όριο και προαιρετικό μπόνους.":"Define your own parties, seat total, threshold and optional bonus."));
  tips.add(new Tip("next-step",greek?"Συνεχίστε":"Move forward",greek?"Με το Επόμενο συνεχίζετε. Το Προηγούμενο επιστρέφει χωρίς να μηδενίζει τις τιμές σας.":"Use Next to continue. Previous returns to the last page without clearing your inputs."));
  tips.add(new Tip("settings",greek?"Ρυθμίσεις":"Settings",greek?"Αλλάξτε γλώσσα, εμφάνιση και ήχους. Εδώ επαναλαμβάνετε τις συμβουλές.":"Change language, appearance and sounds here. You can replay these tips."));
  break;
 case "source":
  tips.add(new Tip("source-election",greek?"Προηγούμενες εκλογές":"Previous election",greek?"Φορτώστε κόμματα και αποτελέσματα προηγούμενων εκλογών και τροποποιήστε τα.":"Load a past election’s parties and results, then edit them."));
  tips.add(new Tip("source-poll",greek?"Χρήση δημοσκόπησης":"Use a poll",greek?"Επιλέξτε δημοσκόπηση. Τα ευρωπαϊκά σενάρια χρησιμοποιούν τις ίδιες εθνικές δημοσκοπήσεις, όχι δημοσκοπήσεις ευρωεκλογών.":"Choose an individual poll. European scenarios use the same national polls, not European-election polls."));
  break;
 case "source-history":
  tips.add(new Tip("dataset",greek?"Επιλογή εκλογών":"Select an election",greek?"Ανοίξτε τη λίστα και επιλέξτε εκλογές ή τοπική περιοχή για εισαγωγή.":"Open this list and choose the election or local area to import."));
  break;
 case "source-poll":
  tips.add(new Tip("polls",greek?"Επιλογή δημοσκόπησης":"Select a poll",greek?"Επιλέξτε εταιρεία και ημερομηνία. Τα ποσοστά φορτώνονται άμεσα και μπορούν να αλλάξουν.":"Choose the polling firm and date. Its percentages load directly and remain editable."));
  break;
 case "history":
  tips.add(new Tip("election-search",greek?"Βρείτε τις εκλογές":"Find your election",greek?"Αναζητήστε όνομα εκλογών, δήμου ή περιφέρειας.":"Search by election, municipality or region name."));
  tips.add(new Tip("election-list",greek?"Εισαγωγή αποτελεσμάτων":"Import results",greek?"Επιλέξτε μια γραμμή για να φορτώσετε κόμματα και αποτελέσματα.":"Select a row to load that election’s parties and results."));
  break;
 case "poll":
  tips.add(new Tip("poll-list",greek?"Μεμονωμένες δημοσκοπήσεις":"Individual polls",greek?"Επιλέξτε δημοσίευση για τα στοιχεία της. Η κατανομή είναι σενάριο, όχι πρόβλεψη.":"Select one publication to use its figures. Polls describe a scenario, not a prediction."));
  break;
 case "law":
  tips.add(new Tip("law",greek?"Εκλογικός νόμος":"Electoral law",greek?"Αρχικά επιλέγεται ο ισχύων νόμος. Ανοίξτε για άλλον διαθέσιμο νόμο.":"The current law is selected initially. Open this control to choose another available law."));
  tips.add(new Tip("next-step",greek?"Επόμενο: αποτελέσματα":"Enter results next",greek?"Συνεχίστε στη σελίδα εισαγωγής και επεξεργασίας ψήφων.":"Continue to the editable vote-entry page."));
  break;
 case "law-picker":
  tips.add(new Tip("law-list",greek?"Επιλέξτε νόμο":"Choose a law",greek?"Επιλέξτε τον κανόνα προσομοίωσης. Αλλάζει η κατανομή, όχι τα εισαγόμενα αποτελέσματα.":"Select the rule you want to simulate. This changes allocation rules, not your imported results."));
  break;
 case "local-law":
  tips.add(new Tip("law",greek?"Κανόνες αυτοδιοικητικών":"Local electoral rules",greek?"Επιλέξτε διαθέσιμους ισχύοντες ή παλαιότερους κανόνες.":"Choose the available current or historical rules."));
  tips.add(new Tip("winner",greek?"Δήλωση νικητή":"Declare a winner",greek?"Όταν χρειάζεται επόμενος γύρος, επιλέξτε ρητά τον νικητή του.":"Where a later round is needed, choose its winner explicitly."));
  tips.add(new Tip("transfers",greek?"Εναλλακτικές ψήφοι":"Alternative votes",greek?"Εισαγάγετε μεταφορές στους φιναλίστ με τις ίδιες μονάδες των αποτελεσμάτων.":"Enter transfers to finalists in the same units as your results."));
  break;
 case "custom-law":
  tips.add(new Tip("custom-0",greek?"Συνολικές έδρες":"Total seats",greek?"Ορίστε πόσες έδρες θα έχει η εκλογική διαδικασία.":"Set the number of seats for your election."));
  tips.add(new Tip("custom-1",greek?"Εκλογικό όριο":"Entry threshold",greek?"Κόμματα κάτω από αυτό το ποσοστό δεν λαμβάνουν έδρες. Ορίστε 0 για κανένα όριο.":"Parties below this percentage receive no seats. Use 0 for no threshold."));
  tips.add(new Tip("custom-bonus",greek?"Προαιρετικό μπόνους":"Optional bonus",greek?"Επιλέξτε Ναι για κανόνες μπόνους του πρώτου κόμματος ή Όχι για απλή αναλογική.":"Choose Yes to reveal the first party’s bonus rules; No uses simple proportional allocation."));
  break;
 case "custom-bonus":
  tips.add(new Tip("custom-2",greek?"Έναρξη μπόνους":"Bonus starts at",greek?"Το πρώτο κόμμα πρέπει να φτάσει αυτό το ποσοστό για το αρχικό μπόνους.":"The first party must reach this percentage to receive the base bonus."));
  tips.add(new Tip("custom-3",greek?"Αρχικό μπόνους":"Base bonus",greek?"Έδρες που δίνονται όταν επιτευχθεί το ποσοστό έναρξης.":"Seats granted when the starting percentage is reached."));
  tips.add(new Tip("custom-4",greek?"Βήμα ποσοστού":"Percentage step",greek?"Πόσες επιπλέον ποσοστιαίες μονάδες χρειάζονται για κάθε αύξηση μπόνους.":"The percentage-point increase needed for each additional bonus increment."));
  tips.add(new Tip("custom-5",greek?"Έδρες ανά βήμα":"Seats per step",greek?"Πόσες επιπλέον έδρες δίνει κάθε πλήρες βήμα.":"How many extra seats each complete step earns."));
  tips.add(new Tip("custom-6",greek?"Μέγιστο μπόνους":"Bonus cap",greek?"Το μπόνους δεν ξεπερνά αυτό το όριο και περιλαμβάνεται στις συνολικές έδρες.":"The bonus cannot exceed this limit and is included within your total seats."));
  break;
 case "votes":
  tips.add(new Tip("value-*",greek?"Αλλάξτε ποσοστά":"Edit party percentages",greek?"Πατήστε μια τιμή για αλλαγή. Τα ιστορικά στοιχεία διατηρούν την ακρίβειά τους στους υπολογισμούς.":"Tap a value to edit it. Historical values retain their full precision for calculations."));
  tips.add(new Tip("input-total",greek?"Ελέγξτε το σύνολο":"Check the total",greek?"Με όριο 3%, το υπόλοιπο έως το 100% αντιστοιχεί σε λοιπά κόμματα, καθένα κάτω από 3%.":"With a 3% threshold, an unentered share up to 100% represents other parties, each below 3%."));
  tips.add(new Tip("add",greek?"Προσθήκη κόμματος":"Add a party",greek?"Επιλέξτε άλλο κόμμα ή δημιουργήστε νέο.":"Choose another party or create a new one."));
  tips.add(new Tip("scenario-menu",greek?"Ενέργειες σεναρίου":"Scenario actions",greek?"Από τα Άλλα αποθηκεύετε, ανοίγετε ή μηδενίζετε τιμές.":"Save, reopen or clear values using More."));
  tips.add(new Tip("calculate",greek?"Υπολογισμός εδρών":"Calculate seats",greek?"Υπολογίστε με τις τρέχουσες τιμές και τον επιλεγμένο νόμο.":"Calculate from your current inputs and selected law."));
  break;
 case "custom-votes":
  tips.add(new Tip("percent",greek?"Μορφή εισαγωγής":"Input format",greek?"Επιλέξτε ποσοστά ή ψήφους. Τα ποσοστά αθροίζουν 100%, με υπόλοιπο μόνο όταν το όριο είναι 3%.":"Choose percentages or vote counts before entering results. Percentages total 100%, with a remainder allowed only at a 3% threshold."));
  tips.add(new Tip("add",greek?"Δημιουργήστε κόμματα":"Create your parties",greek?"Εισαγάγετε ένα όνομα και αριθμό υποψηφίων για κάθε κόμμα.":"Enter one name and a candidate count for every party."));
  tips.add(new Tip("calculate",greek?"Κατανομή εδρών":"Allocate seats",greek?"Ισχύουν τα όρια υποψηφίων. Έδρες που δεν καλύπτονται μένουν κενές.":"Candidate limits apply. Unfillable seats remain vacant."));
  break;
 case "local-votes":
  tips.add(new Tip("percent",greek?"Επιλογή μορφής":"Choose input format",greek?"Χρησιμοποιήστε με συνέπεια ποσοστά ή αριθμό ψήφων.":"Use either percentages or vote counts consistently."));
  tips.add(new Tip("value-*",greek?"Επεξεργασία αποτελεσμάτων":"Edit results",greek?"Αλλάξτε εδώ το αποτέλεσμα του συνδυασμού.":"Change the imported list’s result here."));
  tips.add(new Tip("calculate",greek?"Υπολογισμός τοπικών εδρών":"Calculate local seats",greek?"Εφαρμόστε τον τοπικό νόμο και τα στοιχεία νικητή ή μεταφορών.":"Apply your selected local law and winner or transfer inputs."));
  break;
 case "party-picker":
  tips.add(new Tip("party-list",greek?"Επιλογή κόμματος":"Select a party",greek?"Πατήστε κόμμα για προσθήκη. Το χρώμα βοηθά στην αναγνώριση.":"Tap a party to add it to your inputs. Its color helps identify it."));
  tips.add(new Tip("new-party",greek?"Νέο κόμμα":"New party",greek?"Δημιουργήστε κόμμα με ένα όνομα κοινό και στις δύο γλώσσες.":"Create a party using one name shared by both languages."));
  break;
 case "party-edit":
 case "party-editor":
  tips.add(new Tip("party-name",greek?"Όνομα κόμματος":"Party name",greek?"Τα νέα κόμματα χρησιμοποιούν αυτό το όνομα και στις δύο γλώσσες.":"New parties use this name in both languages."));
  tips.add(new Tip("members",greek?"Μέλη συνασπισμού":"Coalition members",greek?"Ορίστε 1 για αυτοτελές κόμμα. Τα μέλη συνασπισμού μπορεί να επηρεάζουν το μπόνους.":"Use 1 for a standalone party; a coalition’s member count may affect bonus eligibility."));
  tips.add(new Tip("dialog-save",greek?"Αποθήκευση στοιχείων":"Save details",greek?"Εφαρμόστε τα στοιχεία του κόμματος στο σενάριο.":"Apply these party details to the scenario."));
  break;
 case "custom-party-edit":
 case "custom-party-editor":
  tips.add(new Tip("party-name",greek?"Ένα όνομα κόμματος":"One party name",greek?"Το ίδιο όνομα εμφανίζεται στα ελληνικά και αγγλικά.":"The same name appears in Greek and English."));
  tips.add(new Tip("candidates",greek?"Όριο υποψηφίων":"Candidate limit",greek?"Το κόμμα δεν καλύπτει περισσότερες έδρες από τους υποψηφίους του. Οι επιπλέον μεταφέρονται σε επιλέξιμα κόμματα με διαθέσιμους υποψηφίους, αλλιώς μένουν κενές.":"A party cannot fill more seats than its candidates. Surplus seats move to eligible parties with capacity; otherwise they stay vacant."));
  tips.add(new Tip("dialog-save",greek?"Προσθήκη ή αλλαγή κόμματος":"Add or update party",greek?"Αποθηκεύστε όνομα και υποψηφίους και μετά εισαγάγετε αποτέλεσμα.":"Save the name and candidate count, then enter its result."));
  break;
 case "results":
  tips.add(new Tip("seat-total",greek?"Η κατανομή σας":"Your allocation",greek?"Το διάγραμμα και οι γραμμές δείχνουν τις έδρες του σεναρίου.":"The chart and rows show the seats produced by your scenario."));
  tips.add(new Tip("seat-change-*",greek?"Μεταβολές εδρών":"Seat changes",greek?"Σύγκριση με τις αναγραφόμενες εκλογές, όχι με τη σημερινή σύνθεση.":"Compare with the named past election, not today’s parliamentary membership."));
  tips.add(new Tip("government-page",greek?"Δοκιμή συνεργασιών":"Test coalitions",greek?"Ανοίξτε τον προσομοιωτή για να ελέγξετε αν τα κόμματα φτάνουν 151 έδρες.":"Open the simulator to check whether selected parties reach 151 seats."));
  tips.add(new Tip("explanation-page",greek?"Δείτε τον υπολογισμό":"See the calculation",greek?"Ελέγξτε όρια, εκλογικά μέτρα, υπόλοιπα και μπόνους.":"Inspect thresholds, quotas, remainders and bonus allocation."));
  tips.add(new Tip("poll-source-page",greek?"Παραδοχές δημοσκόπησης":"Poll assumptions",greek?"Δείτε τη δημοσκόπηση και την πηγή της.":"Review the chosen poll and its source."));
  tips.add(new Tip("share",greek?"Κοινοποίηση εικόνας":"Share an image",greek?"Μοιραστείτε πλήρη εικόνα αποτελεσμάτων μέσω Android.":"Share the full results as an image using Android’s share sheet."));
  break;
 case "custom-results":
  tips.add(new Tip("vacant-seats",greek?"Κενές έδρες":"Vacant seats",greek?"Έδρες που δεν καλύπτονται από επιλέξιμους υποψηφίους μένουν κενές και γκρίζες στο διάγραμμα.":"Seats that eligible candidates cannot fill remain vacant and appear grey in the chart."));
  tips.add(new Tip("explanation-page",greek?"Έλεγχος ανακατανομής":"Inspect redistribution",greek?"Δείτε όρια υποψηφίων και μεταφορές μεταξύ επιλέξιμων κομμάτων.":"See candidate limits and any transfers between eligible parties."));
  tips.add(new Tip("share",greek?"Κοινοποίηση αποτελεσμάτων":"Share results",greek?"Εξαγάγετε πλήρη εικόνα αποτελεσμάτων.":"Export the complete results as an image."));
  break;
 case "coalition":
  tips.add(new Tip("coalition-choice",greek?"Επιλέξτε κόμματα συνεργασίας":"Select coalition parties",greek?"Επιλέξτε ένα ή περισσότερα κόμματα· 151 έδρες δίνουν κοινοβουλευτική πλειοψηφία.":"Tick one or more parties; 151 seats form a parliamentary majority."));
  tips.add(new Tip("government-total",greek?"Συνολικές έδρες":"Combined seats",greek?"Το σύνολο ενημερώνεται με κάθε αλλαγή επιλογής.":"This total updates as you change the selection."));
  break;
 case "explanation":
  tips.add(new Tip("explanation",greek?"Εξήγηση κατανομής":"Allocation audit",greek?"Διαβάστε τα βήματα με τη σειρά για να δείτε πώς προέκυψε το αποτέλεσμα.":"Read the steps in order to see how the result was derived."));
  break;
 case "poll-details":
  tips.add(new Tip("source-link*",greek?"Ελέγξτε την πηγή":"Check the source",greek?"Ανοίξτε τη δημοσίευση και ελέγξτε τις παραδοχές της σελίδας.":"Open the publication and review the assumptions shown on this page."));
  break;
 case "settings":
  tips.add(new Tip("language",greek?"Γλώσσα":"Language",greek?"Εναλλαγή ελληνικών και αγγλικών.":"Switch between Greek and English."));
  tips.add(new Tip("theme-system",greek?"Εμφάνιση":"Appearance",greek?"Επιλέξτε Φωτεινό, Σκοτεινό ή Σύστημα. Το Σύστημα ακολουθεί τη συσκευή και είναι η προεπιλογή.":"Choose Light, Dark or System. System follows your device and is the default."));
  tips.add(new Tip("sound",greek?"Ήχοι":"Sounds",greek?"Ενεργοποιήστε ή απενεργοποιήστε τους ήχους.":"Turn interaction sounds on or off."));
  tips.add(new Tip("tips-enabled",greek?"Συμβουλές πρώτης χρήσης":"First-use tips",greek?"Απενεργοποιήστε τις αυτόματες συμβουλές ή επιλέξτε Επανάληψη συμβουλών λειτουργιών για νέα αρχή.":"Disable automatic tips here, or use Replay feature tips to start them again."));
  tips.add(new Tip("refresh-polls",greek?"Ενημέρωση δημοσκοπήσεων":"Refresh polls",greek?"Ελέγξτε για νέες δημοσκοπήσεις χωρίς αλλαγή του ενεργού σεναρίου.":"Check for new polls without changing the active scenario."));
  tips.add(new Tip("auto-polls",greek?"Αυτόματη ενημέρωση":"Automatic refresh",greek?"Επιτρέψτε περιοδικό έλεγχο δημοσκοπήσεων όταν υπάρχει σύνδεση.":"Allow periodic poll checks when network access is available."));
  tips.add(new Tip("sources",greek?"Πηγές και απόρρητο":"Sources and privacy",greek?"Διαβάστε το πεδίο εφαρμογής, τις επίσημες πηγές και την πολιτική απορρήτου.":"Read the app’s scope, official sources and privacy policy."));
  break;
 case "more":
  tips.add(new Tip("save",greek?"Αποθήκευση σεναρίου":"Save a scenario",greek?"Κρατήστε αντίγραφο με όνομα τοπικά στη συσκευή.":"Keep a named copy locally on your device."));
  tips.add(new Tip("open",greek?"Άνοιγμα σεναρίου":"Open a scenario",greek?"Επαναφέρετε αποθηκευμένες τιμές και κανόνες.":"Restore a previously saved set of inputs and rules."));
  tips.add(new Tip("clear",greek?"Μηδενισμός τιμών":"Clear values",greek?"Μηδενίστε τις τρέχουσες ψήφους πριν από άλλο σενάριο.":"Reset the current vote inputs before creating another scenario."));
  break;
 case "save":
  tips.add(new Tip("scenario-name",greek?"Ονομάστε το σενάριο":"Name this scenario",greek?"Επιλέξτε αναγνωρίσιμο όνομα. Ίδιο όνομα αντικαθιστά το αποθηκευμένο σενάριο.":"Use a recognizable name. Reusing a name replaces that saved scenario."));
  tips.add(new Tip("dialog-save",greek?"Τοπική αποθήκευση":"Save locally",greek?"Αποθηκεύστε το σενάριο σε αυτή τη συσκευή.":"Store the scenario on this device."));
  break;
 case "open":
  tips.add(new Tip("scenario-list",greek?"Επαναφορά σεναρίου":"Restore a scenario",greek?"Επιλέξτε όνομα για να αντικαταστήσετε το τρέχον σενάριο με το αποθηκευμένο.":"Select a saved name to replace the current scenario with its saved state."));
  break;
 case "sources":
  tips.add(new Tip("privacy",greek?"Πολιτική απορρήτου":"Privacy policy",greek?"Δείτε τι αποθηκεύεται τοπικά και πότε γίνονται αιτήματα δικτύου.":"Read what is stored locally and when network requests occur."));
  tips.add(new Tip("source-link*",greek?"Αρχικές πηγές":"Original sources",greek?"Ανοίξτε επίσημα αποτελέσματα και νομοθεσία στον περιηγητή.":"Open official results and legislation in your browser."));
  break;
 case "privacy":
  tips.add(new Tip("android-message",greek?"Πληροφορίες απορρήτου":"Privacy information",greek?"Κυλήστε για την πολιτική. Το Κλείσιμο επιστρέφει στην εφαρμογή.":"Scroll to read the policy. Close returns to the app."));
  break;
 case "winner":
  tips.add(new Tip("winner-list",greek?"Επιλογή νικητή":"Winner selection",greek?"Επιλέξτε Αυτόματα ή δηλώστε τον νικητή επόμενου γύρου για το σενάριο.":"Use Automatic or declare the later-round winner required for your scenario."));
  break;
 case "transfers":
  tips.add(new Tip("transfer-*",greek?"Μεταφερόμενες ψήφοι":"Transferred votes",greek?"Εισαγάγετε μεταφορές μόνο προς φιναλίστ από αποκλεισμένους συνδυασμούς, στις μονάδες των αποτελεσμάτων.":"Enter transfers only to finalists, from eliminated lists, in the same units as your results."));
  tips.add(new Tip("dialog-save",greek?"Εφαρμογή μεταφορών":"Apply transfers",greek?"Εφαρμόστε τις τιμές στο τρέχον αυτοδιοικητικό σενάριο.":"Apply the values to the current local-election scenario."));
  break;
 case "lottery":
  tips.add(new Tip("lottery-list",greek?"Επίλυση ισοπαλίας":"Resolve a tie",greek?"Δηλώστε ρητά το αποτέλεσμα κλήρωσης· η εφαρμογή δεν επιλέγει τυχαίο νικητή.":"Declare the lottery outcome explicitly; the app does not choose a random winner for you."));
  break;
 case "format":
  tips.add(new Tip("dialog-save",greek?"Αλλαγή μονάδων":"Change input units",greek?"Επιβεβαιώστε μόνο αν θέλετε να μηδενίσετε τιμές και να αλλάξετε μορφή.":"Confirm only if you want to clear the existing values and switch format."));
  break;
 default:break;
 }
 if(feature.equals("votes")||feature.equals("local-votes")||feature.equals("custom-votes")){
  tips.add(new Tip("party-details",greek?"Στοιχεία κόμματος":"Party details",greek?"Πατήστε το όνομα για επεξεργασία ή αφαίρεση του κόμματος.":"Tap the name to edit or remove the party."));
  tips.add(new Tip("expand",greek?"Περισσότερα κόμματα":"More parties",greek?"Εμφανίστε ή συμπτύξτε την πλήρη λίστα.":"Expand or collapse the full party list."));
  tips.add(new Tip("remove-*",greek?"Αφαίρεση από το σενάριο":"Remove from scenario",greek?"Το × αφαιρεί το κόμμα από τις εισαγόμενες τιμές.":"The × removes the party from the current inputs."));
 }
 if(feature.endsWith("party-edit"))tips.add(new Tip("dialog-remove",greek?"Αφαίρεση κόμματος":"Remove party",greek?"Αφαιρέστε αυτό το κόμμα από το τρέχον σενάριο.":"Remove this party from the current scenario."));
 return tips;}
}
