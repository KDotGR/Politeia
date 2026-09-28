package gr.politeia.app;

import android.content.SharedPreferences;

/** Install-scoped state: updates and future feature guides must not reset this marker. */
final class InstallationOnboarding {
 private InstallationOnboarding(){}
 static boolean claimFirstLaunch(SharedPreferences onboarding,SharedPreferences legacy){
  if(onboarding.getBoolean("installationIntroShown",false))return false;
  boolean returning=legacy.contains("draft")||legacy.contains("walkthroughSeen")
   ||legacy.contains("customWalkthroughSeen")||legacy.contains("featureTips");
  for(String key:legacy.getAll().keySet())if(key.startsWith("guide.v1."))returning=true;
  // Persist before showing, so exiting mid-introduction cannot replay it on a later launch.
  // A restored DialogFragment retains its page during rotation within this launch.
  onboarding.edit().putBoolean("installationIntroShown",true).commit();
  return !returning;
 }
}
