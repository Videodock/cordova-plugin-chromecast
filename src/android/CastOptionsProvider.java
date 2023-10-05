package acidhax.cordova.chromecast;

import java.util.List;

import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.framework.CastOptions;
import com.google.android.gms.cast.framework.OptionsProvider;
import com.google.android.gms.cast.framework.SessionProvider;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public final class CastOptionsProvider implements OptionsProvider {

    protected String getReceiverApplicationId(Context context) {
        String appId = null;
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            appId = packageInfo.applicationInfo.metaData.getString("com.google.android.gms.cast.framework.RECEIVER_APPLICATION_ID");
        } catch (PackageManager.NameNotFoundException e) {
        }

        return appId != null ? appId : CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID;
    }

    @Override
    public CastOptions getCastOptions(Context context) {
        return new CastOptions.Builder()
                .setReceiverApplicationId(getReceiverApplicationId(context))
                .build();
    }

    @Override
    public List<SessionProvider> getAdditionalSessionProviders(Context context) {
        return null;
    }
}
