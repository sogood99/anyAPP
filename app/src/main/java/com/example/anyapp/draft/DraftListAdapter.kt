package com.example.anyapp.draft

import android.graphics.Outline
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.example.anyapp.databinding.ItemDraftBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.squareup.picasso.Picasso

class DraftListAdapter(
    var draftList: List<Draft>
) : RecyclerView.Adapter<DraftListAdapter.DraftViewHolder>() {

    private var onDraftNewTweet: (draft: Draft) -> Unit = {
        Log.v("Pity", it.toString())
    }

    inner class DraftViewHolder(val binding: ItemDraftBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DraftViewHolder {
        // how new item_draft are created
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemDraftBinding.inflate(layoutInflater, parent, false)
        return DraftViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DraftViewHolder, position: Int) {
        holder.binding.apply {
            val draft = draftList[position]
            textContent.text = draft.text

            if (draft.imageFile != null) {
                Picasso.get().load(draft.imageFile).into(imageContent)
            } else {
                imageContent.visibility = View.GONE
            }

            if (draft.videoFile != null) {
                videoContent.clipToOutline = true
                videoContent.outlineProvider = object : ViewOutlineProvider() {
                    override fun getOutline(view: View?, outline: Outline?) {
                        if (view != null) {
                            outline?.setRoundRect(0, 0, view.width, view.height, 40F)
                        }
                    }
                }

                val player = ExoPlayer.Builder(videoContent.context).build()
                videoContent.player = player
                val mediaItem = MediaItem.fromUri(draft.videoFile.toUri())
                player.setMediaItem(mediaItem)
                player.prepare()
            } else {
                videoContent.visibility = View.GONE
            }

            setButton.setOnClickListener {
                onDraftNewTweet(draft)
            }

            deleteButton.setOnClickListener {
                DraftList().remove(draft)
            }
        }
    }

    override fun getItemCount(): Int {
        return draftList.size
    }

    fun setOnDraftNewTweet(callback: (Draft) -> Unit) {
        onDraftNewTweet = callback
    }
}