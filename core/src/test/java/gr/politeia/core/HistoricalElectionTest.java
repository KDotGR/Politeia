package gr.politeia.core;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.math.BigDecimal;
import static org.junit.Assert.*;
@RunWith(Parameterized.class)
public class HistoricalElectionTest {
 @Parameterized.Parameters(name="{0}") public static Collection<Object[]> data() throws Exception {
  List<Object[]> rows=new ArrayList<>();
  try(BufferedReader r=new BufferedReader(new InputStreamReader(HistoricalElectionTest.class.getResourceAsStream("/historical.tsv"),StandardCharsets.UTF_8))){String line;while((line=r.readLine())!=null)rows.add(new Object[]{line.split("\\t",-1)[0],line});}return rows;
 }
 final String line;
 public HistoricalElectionTest(String id,String line){this.line=line;}
 @Test public void matchesPublishedSeatAllocation(){
  String[] f=line.split("\\t",-1), votes=f[5].split(","), expected=f[6].split(",");
  ElectionEngine.Rule rule=f[1].equals("LOCAL")?ElectionEngine.Rule.local(Integer.parseInt(f[2])):f[1].equals("EU")?ElectionEngine.Rule.european():ElectionEngine.Rule.parliament(f[1].equals("PARLIAMENT"));
  List<ElectionEngine.Party> parties=new ArrayList<>();int[] seats=new int[votes.length];
  for(int i=0;i<votes.length;i++){parties.add(new ElectionEngine.Party("p"+i,new BigDecimal(votes[i]),1));seats[i]=Integer.parseInt(expected[i]);}
  ElectionEngine.Input in=new ElectionEngine.Input(rule,parties,false);
  // Winner ID in the fixture is converted to input order by the importer.
  if(f[1].equals("LOCAL"))in.winnerId="p"+f[4];
  assertEquals(new BigDecimal(f[3]),parties.stream().map(p->p.value).reduce(BigDecimal.ZERO,BigDecimal::add));
  assertArrayEquals(seats,ElectionEngine.calculate(in).seats);
 }
}
