package dev.msfjarvis.lobsters.ui.posts

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumnForIndexed
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.msfjarvis.lobsters.model.LobstersPost
import dev.msfjarvis.lobsters.ui.urllauncher.UrlLauncherAmbient

@Composable
fun HottestPosts(
  posts: List<LobstersPost>,
  listState: LazyListState,
  modifier: Modifier = Modifier,
  saveAction: (LobstersPost) -> Unit,
  overscrollAction: () -> Unit,
) {
  val urlLauncher = UrlLauncherAmbient.current

  if (posts.isEmpty()) {
    EmptyList(saved = false)
  } else {
    LazyColumnForIndexed(
      items = posts,
      state = listState,
      modifier = Modifier.padding(horizontal = 8.dp).then(modifier)
    ) { index, item ->
      if (posts.lastIndex == index) {
        overscrollAction.invoke()
      }
      LobstersItem(
        post = item,
        linkOpenAction = { post -> urlLauncher.launch(post.url.ifEmpty { post.commentsUrl }) },
        commentOpenAction = { post -> urlLauncher.launch(post.commentsUrl) },
        saveAction = saveAction,
      )
    }
  }
}