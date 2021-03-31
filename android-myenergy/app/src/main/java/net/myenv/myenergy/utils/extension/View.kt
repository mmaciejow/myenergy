package net.myenv.myenergy.utils.extension

import android.view.View

fun View.show(visibility: Boolean) {
    if(visibility)
      this.visibility = View.VISIBLE
    else
        this.visibility = View.INVISIBLE
}