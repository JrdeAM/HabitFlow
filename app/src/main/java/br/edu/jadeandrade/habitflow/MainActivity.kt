package br.edu.jadeandrade.habitflow

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.edu.jadeandrade.habitflow.adapter.HabitAdapter
import br.edu.jadeandrade.habitflow.controller.HabitController
import br.edu.jadeandrade.habitflow.model.Habit
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.widget.ProgressBar

class MainActivity : AppCompatActivity() {

    private val habits = mutableListOf<Habit>()

    private lateinit var adapter: HabitAdapter
    private lateinit var controller: HabitController
    private lateinit var tvGreeting: TextView
    private lateinit var tvStreak: TextView

    private lateinit var progressConsistency: ProgressBar

    private lateinit var tvConsistencyDetail: TextView

    private var userId = -1
    private lateinit var userName: String

    private val addHabitLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == RESULT_OK) {
                val name = result.data?.getStringExtra("habit_name")
                    ?: return@registerForActivityResult

                val category = result.data?.getStringExtra("habit_category")
                    ?: return@registerForActivityResult

                val goal = result.data?.getStringExtra("habit_goal")
                    ?: return@registerForActivityResult

                val totalDays = result.data?.getIntExtra(
                    "habit_total_days",
                    30
                ) ?: 30

                val durationDays = result.data?.getIntExtra(
                    "habit_duration_days",
                    30
                ) ?: 30

                controller.createHabit(
                    name = name,
                    category = category,
                    goal = goal,
                    totalDays = totalDays,
                    durationDays = durationDays
                ) {
                    loadHabits()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userId = intent.getIntExtra("user_id", -1)
        userName = intent.getStringExtra("user_name") ?: "Usuário"

        if (userId == -1) {
            Toast.makeText(
                this,
                "Sessão inválida. Faça login novamente.",
                Toast.LENGTH_SHORT
            ).show()

            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        controller = HabitController(this, userId)

        tvGreeting = findViewById(R.id.tvGreeting)
        tvStreak = findViewById(R.id.tvStreak)
        progressConsistency = findViewById(R.id.progressConsistency)
        tvConsistencyDetail = findViewById(R.id.tvConsistencyDetail)

        tvGreeting.text = "Olá, $userName 👋"

        val recyclerView = findViewById<RecyclerView>(R.id.rvHabits)
        val fabAddHabit =
            findViewById<FloatingActionButton>(R.id.fabAddHabit)

        adapter = HabitAdapter(
            habits = habits,
            onCompleteClick = { habit ->
                completeHabit(habit)
            },
            onDeleteClick = { habit ->
                deleteHabit(habit)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        fabAddHabit.setOnClickListener {
            addHabitLauncher.launch(
                Intent(this, AddHabitActivity::class.java)
            )
        }

        loadHabits()
    }

    private fun loadHabits() {
        controller.loadHabits { savedHabits ->
            habits.clear()
            habits.addAll(savedHabits)

            adapter.notifyDataSetChanged()
            updateDashboard()
        }
    }

    private fun completeHabit(habit: Habit) {
        controller.completeHabit(habit) { updatedHabit, message ->

            if (message != null) {
                Toast.makeText(
                    this,
                    message,
                    Toast.LENGTH_SHORT
                ).show()

                return@completeHabit
            }

            if (updatedHabit != null) {
                val index = habits.indexOfFirst {
                    it.id == updatedHabit.id
                }

                if (index != -1) {
                    habits[index] = updatedHabit
                }

                adapter.notifyDataSetChanged()
                updateDashboard()
            }
        }
    }

    private fun deleteHabit(habit: Habit) {
        controller.deleteHabit(habit) {
            habits.removeAll { it.id == habit.id }

            adapter.notifyDataSetChanged()
            updateDashboard()
        }
    }

    private fun updateDashboard() {
        controller.calculateWeeklyConsistency { consistency ->
            tvStreak.text = "$consistency%"
            progressConsistency.progress = consistency

            tvConsistencyDetail.text = when {
                consistency == 100 ->
                    "Meta semanal completa. Excelente consistência!"
                consistency >= 70 ->
                    "Você está perto de concluir sua meta semanal."
                consistency > 0 ->
                    "Continue: cada conclusão fortalece sua rotina."
                else ->
                    "Sua semana está começando. Escolha um hábito para concluir."
            }
        }
    }
}