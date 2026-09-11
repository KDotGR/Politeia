package gr.politeia.app;
import android.content.Context;
import android.graphics.*;
import android.view.View;
import java.util.*;
/** Lightweight, size-aware seat chamber. Exact totals remain accessible in the adjacent table. */
public final class SeatChart extends View {
 private final int[] seats,colors;private final Paint paint=new Paint(3);
 public SeatChart(Context c,int[] seats,int[] colors,String description){super(c);this.seats=seats.clone();this.colors=colors.clone();setContentDescription(description);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);}
 @Override protected void onDraw(Canvas c){
  super.onDraw(c);int total=Arrays.stream(seats).sum();if(total==0)return;
  float w=getWidth(),h=getHeight(),cx=w/2,cy=h-12,r=Math.min(w/2-12,h-24);
  if(total>600){float angle=180;for(int i=0;i<seats.length;i++){paint.setColor(colors[i]);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(r*.4f);c.drawArc(cx-r*.75f,cy-r*.75f,cx+r*.75f,cy+r*.75f,angle,180f*seats[i]/total,false,paint);angle+=180f*seats[i]/total;}paint.setStyle(Paint.Style.FILL);return;}
  int rows=Math.max(2,(int)Math.ceil(Math.sqrt(total/3.4)));int[] counts=new int[rows];double weight=0;for(int j=0;j<rows;j++)weight+=.42+.58*j/Math.max(1,rows-1);
  int assigned=0;for(int j=0;j<rows;j++){counts[j]=(int)Math.floor(total*(.42+.58*j/Math.max(1,rows-1))/weight);assigned+=counts[j];}for(int j=rows-1;assigned<total;j=(j+rows-1)%rows){counts[j]++;assigned++;}
  class Dot{float x,y;double angle;Dot(float x,float y,double a){this.x=x;this.y=y;angle=a;}}
  List<Dot> dots=new ArrayList<>();float dot=Math.min(r*.58f/rows*.37f,7*getResources().getDisplayMetrics().density);
  for(int row=0;row<rows;row++){float radius=r*(.42f+.58f*row/Math.max(1,rows-1));for(int j=0;j<counts[row];j++){double a=Math.PI*(counts[row]==1?.5:(double)j/(counts[row]-1));dots.add(new Dot(cx-(float)Math.cos(a)*radius,cy-(float)Math.sin(a)*radius,a));}}
  dots.sort(Comparator.comparingDouble(d->d.angle));int offset=0;for(int i=0;i<seats.length;i++){paint.setColor(colors[i]);for(int j=0;j<seats[i];j++){Dot d=dots.get(offset++);c.drawCircle(d.x,d.y,dot,paint);}}
 }
}
