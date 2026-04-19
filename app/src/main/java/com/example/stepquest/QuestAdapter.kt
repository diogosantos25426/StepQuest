package com.example.stepquest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.stepquest.data.Quest

class QuestAdapter(
    private var quests: List<Quest>
) : RecyclerView.Adapter<QuestAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tv_quest_title)
        val tvDescription: TextView = view.findViewById(R.id.tv_quest_description)
        val pbProgress: ProgressBar = view.findViewById(R.id.pb_quest_progress)
        val tvProgressText: TextView = view.findViewById(R.id.tv_quest_progress_text)
        val tvReward: TextView = view.findViewById(R.id.tv_quest_reward)
        val ivCompleted: ImageView = view.findViewById(R.id.iv_quest_completed)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_quest, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val quest = quests[position]
        holder.tvTitle.text = quest.titulo
        holder.tvDescription.text = quest.descricao
        
        holder.pbProgress.max = quest.objetivoQuantidade
        holder.pbProgress.progress = quest.progressoAtual
        holder.tvProgressText.text = "${quest.progressoAtual}/${quest.objetivoQuantidade}"
        
        val rewardText = StringBuilder("Recompensa: ${quest.recompensaXP} XP")
        quest.recompensaItemNome?.let { rewardText.append(" + $it") }
        holder.tvReward.text = rewardText.toString()

        if (quest.isConcluida) {
            holder.ivCompleted.visibility = View.VISIBLE
            holder.itemView.alpha = 0.6f
        } else {
            holder.ivCompleted.visibility = View.GONE
            holder.itemView.alpha = 1.0f
        }
    }

    override fun getItemCount() = quests.size

    fun updateQuests(newQuests: List<Quest>) {
        quests = newQuests
        notifyDataSetChanged()
    }
}