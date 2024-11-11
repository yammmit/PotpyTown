package com.example.potpytown.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.potpytown.R
import com.example.potpytown.data.ModelWeather
import java.util.Calendar

class WeatherAdapter(var items: Array<ModelWeather>) : RecyclerView.Adapter<WeatherAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_weather, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.setItem(item)
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun setItem(item: ModelWeather) {
            val imgWeather = itemView.findViewById<ImageView>(R.id.imgWeather) // 날씨 아이콘
            val tvTemp = itemView.findViewById<TextView>(R.id.tvTemp) // 기온 텍스트

            val currentTime = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val isDaytime = currentTime in 6..18

            imgWeather.setImageResource(getWeatherIcon(item.rainType, item.sky, isDaytime))
            tvTemp.text = "${item.temp}°"
        }
    }

    fun getWeatherIcon(rainType: String, sky: String, isDaytime: Boolean): Int {
        return when {
            rainType == "0" -> { // 강수 형태가 없음
                when (sky) {
                    "1" -> if (isDaytime) R.raw.clear_day else R.raw.clear_night // 맑음
                    "3" -> if (isDaytime) R.raw.cloudy_day else R.raw.cloudy_night // 구름 많음
                    "4" -> R.raw.foggy // 안개
                    else -> R.raw.weather_unknown
                }
            }
            rainType == "1" || rainType == "4" -> { // 비 또는 소나기
                if (isDaytime) R.raw.rain_day else R.raw.rain_night
            }
            rainType == "3" -> R.raw.snow // 눈
            else -> R.raw.weather_unknown // 알 수 없는 상태
        }
    }
}
