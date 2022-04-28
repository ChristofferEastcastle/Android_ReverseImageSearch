package no.exam.android.adapters

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat.PNG
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import no.exam.android.R
import no.exam.android.activities.ImagePopupActivity
import java.io.ByteArrayOutputStream

class ImageAdapter(private val imageList: List<Bitmap>) :
    RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    var viewGroup: ViewGroup? = null
    var parentPos: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.list_item,
            parent, false
        )
        viewGroup = parent
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentImage = imageList[position]
        val image = holder.image
        image.setImageBitmap(currentImage)
        image.setOnClickListener {
            onClickImage(position)
        }
    }

    private fun onClickImage(position: Int) {
        if (viewGroup == null) return
        val intent = Intent(viewGroup?.context, ImagePopupActivity::class.java)
        val image = imageList[position]
        val parent = viewGroup?.parent
        if (parent is View?) {
            intent.putExtra("PARENT_TAG", parent?.tag.toString())
        }
        val stream = ByteArrayOutputStream()
        image.compress(PNG, 100, stream)
        intent.putExtra("IMAGE", stream.toByteArray())
        intent.putExtra("PARENT_POSITION", parentPos)
        intent.putExtra("POSITION", position)

        startActivity(viewGroup!!.context, intent, null)
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.list_image)
    }
}