package com.example.simlab4

import java.util.*
import kotlin.math.atan

class ExchangeRateProvider {
    companion object{
        private val rnd=Random()
        var defCost=130
        var currentCost= rnd.nextDouble()* defCost
        private var sold=0f

        fun updateRate()
        {
            currentCost = currentCost*(1  - atan(sold + 0.0) * 0.002 / Math.PI)+ defCost*(rnd.nextDouble() * 0.02-0.01)
            sold /= 2
        }

        fun sell(amount:Float):Float
        {
            sold+=amount
            return currentCost.toFloat()*amount
        }

        fun buy(amount:Float):Float
        {
            sold-=amount
            return currentCost.toFloat()*amount
        }
    }
}