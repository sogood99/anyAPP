package com.example.anyapp.draft

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.anyapp.databinding.FragmentDraftListBinding

class DraftListFragment : Fragment() {

    private lateinit var binding: FragmentDraftListBinding
    private var adapter: DraftListAdapter = DraftListAdapter(DraftList.draftList)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDraftListBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        DraftList.adapter = adapter
        binding.draftRecyclerView.adapter = adapter
    }

    fun setOnDraftNewTweet(callback: (Draft) -> Unit) {
        adapter.setOnDraftNewTweet(callback)
    }

    companion object {
        @JvmStatic
        fun newInstance() = DraftListFragment()
    }
}