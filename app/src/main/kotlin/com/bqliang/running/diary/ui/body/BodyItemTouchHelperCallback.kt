package com.bqliang.running.diary.ui.body

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.view.HapticFeedbackConstants
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.bqliang.running.diary.R
import com.bqliang.running.diary.database.entity.Body
import com.google.android.material.color.MaterialColors
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlin.math.abs


class BodyItemTouchHelperCallback(
    private val adapter: BodyAdapter,
    context: Context,
    private val onSwipe: (bodyId: Body) -> Unit,
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    private var errorColor: Int

    private var colorOnError: Int

    init {
        errorColor = MaterialColors.getColor(
            context,
            com.google.android.material.R.attr.colorError,
            Color.RED
        )

        colorOnError = MaterialColors.getColor(
            context,
            com.google.android.material.R.attr.colorOnError,
            Color.WHITE
        )
    }

    private var oldDx = 0f

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ) = false // 不支持拖动

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder) = 0.35f

    // 调用: 当用户滑动了一个 item 并且手指松开的时候会回调这个方法
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val threshold = viewHolder.itemView.width * getSwipeThreshold(viewHolder)
        val position = viewHolder.absoluteAdapterPosition
        val body = adapter.currentList[position]
        onSwipe(body)
    }

    override fun onChildDraw(
        canvas: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        RecyclerViewSwipeDecorator.Builder(
            canvas,
            recyclerView,
            viewHolder,
            dX,
            dY,
            actionState,
            isCurrentlyActive
        )
            .addBackgroundColor(errorColor)
            .addActionIcon(R.drawable.round_delete_sweep_24)
            .setActionIconTint(colorOnError)
            .create()
            .decorate()

        val threshold = viewHolder.itemView.width * getSwipeThreshold(viewHolder)
        if (abs(oldDx) < threshold && abs(dX) >= threshold ) {
            recyclerView.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        } else if (abs(oldDx) >= threshold && abs(dX) < threshold) {
            recyclerView.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        }
        oldDx = dX

        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}