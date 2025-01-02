package no.steffenhove.betongkalkulator

import android.os.Bundle
import android.util.Log
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.DecimalFormat


class HistoryActivity : AppCompatActivity() {

    private lateinit var historyListView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var history: MutableList<Calculation>
    private val gson = Gson() // Opprett en Gson-instans

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        historyListView = findViewById(R.id.historyListView)
        history = getHistory().toMutableList()

        // Bruk en adapter som viser Calculation-objektene som strenger
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_activated_1, history.map { it.toStringRepresentation() })
        historyListView.adapter = adapter
        historyListView.choiceMode = ListView.CHOICE_MODE_MULTIPLE_MODAL

        historyListView.setMultiChoiceModeListener(object : AbsListView.MultiChoiceModeListener {
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
            adapter.clear() // Tøm adapteren
            adapter.notifyDataSetChanged() // Oppdater visningen
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
        saveHistory(emptyList()) // Lagre en tom liste for å tømme historikken
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
        adapter.addAll(history.map{it.toStringRepresentation()})
        adapter.notifyDataSetChanged()
    }
    private fun Calculation.toStringRepresentation(): String {
        return "Volum: ${this.volume} m³, Vekt: ${this.weight} kg, ${this.shape}, ${this.dimensions}, Dato: ${this.datetime}"
    }
}