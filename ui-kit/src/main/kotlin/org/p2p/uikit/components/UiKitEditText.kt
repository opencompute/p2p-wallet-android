package org.p2p.uikit.components

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.util.AttributeSet
import org.p2p.uikit.R
import org.p2p.uikit.utils.inflateViewBinding
import org.p2p.uikit.databinding.WidgetUiKitEdittextBinding
import org.p2p.uikit.utils.focusAndShowKeyboard

private const val CORNER_RADIUS = 20f
private const val STROKE_WIDTH = 1

class UiKitEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val binding = inflateViewBinding<WidgetUiKitEdittextBinding>()

    private val bgRed = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = CORNER_RADIUS
        setColor(context.getColor(R.color.bg_snow))
        setStroke(STROKE_WIDTH, context.getColor(R.color.bg_rose))
    }

    private val bgNormal = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = CORNER_RADIUS
        setColor(context.getColor(R.color.snow))
        setStroke(STROKE_WIDTH, context.getColor(R.color.bg_rain))
    }

    init {
        val styleAttrs = context.obtainStyledAttributes(attrs, R.styleable.UiKitTextField, 0, 0)
        val labelText = styleAttrs.getString(R.styleable.UiKitTextField_labelText).orEmpty()
        if (labelText.isNotEmpty()) {
            binding.textViewLabel.text = labelText
            binding.textViewLabel.isVisible = true
        }
        val hintText = styleAttrs.getString(R.styleable.UiKitTextField_hintText).orEmpty()
        if (hintText.isNotEmpty()) {
            binding.editTextField.hint = hintText
            binding.textViewHint.isVisible = true
        }
        val textAppearance = styleAttrs.getResourceId(R.styleable.UiKitTextField_android_textAppearance, -1)
        if (textAppearance != -1) {
            binding.editTextField.setTextAppearance(textAppearance)
        }

        val text = styleAttrs.getText(R.styleable.UiKitTextField_android_text)
        if (!text.isNullOrEmpty()) {
            binding.editTextField.setText(text)
        }

        binding.inputViewContainer.background = bgNormal
        styleAttrs.recycle()
    }

    fun setText(text: String) {
        binding.editTextField.setText(text)
    }

    fun setHint(hint: String) {
        binding.editTextField.hint = hint
    }

    val text: Editable?
        get() = binding.editTextField.text

    val stringText: String?
        get() = text?.toString()

    val hint: CharSequence
        get() = binding.editTextField.hint

    val length: Int
        get() = binding.editTextField.length()

    fun addOnTextChangedListener(block: (String) -> Unit) {
        binding.editTextField.doAfterTextChanged { block(it.toString()) }
    }

    fun focusAndShowKeyboard() {
        binding.editTextField.focusAndShowKeyboard()
    }
}