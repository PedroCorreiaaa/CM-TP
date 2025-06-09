package pt.techcare.app.ui.avaria.detalhe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pt.techcare.app.R
import pt.techcare.app.data.model.User

class TecnicoAdapter(
    private val tecnicos: List<User>,
    private val onItemClick: (User) -> Unit
) : RecyclerView.Adapter<TecnicoAdapter.TecnicoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TecnicoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tecnico, parent, false)
        return TecnicoViewHolder(view)
    }

    override fun onBindViewHolder(holder: TecnicoViewHolder, position: Int) {
        holder.bind(tecnicos[position])
    }

    override fun getItemCount() = tecnicos.size

    inner class TecnicoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(tecnico: User) {
            itemView.findViewById<TextView>(R.id.txtNomeTecnico).text = tecnico.nome
            itemView.setOnClickListener { onItemClick(tecnico) }
        }
    }
}