package com.example

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background

class FloatingBubbleService : Service(), LifecycleOwner, SavedStateRegistryOwner, ViewModelStoreOwner {

    private lateinit var windowManager: WindowManager
    private lateinit var bubbleLayout: View
    private lateinit var bubbleParams: WindowManager.LayoutParams
    
    private var overlayPanel: OverlayPanelManager? = null

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()

    override val viewModelStore: ViewModelStore
        get() = store

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        
        startForegroundService()
        setupBubble()
    }

    private fun startForegroundService() {
        val channelId = "replify_bubble_service"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Replify Overlay",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Replify is running")
            .setContentText("Tap the floating bubble to generate replies.")
            .setSmallIcon(android.R.drawable.ic_menu_edit)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(1, notification)
        }
    }

    private var isDragging = false
    private lateinit var dismissLayout: View
    private lateinit var dismissParams: WindowManager.LayoutParams

    private fun setupBubble() {
        if (!android.provider.Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }
        
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        bubbleLayout = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@FloatingBubbleService)
            setViewTreeSavedStateRegistryOwner(this@FloatingBubbleService)
            setViewTreeViewModelStoreOwner(this@FloatingBubbleService)
            setContent {
                BubbleView(
                    onClick = { openOverlay() },
                    onDragStart = { showDismissArea() },
                    onDrag = { dx, dy ->
                        bubbleParams.x += dx.toInt()
                        bubbleParams.y += dy.toInt()
                        windowManager.updateViewLayout(this@apply, bubbleParams)
                    },
                    onDragEnd = { snapToEdge() }
                )
            }
        }

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        dismissLayout = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@FloatingBubbleService)
            setViewTreeSavedStateRegistryOwner(this@FloatingBubbleService)
            setViewTreeViewModelStoreOwner(this@FloatingBubbleService)
            setContent {
                androidx.compose.foundation.layout.Box(
                    modifier = androidx.compose.ui.Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(androidx.compose.ui.graphics.Color.Transparent, androidx.compose.ui.graphics.Color.Red.copy(alpha = 0.6f))
                            )
                        ),
                    contentAlignment = androidx.compose.ui.Alignment.BottomCenter
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = androidx.compose.ui.graphics.Color.White,
                        modifier = androidx.compose.ui.Modifier.padding(bottom = 32.dp).size(40.dp)
                    )
                }
            }
        }

        dismissParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            150.toIntPx(), // Will deal with this later, let's use actual pixels calculated
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM
            y = 0
            alpha = 0f // Hidden by default
        }
        windowManager.addView(dismissLayout, dismissParams)

        bubbleParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 300
        }

        windowManager.addView(bubbleLayout, bubbleParams)
        
        overlayPanel = OverlayPanelManager(this, windowManager, layoutFlag)
    }
    
    private fun Int.toIntPx(): Int = (this * resources.displayMetrics.density).toInt()

    private fun showDismissArea() {
        dismissParams.alpha = 1f
        windowManager.updateViewLayout(dismissLayout, dismissParams)
    }

    private fun hideDismissArea() {
        dismissParams.alpha = 0f
        windowManager.updateViewLayout(dismissLayout, dismissParams)
    }

    private fun snapToEdge() {
        hideDismissArea()
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        
        // If dragged to the bottom of the screen, stop the service
        if (bubbleParams.y > screenHeight - 400) {
            android.widget.Toast.makeText(this, "Replify Bubble Dismissed", android.widget.Toast.LENGTH_SHORT).show()
            stopSelf()
            return
        }

        val midX = screenWidth / 2
        bubbleParams.x = if (bubbleParams.x < midX) 0 else screenWidth
        windowManager.updateViewLayout(bubbleLayout, bubbleParams)
    }

    private fun openOverlay() {
        overlayPanel?.showPanel()
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        if (::bubbleLayout.isInitialized) {
            windowManager.removeView(bubbleLayout)
        }
        if (::dismissLayout.isInitialized) {
            windowManager.removeView(dismissLayout)
        }
        overlayPanel?.destroy()
    }

    override fun onBind(intent: Intent): IBinder? = null

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry
}
