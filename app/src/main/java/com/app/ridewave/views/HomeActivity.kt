package com.app.ridewave.views

import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.app.ridewave.R
import com.app.ridewave.databinding.ActivityHomeBinding
import com.app.ridewave.viewmodels.RiderViewModel
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import java.io.IOException


class HomeActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var binding: ActivityHomeBinding
    private lateinit var map: GoogleMap
    val context: Context = this
    lateinit var pickupLatLng: LatLng
    lateinit var dropOffLatLng: LatLng
    lateinit var currentPolyline: Polyline
    lateinit var viewModel: RiderViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)


        viewModel = ViewModelProvider(this).get(RiderViewModel::class.java)
        binding.profileCard.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // starting point
        setPageState(0)



        binding.pickUp.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                var addressList: ArrayList<Address> = ArrayList()

                val location = binding.pickUp.text.toString()

                val geoCoder = Geocoder(context)

                try {
                    addressList = geoCoder.getFromLocationName(location, 1) as ArrayList<Address>
                } catch (e: IOException) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    println("ErrorMessage: $e.message")
                }

                println("ArrayContents: $addressList.joinToString()")

                val address = addressList[0]
                pickupLatLng = LatLng(address.latitude, address.longitude)
                map.addMarker(MarkerOptions().position(pickupLatLng).title("Pickup"))
                map.moveCamera(CameraUpdateFactory.newLatLng(pickupLatLng))

                return false
            }

        })

        binding.dropOff.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                var addressList: ArrayList<Address> = ArrayList()

                val location = binding.dropOff.text.toString()

                val geoCoder = Geocoder(context)

                try {
                    addressList = geoCoder.getFromLocationName(location, 1) as ArrayList<Address>
                } catch (e: IOException) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    println("ErrorMessage: $e.message")
                }

                println("ArrayContents: $addressList.joinToString()")

                val address = addressList[0]
                dropOffLatLng = LatLng(address.latitude, address.longitude)
                map.addMarker(MarkerOptions().position(dropOffLatLng).title("Drop off"))
                map.moveCamera(CameraUpdateFactory.newLatLng(dropOffLatLng))

                // Draw the route
//                DrawRoute().execute(getDirectionsUrl(pickupLatLng, dropOffLatLng))


                return false
            }
        })


        binding.requestRide.setOnClickListener{
            setPageState(1)
            viewModel.searchForDrivers().observe(this){

                if (it != null) {
                    setPageState(2)
                    binding.description.text = it.carDescription
                    Glide.with(context).load(it.carPhoto).into(binding.carImage)
                    binding.selectDriver.visibility = View.VISIBLE

                } else {
                    Toast.makeText(this, "No drivers found", Toast.LENGTH_SHORT).show()
                    setPageState(0)
                }

            }

        }


        binding.cancel.setOnClickListener{
            setPageState(0)
        }


    }

    override fun onMapReady(googleMap: GoogleMap) {

        map = googleMap

    }


    /**
     * this will hide / show views depending on page state
     * @state 0 -> searching
     * @state 1 -> looking for drivers
     * @state 2 -> select a driver
     * @state 3 -> arrived to destination
     */
    fun setPageState(state: Int) {
        when (state) {

            0 -> {
                binding.addressSearchLayout.visibility = View.VISIBLE
                binding.driverSearchLayout.visibility = View.GONE
            }

            1 -> {
                binding.addressSearchLayout.visibility = View.GONE
                binding.driverSearchLayout.visibility = View.VISIBLE
                binding.progressLayout.visibility = View.VISIBLE
                binding.selectDriverLayout.visibility = View.GONE
                binding.arrivedLayout.visibility = View.GONE
            }

            2 -> {

                binding.addressSearchLayout.visibility = View.GONE
                binding.driverSearchLayout.visibility = View.VISIBLE
                binding.progressLayout.visibility = View.GONE
                binding.selectDriverLayout.visibility = View.VISIBLE
                binding.arrivedLayout.visibility = View.GONE
            }

            3 -> {

                binding.addressSearchLayout.visibility = View.GONE
                binding.driverSearchLayout.visibility = View.VISIBLE
                binding.progressLayout.visibility = View.GONE
                binding.selectDriverLayout.visibility = View.GONE
                binding.arrivedLayout.visibility = View.VISIBLE
            }
        }
    }



//    fun drawRoute(pickupLatLng: LatLng, dropOffLatLng: LatLng)
//    {
//
//        //Define list to get all latlng for the route
//        val path: MutableList<LatLng> = ArrayList()
//
//
//        //Execute Directions API request
//        val context: GeoApiContext = Builder().apiKey("YOUR_API_KEY").build()
//        val req: DirectionsApiRequest =
//            DirectionsApi.getDirections(context, "41.385064,2.173403", "40.416775,-3.70379")
//        try {
//            val res: DirectionsResult = req.await()
//
//            //Loop through legs and steps to get encoded polylines of each step
//            if (res.routes != null && res.routes.length > 0) {
//                val route: DirectionsRoute = res.routes.get(0)
//
//                if (route.legs != null) {
//                    for (i in 0 until route.legs.length) {
//                        val leg: DirectionsLeg = route.legs.get(i)
//                        if (leg.steps != null) {
//                            for (j in 0 until leg.steps.length) {
//                                val step: DirectionsStep = leg.steps.get(j)
//                                if (step.steps != null && step.steps.length > 0) {
//                                    for (k in 0 until step.steps.length) {
//                                        val step1: DirectionsStep = step.steps.get(k)
//                                        val points1: EncodedPolyline = step1.polyline
//                                        if (points1 != null) {
//                                            //Decode polyline and add points to list of route coordinates
//                                            val coords1: List<com.google.maps.model.LatLng> =
//                                                points1.decodePath()
//                                            for (coord1 in coords1) {
//                                                path.add(LatLng(coord1.lat, coord1.lng))
//                                            }
//                                        }
//                                    }
//                                } else {
//                                    val points: EncodedPolyline = step.polyline
//                                    if (points != null) {
//                                        //Decode polyline and add points to list of route coordinates
//                                        val coords: List<com.google.maps.model.LatLng> =
//                                            points.decodePath()
//                                        for (coord in coords) {
//                                            path.add(LatLng(coord.lat, coord.lng))
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        } catch (ex: Exception) {
//            Log.e(TAG, ex.localizedMessage)
//        }
//
//
//        //Draw the polyline
//        if (path.size > 0) {
//            val opts = PolylineOptions().addAll(path).color(Color.BLUE).width(5f)
//            mMap.addPolyline(opts)
//        }
//
//        mMap.getUiSettings().setZoomControlsEnabled(true)
//
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(zaragoza, 6f))
//    }




}