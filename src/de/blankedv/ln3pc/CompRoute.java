package de.blankedv.ln3pc;

import static de.blankedv.ln3pc.MainUI.DEBUG;
import static de.blankedv.ln3pc.Variables.*;
import java.util.ArrayList;

/**
 * composite route, i.e. a list of routes which build a new route, is only a
 * helper for ease of use, no more functionality than the "simple" Route which
 * it is comprised of
 *
 * @author mblank
 *
 */
public class CompRoute {

    int id; // must be unique
    String routesString = ""; // identical to config string

    // route is comprised of a list of routes
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

        // routes = "12,13": these routes need to be activated.
        String[] iID = routesString.split(",");
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < iID.length; i++) {
            int routeID = Integer.parseInt(iID[i]);
            for (Route rt : routes) {
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

    public void set() {

        if (DEBUG) {
            System.out.println(" setting comproute id=" + id);
        }

        for (Route rt : myroutes) {
            rt.set();

        }
    }

}
