package com.zenith.expensetracker.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

object CategoryIconHelper {

    fun getIconByName(iconName: String): ImageVector {
        return when (iconName.lowercase()) {
            "restaurant", "food", "dining" -> Icons.Default.Restaurant
            "shopping_cart", "cart", "groceries" -> Icons.Default.ShoppingCart
            "directions_car", "transport", "car", "travel" -> Icons.Default.DirectionsCar
            "shopping_bag", "shopping" -> Icons.Default.ShoppingBag
            "home", "housing", "rent" -> Icons.Default.Home
            "receipt_long", "bills", "utilities", "receipt" -> Icons.AutoMirrored.Filled.ReceiptLong
            "movie", "entertainment", "cinema" -> Icons.Default.Movie
            "medical_services", "health", "medical", "medicine" -> Icons.Default.MedicalServices
            "school", "education", "books" -> Icons.Default.School
            "spa", "personal_care", "beauty" -> Icons.Default.Spa
            "fitness_center", "fitness", "gym", "workout" -> Icons.Default.FitnessCenter
            "flight", "flight_takeoff", "vacation" -> Icons.Default.Flight
            "pets", "pet", "dog", "cat" -> Icons.Default.Pets
            "sports_esports", "gaming", "games" -> Icons.Default.SportsEsports
            "local_cafe", "cafe", "coffee" -> Icons.Default.LocalCafe
            "local_gas_station", "gas", "fuel" -> Icons.Default.LocalGasStation
            "phone_android", "phone", "mobile", "tech" -> Icons.Default.PhoneAndroid
            "wifi", "internet", "wifi_channel" -> Icons.Default.Wifi
            "child_care", "family", "kids", "baby" -> Icons.Default.ChildCare
            "savings", "savings_bank" -> Icons.Default.Savings
            "payments", "salary", "paycheck" -> Icons.Default.Payments
            "laptop_mac", "freelance", "work", "tech_work" -> Icons.Default.LaptopMac
            "trending_up", "investment", "investments", "stocks" -> Icons.AutoMirrored.Filled.TrendingUp
            "card_giftcard", "gift", "bonus" -> Icons.Default.CardGiftcard
            "account_balance_wallet", "wallet", "other_income" -> Icons.Default.AccountBalanceWallet
            "attach_money", "cash", "money" -> Icons.Default.AttachMoney
            "build", "maintenance", "repairs" -> Icons.Default.Build
            "local_hospital", "hospital" -> Icons.Default.LocalHospital
            else -> Icons.Default.Category
        }
    }

    val availableIcons = listOf(
        "restaurant" to "Food & Dining",
        "shopping_cart" to "Groceries",
        "shopping_bag" to "Shopping",
        "directions_car" to "Transportation",
        "home" to "Home & Rent",
        "receipt_long" to "Bills & Utilities",
        "movie" to "Entertainment",
        "medical_services" to "Health & Medical",
        "school" to "Education",
        "spa" to "Personal Care",
        "fitness_center" to "Fitness & Gym",
        "flight" to "Travel & Trips",
        "pets" to "Pets & Animals",
        "sports_esports" to "Gaming & Hobbies",
        "local_cafe" to "Cafe & Coffee",
        "local_gas_station" to "Fuel & Gas",
        "phone_android" to "Mobile & Tech",
        "wifi" to "Internet & TV",
        "child_care" to "Family & Kids",
        "savings" to "Savings & Deposits",
        "payments" to "Salary & Wages",
        "laptop_mac" to "Freelance & Projects",
        "trending_up" to "Investments & Stocks",
        "card_giftcard" to "Gifts & Rewards",
        "account_balance_wallet" to "Wallet & Other",
        "attach_money" to "Bonus & Income",
        "build" to "Maintenance & Fixes",
        "local_hospital" to "Hospital & Clinic"
    )

    val availableColors = listOf(
        "#10B981", // Emerald
        "#2563EB", // Sapphire Blue
        "#8B5CF6", // Royal Violet
        "#F97316", // Sunset Orange
        "#E11D48", // Ruby
        "#06B6D4", // Cyber Teal
        "#EC4899", // Rose Pink
        "#FF7043", // Coral
        "#66BB6A", // Light Green
        "#42A5F5", // Blue
        "#AB47BC", // Purple
        "#FFA726", // Orange
        "#EF5350", // Red
        "#26A69A", // Teal
        "#5C6BC0", // Indigo
        "#8D6E63"  // Brown
    )
}
