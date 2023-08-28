/*
 * Copyright (C) 2019  All rights reserved for FaraSource (ABBAS GHASEMI)
 * https://farasource.com
 */
package ghasemi.abbas.book.general;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;

import androidx.annotation.Dimension;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.DialogTitle;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Build;
import android.os.Environment;

import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;

import ghasemi.abbas.book.ApplicationLoader;
import ghasemi.abbas.book.R;

import static androidx.annotation.Dimension.DP;
import static ghasemi.abbas.book.BuildApp.*;

public class AndroidUtilities {

    private static Typeface light, bold;

    public static void setCustomFontDialog(AlertDialog alertDialog) {
        DialogTitle title = alertDialog.findViewById(androidx.appcompat.R.id.alertTitle);
        if (title != null) {
            title.setTypeface(FONT_TYPE.getTypefaceBold());
        }
        android.widget.TextView message = alertDialog.findViewById(android.R.id.message);
        if (message != null) {
            message.setTypeface(FONT_TYPE.getTypefaceLight());
        }
        android.widget.TextView button1 = alertDialog.findViewById(android.R.id.button1);
        if (button1 != null) {
            button1.setTypeface(FONT_TYPE.getTypefaceBold());
        }
        android.widget.TextView button2 = alertDialog.findViewById(android.R.id.button2);
        if (button2 != null) {
            button2.setTypeface(FONT_TYPE.getTypefaceBold());
        }
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.alert_dialog_bg);
    }

    public static void setTypeFont() {
        switch (TinyData.getInstance().getString("fontType", "-1")) {
            case "0":
                light = Typeface.createFromAsset(ApplicationLoader.context.getAssets(), "fonts/" + ApplicationLoader.context.getResources().getString(R.string.font_normal_1));
                bold = Typeface.createFromAsset(ApplicationLoader.context.getAssets(), "fonts/" + ApplicationLoader.context.getResources().getString(R.string.font_bold_1));
                break;
            case "1":
                light = Typeface.createFromAsset(ApplicationLoader.context.getAssets(), "fonts/" + ApplicationLoader.context.getResources().getString(R.string.font_normal_2));
                bold = Typeface.createFromAsset(ApplicationLoader.context.getAssets(), "fonts/" + ApplicationLoader.context.getResources().getString(R.string.font_bold_2));
                break;
            case "2":
                light = Typeface.createFromAsset(ApplicationLoader.context.getAssets(), "fonts/" + ApplicationLoader.context.getResources().getString(R.string.font_normal_3));
                bold = Typeface.createFromAsset(ApplicationLoader.context.getAssets(), "fonts/" + ApplicationLoader.context.getResources().getString(R.string.font_bold_3));
                break;
            case "3":
                light = Typeface.createFromAsset(ApplicationLoader.context.getAssets(), "fonts/" + ApplicationLoader.context.getResources().getString(R.string.font_normal_4));
                bold = Typeface.createFromAsset(ApplicationLoader.context.getAssets(), "fonts/" + ApplicationLoader.context.getResources().getString(R.string.font_bold_4));
                break;
            default:
                light = FONT_TYPE.getTypefaceLight();
                bold = FONT_TYPE.getTypefaceBold();
        }
    }

    public static RecyclerView.LayoutManager getLayoutManager(Context context, String post_type) {
        if (post_type.equals("row_list")) {
            return new LinearLayoutManager(context);
        } else if (post_type.equals("card_list")) {
            int s = (int) (context.getResources().getDisplayMetrics().widthPixels / getDensity() / (140));
            return new GridLayoutManager(context, s == 0 ? 1 : s);
        } else {
            int s = (int) (context.getResources().getDisplayMetrics().widthPixels / getDensity() / (250));
            return new GridLayoutManager(context, s == 0 ? 1 : s);
        }
    }

    public static Typeface getLightTypeFace() {
        if (light == null) {
            setTypeFont();
        }
        return light;
    }

    public static Typeface getBoldTypeFace() {
        if (bold == null) {
            setTypeFont();
        }
        return bold;
    }

    public static int getTextSize() {
        return TinyData.getInstance().getInt("systemFontSize", DEFAULT_FONT_SIZE);
    }

    public static void toast(String msg) {
        Toast.makeText(ApplicationLoader.context, msg, Toast.LENGTH_SHORT).show();
    }


    public static void commentApp(Activity activity) {
        try {
            Intent intent = new Intent(MARKET_TYPE.getIntentAction());
            intent.setData(Uri.parse(MARKET_TYPE.getAddressRate()));
            activity.startActivity(intent);
            intent.setPackage(MARKET_TYPE.getPackageName());
        } catch (ActivityNotFoundException e) {
            AndroidUtilities.toast("به نظر میرسه شما" + MARKET_TYPE.getMarketName() + "را نصب نداری!");
        }
    }

    public static void addToClipboard(String text) {
        final android.content.ClipboardManager clipboardManager = (android.content.ClipboardManager) ApplicationLoader.context.getSystemService(Context.CLIPBOARD_SERVICE);
        final android.content.ClipData clipData = android.content.ClipData.newPlainText("label", text);
        if (clipboardManager != null) {
            clipboardManager.setPrimaryClip(clipData);
        }
    }

    public static int getLogoId(int id, int season_id, int def) {
        FileLogs.print(id + ":" + season_id);
        int drawableID = ApplicationLoader.context.getResources().getIdentifier("i_" + id, "drawable", ApplicationLoader.context.getPackageName());
        if (drawableID != 0) {
            return drawableID;
        }
        drawableID = ApplicationLoader.context.getResources().getIdentifier("i_" + season_id, "drawable", ApplicationLoader.context.getPackageName());
        if (drawableID != 0) {
            return drawableID;
        }
        if (def != 0) {
            return def;
        }
        return R.drawable.base_icon;
    }

    public static int getImageId(String name) {
        int id = ApplicationLoader.context.getResources().getIdentifier(name, "drawable", ApplicationLoader.context.getPackageName());
        if (id == 0) {
            return R.drawable.icplaceholder;
        }
        return id;
    }

    public static String removeTags(String content) {
        if (content == null) {
            return "";
        }
        return content.replaceAll("[\\[](.*)[]]", "")
                .replaceAll("<[^>]*>", "")
                .replaceAll("\n+", "\n")
                .replaceAll("((?!\n+)\\s+)", " ");
    }

    public static String cleanTag(String src, String model) {
        return src.substring(model.length() + 2).trim();
    }

    public interface Response {
        void onSrc(String src);
    }

    public static void split(String content, Response response) {
        String[] data = content.split("\\[t]");
        for (String src : data) {
            if (src.isEmpty()) {
                continue;
            }
            response.onSrc(src);
        }
    }

    public static String[] split(String src, String sep) {
        String[] data = src.split(sep);
        for (int i = 0, j = data.length; i < j; i++) {
            data[i] = data[i].trim();
        }
        return data;
    }

    public static String getSrcFromTag(String tag) {
        return tag.substring(1, tag.length() - 1);
    }

    public static String getViewModel(String tag) {
        if (tag.startsWith("[")) {
            int end = tag.indexOf(']');
            if (end != -1)
                return tag.substring(1, end);
        }
        return "";
    }

    public static void hideKeyboard(EditText editText) {
        try {
            InputMethodManager imm = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            assert imm != null;
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        } catch (Exception e) {
            FileLogs.e(e);
        }
    }

    private static float getDensity() {
        return ApplicationLoader.context.getResources().getDisplayMetrics().density;
    }

    public static int dp(@Dimension(unit = DP) float dp) {
        if (dp == 0) {
            return 0;
        }
        return (int) Math.ceil(getDensity() * dp);
    }

    public static boolean isExternalStorageWritable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static long getFolderSize(File f) {
        long size = 0;
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            if (files != null) {
                for (File file : files) {
                    size += getFolderSize(file);
                }
            }
        } else {
            size = f.length();
        }
        return size;
    }

    public static String getTotalSpace(long totalSpace) {
        if (totalSpace == 0) {
            return "0 MB";
        }
        if (1000 > totalSpace / 1024 / 1024) {
            return String.format("%.1f MB", (float) totalSpace / 1024 / 1024);
        } else {
            return String.format("%.1f GB", (float) totalSpace / 1024 / 1024 / 1024);
        }
    }

    public static void deleteCatch() {
        deleteCatch(ApplicationLoader.context.getExternalFilesDir(null));
    }

    public static void deleteCatch(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            if (files == null) {
                return;
            }
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteCatch(file);
                } else {
                    file.delete();
                }
            }
        }
        path.delete();
    }


    public static boolean disableFitsSystemWindow(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                activity.getWindow().setDecorFitsSystemWindows(false);
            } else {
                activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            }
            return true;
        }
        return false;
    }


    public static void setWindowFullScreen(Activity activity) {
        if (TinyData.getInstance().getBool("windowFullScreen", false)) {
            setForceWindowFullScreen(activity.getWindow());
        }
    }


    public static void setForceWindowFullScreen(Window window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = window.getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    public static int getStatusBarHeight() {
        final Resources resources = ApplicationLoader.context.getResources();
        final int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId <= 0) {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? dp(24) : dp(25);
        } else {
            return resources.getDimensionPixelSize(resourceId);
        }
    }

    public static int[] getColorByName(String name) {
        int[] colors = new int[2];
        switch (name) {
            case "blue":
                colors[0] = 0xff2196F3;
                colors[1] = 0xff64B5F6;
                break;
            case "red":
                colors[0] = 0xffF44336;
                colors[1] = 0xffE57373;
                break;
            case "green":
                colors[0] = 0xff4CAF50;
                colors[1] = 0xff81C784;
                break;
            case "orange":
                colors[0] = 0xffFF9800;
                colors[1] = 0xffFFD54F;
                break;
            case "yellow":
                colors[0] = 0xffFFC107;
                colors[1] = 0xffFFF176;
                break;
            case "purple":
                colors[0] = 0xff9C27B0;
                colors[1] = 0xffBA68C8;
                break;
        }
        return colors;
    }
}