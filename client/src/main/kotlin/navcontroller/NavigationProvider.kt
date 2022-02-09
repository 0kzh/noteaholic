package navcontroller

import androidx.compose.runtime.Composable

class NavigationProvider(
    /**
     * NavigationHost class
     */
    val navController: NavController,
    val contents: @Composable NavigationGraphBuilder.() -> Unit
) {

    @Composable
    fun build() {
        NavigationGraphBuilder().renderContents()
    }

    inner class NavigationGraphBuilder(
        val navController: NavController = this@NavigationProvider.navController
    ) {
        @Composable
        fun renderContents() {
            this@NavigationProvider.contents(this)
        }
    }
}

@Composable
fun NavigationProvider.NavigationGraphBuilder.composable(
    route: String,
    content: @Composable () -> Unit
) {
    if (navController.currentScreen.value == route) {
        content()
    }
}