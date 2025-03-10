import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.mshetty.tracksearch.R
import com.mshetty.tracksearch.search.adapter.SearchRecyclerAdapter
import kotlin.math.max

class SwipeToDeleteCallback(
    private val adapter: SearchRecyclerAdapter,
    private val onSwipeAction: (position: Int, action: SwipeAction) -> Unit
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    private val deleteIcon = R.drawable.ic_delete
    private val editIcon = R.drawable.ic_edit
    private val deleteBackgroundColor = Color.WHITE
    private val editBackgroundColor = Color.WHITE

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // Swipe action is handled, but we no longer need to rely on swipe for delete/edit
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
        val paint = Paint()
        val iconPadding = 20
        val iconSize = 80
        val threshold = itemView.width / 2f
        val clampedDx = max(dX, -threshold)

        if (dX < 0) {
            paint.color = if (clampedDx < -threshold / 2) editBackgroundColor else deleteBackgroundColor
            c.drawRect(
                itemView.right + clampedDx, itemView.top.toFloat(),
                itemView.right.toFloat(), itemView.bottom.toFloat(), paint
            )

            val deleteBitmap: Bitmap = BitmapFactory.decodeResource(itemView.context.resources, deleteIcon)
            val editBitmap: Bitmap = BitmapFactory.decodeResource(itemView.context.resources, editIcon)

            val scaledDeleteBitmap = Bitmap.createScaledBitmap(deleteBitmap, iconSize, iconSize, false)
            val scaledEditBitmap = Bitmap.createScaledBitmap(editBitmap, iconSize, iconSize, false)

            val centerY = itemView.top + (itemView.height - iconSize) / 2

            val deleteIconRight = itemView.right - iconPadding - scaledDeleteBitmap.width
            val deleteIconTop = centerY

            val editIconRight = itemView.right - (threshold / 2) - iconPadding - scaledEditBitmap.width
            val editIconTop = centerY

            c.drawBitmap(scaledDeleteBitmap, deleteIconRight.toFloat(), deleteIconTop.toFloat(), paint)

            if (clampedDx < -threshold / 2) {
                c.drawBitmap(scaledEditBitmap, editIconRight.toFloat(), editIconTop.toFloat(), paint)
            }
        }

        super.onChildDraw(c, recyclerView, viewHolder, clampedDx, dY, actionState, isCurrentlyActive)
    }

    // This method checks if the user clicked on the delete or edit icon
    fun onItemClick(viewHolder: RecyclerView.ViewHolder, x: Float, y: Float) {
        val itemView = viewHolder.itemView
        val deleteIconRect = getIconRect(itemView, deleteIcon)
        val editIconRect = getIconRect(itemView, editIcon)

        // Check if the click happened inside the delete or edit icon
        if (deleteIconRect.contains(x.toInt(), y.toInt())) {
            val position = viewHolder.adapterPosition
            onSwipeAction(position, SwipeAction.DELETE)
            Toast.makeText(itemView.context, "Item Deleted", Toast.LENGTH_SHORT).show()
        } else if (editIconRect.contains(x.toInt(), y.toInt())) {
            val position = viewHolder.adapterPosition
            onSwipeAction(position, SwipeAction.EDIT)
            Toast.makeText(itemView.context, "Item Edited", Toast.LENGTH_SHORT).show()
        }
    }

    // Helper function to get the bounding rectangle of the icons
    private fun getIconRect(itemView: View, icon: Int): Rect {
        val iconBitmap = BitmapFactory.decodeResource(itemView.context.resources, icon)
        val iconWidth = iconBitmap.width
        val iconHeight = iconBitmap.height
        val threshold = itemView.width / 2
        val iconPadding = 20

        // For delete icon
        if (icon == deleteIcon) {
            return Rect(
                itemView.right - iconPadding - iconWidth,
                itemView.top + (itemView.height - iconHeight) / 2,
                itemView.right - iconPadding,
                itemView.top + (itemView.height + iconHeight) / 2
            )
        }
        // For edit icon
        else if (icon == editIcon) {
            return Rect(
                itemView.right - threshold / 2 - iconPadding - iconWidth,
                itemView.top + (itemView.height - iconHeight) / 2,
                itemView.right - threshold / 2 - iconPadding,
                itemView.top + (itemView.height + iconHeight) / 2
            )
        }
        return Rect()
    }
}

enum class SwipeAction {
    DELETE,
    EDIT
}
