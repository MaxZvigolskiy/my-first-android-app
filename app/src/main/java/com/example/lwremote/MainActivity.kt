package com.example.lwremote

import android.content.Context
import android.hardware.ConsumerIrManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

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

    /*
     * СТАРТ ИГРЫ
     *
     * Частота: 37 кГц
     * Команда: 83 05 E8
     *
     * Формат:
     * 2400 мкс MARK
     * 600 мкс SPACE
     *
     * Бит 0:
     * 600 мкс MARK
     * 600 мкс SPACE
     *
     * Бит 1:
     * 1200 мкс MARK
     * 600 мкс SPACE
     */
    private fun sendStart() {

        val frequency = 37_000

        val pattern = ArrayList<Int>()

        // Стартовый импульс
        pattern.add(2400)
        pattern.add(600)

        // 0x83
        sendByte(pattern, 0x83)

        // 0x05
        sendByte(pattern, 0x05)

        // 0xE8
        sendByte(pattern, 0xE8)

        ir.transmit(
            frequency,
            pattern.toIntArray()
        )
    }

    /*
     * УБИТИЕ / 100 HP
     *
     * PLAYER_ID = 23
     * TEAM_ID   = 2
     * DAMAGE_ID = 15
     *
     * Частота: 50 кГц
     */
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

        // Стартовый импульс
        pattern.add(2400)
        pattern.add(600)

        var ones = 0

        // Передаём 16 бит, начиная со старшего
        for (i in 15 downTo 0) {

            val bit = (playerData shr i) and 1

            if (bit == 1) {
                ones++

                // Логическая 1
                pattern.add(1200)
                pattern.add(600)

            } else {

                // Логический 0
                pattern.add(600)
                pattern.add(600)
            }
        }

        // Бит чётности
        if (ones % 2 != 0) {
            pattern.add(1200)
        } else {
            pattern.add(600)
        }

        pattern.add(600)

        ir.transmit(
            frequency,
            pattern.toIntArray()
        )
    }

    /*
     * Передача одного байта.
     *
     * Старший бит идёт первым.
     */
    private fun sendByte(
        pattern: ArrayList<Int>,
        value: Int
    ) {

        for (i in 7 downTo 0) {

            val bit = (value shr i) and 1

            if (bit == 1) {

                // 1 = 1200 мкс MARK + 600 мкс SPACE
                pattern.add(1200)
                pattern.add(600)

            } else {

                // 0 = 600 мкс MARK + 600 мкс SPACE
                pattern.add(600)
                pattern.add(600)
            }
        }
    }
}
