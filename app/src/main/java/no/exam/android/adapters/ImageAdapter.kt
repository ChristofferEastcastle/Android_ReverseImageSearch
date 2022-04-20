package no.exam.android.adapters

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.drawToBitmap
import androidx.recyclerview.widget.RecyclerView
import no.exam.android.R
import no.exam.android.activities.SavePopupActivity
import no.exam.android.fragments.ResultsFragment
import no.exam.android.models.ImageBitmap
import java.io.ByteArrayOutputStream

class ImageAdapter(private val imageList: ArrayList<Bitmap>, private val context: ResultsFragment) : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    var viewGroup: ViewGroup? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item,
        parent, false)
        viewGroup = parent
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentImage = imageList[position]
        val image = holder.image
        image.setImageBitmap(currentImage)
        image.setOnClickListener {
            onClickImage(position, holder)
        }
    }

    private fun onClickImage(
        position: Int,
        holder: ViewHolder
    ) {
        Toast.makeText(
            viewGroup?.context,
            "Position: $position ID: ${holder.itemId}",
            Toast.LENGTH_SHORT
        ).show()
        val intent = Intent(context.requireContext(), SavePopupActivity::class.java)
        val bitmap = imageList[position]
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        intent.putExtra("IMAGE", byteArrayOutputStream.toByteArray())
        context.startActivity(intent)
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.list_image)
    }
}