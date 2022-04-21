package no.exam.android.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import no.exam.android.R
import no.exam.android.models.ParentItem

class ParentAdapter(
    private val parentList: ArrayList<ParentItem>,
    private val context: Context
) : RecyclerView.Adapter<ParentAdapter.ViewHolder>() {

    var viewGroup: ViewGroup? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.saved_parent_item,
            parent, false
        )
        viewGroup = parent
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val parentItem = parentList[position]
        holder.original.setImageBitmap(parentItem.original.bitmap)
        val images = holder.images
        images.layoutManager = LinearLayoutManager(context)
        images.setHasFixedSize(false)
        images.adapter = ImageAdapter(parentItem.images, context)
    }

    override fun getItemCount(): Int {
        return parentList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val original: ImageView = itemView.findViewById(R.id.original)
        val images: RecyclerView = itemView.findViewById(R.id.images)
    }
}