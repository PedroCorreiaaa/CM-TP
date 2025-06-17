package pt.techcare.app.ui.notificacoes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pt.techcare.app.R
import pt.techcare.app.data.model.Notificacao
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class NotificacaoAdapter(
    private var notificacoes: List<Notificacao>,
    private val onItemClick: (Notificacao) -> Unit
) : RecyclerView.Adapter<NotificacaoAdapter.NotificacaoViewHolder>() {

    // Atualiza a lista de notificações
    fun updateList(newNotificacoes: List<Notificacao>) {
        notificacoes = newNotificacoes.toList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificacaoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notificacao, parent, false)
        return NotificacaoViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificacaoViewHolder, position: Int) {
        val notificacao = notificacoes[position]
        holder.bind(notificacao)
        holder.itemView.setOnClickListener { onItemClick(notificacao) }
    }

    override fun getItemCount(): Int = notificacoes.size

    class NotificacaoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMensagem: TextView = itemView.findViewById(R.id.tvMensagem)
        private val tvData: TextView = itemView.findViewById(R.id.tvData)
        private val tvTipo: TextView = itemView.findViewById(R.id.tvTipo)

        // Liga os dados da notificação ao layout
        fun bind(notificacao: Notificacao) {
            tvMensagem.text = notificacao.mensagem

            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.getDefault())
            val dateTime = LocalDateTime.parse(notificacao.data_emissao, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            tvData.text = dateTime.format(formatter)

            tvTipo.text = notificacao.tipo
        }
    }
}