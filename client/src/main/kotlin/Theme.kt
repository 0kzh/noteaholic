import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.sp

val InterFontFamily = FontFamily(
    Font(
        resource = "font/inter_bold.ttf",
        weight = FontWeight.Bold,
        style = FontStyle.Normal
    ),
    Font(
        resource = "font/inter_medium.ttf",
        weight = FontWeight.W500,
        style = FontStyle.Normal
    ),
    Font(
        resource = "font/inter_regular.ttf",
        weight = FontWeight.W400,
        style = FontStyle.Normal
    ),
)

val CustomTypography = Typography(
    h1 = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp
    ),
    h2 = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp
    ),
    h3 = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    ),
    h4 = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.W500,
        fontSize = 18.sp
    ),
    h5 = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.W500,
        fontSize = 16.sp
    ),
    h6 = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.W500,
        fontSize = 14.sp
    ),
    caption = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.W500,
        fontSize = 12.sp
    ),
)