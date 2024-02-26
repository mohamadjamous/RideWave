package com.app.ridewave.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import com.app.ridewave.databinding.VehicleInfoItemBinding
import com.app.ridewave.models.DriverModel
import com.bumptech.glide.Glide

class DriversAdapter : BaseAdapter {

    private lateinit var list: List<DriverModel>
    private lateinit var context: Context

    constructor(list: List<DriverModel>, context: Context) : super() {
        this.list = list
        this.context = context
    }


    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(position: Int): Any {
        return  list.get(position)
    }

    override fun getItemId(position: Int): Long {
      return  0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val binding: VehicleInfoItemBinding =
            VehicleInfoItemBinding.inflate(LayoutInflater.from(parent?.context), parent, false)

        binding.description.text = list.get(position).carDescription
        Glide.with(context).load(list.get(position).carPhoto).into(binding.carImage)

        binding.selectDriver.setOnClickListener{
            Toast.makeText(context,"Driver Selected",Toast.LENGTH_SHORT).show()
        }


        return binding.root
    }
}