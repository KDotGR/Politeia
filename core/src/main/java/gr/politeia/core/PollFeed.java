package gr.politeia.core;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
/** Reads only the individual-poll matrix, never a trend, seat projection or commentary. */
public final class PollFeed {
 public static final String URL="https://politpro.eu/en/greece";
 public static final class Party {
  public final String id,en,el,color;public final BigDecimal percent;
  Party(String id,String en,String el,String color,BigDecimal percent){this.id=id;this.en=en;this.el=el;this.color=color;this.percent=percent;}
 }
 public static final class Poll {
  public final String firm,date,source; public final List<Party> parties;public final BigDecimal other;
  Poll(String firm,String date,String source,List<Party> parties,BigDecimal other){this.firm=firm;this.date=date;this.source=source;this.parties=Collections.unmodifiableList(parties);this.other=other;}
 }
 private static final Map<String,String> GREEK=new HashMap<>();
 static {GREEK.put("ND","Νέα Δημοκρατία");GREEK.put("ELAS","Ελληνική Αριστερή Συμπαράταξη");GREEK.put("PASOK","ΠΑΣΟΚ");GREEK.put("EL","Ελληνική Λύση");GREEK.put("KKE","ΚΚΕ");GREEK.put("PE","Πλεύση Ελευθερίας");GREEK.put("FL","Φωνή Λογικής");GREEK.put("ELPIDA","Ελπίδα για τη Δημοκρατία");GREEK.put("MeRA25","ΜέΡΑ25");GREEK.put("SYRIZA","ΣΥΡΙΖΑ");GREEK.put("N","Νίκη");GREEK.put("NA","Νέα Αριστερά");GREEK.put("DPK","Δημοκράτες");GREEK.put("Spart.","Σπαρτιάτες");GREEK.put("EgtP","Έλληνες για την Πατρίδα");}

 public static List<Poll> parse(String html,LocalDate today){
  Element table=Jsoup.parse(html,URL).selectFirst("table.institute-polls-matrix");
  if(table==null)throw new IllegalArgumentException("Poll table unavailable");
  Elements headers=table.select("thead th");
  if(headers.size()<6||!headers.get(1).text().equals("Institute")||!headers.get(2).text().equals("Date")||!headers.get(headers.size()-2).text().equals("Other"))throw new IllegalArgumentException("Poll schema changed");
  List<Poll> polls=new ArrayList<>();Set<String> seen=new HashSet<>();
  for(Element row:table.select("tbody tr")){try{
   Elements cells=row.select("td");if(cells.size()!=headers.size())continue;
   String source=row.attr("data-href");if(!source.startsWith(URL+"/opinion-polls/")||!source.endsWith("/parliamentary-election"))continue;
   Matcher date=Pattern.compile("(\\d{4}-\\d{2}-\\d{2})/parliamentary-election$").matcher(source);if(!date.find()||LocalDate.parse(date.group(1)).isAfter(today))continue;
   String firm=cells.get(1).text();if(firm.isEmpty()||firm.toLowerCase(Locale.ROOT).contains("politpro"))continue;
   List<Party> parties=new ArrayList<>();BigDecimal sum=BigDecimal.ZERO;Set<String> ids=new HashSet<>();
   for(int i=3;i<headers.size()-2;i++){
    Element cell=cells.get(i).selectFirst("[data-matrix-cell=percent]");if(cell==null)throw new IllegalArgumentException();String value=cell.text();if(value.equals("–")||value.equals("—"))continue;
    BigDecimal percentage=number(value);Element link=headers.get(i).selectFirst("a[data-party-tip]");if(link==null)throw new IllegalArgumentException();
    String slug=link.attr("href").substring(link.attr("href").lastIndexOf('/')+1);if(!ids.add(slug))throw new IllegalArgumentException();String name=link.text(),color=link.attr("data-party-tip-color");if(!color.matches("#[0-9a-fA-F]{6}"))color="#64748B";
    parties.add(new Party("poll-"+slug,name,GREEK.getOrDefault(name,link.attr("data-party-tip")),color,percentage));sum=sum.add(percentage);
   }
   Element rest=cells.get(headers.size()-2).selectFirst("[data-matrix-cell=percent]");if(rest==null)continue;BigDecimal other=number(rest.text());
   // Never silently classify a missing undecided/abstention category as small parties.
   if(parties.size()<2||sum.compareTo(new BigDecimal("100"))>0||sum.add(other).subtract(new BigDecimal("100")).abs().compareTo(new BigDecimal("0.5"))>0)continue;
   if(seen.add(source))polls.add(new Poll(firm,date.group(1),source,parties,other));
  }catch(RuntimeException invalid){/* Invalid rows do not replace valid cached polls. */}}
  if(polls.isEmpty())throw new IllegalArgumentException("No valid dated polls");polls.sort(Comparator.comparing((Poll p)->p.date).reversed());return polls;
 }
 private static BigDecimal number(String s){if(!s.matches("\\d{1,3}(\\.\\d{1,4})?"))throw new IllegalArgumentException();BigDecimal n=new BigDecimal(s);if(n.compareTo(new BigDecimal("100"))>0)throw new IllegalArgumentException();return n;}
}
