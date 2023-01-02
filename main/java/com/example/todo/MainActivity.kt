package com.example.todo

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot


class MainActivity : AppCompatActivity() {

    private lateinit var todoAdapter: TodoAdapter

    private val fireStoreDatabase = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        todoAdapter = TodoAdapter(mutableListOf())

        val todolist : RecyclerView = findViewById(R.id.ToDoList)
        val btnAdd : Button = findViewById(R.id.btnAddToDo)
        val btnDel : Button = findViewById(R.id.btnDeleteDoneToDo)
        val editText : EditText = findViewById(R.id.editText)

        todolist.adapter = todoAdapter
        todolist.layoutManager = LinearLayoutManager(this)

        fun readData(){
            fireStoreDatabase.collection("List")
                .get()
                .addOnSuccessListener {
                        results ->
                    for (result in results){
                        todoAdapter.addTodo(Todo(result.get("title").toString(),
                            result.get("isChecked") as Boolean
                        ))
                    }
                }
        }

        readData()


        btnAdd.setOnClickListener {
            val todoTitle = editText.text.toString()

            if (todoTitle.isNotEmpty()) {
                val todo = Todo(todoTitle)

                fireStoreDatabase.collection("List")
                    .add(todo)

                todoAdapter.addTodo(todo)
                editText.text.clear()
            }
        }
        btnDel.setOnClickListener {
            fireStoreDatabase.collection("List")
                .whereEqualTo("isChecked", true)
                .get()
                .addOnCompleteListener(OnCompleteListener<QuerySnapshot?> { task ->
                    if (task.isSuccessful) {
                        for (document in task.result) {
                            fireStoreDatabase.collection("List").document(document.id).delete()
                        }
                    }
                })

            todoAdapter.deleteDoneTodos()

        }


    }


}
