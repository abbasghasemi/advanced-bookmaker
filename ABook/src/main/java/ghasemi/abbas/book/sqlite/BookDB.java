/*
 * Copyright (C) 2019  All rights reserved for FaraSource (ABBAS GHASEMI)
 * https://farasource.com
 */
package ghasemi.abbas.book.sqlite;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ghasemi.abbas.book.general.AndroidUtilities;

public class BookDB extends AnalyzeDB {
    private final String[] related = new String[]{
            "و", "به", "را", "چه"
            , "که", "در", "تا", "از", "روی"
            , "زیر", "رو", "بر", "ما",
            "زیرا", "هم", "ولی", "با", "یا", "پس"
            , "اگر", "نه", "چون", "ی", "چرا",
            "ها", "های", "هایی", "هائی", "سپس",
            "ترین", "تر", "می", "آنرا", "آن", "او"
    };
    private final String[] empty = new String[]{
            ".", "،", "-", ",", ">", "<", "'", "\"",
            "?", "+", "؟", "=", "/", "_", "!", "@", "#",
            "$", "%", "\\", ";", ":", "*"
    };

    private BookDB() {
        super();
    }


    public static BookDB getInstance() {
        return new BookDB();
    }


    public void seasons(final String season, final String season_id, final ResultRequest resultRequest) {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                String query = String.format("select id,title,content,post_type,icon_name from book where season = %s and season_id = %s", season, season_id);
                Cursor cursor = getSqLiteDatabase().rawQuery(query, null);
                if (cursor.moveToFirst()) {
                    List<SqlModel> list = new ArrayList<>();
                    do {
                        SqlModel sqlModel = new SqlModel(cursor.getInt(0), Integer.parseInt(season), Integer.parseInt(season_id), cursor.getString(1),
                                cursor.getString(2), cursor.getString(3), -1, cursor.getString(4));
                        list.add(sqlModel);
                    } while (cursor.moveToNext());
                    cursor.close();
                    resultRequest.onSuccess(list);
                } else {
                    cursor.close();
                    resultRequest.onFail();
                }
            }
        });
    }

    public void post(final String contentID, final ResultRequest resultRequest) {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                String query = String.format("select title,content,is_favorite from book where id = %s", contentID);
                Cursor cursor = getSqLiteDatabase().rawQuery(query, null);

                if (cursor.moveToFirst()) {
                    List<SqlModel> list = new ArrayList<>();
                    SqlModel sqlModel = new SqlModel(Integer.parseInt(contentID), -1, -1, cursor.getString(0),
                            cursor.getString(1), "post", cursor.getInt(2), null);
                    list.add(sqlModel);
                    cursor.close();
                    resultRequest.onSuccess(list);
                } else {
                    resultRequest.onFail();
                }
            }
        });
    }

    public void addPostFavorite(final String contentID) {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                ContentValues values = new ContentValues();
                values.put("is_favorite", 1);
                getSqLiteDatabase().update("book", values, "id = ?", new String[]{contentID});
                values = new ContentValues();
                values.put("post_id", contentID);
                getSqLiteDatabase().insert("favorites", null, values);
            }
        });
    }

    public void removePostFavorite(final String contentID) {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                ContentValues values = new ContentValues();
                values.put("is_favorite", 0);
                getSqLiteDatabase().update("book", values, "id = ?", new String[]{contentID});
                getSqLiteDatabase().delete("favorites", "post_id = ?", new String[]{contentID});
            }
        });
    }

    public void favorite(final ResultRequest resultRequest) {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = getSqLiteDatabase().rawQuery("select id,title,content,season_id,icon_name from book where id in (select post_id from favorites)", null);

                if (cursor.moveToFirst()) {
                    List<SqlModel> sqlModelList = new ArrayList<>();
                    do {
                        sqlModelList.add(new SqlModel(cursor.getInt(0), -1, cursor.getInt(3), cursor.getString(1),
                                cursor.getString(2), "post", -1, cursor.getString(4)));
                    } while (cursor.moveToNext());
                    cursor.close();
                    resultRequest.onSuccess(sqlModelList);
                } else {
                    resultRequest.onFail();
                }
            }
        });
    }

    public void search(final Bundle bundle, final ResultRequest resultRequest) {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                ArrayList<String> parts = new ArrayList<>();
                parts.add(bundle.getString("search"));
                if (bundle.getBoolean("similar")) {
                    String query = bundle.getString("search");
                    for (String em : empty) {
                        query = query.replace(em, " ");
                    }
                    parts.addAll(Arrays.asList(query.split(" ")));
                    for (int i = 0; i < parts.size(); i++) {
                        for (String related : BookDB.this.related) {
                            if (parts.get(i).equals(related) || parts.get(i).length() < 3) {
                                parts.remove(i--);
                                break;
                            }
                        }
                    }
                }
                if (parts.isEmpty()) {
                    resultRequest.onFail();
                } else {
                    List<SqlModel> sqlModelList = new ArrayList<>();
                    if (bundle.getBoolean("title")) {
                        searchQuery(parts, "title", bundle, sqlModelList);
                    }
                    if (bundle.getBoolean("content")) {
                        searchQuery(parts, "content", bundle, sqlModelList);
                    }
                    if (sqlModelList.isEmpty()) {
                        resultRequest.onFail();
                    } else {
                        sqlModelList.add(new SqlModel(-1, -1, -1, null, null, join(parts), -1, null));
                        resultRequest.onSuccess(sqlModelList);
                    }
                }
            }
        });
    }

    private void searchQuery(ArrayList<String> search, String on, Bundle bundle, List<SqlModel> sqlModelList) {
        String q = createLike(search, on);
        String query = String.format("select id,season,season_id,title,post_type,icon_name from book where post_type in('post','row_list','card_list','classic_list') and (%s)", q);
        if (!bundle.getBoolean("all")) {
            query += " and season_id = " + bundle.getString("season_id");
        }
        if (bundle.getBoolean("fav")) {
            query += " and is_favorite = 0";
        }
        Cursor cursor = getSqLiteDatabase().rawQuery(query, null);
        if (cursor.moveToFirst()) {
            sqlModelList.add(new SqlModel(-1, -1, -1, null, null, on, -1, null));
            do {
                sqlModelList.add(new SqlModel(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2),
                        cursor.getString(3), on, cursor.getString(4), -1, cursor.getString(5)));
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private String createLike(ArrayList<String> search, String on) {
        StringBuilder query = new StringBuilder();
        for (int i = 1; i < search.size(); i++) {
            query.append(String.format("%s like '%s%s%s'", on, "%", search.get(i).trim(), "%"));
            if (i < search.size() - 1) {
                query.append(" and ");
            }
        }
        if (query.toString().isEmpty()) {
            query.append(String.format("%s like '%s%s%s'", on, "%", search.get(0), "%"));
        } else {
            query.append(String.format(" or %s like '%s%s%s'", on, "%", search.get(0), "%"));
        }
        return query.toString();
    }

    private String join(ArrayList<String> search) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String part : search) {
            stringBuilder.append(",").append(part);
        }
        return stringBuilder.substring(1);
    }

    public void single(final String post_type, final ResultRequest resultRequest) {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = getSqLiteDatabase().rawQuery(String.format("select title,content from book where post_type = '%s' limit 1", post_type), null);
                if (cursor.moveToFirst()) {
                    List<SqlModel> list = new ArrayList<>();
                    SqlModel sqlModel = new SqlModel(-1, -1, -1, cursor.getString(0),
                            cursor.getString(1), post_type, -1, null);
                    list.add(sqlModel);
                    cursor.close();
                    resultRequest.onSuccess(list);
                } else {
                    cursor.close();
                    resultRequest.onFail();
                }
            }
        });
    }

    public void requestTypeFromId(String id, final ResultRequest resultRequest) {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                String query = String.format("select id,season,season_id,title,content,post_type,icon_name from book where id = %s", id);
                Cursor cursor = getSqLiteDatabase().rawQuery(query, null);
                if (cursor.moveToFirst()) {
                    List<SqlModel> list = new ArrayList<>();
                    SqlModel sqlModel = new SqlModel(cursor.getInt(0), cursor.getInt(1),
                            cursor.getInt(2), cursor.getString(3),
                            cursor.getString(4), cursor.getString(5),
                            -1, cursor.getString(6));
                    list.add(sqlModel);
                    cursor.close();
                    resultRequest.onSuccess(list);
                } else {
                    cursor.close();
                    resultRequest.onFail();
                }
            }
        });
    }
}