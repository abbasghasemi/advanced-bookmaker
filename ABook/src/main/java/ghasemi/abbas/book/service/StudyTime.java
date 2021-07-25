/*
 * Copyright (C) 2019  All rights reserved for FaraSource (ABBAS GHASEMI)
 * https://farasource.com
 */
package ghasemi.abbas.book.service;

import ghasemi.abbas.book.general.TinyData;

public class StudyTime {

    private final String STUDY_TIME = "studyTime";
    private long time;
    private boolean isStart;
    private String timeParser = "";

    public void startTask() {
        if (isStart) {
            return;
        }
        isStart = true;

        time = System.currentTimeMillis();
    }

    public void finish() {
        if (isStart) {
            isStart = false;
            long lastTime = Long.parseLong(TinyData.getInstance().getString(STUDY_TIME, "0"));
            TinyData.getInstance().putString(STUDY_TIME, String.valueOf(lastTime + (System.currentTimeMillis() - time)));
        }
    }

    public void reset(){
        TinyData.getInstance().putString(STUDY_TIME, "0");
    }

    public String getStudyTime() {

        long time = Long.parseLong(TinyData.getInstance().getString(STUDY_TIME, "0")) / 1000;

        parsTime(time);

        return timeParser;

    }

    private long parsTime(long time) {
        if (time >= 3110400) { // year
            long t = time / 3110400;
            timeParser += String.format("%s سال و ", t);
            return parsTime(time - (t * 3110400));
        } else if (time >= 2592000) { // month
            long t = time / 2592000;
            timeParser += String.format("%s ماه و ", t);
            return parsTime(time - (t * 2592000));
        } else if (time >= 604800) { // week
            long t = time / 604800;
            timeParser += String.format("%s هفته و ", t);
            return parsTime(time - (t * 604800));
        } else if (time >= 86400) { // day
            long t = time / 86400;
            timeParser += String.format("%s روز و ", t);
            return parsTime(time - (t * 86400));
        } else if (time >= 3600) { // hour
            long t = time / 3600;
            timeParser += String.format("%s ساعت و ", t);
            return parsTime(time - (t * 3600));
        } else if (time >= 60) { // minute
            long t = time / 60;
            timeParser += String.format("%s دقیقه و ", t);
            return parsTime(time - (t * 60));
        } else { // second
            timeParser += String.format("%s ثانیه", time);
            return 0;
        }
    }
}