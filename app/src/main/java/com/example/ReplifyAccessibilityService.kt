package com.example

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class ReplifyAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        instance = null
        return super.onUnbind(intent)
    }

    fun getLastReceivedMessage(): String {
        var targetRoot: AccessibilityNodeInfo? = null
        val myPackage = packageName // inherited from Context wrapper
        
        fun isOurOverlay(node: AccessibilityNodeInfo?): Boolean {
            if (node == null) return false
            if (node.text?.toString()?.contains("Replify") == true || node.text?.toString()?.contains("Generate Reply") == true) return true
            for (i in 0 until node.childCount) {
                if (isOurOverlay(node.getChild(i))) return true
            }
            return false
        }
        
        for (window in windows) {
            val root = window.root
            if (root != null && window.type == android.view.accessibility.AccessibilityWindowInfo.TYPE_APPLICATION) {
                 if (root.packageName?.toString() != myPackage && root.packageName?.toString() != "com.example") {
                     if (!isOurOverlay(root)) {
                         targetRoot = root
                         break // Pick the topmost non-replify application window
                     }
                 }
            }
        }
        
        val root = targetRoot ?: rootInActiveWindow ?: return ""
        if (root.packageName?.toString() == myPackage || root.packageName?.toString() == "com.example") return ""
        
        return extractRelevantText(root)
    }

    private val timeRegex = Regex(".*\\b\\d{1,2}:\\d{2}\\b.*")

    private data class NodeData(
        val text: String,
        val bounds: android.graphics.Rect,
        val className: String,
        val parentClassName: String
    )

    private fun extractRelevantText(root: AccessibilityNodeInfo): String {
        val candidates = mutableListOf<NodeData>()
        var inputFieldTop = Int.MAX_VALUE
        val displayHeight = android.content.res.Resources.getSystem().displayMetrics.heightPixels

        fun parseNode(node: AccessibilityNodeInfo?) {
            if (node == null) return
            try {
                val className = node.className?.toString() ?: ""
                val text = node.text?.toString() ?: ""
                val desc = node.contentDescription?.toString() ?: ""
                val nodeText = if (text.isNotBlank()) text else if (desc.isNotBlank()) desc else ""
                val bounds = android.graphics.Rect()
                node.getBoundsInScreen(bounds)

                // Try to identify the chat input field to find its top boundary
                if (node.isEditable || className.contains("EditText")) {
                    if (bounds.top in (displayHeight / 4) until inputFieldTop) {
                        inputFieldTop = bounds.top
                    }
                } else if (nodeText.isNotBlank()) {
                    val lowerClean = nodeText.trim().lowercase()
                    if (lowerClean == "message" || lowerClean.contains("type a message") || lowerClean == "send sms") {
                        if (bounds.top in (displayHeight / 4) until inputFieldTop) {
                            inputFieldTop = bounds.top
                        }
                    }
                }

                if (nodeText.isNotBlank()) {
                    val clean = nodeText.trim()
                    val lowerClean = clean.lowercase()
                    val isTime = timeRegex.matches(clean) && clean.length < 15
                    val isDate = clean.matches(Regex("^(?i)(today|yesterday|monday|tuesday|wednesday|thursday|friday|saturday|sunday)$")) || 
                                 clean.matches(Regex("^\\s*\\d{1,2}\\s+[A-Za-z]+\\s*(\\d{2,4})?\\s*$")) || 
                                 clean.matches(Regex("^\\s*\\d{1,2}/\\d{1,2}/\\d{2,4}\\s*$")) ||
                                 clean.matches(Regex("^\\s*[A-Za-z]+\\s+\\d{1,2}\\s*(,\\s*\\d{2,4})?\\s*$"))
                    
                    val isSystemMessage = lowerClean.contains("messages to this chat and calls are now secured") ||
                                          lowerClean.contains("waiting for this message") ||
                                          lowerClean.contains("missed voice call") ||
                                          lowerClean.contains("missed video call")

                    val isUiLabel = lowerClean == "send" || lowerClean == "camera" || lowerClean == "attach" ||
                                    lowerClean == "voice message" || lowerClean == "unread" || lowerClean == "read" ||
                                    lowerClean == "delivered" || lowerClean == "double tap to like" ||
                                    lowerClean == "sticker" || lowerClean.contains("call") || lowerClean == "more" ||
                                    lowerClean == "search" || lowerClean == "options" || lowerClean == "navigate up" ||
                                    lowerClean == "back" || lowerClean == "type message" || lowerClean == "type an sms message" ||
                                    lowerClean == "message" || lowerClean.contains("type a message") || lowerClean == "send sms"

                    val isHeaderNumber = clean.matches(Regex("[\\d\\+\\-\\s]+")) && bounds.top < 300
                    val isSeen = lowerClean.startsWith("seen") || lowerClean.startsWith("delivered") || lowerClean.startsWith("read")
                    val isBottomUi = bounds.bottom > displayHeight - 200
                    val isInputOrButton = className.contains("Button") || className.contains("EditText") || node.isEditable || className.contains("Image")

                    if (!isTime && !isDate && !isSystemMessage && !isUiLabel && !isHeaderNumber && !isSeen && !isBottomUi && !isInputOrButton && clean.isNotEmpty()) {
                        val parentClassName = node.parent?.className?.toString() ?: ""
                        candidates.add(NodeData(clean, bounds, className, parentClassName))
                    }
                }

                for (i in 0 until node.childCount) {
                    parseNode(node.getChild(i))
                }
            } catch (e: Exception) {}
        }

        parseNode(root)

        if (candidates.isEmpty()) {
            return "No text found on screen"
        }

        // If we found an input field, prioritize messages above it. Otherwise use the bottom-most message.
        val validCandidates = if (inputFieldTop < Int.MAX_VALUE) {
            candidates.filter { it.bounds.bottom <= inputFieldTop + 50 } // allow slight overlap
        } else {
            candidates
        }

        if (validCandidates.isEmpty()) {
            return candidates.last().text
        }

        // Sort by vertical position to find the one closest to the bottom (last message)
        // Also give slight preference to typical chat bubble containers (ViewGroup/LinearLayout/FrameLayout)
        val sorted = validCandidates.sortedWith(compareBy({ it.bounds.bottom }, { 
            it.parentClassName.contains("Layout") || it.parentClassName.contains("ViewGroup") 
        }))

        return sorted.last().text
    }

    fun setReplyTextAndSend(reply: String, autoSend: Boolean) {
        var targetRoot: AccessibilityNodeInfo? = null
        val myPackage = packageName
        
        fun isOurOverlay(node: AccessibilityNodeInfo?): Boolean {
            if (node == null) return false
            if (node.text?.toString()?.contains("Replify") == true || node.text?.toString()?.contains("Generate Reply") == true) return true
            for (i in 0 until node.childCount) {
                if (isOurOverlay(node.getChild(i))) return true
            }
            return false
        }
        
        for (window in windows) {
            val root = window.root
            if (root != null && window.type == android.view.accessibility.AccessibilityWindowInfo.TYPE_APPLICATION) {
                 if (root.packageName?.toString() != myPackage && root.packageName?.toString() != "com.example") {
                     if (!isOurOverlay(root)) {
                         targetRoot = root
                         break // Pick the topmost non-replify application window
                     }
                 }
            }
        }
        
        val root = targetRoot ?: rootInActiveWindow ?: return
        if (root.packageName?.toString() == myPackage || root.packageName?.toString() == "com.example") return

        val editNode = findEditTextNode(root)
        if (editNode != null) {
            val arguments = Bundle()
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, reply)
            editNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
            
            if (autoSend) {
                val sendButton = findSendButton(root)
                sendButton?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
        }
    }

    private fun findTextNodes(node: AccessibilityNodeInfo?, outNodes: MutableList<AccessibilityNodeInfo>) {
        if (node == null) return
        if (node.text != null && node.text.isNotBlank()) {
            outNodes.add(node)
        }
        for (i in 0 until node.childCount) {
            findTextNodes(node.getChild(i), outNodes)
        }
    }

    private fun findEditTextNode(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (node == null) return null
        if (node.isEditable || node.className?.contains("EditText") == true) {
            return node
        }
        for (i in 0 until node.childCount) {
            val result = findEditTextNode(node.getChild(i))
            if (result != null) return result
        }
        return null
    }

    private fun findSendButton(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (node == null) return null
        if (node.isClickable) {
            val description = node.contentDescription?.toString()?.lowercase() ?: ""
            val text = node.text?.toString()?.lowercase() ?: ""
            if (description.contains("send") || text.contains("send")) {
                return node
            }
        }
        for (i in 0 until node.childCount) {
            val result = findSendButton(node.getChild(i))
            if (result != null) return result
        }
        return null
    }

    companion object {
        var instance: ReplifyAccessibilityService? = null
            private set
            
        val isServiceEnabled: Boolean
            get() = instance != null
    }
}
