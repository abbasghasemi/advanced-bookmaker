/*
 * Copyright (C) 2019  All rights reserved for FaraSource (ABBAS GHASEMI)
 * https://farasource.com
 */
package ghasemi.abbas.book.sqlite;

import android.text.TextUtils;

import ghasemi.abbas.book.general.AndroidUtilities;

public class SqlModel {

    private final String content;
    private String contentWithoutTags;
    private String contentWithoutTags310;
    private final String title;
    private final String postType;
    private final int id;
    private final int fav;
    private final int season;
    private final int seasonId;
    private final String icon;


    SqlModel(int id, int season, int seasonId, String title, String content, String postType, int fav, String icon) {
        this.content = content;
        this.title = title;
        this.fav = fav;
        this.id = id;
        this.season = season;
        this.seasonId = seasonId;
        this.postType = postType;
        if (TextUtils.isEmpty(icon)) {
            icon = "";
        } else {
            try {
                int i = Integer.parseInt(icon);
                icon = "i_" + i;
            } catch (Exception e) {
                //
            }
        }
        this.icon = icon;
    }

    public int getId() {
        return id;
    }

    public int getSeason() {
        return season;
    }

    public int getSeasonId() {
        return seasonId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getIconName() {
        return icon;
    }

    public boolean deleteIcon(){
        return getIconName().equals("remove");
    }

    public String getContentWithoutTags() {
        if (contentWithoutTags == null) {
            contentWithoutTags = AndroidUtilities.removeTags(content);
        }
        return contentWithoutTags;
    }

    public boolean isFavorite() {
        return fav == 1;
    }

    public String getPostType() {
        return postType;
    }

    public boolean hasList() {
        return postType.equals("row_list") || postType.equals("card_list") || postType.equals("classic_list");
    }

    public boolean isPost() {
        return postType.equals("post");
    }

    public String getContentWithoutTags310() {
        if (contentWithoutTags310 == null) {
            if (getContentWithoutTags().length() < 311) {
                contentWithoutTags310 = getContentWithoutTags();
            } else {
                contentWithoutTags310 = getContentWithoutTags().substring(0, 310);
            }
        }
        return contentWithoutTags310;
    }
}