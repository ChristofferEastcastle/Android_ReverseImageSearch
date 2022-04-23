package no.exam.android.adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.recyclerview.widget.RecyclerView
import no.exam.android.R
import no.exam.android.entities.ImageEntity

class ParentAdapter(
    private val parentList: List<ImageEntity>,
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
        val bitmap = BitmapFactory.decodeByteArray(parentItem.bytes, 0, 0)
        holder.original.setImageBitmap(bitmap)
        val images = holder.images
        images.layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
        images.setHasFixedSize(false)
        parentItem.originals?.let {
            images.adapter = ImageAdapter(parentItem.originals, context)
        }
    }

    override fun getItemCount(): Int {
        return parentList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val original: ImageView = itemView.findViewById(R.id.original)
        val images: RecyclerView = itemView.findViewById(R.id.images)
    }
}