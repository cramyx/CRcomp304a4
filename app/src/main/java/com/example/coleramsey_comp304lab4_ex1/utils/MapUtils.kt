package com.example.coleramsey_comp304lab4_ex1.utils

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil

object MapUtils {
    fun calculateDistance(start: LatLng, end: LatLng): Double {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            start.latitude, start.longitude,
            end.latitude, end.longitude,
            results
        )
        return results[0].toDouble()
    }

    fun isPointWithinPolygon(point: LatLng, polygon: List<LatLng>): Boolean {
        return PolyUtil.containsLocation(point, polygon, true)
    }
}