package com.mousebirdconsulting.charlyexample;

import android.util.Log;

import com.mousebird.maply.GlobeController;
import com.mousebird.maply.GlobeMapFragment;
import com.mousebird.maply.MaplyBaseController;
import com.mousebird.maply.Point2d;
import com.mousebird.maply.Point3d;
import com.mousebird.maply.QuadImageTileLayer;
import com.mousebird.maply.RemoteTileInfo;
import com.mousebird.maply.RemoteTileSource;
import com.mousebird.maply.SelectedObject;
import com.mousebird.maply.SphericalMercatorCoordSystem;

import java.io.File;

/**
 * Subclass of main globe/map fragment.
 */
public class CharlyGlobeFragment extends GlobeMapFragment implements GlobeController.GestureDelegate
{
    protected void preControlCreated() {
        mapDisplayType = MapDisplayType.Globe;
    }

    // Set up a basemap from CartoDB
    // Note: You should read the terms of service on their data
    private QuadImageTileLayer setupImageLayer(MaplyBaseController baseController)
    {
        String cacheDirName = "cartodb_light2";
        File cacheDir = new File(getActivity().getCacheDir(), cacheDirName);
        cacheDir.mkdir();
        RemoteTileSource remoteTileSource = new RemoteTileSource(new RemoteTileInfo("http://light_all.basemaps.cartocdn.com/light_all/", "png", 0, 22));
        remoteTileSource.setCacheDir(cacheDir);
        SphericalMercatorCoordSystem coordSystem = new SphericalMercatorCoordSystem();
        QuadImageTileLayer baseLayer = new QuadImageTileLayer(baseController, coordSystem, remoteTileSource);
        baseLayer.setCoverPoles(true);
        baseLayer.setHandleEdges(true);

        baseLayer.setDrawPriority(MaplyBaseController.ImageLayerDrawPriorityDefault);
        return baseLayer;
    }

    CharlyFeed feed = null;

    /**
     * Called after things are started.  We'll set up everything here.
     */
    protected void controlHasStarted()
    {
        QuadImageTileLayer baseLayer = setupImageLayer(globeControl);

        globeControl.gestureDelegate = this;
        globeControl.addLayer(baseLayer);

        Point2d startPt = Point2d.FromDegrees(-79.4,43.7);
        globeControl.setPositionGeo(startPt.getX(),startPt.getY(),1.0);

        // Cache directory for avatars
        String cacheDirName = "avatarImages";
        File cacheDir = new File(getActivity().getCacheDir(), cacheDirName);
        cacheDir.mkdir();


        // Let's get the JSON feed going
        feed = new CharlyFeed(baseControl,getActivity(),"<replace-with-url-here>",cacheDir);
        feed.startFetch();
    }

    /**
     * The user selected the given object.  Up to you to figure out what it is.
     *
     * @param globeControl The maply controller this is associated with.
     * @param selObjs The objects the user selected (e.g. MaplyScreenMarker).
     * @param loc The location they tapped on.  This is in radians.
     * @param screenLoc The location on the OpenGL surface.
     */
    public void userDidSelect(GlobeController globeControl,SelectedObject selObjs[],Point2d loc,Point2d screenLoc)
    {
        Log.d("CharlyExample","Selected " + selObjs.length + " objects.");
    }

    /**
     * The user tapped somewhere, but not on a selectable object.
     *
     * @param globeControl The maply controller this is associated with.
     * @param loc The location they tapped on.  This is in radians.  If null, then the user tapped outside the globe.
     * @param screenLoc The location on the OpenGL surface.
     */
    public void userDidTap(GlobeController globeControl,Point2d loc,Point2d screenLoc)
    {

    }

    /**
     * The user tapped outside of the globe.
     *
     * @param globeControl The maply controller this is associated with.
     * @param screenLoc The location on the OpenGL surface.
     */
    public void userDidTapOutside(GlobeController globeControl,Point2d screenLoc)
    {

    }

    /**
     * The user did long press somewhere, there might be an object
     * @param globeControl The maply controller this is associated with.
     * @param selObjs The objects the user selected (e.g. MaplyScreenMarker) or null if there was no object.
     * @param loc The location they tapped on.  This is in radians.  If null, then the user tapped outside the globe.
     * @param screenLoc The location on the OpenGL surface.
     */
    public void userDidLongPress(GlobeController globeControl, SelectedObject selObjs[], Point2d loc, Point2d screenLoc)
    {

    }

    /**
     * Called when the globe first starts moving.
     *
     * @param globeControl The globe controller this is associated with.
     * @param userMotion Set if the motion was caused by a gesture.
     */
    public void globeDidStartMoving(GlobeController globeControl, boolean userMotion)
    {

    }

    /**
     * Called when the globe stops moving.
     *
     * @param globeControl The globe controller this is associated with.
     * @param corners Corners of the viewport.  If one of them is null, that means it doesn't land on the globe.
     * @param userMotion Set if the motion was caused by a gesture.
     */
    public void globeDidStopMoving(GlobeController globeControl, Point3d corners[], boolean userMotion)
    {

    }

    /**
     * Called for every single visible frame of movement.  Be careful what you do in here.
     *
     * @param globeControl The globe controller this is associated with.
     * @param corners Corners of the viewport.  If one of them is null, that means it doesn't land on the globe.
     * @param userMotion Set if the motion was caused by a gesture.
     */
    public void globeDidMove(GlobeController globeControl,Point3d corners[], boolean userMotion)
    {

    }


}
