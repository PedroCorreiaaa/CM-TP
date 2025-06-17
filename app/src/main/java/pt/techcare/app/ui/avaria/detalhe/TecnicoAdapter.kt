package pt.techcare.app.ui.avaria.detalhe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pt.techcare.app.data.model.User

//Mostrar uma lista de técnicos,Reagir quando o utilizador clica num técnico e Atualizar dinamicamente essa lista.
class TecnicoAdapter(
    private var tecnicos: List<User>,
    private val onTecnicoSelected: (User) -> Unit
) : RecyclerView.Adapter<TecnicoAdapter.TecnicoViewHolder>() {

    class TecnicoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nomeTextView: TextView = itemView.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TecnicoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return TecnicoViewHolder(view)
    }

    override fun onBindViewHolder(holder: TecnicoViewHolder, position: Int) {
        val tecnico = tecnicos[position]
        holder.nomeTextView.text = tecnico.nome
        holder.itemView.setOnClickListener { onTecnicoSelected(tecnico) }
    }

    override fun getItemCount(): Int = tecnicos.size

    fun updateList(newTecnicos: List<User>) {
        tecnicos = newTecnicos
        notifyDataSetChanged()
    }
}