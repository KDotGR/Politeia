package gr.politeia.app;
import android.app.job.*;
import android.content.*;
import java.util.concurrent.*;
public final class PollUpdateService extends JobService {
 private static final int ID=151;
 private ExecutorService executor;private Future<?> work;
 public static void schedule(Context c){JobScheduler scheduler=c.getSystemService(JobScheduler.class);if(!new PollRepository(c).enabled()){scheduler.cancel(ID);return;}if(scheduler.getPendingJob(ID)!=null)return;scheduler.schedule(new JobInfo.Builder(ID,new ComponentName(c,PollUpdateService.class)).setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).setPeriodic(PollRepository.INTERVAL).setPersisted(true).build());}
 @Override public boolean onStartJob(JobParameters params){executor=Executors.newSingleThreadExecutor();work=executor.submit(()->{boolean success=new PollRepository(this).refresh();if(!Thread.currentThread().isInterrupted())jobFinished(params,!success);executor.shutdown();});return true;}
 @Override public boolean onStopJob(JobParameters params){if(work!=null)work.cancel(true);if(executor!=null)executor.shutdownNow();return true;}
}
