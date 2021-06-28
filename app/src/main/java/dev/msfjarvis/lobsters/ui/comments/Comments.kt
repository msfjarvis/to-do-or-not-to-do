package dev.msfjarvis.lobsters.ui.comments

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.msfjarvis.lobsters.R
import dev.msfjarvis.lobsters.model.LobstersPostDetails
import dev.msfjarvis.lobsters.ui.posts.PostTitle
import dev.msfjarvis.lobsters.ui.posts.SubmitterName
import dev.msfjarvis.lobsters.ui.posts.TagRow
import dev.msfjarvis.lobsters.ui.urllauncher.LocalUrlLauncher
import dev.msfjarvis.lobsters.util.IconResource
import dev.msfjarvis.lobsters.utils.Strings
import dev.msfjarvis.lobsters.utils.get

private const val ScrollDelta = 50
private const val AnimationDuration = 100

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun CommentsPageInternal(
  details: LobstersPostDetails,
  paddingValues: PaddingValues,
  modifier: Modifier = Modifier,
) {
  val urlLauncher = LocalUrlLauncher.current
  var isFabVisible by remember { mutableStateOf(true) }

  val nestedScrollConnection = remember {
    object : NestedScrollConnection {
      override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        val delta = available.y

        if (delta > ScrollDelta) {
          isFabVisible = true
        } else if (delta < -ScrollDelta) {
          isFabVisible = false
        }

        // We didn't consume any offset here so return Offset.Zero
        return Offset.Zero
      }
    }
  }

  Scaffold(
    modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding()),
    floatingActionButton = {
      AnimatedVisibility(
        visible = isFabVisible,
        enter =
          slideInVertically(
            // Enters by sliding up from offset 0 to fullHeight.
            initialOffsetY = { fullHeight -> fullHeight },
            animationSpec =
              tween(durationMillis = AnimationDuration, easing = LinearOutSlowInEasing),
          ),
        exit =
          slideOutVertically(
            // Exits by sliding up from offset 0 to -fullHeight.
            targetOffsetY = { fullHeight -> fullHeight },
            animationSpec =
              tween(durationMillis = AnimationDuration, easing = FastOutLinearInEasing),
          )
      ) {
        // Post links from lobste.rs are of the form $baseUrl/s/$postId/$postTitle
        // Interestingly, lobste.rs does not actually care for the value of $postTitle, and will
        // happily accept both a missing as well as a completely arbitrary $postTitle. We
        // leverage this to create a new URL format which looks like
        // $baseUrl/s/$postId/$postTitle/r,
        // and does not trigger our deeplinks, instead opening in the custom tab as we want it to.
        FloatingActionButton(
          onClick = { urlLauncher.launch(details.commentsUrl.replaceAfterLast('/', "r")) }
        ) {
          IconResource(
            resourceId = R.drawable.ic_reply_24dp,
            contentDescription = Strings.ReplyButtonContentDescription.get()
          )
        }
      }
    }
  ) {
    LazyColumn(Modifier.nestedScroll(nestedScrollConnection).then(modifier)) {
      item { CommentsHeader(postDetails = details) }

      item { Spacer(modifier = Modifier.height(8.dp)) }

      item { Divider(color = Color.Gray.copy(0.4f)) }

      items(details.comments) { CommentEntry(it) }
    }
  }
}

@Suppress("UNCHECKED_CAST")
@Composable
fun CommentsPage(
  postId: String,
  paddingValues: PaddingValues,
  scaffoldState: ScaffoldState,
  getDetails: suspend (String) -> LobstersPostDetails,
) {
  var postDetails: NetworkState by remember { mutableStateOf(NetworkState.Loading) }

  LaunchedEffect(postId) {
    postDetails =
      try {
        NetworkState.Success(getDetails(postId))
      } catch (e: Throwable) {
        NetworkState.Error(e.message ?: "Failed to load posts")
      }
  }

  when (postDetails) {
    is NetworkState.Success<*> -> {
      CommentsPageInternal(
        details = (postDetails as NetworkState.Success<LobstersPostDetails>).data,
        paddingValues = paddingValues,
      )
    }
    is NetworkState.Error -> {
      LaunchedEffect(scaffoldState.snackbarHostState) {
        scaffoldState.snackbarHostState.showSnackbar(
          message = (postDetails as NetworkState.Error).message,
        )
      }
    }
    NetworkState.Loading -> ProgressBar(paddingValues.calculateBottomPadding())
  }
}

@Composable
private fun CommentsHeader(postDetails: LobstersPostDetails) {
  Column(
    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
  ) {
    PostTitle(
      title = postDetails.title,
      modifier = Modifier.padding(bottom = 4.dp),
    )
    Row {
      TagRow(
        tags = postDetails.tags,
      )
      Spacer(
        modifier = Modifier.weight(1f),
      )
    }

    if (postDetails.description.isNotEmpty()) {
      Spacer(modifier = Modifier.height(16.dp))
      Text(text = getAnnotatedStringFromHtml(postDetails.description))
    } else {
      Spacer(modifier = Modifier.height(12.dp))
    }

    SubmitterName(
      text = postDetails.submitter.username,
      avatarUrl = postDetails.submitter.avatarUrl,
      contentDescription = Strings.AvatarContentDescription.get(postDetails.submitter.username),
    )
  }
}

@Composable
private fun ProgressBar(bottomPadding: Dp) {
  Box(
    modifier = Modifier.padding(bottom = bottomPadding).fillMaxSize(),
    contentAlignment = Alignment.Center
  ) { CircularProgressIndicator(color = MaterialTheme.colors.secondary) }
}
