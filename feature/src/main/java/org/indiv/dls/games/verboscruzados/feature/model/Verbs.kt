package org.indiv.dls.games.verboscruzados.feature.model

import org.indiv.dls.games.verboscruzados.feature.conjugation.getIrAlteredRoot

//
// See conjugation rules: https://en.wikipedia.org/wiki/Spanish_irregular_verbs
//

enum class InfinitiveEnding(val ending: String) {
    ER("er"),
    AR("ar"),
    IR("ir")
}

enum class Irregularity {
    NO_ACCENT_ON_PRETERIT, // pude/pudo, dije/dijo
    SPELLING_CHANGE_PHONETIC,
    SPELLING_CHANGE_YO_ZC,
    SPELLING_CHANGE_YO_GO,
    SPELLING_CHANGE_Y,
    STEM_CHANGE_E_to_I,
    STEM_CHANGE_E_to_IE,
    STEM_CHANGE_O_to_UE,
    STEM_CHANGE_U_to_UE
}

/**
 * Conjugation type enum
 *
 * @param text name of the conjugation type
 * @param clueTemplate template for the clue. Parentheses represent the subject pronoun, square
 * brackets represent the optional direct object (typically followed by a space), and the underscore
 * represents the answer.
 */
enum class ConjugationType(val text: String) {
    PRESENT("Present"),
    PRETERIT("Preterit"),
    IMPERFECT("Imperfect"),
    CONDITIONAL("Conditional"),
    FUTURE("Future"),
    IMPERATIVE("Imperative"),
    SUBJUNCTIVE_PRESENT("Subjunctive Present"),
    SUBJUNCTIVE_IMPERFECT("Subjunctive Imperfect"),
    GERUND("Gerund"),
    PAST_PARTICIPLE("Past Participle")
}

enum class SubjectPronoun(val text: String, val isThirdPerson: Boolean = false) {
    YO("Yo"),
    TU("Tú"),
    EL_ELLA_USTED("Él/Ella/Ud.", true),
    ELLOS_ELLAS_USTEDES("Ellos/Ellas/Uds.", true),
    NOSOTROS("Nosotros"),
    VOSOTROS("Vosotros");

    val isNosotrosOrVosotros
        get() = (this == NOSOTROS || this == VOSOTROS)
}

data class Verb(val infinitive: String,
                val translation: String,
                val requiresDirectObject: Boolean = false,
                val irregularities: List<Irregularity> = emptyList(),
                val irregularGerund: String? = null,
                val irregularPastParticiple: String? = null,
                val irregularImperativeTu: String? = null,
                val altPreteritRoot: String? = null, // e.g. supe, quepo
                val altInfinitiveRoot: String? = null, // used in future, conditional (e.g. poder/podr)
                val customConjugation: ((SubjectPronoun, ConjugationType) -> String?)? = null) {
    val infinitiveEnding = when {
        infinitive.endsWith("ar") -> InfinitiveEnding.AR
        infinitive.endsWith("er") -> InfinitiveEnding.ER
        else -> InfinitiveEnding.IR // this includes "ír" as in "oír"
    }
    val root = infinitive.dropLast(2)
    val pastParticiple = irregularPastParticiple ?: when (infinitiveEnding) {
        InfinitiveEnding.AR -> root + "ado"
        else -> {
            val suffix = if (root.takeLast(1) in strongVowels) "ído" else "ido"
            root + suffix
        }
    }
    val gerund = irregularGerund ?: when (infinitiveEnding) {
        InfinitiveEnding.AR -> root + "ando"
        InfinitiveEnding.ER, InfinitiveEnding.IR -> {
            val alteredRoot = if (infinitiveEnding == InfinitiveEnding.IR)
                getIrAlteredRoot(root, irregularities) else root
            val suffix = if (alteredRoot.isEmpty() || (alteredRoot.takeLast(1) in listOf("a", "e", "o", "u")  &&
                            alteredRoot.takeLast(2) !in listOf("qu", "gu")))
                "yendo" else "iendo"
            alteredRoot + suffix
        }
    }
}

