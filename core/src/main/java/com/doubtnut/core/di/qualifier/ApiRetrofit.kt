package com.doubtnut.core.di.qualifier

import javax.inject.Qualifier

/**
 * Created by Anand Gaurav on 30/11/20.
 */
@Qualifier
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ApiRetrofit