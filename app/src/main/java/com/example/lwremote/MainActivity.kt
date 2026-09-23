package com.example.lwremote

import android.content.Context
import android.hardware.ConsumerIrManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.min

class MainActivity : AppCompatActivity() {

    private lateinit var ir: ConsumerIrManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ir = getSystemService(Context.CONSUMER_IR_SERVICE) as ConsumerIrManager

        val startButton = findViewById<Button>(R.id.btnStart)
        val killButton = findViewById<Button>(R.id.btnKill)
        val status = findViewById<TextView>(R.id.txtStatus)

        if (!ir.hasIrEmitter()) {
            status.text = "ИК-передатчик: НЕ НАЙДЕН"
            startButton.isEnabled = false
            killButton.isEnabled = false
            return
        }

        status.text = "ИК-передатчик: НАЙДЕН"

        startButton.setOnClickListener {
            try {
                sendStart()
                Toast.makeText(
                    this,
                    "СТАРТ: сигнал отправлен",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    this,
                    "Ошибка ИК: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        killButton.setOnClickListener {
            try {
                sendKill()
                Toast.makeText(
                    this,
                    "УБИТИЕ: сигнал отправлен",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    this,
                    "Ошибка ИК: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // START GAME: 83 05 E8, 37 кГц, ровно одна передача.
    private fun sendStart() {
        val frequency = 37_000
        val pattern = ArrayList<Int>()

        addCarrier(pattern, 2400, frequency)
        pattern.add(600)

        sendByte(pattern, 0x83, frequency)
        sendByte(pattern, 0x05, frequency)
        sendByte(pattern, 0xE8, frequency)

        ir.transmit(frequency, pattern.toIntArray())
    }

    // KILL / 100 HP:
    // PLAYER_ID=23, TEAM_ID=2, DAMAGE_ID=15.
    // 50 кГц, ровно одна передача.
    private fun sendKill() {
        val playerId = 23
        val teamId = 2
        val damageId = 15

        val playerData =
            (playerId shl 8) or
            (teamId shl 6) or
            (damageId shl 2)

        val frequency = 50_000
        val pattern = ArrayList<Int>()

        addCarrier(pattern, 2400, frequency)
        pattern.add(600)

        var ones = 0

        for (i in 15 downTo 0) {
            val bit = (playerData shr i) and 1

            if (bit == 1) {
                ones++
                addCarrier(pattern, 1200, frequency)
            } else {
                addCarrier(pattern, 600, frequency)
            }

            pattern.add(600)
        }

        if (ones % 2 != 0) {
            addCarrier(pattern, 1200, frequency)
        } else {
            addCarrier(pattern, 600, frequency)
        }

        pattern.add(600)

        ir.transmit(frequency, pattern.toIntArray())
    }

    // Формирует ON/OFF последовательность несущей.
    private fun addCarrier(
        pattern: ArrayList<Int>,
        durationUs: Int,
        frequency: Int
    ) {
        val periodUs = 1_000_000.0 / frequency
        val halfPeriod = (periodUs / 2.0).toInt()

        var remaining = durationUs

        while (remaining > 0) {
            val onTime = min(halfPeriod, remaining)
            pattern.add(onTime)
            remaining -= onTime

            if (remaining <= 0) {
                break
            }

            val offTime = min(halfPeriod, remaining)
            pattern.add(offTime)
            remaining -= offTime
        }
    }

    // Передача байта MSB first.
    private fun sendByte(
        pattern: ArrayList<Int>,
        value: Int,
        frequency: Int
    ) {
        for (i in 7 downTo 0) {
            val bit = (value shr i) and 1

            if (bit == 1) {
                addCarrier(pattern, 1200, frequency)
            } else {
                addCarrier(pattern, 600, frequency)
            }

            pattern.add(600)
        }
    }
}