// Strong vowels form single-syllable dipthongs when combined with weak vowels
internal val strongVowels = listOf("a", "e", "o")


val regularArVerbs = listOf(
        Verb("acabar", "finish"),
        Verb("aceptar", "accept"),
        Verb("adivinar", "guess"),
        Verb("agradar", "please"),
        Verb("alegrar", "make happy"),
        Verb("alejar", "move away"),
        Verb("amar", "love"),
        Verb("arrastrar", "drag"),
        Verb("arreglar", "fix, arrange"),
        Verb("asomar", "stick out"),
        Verb("asombrar", "amaze"),
        Verb("atrapar", "catch"),
        Verb("ayudar", "help"),
        Verb("bailar", "dance"),
        Verb("bajar", "go down"),
        Verb("bañar", "bathe"),
        Verb("besar", "kiss"),
        Verb("bromear", "joke"),
        Verb("callar", "quiet"),
        Verb("cambiar", "change"),
        Verb("caminar", "walk"),
        Verb("cocinar", "cook"),
        Verb("comprar", "buy"),
        Verb("cantar", "sing"),
        Verb("cenar", "dine"),
        Verb("comentar", "discuss"),
        Verb("contestar", "answer"),
        Verb("cortar", "cut"),
        Verb("crear", "create"),
        Verb("considerar", "consider"),
        Verb("dejar", "leave, let"),
        Verb("descansar", "rest"),
        Verb("desear", "wish, desire"),
        Verb("dibujar", "draw"),
        Verb("disfrutar", "enjoy"),
        Verb("dudar", "doubt"),
        Verb("echar", "throw, pour"),
        Verb("empujar", "push"),
        Verb("enojar", "anger"),
        Verb("enseñar", "teach, show"),
        Verb("entrar", "enter"),
        Verb("escuchar", "listen (to)"),
        Verb("esperar", "wait/hope for"),
        Verb("estudiar", "study"),
        Verb("fijar", "fix"),
        Verb("frotar", "rub"),
        Verb("ganar", "win, earn"),
        Verb("gastar", "spend"),
        Verb("gritar", "shout"),
        Verb("gustar", "like"),
        Verb("hablar", "speak"),
        Verb("hallar", "find"),
        Verb("intentar", "try"),
        Verb("jalar", "pull"),
        Verb("lastimar", "hurt"),
        Verb("lavar", "wash"),
        Verb("levantar", "raise, lift"),
        Verb("limpiar", "clean"),
        Verb("llamar", "call"),
        Verb("llevar", "wear, carry"),
        Verb("llorar", "cry"),
        Verb("lograr", "obtain, achieve"),
        Verb("manejar", "drive"),
        Verb("mandar", "order"),
        Verb("mejorar", "improve"),
        Verb("mirar", "look at"),
        Verb("montar", "ride"),
        Verb("necesitar", "need"),
        Verb("olvidar", "forget"),
        Verb("ordenar", "order, arrange"),
        Verb("parar", "stop"),
        Verb("pasar", "pass/happen"),
        Verb("pesar", "weigh"),
        Verb("preguntar", "ask"),
        Verb("preocupar", "worry"),
        Verb("preparar", "prepare"),
        Verb("prestar", "lend, borrow"),
        Verb("quedar", "stay, remain"),
        Verb("quejar", "complain"),
        Verb("quitar", "take away"),
        Verb("regresar", "return"),
        Verb("relajar", "relax, calm"),
        Verb("reparar", "fix"),
        Verb("retrasar", "postpone"),
        Verb("sacar", "take out,"),
        Verb("serenar", "calm"),
        Verb("tapar", "cover"),
        Verb("tardar", "take time"),
        Verb("telefonear", "phone"),
        Verb("terminar", "end"),
        Verb("tirar", "throw away"),
        Verb("tomar", "take, drink"),
        Verb("trabajar", "work"),
        Verb("tratar", "treat"),
        Verb("trepar", "climb"),
        Verb("usar", "use, wear"),
        Verb("viajar", "travel"),
        Verb("visitar", "visit")
)

