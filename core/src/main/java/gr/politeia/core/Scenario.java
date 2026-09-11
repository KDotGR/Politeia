package gr.politeia.core;
import java.math.BigDecimal;
/** User scenario operations, separate from strict certified-result allocation. */
public final class Scenario {
 private Scenario() {}
 public static void completePercentages(ElectionEngine.Input input) {
  if(!input.percent || input.rule.threshold.compareTo(new BigDecimal("3"))!=0)return;
  BigDecimal sum=BigDecimal.ZERO;
  for(ElectionEngine.Party p:input.parties){if(p.value==null||p.value.signum()<0)throw new IllegalArgumentException("NUMBER");sum=sum.add(p.value);}
  if(sum.compareTo(new BigDecimal("100"))>0)throw new IllegalArgumentException("OVER_TOTAL");
  input.otherVotes=new BigDecimal("100").subtract(sum);
 }
 public static int coalitionSeats(int[] seats,boolean[] selected){
  if(seats.length!=selected.length)throw new IllegalArgumentException("Selection size");
  int sum=0;for(int i=0;i<seats.length;i++){if(seats[i]<0)throw new IllegalArgumentException("Negative seats");if(selected[i])sum=Math.addExact(sum,seats[i]);}return sum;
 }
 public static boolean hasMajority(int seats){return seats>=151;}
}
