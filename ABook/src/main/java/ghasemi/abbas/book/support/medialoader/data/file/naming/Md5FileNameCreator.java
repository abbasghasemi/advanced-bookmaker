package ghasemi.abbas.book.support.medialoader.data.file.naming;

import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import ghasemi.abbas.book.ApplicationLoader;
import ghasemi.abbas.book.general.TinyData;
import ghasemi.abbas.book.support.medialoader.data.file.RandomAcessFileDataSource;
import ghasemi.abbas.book.support.medialoader.utils.Util;

/**
 * 使用图片URL地址的 MD5编码来进行生成缓存的文件名称
 *
 * @author vincanyang
 */
public class Md5FileNameCreator implements FileNameCreator {

    @Override
    public String create(String url) {
        String base = TinyData.getInstance().getStringMD5(url, TinyData.KeyType.MOVIES);
        if (!TextUtils.isEmpty(base) && (new File(base).exists() || new File(base + RandomAcessFileDataSource.TEMP_POSTFIX).exists())) {
            return new File(base).getName();
        }
        File cacheDir = ApplicationLoader.context.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        String extension = Util.getExtensionFromUrl(url);
        String name;
        File file;
        do {
            name = Math.abs(new Random().nextLong()) + "_" + Math.abs(new Random().nextInt()) + extension;
            file = new File(cacheDir, name);
        } while (file.exists());
        try {
            new File(file.getPath() + RandomAcessFileDataSource.TEMP_POSTFIX).createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        TinyData.getInstance().putStringMD5(url, TinyData.KeyType.MOVIES, file.getPath());
        return name;
    }
}