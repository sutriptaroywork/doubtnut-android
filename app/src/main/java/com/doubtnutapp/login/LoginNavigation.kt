package com.doubtnutapp.login

/**
 * Created by Anand Gaurav on 2019-08-19.
 */
sealed class LoginNavigation {
    object OtpScreen : LoginNavigation()
    object MainScreen : LoginNavigation()
    object OnBoardingScreen : LoginNavigation()
    object LoginScreen : LoginNavigation()
}