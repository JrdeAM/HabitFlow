package br.edu.jadeandrade.habitflow.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.edu.jadeandrade.habitflow.R
import br.edu.jadeandrade.habitflow.model.Habit

class HabitAdapter(
    private val habits: MutableList<Habit>,
    private val onCompleteClick: (Habit) -> Unit,
    private val onDeleteClick: (Habit) -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvHabitName: TextView = itemView.findViewById(R.id.tvHabitName)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvStreak: TextView = itemView.findViewById(R.id.tvStreak)
        val progressHabit: ProgressBar = itemView.findViewById(R.id.progressHabit)
        val tvProgress: TextView = itemView.findViewById(R.id.tvProgress)
        val btnComplete: Button = itemView.findViewById(R.id.btnComplete)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit, parent, false)

        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]

        val progress = if (habit.totalDays > 0) {
            ((habit.completedDays * 100) / habit.totalDays).coerceAtMost(100)
        } else {
            0
        }

        holder.tvHabitName.text = habit.name
        holder.tvCategory.text = "${habit.category} • ${habit.goal}"
        holder.tvStreak.text =
            if (habit.streak == 1) {
                "🔥 1 dia consecutivo"
            } else {
                "🔥 ${habit.streak} dias consecutivos"
            }
        holder.progressHabit.progress = progress

        holder.tvProgress.text =
            "${habit.completedDays}/${habit.totalDays} conclusões • $progress%"

        holder.btnComplete.text =
            if (habit.completedToday) "Concluído hoje" else "Concluir"

        holder.btnComplete.isEnabled = !habit.completedToday

        holder.btnComplete.setOnClickListener {
            onCompleteClick(habit)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(habit)
        }
    }

    override fun getItemCount(): Int = habits.size
}