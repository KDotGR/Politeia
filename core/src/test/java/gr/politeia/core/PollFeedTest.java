package gr.politeia.core;
import org.junit.Test;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import static org.junit.Assert.*;
public class PollFeedTest {
 private String fixture()throws Exception {try(var in=getClass().getResourceAsStream("/polls.html")){return new String(in.readAllBytes(),StandardCharsets.UTF_8);}}
 @Test public void actualFeedHasDatedIndividualPolls()throws Exception{var polls=PollFeed.parse(fixture(),LocalDate.of(2026,9,10));assertTrue(polls.size()>=10);var p=polls.get(0);assertEquals("GPO",p.firm);assertEquals("2026-09-08",p.date);assertEquals("30.6",p.parties.get(0).percent.toPlainString());assertEquals("4.6",p.other.toPlainString());}
 @Test public void futureRowsExcluded()throws Exception{var polls=PollFeed.parse(fixture(),LocalDate.of(2026,9,1));assertTrue(polls.stream().noneMatch(p->p.firm.equals("GPO")));}
 @Test public void malformedPageRejected(){assertThrows(IllegalArgumentException.class,()->PollFeed.parse("<html>Unavailable</html>",LocalDate.of(2026,9,10)));}
 @Test public void impossiblePercentagesRejectRow()throws Exception{var p=PollFeed.parse(fixture().replace(">30.6<",">130.6<"),LocalDate.of(2026,9,10));assertTrue(p.stream().noneMatch(x->x.firm.equals("GPO")));}
 @Test public void rawUndecidedOrMissingVotesNotImputedAsOther()throws Exception{var p=PollFeed.parse(fixture().replace(">30.6<",">25.8<"),LocalDate.of(2026,9,10));assertTrue(p.stream().noneMatch(x->x.firm.equals("GPO")));}
 @Test public void publishedPartiesRetainThresholdPrecision()throws Exception{var p=PollFeed.parse(fixture(),LocalDate.of(2026,9,10)).stream().filter(x->x.firm.equals("Interview")).findFirst().get();assertEquals(0,p.parties.stream().filter(x->x.en.equals("MeRA25")).findFirst().get().percent.compareTo(new java.math.BigDecimal("3")));}
}
