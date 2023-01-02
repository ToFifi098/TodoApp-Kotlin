package com.example.todo

import android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot


class TodoAdapter(
    private val todos: MutableList<Todo>

) : RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {

    private val fireStoreDatabase = FirebaseFirestore.getInstance()

    class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val tvTodoTitle : TextView = itemView.findViewById(R.id.tvTodoTitle)
        val cbDone : CheckBox = itemView.findViewById(R.id.cbDone)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        return TodoViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_list,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        var curTodo = todos[position]
        holder.tvTodoTitle.text = curTodo.title
        holder.cbDone.isChecked = curTodo.isChecked
        toggleStrikeThrough(holder.tvTodoTitle, holder.cbDone.isChecked)
        holder.cbDone.setOnCheckedChangeListener { _, b ->
            toggleStrikeThrough(holder.tvTodoTitle, b)
            curTodo.isChecked = !curTodo.isChecked
        }
    }

    fun addTodo (todo: Todo){
        todos.add(todo)
        notifyItemInserted(todos.size -1)
    }

    fun deleteDoneTodos(){
        todos.removeAll{
                todo -> todo.isChecked
        }
        notifyDataSetChanged()
    }

    private fun toggleStrikeThrough (tvTodoTitle: TextView, isChecked: Boolean){
        if(isChecked){
            fireStoreDatabase.collection("List").whereEqualTo("title" ,tvTodoTitle.text).get().addOnCompleteListener(
                OnCompleteListener<QuerySnapshot?> { task ->
                if (task.isSuccessful) {
                    for (document in task.result) {
                        fireStoreDatabase.collection("List").document(document.id).update("isChecked", true)
                    }
                }
            })
            tvTodoTitle.paintFlags = tvTodoTitle.paintFlags or STRIKE_THRU_TEXT_FLAG
        }   else {
            fireStoreDatabase.collection("List").whereEqualTo("title" ,tvTodoTitle.text).get().addOnCompleteListener(
                OnCompleteListener<QuerySnapshot?> { task ->
                    if (task.isSuccessful) {
                        for (document in task.result) {
                            fireStoreDatabase.collection("List").document(document.id).update("isChecked", false)
                        }
                    }
                })
            tvTodoTitle.paintFlags = tvTodoTitle.paintFlags and STRIKE_THRU_TEXT_FLAG.inv()
        }
    }

    override fun getItemCount(): Int {
        return todos.size
    }

}