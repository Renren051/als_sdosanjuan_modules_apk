package com.jannlawm.alssdosanjuan.ui.module

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class ModuleGameDefinition(
    val gameType: String,
    val title: String,
    val instruction: String
)

@Composable
fun ModuleLearningGameDialog(
    moduleTitle: String,
    moduleLevel: String,
    moduleSubLevel: String?,
    moduleLanguage: String?,
    onDismiss: () -> Unit,
    onCompleted: (gameType: String, score: Int, moves: Int, elapsedSeconds: Int) -> Unit
) {
    val isFilipino = moduleLanguage.equals("Filipino", ignoreCase = true) ||
        (moduleLanguage.isNullOrBlank() && moduleLevel.equals("BLP", ignoreCase = true))
    val game = remember(moduleLevel, moduleSubLevel, isFilipino) {
        when {
            moduleLevel.equals("BLP", ignoreCase = true) -> ModuleGameDefinition(
                "blp_literacy_quiz",
                if (isFilipino) "Laro ng Batayang Kasanayan" else "Basic Literacy Challenge",
                if (isFilipino) "Sagutin ang 5 tanong tungkol sa alpabeto, salita, at pantig." else "Answer 5 questions about letters, words, and syllables."
            )
            moduleLevel.equals("Elementary", ignoreCase = true) -> ModuleGameDefinition(
                "elementary_math_quiz",
                if (isFilipino) "Landas ng Bilang" else "Number Trail",
                if (isFilipino) "Sagutin ang 5 tanong sa matematika para sa antas mo." else "Answer 5 math questions for your elementary level."
            )
            else -> ModuleGameDefinition(
                "secondary_reasoning_quiz",
                if (isFilipino) "Hamon sa Isip" else "Brain Challenge",
                if (isFilipino) "Sagutin ang 3 tanong sa matematika/lohika at 2 tanong sa Ingles." else "Answer 3 math/logic questions and 2 English questions."
            )
        }
    }
    val questionBank = remember(moduleLevel, moduleSubLevel, moduleLanguage, moduleTitle) {
        ModuleGameQuestionBank.forModule(moduleLevel, moduleSubLevel, moduleLanguage)
    }
    val questions = remember(questionBank, moduleLevel, moduleTitle) {
        val isSecondary = !moduleLevel.equals("BLP", ignoreCase = true) &&
            !moduleLevel.equals("Elementary", ignoreCase = true)
        if (isSecondary) {
            val englishQuestions = questionBank.filter { it.isEnglish }.shuffled().take(2)
            val otherQuestions = questionBank.filterNot { it.isEnglish }.shuffled().take(3)
            (englishQuestions + otherQuestions).shuffled()
        } else {
            questionBank.shuffled().take(5)
        }
    }

    var questionIndex by remember(moduleTitle, game.gameType) { mutableIntStateOf(0) }
    var selectedAnswer by remember(moduleTitle, game.gameType) { mutableIntStateOf(-1) }
    var selectedAnswerIsCorrect by remember(moduleTitle, game.gameType) { mutableStateOf(false) }
    var missedCurrentQuestion by remember(moduleTitle, game.gameType) { mutableStateOf(false) }
    var correctOnFirstTry by remember(moduleTitle, game.gameType) { mutableIntStateOf(0) }
    var answerAttempts by remember(moduleTitle, game.gameType) { mutableIntStateOf(0) }
    var isAdvancing by remember(moduleTitle, game.gameType) { mutableStateOf(false) }
    var elapsedSeconds by remember(moduleTitle, game.gameType) { mutableIntStateOf(0) }
    var isComplete by remember(moduleTitle, game.gameType) { mutableStateOf(false) }
    var resultSent by remember(moduleTitle, game.gameType) { mutableStateOf(false) }
    val startedAt = remember(moduleTitle, game.gameType) { System.currentTimeMillis() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(isComplete, startedAt) {
        while (!isComplete) {
            elapsedSeconds = ((System.currentTimeMillis() - startedAt) / 1000L).toInt()
            delay(1000)
        }
    }

    LaunchedEffect(isComplete) {
        if (isComplete && !resultSent) {
            resultSent = true
            onCompleted(
                game.gameType,
                (correctOnFirstTry * 100 / questions.size).coerceIn(0, 100),
                answerAttempts.coerceAtLeast(3),
                elapsedSeconds
            )
        }
    }

    val currentQuestion = questions[questionIndex.coerceIn(0, questions.lastIndex)]

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 720.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isComplete) {
                        if (isFilipino) "Magaling!" else "Great job!"
                    } else {
                        game.title
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = moduleTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )

                if (isComplete) {
                    Text(
                        text = "${if (isFilipino) "Puntos" else "Score"}: ${(correctOnFirstTry * 100 / questions.size).coerceIn(0, 100)} / 100",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isFilipino) {
                            "$answerAttempts sagot · $elapsedSeconds segundo"
                        } else {
                            "$answerAttempts answers · $elapsedSeconds seconds"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = game.instruction,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${if (isFilipino) "Tanong" else "Question"} ${questionIndex + 1} / ${questions.size}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = currentQuestion.prompt,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    currentQuestion.options.forEachIndexed { index, option ->
                        val optionColor = when {
                            index != selectedAnswer -> MaterialTheme.colorScheme.secondaryContainer
                            selectedAnswerIsCorrect -> Color(0xFFC8E6C9)
                            else -> Color(0xFFFFCDD2)
                        }
                        Button(
                            onClick = {
                                if (selectedAnswer == -1 && !isAdvancing) {
                                    answerAttempts += 1
                                    selectedAnswer = index
                                    selectedAnswerIsCorrect = index == currentQuestion.answerIndex
                                    if (selectedAnswerIsCorrect) {
                                        if (!missedCurrentQuestion) correctOnFirstTry += 1
                                        isAdvancing = true
                                        coroutineScope.launch {
                                            delay(550)
                                            if (questionIndex == questions.lastIndex) {
                                                isComplete = true
                                            } else {
                                                questionIndex += 1
                                                selectedAnswer = -1
                                                missedCurrentQuestion = false
                                                isAdvancing = false
                                            }
                                        }
                                    } else {
                                        missedCurrentQuestion = true
                                        coroutineScope.launch {
                                            delay(450)
                                            selectedAnswer = -1
                                        }
                                    }
                                }
                            },
                            enabled = selectedAnswer == -1 && !isAdvancing,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = optionColor,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                disabledContainerColor = optionColor,
                                disabledContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Text(
                                text = option,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                if (isComplete) {
                    Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                        Text(if (isFilipino) "Magpatuloy" else "Continue")
                    }
                } else {
                    TextButton(onClick = onDismiss) {
                        Text(if (isFilipino) "Lumabas muna" else "Exit game")
                    }
                }
            }
        }
    }
}
