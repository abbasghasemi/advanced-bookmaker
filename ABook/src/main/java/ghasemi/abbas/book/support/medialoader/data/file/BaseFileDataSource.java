package ghasemi.abbas.book.support.medialoader.data.file;


import ghasemi.abbas.book.support.medialoader.data.file.cleanup.DiskLruCache;
import ghasemi.abbas.book.support.medialoader.utils.FileUtil;
import ghasemi.abbas.book.support.medialoader.utils.LogUtil;

import java.io.File;
import java.io.IOException;

/**
 * {@link FileDataSource}的通用实现
 *
 * @author vincanyang
 */
public abstract class BaseFileDataSource implements FileDataSource {

    protected File mOriginFile;

    protected DiskLruCache mDiskLruStorage;

    public BaseFileDataSource(File file, DiskLruCache diskLruStorage) {
        mOriginFile = file;
        mDiskLruStorage = diskLruStorage;
        try {
            FileUtil.mkdirs(file.getParentFile());
        } catch (IOException e) {
            LogUtil.e(e);
        }
    }

    @Override
    public void close() throws IOException {
        mDiskLruStorage.save("", mOriginFile);
    }

}
