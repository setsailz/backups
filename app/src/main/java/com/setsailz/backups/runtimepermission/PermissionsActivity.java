package com.setsailz.backups.runtimepermission;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.View;

import com.setsailz.backups.widgets.CommonDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Setsail
 * @ClassName: PermissionsActivity
 * @Description: 6.0+权限申请host Activity
 * @date 2018/12/21 16:35
 */
public class PermissionsActivity extends Activity {

    private final int PERMISSIONS_REQUEST_CODE = 100;
    private final int SETTINGS_REQUEST_CODE = 101;
    private final int INSTALL_PERMISSION_REQUEST_CODE = 102;
    private final int DRAW_OVERLAYS_PERMISSION_REQUEST_CODE = 103;
    static final String EXTRA_CALLBACK_UUID = "callback_uuid";
    static final String EXTRA_PERMISSIONS = "runtime_permissions";
    /**
     * 如果rationale传入空字符串或null，表示不弹出rationale直接请求权限
     */
    static final String EXTRA_RATIONALE = "rationale";

    /**
     * 运行时权限之外的功能，包含安装权限、悬浮窗权限、打开设置页面
     */
    static final String EXTRA_BONUS_FUNCTION_TYPE = "bonus_function_type";

    static final int TYPE_REQUEST_INSTALL_PERM = 0;
    static final int TYPE_REQUEST_DRAW_OVERLAYS_PERM = 1;
    static final int TYPE_SHOW_APP_SETTING = 2;

    private UUID mUUID;
    private String[] mPermissions;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        Intent intent = getIntent();

        mPermissions = intent.getStringArrayExtra(EXTRA_PERMISSIONS);
        mUUID = (UUID) intent.getSerializableExtra(EXTRA_CALLBACK_UUID);
        String rationale = intent.getStringExtra(EXTRA_RATIONALE);

        // 如果是运行时权限之外的功能
        int bonusFuncType = intent.getIntExtra(EXTRA_BONUS_FUNCTION_TYPE, -1);
        if (bonusFuncType >= 0) {
            switch (bonusFuncType) {
                case TYPE_REQUEST_INSTALL_PERM:
                    showSettingRationale(rationale, Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, INSTALL_PERMISSION_REQUEST_CODE);
                    return;
                case TYPE_REQUEST_DRAW_OVERLAYS_PERM:
                    showSettingRationale(rationale, Settings.ACTION_MANAGE_OVERLAY_PERMISSION, DRAW_OVERLAYS_PERMISSION_REQUEST_CODE);
                    return;
                case TYPE_SHOW_APP_SETTING:
                    showSettingRationale(rationale, Settings.ACTION_APPLICATION_DETAILS_SETTINGS, SETTINGS_REQUEST_CODE);
                    return;
                default:
                    break;
            }
        }

        if (shouldShowRequestPermissionRationale(mPermissions) && !TextUtils.isEmpty(rationale)) {
            showRationale(rationale);
        } else {
            ActivityCompat.requestPermissions(this, mPermissions, PERMISSIONS_REQUEST_CODE);
        }
    }

    private boolean shouldShowRequestPermissionRationale(String[] perms) {
        for (String perm : perms) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, perm)) {
                return true;
            }
        }
        return false;
    }

    private void showSettingRationale(String rationale, final String settingAction, final int requestCode) {

        if (TextUtils.isEmpty(rationale)) {
            goToSettingPage(settingAction, requestCode);
        } else {
            showRationaleDialog(rationale, "去设置", new RationaleCallbacks() {
                @Override
                public void onRationaleAccepted() {
                    goToSettingPage(settingAction, requestCode);
                }

                @Override
                public void onRationaleDenied() {
                }
            });
        }
    }

    private void goToSettingPage(String settingAction, int requestCode) {
        Intent intent = new Intent(settingAction)
                .setData(Uri.fromParts("package", getPackageName(), null));
        PackageManager pm = getPackageManager();
        ComponentName cn = intent.resolveActivity(pm);
        if (cn != null) {
            startActivityForResult(intent, requestCode);
        }
    }

    private void showRationale(String rationale) {

        showRationaleDialog(rationale, "去允许", new RationaleCallbacks() {
            @Override
            public void onRationaleAccepted() {
                ActivityCompat.requestPermissions(PermissionsActivity.this, mPermissions, PERMISSIONS_REQUEST_CODE);
            }

            @Override
            public void onRationaleDenied() {
            }
        });

    }

    private void showRationaleDialog(String rationale, String positiveBtn, final RationaleCallbacks callbacks) {
        new CommonDialog.Builder(this)
                .setMainText(rationale)
                .setPositiveButton(positiveBtn, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        callbacks.onRationaleAccepted();
                    }
                }).build().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        onRequestPermissionsResult(permissions, grantResults);
        finish();
    }

    private void onRequestPermissionsResult(@NonNull String[] permissions,
                                            @NonNull int[] grantResults) {
        List<String> denied = new ArrayList<String>();
        for (int i = 0; i < permissions.length; i++) {
            String perm = permissions[i];
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                denied.add(perm);
            }
        }

        if (!denied.isEmpty()) {
            onSomePermissionDenied(denied);
        } else {
            PermissionManager.getInstance(this).onAllPermissionsGranted(mUUID);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 以下是从setting页面返回的逻辑，重新检查一次权限的授予状态
        if (requestCode == SETTINGS_REQUEST_CODE) {
            if (PermissionManager.hasPermissions(this, mPermissions)) {
                PermissionManager.getInstance(this).onAllPermissionsGranted(mUUID);
            } else {
                List<String> denied = new ArrayList<String>();
                for (int i = 0; i < mPermissions.length; i++) {
                    String perm = mPermissions[i];
                    if (!PermissionManager.hasPermissions(this, perm)) {
                        denied.add(perm);
                    }
                }
                onSomePermissionDenied(denied);
            }
        } else if (requestCode == INSTALL_PERMISSION_REQUEST_CODE) {// 安装权限
            if (PermissionManager.hasInstallPermission(this)) {
                PermissionManager.getInstance(this).onAllPermissionsGranted(mUUID);
            } else {
                List<String> denied = new ArrayList<String>();
                denied.add(Manifest.permission.REQUEST_INSTALL_PACKAGES);
                PermissionManager.getInstance(this).onSomePermissionsDenied(mUUID, denied);
            }
        } else if (requestCode == DRAW_OVERLAYS_PERMISSION_REQUEST_CODE) {// 系统级悬浮窗权限
            if (PermissionManager.hasDrawOverlaysPermission(this)) {
                PermissionManager.getInstance(this).onAllPermissionsGranted(mUUID);
            } else {
                List<String> denied = new ArrayList<String>();
                denied.add(Manifest.permission.SYSTEM_ALERT_WINDOW);
                PermissionManager.getInstance(this).onSomePermissionsDenied(mUUID, denied);
            }
        }
        finish();
    }

    private void onSomePermissionDenied(List<String> denied) {
        if (PermissionManager.somePermissionPermanentlyDenied(this, denied)) {
            PermissionManager.getInstance(this).onSomePermissionsPermanentlyDenied(mUUID, denied);
        } else {
            PermissionManager.getInstance(this).onSomePermissionsDenied(mUUID, denied);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }
}