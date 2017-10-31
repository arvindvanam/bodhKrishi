package com.bodhileaf.buttontest;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.net.URISyntaxException;
import java.util.Locale;
import android.widget.Toast ;

/**
 * Created by shwetathareja on 10/24/17.
 */

import android.content.res.Configuration ;
import android.content.res.Resources;

public class FileUtils {

    public static String getPath(Context  context, Uri  uri) throws URISyntaxException  {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { "_data" };
            Cursor  cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static void changeLang(Context context , String locale) {
        Configuration config = context.getResources().getConfiguration();

        switch (locale ) {
            case "hi" :
                Locale lang = new Locale("hi","rIN");
                config.setLocale(lang );

                // Toast.makeText(context , "string:" + lang.toString()+ " lang:" +  config.getLocales().toString(), Toast.LENGTH_LONG).show();

            default :
                config.setLocale(Locale.ENGLISH );
        };


    }




}
