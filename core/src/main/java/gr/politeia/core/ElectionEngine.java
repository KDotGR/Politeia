package gr.politeia.core;

import java.math.*;
import java.util.*;

/** Pure Java domain engine. All allocation comparisons use exact decimal/integer arithmetic. */
public final class ElectionEngine {
 private ElectionEngine() {}
 private static final BigDecimal HUNDRED=new BigDecimal("100");
 public enum Kind { PARLIAMENT, SIMPLE, EU, LOCAL, LOCAL_2026, CUSTOM }
 public static final class Rule {
  public final Kind kind; public final int seats,baseBonus,stepSeats,maxBonus;
  public final BigDecimal threshold,bonusStart,stepPercent;
  private Rule(Kind kind,int seats,String threshold,String start,int base,String step,int increment,int max){
   this.kind=kind;this.seats=seats;this.threshold=new BigDecimal(threshold);bonusStart=new BigDecimal(start);baseBonus=base;stepPercent=new BigDecimal(step);stepSeats=increment;maxBonus=max;
   if(seats<1||seats>10000||this.threshold.signum()<0||this.threshold.compareTo(HUNDRED)>0||bonusStart.signum()<0||bonusStart.compareTo(HUNDRED)>0||stepPercent.signum()<=0||base<0||increment<0||max<base||max>seats)throw new IllegalArgumentException("CONFIG");
  }
  public static Rule parliament(boolean reinforced){return new Rule(reinforced?Kind.PARLIAMENT:Kind.SIMPLE,300,"3","25",reinforced?20:0,"0.5",reinforced?1:0,reinforced?50:0);}
  public static Rule european(){return new Rule(Kind.EU,21,"3","100",0,"1",0,0);}
  public static Rule local(int seats){return new Rule(Kind.LOCAL,seats,"3","43",0,"1",0,0);}
  public static Rule local2026(int seats){return new Rule(Kind.LOCAL_2026,seats,"3","42",0,"1",0,0);}
  public static Rule custom(int seats,String threshold,String start,int base,String step,int increment,int cap){return new Rule(Kind.CUSTOM,seats,threshold,start,base,step,increment,cap);}
  public boolean local(){return kind==Kind.LOCAL||kind==Kind.LOCAL_2026;}
 }
 public static final class Party {
  public final String id; public final BigDecimal value; public final int members,candidates;
  public Party(String id,BigDecimal value,int members){this(id,value,members,Integer.MAX_VALUE);}
  public Party(String id,BigDecimal value,int members,int candidates){this.id=id;this.value=value;this.members=members;this.candidates=candidates;}
 }
 public static final class Input {
  public final Rule rule; public final List<Party> parties; public boolean percent;
  /** Explicit aggregate of individually excluded lists, never treated as one eligible party. */
  public BigDecimal otherVotes=BigDecimal.ZERO;
  public String winnerId;
  /** Certified or scenario lottery order, highest priority first; empty means do not resolve ties. */
  public List<String> tieOrder=new ArrayList<>();
  /** Alternative votes transferred from non-finalists; first preferences remain the seat denominator. */
  public Map<String,BigDecimal> transfers=new HashMap<>();
  public Input(Rule rule,List<Party> parties,boolean percent){this.rule=rule;this.parties=new ArrayList<>(parties);this.percent=percent;}
 }
 public static final class Step {
  public final String code;public final int party;public final String[] args;
  Step(String code,int party,Object... args){this.code=code;this.party=party;this.args=Arrays.stream(args).map(Object::toString).toArray(String[]::new);}
 }
 public static final class Result {
  public final int[] seats; public int bonus,vacantSeats;public int winner=-1;
  public final List<Step> steps=new ArrayList<>();public final BigDecimal[] percentages;
  Result(int n){seats=new int[n];percentages=new BigDecimal[n];}
 }
 public static final class TieException extends IllegalArgumentException {
  public final List<String> parties;
  TieException(List<String> parties){super("TIE");this.parties=parties;}
 }
 public static int parliamentBonus(BigDecimal percent){return percent.compareTo(new BigDecimal("25"))<0?0:Math.min(50,20+percent.subtract(new BigDecimal("25")).divideToIntegralValue(new BigDecimal("0.5")).intValue());}
 public static int municipalSeats(int population,int units){
  if(population<1||units<1)throw new IllegalArgumentException("CONFIG");
  int[] bounds={2000,5000,10000,30000,50000,100000,150000,Integer.MAX_VALUE}, counts={13,15,19,25,29,35,39,43};
  int i=0;while(population>bounds[i])i++;if(units>=5)i=Math.min(7,i+1);return counts[i];
 }
 public static int regionalSeats(int population,int regionId,boolean current){
  if(population<1)throw new IllegalArgumentException("CONFIG");
  if(regionId==9)return current?101:85;if(regionId==12)return current?51:45;
  return population<=300000?35:population<=800000?45:61;
 }
 public static Result calculate(Input in){
  Rule rule=in.rule;int n=in.parties.size();if(n==0||n>500)throw new IllegalArgumentException("PARTIES");
  BigDecimal total=in.otherVotes;if(total==null||total.signum()<0)throw new IllegalArgumentException("NUMBER");
  checkNumber(total,in.percent);Set<String> ids=new HashSet<>();
  for(Party p:in.parties){if(p.id==null||p.id.trim().isEmpty()||!ids.add(p.id)||p.members<1||p.members>100)throw new IllegalArgumentException("PARTIES");if(rule.kind==Kind.CUSTOM&&p.candidates<0)throw new IllegalArgumentException("CANDIDATES");checkNumber(p.value,in.percent);total=total.add(p.value);}
  if(total.signum()<=0)throw new IllegalArgumentException("ZERO");
  if(in.percent&&total.compareTo(HUNDRED)!=0)throw new IllegalArgumentException("TOTAL");
  Result out=new Result(n);out.steps.add(new Step("VALID",-1,total.toPlainString(),rule.seats));
  if(in.percent)out.steps.add(new Step("ESTIMATE",-1));
  if(in.otherVotes.signum()>0)out.steps.add(new Step("REST",-1,in.otherVotes.toPlainString(),in.percent?"%":"",rule.threshold));
  List<Integer> eligible=new ArrayList<>();BigDecimal[] votes=new BigDecimal[n];
  for(int i=0;i<n;i++){
   Party p=in.parties.get(i);out.percentages[i]=p.value.multiply(HUNDRED).divide(total,8,RoundingMode.HALF_UP);
   votes[i]=in.percent?p.value.movePointRight(6):p.value;
   boolean ok=p.value.signum()>0&&p.value.multiply(HUNDRED).compareTo(total.multiply(rule.threshold))>=0;
   if(ok)eligible.add(i);out.steps.add(new Step(ok?"ELIGIBLE":"EXCLUDED",i,out.percentages[i].stripTrailingZeros().toPlainString(),rule.threshold));
  }
  if(eligible.isEmpty())throw new IllegalArgumentException("ELIGIBLE");
  if(rule.local()) {
   List<Integer> ordered=new ArrayList<>(eligible);ordered.sort((a,b)->votes[b].compareTo(votes[a]));
   int leader=ordered.get(0);boolean direct=in.parties.get(leader).value.multiply(HUNDRED).compareTo(total.multiply(rule.bonusStart))>0;
   if(ordered.size()>1&&votes[leader].compareTo(votes[ordered.get(1)])==0)direct=false;
   int winner=leader;
   if(!direct){
    BigDecimal cutoff=votes[ordered.get(Math.min(1,ordered.size()-1))];List<Integer> finalists=new ArrayList<>();
    for(int i:ordered)if(votes[i].compareTo(cutoff)>=0)finalists.add(i);
    if(rule.kind==Kind.LOCAL_2026&&!in.transfers.isEmpty()){
     BigDecimal available=total;for(int i:finalists)available=available.subtract(in.parties.get(i).value);
     BigDecimal used=BigDecimal.ZERO;Map<Integer,BigDecimal> scores=new HashMap<>();
     for(String id:in.transfers.keySet())if(finalists.stream().noneMatch(i->in.parties.get(i).id.equals(id)))throw new IllegalArgumentException("TRANSFERS");
     for(int i:finalists){BigDecimal t=in.transfers.getOrDefault(in.parties.get(i).id,BigDecimal.ZERO);checkNumber(t,in.percent);used=used.add(t);scores.put(i,in.parties.get(i).value.add(t));out.steps.add(new Step("TRANSFER",i,t,scores.get(i)));}
     if(used.compareTo(available)>0)throw new IllegalArgumentException("TRANSFERS");
     finalists.sort((a,b)->scores.get(b).compareTo(scores.get(a)));winner=chooseFirst(in,finalists,scores,out);
    }else{
     if(in.winnerId==null)throw new IllegalArgumentException(rule.kind==Kind.LOCAL?"RUNOFF":"SECOND_STAGE");
     winner=-1;for(int i:finalists)if(in.parties.get(i).id.equals(in.winnerId))winner=i;
     if(winner<0)throw new IllegalArgumentException("FINALIST");
     out.steps.add(new Step(rule.kind==Kind.LOCAL?"RUNOFF":"CERTIFIED",winner));
    }
   }else if(in.winnerId!=null&&!in.winnerId.equals(in.parties.get(winner).id))throw new IllegalArgumentException("DIRECT");
   out.winner=winner;
   if(in.parties.get(winner).value.multiply(HUNDRED).compareTo(total.multiply(new BigDecimal("60")))>0){out.steps.add(new Step("LOCAL_PROPORTIONAL",winner));allocate(in,out,eligible,votes,rule.seats,true);}
   else{
    int guaranteed=(rule.seats*3+2)/5;out.seats[winner]=guaranteed;out.steps.add(new Step("GUARANTEE",winner,guaranteed,rule.seats-guaranteed));
    List<Integer> rest=new ArrayList<>(eligible);rest.remove(Integer.valueOf(winner));
    if(rest.isEmpty()){out.seats[winner]=rule.seats;out.steps.add(new Step("ONLY",winner,rule.seats));}
    else allocate(in,out,rest,votes,rule.seats-guaranteed,true);
   }
  }else{
   int winner=-1,bonus=0;
   if(rule.maxBonus>0){
    List<Integer> ranked=new ArrayList<>(eligible);ranked.sort((a,b)->votes[b].compareTo(votes[a]));
    winner=ranked.get(0);
    if(in.parties.get(winner).value.multiply(HUNDRED).compareTo(total.multiply(rule.bonusStart))>=0)winner=chooseFirst(in,ranked,indexMap(votes),out);
    if(rule.kind==Kind.PARLIAMENT&&in.parties.get(winner).members>1){
     List<Integer> singles=new ArrayList<>();for(int i:ranked)if(in.parties.get(i).members==1)singles.add(i);
     if(singles.isEmpty())throw new IllegalArgumentException("COALITION");
     int single=chooseFirst(in,singles,indexMap(votes),out);
     if(votes[winner].compareTo(votes[single].multiply(BigDecimal.valueOf(in.parties.get(winner).members)))<=0)winner=single;
     out.steps.add(new Step("COALITION",winner));
    }
    BigDecimal value=in.parties.get(winner).value;
    BigDecimal above=value.multiply(HUNDRED).subtract(total.multiply(rule.bonusStart));
    if(above.signum()>=0){BigDecimal increments=above.divideToIntegralValue(total.multiply(rule.stepPercent));bonus=BigDecimal.valueOf(rule.baseBonus).add(increments.multiply(BigDecimal.valueOf(rule.stepSeats))).min(BigDecimal.valueOf(rule.maxBonus)).intValueExact();}
    out.winner=winner;out.bonus=bonus;
   }
   out.steps.add(new Step("BONUS",winner,bonus,rule.seats-bonus,rule.bonusStart,rule.baseBonus,rule.stepPercent,rule.stepSeats,rule.maxBonus));
   allocate(in,out,eligible,votes,rule.seats-bonus,rule.kind==Kind.EU);
   if(winner>=0)out.seats[winner]+=bonus;
  }
  if(rule.kind==Kind.CUSTOM)applyCandidateLimits(in,out,eligible,votes);
  if(Arrays.stream(out.seats).sum()+out.vacantSeats!=rule.seats)throw new IllegalStateException("Seat conservation failed");
  out.steps.add(new Step("CHECK",-1,Arrays.stream(out.seats).sum()));return out;
 }
 /** Reallocate only among eligible lists that still have candidates, until capacity is exhausted. */
 private static void applyCandidateLimits(Input in,Result out,List<Integer> eligible,BigDecimal[] votes){
  int excess=0;
  for(int i:eligible){int removed=Math.max(0,out.seats[i]-in.parties.get(i).candidates);if(removed>0){out.seats[i]-=removed;excess+=removed;out.steps.add(new Step("CANDIDATE_CAP",i,removed));}}
  while(excess>0){
   List<Integer> available=new ArrayList<>();for(int i:eligible)if(out.seats[i]<in.parties.get(i).candidates)available.add(i);
   if(available.isEmpty()){out.vacantSeats=excess;out.steps.add(new Step("VACANT_SEATS",-1,excess));return;}
   Result transfer=new Result(out.seats.length);allocate(in,transfer,available,votes,excess,false);
   // The same explicit lottery policy applies when a transfer's last seat is tied.
   for(Step step:transfer.steps)if(step.code.equals("LOTTERY"))out.steps.add(step);
   int distributed=0;
   for(int i:available){int gained=Math.min(transfer.seats[i],in.parties.get(i).candidates-out.seats[i]);if(gained>0){out.seats[i]+=gained;distributed+=gained;out.steps.add(new Step("CANDIDATE_TRANSFER",i,gained));}}
   if(distributed==0)throw new IllegalStateException("Candidate redistribution made no progress");
   excess-=distributed;
  }
 }
 private static void checkNumber(BigDecimal n,boolean percent){if(n==null||n.signum()<0||n.precision()>24||n.scale()>8||(!percent&&n.stripTrailingZeros().scale()>0))throw new IllegalArgumentException("NUMBER");}
 private static Map<Integer,BigDecimal> indexMap(BigDecimal[] a){Map<Integer,BigDecimal> m=new HashMap<>();for(int i=0;i<a.length;i++)m.put(i,a[i]);return m;}
 private static int chooseFirst(Input in,List<Integer> ordered,Map<Integer,BigDecimal> values,Result out){
  int a=ordered.get(0);List<Integer> same=new ArrayList<>();for(int i:ordered)if(values.get(i).compareTo(values.get(a))==0)same.add(i);
  if(same.size()==1)return a;return resolve(in,same,out);
 }
 private static int resolve(Input in,List<Integer> tied,Result out){
  for(String id:in.tieOrder)for(int i:tied)if(in.parties.get(i).id.equals(id)){out.steps.add(new Step("LOTTERY",i));return i;}
  List<String> ids=new ArrayList<>();for(int i:tied)ids.add(in.parties.get(i).id);throw new TieException(ids);
 }
 private static void allocate(Input in,Result out,List<Integer> indices,BigDecimal[] votes,int count,boolean integerQuota){
  if(count==0)return;
  BigDecimal total=BigDecimal.ZERO;for(int i:indices)total=total.add(votes[i]);
  BigDecimal divisor=BigDecimal.valueOf(count+(in.rule.local()?1:0));
  BigDecimal quota=integerQuota?total.divide(divisor,0,RoundingMode.DOWN):total;
  if(quota.signum()==0)throw new IllegalArgumentException("QUOTA_ZERO");
  out.steps.add(new Step(integerQuota?"INTEGER_QUOTA":"HARE_QUOTA",-1,total.toPlainString(),divisor,quota.toPlainString(),count));
  Map<Integer,BigDecimal> remainders=new HashMap<>();int assigned=0;
  for(int i:indices){BigDecimal numerator=integerQuota?votes[i]:votes[i].multiply(BigDecimal.valueOf(count));
   BigDecimal[] qr=numerator.divideAndRemainder(quota);int base=qr[0].intValueExact();out.seats[i]+=base;assigned+=base;remainders.put(i,qr[1]);
   out.steps.add(new Step("QUOTIENT",i,numerator,quota,base,qr[1]));}
  List<Integer> order=new ArrayList<>(indices);
  boolean remove=assigned>count;order.sort((a,b)->remove?remainders.get(a).compareTo(remainders.get(b)):remainders.get(b).compareTo(remainders.get(a)));
  int needed=Math.abs(count-assigned);
  while(needed>0){
   List<Integer> candidates=new ArrayList<>();for(int i:order)if(!remove||out.seats[i]>0)candidates.add(i);
   if(candidates.isEmpty())throw new IllegalStateException("No remainder candidates");
   int first=candidates.get(0);List<Integer> tied=new ArrayList<>();for(int i:candidates)if(remainders.get(i).compareTo(remainders.get(first))==0)tied.add(i);
   // Ties only matter if they straddle the final available seat.
   if(tied.size()>needed)first=resolve(in,tied,out);
   out.seats[first]+=remove?-1:1;out.steps.add(new Step(remove?"REMOVE":"REMAINDER",first,remainders.get(first)));order.remove(Integer.valueOf(first));needed--;
   if(order.isEmpty()&&needed>0){order=new ArrayList<>(indices);order.sort((a,b)->votes[a].compareTo(votes[b]));for(int i:order)remainders.put(i,votes[i].negate());}
  }
 }
}
