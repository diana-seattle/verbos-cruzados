package org.indiv.dls.games.verboscruzados.feature.model

import org.indiv.dls.games.verboscruzados.feature.conjugation.getIrAlteredRoot

//
// See conjugation rules: https://en.wikipedia.org/wiki/Spanish_irregular_verbs
//
// Also, good source of verbs: http://www.intro2spanish.com/verbs/listas/all.htm

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
    STEM_CHANGE_I_to_I, // to accented í
    STEM_CHANGE_O_to_UE,
    STEM_CHANGE_U_to_UE
}

enum class IrregularityCategory(val text: String) {
    REGULAR("Regular"),
    SPELLING_CHANGE("Spelling Change"),
    STEM_CHANGE("Stem Change"),
    IRREGULAR("Irregular")
}

/**
 * Conjugation type enum
 *
 * @param text name of the conjugation type
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
            val suffix = if (alteredRoot.isEmpty() || (alteredRoot.takeLast(1) in listOf("a", "e", "o", "u") &&
                            alteredRoot.takeLast(2) !in listOf("qu", "gu"))) {
                "yendo"
            } else if (alteredRoot.takeLast(1) in listOf("i", "ñ")) {
                "endo"
            } else {
                "iendo"
            }
            alteredRoot + suffix
        }
    }

    /**
     * Implementing equals() so that we can ensure no duplicates based on the infinitive.
     */
    override fun equals(other: Any?): Boolean {
        return infinitive == (other as? Verb)?.infinitive
    }
}

// Strong vowels form single-syllable dipthongs when combined with weak vowels
internal val strongVowels = listOf("a", "e", "o")

// todo: add more verbs, add other irregularities, sort, etc

