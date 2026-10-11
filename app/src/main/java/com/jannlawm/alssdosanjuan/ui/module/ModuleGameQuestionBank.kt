package com.jannlawm.alssdosanjuan.ui.module

import java.util.Locale
import kotlin.random.Random

internal data class ModuleGameQuestion(
    val prompt: String,
    val options: List<String>,
    val answerIndex: Int,
    val isEnglish: Boolean = false
)

internal object ModuleGameQuestionBank {
    fun forModule(level: String, subLevel: String?, language: String?): List<ModuleGameQuestion> {
        val isFilipino = language.equals("Filipino", ignoreCase = true) ||
            (language.isNullOrBlank() && level.equals("BLP", ignoreCase = true))
        val questions = when {
            level.equals("BLP", ignoreCase = true) -> blpQuestions(isFilipino)
            level.equals("Elementary", ignoreCase = true) -> elementaryQuestions(
                isLowerElementary = subLevel.equals("Lower Elementary", ignoreCase = true),
                isFilipino = isFilipino
            )
            else -> secondaryQuestions(isFilipino)
        }
        // Keep an incomplete bank from crashing the viewer when the game opens.
        // The generated banks below are maintained at 100 questions per program.
        return questions.shuffled().take(100)
    }

    private fun blpQuestions(isFilipino: Boolean): List<ModuleGameQuestion> {
        val alphabet = if (isFilipino) {
            listOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "Ñ", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z")
        } else {
            ('A'..'Z').map(Char::toString)
        }
        val vowels = setOf("A", "E", "I", "O", "U")
        val words = if (isFilipino) {
            listOf(
                "araw", "aso", "aklat", "bola", "bata", "bahay", "bundok", "damit", "daga", "dahon",
                "gulay", "gatas", "isda", "ilaw", "ibon", "kaibigan", "kamay", "libro", "lupa", "mata",
                "nanay", "paaralan", "papel", "puno", "tubig"
            )
        } else {
            listOf(
                "apple", "ant", "ball", "bag", "bird", "book", "cake", "cat", "cow", "desk",
                "dog", "door", "egg", "fish", "flower", "goat", "hat", "house", "ice", "key",
                "kite", "lamp", "leaf", "milk", "moon", "nose", "pen", "pig", "sun"
            )
        }
        val syllableWords = if (isFilipino) {
            listOf(
                "araw" to 2, "aso" to 2, "bahay" to 2, "bata" to 2, "bundok" to 2,
                "dahon" to 2, "guro" to 2, "isda" to 2, "ibon" to 2, "kamisa" to 3,
                "kaibigan" to 4, "laro" to 2, "libro" to 2, "mata" to 2, "nanay" to 2,
                "paaralan" to 4, "papel" to 2, "puno" to 2, "saging" to 2, "tubig" to 2,
                "lapis" to 2, "mesa" to 2
            )
        } else {
            listOf(
                "apple" to 2, "baby" to 2, "banana" to 3, "basket" to 2, "beautiful" to 3,
                "bird" to 1, "book" to 1, "candle" to 2, "carrot" to 2, "chicken" to 2,
                "computer" to 3, "family" to 3, "flower" to 2, "garden" to 2, "happy" to 2,
                "market" to 2, "orange" to 2, "paper" to 2, "pencil" to 2, "tomato" to 3
            )
        }
        val questions = mutableListOf<ModuleGameQuestion>()

        alphabet.forEach { letter ->
            val answer = if (letter in vowels) {
                if (isFilipino) "Patinig" else "Vowel"
            } else {
                if (isFilipino) "Katinig" else "Consonant"
            }
            questions += question(
                englishPrompt = "Is the letter $letter a vowel or consonant?",
                filipinoPrompt = "Ang titik $letter ba ay patinig o katinig?",
                answer = answer,
                distractors = listOf(if (answer == "Patinig" || answer == "Vowel") {
                    if (isFilipino) "Katinig" else "Consonant"
                } else {
                    if (isFilipino) "Patinig" else "Vowel"
                }),
                isFilipino = isFilipino
            )
        }

        for (index in 0 until alphabet.lastIndex) {
            val letter = alphabet[index]
            val answer = alphabet[index + 1]
            val distractors = listOf(2, 3, 4)
                .map { alphabet[(index + 1 + it) % alphabet.size] }
                .distinct()
                .filterNot { it == answer }
            questions += question(
                englishPrompt = "Which letter comes after $letter?",
                filipinoPrompt = "Anong titik ang kasunod ng $letter?",
                answer = answer,
                distractors = distractors,
                isFilipino = isFilipino
            )
        }

        words.forEach { word ->
            val answer = word.first().uppercaseChar().toString()
            val distractors = alphabet.filterNot { it == answer }.shuffled().take(3)
            questions += question(
                englishPrompt = "What is the first letter in “$word”?",
                filipinoPrompt = "Ano ang unang titik ng salitang “$word”?",
                answer = answer,
                distractors = distractors,
                isFilipino = isFilipino
            )
        }

        syllableWords.forEach { (word, count) ->
            val answer = count.toString()
            val distractors = (1..5).filterNot { it == count }.shuffled().take(3).map(Int::toString)
            questions += question(
                englishPrompt = "How many syllables are in “$word”?",
                filipinoPrompt = "Ilang pantig mayroon ang salitang “$word”?",
                answer = answer,
                distractors = distractors,
                isFilipino = isFilipino
            )
        }
        return questions
    }

