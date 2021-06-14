import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntSize
import dev.msfjarvis.claw.common.posts.LobstersItem
import dev.msfjarvis.claw.common.posts.TEST_POST
import dev.msfjarvis.claw.common.theme.LobstersTheme
import dev.msfjarvis.claw.common.urllauncher.UrlLauncher
import org.pushingpixels.aurora.auroraBackground
import org.pushingpixels.aurora.skin.marinerSkin
import org.pushingpixels.aurora.window.AuroraWindow


fun main() = AuroraWindow(
  skin = marinerSkin(),
  title = "",
  size = IntSize(300, 100),
  undecorated = true,
) {
  val urlLauncher = UrlLauncher()
  LobstersTheme(darkTheme = false) {
    Box(
      modifier = Modifier.fillMaxSize().auroraBackground(),
    ) {
      val stateVertical = rememberScrollState(0)
      Box(
        modifier = Modifier.fillMaxSize().verticalScroll(stateVertical),
      ) {
        Column {
          repeat(10) {
            LobstersItem(
              post = TEST_POST,
              isSaved = false,
              viewPost = { urlLauncher.launch(TEST_POST.url.ifEmpty { TEST_POST.commentsUrl }) },
              viewComments = { urlLauncher.launch(TEST_POST.commentsUrl) },
              toggleSave = {},
            )
          }
        }
      }
      VerticalScrollbar(
        modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
        adapter = rememberScrollbarAdapter(stateVertical),
      )
    }
  }
}
