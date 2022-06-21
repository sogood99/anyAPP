package com.example.anyapp.draft

import java.io.File

data class Draft(
    val text: String,
    val replyId: Int?,
    val imageFile: File?,
    val videoFile: File?,
    val audioFile: File?,
    val location: String,
)

class DraftList {

    fun add(draft: Draft) {
        draftList.add(draft)
        adapter?.notifyDataSetChanged()
    }

    fun remove(draft: Draft) {
        draftList.remove(draft)
        adapter?.notifyDataSetChanged()
    }

    companion object {
        var adapter: DraftListAdapter? = null
        val draftList: MutableList<Draft> = mutableListOf()
    }
}
