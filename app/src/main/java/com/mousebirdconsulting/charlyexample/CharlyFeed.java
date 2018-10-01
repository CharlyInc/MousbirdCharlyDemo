package com.mousebirdconsulting.charlyexample;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import com.mousebird.maply.BasicClusterGenerator;
import com.mousebird.maply.ClusterGenerator;
import com.mousebird.maply.MaplyBaseController;
import com.mousebird.maply.MaplyTexture;
import com.mousebird.maply.MarkerInfo;
import com.mousebird.maply.Point2d;
import com.mousebird.maply.ScreenMarker;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Fetch the
 */
class CharlyFeed
{
    MaplyBaseController baseControl;
    String jsonURLString;
    private final OkHttpClient client = new OkHttpClient();
    File cacheDir;

    CharlyFeed(MaplyBaseController inBaseControl, Activity activity, String urlString, File inCacheDir)
    {
        baseControl = inBaseControl;
        jsonURLString = urlString;
        markerInfo.setClusterGroup(1);
        markerInfo.setLayoutImportance(1.f);
        markerInfo.setFade(0.f);
        cacheDir = inCacheDir;

        // Set up a cluster group too
        Bitmap bitmap = BitmapFactory.decodeResource(baseControl.getContentView().getResources(),R.drawable.clusterbackground);
        ClusterGenerator ourClusterGen = new BasicClusterGenerator(bitmap,1,new Point2d(128,128),40.f,baseControl,activity);
        baseControl.addClusterGenerator(ourClusterGen);
    }

    // Single user from JSON
    protected class User
    {
        public User(String inId,String inUserName,String inName,Double coords[],String inAvatar)
        {
            id = inId;
            userName = inUserName;
            name = inName;
            coord = Point2d.FromDegrees(coords[1],coords[0]);
            avatar = inAvatar;
        }

        public String id;
        public String userName;
        public String name;
        public Point2d coord;
        public String avatar;
        boolean avatarLoaded = false;
        public ScreenMarker marker;
        MaplyTexture texture;
    }

    // Connection task fetches the image
    private class JSONTask implements com.squareup.okhttp.Callback
    {
        JSONTask()
        {
        }

        // Callback from OK HTTP on tile loading failure
        public void onFailure(Request request, IOException e) {
            Log.e("CharlyExample", "Failed to fetch JSON feed.");
        }

        // Callback from OK HTTP on success
        public void onResponse(Response response) {
            if (!response.isSuccessful())
            {
                Log.e("CharlyExample","Failed to fetch JSON feed.");
            } else {
                try
                {
                    // Turn this into JSON
                    JsonReader reader = new JsonReader(response.body().charStream());
                    try {
                        setUsers(readUserArray(reader));
                    } finally {
                        reader.close();
                    }
                }
                catch (Exception e)
                {
                    Log.e("CharlyExample","Failed to parse JSON feed.");
                }
            }
        }

        // Courtesy Android docs adapted for our use
        public List<User> readUserArray(JsonReader reader) throws IOException {
            List<User> messages = new ArrayList<User>();

            reader.beginArray();
            while (reader.hasNext()) {
                messages.add(readUser(reader));
            }
            reader.endArray();
            return messages;
        }

        // Courtesy Android docs adapted for our use
        public User readUser(JsonReader reader) throws IOException {
            String id = null;
            String userName = null;
            String name = null;
            List<Double> coords = null;
            String avatar = null;

            reader.beginObject();
            while (reader.hasNext()) {
                String key = reader.nextName();
                if (key.equals("id")) {
                    id = reader.nextString();
                } else if (key.equals("username")) {
                    userName = reader.nextString();
                } else if (key.equals("name")) {
                    name = reader.nextString();
                } else if (key.equals("coords") && reader.peek() != JsonToken.NULL) {
                    coords = readDoublesArray(reader);
                } else if (key.equals("avatar")) {
                    avatar = reader.nextString();
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();

            return new User(id,userName,name,coords.toArray(new Double[coords.size()]),avatar);
        }

        public List<Double> readDoublesArray(JsonReader reader) throws IOException {
            List<Double> doubles = new ArrayList<Double>();

            reader.beginArray();
            while (reader.hasNext()) {
                doubles.add(reader.nextDouble());
            }
            reader.endArray();
            return doubles;
        }
    }

    // Kick off the request and do the rest of it
    void startFetch()
    {
        Request request = new Request.Builder()
                .url(jsonURLString)
                .build();

        client.newCall(request).enqueue(new JSONTask());
    }

    List<User> users = null;
    MarkerInfo markerInfo = new MarkerInfo();

    // Display a user when we've gotten an avatar
    void displayUser(User user,Bitmap bm)
    {
        if (bm != null)
        {
            ScreenMarker marker = new ScreenMarker();
            marker.image = bm;
            marker.loc = user.coord;
            marker.size = new Point2d(128,128);

            baseControl.addScreenMarker(marker,markerInfo, MaplyBaseController.ThreadMode.ThreadAny);
        }
    }

    // Users were parsed, so start displaying them
    void setUsers(List<User> inUsers)
    {
        users = inUsers;

        // Display a list of blank markers and start loading the avatars
        for (User user : users)
        {
            // See if it's cached
            final File cacheFile = new File(cacheDir,user.id);
            Bitmap bm = null;
            if (cacheFile.exists())
            {
                try
                {
                    BufferedInputStream aBufferedInputStream = new BufferedInputStream(new FileInputStream(cacheFile));
                    bm = BitmapFactory.decodeStream(aBufferedInputStream,null,null);
                }
                catch (Exception e)
                {
                }
            }

            if (bm != null)
            {
                displayUser(user, bm);
            } else {
                // Fetch the appropiate avatar.  We assume no duplication
                final User thisUser = user;
                try {
                    URL url = new URL(user.avatar);

                    Request request = new Request.Builder().url(url).build();
                    client.newCall(request).enqueue(
                            new com.squareup.okhttp.Callback() {
                                @Override
                                public void onFailure(Request request, IOException e) {
                                    Log.e("CharlyExample", "Failed to fetch avatar: " + thisUser.avatar);
                                }

                                @Override
                                public void onResponse(Response response) {
                                    if (!response.isSuccessful()) {
                                        Log.e("CharlyExample", "Failed to fetch avatar: " + thisUser.avatar);
                                    } else {
                                        byte[] rawImage = null;
                                        Bitmap bm = null;
                                        try {
                                            rawImage = response.body().bytes();
                                            bm = BitmapFactory.decodeByteArray(rawImage, 0, rawImage.length, null);
                                        } catch (Exception e) {
                                        }

                                        // Cache the file
                                        if (cacheFile != null && bm != null) {
                                            OutputStream fOut;
                                            try {
                                                fOut = new FileOutputStream(cacheFile);
                                                fOut.write(rawImage);
                                                fOut.close();
                                            }
                                            catch (Exception e)
                                            {
                                            }
                                        }

                                        // Display
                                        displayUser(thisUser, bm);
                                    }
                                }
                            }
                    );
                } catch (Exception e) {

                }
            }
        }
    }
}