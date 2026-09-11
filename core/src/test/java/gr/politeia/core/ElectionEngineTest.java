package gr.politeia.core;

import org.junit.Test;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.Assert.*;

/** Written before the allocation engine. Regression data comes from the Ministry's archived results. */
public class ElectionEngineTest {
    private ElectionEngine.Input input(ElectionEngine.Rule rule, String... values) {
        List<ElectionEngine.Party> parties = new ArrayList<>();
        for (int i=0;i<values.length;i++) parties.add(new ElectionEngine.Party("p"+i, new BigDecimal(values[i]), 1));
        return new ElectionEngine.Input(rule, parties, false);
    }
    private void seats(ElectionEngine.Result r, int... expected) {
        assertArrayEquals(expected, r.seats);
        assertEquals(Arrays.stream(expected).sum(), Arrays.stream(r.seats).sum());
        assertFalse(r.steps.isEmpty());
    }
    @Test public void june2023OfficialVotes() {
        ElectionEngine.Input in=input(ElectionEngine.Rule.parliament(true),
            "2115322","930013","617487","401224","243922","231491","193124","165523","130378");
        in.otherVotes=new BigDecimal("186723");
        seats(ElectionEngine.calculate(in),158,47,32,21,12,12,10,8,0);
    }
    @Test public void may2023OfficialVotes() {
        // Minor parties kept separate so a combined 'other' does not incorrectly cross 3%.
        ElectionEngine.Input in=input(ElectionEngine.Rule.parliament(false),"2407750","1184621","676165","426633","262498");
        in.otherVotes=new BigDecimal("945096");
        seats(ElectionEngine.calculate(in),146,71,41,26,16);
    }
    @Test public void bonusBoundariesAreExact() {
        assertEquals(0,ElectionEngine.parliamentBonus(new BigDecimal("24.999999")));
        assertEquals(20,ElectionEngine.parliamentBonus(new BigDecimal("25")));
        assertEquals(20,ElectionEngine.parliamentBonus(new BigDecimal("25.499999")));
        assertEquals(21,ElectionEngine.parliamentBonus(new BigDecimal("25.5")));
        assertEquals(49,ElectionEngine.parliamentBonus(new BigDecimal("39.99999")));
        assertEquals(50,ElectionEngine.parliamentBonus(new BigDecimal("40")));
        assertEquals(50,ElectionEngine.parliamentBonus(new BigDecimal("100")));
    }
    @Test public void customStepBonus() {
        ElectionEngine.Rule rule=ElectionEngine.Rule.custom(100,"0","20",0,"5",1,20);
        ElectionEngine.Input in=input(rule,"35","30","25","10"); in.percent=true;
        assertEquals(3,ElectionEngine.calculate(in).bonus);
        assertEquals(100,Arrays.stream(ElectionEngine.calculate(in).seats).sum());
    }
    @Test public void thresholdAndExcludedVotes() {
        ElectionEngine.Input in=input(ElectionEngine.Rule.parliament(false),"50","44","3","2.999999");
        in.percent=true; in.otherVotes=new BigDecimal("0.000001");
        ElectionEngine.Result r=ElectionEngine.calculate(in);
        assertTrue(r.seats[2]>0); assertEquals(0,r.seats[3]);
    }
    @Test public void percentageMustTotal100() {
        ElectionEngine.Input in=input(ElectionEngine.Rule.parliament(true),"40","30"); in.percent=true;
        assertThrows(IllegalArgumentException.class,()->ElectionEngine.calculate(in));
    }
    @Test public void negativeAndZeroRejected() {
        assertThrows(IllegalArgumentException.class,()->ElectionEngine.calculate(input(ElectionEngine.Rule.european(),"-1","10")));
        assertThrows(IllegalArgumentException.class,()->ElectionEngine.calculate(input(ElectionEngine.Rule.european(),"0","0")));
    }
    @Test public void fractionalVoteCountsRejected() {
        assertThrows(IllegalArgumentException.class,()->ElectionEngine.calculate(input(ElectionEngine.Rule.european(),"10.5","20")));
    }
    @Test public void tiedWinnerNeedsDecision() {
        assertThrows(ElectionEngine.TieException.class,()->ElectionEngine.calculate(input(ElectionEngine.Rule.parliament(true),"50","50")));
    }
    @Test public void unresolvedRemainderDoesNotSilentlyFavorInputOrder() {
        assertThrows(ElectionEngine.TieException.class,()->ElectionEngine.calculate(input(ElectionEngine.Rule.custom(3,"0","100",0,"1",0,0),"1","1")));
    }
    @Test public void noEligiblePartyRejected() {
        ElectionEngine.Input in=input(ElectionEngine.Rule.parliament(true),"2","1"); in.otherVotes=new BigDecimal("97");
        assertThrows(IllegalArgumentException.class,()->ElectionEngine.calculate(in));
    }
    @Test public void localNeedsRunoffWinnerAtExactly43Percent() {
        assertThrows(IllegalArgumentException.class,()->ElectionEngine.calculate(input(ElectionEngine.Rule.local(43),"4300","3500","2200")));
    }
    @Test public void localRunoffUsesFirstRoundVotes() {
        ElectionEngine.Input in=input(ElectionEngine.Rule.local(43),"4100","3500","2400"); in.winnerId="p1";
        ElectionEngine.Result r=ElectionEngine.calculate(in); assertEquals(26,r.seats[1]); assertEquals(43,Arrays.stream(r.seats).sum());
    }
    @Test public void localWinnerAbove60UsesProportionalBranch() {
        ElectionEngine.Result r=ElectionEngine.calculate(input(ElectionEngine.Rule.local(43),"70000","20000","10000"));
        assertTrue(r.seats[0]>26); assertEquals(43,Arrays.stream(r.seats).sum());
    }
    @Test public void localCannotChooseNonFinalist() {
        ElectionEngine.Input in=input(ElectionEngine.Rule.local(43),"4100","3500","2400"); in.winnerId="p2";
        assertThrows(IllegalArgumentException.class,()->ElectionEngine.calculate(in));
    }
    @Test public void conservationAcrossRandomCustomElections() {
        Random random=new Random(5314);
        for(int k=0;k<1000;k++) {
            String[] values=new String[3+random.nextInt(12)];
            for(int i=0;i<values.length;i++) values[i]=""+(1+random.nextInt(1000000));
            int total=1+random.nextInt(600);
            try { ElectionEngine.Result r=ElectionEngine.calculate(input(ElectionEngine.Rule.custom(total,"0","100",0,"1",0,0),values));
                assertEquals(total,Arrays.stream(r.seats).sum()); for(int n:r.seats)assertTrue(n>=0);
            } catch(ElectionEngine.TieException legitimateTie) { }
        }
    }
}
