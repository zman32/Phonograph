package com.kabouzeid.gramophone.appwidgets;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.kabouzeid.appthemehelper.util.MaterialValueHelper;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.appwidgets.base.BaseAppWidget;
import com.kabouzeid.gramophone.glide.SongGlideRequest;
import com.kabouzeid.gramophone.glide.palette.BitmapPaletteWrapper;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.service.MusicService;
import com.kabouzeid.gramophone.ui.activities.MainActivity;
import com.kabouzeid.gramophone.util.NotificationColorUtil;


public class AppWidgetClock extends BaseAppWidget {


   //public int myTextTwo = (getUpNextAndQueueTime());


    //google's code
    /**
     * The fraction below which we select the vibrant instead of the light/dark vibrant color
     */
    private static final float POPULATION_FRACTION_FOR_MORE_VIBRANT = 0.2f; // 1f
    /**
     * Minimum saturation that a muted color must have if there exists if deciding between two
     * colors
     */
    private static final float MIN_SATURATION_WHEN_DECIDING = 0.19f;  //0.19
    /**
     * Minimum fraction that any color must have to be picked up as a text color
     */
    private static final double MINIMUM_IMAGE_FRACTION = 0.001;
    /**
     * The population fraction to select the dominant color as the text color over a the colored
     * ones.
     */
    //private static final float POPULATION_FRACTION_FOR_DOMINANT = 0.01f;
    private static final float POPULATION_FRACTION_FOR_DOMINANT = 0.009f;
    /**
     *
     */
    private static final float POPULATION_FRACTION_FOR_WHITE_OR_BLACK = 2.4f;  //2.5f higher is less chance sets background white or black
   private static final float BLACK_MAX_LIGHTNESS = 0.08f;
   private static final float WHITE_MIN_LIGHTNESS = 0.90f;
    private static final int RESIZE_BITMAP_AREA = 150* 150;  //150 * 150



    //end google's code


    public static final String NAME = "app_widget_clock";

    private static AppWidgetClock mInstance;
    private static int imageSize = 0;
    private static float cardRadius = 0f;
    private Target<BitmapPaletteWrapper> target; // for cancellation

    public static synchronized AppWidgetClock getInstance() {
        if (mInstance == null) {
            mInstance = new AppWidgetClock();
        }
        return mInstance;
    }

    /**
     * Initialize given widgets to default state, where we launch Music on
     * default click and hide actions if service not running.
     */
    protected void defaultAppWidget(final Context context, final int[] appWidgetIds) {
        final RemoteViews appWidgetView = new RemoteViews(context.getPackageName(), R.layout.app_widget_clock);

        // appWidgetView.setViewVisibility(R.id.media_titles, View.INVISIBLE);
        // appWidgetView.setImageViewResource(R.id.image, R.drawable.default_album_art);
        //  appWidgetView.setImageViewBitmap(R.id.button_next, createBitmap(Util.getTintedVectorDrawable(context, R.drawable.ic_skip_next_white_24dp, MaterialValueHelper.getSecondaryTextColor(context, true)), 1f));
        // appWidgetView.setImageViewBitmap(R.id.button_prev, createBitmap(Util.getTintedVectorDrawable(context, R.drawable.ic_skip_previous_white_24dp, MaterialValueHelper.getSecondaryTextColor(context, true)), 1f));
        // appWidgetView.setImageViewBitmap(R.id.button_toggle_play_pause, createBitmap(Util.getTintedVectorDrawable(context, R.drawable.ic_play_arrow_white_24dp, MaterialValueHelper.getSecondaryTextColor(context, true)), 1f));

        // linkButtons(context, appWidgetView);
        pushUpdate(context, appWidgetIds, appWidgetView);
    }

