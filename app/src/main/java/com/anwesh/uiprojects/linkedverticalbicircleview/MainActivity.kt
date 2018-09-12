package com.anwesh.uiprojects.linkedverticalbicircleview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.verticalbicircleview.VerticalBiCircleView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        VerticalBiCircleView.create(this)
    }
}
