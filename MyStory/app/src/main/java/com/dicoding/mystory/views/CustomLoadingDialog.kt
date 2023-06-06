package com.dicoding.mystory.views

import android.app.Dialog
import android.content.Context
import android.view.Window
import com.dicoding.mystory.R

class CustomLoadingDialog(context: Context): Dialog(context) {

    init {
        requestWindowFeature(Window.FEATURE_ACTION_MODE_OVERLAY)
        setCancelable(false)
        setContentView(R.layout.loading_dialog)
        window?.setBackgroundDrawableResource(R.drawable.rounded_dialog)
    }

    fun showDialog() {
        show()
    }

    fun dismissDialog() {
        dismiss()
    }
}