// AR verbs
val regularArVerbs = listOf(
        Verb("abandonar", "abandon"),
        Verb("abusar", "abuse"),
        Verb("acabar", "finish"),
        Verb("aceptar", "accept"),
        Verb("acompañar", "accompany"),
        Verb("aconsejar", "counsel, advise"),
        Verb("acostumbrar", "be used to"),
        Verb("acumular", "accumulate"),
        Verb("acusar", "accuse"),
        Verb("adaptar", "adapt"),
        Verb("adelantar", "advance, pass"),
        Verb("adivinar", "guess"),
        Verb("admirar", "admire"),
        Verb("adoptar", "adopt"),
        Verb("adorar", "adore, worship"),
        Verb("afectar", "affect"),
        Verb("afeitar", "shave"),
        Verb("agarrar", "grab"),
        Verb("agitar", "shake"),
        Verb("agotar", "use up"),
        Verb("agradar", "please"),
        Verb("aguardar", "expect, await"),
        Verb("ahorrar", "save, not waste"),
        Verb("alegrar", "make happy"),
        Verb("alejar", "move away"),
        Verb("alimentar", "feed"),
        Verb("aliviar", "alleviate"),
        Verb("amar", "love"),
        Verb("anunciar", "announce"),
        Verb("arrastrar", "drag"),
        Verb("arreglar", "fix, arrange"),
        Verb("arrojar", "throw"),
        Verb("asegurar", "assure"),
        Verb("asomar", "stick out"),
        Verb("asombrar", "amaze"),
        Verb("aspirar", "inhale"),
        Verb("asustar", "frighten"),
        Verb("atrapar", "catch"),
        Verb("ayudar", "help"),
        Verb("bailar", "dance"),
        Verb("bajar", "go down"),
        Verb("bañar", "bathe"),
        Verb("besar", "kiss"),
        Verb("bloquear", "block"),
        Verb("borrar", "delete, erase"),
        Verb("brindar", "toast, offer"),
        Verb("bromear", "joke"),
        Verb("callar", "quiet"),
        Verb("cambiar", "change"),
        Verb("caminar", "walk"),
        Verb("cansar", "tire"),
        Verb("cantar", "sing"),
        Verb("casar", "marry"),
        Verb("causar", "cause"),
        Verb("celebrar", "celebrate"),
        Verb("cenar", "eat dinner"),
        Verb("cepillar", "brush"),
        Verb("charlar", "chat"),
        Verb("chillar", "shriek"),
        Verb("cocinar", "cook"),
        Verb("comentar", "discuss, comment"),
        Verb("comparar", "compare"),
        Verb("completar", "complete"),
        Verb("comprar", "buy"),
        Verb("condenar", "condemn"),
        Verb("considerar", "consider"),
        Verb("contestar", "answer"),
        Verb("contratar", "hire"),
        Verb("cortar", "cut"),
        Verb("crear", "create"),
        Verb("cuidar", "take care of"),
        Verb("dejar", "leave, let"),
        Verb("desayunar", "eat breakfast"),
        Verb("descansar", "rest"),
        Verb("desear", "wish, desire"),
        Verb("desmayar", "lose heart"),
        Verb("dibujar", "draw"),
        Verb("disculpar", "excuse, forgive"),
        Verb("disfrutar", "enjoy"),
        Verb("disgustar", "disgust"),
        Verb("doblar", "fold, bend"),
        Verb("duchar", "shower"),
        Verb("dudar", "doubt"),
        Verb("echar", "throw, pour"),
        Verb("empeorar", "worsen"),
        Verb("empujar", "push"),
        Verb("encantar", "love, enchant"),
        Verb("enfadar", "upset, annoy"),
        Verb("engañar", "deceive"),
        Verb("enojar", "anger"),
        Verb("enseñar", "teach, show"),
        Verb("entrar", "enter"),
        Verb("escoltar", "escort"),
        Verb("escuchar", "listen (to)"),
        Verb("esperar", "wait/hope for"),
        Verb("estornudar", "sneeze"),
        Verb("estudiar", "study"),
        Verb("evitar", "avoid"),
        Verb("fallar", "fail"),
        Verb("faltar", "lack, miss"),
        Verb("fijar", "fix"),
        Verb("firmar", "sign"),
        Verb("frotar", "rub"),
        Verb("frustrar", "frustrate"),
        Verb("fumar", "smoke"),
        Verb("funcionar", "function, work"),
        Verb("ganar", "win, earn"),
        Verb("gastar", "spend"),
        Verb("girar", "turn, spin"),
        Verb("golpear", "hit"),
        Verb("gritar", "shout"),
        Verb("gustar", "like, please"),
        Verb("hablar", "speak"),
        Verb("hallar", "find"),
        Verb("imaginar", "imagine"),
        Verb("intentar", "try"),
        Verb("interesar", "interest"),
        Verb("invitar", "invite"),
        Verb("jalar", "pull"),
        Verb("jurar", "swear"),
        Verb("lastimar", "hurt"),
        Verb("lavar", "wash"),
        Verb("levantar", "raise, lift"),
        Verb("limpiar", "clean"),
        Verb("llamar", "call"),
        Verb("llenar", "fill"),
        Verb("llevar", "take, wear"),
        Verb("llorar", "cry"),
        Verb("lograr", "obtain, achieve"),
        Verb("luchar", "fight, battle"),
        Verb("manchar", "stain"),
        Verb("mandar", "send, order"),
        Verb("manejar", "drive, manage"),
        Verb("mandar", "order"),
        Verb("mejorar", "improve"),
        Verb("mencionar", "mention"),
        Verb("mirar", "look at"),
        Verb("molestar", "bother"),
        Verb("montar", "ride"),
        Verb("nadar", "swim"),
        Verb("necesitar", "need"),
        Verb("odiar", "hate"),
        Verb("olvidar", "forget"),
        Verb("orar", "pray"),
        Verb("ordenar", "order, arrange"),
        Verb("parar", "stop, stand"),
        Verb("pasar", "pass, happen"),
        Verb("patinar", "skate"),
        Verb("peinar", "comb, style"),
        Verb("pelear", "fight"),
        Verb("perdonar", "pardon, forgive"),
        Verb("pesar", "weigh"),
        Verb("pintar", "paint"),
        Verb("preguntar", "ask"),
        Verb("preocupar", "worry"),
        Verb("preparar", "prepare"),
        Verb("presentar", "present"),
        Verb("prestar", "lend, borrow"),
        Verb("pronunciar", "pronounce"),
        Verb("quebrar", "break"),
        Verb("quedar", "stay, remain"),
        Verb("quejar", "complain"),
        Verb("quemar", "burn"),
        Verb("quitar", "remove, take away"),
        Verb("regresar", "return"),
        Verb("relajar", "relax, calm"),
        Verb("reparar", "fix"),
        Verb("resultar", "result"),
        Verb("retrasar", "postpone"),
        Verb("serenar", "calm"),
        Verb("señalar", "mark, point out"),
        Verb("saludar", "greet"),
        Verb("tapar", "cover"),
        Verb("tardar", "take time"),
        Verb("telefonear", "phone"),
        Verb("terminar", "end, finish"),
        Verb("tirar", "throw, throw away"),
        Verb("tomar", "take, drink"),
        Verb("trabajar", "work"),
        Verb("tratar", "treat, try"),
        Verb("trepar", "climb"),
        Verb("usar", "use, wear"),
        Verb("viajar", "travel"),
        Verb("vigilar", "watch, guard"),
        Verb("visitar", "visit"),
        Verb("voltear", "turn over")
)
val spellingChangeArVerbs = listOf(
        // Phonetic only
        Verb("abrazar", "hug", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("acercar", "approach", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("ahogar", "drown, suffocate", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("alcanzar", "reach", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("amenazar", "threaten", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("arrancar", "pull out", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("apagar", "turn off", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("atacar", "attack", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("bostezar", "yawn", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("buscar", "look for", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("cargar", "load, charge", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("castigar", "punish", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("cazar", "hunt", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("colocar", "place", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("chocar", "collide", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("complicar", "complicate", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("comunicar", "communicate", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("conjugar", "conjugate", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("criticar", "criticize", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("cruzar", "cross", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("dedicar", "dedicate", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("delegar", "delegate", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("encargar", "entrust", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("entregar", "submit", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("equivocar", "get wrong", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("explicar", "explain", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("fabricar", "fabricate", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("glorificar", "glorify", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("identificar", "identify", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("investigar", "investigate", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("justificar", "justify", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("juzgar", "judge", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("lanzar", "throw, hurl", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("llegar", "arrive", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("localizar", "locate", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("marcar", "mark", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("masticar", "chew", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("modificar", "modify", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("notificar", "notify", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("obligar", "obligate", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("pagar", "pay", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("pescar", "fish", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("picar", "sting, itch", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("practicar", "practicar", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("propagar", "propagate", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("provocar", "provoke, cause", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("replicar", "reply", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("rezar", "pray", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("roncar", "snore", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("sacar", "take out, remove", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("secar", "dry", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("significar", "signify, mean", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("tocar", "touch, play instrument", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("utilizar", "use, utilize", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("verificar", "verify", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC))
)
val stemChangeArVerbs = listOf(
        // stem change o -> ue
        Verb("acordar", "agree", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("acostar", "put to bed", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("almorzar", "eat lunch", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE, Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("apostar", "bet", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("aprobar", "approve", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("avergonzar", "embarrass", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE, Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("colgar", "hang", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE, Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("contar", "tell, count", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("costar", "cost", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("demostrar", "demonstrate, prove", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("encontrar", "meet, find", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("esforzar", "strain", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE, Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("forzar", "force", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE, Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("mostrar", "show", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("probar", "test, taste, prove", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("recordar", "remember", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("reforzar", "reinforce", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE, Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("renovar", "renovate", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("rodar", "roll", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE, Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("rogar", "beg", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE, Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("soltar", "let go of, loosen", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("sonar", "sound", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("soñar", "dream", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("tostar", "toast", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("tronar", "thunder", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("volar", "fly", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),

        // stem change e -> ie
        Verb("acertar", "guess correctly", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("apretar", "press, squeeze", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("atravesar", "cross", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("calentar", "warm", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("cerrar", "close", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("comenzar", "begin, start", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE, Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("despertar", "wake", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("empezar", "begin", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE, Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("enterrar", "bury", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("errar", "miss, wander", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("gobernar", "govern", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("helar", "freeze", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("merendar", "snack", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("negar", "deny, negate", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE, Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("nevar", "snow", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("pensar", "think", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("recomendar", "recommend", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("regar", "water", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE, Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("remendar", "mend", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("sembrar", "plant, sow", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("sentar", "sit", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("serrar", "saw", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("temblar", "tremble", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("tentar", "tempt", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("tropezar", "stumble", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE, Irregularity.SPELLING_CHANGE_PHONETIC)),

        // stem change i -> í
        Verb("confiar", "confide, trust", irregularities = listOf(Irregularity.STEM_CHANGE_I_to_I)),
        Verb("enviar", "send", irregularities = listOf(Irregularity.STEM_CHANGE_I_to_I)),
        Verb("espiar", "spy on", irregularities = listOf(Irregularity.STEM_CHANGE_I_to_I)),
        Verb("esquiar", "ski", irregularities = listOf(Irregularity.STEM_CHANGE_I_to_I)),
        Verb("fotografiar", "photograph", irregularities = listOf(Irregularity.STEM_CHANGE_I_to_I)),
        Verb("vaciar", "empty", irregularities = listOf(Irregularity.STEM_CHANGE_I_to_I)),

        // stem change u -> ue
        Verb("jugar", "play", irregularities = listOf(Irregularity.STEM_CHANGE_U_to_UE))
)
val irregularArVerbs = listOf(

        // no accent on preterit
        Verb("andar", "walk", altPreteritRoot = "anduv", irregularities = listOf(Irregularity.NO_ACCENT_ON_PRETERIT)),

        // Custom conjugation
        Verb("criar", "raise", irregularities = listOf(Irregularity.STEM_CHANGE_I_to_I)) { subjectPronoun: SubjectPronoun, conjugationType: ConjugationType ->
            if (subjectPronoun == SubjectPronoun.VOSOTROS) {
                when (conjugationType) {
                    ConjugationType.PRESENT -> "criais"
                    ConjugationType.SUBJUNCTIVE_PRESENT -> "crieis"
                    else -> null
                }
            } else {
                null
            }
        },
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
        },
        Verb("guiar", "guide", irregularities = listOf(Irregularity.STEM_CHANGE_I_to_I)) { subjectPronoun: SubjectPronoun, conjugationType: ConjugationType ->
            if (subjectPronoun == SubjectPronoun.VOSOTROS) {
                when (conjugationType) {
                    ConjugationType.PRESENT -> "guiais"
                    ConjugationType.SUBJUNCTIVE_PRESENT -> "guieis"
                    else -> null
                }
            } else {
                null
            }
        }

)

// IR verbs
val regularIrVerbs = listOf(
        Verb("aburrir", "bore"),
        Verb("admitir", "admit"),
        Verb("añadir", "add"),
        Verb("asistir", "attend"),
        Verb("asumir", "assume"),
        Verb("compartir", "share"),
        Verb("confundir", "confuse"),
        Verb("consumir", "consume"),
        Verb("cumplir", "achieve"),
        Verb("decidir", "decide"),
        Verb("definir", "define"),
        Verb("deprimir", "depress"),
        Verb("discutir", "discuss"),
        Verb("disuadir", "dissuade"),
        Verb("dividir", "divide"),
        Verb("escupir", "spit"),
        Verb("existir", "exist"),
        Verb("imprimir", "print"),
        Verb("insistir", "insist"),
        Verb("interrumpir", "interrupt"),
        Verb("invadir", "invade"),
        Verb("ocurrir", "occur"),
        Verb("omitir", "omit"),
        Verb("partir", "cut, split"),
        Verb("percibir", "perceive"),
        Verb("permitir", "permit"),
        Verb("persistir", "persist"),
        Verb("persuadir", "persusade"),
        Verb("recibir", "receive"),
        Verb("subir", "go up, climb"),
        Verb("sufrir", "suffer"),
        Verb("unir", "join, unite"),
        Verb("vivir", "live")
)
val spellingChangeIrVerbs = listOf(
        // spelling change - phonetic
        Verb("dirigir", "manage, direct", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("distinguir", "distinguish", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("construir", "build", irregularities = listOf(Irregularity.SPELLING_CHANGE_Y)),
        Verb("fluir", "flow", irregularities = listOf(Irregularity.SPELLING_CHANGE_Y)),
        Verb("huir", "escape, flee", irregularities = listOf(Irregularity.SPELLING_CHANGE_Y)),

        // Yo Go verbs
        Verb("oír", "hear", altInfinitiveRoot = "oir", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO)),
        Verb("desoír", "ignore", altInfinitiveRoot = "desoir", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO)),
        Verb("salir", "go out, leave", altInfinitiveRoot = "saldr", irregularImperativeTu = "sal", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO))
)
val stemChangeIrVerbs = listOf(
        // stem changes e -> i
        Verb("elegir", "choose", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC, Irregularity.STEM_CHANGE_E_to_I)),
        Verb("competir", "compete", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_I)),
        Verb("conseguir", "get", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_I)),
        Verb("corregir", "correct", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_I, Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("derretir", "melt", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_I)),
        Verb("despedir", "say goodbye", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_I)),
        Verb("elegir", "choose", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_I)),
        Verb("estreñir", "constipate", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_I)),
        Verb("freír", "fry", irregularPastParticiple = "frito", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_I)),
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

        // stem changes i -> í
        Verb("prohibir", "prohibit", irregularities = listOf(Irregularity.STEM_CHANGE_I_to_I)),

        // stem changes o -> ue
        Verb("dormir", "sleep", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("morir", "die", irregularPastParticiple = "muerto", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),

        // stem changes e -> ie
        Verb("asentir", "agree, assent, nod", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("consentir", "allow", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("convertir", "turn into", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("desmentir", "deny", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("diferir", "differ", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("digerir", "digest", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("discernir", "discern", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("disentir", "disagree, dissent", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("divertir", "amuse, entertain", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("herir", "wound", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("hervir", "boil", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("inferir", "infer", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("invertir", "invest", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("mentir", "lie", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("preferir", "prefer", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("referir", "refer", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("sentir", "feel", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("sugerir", "suggest", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("transferir", "transfer", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE))
)
val irregularIrVerbs = listOf(
        // past participle only
        Verb("abrir", "open", irregularPastParticiple = "abierto"),
        Verb("cubrir", "cover", irregularPastParticiple = "cubierto"),
        Verb("describir", "describe", irregularPastParticiple = "descrito"),
        Verb("descubrir", "discover", irregularPastParticiple = "descubierto"),
        Verb("escribir", "write", irregularPastParticiple = "escrito"),
        Verb("pudrir", "rot, decay", irregularPastParticiple = "podrido"),

        // no accent on preterit
        Verb("conducir", "drive, conduct", altPreteritRoot = "conduj", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("intervenir", "intervene", altPreteritRoot = "intervin", altInfinitiveRoot = "intervendr", irregularImperativeTu = "intervén", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.STEM_CHANGE_E_to_IE, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("prevenir", "prevent", altPreteritRoot = "previn", altInfinitiveRoot = "prevendr", irregularImperativeTu = "prevén", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.STEM_CHANGE_E_to_IE, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("producir", "produce", altPreteritRoot = "produj", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("reducir", "reduce", altPreteritRoot = "reduj", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("reproducir", "reproduce", altPreteritRoot = "reproduj", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("traducir", "translate", altPreteritRoot = "traduj", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("venir", "come", altPreteritRoot = "vin", altInfinitiveRoot = "vendr", irregularImperativeTu = "ven", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.STEM_CHANGE_E_to_IE, Irregularity.NO_ACCENT_ON_PRETERIT)),

        // Custom conjugations
        Verb("contradecir", "contradict", altPreteritRoot = "contradij", altInfinitiveRoot = "contradir", irregularPastParticiple = "contradicho", irregularGerund = "contradiciendo", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.STEM_CHANGE_E_to_I, Irregularity.NO_ACCENT_ON_PRETERIT)) { subjectPronoun: SubjectPronoun, conjugationType: ConjugationType ->
            when (conjugationType) {
                ConjugationType.PRESENT -> if (subjectPronoun == SubjectPronoun.YO) "digo" else null
                else -> null
            }
        },
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

// ER verbs
val regularErVerbs = listOf(
        Verb("absorber", "absorb"),
        Verb("aprender", "learn"),
        Verb("barrer", "sweep"),
        Verb("beber", "drink"),
        Verb("comer", "eat"),
        Verb("cometer", "commit"),
        Verb("compeler", "compel"),
        Verb("comprender", "understand"),
        Verb("conceder", "concede"),
        Verb("correr", "run"),
        Verb("deber", "owe, should"),
        Verb("depender", "depend"),
        Verb("esconder", "hide"),
        Verb("exceder", "exceed"),
        Verb("meter", "put into"),
        Verb("ofender", "offend"),
        Verb("poseer", "possess"),
        Verb("prender", "catch, light"),
        Verb("proceder", "proceed"),
        Verb("prometer", "promise"),
        Verb("reprender", "reprimand"),
        Verb("sorprender", "surprise"),
        Verb("suceder", "happen"),
        Verb("tejer", "knit, weave"),
        Verb("temer", "fear"),
        Verb("toser", "cough"),
        Verb("vender", "sell"))
val spellingChangeErVerbs = listOf(
        // Spelling change
        Verb("creer", "believe", irregularities = listOf(Irregularity.SPELLING_CHANGE_Y)),
        Verb("leer", "read", irregularities = listOf(Irregularity.SPELLING_CHANGE_Y)),
        Verb("poseer", "possess", irregularities = listOf(Irregularity.SPELLING_CHANGE_Y)),
        Verb("proveer", "provide", irregularities = listOf(Irregularity.SPELLING_CHANGE_Y)),

        // Yo Go verbs
        Verb("caer", "fall", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.SPELLING_CHANGE_Y)),
        Verb("valer", "be worth, cost", altInfinitiveRoot = "valdr", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO)),

        // Yo ZC verbs
        Verb("agradecer", "thank", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("amanecer", "dawn, get light", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("anochecer", "get dark", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("aparecer", "appear", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("conocer", "know, meet", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("crecer", "grow", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("desaparecer", "disappear", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("embellecer", "embellish", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("establecer", "establish", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("merecer", "deserve", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("nacer", "be born", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("obedecer", "obey", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("ofrecer", "offer", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("parecer", "seem", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("permanecer", "remain", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("pertenecer", "belong to", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("reconocer", "recognize", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC))
)
val stemChangeErVerbs = listOf(
        // stem changes o -> ue
        Verb("cocer", "cook, bake", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE, Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("demoler", "demolish", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("devolver", "return something", irregularPastParticiple = "devuelto", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("disolver", "dissolve", irregularPastParticiple = "disuelto", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("doler", "hurt", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("envolver", "wrap", irregularPastParticiple = "envuelto", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("llover", " rain", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("moler", "grind", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("morder", "bite", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("mover", "move", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("oler", "smell", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("promover", "promote", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("remover", "remove", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("resolver", "resolve", irregularPastParticiple = "resuelto", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("retorcer", "twist, wring", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE, Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("revolver", "stir, mix, shake", irregularPastParticiple = "revuelto", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("soler", "use to, usually", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("torcer", "twist", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE, Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("volver", "return", irregularPastParticiple = "vuelto", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),

        // stem changes e -> ie
        Verb("ascender", "rise, promote", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("atender", "attend to", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("defender", "defend", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("descender", "fall, descend", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("encender", "light, turn on", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("entender", "understand", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("extender", "extend", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("perder", "lose", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("verter", "pour, spill", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE))
)
val irregularErVerbs = listOf(
        // past participle only
        Verb("romper", "break", irregularPastParticiple = "roto"),

        // no accent on preterit
        Verb("hacer", "make, do", altPreteritRoot = "hic", altInfinitiveRoot = "har", irregularImperativeTu = "haz", irregularPastParticiple = "hecho", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("poder", "can, be able to", altPreteritRoot = "pud", altInfinitiveRoot = "podr", irregularGerund = "pudiendo", irregularities = listOf(Irregularity.NO_ACCENT_ON_PRETERIT, Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("poner", "put", altPreteritRoot = "pus", altInfinitiveRoot = "pondr", irregularImperativeTu = "pon", irregularPastParticiple = "puesto", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("querer", "want", altPreteritRoot = "quis", altInfinitiveRoot = "querr", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("tener", "have", altPreteritRoot = "tuv", altInfinitiveRoot = "tendr", irregularImperativeTu = "ten", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.STEM_CHANGE_E_to_IE, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("traer", "bring", altPreteritRoot = "traj", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.NO_ACCENT_ON_PRETERIT)),

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
        Verb("sostener", "hold, sustain", altPreteritRoot = "sostuv", altInfinitiveRoot = "sostendr", irregularImperativeTu = "sostén", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.STEM_CHANGE_E_to_IE, Irregularity.NO_ACCENT_ON_PRETERIT)),

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
