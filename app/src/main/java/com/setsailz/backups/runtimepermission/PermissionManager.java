package com.setsailz.backups.runtimepermission;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.annotation.Size;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * @author Setsail
 * @ClassName: PermissionManager
 * @Description: 6.0+运行时权限申请相关方法类。
 * 还提供了8.0+安装权限和6.0+悬浮窗权限的判断和请求方法，由于安装权限和悬浮窗权限不同于运行时权限，所以处理方式不一样。使用这两个权限前请详细了解这两个权限。
 * @date 2018/12/20 14:40
 */
public class PermissionManager {

    private Context mContext;
    private static PermissionManager sInstance;
    private HashMap<UUID, PermissionCallbacks> mPermissionCallbacksMap = new HashMap<UUID, PermissionCallbacks>();

    private PermissionManager(@NonNull Context context) {
        mContext = context.getApplicationContext();
    }

    public static PermissionManager getInstance(Context appContext) {
        if (sInstance == null) {
            synchronized (PermissionManager.class) {
                if (sInstance == null) {
                    sInstance = new PermissionManager(appContext);
                }
            }
        }

        return sInstance;
    }

    /**
     * 是否已经拥有这些权限
     *
     * @param context 可以是应用的Application Context
     * @param perms   检查的权限列表
     * @return 所有权限都被允许返回true，只要有一项被拒绝就返回false
     */
    public static boolean hasPermissions(@NonNull Context context,
                                         @Size(min = 1) @NonNull String... perms) {
        // Always return true for SDK < M, let the system deal with the permissions
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        // Null context may be passed if we have detected Low API (less than M) so getting
        // to this point with a null context should not be possible.
        if (context == null) {
            throw new IllegalArgumentException("Can't check permissions for null context");
        }

        for (String perm : perms) {
            if (ContextCompat.checkSelfPermission(context, perm)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    /**
     * 判断是否有安装APK的权限
     *
     * @param context
     * @return
     */
    public static boolean hasInstallPermission(@NonNull Context context) {
        // 8.0之前没有安装权限，默认返回true
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return true;
        } else {
            return context.getPackageManager().canRequestPackageInstalls();
        }
    }

    /**
     * 判断是否有悬浮窗权限
     *
     * @param context
     * @return
     */
    public static boolean hasDrawOverlaysPermission(@NonNull Context context) {
        // 6.0之前默认返回true
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        } else {
            return Settings.canDrawOverlays(context);
        }
    }

    /**
     * 判断权限列表中是否有被永久拒绝的权限
     * 判断的方法是首先确认这项权限是被拒绝的，然后再检查权限是否需要显示Rationale，如果一项权限被永久拒绝了，是不需要显示Rationale的
     *
     * @param deniedPerms 必须全都是被拒绝的权限，如果列表中有被允许的权限就会导致结果出错，因为被允许的权限也是不需要显示Rationale的
     * @return
     */
    public static boolean somePermissionPermanentlyDenied(@NonNull Activity host, @NonNull List<String> deniedPerms) {
        for (String deniedPerm : deniedPerms) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(host, deniedPerm)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 当用户永久拒绝某项权限，应用需要引导用户到系统设置页面打开权限时，弹出一个引导对话框
     *
     * @param rationale 对话框内容
     * @param callbacks 用户最终授权与否的回调
     * @param perms     需要请求的权限
     */
    public void showAppSettingDialog(@NonNull String rationale, @NonNull PermissionCallbacks callbacks, @Size(min = 1) @NonNull String... perms) {

        UUID uuid = UUID.randomUUID();
        mPermissionCallbacksMap.put(uuid, callbacks);

        startPermissionActivity(uuid, rationale, PermissionsActivity.TYPE_SHOW_APP_SETTING, perms);
    }

    /**
     * 请求权限，不传入rationale
     *
     * @param callbacks 请求权限整个流程结束后的回调
     * @param perms     要申请的权限列表
     */
    public void requestPermissions(@NonNull PermissionCallbacks callbacks, @Size(min = 1) @NonNull String... perms) {

        requestPermissions("", callbacks, perms);

    }

    /**
     * 请求权限
     *
     * @param rationale 如果向用户请求权限的第一次被拒绝了，第二次开始，就需要向用户展示一段文本，解释为何需要用户授予该权限
     * @param callbacks 请求权限整个流程结束后的回调
     * @param perms     要申请的权限列表
     */
    public void requestPermissions(@StringRes int rationale, @NonNull PermissionCallbacks callbacks, @Size(min = 1) @NonNull String... perms) {

        requestPermissions(mContext.getString(rationale), callbacks, perms);

    }

    /**
     * 请求权限
     *
     * @param rationale 如果向用户请求权限的第一次被拒绝了，第二次开始，就需要向用户展示一段文本，解释为何需要用户授予该权限
     *                  可以为空字符串，为空字符串就不弹出理由直接请求权限
     * @param callbacks 请求权限整个流程结束后的回调
     * @param perms     要申请的权限列表
     */
    public void requestPermissions(@NonNull String rationale, @NonNull PermissionCallbacks callbacks, @Size(min = 1) @NonNull String... perms) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            callbacks.onAllPermissionsGranted();
            return;
        }

        UUID uuid = UUID.randomUUID();
        mPermissionCallbacksMap.put(uuid, callbacks);

        startPermissionActivity(uuid, rationale, -1, perms);
    }

