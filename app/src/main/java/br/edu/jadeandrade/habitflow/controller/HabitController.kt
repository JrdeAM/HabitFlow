package br.edu.jadeandrade.habitflow.controller

import android.content.Context
import android.os.Handler
import android.os.Looper
import br.edu.jadeandrade.habitflow.data.AppDatabase
import br.edu.jadeandrade.habitflow.data.HabitCompletionEntity
import br.edu.jadeandrade.habitflow.data.HabitEntity
import br.edu.jadeandrade.habitflow.model.Habit
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HabitController(
    context: Context,
    private val userId: Int
) {
    private val database = AppDatabase.getDatabase(context.applicationContext)
    private val mainHandler = Handler(Looper.getMainLooper())

    fun loadHabits(onLoaded: (List<Habit>) -> Unit) {
        Thread {
            val completionDao = database.habitCompletionDao()
            val today = getToday()

            val habits = database.habitDao().getByUserId(userId).map { entity ->
                entityToHabit(entity).apply {
                    completedToday =
                        completionDao.wasCompletedOnDate(entity.id, today)
                }
            }

            mainHandler.post {
                onLoaded(habits)
            }
        }.start()
    }

    fun createHabit(
        name: String,
        category: String,
        goal: String,
        totalDays: Int,
        durationDays: Int,
        onCreated: () -> Unit
    ) {
        Thread {
            val newHabit = HabitEntity(
                userId = userId,
                name = name,
                category = category,
                goal = goal,
                completedDays = 0,
                totalDays = totalDays,
                durationDays = durationDays,
                startDate = getToday(),
                streak = 0,
                completedToday = false
            )

            database.habitDao().insert(newHabit)

            mainHandler.post {
                onCreated()
            }
        }.start()
    }

    fun completeHabit(
        habit: Habit,
        onFinished: (Habit?, String?) -> Unit
    ) {
        Thread {
            val completionDao = database.habitCompletionDao()
            val habitDao = database.habitDao()
            val today = getToday()

            if (completionDao.wasCompletedOnDate(habit.id, today)) {
                mainHandler.post {
                    onFinished(null, "Você já concluiu este hábito hoje.")
                }
                return@Thread
            }

            val (startOfWeek, endOfWeek) = getCurrentWeekPeriod()

            val completionsThisWeek = completionDao.countCompletionsInPeriod(
                habitId = habit.id,
                startDate = startOfWeek,
                endDate = endOfWeek
            )

            if (completionsThisWeek >= getWeeklyTarget(habit.goal)) {
                mainHandler.post {
                    onFinished(null, "A meta desta semana já foi concluída.")
                }
                return@Thread
            }

            val updatedHabit = habit.copy(
                completedDays = habit.completedDays + 1,
                streak = if (
                    completionDao.wasCompletedOnDate(habit.id, getYesterday())
                ) {
                    habit.streak + 1
                } else {
                    1
                },
                completedToday = true
            )

            completionDao.insert(
                HabitCompletionEntity(
                    habitId = habit.id,
                    completedDate = today
                )
            )

            habitDao.update(habitToEntity(updatedHabit))

            mainHandler.post {
                onFinished(updatedHabit, null)
            }
        }.start()
    }

    fun deleteHabit(habit: Habit, onDeleted: () -> Unit) {
        Thread {
            database.habitDao().delete(habitToEntity(habit))

            mainHandler.post {
                onDeleted()
            }
        }.start()
    }

    fun calculateWeeklyConsistency(onCalculated: (Int) -> Unit) {
        Thread {
            val habits = database.habitDao().getByUserId(userId)
            val completionDao = database.habitCompletionDao()
            val (startOfWeek, endOfWeek) = getCurrentWeekPeriod()

            val expectedCompletions = habits.sumOf {
                getWeeklyTarget(it.goal)
            }

            val completedCompletions = habits.sumOf { habit ->
                completionDao.countCompletionsInPeriod(
                    habitId = habit.id,
                    startDate = startOfWeek,
                    endDate = endOfWeek
                )
            }

            val consistency = if (expectedCompletions > 0) {
                ((completedCompletions * 100) / expectedCompletions)
                    .coerceAtMost(100)
            } else {
                0
            }

            mainHandler.post {
                onCalculated(consistency)
            }
        }.start()
    }

    private fun getWeeklyTarget(goal: String): Int {
        return when {
            goal.contains("segunda", ignoreCase = true) -> 5
            goal.contains("2 vezes", ignoreCase = true) -> 2
            goal.contains("3 vezes", ignoreCase = true) -> 3
            else -> 7
        }
    }

    private fun getCurrentWeekPeriod(): Pair<String, String> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        val calendar = Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        }

        val startOfWeek = dateFormat.format(calendar.time)

        calendar.add(Calendar.DAY_OF_YEAR, 6)
        val endOfWeek = dateFormat.format(calendar.time)

        return Pair(startOfWeek, endOfWeek)
    }

    private fun getToday(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    private fun getYesterday(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)

        return SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        ).format(calendar.time)
    }

    private fun entityToHabit(entity: HabitEntity): Habit {
        return Habit(
            id = entity.id,
            name = entity.name,
            category = entity.category,
            goal = entity.goal,
            completedDays = entity.completedDays,
            totalDays = entity.totalDays,
            durationDays = entity.durationDays,
            startDate = entity.startDate,
            streak = entity.streak,
            completedToday = entity.completedToday
        )
    }

    private fun habitToEntity(habit: Habit): HabitEntity {
        return HabitEntity(
            id = habit.id,
            userId = userId,
            name = habit.name,
            category = habit.category,
            goal = habit.goal,
            completedDays = habit.completedDays,
            totalDays = habit.totalDays,
            durationDays = habit.durationDays,
            startDate = habit.startDate,
            streak = habit.streak,
            completedToday = habit.completedToday
        )
    }
}