package com.srirangadigital.tungalahari;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by root on 17/4/17.
 */

public class JsonOperator {

    public static JSONObject loadJSONObjectFromAsset(Context myContext, String jsonPath) {

        jsonPath = jsonPath.replace("album_", "");
        String json = null;
        try {

            InputStream is = myContext.getAssets().open(jsonPath);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");

            try {

                return new JSONObject(json);
            }
            catch (JSONException e) {

                e.printStackTrace();
                return null;
            }
        }
        catch (IOException ex) {

            ex.printStackTrace();
            return null;
        }
    }
}
