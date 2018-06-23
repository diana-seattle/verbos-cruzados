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
            val suffix = if (alteredRoot.takeLast(1) in listOf("a", "e", "o", "u"))
                "yendo" else "iendo"
            root + suffix
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
        Verb("sacar", "take out, remove", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),

        // TODO

        Verb("acordar", "agree", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("avergonzar", "embarrass", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("errar", "miss, wander", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("nevar", "snow", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("jugar", "play", irregularities = listOf(Irregularity.STEM_CHANGE_U_to_UE)),
        Verb("sentar", "sit", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),

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

        // TODO

        // TODO add stem change
        Verb("elegir", "choose", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC, Irregularity.STEM_CHANGE_E_to_I)),

        // stem changes
        Verb("conseguir", "get", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_I)),
        Verb("dormir", "sleep", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("sentir", "feel", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),

        Verb("construir", "build", irregularities = listOf(Irregularity.SPELLING_CHANGE_Y)),
        Verb("fluir", "flow", irregularities = listOf(Irregularity.SPELLING_CHANGE_Y)),
        Verb("huir", "escape, flee", irregularities = listOf(Irregularity.SPELLING_CHANGE_Y)),

        // Yo Go verbs
        Verb("oír", "hear", altInfinitiveRoot = "oir", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO)),
        Verb("desoír", "ignore", altInfinitiveRoot = "desoir", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO)),
        Verb("salir", "go out, leave", altInfinitiveRoot = "saldr", irregularImperativeTu = "sal", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO)),
        Verb("venir", "come", altPreteritRoot = "vin", altInfinitiveRoot = "vendr", irregularImperativeTu = "ven", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.STEM_CHANGE_E_to_IE, Irregularity.NO_ACCENT_ON_PRETERIT)),

        // Yo zc
        Verb("producir", "produce", altPreteritRoot = "produj", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("reducir", "reduce", altPreteritRoot = "reduj", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC, Irregularity.SPELLING_CHANGE_PHONETIC, Irregularity.NO_ACCENT_ON_PRETERIT)),

        // Custom conjugations
        Verb("decir", "say, tell", altPreteritRoot = "dij", altInfinitiveRoot = "dir", irregularImperativeTu = "di", irregularPastParticiple = "dicho", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.STEM_CHANGE_E_to_I, Irregularity.NO_ACCENT_ON_PRETERIT)) { subjectPronoun: SubjectPronoun, conjugationType: ConjugationType ->
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

        // TODO

        // stem changes
        Verb("oler", "smell", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("querer", "want", altPreteritRoot = "quis", altInfinitiveRoot = "querr", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE, Irregularity.NO_ACCENT_ON_PRETERIT)),


        Verb("volver", "return", irregularPastParticiple = "vuelto", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),

        Verb("creer", "believe", irregularities = listOf(Irregularity.SPELLING_CHANGE_Y)),


        // no accent on preterit
        Verb("poder", "can, be able to", altPreteritRoot = "pud", altInfinitiveRoot = "podr", irregularGerund = "pudiendo", irregularities = listOf(Irregularity.NO_ACCENT_ON_PRETERIT, Irregularity.STEM_CHANGE_O_to_UE)),


        // Yo Go verbs
        Verb("caer", "fall", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.SPELLING_CHANGE_Y)),
        Verb("hacer", "make, do", altPreteritRoot = "hic", altInfinitiveRoot = "har", irregularImperativeTu = "haz", irregularPastParticiple = "hecho", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("poner", "put", altPreteritRoot = "pus", altInfinitiveRoot = "pondr", irregularImperativeTu = "pon", irregularPastParticiple = "puesto", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("tener", "have", altPreteritRoot = "tuv", altInfinitiveRoot = "tendr", irregularImperativeTu = "ten", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.STEM_CHANGE_E_to_IE, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("traer", "bring", altPreteritRoot = "traj", irregularities = listOf(Irregularity.NO_ACCENT_ON_PRETERIT, Irregularity.SPELLING_CHANGE_YO_GO)),
        Verb("valer", "be worth, cost", altInfinitiveRoot = "valdr", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO)),

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
