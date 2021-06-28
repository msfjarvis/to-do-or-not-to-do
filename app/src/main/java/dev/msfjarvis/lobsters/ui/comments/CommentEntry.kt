package dev.msfjarvis.lobsters.ui.comments

import android.graphics.Typeface
import android.os.Build
import android.text.Html
import android.text.Layout
import android.text.style.AbsoluteSizeSpan
import android.text.style.AlignmentSpan
import android.text.style.BackgroundColorSpan
import android.text.style.BulletSpan
import android.text.style.DrawableMarginSpan
import android.text.style.ForegroundColorSpan
import android.text.style.IconMarginSpan
import android.text.style.ImageSpan
import android.text.style.LeadingMarginSpan
import android.text.style.LineHeightSpan
import android.text.style.LocaleSpan
import android.text.style.QuoteSpan
import android.text.style.RelativeSizeSpan
import android.text.style.ScaleXSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.SubscriptSpan
import android.text.style.SuggestionSpan
import android.text.style.SuperscriptSpan
import android.text.style.TextAppearanceSpan
import android.text.style.TypefaceSpan
import android.text.style.URLSpan
import android.text.style.UnderlineSpan
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextGeometricTransform
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import dev.msfjarvis.lobsters.model.Comment
import dev.msfjarvis.lobsters.ui.posts.SubmitterName
import dev.msfjarvis.lobsters.utils.Strings
import dev.msfjarvis.lobsters.utils.get

@Composable
fun CommentEntry(
  comment: Comment,
) {
  val indentLevel = comment.indentLevel.toInt() - 1

  Row(modifier = Modifier.height(IntrinsicSize.Min)) {
    CommentTreeColors(indentLevel = indentLevel)
    Column(modifier = Modifier.padding(bottom = 4.dp, end = 8.dp, start = 12.dp, top = 4.dp)) {
      CompositionLocalProvider(LocalTextStyle provides TextStyle(fontWeight = FontWeight.Bold)) {
        SubmitterName(
          text = comment.user.username,
          avatarUrl = comment.user.avatarUrl,
          contentDescription = Strings.AvatarContentDescription.get(comment.user.username),
        )
      }
      Text(
        text = getAnnotatedStringFromHtml(comment.comment),
        modifier = Modifier.padding(top = 8.dp)
      )
    }
  }
}

@Composable
private fun CommentTreeColors(
  indentLevel: Int,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier) {
    for (level in 1..indentLevel) {
      Box(
        modifier =
          Modifier.padding(start = (level * 12).dp)
            .fillMaxHeight()
            .width(1.dp)
            .background(CommentTreeColor[level % CommentTreeColor.size])
      )
    }
  }
}

@Suppress("Deprecation")
@Composable
fun getAnnotatedStringFromHtml(text: String): AnnotatedString {
  val spannableString = Html.fromHtml(text)
  val indices = mutableListOf<Triple<Int, Int, Array<Any>>>()
  var next = -1

  for (i in spannableString.indices) {
    if (i < next) continue

    next = spannableString.nextSpanTransition(i, spannableString.length, Any::class.java)

    val spans = spannableString.getSpans(i, next, Any::class.java)

    indices.add(Triple(i, next, spans))
  }

  return buildAnnotatedString {
    indices.forEach { index ->
      val spans = index.third

      spans.forEach { span ->
        getParagraphStyleForSpan(span)?.let { nonNullSpan -> pushStyle(nonNullSpan) }
        getSpanStyleForSpan(span)?.let { nonNullSpan -> pushStyle(nonNullSpan) }
      }

      append(spannableString.substring(index.first, index.second))

      spans.forEach { _ -> pop() }
    }
  }
}

@Composable
inline fun <reified T> getParagraphStyleForSpan(span: T): ParagraphStyle? {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
    return when (span) {
      is LineHeightSpan.Standard -> {
        with(LocalDensity.current) { ParagraphStyle(lineHeight = span.height.toSp()) }
      }
      else -> null
    }
  }

  return when (span) {
    is LeadingMarginSpan.Standard -> {
      val density: Density = LocalDensity.current

      with(density) {
        val first = span.getLeadingMargin(true).toSp()
        val rest = span.getLeadingMargin(false).toSp()

        ParagraphStyle(textIndent = TextIndent(first, rest))
      }
    }
    is AlignmentSpan.Standard -> {
      when (span.alignment) {
        Layout.Alignment.ALIGN_NORMAL -> ParagraphStyle(textAlign = TextAlign.Start)
        Layout.Alignment.ALIGN_OPPOSITE -> ParagraphStyle(textAlign = TextAlign.End)
        Layout.Alignment.ALIGN_CENTER -> ParagraphStyle(textAlign = TextAlign.Center)
        null -> null
      }
    }
    else -> null
  }
}

@Composable
inline fun <reified T> getSpanStyleForSpan(span: T): SpanStyle? {
  return when (span) {
    is BackgroundColorSpan -> SpanStyle(background = Color(span.backgroundColor))
    is URLSpan -> SpanStyle(textDecoration = TextDecoration.Underline, color = Color.Blue)
    is ForegroundColorSpan -> SpanStyle(color = Color(span.foregroundColor))
    is StrikethroughSpan -> SpanStyle(textDecoration = TextDecoration.LineThrough)
    is SuggestionSpan -> SpanStyle(textDecoration = TextDecoration.LineThrough)
    is UnderlineSpan -> SpanStyle(textDecoration = TextDecoration.Underline)
    is AbsoluteSizeSpan -> SpanStyle(textGeometricTransform = TextGeometricTransform())
    is LocaleSpan -> {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        SpanStyle(localeList = LocaleList(span.locales.toLanguageTags()))
      } else {
        TODO("VERSION.SDK_INT < N")
      }
    }
    is RelativeSizeSpan -> SpanStyle(textGeometricTransform = TextGeometricTransform())
    is ImageSpan -> TODO()
    is ScaleXSpan -> SpanStyle(textGeometricTransform = TextGeometricTransform(span.scaleX))
    is StyleSpan -> {
      when (span.style) {
        Typeface.NORMAL -> SpanStyle(fontStyle = FontStyle.Normal)
        Typeface.ITALIC ->
          SpanStyle(fontStyle = FontStyle.Italic, fontSynthesis = FontSynthesis.Style)
        Typeface.BOLD ->
          SpanStyle(fontWeight = FontWeight.Bold, fontSynthesis = FontSynthesis.Weight)
        Typeface.BOLD_ITALIC ->
          SpanStyle(
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Bold,
            fontSynthesis = FontSynthesis.All
          )
        else -> null
      }
    }
    is SubscriptSpan -> SpanStyle(baselineShift = BaselineShift.Subscript)
    is SuperscriptSpan -> SpanStyle(baselineShift = BaselineShift.Superscript)
    is TextAppearanceSpan -> TODO()
    is TypefaceSpan -> {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        when (span.typeface) {
          Typeface.DEFAULT -> SpanStyle(fontFamily = FontFamily.Default)
          Typeface.SANS_SERIF -> SpanStyle(fontFamily = FontFamily.SansSerif)
          Typeface.SERIF -> SpanStyle(fontFamily = FontFamily.Serif)
          Typeface.MONOSPACE -> SpanStyle(fontFamily = FontFamily.Monospace)
          else -> null
        }
      } else {
        null
      }
    }
    is BulletSpan -> SpanStyle(textDecoration = TextDecoration.Underline)
    is DrawableMarginSpan -> TODO()
    is IconMarginSpan -> TODO()
    is QuoteSpan -> SpanStyle(background = Color.LightGray)
    else -> null
  }
}
