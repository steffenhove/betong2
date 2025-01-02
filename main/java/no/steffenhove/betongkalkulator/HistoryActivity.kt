package no.steffenhove.betongkalkulator

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.DecimalFormat

class HistoryActivity : AppCompatActivity() {
    private lateinit var historyListView: ListView
    private lateinit var adapter: HistoryAdapter
    private val gson = Gson()
    private var history = mutableListOf<Calculation>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        historyListView = findViewById(R.id.history_list_view)
        history = getHistory().toMutableList()
        adapter = HistoryAdapter(this, history.map { it.toStringRepresentation() }.toMutableList())
        historyListView.adapter = adapter

        historyListView.setMultiChoiceModeListener(object: AbsListView.MultiChoiceModeListener {
            override fun onItemCheckedStateChanged(mode: ActionMode?, position: Int, id: Long, checked: Boolean) {
                mode?.title = "${historyListView.checkedItemCount} valgt"
            }

            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                menuInflater.inflate(R.menu.history_context_menu, menu)
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                return when (item?.itemId) {
                    R.id.action_delete -> {
                        deleteSelectedItems()
                        mode?.finish()
                        true
                    }
                    R.id.action_sum -> {
                        sumSelectedItems()
                        mode?.finish()
                        true
                    }
                    else -> false
                }
            }

            override fun onDestroyActionMode(mode: ActionMode?) {}
        })

        val buttonClearHistory = findViewById<Button>(R.id.button_clear_history)
        buttonClearHistory.setOnClickListener {
            clearHistory()
            adapter.clear()
            adapter.notifyDataSetChanged()
        }
    }

    private fun getHistory(): List<Calculation> {
        val prefs = getSharedPreferences("history", MODE_PRIVATE)
        val calculationsString = prefs.getString("calculations", "[]")

        return try {
            val type = object : TypeToken<List<Calculation>>() {}.type
            gson.fromJson(calculationsString, type) ?: emptyList()
        } catch (e: Exception) {
            Log.e("HistoryActivity", "Error parsing history JSON", e)
            emptyList()
        }
    }

    private fun saveHistory(history: List<Calculation>) {
        val prefs = getSharedPreferences("history", MODE_PRIVATE)
        val editor = prefs.edit()
        val jsonString = gson.toJson(history)
        editor.putString("calculations", jsonString)
        editor.apply()
    }

    private fun clearHistory() {
        saveHistory(emptyList())
    }

    private fun sumSelectedItems() {
        val checkedItems = historyListView.checkedItemPositions
        var totalVolume = 0.0
        var totalWeight = 0.0

        for (i in 0 until history.size) {
            if (checkedItems.get(i)) {
                totalVolume += history[i].volume
                totalWeight += history[i].weight
            }
        }

        if (totalVolume > 0.0 || totalWeight > 0.0) {
            val df = DecimalFormat("#.##")
            Toast.makeText(this, "Total volum: ${df.format(totalVolume)} m³, Total vekt: ${df.format(totalWeight)} kg", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Ingen elementer valgt for summering", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteSelectedItems() {
        val checkedItems = historyListView.checkedItemPositions
        val itemsToRemove = mutableListOf<Calculation>()

        for (i in history.size - 1 downTo 0) {
            if (checkedItems.get(i)) {
                itemsToRemove.add(history[i])
            }
        }

        history.removeAll(itemsToRemove)
        saveHistory(history)
        adapter.clear()
        adapter.addAll(history.map { it.toStringRepresentation() })
        adapter.notifyDataSetChanged()
    }

    private fun Calculation.toStringRepresentation(): String {
        return "Volum: ${this.volume} m³, Vekt: ${this.weight} kg, ${this.shape}, ${this.dimensions}, Dato: ${this.datetime}"
    }
}
