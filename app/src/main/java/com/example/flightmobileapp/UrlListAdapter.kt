package com.example.flightmobileapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Display list of urls in a recyclerView.
class UrlListAdapter internal constructor(
    context: Context
) : RecyclerView.Adapter<UrlListAdapter.UrlViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    // Cached copy of urls.
    private var urls = emptyList<Url>()

    inner class UrlViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val urlItemView: TextView = itemView.findViewById(R.id.textView)
    }

    // Create ViewHolder which holds the recyclerview_item.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UrlViewHolder {
        val itemView = inflater.inflate(R.layout.recyclerview_item, parent, false)
        return UrlViewHolder(itemView)
    }

    // Locate url item in the recycler view.
    override fun onBindViewHolder(holder: UrlViewHolder, position: Int) {
        val current = urls[position]
        holder.urlItemView.text = current.url
    }

    // Update urls list.
    internal fun setUrls(urls: List<Url>) {
        this.urls = urls
        notifyDataSetChanged()
    }

    // Get the list size.
    override fun getItemCount() = urls.size
}