    /**
     * Update all active widget instances by pushing changes
     */
    public void performUpdate(final MusicService service, final int[] appWidgetIds) {
        final RemoteViews appWidgetView = new RemoteViews(service.getPackageName(), R.layout.app_widget_clock);

        final boolean isPlaying = service.isPlaying();
        final Song song = service.getCurrentSong();


        // Set the titles and artwork
        if (TextUtils.isEmpty(song.title) && TextUtils.isEmpty(song.artistName)) {
            // appWidgetView.setViewVisibility(R.id.media_titles, View.INVISIBLE);
        } else {
            if (TextUtils.isEmpty(song.title) || TextUtils.isEmpty(song.artistName)) {
                //  appWidgetView.setTextViewText(R.id.text_separator, "");
            } else {
                // appWidgetView.setTextViewText(R.id.text_separator, "•");
            }

            //  appWidgetView.setViewVisibility(R.id.media_titles, View.VISIBLE);
            //appWidgetView.setTextViewText(R.id.title, song.title);
            //  appWidgetView.setTextViewText(R.id.text, song.artistName);
        }

        // Link actions buttons to intents
        linkButtons(service, appWidgetView);

        if (imageSize == 0)
            //imageSize = service.getResources().getDimensionPixelSize(R.dimen.app_widget_small_image_size);
            imageSize = service.getResources().getDimensionPixelSize(R.dimen.app_widget_classic_image_size)*2;
        if (cardRadius == 0f)
            cardRadius = service.getResources().getDimension(R.dimen.app_widget_card_radius);

        // Load the album cover async and push the update on completion
        final Context appContext = service.getApplicationContext();
        service.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (target != null) {
                    Glide.clear(target);
                }
                target = SongGlideRequest.Builder.from(Glide.with(appContext), song)
                        .checkIgnoreMediaStore(appContext)
                        .generatePalette(service).build()
                        .centerCrop()
                        .into(new SimpleTarget<BitmapPaletteWrapper>(imageSize, imageSize) {
                            @Override
                            public void onResourceReady(BitmapPaletteWrapper resource, GlideAnimation<? super BitmapPaletteWrapper> glideAnimation) {
                                Palette palette = resource.getPalette();
                                update(resource.getBitmap(), palette.getVibrantColor(palette.getMutedColor(MaterialValueHelper.getSecondaryTextColor(appContext, true))));
                            }

                            @Override
                            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                super.onLoadFailed(e, errorDrawable);
                                update(null, MaterialValueHelper.getSecondaryTextColor(appContext, true));
                            }

                            private void update(@Nullable Bitmap bitmap, int color) {
                                // Set correct drawable for pause state
                                //   int playPauseRes = isPlaying ? R.drawable.ic_pause_white_24dp : R.drawable.ic_play_arrow_white_24dp;
                                //   appWidgetView.setImageViewBitmap(R.id.button_toggle_play_pause, createBitmap(Util.getTintedVectorDrawable(service, playPauseRes, color), 1f));

                                // Set prev/next button drawables
                                //    appWidgetView.setImageViewBitmap(R.id.button_next, createBitmap(Util.getTintedVectorDrawable(service, R.drawable.ic_skip_next_white_24dp, color), 1f));
                                //    appWidgetView.setImageViewBitmap(R.id.button_prev, createBitmap(Util.getTintedVectorDrawable(service, R.drawable.ic_skip_previous_white_24dp, color), 1f));

                                final Drawable image = getAlbumArtDrawable(service.getResources(), bitmap);
                                final Bitmap roundedBitmap = createRoundedBitmap(image, imageSize, imageSize, cardRadius, 0, 0, 0);
                                //   appWidgetView.setImageViewBitmap(R.id.image, roundedBitmap);

                                appWidgetView.setTextColor(R.id.Hour, Color.WHITE);
                                appWidgetView.setTextColor(R.id.Sep, Color.WHITE);
                                appWidgetView.setTextColor(R.id.Minute, Color.WHITE);
                                appWidgetView.setTextColor(R.id.AmPm, Color.WHITE);

                                appWidgetView.setTextColor(R.id.day, Color.WHITE);
                                appWidgetView.setTextColor(R.id.date, Color.WHITE);
                                appWidgetView.setTextColor(R.id.month, Color.WHITE);

                             //   appWidgetView.setInt(R.id.tmuted, "setBackgroundColor", Color.BLACK);
                              //  appWidgetView.setInt(R.id.tvibrant, "setBackgroundColor", Color.BLACK);
                              //  appWidgetView.setInt(R.id.tmorevibrant, "setBackgroundColor", Color.BLACK);


                                if (bitmap != null) {

                                    Palette palette = Palette.from(roundedBitmap)
                                            .clearFilters()

                                            .generate();

                                    Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
                                    Palette.Swatch moreVibrantSwatch = palette.getLightVibrantSwatch();
                                    Palette.Swatch lightMutedSwatch = palette.getLightMutedSwatch();


                                    if (lightMutedSwatch != null){

                                        // appWidgetView.setTextViewText(R.id.Hour, song.title);

                                        int theVibrantColor = lightMutedSwatch.getRgb();

                                        int myContrastColor;

                                        myContrastColor = NotificationColorUtil.findContrastColorAgainstDark(theVibrantColor, Color.BLACK, true, 2.5);  //5 is good so far  3.5  last was 4

                                        int myShiftedClockTextColor = NotificationColorUtil.getShiftedColor(myContrastColor , -5);  // -1
                                        int myShiftedDarkerTextColor = NotificationColorUtil.getShiftedColor(myContrastColor , 11); //10


                                        appWidgetView.setTextColor(R.id.Hour, (myShiftedClockTextColor));
                                        appWidgetView.setTextColor(R.id.Sep, (myShiftedDarkerTextColor));
                                        appWidgetView.setTextColor(R.id.Minute, (myShiftedClockTextColor));
                                        appWidgetView.setTextColor(R.id.AmPm, (myShiftedDarkerTextColor));

                                        appWidgetView.setTextColor(R.id.day, (myShiftedDarkerTextColor));
                                        appWidgetView.setTextColor(R.id.date, (myShiftedClockTextColor));
                                        appWidgetView.setTextColor(R.id.month, (myShiftedDarkerTextColor));

                                 //       appWidgetView.setInt(R.id.tmuted, "setBackgroundColor", myShiftedClockTextColor);;


                                    }




                                    if (moreVibrantSwatch != null){

                                        int theVibrantColor = moreVibrantSwatch.getRgb();

                                        int myContrastColor;

                                        myContrastColor = NotificationColorUtil.findContrastColorAgainstDark(theVibrantColor, Color.BLACK, true, 2.5);  //5 is good so far  3.5  last was 4

                                        int myShiftedClockTextColor = NotificationColorUtil.getShiftedColor(myContrastColor , -5);  // -1
                                        int myShiftedDarkerTextColor = NotificationColorUtil.getShiftedColor(myContrastColor , 11); //10


                                        appWidgetView.setTextColor(R.id.Hour, (myShiftedClockTextColor));
                                        appWidgetView.setTextColor(R.id.Sep, (myShiftedDarkerTextColor));
                                        appWidgetView.setTextColor(R.id.Minute, (myShiftedClockTextColor));
                                        appWidgetView.setTextColor(R.id.AmPm, (myShiftedDarkerTextColor));

                                        appWidgetView.setTextColor(R.id.day, (myShiftedDarkerTextColor));
                                        appWidgetView.setTextColor(R.id.date, (myShiftedClockTextColor));
                                        appWidgetView.setTextColor(R.id.month, (myShiftedDarkerTextColor));



                                //        appWidgetView.setInt(R.id.tmorevibrant, "setBackgroundColor", myShiftedClockTextColor);


                                    }



                                    if (vibrantSwatch != null){

                                        // appWidgetView.setTextViewText(R.id.Hour, song.title);
                                        int theVibrantColor = vibrantSwatch.getRgb();

                                        int myContrastColor;

                                        myContrastColor = NotificationColorUtil.findContrastColorAgainstDark(theVibrantColor, Color.BLACK, true, 2.5);  //5 is good so far  3.5  last was 4

                                        int myShiftedClockTextColor = NotificationColorUtil.getShiftedColor(myContrastColor , -5);  // -1
                                        int myShiftedDarkerTextColor = NotificationColorUtil.getShiftedColor(myContrastColor , 11); //10


                                        appWidgetView.setTextColor(R.id.Hour, (myShiftedClockTextColor));
                                        appWidgetView.setTextColor(R.id.Sep, (myShiftedDarkerTextColor));
                                        appWidgetView.setTextColor(R.id.Minute, (myShiftedClockTextColor));
                                        appWidgetView.setTextColor(R.id.AmPm, (myShiftedDarkerTextColor));

                                        appWidgetView.setTextColor(R.id.day, (myShiftedDarkerTextColor));
                                        appWidgetView.setTextColor(R.id.date, (myShiftedClockTextColor));
                                        appWidgetView.setTextColor(R.id.month, (myShiftedDarkerTextColor));


                          //              appWidgetView.setInt(R.id.tvibrant, "setBackgroundColor", myShiftedClockTextColor);



                                    }





                                }




                                pushUpdate(appContext, appWidgetIds, appWidgetView);
                            }
                        });
            }
        });
    }

    /**
     * Link up various button actions using {@link PendingIntent}.
     */
    private void linkButtons(final Context context, final RemoteViews views) {
        Intent action;
        PendingIntent pendingIntent;

        final ComponentName serviceName = new ComponentName(context, MusicService.class);

        // Home
        action = new Intent(context, MainActivity.class);
        action.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        pendingIntent = PendingIntent.getActivity(context, 0, action, 0);
        views.setOnClickPendingIntent(R.id.image, pendingIntent);
        views.setOnClickPendingIntent(R.id.media_titles, pendingIntent);

        // Previous track
        pendingIntent = buildPendingIntent(context, MusicService.ACTION_REWIND, serviceName);
        views.setOnClickPendingIntent(R.id.button_prev, pendingIntent);

        // Play and pause
        pendingIntent = buildPendingIntent(context, MusicService.ACTION_TOGGLE_PAUSE, serviceName);
        views.setOnClickPendingIntent(R.id.button_toggle_play_pause, pendingIntent);

        // Next track
        pendingIntent = buildPendingIntent(context, MusicService.ACTION_SKIP, serviceName);
        views.setOnClickPendingIntent(R.id.button_next, pendingIntent);
    }
}
