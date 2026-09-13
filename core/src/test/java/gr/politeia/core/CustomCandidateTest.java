package gr.politeia.core;

import java.math.BigDecimal;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class CustomCandidateTest {
 private ElectionEngine.Party party(String id,int votes,int candidates){return new ElectionEngine.Party(id,BigDecimal.valueOf(votes),1,candidates);}
 private ElectionEngine.Rule simple(int seats,String threshold){return ElectionEngine.Rule.custom(seats,threshold,"100",0,"1",0,0);}
 private ElectionEngine.Input input(ElectionEngine.Rule rule,ElectionEngine.Party... parties){return new ElectionEngine.Input(rule,Arrays.asList(parties),false);}
 private void check(ElectionEngine.Result r,int vacant,int... seats){assertArrayEquals(seats,r.seats);assertEquals(vacant,r.vacantSeats);}
 @Test public void bonusWinnerCannotExceedCandidates(){
  ElectionEngine.Result r=ElectionEngine.calculate(input(ElectionEngine.Rule.custom(10,"0","50",2,"1",0,2),party("a",60,3),party("b",40,10)));
  check(r,0,3,7);assertTrue(r.steps.stream().anyMatch(s->s.code.equals("CANDIDATE_CAP")&&s.party==0&&s.args[0].equals("4")));assertTrue(r.steps.stream().anyMatch(s->s.code.equals("CANDIDATE_TRANSFER")&&s.party==1));
 }
 @Test public void transferCascadesPastAnotherFullList(){check(ElectionEngine.calculate(input(simple(10,"0"),party("a",60,1),party("b",30,4),party("c",10,10))),0,1,4,5);}
 @Test public void insufficientCapacityLeavesVacancies(){ElectionEngine.Result r=ElectionEngine.calculate(input(simple(10,"0"),party("a",60,2),party("b",40,3)));check(r,5,2,3);assertTrue(r.steps.stream().anyMatch(s->s.code.equals("VACANT_SEATS")&&s.args[0].equals("5")));}
 @Test public void belowThresholdAndZeroVotesNeverReceiveTransfers(){check(ElectionEngine.calculate(input(simple(10,"3"),party("a",98,2),party("b",2,100),party("c",0,100))),8,2,0,0);}
 @Test public void allZeroCandidatesLeavesAllSeatsVacant(){check(ElectionEngine.calculate(input(simple(10,"0"),party("a",60,0),party("b",40,0))),10,0,0);}
 @Test public void remainderTieRequiresExplicitLottery(){
  ElectionEngine.Input in=input(simple(10,"0"),party("a",60,5),party("b",20,10),party("c",20,10));
  ElectionEngine.TieException tie=assertThrows(ElectionEngine.TieException.class,()->ElectionEngine.calculate(in));assertEquals(Arrays.asList("b","c"),tie.parties);
  in.tieOrder=Arrays.asList("c");ElectionEngine.Result r=ElectionEngine.calculate(in);check(r,0,5,2,3);assertTrue(r.steps.stream().anyMatch(s->s.code.equals("LOTTERY")&&s.party==2));
 }
 @Test public void unlimitedConstructorPreservesAllocation(){check(ElectionEngine.calculate(input(simple(10,"0"),new ElectionEngine.Party("a",BigDecimal.valueOf(60),1),new ElectionEngine.Party("b",BigDecimal.valueOf(40),1))),0,6,4);}
 @Test public void noncustomRulesIgnoreCandidateLimits(){check(ElectionEngine.calculate(input(ElectionEngine.Rule.parliament(true),party("a",60,0),party("b",40,1))),0,200,100);}
 @Test public void negativeCustomCapacityIsRejected(){assertThrows(IllegalArgumentException.class,()->ElectionEngine.calculate(input(simple(10,"0"),party("a",100,-1))));}
 @Test public void percentagesAndVoteCountsHaveSameTransfers(){ElectionEngine.Input in=input(simple(10,"0"),party("a",60,1),party("b",30,4),party("c",10,10));in.percent=true;check(ElectionEngine.calculate(in),0,1,4,5);}
}