    /**
     * 请求安装APK权限，Android 8.0之后的版本用
     *
     * @param callbacks
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void requestInstallPermission(@NonNull PermissionCallbacks callbacks) {

        UUID uuid = UUID.randomUUID();
        mPermissionCallbacksMap.put(uuid, callbacks);

        startPermissionActivity(uuid, "", PermissionsActivity.TYPE_REQUEST_INSTALL_PERM);

    }

    /**
     * 请求安装APK权限，Android 8.0之后的版本用
     *
     * @param rationale 空字符串的话就不显示rationale直接跳转设置页
     * @param callbacks
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void requestInstallPermission(@NonNull String rationale, @NonNull PermissionCallbacks callbacks) {

        UUID uuid = UUID.randomUUID();
        mPermissionCallbacksMap.put(uuid, callbacks);

        startPermissionActivity(uuid, rationale, PermissionsActivity.TYPE_REQUEST_INSTALL_PERM);

    }

    /**
     * 请求悬浮窗权限
     *
     * @param callbacks
     */
    public void requestDrawOverlaysPermission(@NonNull PermissionCallbacks callbacks) {

        UUID uuid = UUID.randomUUID();
        mPermissionCallbacksMap.put(uuid, callbacks);

        startPermissionActivity(uuid, "", PermissionsActivity.TYPE_REQUEST_DRAW_OVERLAYS_PERM);

    }

    /**
     * 请求悬浮窗权限
     *
     * @param rationale
     * @param callbacks
     */
    public void requestDrawOverlaysPermission(@NonNull String rationale, @NonNull PermissionCallbacks callbacks) {

        UUID uuid = UUID.randomUUID();
        mPermissionCallbacksMap.put(uuid, callbacks);

        startPermissionActivity(uuid, rationale, PermissionsActivity.TYPE_REQUEST_DRAW_OVERLAYS_PERM);

    }

    private void startPermissionActivity(UUID uuid, String rationale, int bonusFuncType, @Size(min = 1) @NonNull String... perms) {
        Intent intent = new Intent(mContext, PermissionsActivity.class)
                .putExtra(PermissionsActivity.EXTRA_RATIONALE, rationale)
                .putExtra(PermissionsActivity.EXTRA_CALLBACK_UUID, uuid)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (perms.length > 0) {
            intent.putExtra(PermissionsActivity.EXTRA_PERMISSIONS, perms);
        }
        if (bonusFuncType >= 0) {
            intent.putExtra(PermissionsActivity.EXTRA_BONUS_FUNCTION_TYPE, bonusFuncType);
        }
        mContext.startActivity(intent);
    }

    protected void onSomePermissionsDenied(@NonNull UUID uuid, @NonNull List<String> perms) {
        PermissionCallbacks callbacks = mPermissionCallbacksMap.get(uuid);
        if (callbacks != null) {
            callbacks.onSomePermissionsDenied(perms);
            mPermissionCallbacksMap.remove(uuid);
        }
    }

    protected void onSomePermissionsPermanentlyDenied(@NonNull UUID uuid, @NonNull List<String> perms) {
        PermissionCallbacks callbacks = mPermissionCallbacksMap.get(uuid);
        if (callbacks != null) {
            callbacks.onSomePermissionsPermanentlyDenied(perms);
            mPermissionCallbacksMap.remove(uuid);
        }
    }

    protected void onAllPermissionsGranted(@NonNull UUID uuid) {
        PermissionCallbacks callbacks = mPermissionCallbacksMap.get(uuid);
        if (callbacks != null) {
            callbacks.onAllPermissionsGranted();
            mPermissionCallbacksMap.remove(uuid);
        }
    }

    /**
     * 申请权限结果的回调
     */
    public interface PermissionCallbacks {

        /**
         * 只要请求的权限列表中有一项被拒绝，就会回调这个方法
         *
         * @param perms 被拒绝的权限
         */
        void onSomePermissionsDenied(List<String> perms);

        /**
         * 只要请求的权限列表中有一项被永久拒绝，就会回调这个方法
         *
         * @param perms 被拒绝的权限(包括被永久拒绝的)
         */
        void onSomePermissionsPermanentlyDenied(List<String> perms);

        /**
         * 所有的权限均被授权才会回调
         */
        void onAllPermissionsGranted();

    }

}
