package com.yourdomain.walletmateeu.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

object IconHelper {
    val allIconNames: List<String> = listOf(
        "Home", "AccountBalanceWallet", "ShoppingCart", "Receipt", "LocalPizza", "Fastfood",
        "Restaurant", "LocalCafe", "LocalBar", "Flight", "DirectionsCar", "Train", "Subway",
        "LocalHospital", "Movie", "FitnessCenter", "School", "Pets", "CardGiftcard",
        "Checkroom", "Computer", "PhoneAndroid", "Savings", "CreditCard", "MonetizationOn",
        "ShoppingBag", "GasStation", "LocalPharmacy", "Hotel", "FamilyRestroom", "SportsEsports",
        "Book", "MusicNote", "Build", "Work", "Public", "Place", "Phone", "Wifi", "ShoppingCartCheckout",
        "MedicalServices", "Park", "TravelExplore", "Payment", "AccountBalance", "TrendingUp"
    )

    fun getIcon(iconName: String): ImageVector {
        return when (iconName) {
            "Home" -> Icons.Filled.Home
            "AccountBalanceWallet" -> Icons.Filled.AccountBalanceWallet
            "ShoppingCart" -> Icons.Filled.ShoppingCart
            "Receipt" -> Icons.Filled.Receipt
            "LocalPizza" -> Icons.Filled.LocalPizza
            "Fastfood" -> Icons.Filled.Fastfood
            "Restaurant" -> Icons.Filled.Restaurant
            "LocalCafe" -> Icons.Filled.LocalCafe
            "LocalBar" -> Icons.Filled.LocalBar
            "Flight" -> Icons.Filled.Flight
            "DirectionsCar" -> Icons.Filled.DirectionsCar
            "Train" -> Icons.Filled.Train
            "Subway" -> Icons.Filled.Subway
            "LocalHospital" -> Icons.Filled.LocalHospital
            "Movie" -> Icons.Filled.Movie
            "FitnessCenter" -> Icons.Filled.FitnessCenter
            "School" -> Icons.Filled.School
            "Pets" -> Icons.Filled.Pets
            "CardGiftcard" -> Icons.Filled.CardGiftcard
            "Checkroom" -> Icons.Filled.Checkroom
            "Computer" -> Icons.Filled.Computer
            "PhoneAndroid" -> Icons.Filled.PhoneAndroid
            "Savings" -> Icons.Filled.Savings
            "CreditCard" -> Icons.Filled.CreditCard
            "MonetizationOn" -> Icons.Filled.MonetizationOn
            "ShoppingBag" -> Icons.Filled.ShoppingBag
            "GasStation" -> Icons.Filled.EvStation // EvStation is a good proxy
            "LocalPharmacy" -> Icons.Filled.LocalPharmacy
            "Hotel" -> Icons.Filled.Hotel
            "FamilyRestroom" -> Icons.Filled.FamilyRestroom
            "SportsEsports" -> Icons.Filled.SportsEsports
            "Book" -> Icons.Filled.Book
            "MusicNote" -> Icons.Filled.MusicNote
            "Build" -> Icons.Filled.Build
            "Work" -> Icons.Filled.Work
            "Public" -> Icons.Filled.Public
            "Place" -> Icons.Filled.Place
            "Phone" -> Icons.Filled.Phone
            "Wifi" -> Icons.Filled.Wifi
            "ShoppingCartCheckout" -> Icons.Filled.ShoppingCartCheckout
            "MedicalServices" -> Icons.Filled.MedicalServices
            "Park" -> Icons.Filled.Park
            "TravelExplore" -> Icons.Filled.TravelExplore
            "Payment" -> Icons.Filled.Payment
            "AccountBalance" -> Icons.Filled.AccountBalance
            "TrendingUp" -> Icons.Filled.TrendingUp
            else -> Icons.Filled.Category
        }
    }
}