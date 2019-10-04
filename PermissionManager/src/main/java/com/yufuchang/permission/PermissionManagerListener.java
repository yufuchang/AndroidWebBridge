package com.yufuchang.permission;

import java.util.List;
/**
 * Created by yufuchang on 2019/10/1.
 */
public interface PermissionManagerListener {

    void onPermissionGranted(List<String> grantPermissions);

    void onPermissionDenied(List<String> deniedPermissions);

}
