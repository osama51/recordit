package com.toddler.recordit

interface Destinations {
    val route: String
}

object Dashboard : Destinations {
    override val route: String = "dashboard"
}

object Record : Destinations {
    override val route: String = "record"
}

object LogIn : Destinations {
    override val route: String = "login"
}