val regularIrVerbs = listOf(
        Verb("aburrir", "bore"),
        Verb("admitir", "admit"),
        Verb("asistir", "attend"),
        Verb("compartir", "share"),
        Verb("cumplir", "achieve"),
        Verb("decidir", "decide"),
        Verb("definir", "define"),
        Verb("describir", "describe"),
        Verb("descubrir", "discover"),
        Verb("discutir", "discuss"),
        Verb("existir", "exist"),
        Verb("ocurrir", "occur"),
        Verb("omitir", "omit"),
        Verb("partir", "divide, share"),
        Verb("permitir", "permit"),
        Verb("recibir", "receive"),
        Verb("subir", "go up, climb"),
        Verb("sufrir", "suffer"),
        Verb("unir", "join, unite"),
        Verb("vivir", "live")
)

val regularErVerbs = listOf(
        Verb("aprender", "learn"),
        Verb("beber", "drink"),
        Verb("comer", "eat"),
        Verb("comprender", "understand"),
        Verb("correr", "run"),
        Verb("deber", "owe, should"),
        Verb("depender", "depend"),
        Verb("esconder", "hide"),
        Verb("meter:", "put into"),
        Verb("poseer", "possess"),
        Verb("prometer", "promise"),
        Verb("reprender", "reprimand"),
        Verb("sorprender", "surprise"),
        Verb("suceder", "happen"),
        Verb("temer", "fear"),
        Verb("toser", "cough"),
        Verb("vender", "sell"))

