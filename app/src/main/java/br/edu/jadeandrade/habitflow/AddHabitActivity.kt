package br.edu.jadeandrade.habitflow

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class AddHabitActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_habit)

        val etHabitName = findViewById<EditText>(R.id.etHabitName)
        val rgCategory = findViewById<RadioGroup>(R.id.rgCategory)
        val rgGoal = findViewById<RadioGroup>(R.id.rgGoal)
        val btnCreateHabit = findViewById<Button>(R.id.btnCreateHabit)
        val rgDuration = findViewById<RadioGroup>(R.id.rgDuration)

        val btnBack = findViewById<Button>(R.id.btnBack)

        btnBack.setOnClickListener {
            finish()
        }

        btnCreateHabit.setOnClickListener {
            val name = etHabitName.text.toString().trim()

            if (name.isEmpty()) {
                etHabitName.error = "Digite o nome do hábito"
                return@setOnClickListener
            }

            val categoryButton =
                findViewById<RadioButton>(rgCategory.checkedRadioButtonId)

            val goalButton =
                findViewById<RadioButton>(rgGoal.checkedRadioButtonId)

            val durationDays = when (rgDuration.checkedRadioButtonId) {
                R.id.rbDuration3Months -> 90
                R.id.rbDuration6Months -> 180
                R.id.rbDuration1Year -> 365
                else -> 30
            }

            val totalDays = when (rgGoal.checkedRadioButtonId) {
                R.id.rbWeekly -> kotlin.math.ceil(durationDays * 5.0 / 7.0).toInt()
                R.id.rbTwoWeekly -> kotlin.math.ceil(durationDays * 2.0 / 7.0).toInt()
                R.id.rbThreeWeekly -> kotlin.math.ceil(durationDays * 3.0 / 7.0).toInt()
                else -> durationDays
            }

            val resultIntent = Intent().apply {
                putExtra("habit_name", name)
                putExtra("habit_category", categoryButton.text.toString())
                putExtra("habit_goal", goalButton.text.toString())
                putExtra("habit_total_days", totalDays)
                putExtra("habit_duration_days", durationDays)
            }

            setResult(RESULT_OK, resultIntent)

            Toast.makeText(this, "Hábito criado com sucesso!", Toast.LENGTH_SHORT).show()

            finish()
        }
    }
}