/*
 * Copyright (C) 2019  All rights reserved for FaraSource (ABBAS GHASEMI)
 * https://farasource.com
 */

package ghasemi.abbas.book.support;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import ghasemi.abbas.book.components.TouchImageView;
import ghasemi.abbas.book.general.TinyData;


public class Glide {

    private Context context;
    private String url;
    private ImageView imageView;
    private Runnable runnable;
    private boolean saveImage;
    private ImageView.ScaleType scaleType;

    private Glide() {
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public static Glide with(Context context) {
        Glide glide = new Glide();
        glide.setContext(context);
        return glide;
    }

    public Glide load(String url) {
        this.url = url;
        return this;
    }

    public void into(ImageView imageView) {
        into(imageView, null);
    }

    public void into(ImageView _imageView, Runnable _runnable) {
        String path = TinyData.getInstance().getStringMD5(url, TinyData.KeyType.PICTURES);
        this.imageView = _imageView;
        this.runnable = _runnable;
        if (TextUtils.isEmpty(path) || !new File(path).exists()) {
            saveImage = true;
        }
        com.bumptech.glide.Glide.with(context)
                .load(saveImage ? url : path)
                .skipMemoryCache(!saveImage)
                .diskCacheStrategy(saveImage ? DiskCacheStrategy.AUTOMATIC : DiskCacheStrategy.NONE)
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        String path = TinyData.getInstance().getStringMD5(url, TinyData.KeyType.PICTURES);
                        if (saveImage && (TextUtils.isEmpty(path) || !new File(path).exists())) {
                            saveBitmapToFile(drawableToBitmap(resource));
                        }
                        if (runnable != null) {
                            runnable.run();
                        }
                        if (scaleType != null) {
                            imageView.setAdjustViewBounds(false);
                            imageView.setScaleType(scaleType);
                        }
                        if (imageView instanceof TouchImageView) {
                            TouchImageView touchImageView = ((TouchImageView) imageView);
                            touchImageView.setImageDrawable(resource);
                        } else {
                            imageView.setImageDrawable(resource);
                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
    }

    private void saveBitmapToFile(Bitmap bm) {
        File file = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        String format = getFormat();
        String name;
        do {
            name = Math.abs(new Random().nextLong()) + "_" + Math.abs(new Random().nextInt()) + format;
        } while (new File(file, name).exists());
        File imageFile = new File(file, name);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imageFile);
            bm.compress(format.equals(".png") ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
            TinyData.getInstance().putStringMD5(url, TinyData.KeyType.PICTURES, imageFile.getPath());
        } catch (IOException e) {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    private String getFormat() {
        String format = url;
        int index = format.indexOf("?");
        if (index != -1) {
            format = format.substring(0, index);
        }
        format = format.replace("/", "").replace("#", "");
        String[] split = format.split("\\.");
        format = split[split.length - 1];
        if ("jpg".equals(format) || "png".equals(format) || "jpeg".equals(format)) {
            return "." + format;
        }
        return ".jpg";
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public Glide setScaleType(ImageView.ScaleType scaleType) {
        this.scaleType = scaleType;
        return this;
    }
}