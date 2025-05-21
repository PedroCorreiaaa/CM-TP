package pt.techcare.app.ui.comentario

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pt.techcare.app.data.model.Comentario
import pt.techcare.app.databinding.ItemComentarioBinding

class ComentarioAdapter(private val lista: List<Comentario>) :
    RecyclerView.Adapter<ComentarioAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemComentarioBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemComentarioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comentario = lista[position]
        holder.binding.txtNome.text = "${comentario.nome_utilizador} (${mapearTipo(comentario.tipo_utilizador)})"
        holder.binding.txtData.text = comentario.data_comentario
        holder.binding.txtConteudo.text = comentario.conteudo
    }

    private fun mapearTipo(tipo: Int): String = when (tipo) {
        1 -> "Utilizador"
        2 -> "Técnico"
        3 -> "Gestor"
        else -> "Desconhecido"
    }
}