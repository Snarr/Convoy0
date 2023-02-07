package edu.temple.convoy

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings.Global
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import edu.temple.convoy.databinding.ActivityMapsBinding
import kotlinx.coroutines.*
import okhttp3.MultipartBody
import okhttp3.RequestBody

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    val convoyApi = RetrofitHelper.getInstance().create(Convoy::class.java)

    lateinit var convoySessionViewModel: ConvoySession

    val locationManager : LocationManager by lazy {
        getSystemService(LocationManager::class.java)
    }

    lateinit var locationListener: LocationListener

    var previousLocation : Location? = null
    var distanceTraveled = 0f


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        convoySessionViewModel = ViewModelProvider(this).get(ConvoySession::class.java)

        if (!permissionGranted()) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 123)
        }

        locationListener = LocationListener {
            if (previousLocation != null) {
                distanceTraveled += it.distanceTo(previousLocation)

                val latLng = LatLng(it.latitude, it.longitude)

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
            }
            previousLocation = it
        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val startConvoyButton = findViewById<ImageButton>(R.id.startConvoyButton)
        startConvoyButton.setOnClickListener {

            GlobalScope.launch {
                createConvoy()
            }
        }

        val stopConvoyButton = findViewById<ImageButton>(R.id.stopConvoyButton)
        stopConvoyButton.setOnClickListener {
            GlobalScope.launch {
                queryConvoy()

                endConvoy()
            }
        }

        val joinConvoyButton = findViewById<ImageButton>(R.id.joinConvoyButton)

    }

    @SuppressLint("MissingPermission")
    private fun doGPSStuff() {
        if (permissionGranted())
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000.toLong(), 10f, locationListener)
    }

    private fun permissionGranted () : Boolean {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                finish()
            }
        }

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }

    suspend fun createConvoy() {
        withContext(Dispatchers.IO) {
            val body: RequestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("action", "CREATE")
                .addFormDataPart("username", this@MapsActivity.intent.getStringExtra("username"))
                .addFormDataPart("session_key", this@MapsActivity.intent.getStringExtra("session_key"))
                .build()

            val result = convoyApi.update(body)
            result?.run {
                Log.d("create: ", this.body().toString())
                this.body()?.run {
                    if (this.status == "SUCCESS") {
                        val convoy_id = this.convoy_id
                        withContext(Dispatchers.Main) {
                            convoySessionViewModel.setConvoyId(convoy_id)
                            (findViewById<ImageButton>(R.id.startConvoyButton)).visibility = View.GONE
                            (findViewById<ImageButton>(R.id.stopConvoyButton)).visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    suspend fun endConvoy() {
        withContext(Dispatchers.IO) {
            val body: RequestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("action", "END")
                .addFormDataPart("username", this@MapsActivity.intent.getStringExtra("username"))
                .addFormDataPart("session_key", this@MapsActivity.intent.getStringExtra("session_key"))
                .addFormDataPart("convoy_id", convoySessionViewModel.getConvoyId().value.toString())
                .build()

            val result = convoyApi.update(body)
            result?.run {
                Log.d("end: ", this.body().toString())
                this.body()?.run {
                    if (this.status == "SUCCESS") {
                        withContext(Dispatchers.Main) {
                            convoySessionViewModel.setConvoyId("")
                            (findViewById<ImageButton>(R.id.startConvoyButton)).visibility = View.VISIBLE
                            (findViewById<ImageButton>(R.id.stopConvoyButton)).visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    suspend fun queryConvoy() {
        withContext(Dispatchers.IO) {
            val body: RequestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("action", "QUERY")
                .addFormDataPart("username", this@MapsActivity.intent.getStringExtra("username"))
                .addFormDataPart("session_key", this@MapsActivity.intent.getStringExtra("session_key"))
                .build()

            val result = convoyApi.update(body)
            result?.run {
                Log.d("create: ", this.body().toString())
                this.body()?.run {
                    if (this.status == "SUCCESS") {
                        val convoy_id = this.convoy_id
                        withContext(Dispatchers.Main) {
                            convoySessionViewModel.setConvoyId(convoy_id)
                        }
                    }
                }
            }
        }
    }
}