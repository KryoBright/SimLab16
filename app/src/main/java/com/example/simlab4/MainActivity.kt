package com.example.simlab4

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val chart = findViewById<LineChart>(R.id.chart1)
        val entries = ArrayList<Entry>()
        var num=1f
        var stop=false
        textView.text=MoneyBag.rub.toString()+"\u20BD  "+MoneyBag.dollar.toString()+"$"
        buttonGenerate.setOnClickListener {
            stop=true
            var laststep=System.currentTimeMillis()
            var rem=editTextSAmount.text.toString().toInt()
            entries.clear()
            while (rem>0)
            {
                entries.add(Entry(num,ExchangeRateProvider.currentCost.toFloat()))
                ExchangeRateProvider.updateRate()
                rem--
                num++
            }
            stop=false
            GlobalScope.launch {
                withContext(Dispatchers.Default)
                {
                    while (!stop) {
                        if ((System.currentTimeMillis()-laststep)>1000) {
                            stop=true
                            laststep=System.currentTimeMillis()
                            ExchangeRateProvider.updateRate()
                            entries.add(Entry(num,ExchangeRateProvider.currentCost.toFloat()))
                            if (entries.size>40)
                            {
                                entries.removeAt(0)
                            }
                            num++
                            val dataSet = LineDataSet(entries, "Курс"); // add entries to dataset
                            dataSet.color = Color.GREEN;
                            dataSet.valueTextColor = Color.BLACK
                            dataSet.circleHoleColor= Color.DKGRAY
                            val lineData = LineData(dataSet)
                            GlobalScope.launch {
                                withContext(Dispatchers.Main)
                                {
                                    chart.data = lineData
                                    chart.invalidate()
                                }
                            }
                            stop=false
                        }
                    }
                }
            }

        }

        buttonBuy.setOnClickListener {
            val am=editTextTradeAmount.text.toString().toFloat()
            if (am*ExchangeRateProvider.currentCost>MoneyBag.rub)
            {
                Toast.makeText(this,"Not enough rubles",Toast.LENGTH_LONG).show()
            }
            else {
                MoneyBag.dollar += am
                MoneyBag.rub -= ExchangeRateProvider.buy(am)
                textView.text =
                    MoneyBag.rub.toString() + "\u20BD  " + MoneyBag.dollar.toString() + "$"
            }
        }

        buttonSell.setOnClickListener {
            val am=editTextTradeAmount.text.toString().toFloat()
            if (am>MoneyBag.dollar)
            {
                Toast.makeText(this,"Not enough dollars",Toast.LENGTH_LONG).show()
            }
            else {
                MoneyBag.dollar -= am
                MoneyBag.rub += ExchangeRateProvider.sell(am)
                textView.text =
                    MoneyBag.rub.toString() + "\u20BD  " + MoneyBag.dollar.toString() + "$"
            }
        }
    }
}
