package com.ys.cpm.Ext

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Created by Ys on 2017/6/26.
 * DelegatesExt
 */
object DelegatesExt {

    fun <T : Any> notNullSingleValue(): ReadWriteProperty<Any?, T> = NotNullSingleValueVar()

}

class NotNullSingleValueVar<T> : ReadWriteProperty<Any?, T> {

    private var value: T? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): T =
            value ?: throw IllegalStateException("${javaClass.simpleName} not initialized")

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = if (this.value == null) value
        else throw IllegalStateException("${javaClass.simpleName} already initialized")
    }

}