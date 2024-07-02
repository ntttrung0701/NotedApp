import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appquizlet.databinding.LayoutNotificationBinding
import com.example.appquizlet.interfaceFolder.ItemNotificationClick
import com.example.appquizlet.model.NoticeModel

class NotificationAdapter(
    private val listNotifications: List<NoticeModel>,
    private val context: Context,
    private val onClickItemNotification: ItemNotificationClick
) :
    RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: LayoutNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutNotificationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = listNotifications[position]
        val titleNotification = holder.binding.txtTitleNotification
        val detailNotification = holder.binding.txtContentNotification
        val imgNotification = holder.binding.imgNotification

        val imageName = "ac${listNotifications[position].id}"
        val imageResourceId =
            context.resources.getIdentifier(imageName, "drawable", context.packageName)

        titleNotification.text = notification.title
        detailNotification.text = notification.detail
        imgNotification.setImageResource(imageResourceId)
//        holder.binding.notificationDivider.visibility = View.VISIBLE


        holder.itemView.setOnClickListener {
            onClickItemNotification.handleClickItemNotification(position)
        }

    }

    override fun getItemCount(): Int {
        return listNotifications.size
    }
}