    private fun elementaryQuestions(isLowerElementary: Boolean, isFilipino: Boolean): List<ModuleGameQuestion> {
        val questions = mutableListOf<ModuleGameQuestion>()
        if (isLowerElementary) {
            questions += randomQuestions(25) {
                val first = Random.nextInt(1, 41)
                val second = Random.nextInt(1, 41)
                val answer = first + second
                question("$first + $second = ?", "$first + $second = ?", answer.toString(), numericDistractors(answer), isFilipino)
            }
            questions += randomQuestions(25) {
                val first = Random.nextInt(20, 101)
                val second = Random.nextInt(1, first)
                val answer = first - second
                question("$first − $second = ?", "$first − $second = ?", answer.toString(), numericDistractors(answer), isFilipino)
            }
            questions += randomQuestions(25) {
                val first = Random.nextInt(2, 13)
                val second = Random.nextInt(2, 13)
                val answer = first * second
                question("$first × $second = ?", "$first × $second = ?", answer.toString(), numericDistractors(answer), isFilipino)
            }
            questions += randomQuestions(25) {
                val groups = Random.nextInt(2, 13)
                val objects = Random.nextInt(2, 11)
                val answer = groups * objects
                question(
                    englishPrompt = "There are $groups bags with $objects marbles in each. How many marbles?",
                    filipinoPrompt = "May $groups supot na may tig-$objects holen. Ilang holen lahat?",
                    answer = answer.toString(),
                    distractors = numericDistractors(answer),
                    isFilipino = isFilipino
                )
            }
        } else {
            questions += randomQuestions(25) {
                val denominator = Random.nextInt(4, 21)
                val firstNumerator = Random.nextInt(1, denominator / 2 + 1)
                val secondNumerator = Random.nextInt(1, denominator - firstNumerator + 1)
                val answerNumerator = firstNumerator + secondNumerator
                question(
                    "$firstNumerator/$denominator + $secondNumerator/$denominator = ?",
                    "$firstNumerator/$denominator + $secondNumerator/$denominator = ?",
                    "$answerNumerator/$denominator",
                    listOf(
                        "${(answerNumerator - 1).coerceAtLeast(1)}/$denominator",
                        "${answerNumerator + 1}/$denominator",
                        "$answerNumerator/${denominator * 2}"
                    ),
                    isFilipino
                )
            }
            questions += randomQuestions(25) {
                val percent = Random.nextInt(10, 61)
                val whole = Random.nextInt(100, 1001)
                val answer = percent * whole / 100.0
                val formattedAnswer = formatNumber(answer)
                val distractors = listOf(-10.0, 10.0, 20.0).map { formatNumber(answer + it) }
                question(
                    "What is $percent% of $whole?",
                    "Magkano ang $percent% ng $whole?",
                    formattedAnswer,
                    distractors,
                    isFilipino
                )
            }
            questions += randomQuestions(25) {
                val firstCents = Random.nextInt(100, 1000)
                val secondCents = Random.nextInt(100, 1000)
                val answer = firstCents + secondCents
                val prompt = "${formatMoney(firstCents)} + ${formatMoney(secondCents)} = ?"
                val wrong = listOf(answer - 25, answer + 25, answer + 100).map(::formatMoney)
                question(prompt, prompt, formatMoney(answer), wrong, isFilipino)
            }
            questions += randomQuestions(25) {
                val length = Random.nextInt(4, 31)
                val width = Random.nextInt(3, 21)
                val area = length * width
                question(
                    "A rectangle is $length cm long and $width cm wide. What is its area?",
                    "Ang parihaba ay $length cm ang haba at $width cm ang lapad. Ano ang lawak nito?",
                    "$area cm²",
                    numericDistractors(area).map { "$it cm²" },
                    isFilipino
                )
            }
        }
        return questions
    }

