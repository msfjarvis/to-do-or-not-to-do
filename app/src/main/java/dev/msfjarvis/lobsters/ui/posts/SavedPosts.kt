package dev.msfjarvis.lobsters.ui.posts

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.msfjarvis.lobsters.model.LobstersPost
import dev.msfjarvis.lobsters.ui.urllauncher.UrlLauncherAmbient

@Composable
fun SavedPosts(
  posts: List<LobstersPost>,
  modifier: Modifier = Modifier,
  saveAction: (LobstersPost) -> Unit
) {
  val listState = rememberLazyListState()
  val urlLauncher = UrlLauncherAmbient.current

  if (posts.isEmpty()) {
    EmptyList(saved = true)
  } else {
    LazyColumnFor(
      items = posts,
      state = listState,
      modifier = Modifier.padding(horizontal = 8.dp).then(modifier)
    ) { item ->
      LobstersItem(
        post = item,
        linkOpenAction = { post -> urlLauncher.launch(post.url.ifEmpty { post.commentsUrl }) },
        commentOpenAction = { post -> urlLauncher.launch(post.commentsUrl) },
        saveAction = saveAction,
      )
    }
  }
}