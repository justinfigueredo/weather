package com.example.org.weather

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat.requestPermissions
import android.location.Geocoder
import java.util.*

class LocationInfo (context : Context, mainAct : MainActivity, unitBoolean : Boolean){
    // https://stackoverflow.com/questions/1513485/how-do-i-get-the-current-gps-location-programmatically-in-android

    private var latitude = 0.0
    private var longitude = 0.0
    private var temperature = 0.0
    private var humidity = 0.0
    private var tempUnitFahrenheit = unitBoolean
    private var mContext = context
    private var mAct = mainAct
    private var city = ""
    private var state = ""
    private var postalCode = ""
    private val REQUEST_PERMISSION_LOCATION = 255
    private var locationManager = (mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager)

    private var locationListener = object : LocationListener {

        override fun onLocationChanged(location: Location?) {
            if (location != null) {
                latitude = location.latitude
                longitude = location.longitude

                var geo = Geocoder(mAct)
                var addressInfo = geo.getFromLocation(latitude, longitude, 1)[0]

                city = addressInfo.locality
                state = addressInfo.adminArea
                postalCode = addressInfo.postalCode
                var resp = ApiInterface.GetRealTimeStats(latitude, longitude, tempUnitFahrenheit) { stats ->
                    if(stats != null){
                        this@LocationInfo.temperature = stats.temp.value!!
                        this@LocationInfo.humidity = stats.humidity.value!!
                        Log.d("temp", temperature.toString())
                        locationManager.removeUpdates(this)
                        mAct.supportFragmentManager.beginTransaction()
                            .replace(R.id.fragContainer, weatherDisplayFragment.newInstance(this@LocationInfo))
                            .commit()
                    }
                }

            }
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?){

        }

        override fun onProviderEnabled(provider: String?) {

        }

        override fun onProviderDisabled(provider: String?) {

        }

    }

    fun updateLocationInfo(){
        var gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        Log.d("INFO", "GPS is " + gpsEnabled.toString())

        if (!gpsEnabled){
            Log.i("WARNING", "GPS NOT AVAILABLE REQUESTING GPS ENABLE...")
            buildAlertNoGps()
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }

        if (gpsEnabled){
            if (!checkPermissions(locationManager)){
                getPermission()
            }
            else {
                getLocationInfo()
            }
        }
    }

    fun getLocationInfo(){
        try {
            var location = locationManager.getLastKnownLocation(Context.LOCATION_SERVICE)

            if (location != null && location.time > Calendar.getInstance().timeInMillis - 2 * 60 * 100){
                latitude = location.latitude
                longitude = location.longitude
                var resp = ApiInterface.GetRealTimeStats(latitude, longitude, tempUnitFahrenheit) { stats ->
                    if(stats != null){
                        this@LocationInfo.temperature = stats.temp.value!!
                        this@LocationInfo.humidity = stats.humidity.value!!
                        Log.d("temp", temperature.toString())
                        mAct.supportFragmentManager.beginTransaction()
                            .replace(R.id.fragContainer, weatherDisplayFragment.newInstance(this@LocationInfo))
                            .commit()
                    }
                }


            }
            else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0.0f, locationListener)
            }

        }
        catch (e: SecurityException){
            Log.d("ERROR", "Security Exception even after receiving location permission")
        }
    }

    fun buildAlertNoGps(){
        var builder = AlertDialog.Builder(mContext, R.style.Theme_AppCompat_Dialog_Alert)

        val positiveButtonClick = { dialog: DialogInterface, which: Int ->
            mContext.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))

        }

        val negativeButtonClick = { dialog: DialogInterface, which: Int ->
            dialog.cancel()
        }

        builder.setMessage("Your GPS is disabled, do you want to enable it?")
            .setCancelable(false)
            .setPositiveButton("Yes", DialogInterface.OnClickListener(function = positiveButtonClick))
            .setNegativeButton("No", DialogInterface.OnClickListener(function = negativeButtonClick))


    }

    fun checkPermissions(manager : LocationManager): Boolean{ // For when GPS is enabled
        // https://stackoverflow.com/questions/36280564/how-to-solve-gps-location-provider-security-exception-in-android
        if (mContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            return true
        }

        return false
    }

    private fun getPermission(){
        requestPermissions(
            mAct,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_PERMISSION_LOCATION
        )

    }

    fun logInfo(){
        Log.i("INFO", "Latitude: " + latitude.toString())
        Log.i("INFO", "Longitude: " + longitude.toString())

    }

    fun getLatitude() : Double{

        return latitude
    }

    fun getLongitude() : Double {

        return longitude
    }

    fun getTemperature() : Double{

        return temperature
    }

    fun getUnitBoolean() : Boolean {

        return tempUnitFahrenheit
    }

    fun getHumidity() : Double {

        return humidity
    }

    fun getRequestPermissionCode() : Int{

        return REQUEST_PERMISSION_LOCATION
    }

    fun setCity(city : String){

        this.city = city
    }

    fun getCity() : String {

        return city
    }

    fun setState(state : String){

        this.state = state
    }

    fun getState() : String {

        return state
    }

    fun setPostalCode(postalCode : String){

        this.postalCode = postalCode
    }

    fun getPostalCode() : String{

        return postalCode
    }

    fun setUseFahrenheit(bool : Boolean){
        this@LocationInfo.tempUnitFahrenheit = bool
        var resp = ApiInterface.GetRealTimeStats(latitude, longitude, tempUnitFahrenheit) { stats ->
            if(stats != null){
                this@LocationInfo.temperature = stats.temp.value!!
                this@LocationInfo.humidity = stats.humidity.value!!
                Log.d("temp", temperature.toString())
                mAct.supportFragmentManager.beginTransaction()
                    .replace(R.id.fragContainer, weatherDisplayFragment.newInstance(this@LocationInfo))
                    .commit()
            }
        }
    }
}