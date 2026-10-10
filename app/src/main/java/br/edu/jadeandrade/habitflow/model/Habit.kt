package br.edu.jadeandrade.habitflow.model

data class Habit(
    val id: Int,
    var name: String,
    var category: String,
    var goal: String,
    var completedDays: Int,
    var totalDays: Int,
    var durationDays: Int,
    var startDate: String,
    var streak: Int,
    var completedToday: Boolean
)