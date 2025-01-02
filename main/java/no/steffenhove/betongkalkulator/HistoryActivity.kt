package no.steffenhove.betongkalkulator

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.ListView
import android.content.Context

class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val prefs = getSharedPreferences("calculations", Context.MODE_PRIVATE)
        val results = prefs.getStringSet("results", mutableSetOf())?.toList() ?: listOf()

        val listView: ListView = findViewById(R.id.history_list_view)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, results)
        listView.adapter = adapter
    }
}
