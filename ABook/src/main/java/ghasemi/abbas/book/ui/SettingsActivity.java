/*
 * Copyright (C) 2019  All rights reserved for FaraSource (ABBAS GHASEMI)
 * https://farasource.com
 */
package ghasemi.abbas.book.ui;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.kyleduo.switchbutton.SwitchButton;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import android.util.TypedValue;
import android.view.View;
import android.widget.CompoundButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;

import ghasemi.abbas.book.R;
import ghasemi.abbas.book.general.AndroidUtilities;
import ghasemi.abbas.book.BuildApp;
import ghasemi.abbas.book.general.TinyData;
import ghasemi.abbas.book.ui.components.TextView;

public class SettingsActivity extends BaseActivity {

    private TextView test, font, tFontSize, tSpace, nightType, totalSpace;
    private IndicatorSeekBar fontSize, space;
    private SwitchButton filter, fullScreen, saveLocation;
    private View white, dark, sepia, blueLightFilter;
    private BottomSheetDialog bottomSheetDialog;

    public static float getSpace() {
        int value = TinyData.getInstance().getInt("fontSpace", 1);
        if (value == 1) {
            return 1;
        } else if (value == 10) {
            return 2;
        }
        return 1 + value / 10f;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        test = findViewById(R.id.test);
        font = findViewById(R.id.font);
        tFontSize = findViewById(R.id.t_font_size);
        blueLightFilter = findViewById(R.id.blue_light_filter);
        tSpace = findViewById(R.id.t_space);

        nightType = findViewById(R.id.night_type);
        String nightMode = TinyData.getInstance().getString("nightMode", "system");
        if (nightMode.equals("system")) {
            nightType.setText(R.string.night_system);
        } else if (nightMode.equals("off")) {
            nightType.setText(R.string.night_off);
        }
        findViewById(R.id.l_theme).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetDialog != null) {
                    return;
                }
                bottomSheetDialog = new BottomSheetDialog(SettingsActivity.this, R.style.BottomSheetDialogTheme);
                bottomSheetDialog.setContentView(getLayoutInflater().inflate(R.layout.dialog_select_night_mode, null));
                bottomSheetDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        disableDialog();
                    }
                });
                bottomSheetDialog.show();
                bottomSheetDialog.findViewById(R.id.night_system).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.dismiss();
                        disableDialog();
                        String nightMode = TinyData.getInstance().getString("nightMode", "system");
                        if (!nightMode.equals("system")) {
                            TinyData.getInstance().putString("nightMode", "system");
                            nightType.setText(R.string.night_system);
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                        }
                    }
                });
                bottomSheetDialog.findViewById(R.id.night_off).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.dismiss();
                        disableDialog();
                        String nightMode = TinyData.getInstance().getString("nightMode", "system");
                        if (!nightMode.equals("off")) {
                            TinyData.getInstance().putString("nightMode", "off");
                            nightType.setText(R.string.night_off);
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        }
                    }
                });
                bottomSheetDialog.findViewById(R.id.night_on).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.dismiss();
                        disableDialog();
                        String nightMode = TinyData.getInstance().getString("nightMode", "system");
                        if (!nightMode.equals("on")) {
                            TinyData.getInstance().putString("nightMode", "on");
                            nightType.setText(R.string.night_on);
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        }
                    }
                });
            }
        });

        fontSize = findViewById(R.id.font_size);
        space = findViewById(R.id.space);

        fullScreen = findViewById(R.id.full_screen);
        findViewById(R.id.l_full_screen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullScreen.setChecked(!fullScreen.isChecked());
            }
        });
        filter = findViewById(R.id.light_filter);
        findViewById(R.id.l_light_filter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter.setChecked(!filter.isChecked());
            }
        });
        saveLocation = findViewById(R.id.save_location);
        findViewById(R.id.l_save_location).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveLocation.setChecked(!saveLocation.isChecked());
            }
        });
        white = findViewById(R.id.white);
        dark = findViewById(R.id.dark);
        sepia = findViewById(R.id.sepia);

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        init1();
        init2();

    }

    private void init1() {

        switch (TinyData.getInstance().getString("fontType", "-1")) {
            case "0":
                font.setText(getString(R.string.font_name_1).replace(" ", " : "));
                break;
            case "1":
                font.setText(getString(R.string.font_name_2).replace(" ", " : "));
                break;
            case "2":
                font.setText(getString(R.string.font_name_3).replace(" ", " : "));
                break;
            case "3":
                font.setText(getString(R.string.font_name_4).replace(" ", " : "));
                break;
            default:
                font.setText(BuildApp.FONT_TYPE.getName());
        }

        fontSize.setMax(BuildApp.MAXIMUM_FONT_SIZE);
        fontSize.setMin(BuildApp.MINIMUM_FONT_SIZE);
        fontSize.setProgress(TinyData.getInstance().getInt("systemFontSize", BuildApp.DEFAULT_FONT_SIZE));
        space.setMax(10);
        space.setMin(1);
        space.setProgress(TinyData.getInstance().getInt("fontSpace", 1));

        fullScreen.setChecked(TinyData.getInstance().getBool("windowFullScreen", false));
        filter.setChecked(TinyData.getInstance().getBool("windowFilterScreen", false));
        if (filter.isChecked()) {
            blueLightFilter.setVisibility(View.VISIBLE);
        } else {
            blueLightFilter.setVisibility(View.GONE);
        }
        switch (TinyData.getInstance().getString("postBackground", "0")) {
            case "0":
                white.setBackgroundResource(R.drawable.white_sel_bg);
                test.setBackgroundColor(0xffffffff);
                test.setTextColor(0xff000000);
                break;
            case "1":
                sepia.setBackgroundResource(R.drawable.sepia_sel_bg);
                test.setBackgroundColor(0xffFCF1D1);
                test.setTextColor(0xff000000);
                break;
            case "2":
                dark.setBackgroundResource(R.drawable.dark_sel_bg);
                test.setBackgroundColor(0xff333333);
                test.setTextColor(0xffffffff);
                break;
            default:

        }

        tFontSize.setText(String.format("اندازه فونت : %s", TinyData.getInstance().getInt("systemFontSize", BuildApp.DEFAULT_FONT_SIZE)));
        tSpace.setText(String.format("فاصله بین خطوط : %s", TinyData.getInstance().getInt("fontSpace", 1)));
        test.setTypeface(AndroidUtilities.getLightTypeFace());
        test.setTextSize(TypedValue.COMPLEX_UNIT_DIP, TinyData.getInstance().getInt("systemFontSize", BuildApp.DEFAULT_FONT_SIZE));
        test.setLineSpacing(0, getSpace());
        long TotalSpace = AndroidUtilities.getFolderSize(getExternalFilesDir(null));
        totalSpace = findViewById(R.id.totalSpace);
        totalSpace.setText(AndroidUtilities.getTotalSpace(TotalSpace));
        if (TotalSpace > 0) {
            findViewById(R.id.spaceManager).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AndroidUtilities.setCustomFontDialog(new AlertDialog.Builder(SettingsActivity.this)
                            .setTitle(R.string.app_name)
                            .setMessage("آیا می خواهید فضای استفاده شده، آزاد گردد؟")
                            .setPositiveButton("بله", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    AndroidUtilities.deleteCatch();
                                    totalSpace.setText("0 MB");
                                }
                            })
                            .setNegativeButton("خیر", null)
                            .show());
                }
            });
        }
    }

    private void init2() {

        font.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogFont();
            }
        });

        fontSize.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {
                int progress = seekParams.progress;
                TinyData.getInstance().putInt("systemFontSize", progress);
                tFontSize.setText(String.format("اندازه فونت : %s", progress));
                test.setTextSize(TypedValue.COMPLEX_UNIT_DIP, progress);
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {

            }
        });

        space.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {
                TinyData.getInstance().putInt("fontSpace", seekParams.progress);
                tSpace.setText(String.format("فاصله بین خطوط : %s", TinyData.getInstance().getInt("fontSpace", 1)));
                test.setLineSpacing(0, getSpace());
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {

            }
        });

        filter.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton view, boolean isChecked) {
                TinyData.getInstance().putBool("windowFilterScreen", isChecked);
                if (isChecked) {
                    blueLightFilter.setVisibility(View.VISIBLE);
                } else {
                    blueLightFilter.setVisibility(View.GONE);
                }
            }
        });

        fullScreen.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                TinyData.getInstance().putBool("windowFullScreen", isChecked);
            }
        });

        white.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                white.setBackgroundResource(R.drawable.white_sel_bg);
                sepia.setBackgroundResource(R.drawable.sepia_bg);
                dark.setBackgroundResource(R.drawable.dark_bg);
                TinyData.getInstance().putString("postBackground", "0");
                test.setBackgroundColor(0xffffffff);
                test.setTextColor(0xff000000);
            }
        });

        sepia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                white.setBackgroundResource(R.drawable.white_bg);
                sepia.setBackgroundResource(R.drawable.sepia_sel_bg);
                dark.setBackgroundResource(R.drawable.dark_bg);
                TinyData.getInstance().putString("postBackground", "1");
                test.setBackgroundColor(0xffFCF1D1);
                test.setTextColor(0xff000000);
            }
        });

        dark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                white.setBackgroundResource(R.drawable.white_bg);
                sepia.setBackgroundResource(R.drawable.sepia_bg);
                dark.setBackgroundResource(R.drawable.dark_sel_bg);
                TinyData.getInstance().putString("postBackground", "2");
                test.setBackgroundColor(0xff333333);
                test.setTextColor(0xffffffff);
            }
        });

        saveLocation.setChecked(TinyData.getInstance().getBool("loadLastLocationRead", true));
        saveLocation.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                TinyData.getInstance().putBool("loadLastLocationRead", isChecked);
            }
        });
    }

    private void disableDialog() {
        bottomSheetDialog = null;
    }

    private void dialogFont() {
        if (bottomSheetDialog != null) {
            return;
        }
        bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        bottomSheetDialog.setContentView(getLayoutInflater().inflate(R.layout.dialog_select_font, null));
        bottomSheetDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                disableDialog();
            }
        });
        bottomSheetDialog.show();
        bottomSheetDialog.findViewById(R.id.font_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TinyData.getInstance().putString("fontType", "0");
                AndroidUtilities.setTypeFont();
                font.setText(getString(R.string.font_name_1).replace(" ", " : "));
                test.setTypeface(AndroidUtilities.getLightTypeFace());
                bottomSheetDialog.dismiss();
                disableDialog();
            }
        });

        bottomSheetDialog.findViewById(R.id.font_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TinyData.getInstance().putString("fontType", "1");
                AndroidUtilities.setTypeFont();
                test.setTypeface(AndroidUtilities.getLightTypeFace());
                font.setText(getString(R.string.font_name_2).replace(" ", " : "));
                bottomSheetDialog.dismiss();
                disableDialog();
            }
        });

        bottomSheetDialog.findViewById(R.id.font_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TinyData.getInstance().putString("fontType", "2");
                AndroidUtilities.setTypeFont();
                font.setText(getString(R.string.font_name_3).replace(" ", " : "));
                test.setTypeface(AndroidUtilities.getLightTypeFace());
                bottomSheetDialog.dismiss();
                disableDialog();
            }
        });

        bottomSheetDialog.findViewById(R.id.font_4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TinyData.getInstance().putString("fontType", "3");
                AndroidUtilities.setTypeFont();
                font.setText(getString(R.string.font_name_4).replace(" ", " : "));
                test.setTypeface(AndroidUtilities.getLightTypeFace());
                bottomSheetDialog.dismiss();
                disableDialog();
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activiy_bottom_in, R.anim.activiy_bottom_out);
    }
}

