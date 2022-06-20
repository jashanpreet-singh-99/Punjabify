package com.ck.dev.punjabify.utils;

import com.ck.dev.punjabify.R;

public class GenreConfig {

    public static final String GENRE_GEDI       = "Gedi";
    public static final String GENRE_HIP_HOP    = "HipHop";
    public static final String GENRE_JATTISM    = "Jattism";
    public static final String GENRE_LEGEND     = "Legend";
    public static final String GENRE_LONG_DRIVE = "LongDrive";
    public static final String GENRE_MAHFIL     = "Mahfil";
    public static final String GENRE_ORIGINAL   = "Original";
    public static final String GENRE_PARENTAL   = "Parental";
    public static final String GENRE_PARTY      = "Party";
    public static final String GENRE_PRO        = "Pro";
    public static final String GENRE_RAP        = "Rap";
    public static final String GENRE_ROMANTIC   = "Romantic";
    public static final String GENRE_SAD        = "Sad";

    private static final String[] GENRES = {
            GENRE_GEDI,
            GENRE_HIP_HOP,
            GENRE_JATTISM,
            GENRE_LEGEND,
            GENRE_LONG_DRIVE,
            GENRE_MAHFIL,
            GENRE_ORIGINAL,
            GENRE_PARENTAL,
            GENRE_PARTY,
            GENRE_PRO,
            GENRE_RAP,
            GENRE_ROMANTIC,
            GENRE_SAD
    };

    public static String[] getGenres() {
        return GENRES;
    }

    public static int getGenreResource(String genre) {
        switch (genre) {
            case GENRE_GEDI:
                return R.drawable.rounded_gradient_20_green;
            case GENRE_HIP_HOP:
                return R.drawable.rounded_gradient_20_yellow;
            case GENRE_JATTISM:
                return R.drawable.rounded_gradient_20_red;
            case GENRE_LEGEND:
                return R.drawable.rounded_gradient_20_gray;
            case GENRE_LONG_DRIVE:
                return R.drawable.rounded_gradient_20_sky_blue;
            case GENRE_MAHFIL:
                return R.drawable.rounded_gradient_20_purple;
            case GENRE_PARTY:
                return R.drawable.rounded_gradient_20_blast;
            case GENRE_PARENTAL:
                return R.drawable.rounded_gradient_20_dark_green;
            case GENRE_RAP:
                return R.drawable.rounded_gradient_20_violet;
            case GENRE_ROMANTIC:
                return R.drawable.rounded_gradient_20_pink;
            case GENRE_SAD:
                return R.drawable.rounded_gradient_20_blue;
            default:
                return R.drawable.rounded_btn_20;
        }
    }

    public static String getGenreColor(String genre) {
        switch (genre) {
            case GENRE_GEDI:
                return "#00d4d5";
            case GENRE_HIP_HOP:
                return "#ffa246";
            case GENRE_JATTISM:
                return "#ee5757";
            case GENRE_LEGEND:
                return "#5a6877";
            case GENRE_LONG_DRIVE:
                return "#0bbfe4";
            case GENRE_MAHFIL:
                return "#944ce6";
            case GENRE_PARTY:
                return "#FD80A8";
            case GENRE_PARENTAL:
                return "#7CFF6B";
            case GENRE_RAP:
                return "#AB84C8";
            case GENRE_ROMANTIC:
                return "#f36de1";
            case GENRE_SAD:
                return "#57a3ff";
            default:
                return "#FF0800";
        }
    }

}
