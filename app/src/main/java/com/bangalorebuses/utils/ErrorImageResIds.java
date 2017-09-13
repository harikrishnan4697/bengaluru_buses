package com.bangalorebuses.utils;

import com.bangalorebuses.R;

/**
 * This class is used to store resource ids of images used
 * in error messages.
 *
 * @author Nihar Thakkar
 * @version 1.0
 * @since 11/9/2017
 */

public class ErrorImageResIds
{
    // Location error images
    public static int ERROR_IMAGE_LOCATION_ACCESS_DENIED = R.drawable.ic_location_disabled_black;
    public static int ERROR_IMAGE_LOCATION_OFF = R.drawable.ic_location_off_black;
    public static int ERROR_IMAGE_NO_BUS_STOPS_NEARBY = R.drawable.ic_person_pin_circle_black;
    public static int ERROR_IMAGE_GOOGLE_PLAY_SERVICES_UPDATE_REQUIRED = R.drawable.ic_location_disabled_black;

    // Networking error images
    public static int ERROR_IMAGE_NO_INTERNET = R.drawable.ic_cloud_off_black;
    public static int ERROR_IMAGE_SOMETHING_WENT_WRONG = R.drawable.ic_sad_face;
    public static int ERROR_IMAGE_SLOW_NETWORK = R.drawable.ic_cloud_off_black;

    // Other error images
    public static int ERROR_IMAGE_NO_BUSES_IN_SERVICE = R.drawable.ic_directions_bus_black_big;

}
