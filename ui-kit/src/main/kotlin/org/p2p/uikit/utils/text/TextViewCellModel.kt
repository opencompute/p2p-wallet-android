package org.p2p.uikit.utils.text

import androidx.annotation.ColorRes
import androidx.annotation.Px
import androidx.annotation.StyleRes
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.widget.TextView
import org.p2p.core.common.TextContainer
import org.p2p.core.utils.insets.InitialViewPadding
import org.p2p.core.utils.orZero
import org.p2p.uikit.R
import org.p2p.uikit.utils.drawable.DrawableCellModel
import org.p2p.uikit.utils.drawable.applyBackground
import org.p2p.uikit.utils.drawable.shape.shapeRoundedAll
import org.p2p.uikit.utils.drawable.shapeDrawable
import org.p2p.uikit.utils.getColorStateList
import org.p2p.uikit.utils.skeleton.SkeletonCellModel
import org.p2p.uikit.utils.skeleton.bindSkeleton
import org.p2p.uikit.utils.toPx

sealed interface TextViewCellModel {

    data class Raw(
        val text: TextContainer,
        @StyleRes val textAppearance: Int? = null,
        @ColorRes val textColor: Int? = null,
        val textSize: TextViewSize? = null,
        val gravity: Int? = null,
        val badgeBackground: TextViewBackgroundModel? = null,
    ) : TextViewCellModel

    data class Skeleton(
        val skeleton: SkeletonCellModel,
    ) : TextViewCellModel
}

data class TextViewSize(
    val textSize: Float,
    val typedValue: Int = TypedValue.COMPLEX_UNIT_SP
)

data class TextViewBackgroundModel(
    val background: DrawableCellModel = badgeRounded(),
    val padding: InitialViewPadding = badgePadding()
)

fun badgePadding(
    @Px left: Int = 8.toPx(),
    @Px top: Int = 0.toPx(),
    @Px right: Int = 8.toPx(),
    @Px bottom: Int = 0.toPx(),
): InitialViewPadding = InitialViewPadding(left, top, right, bottom)

fun badgeRounded(
    @Px cornerSize: Float = 32f.toPx(),
    @ColorRes tint: Int = R.color.elements_lime,
): DrawableCellModel = DrawableCellModel(
    drawable = shapeDrawable(shapeRoundedAll(cornerSize)),
    tint = tint,
)

fun TextView.bindOrGone(model: TextViewCellModel?) {
    this.isVisible = model != null
    if (model != null) bind(model)
}

fun TextView.bind(model: TextViewCellModel) {
    when (model) {
        is TextViewCellModel.Raw -> bind(model)
        is TextViewCellModel.Skeleton -> bindSkeleton(model)
    }
}

fun TextView.bind(model: TextViewCellModel.Raw) {
    val initialTextStyle = saveAndGetInitialTextStyle()
    model.textAppearance?.let { setTextAppearance(it) }
        ?: kotlin.run {
            typeface = initialTextStyle.typeface
            letterSpacing = initialTextStyle.letterSpacing
        }
    model.textColor?.let { setTextColor(getColorStateList(it)) }
        ?: kotlin.run { setTextColor(initialTextStyle.textColors) }

    model.textSize?.let { setTextSize(it.typedValue, it.textSize) }
        ?: kotlin.run { setTextSize(TypedValue.COMPLEX_UNIT_PX, initialTextStyle.textSize) }
    gravity = model.gravity ?: initialTextStyle.gravity
    model.badgeBackground?.background?.applyBackground(this)
        ?: kotlin.run {
            background = initialTextStyle.background
            backgroundTintList = initialTextStyle.backgroundTint
        }
    setHintTextColor(initialTextStyle.hintTextColors)
    foreground = null
    foregroundTintList = null
    updatePadding(
        left = model.badgeBackground?.padding?.left.orZero(),
        top = model.badgeBackground?.padding?.top.orZero(),
        right = model.badgeBackground?.padding?.right.orZero(),
        bottom = model.badgeBackground?.padding?.bottom.orZero(),
    )
    model.text.applyTo(this)
}

fun TextView.bindSkeleton(model: TextViewCellModel.Skeleton) {
    saveAndGetInitialTextStyle()
    val transparent = context.getColorStateList(android.R.color.transparent)
    setTextColor(transparent)
    setHintTextColor(transparent)
    text = ""
    bindSkeleton(model.skeleton)
}

private fun TextView.saveAndGetInitialTextStyle(): InitialTextStyle {
    val tagKey = R.id.initial_text_style_tag_id
    return getTag(tagKey) as? InitialTextStyle ?: let {
        val initialTextStyle = InitialTextStyle(this)
        setTag(tagKey, initialTextStyle)
        initialTextStyle
    }
}

private data class InitialTextStyle(
    @Px val textSize: Float,
    val textColors: ColorStateList?,
    val hintTextColors: ColorStateList?,
    val letterSpacing: Float,
    val typeface: Typeface?,
    val background: Drawable?,
    val backgroundTint: ColorStateList?,
    val gravity: Int,
) {
    constructor(textView: TextView) : this(
        textSize = textView.textSize,
        letterSpacing = textView.letterSpacing,
        textColors = textView.textColors,
        typeface = textView.typeface,
        background = textView.background,
        backgroundTint = textView.backgroundTintList,
        gravity = textView.gravity,
        hintTextColors = textView.hintTextColors,
    )
}