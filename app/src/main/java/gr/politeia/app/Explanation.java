package gr.politeia.app;
import gr.politeia.core.ElectionEngine;
public final class Explanation {
 public static String format(ElectionEngine.Step s,boolean el,String party){
  String[] a=s.args;String p=party.isEmpty()?"":party+": ";
  switch(s.code){
   case "REST":return el?"Λοιπά κόμματα: "+a[0]+a[1]+" συνολικά. Κάθε κόμμα θεωρείται κάτω από "+a[2]+"%. Περιλαμβάνονται στο σύνολο ψήφων, αλλά δεν λαμβάνουν έδρες ή μπόνους.":"Rest parties: "+a[0]+a[1]+" combined. Each is assumed below "+a[2]+"%. Included in total votes, but receives no seats or bonus.";
   case "VALID":return el?"Έγκυρα ψηφοδέλτια / ποσοστά: "+a[0]+". Προς κατανομή: "+a[1]+" έδρες. Λευκά και άκυρα εξαιρούνται.":"Valid votes / percentages: "+a[0]+". Allocate "+a[1]+" seats. Blank and invalid ballots are excluded.";
   case "ESTIMATE":return el?"Εκτίμηση από ποσοστά. Τα ποσοστά μετατρέπονται σε μονάδες × 1.000.000. Για ακριβή ακέραια εκλογικά μέτρα χρησιμοποιήστε ψήφους· τα στρογγυλεμένα ποσοστά μπορεί να αλλάξουν την κατανομή.":"Percentage estimate. Shares are represented in units × 1,000,000. Use vote counts for exact integer quotas; rounded percentages can change the allocation.";
   case "ELIGIBLE":return p+a[0]+"% ≥ "+a[1]+"% → "+(el?"συμμετοχή στην κατανομή.":"eligible for allocation.");
   case "EXCLUDED":return p+a[0]+"% → "+(el?"χωρίς έδρες (κάτω από το όριο ή μηδενικές ψήφοι).":"no seats (below threshold or zero votes).");
   case "BONUS":return (el?"Μπόνους: ":"Bonus: ")+a[0]+". "+(el?"Αναλογικές έδρες: ":"Proportional seats: ")+a[1]+". "+(Integer.parseInt(a[0])==0?"":p)+(el?"Τύπος: αν p ≥ ":"Formula: if p ≥ ")+a[2]+"%, min("+a[6]+", "+a[3]+" + floor((p − "+a[2]+") / "+a[4]+") × "+a[5]+").";
   case "HARE_QUOTA":return el?"Αναλογική κατανομή: ψήφοι κάθε κόμματος × "+a[3]+" / "+a[0]+" επιλέξιμες ψήφους.":"Proportional allocation: each party’s votes × "+a[3]+" / "+a[0]+" eligible votes.";
   case "INTEGER_QUOTA":return (el?"Ακέραιο εκλογικό μέτρο: ":"Integer electoral quota: ")+"floor("+a[0]+" / "+a[1]+") = "+a[2]+". "+(el?"Η κατανομή αφορά ":"This pool contains ")+a[3]+(el?" έδρες.":" seats.");
   case "QUOTIENT":return p+a[0]+" / "+a[1]+" → "+a[2]+(el?" αρχικές έδρες, υπόλοιπο ":" initial seats, remainder ")+a[3]+".";
   case "REMAINDER":return p+(el?"+1 έδρα από το αχρησιμοποίητο υπόλοιπο ":"+1 seat from unused remainder ")+a[0]+".";
   case "REMOVE":return p+(el?"−1 πλεονάζουσα έδρα, με βάση το μικρότερο υπόλοιπο ":"−1 excess seat, using the smallest remainder ")+a[0]+".";
   case "CHECK":return (el?"Έλεγχος: το άθροισμα όλων των εδρών είναι ":"Check: the sum of all allocated seats is ")+a[0]+". ✓";
   case "RUNOFF":return p+(el?"επιλεγμένος νικητής β΄ γύρου. Για τις έδρες χρησιμοποιούνται οι ψήφοι του α΄ γύρου.":"selected runoff winner. Seats use first-round votes.");
   case "CERTIFIED":return p+(el?"δηλωμένος νικητής δεύτερου σταδίου. Οι εναλλακτικές ψήφοι δεν αυξάνουν τις αναλογικές έδρες.":"declared second-stage winner. Alternative votes do not increase the proportional seat share.");
   case "TRANSFER":return p+(el?"εναλλακτικές ψήφοι ":"alternative votes ")+a[0]+" → "+(el?"σύνολο δεύτερου σταδίου ":"second-stage total ")+a[1]+".";
   case "GUARANTEE":return p+(el?"λαμβάνει στρογγυλοποιημένα τα 3/5 = ":"receives rounded 3/5 = ")+a[0]+(el?" έδρες. Οι υπόλοιποι συνδυασμοί μοιράζονται ":" seats. Other eligible lists share ")+a[1]+".";
   case "LOCAL_PROPORTIONAL":return p+(el?"ποσοστό πάνω από 60%: αναλογική κατανομή όλων των εδρών.":"share above 60%: allocate all seats proportionally.");
   case "ONLY":return p+(el?"μοναδικός επιλέξιμος συνδυασμός: ":"only eligible list: ")+a[0]+(el?" έδρες.":" seats.");
   case "LOTTERY":return p+(el?"επίλυση ισοπαλίας με τη σειρά κλήρωσης που δηλώσατε.":"tie resolved using your declared lottery order.");
   case "COALITION":return p+(el?"επιλέχθηκε για το μπόνους μετά τη σύγκριση της μέσης δύναμης του συνασπισμού με το πρώτο αυτοτελές κόμμα.":"selected for the bonus after comparing coalition average strength with the leading standalone party.");
   default:return s.code;
  }
 }
 public static String error(String code,boolean el){
  switch(code==null?"":code){
   case "OVER_TOTAL":return el?"Τα ποσοστά δεν μπορούν να υπερβαίνουν το 100%.":"Entered percentages cannot exceed 100%.";
   case "TOTAL":return el?"Το σύνολο των ποσοστών πρέπει να είναι ακριβώς 100%. Συμπληρώστε όλους τους συνδυασμούς.":"Percentages must total exactly 100%. Enter all lists.";
   case "ZERO":return el?"Εισαγάγετε τουλάχιστον μία θετική τιμή.":"Enter at least one positive value.";
   case "NUMBER":return el?"Χρησιμοποιήστε μη αρνητικούς αριθμούς. Οι ψήφοι πρέπει να είναι ακέραιες. Μέχρι 8 δεκαδικά στα ποσοστά.":"Use non-negative numbers. Vote counts must be whole numbers. Percentages support up to 8 decimal places.";
   case "ELIGIBLE":return el?"Κανένας συνδυασμός δεν φτάνει το εκλογικό όριο.":"No list reaches the electoral threshold.";
   case "RUNOFF":return el?"Κανένας συνδυασμός δεν υπερβαίνει το 43%. Επιλέξτε τον νικητή του β΄ γύρου.":"No list exceeds 43%. Select the runoff winner.";
   case "SECOND_STAGE":return el?"Κανένας συνδυασμός δεν υπερβαίνει το 42%. Εισαγάγετε εναλλακτικές ψήφους ή επιλέξτε τον δηλωμένο νικητή.":"No list exceeds 42%. Enter alternative votes or select the declared second-stage winner.";
   case "FINALIST":return el?"Ο νικητής πρέπει να είναι μεταξύ των πρώτων δύο (συμπεριλαμβανομένων ισοπαλιών).":"The winner must be among the top two lists (including ties).";
   case "DIRECT":return el?"Υπάρχει νικητής από το πρώτο στάδιο. Επιλέξτε αυτόματη ανάδειξη νικητή.":"There is a first-stage winner. Choose automatic winner selection.";
   case "TRANSFERS":return el?"Οι μεταφερόμενες ψήφοι πρέπει να προέρχονται μόνο από μη φιναλίστ και να μην υπερβαίνουν το σύνολό τους.":"Transfers must come only from non-finalists and cannot exceed their combined votes.";
   case "QUOTA_ZERO":return el?"Πολύ λίγες ψήφοι για θετικό ακέραιο εκλογικό μέτρο. Εισαγάγετε πραγματικούς αριθμούς ψήφων.":"Too few votes for a positive integer quota. Enter actual vote counts.";
   case "PARTIES":return el?"Εισαγάγετε μοναδικά ονόματα συνδυασμών και έγκυρο αριθμό μελών συνασπισμού.":"Enter unique list names and a valid coalition member count.";
   case "COALITION":return el?"Χρειάζεται τουλάχιστον ένα αυτοτελές κόμμα για σύγκριση του μπόνους συνασπισμού.":"At least one standalone party is needed for the coalition bonus comparison.";
   case "CONFIG":return el?"Ελέγξτε τις έδρες (1–10.000), τα όρια (0–100), το θετικό βήμα και το ανώτατο μπόνους.":"Check seats (1–10,000), thresholds (0–100), a positive step, and a bonus cap no greater than total seats.";
   case "TIE":return el?"Ισοπαλία που επηρεάζει την κατανομή. Δηλώστε αποτέλεσμα κλήρωσης για να συνεχίσετε.":"A tie affects allocation. Declare a lottery outcome to continue.";
   default:return el?"Ελέγξτε τα δεδομένα και τις ρυθμίσεις.":"Check your inputs and settings.";
  }
 }
}
