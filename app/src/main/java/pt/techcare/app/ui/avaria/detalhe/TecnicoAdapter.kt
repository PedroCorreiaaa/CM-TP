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
    private val onTecnicoClick: (User) -> Unit
) : RecyclerView.Adapter<TecnicoAdapter.TecnicoViewHolder>() {

    class TecnicoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNome: TextView = itemView.findViewById(R.id.txtNomeTecnico)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TecnicoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tecnico, parent, false)
        return TecnicoViewHolder(view)
    }

    override fun onBindViewHolder(holder: TecnicoViewHolder, position: Int) {
        val tecnico = tecnicos[position]
        holder.txtNome.text = tecnico.nome
        holder.itemView.setOnClickListener { onTecnicoClick(tecnico) }
    }

    override fun getItemCount(): Int = tecnicos.size
}