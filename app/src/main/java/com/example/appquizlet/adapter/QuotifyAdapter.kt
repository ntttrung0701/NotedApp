import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appquizlet.databinding.QuotifyItemBinding
import com.example.appquizlet.model.MyViewModel

class QuotifyAdapter(
    private val viewModel: MyViewModel,
    private val recyclerView: RecyclerView
) :
    RecyclerView.Adapter<QuotifyAdapter.QuotifyViewHolder>() {

    interface OnQuotifyListener {
        fun handleShareQuote(position: Int)

        fun handleAddToMyQuote(position: Int)
    }

    private var onQuotifyListener: OnQuotifyListener? = null

    class QuotifyViewHolder(val binding: QuotifyItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuotifyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = QuotifyItemBinding.inflate(inflater, parent, false)
        return QuotifyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuotifyViewHolder, position: Int) {
        val item = viewModel.quotes.value?.results?.get(position)
//        item?.let {
//            holder.bind(it, onQuotifyListener)
//        }
        holder.binding.quoteText.text = item?.content
        holder.binding.quoteAuthor.text = item?.author
        holder.binding.btnShareQuote.setOnClickListener {
            onQuotifyListener?.handleShareQuote(position)
        }

        holder.binding.btnSavedQuote.setOnClickListener {
            onQuotifyListener?.handleAddToMyQuote(position)
        }
        holder.binding.executePendingBindings()
    }

    override fun getItemCount(): Int {
        return viewModel.quotes.value?.results?.size ?: 0
    }

    fun handleNextQuote() {
        val nextPosition = viewModel.getNextQuotePosition()
        viewModel.setCurrentPosition(nextPosition)
        recyclerView.smoothScrollToPosition(nextPosition)
    }

    fun handlePrevQuote() {
        val prevPosition = viewModel.getPrevQuotePosition()
        viewModel.setCurrentPosition(prevPosition)
        recyclerView.smoothScrollToPosition(prevPosition)
    }

    fun getQuoteText(position: Int): List<String?> {
        val item = viewModel.quotes.value?.results?.get(position)
        return listOf(item?.content, item?.author)
    }

    fun setOnQuoteShareListener(listener: OnQuotifyListener) {
        onQuotifyListener = listener
    }

}
