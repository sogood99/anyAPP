package com.example.anyapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.anyapp.databinding.ItemTweetBinding
import com.example.anyapp.util.Constants.Companion.BASE_URL
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.squareup.picasso.Picasso

class TweetAdapter(
    var tweets: List<Tweet>
) : RecyclerView.Adapter<TweetAdapter.TweetViewHolder>() {

    inner class TweetViewHolder(val binding: ItemTweetBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TweetViewHolder {
        // how new item_tweets are created
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemTweetBinding.inflate(layoutInflater, parent, false)
        return TweetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TweetViewHolder, position: Int) {
        // how the tweet.kt data class is synced with item_tweet
        holder.binding.apply {
            username.text = tweets[position].username
            userID.text = "@" + tweets[position].username
            textContent.text = tweets[position].text

            if (tweets[position].imageUrl != null) {
                // load image if tweet.imageContent has content
                val url = BASE_URL + "/" + tweets[position].imageUrl
                Picasso.get().load(url).into(imageContent);
            } else {
                // otherwise delete it
                val parent: ViewGroup? = imageContent.parent as? ViewGroup
                parent?.let {
                    parent.removeView(imageContent)
                }
            }

            if (tweets[position].videoUrl != null) {
                // same as image
                val url = BASE_URL + "/" + tweets[position].videoUrl
                url?.let {
                    val player = ExoPlayer.Builder(videoContent.context).build()
                    videoContent.player = player
                    val mediaItem = MediaItem.fromUri(it)
                    player.setMediaItem(mediaItem)
                    player.prepare()
//                    player.play()
                }
            } else {
                val parent: ViewGroup? = videoContent.parent as? ViewGroup
                parent?.let {
                    parent.removeView(videoContent)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return tweets.size
    }
}