package com.peng.jni;

import android.content.Context;
import android.net.wifi.WifiManager;


import java.util.Locale;

import static android.content.Context.WIFI_SERVICE;

/**
 * @author Anqiren
 * @package com.tencent.rtsp.Common
 * @create date 2018/8/10 3:42 AM
 * @describe TODO
 * @email anqirens@qq.com
 */

public class Util {
    private static Context mContext;
    private static String ipAddressFormatted = "";

    public static void setContext(Context context) {
        mContext = context;
    }

    public static String getLocalIpAddress() {

            WifiManager wifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(WIFI_SERVICE);
            int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
            ipAddressFormatted = String.format(Locale.ENGLISH, "%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                    (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
        return ipAddressFormatted;
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
