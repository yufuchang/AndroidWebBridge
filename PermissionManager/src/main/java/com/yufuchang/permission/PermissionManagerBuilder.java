package com.yufuchang.permission;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.support.annotation.StringRes;

import java.util.ArrayList;

/**
 * Created by yufuchang on 2019/10/1.
 */
public abstract class PermissionManagerBuilder<T extends PermissionManagerBuilder> {

    private static final String PREFS_NAME_PERMISSION = "PREFS_NAME_PERMISSION";
    private static final String PREFS_IS_FIRST_REQUEST = "PREFS_IS_FIRST_REQUEST";

    private PermissionManagerListener listener;
    private String[] permissions;
    private CharSequence rationaleTitle;
    private CharSequence rationaleMessage;
    private CharSequence denyTitle;
    private CharSequence denyMessage;
    private CharSequence settingButtonText;
    private boolean hasSettingBtn = true;

    private CharSequence deniedCloseButtonText;
    private CharSequence rationaleConfirmText;
    private int requestedOrientation;
    private Context context;

    public PermissionManagerBuilder(Context context) {
        this.context = context;
        deniedCloseButtonText = context.getString(android.R.string.cancel);
        rationaleConfirmText = context.getString(android.R.string.ok);
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    }

    protected void checkPermissions() {
        if (listener == null) {
            throw new IllegalArgumentException("You must setPermissionListener() on PermissionManager");
        } else if (ObjectUtils.isEmpty(permissions)) {
            throw new IllegalArgumentException("You must setPermissions() on PermissionManager");
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            listener.onPermissionGranted(new ArrayList<String>());
            return;
        }

        Intent intent = new Intent(context, PermissionManagerActivity.class);
        intent.putExtra(PermissionManagerActivity.EXTRA_PERMISSIONS, permissions);

        intent.putExtra(PermissionManagerActivity.EXTRA_RATIONALE_TITLE, rationaleTitle);
        intent.putExtra(PermissionManagerActivity.EXTRA_RATIONALE_MESSAGE, rationaleMessage);
        intent.putExtra(PermissionManagerActivity.EXTRA_DENY_TITLE, denyTitle);
        intent.putExtra(PermissionManagerActivity.EXTRA_DENY_MESSAGE, denyMessage);
        intent.putExtra(PermissionManagerActivity.EXTRA_PACKAGE_NAME, context.getPackageName());
        intent.putExtra(PermissionManagerActivity.EXTRA_SETTING_BUTTON, hasSettingBtn);
        intent.putExtra(PermissionManagerActivity.EXTRA_DENIED_DIALOG_CLOSE_TEXT, deniedCloseButtonText);
        intent.putExtra(PermissionManagerActivity.EXTRA_RATIONALE_CONFIRM_TEXT, rationaleConfirmText);
        intent.putExtra(PermissionManagerActivity.EXTRA_SETTING_BUTTON_TEXT, settingButtonText);
        intent.putExtra(PermissionManagerActivity.EXTRA_SCREEN_ORIENTATION, requestedOrientation);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        PermissionManagerActivity.startActivity(context, intent, listener);
        PermissionManagerBase.setFirstRequest(context, permissions);
    }

    public T setPermissionListener(PermissionManagerListener listener) {
        this.listener = listener;
        return (T) this;
    }

    public T setPermissions(String... permissions) {
        this.permissions = permissions;
        return (T) this;
    }

    public T setRationaleMessage(@StringRes int stringRes) {
        return setRationaleMessage(getText(stringRes));
    }

    private CharSequence getText(@StringRes int stringRes) {
        if (stringRes <= 0) {
            throw new IllegalArgumentException("Invalid String resource id");
        }
        return context.getText(stringRes);
    }

    public T setRationaleMessage(CharSequence rationaleMessage) {
        this.rationaleMessage = rationaleMessage;
        return (T) this;
    }


    public T setRationaleTitle(@StringRes int stringRes) {
        return setRationaleTitle(getText(stringRes));
    }

    public T setRationaleTitle(CharSequence rationaleMessage) {
        this.rationaleTitle = rationaleMessage;
        return (T) this;
    }

    public T setDeniedMessage(@StringRes int stringRes) {
        return setDeniedMessage(getText(stringRes));
    }

    public T setDeniedMessage(CharSequence denyMessage) {
        this.denyMessage = denyMessage;
        return (T) this;
    }

    public T setDeniedTitle(@StringRes int stringRes) {
        return setDeniedTitle(getText(stringRes));
    }

    public T setDeniedTitle(CharSequence denyTitle) {
        this.denyTitle = denyTitle;
        return (T) this;
    }

    public T setGotoSettingButton(boolean hasSettingBtn) {
        this.hasSettingBtn = hasSettingBtn;
        return (T) this;
    }

    public T setGotoSettingButtonText(@StringRes int stringRes) {
        return setGotoSettingButtonText(getText(stringRes));
    }

    public T setGotoSettingButtonText(CharSequence rationaleConfirmText) {
        this.settingButtonText = rationaleConfirmText;
        return (T) this;
    }

    public T setRationaleConfirmText(@StringRes int stringRes) {
        return setRationaleConfirmText(getText(stringRes));
    }

    public T setRationaleConfirmText(CharSequence rationaleConfirmText) {
        this.rationaleConfirmText = rationaleConfirmText;
        return (T) this;
    }

    public T setDeniedCloseButtonText(CharSequence deniedCloseButtonText) {
        this.deniedCloseButtonText = deniedCloseButtonText;
        return (T) this;
    }

    public T setDeniedCloseButtonText(@StringRes int stringRes) {
        return setDeniedCloseButtonText(getText(stringRes));
    }

    public T setScreenOrientation(int requestedOrientation) {
        this.requestedOrientation = requestedOrientation;
        return (T) this;
    }

}
