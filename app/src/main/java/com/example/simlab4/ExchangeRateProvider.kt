package com.example.simlab4

import java.lang.Math.exp
import java.util.*
import kotlin.math.atan

class ExchangeRateProvider {
    companion object{
        private val rnd=Random()
        var defCost=130
        var currentCost= rnd.nextGaussian()* defCost
        var t=0
        var last_W=0.0
        var volat=1.0
        var cut=2.0

        fun updateRate()
        {
            currentCost = defCost* kotlin.math.exp((volat - cut * cut / 2.0) * t + volat * last_W)
            last_W += rnd.nextGaussian()
            t++
        }

        fun sell(amount:Float):Float
        {
            return currentCost.toFloat()*amount
        }

        fun buy(amount:Float):Float
        {
            return currentCost.toFloat()*amount
        }
    }
}