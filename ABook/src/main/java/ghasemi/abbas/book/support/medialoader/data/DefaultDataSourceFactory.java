package ghasemi.abbas.book.support.medialoader.data;

import ghasemi.abbas.book.support.medialoader.MediaLoaderConfig;
import ghasemi.abbas.book.support.medialoader.data.file.FileDataSource;
import ghasemi.abbas.book.support.medialoader.data.file.RandomAcessFileDataSource;
import ghasemi.abbas.book.support.medialoader.data.file.cleanup.DiskLruCache;
import ghasemi.abbas.book.support.medialoader.data.file.cleanup.SimpleDiskLruCache;
import ghasemi.abbas.book.support.medialoader.data.url.DefaultUrlDataSource;
import ghasemi.abbas.book.support.medialoader.data.url.UrlDataSource;

import java.io.File;

/**
 * 数据源默认生产工厂
 *
 * @author vincanyang
 */
public final class DefaultDataSourceFactory {

    public static UrlDataSource createUrlDataSource(String url) {
        return new DefaultUrlDataSource(url);
    }

    public static UrlDataSource createUrlDataSource(DefaultUrlDataSource dataSource) {
        return new DefaultUrlDataSource(dataSource);
    }

    public static FileDataSource createFileDataSource(File file, DiskLruCache diskLruStorage) {
        return new RandomAcessFileDataSource(file, diskLruStorage);
    }

    public static DiskLruCache createDiskLruCache(MediaLoaderConfig mediaLoaderConfig) {
        return new SimpleDiskLruCache(mediaLoaderConfig);
    }
}
