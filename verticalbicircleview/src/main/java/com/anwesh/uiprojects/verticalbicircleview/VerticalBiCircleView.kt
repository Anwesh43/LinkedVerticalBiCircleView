package com.anwesh.uiprojects.verticalbicircleview

/**
 * Created by anweshmishra on 13/09/18.
 */

import android.app.Activity
import android.view.View
import android.view.MotionEvent
import android.content.Context
import android.graphics.*

val nodes : Int = 5

fun Canvas.drawVBCNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = Math.min(w, h) / 60
    paint.strokeCap = Paint.Cap.ROUND
    paint.color = Color.parseColor("#1976D2")
    val sc1 : Float = Math.min(0.5f, scale) * 2
    val sc2 : Float = Math.min(0.5f, Math.max(0f, scale - 0.5f)) * 2
    val gap : Float = w / (nodes + 1)
    val r : Float = gap / 3
    save()
    translate(gap + i * gap, h/2)
    for (j in 0..1) {
        val sf = 1 - 2 * (j % 2)
        save()
        translate(0f, r * sf + (h/2 - 2 * r) * sf * sc2)
        rotate(180f * sc1)
        drawArc(RectF(-r, -2*r + 2 * r * j, r, -2*r + 2 * r * j + 2 * r),
                180f * j, 180f, false, paint)
        restore()
    }
    restore()
}

class VerticalBiCircleView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {
        fun update(cb : (Float) -> Unit) {
            this.scale += 0.1f * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {
        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class VBCNode(var i : Int, val state : State = State()) {
        private var next : VBCNode? = null
        private var prev : VBCNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = VBCNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawVBCNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : VBCNode  {
            var curr : VBCNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class VerticalBiCircle(var i : Int) {
        private var root : VBCNode = VBCNode(0)
        private var curr : VBCNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : VerticalBiCircleView) {
        private val animator : Animator = Animator(view)
        private val vbc : VerticalBiCircle = VerticalBiCircle(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#BDBDBD"))
            vbc.draw(canvas, paint)
            animator.animate {
                vbc.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            vbc.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        fun create(activity : Activity) : VerticalBiCircleView {
            val view : VerticalBiCircleView = VerticalBiCircleView(activity)
            activity.setContentView(view)
            return view
        }
    }
}