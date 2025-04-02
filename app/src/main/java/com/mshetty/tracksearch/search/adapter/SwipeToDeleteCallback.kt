package com.mshetty.tracksearch.search.adapter

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class SwipeToDeleteCallback(
    private val adapter: SearchRecyclerAdapter,
    private val onSwipeAction: (position: Int, action: SwipeAction) -> Unit
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    private val deleteBackgroundColor = Color.RED

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        onSwipeAction(position, SwipeAction.DELETE)
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

        if (dX < 0) {
            paint.color = deleteBackgroundColor

            c.drawRect(
                itemView.right + dX,
                itemView.top.toFloat(),
                itemView.right.toFloat(),
                itemView.bottom.toFloat(),
                paint
            )
            paint.color = Color.WHITE
            paint.textSize = 40f
            paint.textAlign = Paint.Align.CENTER

            val textX = itemView.right + dX / 2
            val textY = itemView.top + itemView.height / 2f
            c.drawText("Deleting...", textX, textY, paint)
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}

enum class SwipeAction {
    DELETE
}
