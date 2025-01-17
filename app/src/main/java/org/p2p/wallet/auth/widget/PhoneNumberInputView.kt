package org.p2p.wallet.auth.widget

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.getIntOrThrow
import androidx.core.content.res.getResourceIdOrThrow
import androidx.core.view.isVisible
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.TextView
import org.p2p.core.common.TextContainer
import org.p2p.core.common.bind
import org.p2p.uikit.utils.focusAndShowKeyboard
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.auth.ui.phone.maskwatcher.PhoneNumberTextWatcher
import org.p2p.wallet.databinding.WidgetPhoneInputViewBinding
import org.p2p.wallet.utils.viewbinding.getString
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

private const val EMOJI_NO_FLAG = "️\uD83C\uDFF4"
private const val CORNER_RADIUS = 20f
private const val STROKE_WIDTH = 1
private const val PLUS_SIGN = '+'

open class PhoneNumberInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    protected val binding = inflateViewBinding<WidgetPhoneInputViewBinding>()

    private lateinit var phoneTextWatcher: PhoneNumberTextWatcher
    val phoneCodeView: TextView by lazy { binding.editTextCountryCode }
    private var viewTag: Any? = null

    private val bgRed = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = CORNER_RADIUS
        setColor(context.getColor(R.color.bg_rain))
        setStroke(STROKE_WIDTH, context.getColor(R.color.bg_rose))
    }

    private val bgNormal = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = CORNER_RADIUS
        setColor(context.getColor(R.color.rain))
        setStroke(STROKE_WIDTH, context.getColor(R.color.bg_rain))
    }

    init {
        val styleAttrs = context.obtainStyledAttributes(attrs, R.styleable.UiKitEditText, 0, 0)
        val labelText = styleAttrs.getString(R.styleable.UiKitEditText_labelText).orEmpty()
        if (labelText.isNotEmpty()) {
            binding.textViewLabel.text = labelText
            binding.textViewLabel.isVisible = true
        }
        val hintText = styleAttrs.getString(R.styleable.UiKitEditText_hintText).orEmpty()
        if (hintText.isNotEmpty()) {
            binding.textViewHint.text = hintText
            binding.textViewHint.isVisible = true
        }
        val textAppearance = styleAttrs.getResourceId(R.styleable.UiKitEditText_android_textAppearance, -1)
        if (textAppearance != -1) {
            binding.editTextPhoneNumber.setTextAppearance(textAppearance)
            binding.editTextCountryCode.setTextAppearance(textAppearance)
            binding.textViewPlusSign.setTextAppearance(textAppearance)
        }
        val text = styleAttrs.getText(R.styleable.UiKitEditText_android_text)
        if (!text.isNullOrEmpty()) {
            binding.editTextPhoneNumber.setText(text)
        }
        val backgroundTint = styleAttrs.getResourceId(R.styleable.UiKitEditText_android_backgroundTint, -1)
        if (backgroundTint != -1) {
            bgNormal.setColor(context.getColor(backgroundTint))
            bgNormal.setStroke(STROKE_WIDTH, context.getColor(R.color.bg_rain))

            bgRed.setColor(context.getColor(backgroundTint))
        }
        val textSize = styleAttrs.getDimensionPixelSize(R.styleable.UiKitEditText_android_textSize, -1)
        if (textSize != -1) {
            val minSize = 12
            val granularity = binding.autoSizeHelperTextView.autoSizeStepGranularity
            val unit = 0
            binding.autoSizeHelperTextView.setAutoSizeTextTypeUniformWithConfiguration(
                minSize,
                textSize,
                granularity,
                unit
            )
        }
        if (styleAttrs.hasValue(R.styleable.UiKitEditText_android_nextFocusDown)) {
            val nextFocusDown = styleAttrs.getResourceIdOrThrow(R.styleable.UiKitEditText_android_nextFocusDown)
            binding.editTextPhoneNumber.nextFocusDownId = nextFocusDown
        }
        if (styleAttrs.hasValue(R.styleable.UiKitEditText_android_imeOptions)) {
            val imeOptions = styleAttrs.getIntOrThrow(R.styleable.UiKitEditText_android_imeOptions)
            binding.editTextPhoneNumber.imeOptions = imeOptions
        }
        binding.inputViewContainer.background = bgNormal
        styleAttrs.recycle()
    }

    fun setText(text: String) {
        binding.editTextPhoneNumber.setText(text)
        binding.editTextPhoneNumber.setSelection(text.length)
    }

    fun setHint(hint: String) {
        binding.editTextPhoneNumber.hint = hint
    }

    val text: Editable?
        get() = binding.editTextPhoneNumber.text

    val stringText: String?
        get() = text?.toString()

    val hint: CharSequence
        get() = binding.editTextPhoneNumber.hint

    val length: Int
        get() = binding.editTextPhoneNumber.length()

    fun setupViewState(
        countryCode: CountryCode?,
        savedPhoneNumber: String? = null,
        onPhoneChanged: (String) -> Unit,
        onCountryClickListener: () -> Unit,
        requestFocus: Boolean = true
    ) = with(binding) {
        countryCode?.phoneCode.let { editTextCountryCode.text = it }

        val flagEmoji = countryCode?.flagEmoji ?: EMOJI_NO_FLAG
        textViewFlagEmoji.text = flagEmoji

        val hint = countryCode?.getZeroFilledMask().orEmpty()
        val restoredNumber = editTextPhoneNumber.text.toString()

        editTextPhoneNumber.setHintText(hint)

        countryPickerView.setOnClickListener {
            onCountryClickListener.invoke()
        }

        val originalTextSize = editTextPhoneNumber.textSize
        editTextPhoneNumber.setTextSize(TypedValue.COMPLEX_UNIT_PX, originalTextSize)

        val focusView = if (countryCode == null) editTextCountryCode else editTextPhoneNumber
        if (requestFocus) {
            focusView.focusAndShowKeyboard()
        }
        editTextPhoneNumber.setSelection(restoredNumber.length)

        phoneTextWatcher = PhoneNumberTextWatcher(binding.editTextPhoneNumber) { phoneNumber ->
            resizeInputs(phoneNumber, originalTextSize)

            // Invoking callbacks
            val phone = phoneNumber.getFullPhoneNumber()
            onPhoneChanged.invoke(phone)
        }
        editTextPhoneNumber.addTextChangedListener(phoneTextWatcher)

        if (restoredNumber.isNotEmpty()) {
            editTextPhoneNumber.setText(savedPhoneNumber)
            onPhoneChanged(restoredNumber.getFullPhoneNumber())
        }
    }

    private fun WidgetPhoneInputViewBinding.resizeInputs(
        phoneNumber: String,
        originalTextSize: Float
    ) {
        // Use invisible auto size textView to handle editText text size
        autoSizeHelperTextView.setText(phoneNumber, TextView.BufferType.EDITABLE)
        val textSize = if (phoneNumber.isEmpty()) originalTextSize else autoSizeHelperTextView.textSize
        editTextPhoneNumber.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        editTextCountryCode.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        textViewPlusSign.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
    }

    fun updateViewState(countryCode: CountryCode?) = with(binding) {
        if (countryCode == null) {
            showError(getString(R.string.error_country_not_found))
            textViewFlagEmoji.text = EMOJI_NO_FLAG
            return@with
        }
        textViewFlagEmoji.text = countryCode.flagEmoji

        editTextCountryCode.text = countryCode.phoneCode
        val hint = countryCode.getZeroFilledMask()
        editTextPhoneNumber.setHintText(hint)

        with(editTextPhoneNumber) {
            setHintText(countryCode.getZeroFilledMask())
            setSelection(length())
            focusAndShowKeyboard()
        }
        showError(text = null)
    }

    fun showError(text: String?) = with(binding) {
        textViewError.text = text
        textViewError.isVisible = !text.isNullOrEmpty()
        inputViewContainer.background = if (!text.isNullOrEmpty()) bgRed else bgNormal
    }

    fun showError(textContainer: TextContainer?) = with(binding) {
        textContainer?.let { textViewError.bind(it) }
        textViewError.isVisible = textContainer != null
        inputViewContainer.background = if (textContainer != null) bgRed else bgNormal
    }

    fun onFoundNewCountry(countryCode: CountryCode) {
        // TODO implement if need find country outside the mask
    }

    private fun String.getFullPhoneNumber(): String {
        return PLUS_SIGN + binding.editTextCountryCode.text.toString().trim() + this.replace(" ", "")
    }

    fun focusAndShowKeyboard() {
        binding.editTextPhoneNumber.focusAndShowKeyboard()
    }

    fun setViewTag(tag: Any?) {
        binding.root.tag = tag
        viewTag = tag
    }
}
