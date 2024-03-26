package com.app.ridewave.views

import android.app.AlertDialog
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
import com.app.ridewave.adapters.DriversAdapter
import com.app.ridewave.databinding.ActivityHomeBinding
import com.app.ridewave.models.DriverModel
import com.app.ridewave.models.RideModel
import com.app.ridewave.utils.CustomProgressDialog
import com.app.ridewave.utils.Helper
import com.app.ridewave.utils.SelectDriverInterface
import com.app.ridewave.viewmodels.DriverViewModel
import com.app.ridewave.viewmodels.RideViewModel
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


class RiderDashboard : AppCompatActivity(), OnMapReadyCallback, SelectDriverInterface {

    private lateinit var dialog: AlertDialog
    lateinit var binding: ActivityHomeBinding
    private lateinit var map: GoogleMap
    val context: Context = this
    var pickupLatLng: LatLng = LatLng(0.0, 0.0)
    var dropOffLatLng: LatLng = LatLng(0.0, 0.0)
    var pickUpAddress = ""
    var dropOffAddress = ""
    lateinit var currentPolyline: Polyline
    lateinit var riderViewModel: RiderViewModel
    lateinit var driverViewModel: DriverViewModel
    lateinit var rideViewModel: RideViewModel
    lateinit var currentRide: RideModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)


        riderViewModel = ViewModelProvider(this).get(RiderViewModel::class.java)
        rideViewModel = ViewModelProvider(this).get(RideViewModel::class.java)
        driverViewModel = ViewModelProvider(this).get(DriverViewModel::class.java)
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


        binding.requestRide.setOnClickListener {

            if (pickupLatLng.latitude == 0.0) {
                Toast.makeText(this, "Please enter pickup location", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (dropOffLatLng.latitude == 0.0) {
                Toast.makeText(this, "Please enter drop off location", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            pickUpAddress = binding.pickUp.text.toString()
            dropOffAddress = binding.dropOff.text.toString()

            setPageState(1)
            binding.destination.text = binding.dropOff.text
            riderViewModel.searchForDrivers().observe(this) {

                if (it != null) {
                    setPageState(2)
                    println("ArraySize: " + it.size)
                    binding.listView.adapter = DriversAdapter(it, context, this)
                } else {
                    Toast.makeText(this, "No drivers found", Toast.LENGTH_SHORT).show()
                    setPageState(0)
                }

            }


        }


        binding.cancel.setOnClickListener {
            updateRide(0, -1)
        }



        binding.finishRide.setOnClickListener {
            updateRide(1, 2)
        }

        binding.returnDashboard.setOnClickListener {
            setPageState(0)
        }

        getCurrentUser(Helper.getUserId(context), Helper.getUserType(context))

        binding.selectRiderButton.setOnClickListener {
            updateRide(0, 2)
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
     * @state 3 -> on going ride
     * @state 4 -> arrived to destination
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
                binding.vehicleInfo.visibility = View.GONE
                binding.arrivedLayout.visibility = View.GONE
                binding.listView.visibility = View.GONE
                binding.finishRide.visibility = View.GONE
                binding.selectRiderButton.visibility = View.GONE

            }

            2 -> {

                binding.addressSearchLayout.visibility = View.GONE
                binding.driverSearchLayout.visibility = View.VISIBLE
                binding.progressLayout.visibility = View.GONE
                binding.selectDriverLayout.visibility = View.VISIBLE
                binding.listView.visibility = View.VISIBLE
                binding.vehicleInfo.visibility = View.GONE
                binding.arrivedLayout.visibility = View.GONE
                binding.finishRide.visibility = View.GONE
                binding.selectRiderButton.visibility = View.GONE
            }

            3 -> {
                binding.addressSearchLayout.visibility = View.GONE
                binding.driverSearchLayout.visibility = View.VISIBLE
                binding.progressLayout.visibility = View.GONE
                binding.selectDriverLayout.visibility = View.VISIBLE
                binding.vehicleInfo.visibility = View.VISIBLE
                binding.listView.visibility = View.GONE
                binding.arrivedLayout.visibility = View.GONE
                binding.finishRide.visibility = View.VISIBLE
                binding.selectRiderButton.visibility = View.GONE

                binding.finishRide.text = "Select Ride"

            }

            4 -> {

                binding.addressSearchLayout.visibility = View.GONE
                binding.driverSearchLayout.visibility = View.VISIBLE
                binding.progressLayout.visibility = View.GONE
                binding.selectDriverLayout.visibility = View.VISIBLE
                binding.vehicleInfo.visibility = View.VISIBLE
                binding.arrivedLayout.visibility = View.VISIBLE
                binding.listView.visibility = View.GONE
                binding.finishRide.visibility = View.GONE
                binding.selectRiderButton.visibility = View.GONE
                binding.pickUp.setText("")
                binding.dropOff.setText("")
            }
        }
    }

    override fun selectedDriver(driverModel: DriverModel) {
        initializeDialog(getString(R.string.selecting_driver))
        showDialog(true)

        riderViewModel.getAccountInfo(Helper.getUserId(context)).observe(this)
        {
            if (it.equals(null)) {
                Toast.makeText(context, "Error Selecting Ride", Toast.LENGTH_SHORT).show()
            } else {
                val ride = RideModel(
                    "",
                    pickupLatLng,
                    dropOffLatLng,
                    pickUpAddress,
                    dropOffAddress,
                    it,
                    driverModel, 0
                )
                rideViewModel.createRide(ride)
                currentRide = ride
                setPageState(3)
                binding.description.text = driverModel.carDescription
                Glide.with(context).load(driverModel.carPhoto).into(binding.carImage)
            }

            showDialog(false)

        }


    }


    fun initializeDialog(text: String) {
        dialog = CustomProgressDialog.showCustomDialog(context, text, R.color.white)
    }


    fun showDialog(show: Boolean) {
        if (show) {
            dialog.show()
        } else {
            dialog.dismiss()
        }

    }


    fun getCurrentUser(id: String, userType: String) {

        initializeDialog(getString(R.string.loading))
        showDialog(true)

        //rider
        if (userType == "0") {
            binding.ridersSearchLayout.visibility = View.GONE
            riderViewModel.getAccountInfo(id).observe(this)
            {
                if (it.equals(null)) {
                    Toast.makeText(context, "Error Loading User", Toast.LENGTH_SHORT).show()
                    setPageState(0)
                } else {
                    rideViewModel.getRide(it.id).observe(this)
                    { rideModel ->
                        //no active ride
                        if (rideModel == null) {
                            setPageState(0)
                        }
                        //active ride
                        else {
                            currentRide = rideModel
                            setPageState(3)
                            binding.description.text = rideModel.driver.name + "\n \n" + rideModel.driver.carDescription
                            Glide.with(context).load(rideModel.driver.carPhoto).into(binding.carImage)
                        }
                    }
                }
                showDialog(false)
            }
        } else {

            val intent = Intent(this, DriverDashboard::class.java)
            val bundle = Bundle()
            bundle.putString("id", id)
            intent.putExtras(bundle)
            startActivity(intent)
            finish()


        }


    }


    fun updateRide(type: Int, state: Int) {

        initializeDialog(getString(R.string.loading))
        showDialog(true)

        println("CurrentIdValue: ${currentRide.id}")

        rideViewModel.updateRide(currentRide.id, state).observe(this)
        {
            if (it.equals("success")) {
                if (type == 0) {
                    setPageState(0)
                } else {
                    setPageState(4)

                }

            } else {
                Toast.makeText(context, "Error Finishing Ride", Toast.LENGTH_SHORT).show()
            }
            showDialog(false)
        }

    }



}