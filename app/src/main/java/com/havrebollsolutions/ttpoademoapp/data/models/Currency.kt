package com.havrebollsolutions.ttpoademoapp.data.models

enum class Currency(
    val isoCode: String,
    val symbol: String,
) {
    SEK("SEK", "kr"), // Swedish kronor, Default
    EUR("EUR", "€"), // Euro
    USD("USD", "$"), // US Dollar
    GBP("GBP", "£"), // British Pound Sterling
    AUD("AUD", "$"), // Australian Dollar
    NOK("NOK", "Kr"), // Norwegian Kronor
    DKK("DKK", "kr"); // Danish Krone
    // Add all supported currencies here

    // Optional: Helper function to get the enum from a stored code
    companion object {
        fun fromIsoCode(code: String): Currency? = entries.find {
            it.isoCode == code
        }
    }
}