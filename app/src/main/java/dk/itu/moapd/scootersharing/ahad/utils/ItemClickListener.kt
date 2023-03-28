package dk.itu.moapd.scootersharing.ahad.utils

import dk.itu.moapd.scootersharing.ahad.model.Scooter

interface ItemClickListener {
    fun onItemCLickListener(scooter: Scooter)
}