package com.example.anyapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.anyapp.databinding.ItemTweetBinding
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
            userID.text = "@" + tweets[position].userID
            textContent.text = tweets[position].textContent

            if (tweets[position].imageContent != null) {
                Picasso.get().load(tweets[position].imageContent).into(imageContent);
            } else {
                val parent: ViewGroup? = imageContent.parent as? ViewGroup
                parent?.let {
                    parent.removeView(imageContent)
                }
            }

            if (tweets[position].videoContent != null) {
                val url =
                    "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
                val player = ExoPlayer.Builder(videoContent.context).build()
                videoContent.player = player
                val mediaItem = MediaItem.fromUri(url);
                player.setMediaItem(mediaItem);
                player.prepare();
                player.play();
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