// todo: add more verbs, add other irregularities, sort, etc
val irregularArVerbs = listOf(
        // Phonetic only
        Verb("alcanzar", "reach", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("apagar", "turn off", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("buscar", "look for", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("entregar", "submit", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("llegar", "arrive", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("pagar", "pay", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("sacar", "take out, remove", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),

        // stem change o -> ue
        Verb("acordar", "agree", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("acostar", "put to bed", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("almorzar", "eat lunch", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("aprobar", "approve", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("avergonzar", "embarrass", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("contar", "tell, count", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("costar", "cost", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("demostrar", "demonstrate, prove", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("encontrar", "meet, find", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("mostrar", "show", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("probar", "test, taste", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("recordar", "remember", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("rogar", "beg", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("soltar", "let go of, loosen", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("sonar", "sound", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("soñar", "dream", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("tronar", "thunder", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("volar", "fly", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),

        // stem change e -> ie
        Verb("acertar", "guess correctly", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("apretar", "press, squeeze", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("atravesar", "cross", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("calentar", "warm", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("cerrar", "close", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("comenzar", "begin, start", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("despertar", "wake", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("empezar", "begin", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("enterrar", "bury", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("errar", "miss, wander", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("gobernar", "govern", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("mentar", "mention", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("nevar", "snow", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("pensar", "think", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("recomendar", "recommend", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("sentar", "sit", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),

        // stem change u -> ue
        Verb("jugar", "play", irregularities = listOf(Irregularity.STEM_CHANGE_U_to_UE)),

        // no accent on preterit
        Verb("andar", "walk", altPreteritRoot = "anduv", irregularities = listOf(Irregularity.NO_ACCENT_ON_PRETERIT)),

        // Custom conjugation
        Verb("dar", "give", irregularities = listOf(Irregularity.NO_ACCENT_ON_PRETERIT)) { subjectPronoun: SubjectPronoun, conjugationType: ConjugationType ->
            when (conjugationType) {
                ConjugationType.PRESENT -> {
                    when (subjectPronoun) {
                        SubjectPronoun.YO -> "doy"
                        SubjectPronoun.VOSOTROS -> "dais"
                        else -> null
                    }
                }
                ConjugationType.PRETERIT -> {
                    when (subjectPronoun) {
                        SubjectPronoun.YO -> "di"
                        SubjectPronoun.EL_ELLA_USTED -> "dio"
                        else -> null
                    }
                }
                ConjugationType.SUBJUNCTIVE_PRESENT -> {
                    when (subjectPronoun) {
                        SubjectPronoun.YO -> "dé"
                        SubjectPronoun.EL_ELLA_USTED -> "dé"
                        SubjectPronoun.VOSOTROS -> "deis"
                        else -> null
                    }
                }
                else -> null
            }
        },
        Verb("estar", "be", altPreteritRoot = "estuv", irregularities = listOf(Irregularity.NO_ACCENT_ON_PRETERIT)) { subjectPronoun: SubjectPronoun, conjugationType: ConjugationType ->
            when (conjugationType) {
                ConjugationType.PRESENT -> {
                    when (subjectPronoun) {
                        SubjectPronoun.YO -> "estoy"
                        SubjectPronoun.TU -> "estás"
                        SubjectPronoun.EL_ELLA_USTED -> "está"
                        SubjectPronoun.ELLOS_ELLAS_USTEDES -> "están"
                        else -> null
                    }
                }
                ConjugationType.SUBJUNCTIVE_PRESENT -> {
                    when (subjectPronoun) {
                        SubjectPronoun.YO -> "esté"
                        SubjectPronoun.TU -> "estés"
                        SubjectPronoun.EL_ELLA_USTED -> "esté"
                        SubjectPronoun.ELLOS_ELLAS_USTEDES -> "estén"
                        else -> null
                    }
                }
                else -> null
            }
        }
)

val irregularIrVerbs = listOf(
        // past participle only
        Verb("abrir", "open", irregularPastParticiple = "abierto"),
        Verb("cubrir", "cover", irregularPastParticiple = "cubierto"),
        Verb("escribir", "write", irregularPastParticiple = "escrito"),

        // spelling change - phonetic
        Verb("dirigir", "manage, direct", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("construir", "build", irregularities = listOf(Irregularity.SPELLING_CHANGE_Y)),
        Verb("fluir", "flow", irregularities = listOf(Irregularity.SPELLING_CHANGE_Y)),
        Verb("huir", "escape, flee", irregularities = listOf(Irregularity.SPELLING_CHANGE_Y)),

        // TODO

        // TODO add stem change
        Verb("elegir", "choose", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC, Irregularity.STEM_CHANGE_E_to_I)),

        // stem changes e -> i
        Verb("conseguir", "get", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_I)),
        Verb("elegir", "choose", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_I)),
        Verb("gemir", "groan", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_I)),
        Verb("impedir", "impede", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_I)),
        Verb("medir", "measure", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_I)),
        Verb("pedir", "ask", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_I)),
        Verb("reír", "laugh", altInfinitiveRoot = "reir", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_I)),
        Verb("repetir", "repeat", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_I)),
        Verb("seguir", "follow", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_I)),
        Verb("servir", "serve", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_I)),
        Verb("sonreír", "smile", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_I)),
        Verb("vestir", "dress, wear", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_I)),

        // stem changes o -> ui
        Verb("dormir", "sleep", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),

        // stem changes e -> ie
        Verb("asentir", "agree, assent, nod", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("consentir", "allow", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("convertir", "turn into", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("desmentir", "deny", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("disentir", "disagree", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("mentir", "lie", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("preferir", "prefer", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("sentir", "feel", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("sugerir", "suggest", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),

        // Yo Go verbs
        Verb("oír", "hear", altInfinitiveRoot = "oir", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO)),
        Verb("desoír", "ignore", altInfinitiveRoot = "desoir", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO)),
        Verb("salir", "go out, leave", altInfinitiveRoot = "saldr", irregularImperativeTu = "sal", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO)),
        Verb("prevenir", "prevent", altPreteritRoot = "previn", altInfinitiveRoot = "prevendr", irregularImperativeTu = "prevén", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.STEM_CHANGE_E_to_IE, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("venir", "come", altPreteritRoot = "vin", altInfinitiveRoot = "vendr", irregularImperativeTu = "ven", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.STEM_CHANGE_E_to_IE, Irregularity.NO_ACCENT_ON_PRETERIT)),

        // Yo zc
        Verb("producir", "produce", altPreteritRoot = "produj", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("reducir", "reduce", altPreteritRoot = "reduj", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC, Irregularity.SPELLING_CHANGE_PHONETIC, Irregularity.NO_ACCENT_ON_PRETERIT)),

        // Custom conjugations
        Verb("decir", "say, tell", altPreteritRoot = "dij", altInfinitiveRoot = "dir", irregularImperativeTu = "di", irregularPastParticiple = "dicho", irregularGerund = "diciendo", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.STEM_CHANGE_E_to_I, Irregularity.NO_ACCENT_ON_PRETERIT)) { subjectPronoun: SubjectPronoun, conjugationType: ConjugationType ->
            when (conjugationType) {
                ConjugationType.PRESENT -> if (subjectPronoun == SubjectPronoun.YO) "digo" else null
                else -> null
            }
        },
        Verb("ir", "go", altPreteritRoot = "fu", irregularities = listOf(Irregularity.NO_ACCENT_ON_PRETERIT, Irregularity.SPELLING_CHANGE_PHONETIC, Irregularity.STEM_CHANGE_E_to_I)) { subjectPronoun: SubjectPronoun, conjugationType: ConjugationType ->
            when (conjugationType) {
                ConjugationType.PRESENT -> {
                    when (subjectPronoun) {
                        SubjectPronoun.YO -> "voy"
                        SubjectPronoun.TU -> "vas"
                        SubjectPronoun.EL_ELLA_USTED -> "va"
                        SubjectPronoun.ELLOS_ELLAS_USTEDES -> "van"
                        SubjectPronoun.NOSOTROS -> "vamos"
                        SubjectPronoun.VOSOTROS -> "vais"
                    }
                }
                ConjugationType.PRETERIT -> {
                    when (subjectPronoun) {
                        SubjectPronoun.YO -> "fui"
                        SubjectPronoun.EL_ELLA_USTED -> "fue"
                        SubjectPronoun.ELLOS_ELLAS_USTEDES -> "fueron"
                        else -> null
                    }
                }
                ConjugationType.IMPERFECT -> {
                    when (subjectPronoun) {
                        SubjectPronoun.YO -> "iba"
                        SubjectPronoun.TU -> "ibas"
                        SubjectPronoun.EL_ELLA_USTED -> "iba"
                        SubjectPronoun.ELLOS_ELLAS_USTEDES -> "iban"
                        SubjectPronoun.NOSOTROS -> "ibamos"
                        SubjectPronoun.VOSOTROS -> "ibais"
                    }
                }
                ConjugationType.SUBJUNCTIVE_PRESENT -> {
                    when (subjectPronoun) {
                        SubjectPronoun.YO -> "vaya"
                        SubjectPronoun.TU -> "vayas"
                        SubjectPronoun.EL_ELLA_USTED -> "vaya"
                        SubjectPronoun.ELLOS_ELLAS_USTEDES -> "vayan"
                        SubjectPronoun.NOSOTROS -> "vayamos"
                        SubjectPronoun.VOSOTROS -> "vayáis"
                    }
                }
                else -> null
            }
        }
)

val irregularErVerbs = listOf(
        // past participle only
        Verb("romper", "break", irregularPastParticiple = "roto"),

        // Spelling change
        Verb("creer", "believe", irregularities = listOf(Irregularity.SPELLING_CHANGE_Y)),
        Verb("leer", "read", irregularities = listOf(Irregularity.SPELLING_CHANGE_Y)),
        Verb("poseer", "possess", irregularities = listOf(Irregularity.SPELLING_CHANGE_Y)),
        Verb("proveer", "provide", irregularities = listOf(Irregularity.SPELLING_CHANGE_Y)),

        // stem changes o -> ue
        Verb("mover", "move", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("demoler", "demolish", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("devolver", "return something", irregularPastParticiple = "devuelto", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("disolver", "dissolve", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("doler", "hurt", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("envolver", "wrap", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("llover", " rain", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("moler", "grind", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("morder", "bite", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("oler", "smell", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("promover", "promote", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("remover", "remove", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("resolver", "resolve", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("revolver", "stir, mix, shake", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("soler", "use to, usually", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("volver", "return", irregularPastParticiple = "vuelto", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),

        // stem changes e -> ie
        Verb("atender", "attend to", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("defender", "defend", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("descender", "fall, descend", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("encender", "light, turn on", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("entender", "understand", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("perder", "lose", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("verter", "pour, spill", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),

        Verb("querer", "want", altPreteritRoot = "quis", altInfinitiveRoot = "querr", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE, Irregularity.NO_ACCENT_ON_PRETERIT)),

        // no accent on preterit
        Verb("poder", "can, be able to", altPreteritRoot = "pud", altInfinitiveRoot = "podr", irregularGerund = "pudiendo", irregularities = listOf(Irregularity.NO_ACCENT_ON_PRETERIT, Irregularity.STEM_CHANGE_O_to_UE)),

        // Yo Go verbs
        Verb("caer", "fall", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.SPELLING_CHANGE_Y)),
        Verb("hacer", "make, do", altPreteritRoot = "hic", altInfinitiveRoot = "har", irregularImperativeTu = "haz", irregularPastParticiple = "hecho", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("poner", "put", altPreteritRoot = "pus", altInfinitiveRoot = "pondr", irregularImperativeTu = "pon", irregularPastParticiple = "puesto", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("tener", "have", altPreteritRoot = "tuv", altInfinitiveRoot = "tendr", irregularImperativeTu = "ten", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.STEM_CHANGE_E_to_IE, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("traer", "bring", altPreteritRoot = "traj", irregularities = listOf(Irregularity.NO_ACCENT_ON_PRETERIT, Irregularity.SPELLING_CHANGE_YO_GO)),
        Verb("valer", "be worth, cost", altInfinitiveRoot = "valdr", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO)),

        // Yo ZC verbs
        Verb("agradecer", "thank", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("aparecer", "appear", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("conocer", "know, meet", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("crecer", "grow", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("desaparecer", "disappear", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("merecer", "deserve", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("nacer", "be born", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("obedecer", "obey", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("ofrecer", "offer", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("parecer", "seem", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),

        // Similar to poner
        Verb("componer", "compose, prepare", altPreteritRoot = "compus", altInfinitiveRoot = "compondr", irregularImperativeTu = "compón", irregularPastParticiple = "compuesto", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("contraponer", "compare, contrast", altPreteritRoot = "contrapus", altInfinitiveRoot = "contrapondr", irregularImperativeTu = "contrapón", irregularPastParticiple = "contrapuesto", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("disponer", "arrange", altPreteritRoot = "dispus", altInfinitiveRoot = "dispondr", irregularImperativeTu = "dispón", irregularPastParticiple = "dispuesto", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("imponer", "impose", altPreteritRoot = "impus", altInfinitiveRoot = "impondr", irregularImperativeTu = "impón", irregularPastParticiple = "impuesto", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("oponer", "oppose", altPreteritRoot = "opus", altInfinitiveRoot = "opondr", irregularImperativeTu = "opón", irregularPastParticiple = "opuesto", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("posponer", "postpone", altPreteritRoot = "pospus", altInfinitiveRoot = "pospondr", irregularImperativeTu = "pospón", irregularPastParticiple = "pospuesto", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("suponer", "suppose", altPreteritRoot = "supus", altInfinitiveRoot = "supondr", irregularImperativeTu = "supón", irregularPastParticiple = "supuesto", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.NO_ACCENT_ON_PRETERIT)),

        // Similar to tener
        Verb("contener", "contain", altPreteritRoot = "contuv", altInfinitiveRoot = "contendr", irregularImperativeTu = "contén", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.STEM_CHANGE_E_to_IE, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("detener", "stop", altPreteritRoot = "detuv", altInfinitiveRoot = "detendr", irregularImperativeTu = "detén", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.STEM_CHANGE_E_to_IE, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("mantener", "maintain", altPreteritRoot = "mantuv", altInfinitiveRoot = "mantendr", irregularImperativeTu = "mantén", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.STEM_CHANGE_E_to_IE, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("obtener", "obtain", altPreteritRoot = "obtuv", altInfinitiveRoot = "obtendr", irregularImperativeTu = "obtén", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.STEM_CHANGE_E_to_IE, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("retener", "retain, keep", altPreteritRoot = "retuv", altInfinitiveRoot = "retendr", irregularImperativeTu = "retén", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.STEM_CHANGE_E_to_IE, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("sostener", "hold", altPreteritRoot = "sostuv", altInfinitiveRoot = "sostendr", irregularImperativeTu = "sostén", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.STEM_CHANGE_E_to_IE, Irregularity.NO_ACCENT_ON_PRETERIT)),

        // Yo zc
        Verb("conocer", "know, meet", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("crecer", "grow", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("nacer", "be born", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),


        // Custom conjugations
        Verb("caber", "fit", altPreteritRoot = "cup", altInfinitiveRoot = "cabr", irregularities = listOf(Irregularity.NO_ACCENT_ON_PRETERIT)) { subjectPronoun: SubjectPronoun, conjugationType: ConjugationType ->
            when (conjugationType) {
                ConjugationType.PRESENT -> if (subjectPronoun == SubjectPronoun.YO) "quepo" else null
                else -> null
            }
        },
        Verb("haber", "have, exist", altPreteritRoot = "hub", altInfinitiveRoot = "habr", irregularities = listOf(Irregularity.NO_ACCENT_ON_PRETERIT)) { subjectPronoun: SubjectPronoun, conjugationType: ConjugationType ->
            when (conjugationType) {
                ConjugationType.PRESENT -> {
                    when (subjectPronoun) {
                        SubjectPronoun.YO -> "he"
                        SubjectPronoun.TU -> "has"
                        SubjectPronoun.EL_ELLA_USTED -> "ha"
                        SubjectPronoun.ELLOS_ELLAS_USTEDES -> "han"
                        SubjectPronoun.NOSOTROS -> "hemos"
                        SubjectPronoun.VOSOTROS -> "habéis"
                    }
                }
                ConjugationType.SUBJUNCTIVE_PRESENT -> {
                    when (subjectPronoun) {
                        SubjectPronoun.YO -> "haya"
                        SubjectPronoun.TU -> "hayas"
                        SubjectPronoun.EL_ELLA_USTED -> "haya"
                        SubjectPronoun.ELLOS_ELLAS_USTEDES -> "hayan"
                        SubjectPronoun.NOSOTROS -> "hayamos"
                        SubjectPronoun.VOSOTROS -> "hayáis"
                    }
                }
                else -> null
            }
        },
        Verb("saber", "know", altPreteritRoot = "sup", altInfinitiveRoot = "sabr", irregularities = listOf(Irregularity.NO_ACCENT_ON_PRETERIT)) { subjectPronoun: SubjectPronoun, conjugationType: ConjugationType ->
            when (conjugationType) {
                ConjugationType.PRESENT -> {
                    when (subjectPronoun) {
                        SubjectPronoun.YO -> "sé"
                        else -> null
                    }
                }
                ConjugationType.SUBJUNCTIVE_PRESENT -> {
                    when (subjectPronoun) {
                        SubjectPronoun.YO -> "sepa"
                        SubjectPronoun.TU -> "sepas"
                        SubjectPronoun.EL_ELLA_USTED -> "sepa"
                        SubjectPronoun.ELLOS_ELLAS_USTEDES -> "sepan"
                        SubjectPronoun.NOSOTROS -> "sepamos"
                        SubjectPronoun.VOSOTROS -> "sepáis"
                    }
                }
                else -> null
            }
        },
        Verb("ser", "be", altPreteritRoot = "fu", irregularImperativeTu = "sé", irregularities = listOf(Irregularity.NO_ACCENT_ON_PRETERIT)) { subjectPronoun: SubjectPronoun, conjugationType: ConjugationType ->
            when (conjugationType) {
                ConjugationType.PRESENT -> {
                    when (subjectPronoun) {
                        SubjectPronoun.YO -> "soy"
                        SubjectPronoun.TU -> "eres"
                        SubjectPronoun.EL_ELLA_USTED -> "es"
                        SubjectPronoun.ELLOS_ELLAS_USTEDES -> "son"
                        SubjectPronoun.NOSOTROS -> "somos"
                        SubjectPronoun.VOSOTROS -> "sois"
                    }
                }
                ConjugationType.PRETERIT -> {
                    when (subjectPronoun) {
                        SubjectPronoun.YO -> "fui"
                        SubjectPronoun.EL_ELLA_USTED -> "fue"
                        SubjectPronoun.ELLOS_ELLAS_USTEDES -> "fueron"
                        else -> null
                    }
                }
                ConjugationType.IMPERFECT -> {
                    when (subjectPronoun) {
                        SubjectPronoun.YO -> "era"
                        SubjectPronoun.TU -> "eras"
                        SubjectPronoun.EL_ELLA_USTED -> "era"
                        SubjectPronoun.ELLOS_ELLAS_USTEDES -> "eran"
                        SubjectPronoun.NOSOTROS -> "éramos"
                        SubjectPronoun.VOSOTROS -> "erais"
                    }
                }
                ConjugationType.SUBJUNCTIVE_PRESENT -> {
                    when (subjectPronoun) {
                        SubjectPronoun.YO -> "sea"
                        SubjectPronoun.TU -> "seas"
                        SubjectPronoun.EL_ELLA_USTED -> "sea"
                        SubjectPronoun.ELLOS_ELLAS_USTEDES -> "sean"
                        SubjectPronoun.NOSOTROS -> "seamos"
                        SubjectPronoun.VOSOTROS -> "seáis"
                    }
                }
                else -> null
            }
        },
        Verb("ver", "see", irregularPastParticiple = "visto", irregularities = listOf(Irregularity.NO_ACCENT_ON_PRETERIT)) { subjectPronoun: SubjectPronoun, conjugationType: ConjugationType ->
            when (conjugationType) {
                ConjugationType.PRESENT -> {
                    when (subjectPronoun) {
                        SubjectPronoun.YO -> "veo"
                        SubjectPronoun.VOSOTROS -> "veis"
                        else -> null
                    }
                }
                ConjugationType.PRETERIT -> {
                    when (subjectPronoun) {
                        SubjectPronoun.YO -> "vi"
                        SubjectPronoun.EL_ELLA_USTED -> "vio"
                        else -> null
                    }
                }
                ConjugationType.IMPERFECT -> {
                    when (subjectPronoun) {
                        SubjectPronoun.YO -> "veía"
                        SubjectPronoun.TU -> "veías"
                        SubjectPronoun.EL_ELLA_USTED -> "veía"
                        SubjectPronoun.ELLOS_ELLAS_USTEDES -> "veían"
                        SubjectPronoun.NOSOTROS -> "veíamos"
                        SubjectPronoun.VOSOTROS -> "veíais"
                    }
                }
                ConjugationType.SUBJUNCTIVE_PRESENT -> {
                    when (subjectPronoun) {
                        SubjectPronoun.YO -> "vea"
                        SubjectPronoun.TU -> "veas"
                        SubjectPronoun.EL_ELLA_USTED -> "vea"
                        SubjectPronoun.ELLOS_ELLAS_USTEDES -> "vean"
                        SubjectPronoun.NOSOTROS -> "veamos"
                        SubjectPronoun.VOSOTROS -> "veáis"
                    }
                }
                else -> null
            }
        }

)