    private fun secondaryQuestions(isFilipino: Boolean): List<ModuleGameQuestion> {
        val questions = mutableListOf<ModuleGameQuestion>()
        questions += randomQuestions(25) {
            val coefficient = Random.nextInt(2, 10)
            val answer = Random.nextInt(1, 26)
            val constant = Random.nextInt(2, 51)
            val total = coefficient * answer + constant
            question(
                "Solve: ${coefficient}x + $constant = $total",
                "Lutasin: ${coefficient}x + $constant = $total",
                answer.toString(),
                numericDistractors(answer),
                isFilipino
            )
        }
        questions += randomQuestions(25) {
            val percent = Random.nextInt(10, 91)
            val whole = Random.nextInt(6, 31) * 100
            val answer = percent * whole / 100
            question(
                "What is $percent% of $whole?",
                "Magkano ang $percent% ng $whole?",
                answer.toString(),
                numericDistractors(answer),
                isFilipino
            )
        }
        questions += secondaryEnglishGrammarQuestions()
        questions += secondaryEnglishVocabularyQuestions()
        return questions
    }

    private fun secondaryEnglishGrammarQuestions(): List<ModuleGameQuestion> {
        val subjects = listOf(
            "She" to true,
            "He" to true,
            "My teacher" to true,
            "They" to false,
            "We" to false
        )
        val activities = listOf(
            "to school" to listOf("walk", "walks", "walked", "walking"),
            "English" to listOf("study", "studies", "studied", "studying"),
            "books to class" to listOf("carry", "carries", "carried", "carrying"),
            "reading" to listOf("practice", "practices", "practiced", "practicing"),
            "after class" to listOf("dance", "dances", "danced", "dancing")
        )

        return subjects.flatMap { (subject, usesThirdPerson) ->
            activities.map { (activity, forms) ->
                val answer = forms[if (usesThirdPerson) 1 else 0]
                val options = forms.shuffled()
                ModuleGameQuestion(
                    prompt = "Choose the correct verb: $subject ___ $activity every day.",
                    options = options,
                    answerIndex = options.indexOf(answer),
                    isEnglish = true
                )
            }
        }
    }

    private fun secondaryEnglishVocabularyQuestions(): List<ModuleGameQuestion> {
        val synonyms = listOf(
            "happy" to "glad", "begin" to "start", "quick" to "fast", "large" to "big",
            "silent" to "quiet", "easy" to "simple", "brave" to "courageous", "smart" to "clever",
            "correct" to "right", "repair" to "fix", "assist" to "help", "select" to "choose",
            "finish" to "complete", "tiny" to "small", "ill" to "sick", "angry" to "mad",
            "purchase" to "buy", "reply" to "answer", "gift" to "present", "ancient" to "old",
            "protect" to "guard", "honest" to "truthful", "neat" to "tidy", "error" to "mistake",
            "fearful" to "afraid"
        )
        val answers = synonyms.map { it.second }
        return synonyms.map { (word, answer) ->
            val distractors = answers.filterNot { it == answer }.shuffled().take(3)
            val options = (listOf(answer) + distractors).shuffled()
            ModuleGameQuestion(
                prompt = "Which word is closest in meaning to ‘$word’?",
                options = options,
                answerIndex = options.indexOf(answer),
                isEnglish = true
            )
        }
    }

    private fun randomQuestions(count: Int, create: () -> ModuleGameQuestion): List<ModuleGameQuestion> {
        val questions = LinkedHashMap<String, ModuleGameQuestion>()
        var attempts = 0
        while (questions.size < count && attempts < 10_000) {
            val question = create()
            questions.putIfAbsent(question.prompt, question)
            attempts++
        }
        check(questions.size == count) { "Could not generate enough unique questions." }
        return questions.values.toList()
    }

    private fun numericDistractors(answer: Int): List<String> {
        val distractors = linkedSetOf<Int>()
        var delta = 1
        while (distractors.size < 3) {
            val above = answer + delta
            val below = answer - delta
            if (above != answer) distractors += above
            if (below >= 0 && below != answer) distractors += below
            delta++
        }
        return distractors.take(3).map(Int::toString)
    }

    private fun formatNumber(value: Double): String = if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        String.format(Locale.US, "%.2f", value).trimEnd('0').trimEnd('.')
    }

    private fun formatMoney(cents: Int): String = "₱${cents / 100}.${(cents % 100).toString().padStart(2, '0')}"

    private fun question(
        englishPrompt: String,
        filipinoPrompt: String,
        answer: String,
        distractors: List<String>,
        isFilipino: Boolean
    ): ModuleGameQuestion {
        val options = (listOf(answer) + distractors).distinct().shuffled().take(4)
        return ModuleGameQuestion(
            prompt = if (isFilipino) filipinoPrompt else englishPrompt,
            options = options,
            answerIndex = options.indexOf(answer)
        )
    }
}
