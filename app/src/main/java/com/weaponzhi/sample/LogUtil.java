package com.weaponzhi.sample;

import android.util.Log;

/**
 * LogUtil 日志工具类
 * <p>
 * author: 张冠之 <br>
 * time:   2017/03/07 22:24 <br>
 * GitHub: https://github.com/WeaponZhi
 * blog:   http://weaponzhi.online
 * CSDN:   http://blog.csdn.net/qq_34795285
 * </p>
 */

public class LogUtil {
    private static final String TAG = "Imooc_okhttp";
    private static boolean debug = true;

    public static void e(String msg) {
        if (debug)
            Log.e(TAG, msg);
    }
}
