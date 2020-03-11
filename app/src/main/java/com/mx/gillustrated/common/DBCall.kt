package com.mx.gillustrated.common

/**
 * Created by maoxin on 2017/2/21.
 */

abstract class DBCall<T> {
    abstract fun enqueue(): T
}
