package de.blankedv.ln3pc;

import static de.blankedv.ln3pc.MainUI.DEBUG;
import static de.blankedv.ln3pc.Variables.*;
import java.util.ArrayList;

/**
 * composite route, i.e. a list of allRoutes which build a new route, is only a
 * helper for ease of use, no more functionality than the "simple" Route which
 * it is comprised of
 *
 * @author mblank
 *
 */
public class CompRoute {

    int id; // must be unique
    
        boolean active = false;
    private long timeSet;
    
    String routesString = ""; // identical to config string

    // route is comprised of a list of allRoutes
    private ArrayList<Route> myroutes = new ArrayList<>();

    /**
     * constructs a composite route
     *
     *
     */
    public CompRoute(int id, String sRoutes) {
        //
        this.id = id;

        // this string written back to config file.
        this.routesString = sRoutes;

        if (DEBUG) {
            System.out.println("creating comproute id=" + id);
        }

        // allRoutes = "12,13": these allRoutes need to be activated.
        String[] iID = routesString.split(",");
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < iID.length; i++) {
            int routeID = Integer.parseInt(iID[i]);
            for (Route rt : allRoutes) {
                try {
                    if (rt.id == routeID) {
                        myroutes.add(rt);
                    }
                } catch (NumberFormatException e) {
                }
            }
        }
        if (DEBUG) {
            System.out.println(myroutes.size() + " routes in this route.");
        }

    }

    /*	public void clear() {

		if (DEBUG)
			Log.d(TAG, "clearing comproute id=" + id);

		for (Route rt : myroutes) {
			if (rt.active == true) {
				rt.clear();
			}

		}

	} */
    public void clearOffendingRoutes() {
        if (DEBUG) {
            System.out.println(" clearing (active) offending Routes");
        }
        for (Route rt : myroutes) {
            rt.clearOffendingRoutes();

        }
    }

    public boolean set() {

        if (DEBUG) {
            System.out.println(" setting comproute id=" + id);
        }
        timeSet = System.currentTimeMillis();
        active = true;
        // check if all routes can be set successfully
        boolean res = true;
        for (Route rt : myroutes) {
            res = rt.set();
            if (res == false) {
                if (DEBUG) {
                    System.out.println("ERROR cannot set comproute id=" + id + " because route=" + rt.id + " cannot be set.");
                }
                return false;  // cannot set comproute.
            }
            // else continue with next route
        }
        return res;
    }

     public static void auto() {
        // check for auto reset of allCompRoutes
        // this function is only needed for the lanbahn-value display, because the individual single routes,
        // which are set by a compound route, are autocleared by the "Route.auto()" function
        for (CompRoute rt : allCompRoutes) {
            if (((System.currentTimeMillis() - rt.timeSet) > AUTO_CLEAR_ROUTE_TIME_SEC * 1000L)
                    && (rt.active)) {
                rt.active = false;
                lanbahnData.put(rt.id, new LbData(0, TYPE_ROUTE));  // reset lanbahn value
            }

        }

    }
}
