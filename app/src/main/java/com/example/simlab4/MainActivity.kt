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
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val chart = findViewById<LineChart>(R.id.chart1)
        val entries = ArrayList<Entry>()
        var numberOfNextEntry=1f
        var updateThreadCoroutine:Job?=null
        textView.text=MoneyBag.rub.toString()+"\u20BD  "+MoneyBag.dollar.toString()+"$"
        buttonGenerate.setOnClickListener {
            ExchangeRateProvider.volat=("0"+volText.text.toString()).toDouble()
            ExchangeRateProvider.cut=("0"+cutText.text.toString()).toDouble()
            if (updateThreadCoroutine!=null)
            {
                updateThreadCoroutine?.cancel()
                updateThreadCoroutine=null
            }
            var laststep=System.currentTimeMillis()
            var remainingEntriesToAdd=editTextSAmount.text.toString().toInt()
            entries.clear()
            while (remainingEntriesToAdd>0)
            {
                entries.add(Entry(numberOfNextEntry,ExchangeRateProvider.currentCost.toFloat()))
                ExchangeRateProvider.updateRate()
                remainingEntriesToAdd--
                numberOfNextEntry++
            }
            updateThreadCoroutine=GlobalScope.launch {
                withContext(Dispatchers.Default)
                {
                    while (true) {
                        if ((System.currentTimeMillis()-laststep)>1000) {
                            var mtx=Mutex()
                            mtx.lock(this)
                            laststep=System.currentTimeMillis()
                            ExchangeRateProvider.updateRate()
                            entries.add(Entry(numberOfNextEntry,ExchangeRateProvider.currentCost.toFloat()))
                            if (entries.size>40)
                            {
                                entries.removeAt(0)
                            }
                            numberOfNextEntry++
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
                            Thread.sleep(1000)
                            mtx.unlock()
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
