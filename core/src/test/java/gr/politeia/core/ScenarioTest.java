package gr.politeia.core;
import java.math.BigDecimal;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;
public class ScenarioTest {
 private ElectionEngine.Input input(ElectionEngine.Rule rule, String... shares){List<ElectionEngine.Party> p=new ArrayList<>();for(int i=0;i<shares.length;i++)p.add(new ElectionEngine.Party("p"+i,new BigDecimal(shares[i]),1));return new ElectionEngine.Input(rule,p,true);}
 @Test public void remainderIsExcludedAndBonusUsesAllVotes(){ElectionEngine.Input in=input(ElectionEngine.Rule.parliament(true),"25","30");Scenario.completePercentages(in);assertEquals(new BigDecimal("45"),in.otherVotes);ElectionEngine.Result r=ElectionEngine.calculate(in);assertArrayEquals(new int[]{123,177},r.seats);assertEquals(30,r.bonus);assertTrue(r.steps.stream().anyMatch(s->s.code.equals("REST")));}
 @Test public void fullInputHasNoRemainder(){ElectionEngine.Input in=input(ElectionEngine.Rule.parliament(true),"60","40");Scenario.completePercentages(in);assertEquals(0,in.otherVotes.signum());}
 @Test(expected=IllegalArgumentException.class) public void over100Rejected(){Scenario.completePercentages(input(ElectionEngine.Rule.european(),"60","40.01"));}
 @Test(expected=IllegalArgumentException.class) public void negativeRejected(){Scenario.completePercentages(input(ElectionEngine.Rule.european(),"60","-1"));}
 @Test public void belowThreeStillExcluded(){ElectionEngine.Input in=input(ElectionEngine.Rule.european(),"50","3","2.99");Scenario.completePercentages(in);ElectionEngine.Result r=ElectionEngine.calculate(in);assertEquals(0,r.seats[2]);assertEquals(21,Arrays.stream(r.seats).sum());}
 @Test public void localSupportsExcludedRemainder(){ElectionEngine.Input in=input(ElectionEngine.Rule.local(43),"45","30");Scenario.completePercentages(in);ElectionEngine.Result r=ElectionEngine.calculate(in);assertArrayEquals(new int[]{26,17},r.seats);}
 @Test public void localRunoffStillRequired(){ElectionEngine.Input in=input(ElectionEngine.Rule.local(43),"25","30");Scenario.completePercentages(in);assertThrows(IllegalArgumentException.class,()->ElectionEngine.calculate(in));in.winnerId="p0";assertArrayEquals(new int[]{26,17},ElectionEngine.calculate(in).seats);}
 @Test public void customThreePercentSupported(){ElectionEngine.Input in=input(ElectionEngine.Rule.custom(100,"3","20",0,"5",0,0),"25","30");Scenario.completePercentages(in);assertEquals(new BigDecimal("45"),in.otherVotes);}
 @Test public void voteCountsUnchanged(){ElectionEngine.Input in=input(ElectionEngine.Rule.european(),"25","30");in.percent=false;Scenario.completePercentages(in);assertEquals(0,in.otherVotes.signum());}
 @Test public void differentThresholdStillRequiresFullTotal(){ElectionEngine.Input in=input(ElectionEngine.Rule.custom(100,"0","20",0,"5",0,0),"25","30");Scenario.completePercentages(in);assertThrows(IllegalArgumentException.class,()->ElectionEngine.calculate(in));}
 @Test public void coalitionBoundaries(){assertFalse(Scenario.hasMajority(150));assertTrue(Scenario.hasMajority(151));assertTrue(Scenario.hasMajority(300));assertEquals(151,Scenario.coalitionSeats(new int[]{100,51,149},new boolean[]{true,true,false}));assertEquals(0,Scenario.coalitionSeats(new int[]{100,51,149},new boolean[]{false,false,false}));}
 @Test public void singlePartyAndNoDoubleCounting(){assertEquals(177,Scenario.coalitionSeats(new int[]{123,177},new boolean[]{false,true}));}
}
