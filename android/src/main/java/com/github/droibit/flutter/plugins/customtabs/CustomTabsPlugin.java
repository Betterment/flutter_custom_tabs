package com.github.droibit.flutter.plugins.customtabs;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import com.github.droibit.flutter.plugins.customtabs.internal.Launcher;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import java.util.List;
import java.util.Map;

public class CustomTabsPlugin implements MethodCallHandler, FlutterPlugin {

  private static final String TAG = "CustomTabsPlugin";
  private static final String KEY_OPTION = "option";
  private static final String KEY_URL = "url";
  private static final String KEY_EXTRA_CUSTOM_TABS = "extraCustomTabs";
  private static final String CODE_LAUNCH_ERROR = "LAUNCH_ERROR";

  @Nullable private MethodChannel methodChannel;
  @Nullable private Launcher launcher;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
    if (methodChannel != null) {
      Log.wtf(TAG, "Already attached to the engine.");
      return;
    }

    methodChannel = new MethodChannel(binding.getBinaryMessenger(), "com.github.droibit.flutter.plugins.custom_tabs");
    methodChannel.setMethodCallHandler(this);
    launcher = new Launcher(binding.getApplicationContext());
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    if (methodChannel == null) {
      Log.wtf(TAG, "Already detached from the engine.");
      return;
    }

    methodChannel.setMethodCallHandler(null);
    methodChannel = null;
    launcher = null;
  }

  @SuppressWarnings("unchecked") @Override
  public void onMethodCall(MethodCall call, @NonNull final MethodChannel.Result result) {
    if ("launch".equals(call.method)) {
      launch(((Map<String, Object>) call.arguments), result);
    } else {
      result.notImplemented();
    }
  }

  @SuppressWarnings("unchecked")
  private void launch(@NonNull Map<String, Object> args, @NonNull MethodChannel.Result result) {
    final Uri uri = Uri.parse(args.get(KEY_URL).toString());
    final Map<String, Object> options = (Map<String, Object>) args.get(KEY_OPTION);
    final CustomTabsIntent customTabsIntent = launcher.buildIntent(options);

    try {
      final List<String> extraCustomTabs;
      if (options.containsKey(KEY_EXTRA_CUSTOM_TABS)) {
        extraCustomTabs = ((List<String>) options.get(KEY_EXTRA_CUSTOM_TABS));
      } else {
        extraCustomTabs = null;
      }
      customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      launcher.launch(uri, customTabsIntent, extraCustomTabs);
      result.success(null);
    } catch (ActivityNotFoundException e) {
      result.error(CODE_LAUNCH_ERROR, e.getMessage(), null);
    }
  }
}
