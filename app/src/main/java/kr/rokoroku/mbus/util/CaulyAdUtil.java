package kr.rokoroku.mbus.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.fsn.cauly.CaulyAdInfo;
import com.fsn.cauly.CaulyNativeAdInfoBuilder;
import com.fsn.cauly.CaulyNativeAdView;
import com.fsn.cauly.CaulyNativeAdViewListener;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

import kr.rokoroku.mbus.R;

/**
 * Created by rok on 2015. 7. 30..
 */
public class CaulyAdUtil {

    private static Map<String, SoftReference<CaulyNativeAdView>> sAdViewMap;

    private static CaulyNativeAdView getNativeAdView(String tag) {
        if (sAdViewMap != null) {
            SoftReference<CaulyNativeAdView> reference = sAdViewMap.get(tag);
            if (reference != null) {
                CaulyNativeAdView adView = reference.get();
                if (adView != null) {
                    return adView;
                } else {
                    sAdViewMap.remove(tag);
                }
            }
        }
        return null;
    }

    private static void putNativeAdView(String tag, CaulyNativeAdView nativeAdView) {
        CaulyNativeAdView storedAdView = getNativeAdView(tag);
        if (storedAdView != null) {
            storedAdView.destroy();
            sAdViewMap.remove(tag);
        }

        if (sAdViewMap == null) {
            sAdViewMap = new HashMap<>();
        }
        sAdViewMap.put(tag, new SoftReference<>(nativeAdView));
    }

    public static void requestAd(Context context, String tag, CaulyNativeAdViewListener listener) {
        CaulyNativeAdView nativeAdView = getNativeAdView(tag);
        if (nativeAdView != null) {
            listener.onReceiveNativeAd(nativeAdView, true);

        } else if (isNetworkAvailable(context)) {
            String appCode = context.getString(R.string.cauly_app_key);
            CaulyAdInfo adInfo = new CaulyNativeAdInfoBuilder(appCode)
                    .layoutID(R.layout.row_ad_view)
                    .iconImageID(R.id.icon)
                    .titleID(R.id.title)
                            //.subtitleID(R.id.subtitle)
                    .textID(R.id.text)
                    .clickingView(R.id.card_view)
                    .sponsorPosition(R.id.sponsor, CaulyAdInfo.Direction.RIGHT)
                    .build();

            CaulyNativeAdView nativeAd = new CaulyNativeAdView(context);
            nativeAd.setAdInfo(adInfo);
            nativeAd.setAdViewListener(listener);
            nativeAd.request();
            putNativeAdView(tag, nativeAd);

        } else {
            listener.onFailedToReceiveNativeAd(null, -1, "NETWORK_UNAVAILABLE");
        }
    }

    public static void requestAd2(Context context, String tag, CaulyNativeAdViewListener listener) {
        CaulyNativeAdView nativeAdView = getNativeAdView(tag);
        if (nativeAdView != null) {
            listener.onReceiveNativeAd(nativeAdView, true);

        } else if (isNetworkAvailable(context)) {
            String appCode = context.getString(R.string.cauly_app_key);
            CaulyAdInfo adInfo = new CaulyNativeAdInfoBuilder(appCode)
                    .layoutID(R.layout.row_ad_view_small)
                    .iconImageID(R.id.icon)
                    .titleID(R.id.title)
                    .textID(R.id.text)
                    .clickingView(R.id.ad_layout)
                    .build();

            CaulyNativeAdView nativeAd = new CaulyNativeAdView(context);
            nativeAd.setAdInfo(adInfo);
            nativeAd.setAdViewListener(listener);
            nativeAd.request();
            putNativeAdView(tag, nativeAd);

        } else {
            listener.onFailedToReceiveNativeAd(null, -1, "NETWORK_UNAVAILABLE");
        }
    }

    public static void removeAd(String tag) {
        if (sAdViewMap != null) {
            CaulyNativeAdView nativeAdView = getNativeAdView(tag);
            if (nativeAdView != null) {
                nativeAdView.destroy();
            }
            sAdViewMap.remove(tag);
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivity == null) {
            return false;

        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) for (NetworkInfo anInfo : info) {
                if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        }
        return false;
    }

    public static abstract class SimpleCaulyNativeAdListener implements CaulyNativeAdViewListener {
        @Override
        public void onFailedToReceiveNativeAd(CaulyNativeAdView caulyNativeAdView, int i, String s) {

        }
    }
}
