package org.indiv.dls.games.verboscruzados.model

import org.indiv.dls.games.verboscruzados.R
import org.indiv.dls.games.verboscruzados.conjugation.getIrAlteredRoot

//
// See conjugation rules: https://en.wikipedia.org/wiki/Spanish_irregular_verbs
//
// Also, good source of verbs: http://www.intro2spanish.com/verbs/listas/all.htm

enum class InfinitiveEnding(val ending: String, val indexForStats: Int) {
    AR("ar", 0),
    ER("er", 1),
    IR("ir", 2)
}

enum class Irregularity {
    NO_ACCENT_ON_PRETERIT, // pude/pudo, dije/dijo
    SPELLING_CHANGE_I_to_ACCENTED_I,
    SPELLING_CHANGE_PHONETIC,
    SPELLING_CHANGE_U_to_ACCENTED_U,
    SPELLING_CHANGE_YO_ZC,
    SPELLING_CHANGE_YO_GO,
    SPELLING_CHANGE_Y,
    STEM_CHANGE_E_to_ACCENTED_I,
    STEM_CHANGE_E_to_I,
    STEM_CHANGE_E_to_IE,
    STEM_CHANGE_I_to_IE, // adquirir --> adquiero, inquirir -> inquiero  (only 2)
    STEM_CHANGE_O_to_UE,
    STEM_CHANGE_U_to_UE
}

enum class IrregularityCategory(val indexForStats: Int) {
    REGULAR(0),
    SPELLING_CHANGE(1),
    STEM_CHANGE(2),
    IRREGULAR(3)
}

/**
 * Conjugation type enum
 *
 * @param textResId descriptive text for the conjugation type, displayed on the keyboard.
 * @param indexForStats index of the conjugation type's position in the stats heat map.
 */
enum class ConjugationType(val textResId: Int, val indexForStats: Int) {
    PRESENT(R.string.clue_tense_present, 0),
    PRETERIT(R.string.clue_tense_preterite, 1),
    IMPERFECT(R.string.clue_tense_imperfect, 2),
    CONDITIONAL(R.string.clue_tense_conditional, 3),
    FUTURE(R.string.clue_tense_future, 4),
    IMPERATIVE(R.string.clue_tense_imperative, 5),
    SUBJUNCTIVE_PRESENT(R.string.clue_tense_subjunctive_present, 6),
    SUBJUNCTIVE_IMPERFECT(R.string.clue_tense_subjunctive_imperfect, 7),
    PAST_PARTICIPLE(R.string.clue_tense_past_participle, 8),
    GERUND(R.string.clue_tense_gerund, 9)
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
                val frequency: Int = 1,
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
     * Implementing equals() and hashCode() so that we can ensure no duplicates based on the infinitive.
     */
    override fun equals(other: Any?): Boolean {
        return infinitive == (other as? Verb)?.infinitive
    }

    override fun hashCode(): Int {
        return infinitive.hashCode()
    }
}

// Strong vowels form single-syllable dipthongs when combined with weak vowels
internal val strongVowels = listOf("a", "e", "o")

// AR verbs
val regularArVerbs = listOf(
        Verb("abandonar", "abandon"),
        Verb("abusar", "abuse"),
        Verb("agobiar", "overwhelm"),
        Verb("alabar", "praise, applaud"),
        Verb("acabar", "finish", frequency = 2),
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
        Verb("agotar", "use up, exhaust"),
        Verb("agradar", "please", frequency = 2),
        Verb("agravar", "aggravate, make worse"),
        Verb("aguardar", "expect, await"),
        Verb("ahorrar", "save, not waste"),
        Verb("alegrar", "make happy", frequency = 2),
        Verb("alejar", "move away"),
        Verb("alimentar", "feed"),
        Verb("aliviar", "alleviate"),
        Verb("alquilar", "rent"),
        Verb("amar", "love"),
        Verb("anunciar", "announce"),
        Verb("apoyar", "support, hold up"),
        Verb("apresurar", "hurry"),
        Verb("aprovechar", "take advantage of"),
        Verb("apurar", "use up, exhaust, rush"),
        Verb("arrastrar", "drag"),
        Verb("arreglar", "fix, arrange"),
        Verb("arrojar", "throw"),
        Verb("asegurar", "assure"),
        Verb("asomar", "stick out"),
        Verb("asombrar", "amaze, astonish"),
        Verb("aspirar", "inhale"),
        Verb("asustar", "frighten"),
        Verb("atrapar", "catch"),
        Verb("atar", "tie"),
        Verb("aumentar", "increase, add to"),
        Verb("ayudar", "help", frequency = 3),
        Verb("bailar", "dance"),
        Verb("bajar", "go down", frequency = 3),
        Verb("bañar", "bathe"),
        Verb("bastar", "be enough"),
        Verb("besar", "kiss"),
        Verb("bloquear", "block"),
        Verb("borrar", "delete, erase"),
        Verb("brindar", "toast, offer"),
        Verb("bromear", "joke"),
        Verb("broncear", "tan, bronze"),
        Verb("burlar", "evade, cheat, mock"),
        Verb("callar", "quiet"),
        Verb("cambiar", "change", frequency = 3),
        Verb("caminar", "walk", frequency = 2),
        Verb("cancelar", "cancel"),
        Verb("cansar", "tire"),
        Verb("cantar", "sing", frequency = 3),
        Verb("casar", "marry"),
        Verb("causar", "cause"),
        Verb("celebrar", "celebrate"),
        Verb("cenar", "eat dinner", frequency = 3),
        Verb("cepillar", "brush"),
        Verb("charlar", "chat"),
        Verb("chillar", "shriek"),
        Verb("chupar", "suck"),
        Verb("cobrar", "charge for, earn"),
        Verb("cocinar", "cook"),
        Verb("comentar", "discuss, comment"),
        Verb("comparar", "compare"),
        Verb("completar", "complete"),
        Verb("comprar", "buy", frequency = 3),
        Verb("condenar", "condemn"),
        Verb("congelar", "freeze"),
        Verb("considerar", "consider"),
        Verb("contestar", "answer"),
        Verb("contratar", "hire"),
        Verb("cortar", "cut", frequency = 3),
        Verb("crear", "create"),
        Verb("cuidar", "take care of", frequency = 2),
        Verb("dañar", "damage, harm"),
        Verb("dejar", "leave, let", frequency = 3),
        Verb("deletrear", "spell"),
        Verb("desayunar", "eat breakfast"),
        Verb("descansar", "rest"),
        Verb("desear", "wish, desire"),
        Verb("desmayar", "lose heart, faint"),
        Verb("dibujar", "draw"),
        Verb("disculpar", "excuse, forgive"),
        Verb("disfrutar", "enjoy", frequency = 2),
        Verb("disgustar", "disgust"),
        Verb("doblar", "fold, bend"),
        Verb("duchar", "shower"),
        Verb("dudar", "doubt"),
        Verb("echar", "throw, pour"),
        Verb("empeorar", "worsen"),
        Verb("empujar", "push"),
        Verb("encantar", "love, enchant", frequency = 3),
        Verb("enfadar", "upset, annoy"),
        Verb("enfermar", "get sick"),
        Verb("engañar", "deceive"),
        Verb("enojar", "anger"),
        Verb("enseñar", "teach, show", frequency = 2),
        Verb("entrar", "enter", frequency = 3),
        Verb("escoltar", "escort"),
        Verb("escuchar", "listen to", frequency = 3),
        Verb("esperar", "wait/hope for", frequency = 3),
        Verb("estornudar", "sneeze"),
        Verb("estudiar", "study", frequency = 3),
        Verb("evitar", "avoid"),
        Verb("exagerar", "exaggerate"),
        Verb("expresar", "express, state"),
        Verb("extrañar", "miss"),
        Verb("fallar", "fail"),
        Verb("faltar", "lack, miss"),
        Verb("felicitar", "congratulate"),
        Verb("festejar", "celebrate"),
        Verb("fijar", "fix, fasten, notice"),
        Verb("firmar", "sign"),
        Verb("formar", "form, shape"),
        Verb("frotar", "rub"),
        Verb("frustrar", "frustrate"),
        Verb("fumar", "smoke"),
        Verb("funcionar", "function, work"),
        Verb("ganar", "win, earn"),
        Verb("gastar", "spend, waste"),
        Verb("girar", "turn, spin"),
        Verb("golpear", "hit"),
        Verb("gritar", "shout"),
        Verb("guardar", "guard, protect, keep"),
        Verb("gustar", "like, please"),
        Verb("hablar", "speak", frequency = 3),
        Verb("hallar", "find"),
        Verb("hornear", "bake"),
        Verb("imaginar", "imagine"),
        Verb("importar", "import, be important"),
        Verb("iniciar", "initiate, start"),
        Verb("intentar", "try"),
        Verb("interesar", "interest"),
        Verb("invitar", "invite"),
        Verb("jalar", "pull"),
        Verb("juntar", "bring together"),
        Verb("jurar", "swear"),
        Verb("lastimar", "hurt"),
        Verb("lavar", "wash"),
        Verb("levantar", "raise, lift", frequency = 2),
        Verb("limpiar", "clean", frequency = 2),
        Verb("llamar", "call", frequency = 3),
        Verb("llenar", "fill"),
        Verb("llevar", "take, wear", frequency = 3),
        Verb("llorar", "cry"),
        Verb("lograr", "obtain, achieve"),
        Verb("luchar", "fight, battle"),
        Verb("manchar", "stain"),
        Verb("mandar", "send, order"),
        Verb("manejar", "drive, manage"),
        Verb("marchar", "walk, leave, march"),
        Verb("matar", "kill"),
        Verb("mejorar", "improve"),
        Verb("mencionar", "mention"),
        Verb("mirar", "look at", frequency = 3),
        Verb("mojar", "get wet"),
        Verb("molestar", "bother"),
        Verb("montar", "ride"),
        Verb("nadar", "swim"),
        Verb("necesitar", "need", frequency = 3),
        Verb("notar", "note, notice"),
        Verb("observar", "observe"),
        Verb("ocultar", "hide, conceal"),
        Verb("odiar", "hate"),
        Verb("objetar", "object"),
        Verb("ocupar", "occupy"),
        Verb("ojear", "have a look at"),
        Verb("olvidar", "forget"),
        Verb("orar", "pray"),
        Verb("ordenar", "order, arrange"),
        Verb("parar", "stop, stand"),
        Verb("pasar", "pass, happen", frequency = 3),
        Verb("pasear", "take a walk"),
        Verb("patinar", "skate"),
        Verb("peinar", "comb, style"),
        Verb("pelear", "fight"),
        Verb("perdonar", "pardon, forgive", frequency = 2),
        Verb("pesar", "weigh, be heavy"),
        Verb("pintar", "paint"),
        Verb("pisar", "step on, tread on"),
        Verb("preguntar", "ask", frequency = 3),
        Verb("preocupar", "worry"),
        Verb("preparar", "prepare"),
        Verb("presentar", "present, introduce"),
        Verb("prestar", "lend, borrow"),
        Verb("pronunciar", "pronounce"),
        Verb("quedar", "stay, remain", frequency = 2),
        Verb("quejar", "complain"),
        Verb("quemar", "burn"),
        Verb("quitar", "remove, take away"),
        Verb("rebajar", "reduce, lower price"),
        Verb("regresar", "return", frequency = 2),
        Verb("regalar", "give as a gift"),
        Verb("registrar", "search, register, record"),
        Verb("relajar", "relax, calm"),
        Verb("reparar", "fix"),
        Verb("representar", "represent"),
        Verb("respetar", "respect"),
        Verb("resultar", "result, turn out"),
        Verb("retirar", "withdraw, retire"),
        Verb("retrasar", "delay, postpone"),
        Verb("robar", "steal"),
        Verb("saltar", "jump"),
        Verb("serenar", "calm"),
        Verb("señalar", "point out, indicate"),
        Verb("saludar", "greet"),
        Verb("sobrar", "be left over"),
        Verb("soplar", "blow"),
        Verb("sospechar", "suspect"),
        Verb("sujetar", "hold, fasten"),
        Verb("tapar", "cover"),
        Verb("tardar", "take time"),
        Verb("telefonear", "phone"),
        Verb("terminar", "end, finish"),
        Verb("tirar", "throw, throw away"),
        Verb("tomar", "take, drink", frequency = 3),
        Verb("trabajar", "work", frequency = 3),
        Verb("tratar", "treat, try, handle"),
        Verb("trepar", "climb"),
        Verb("triunfar", "triumph, succeed"),
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
        Verb("aplicar", "apply", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("arrancar", "pull out", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("apagar", "turn off", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("atacar", "attack", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("avanzar", "advance, move forward", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("averiguar", "find out", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("bostezar", "yawn", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("buscar", "look for", frequency = 3, irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
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
        Verb("gozar", "enjoy oneself", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("identificar", "identify", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("indicar", "indicate", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("investigar", "investigate", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("justificar", "justify", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("juzgar", "judge", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("lanzar", "throw, hurl", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("llegar", "arrive", frequency = 3, irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("localizar", "locate", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("marcar", "mark", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("masticar", "chew", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("modificar", "modify", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("notificar", "notify", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("obligar", "obligate", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("organizar", "organize", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("pagar", "pay", frequency = 2, irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("pegar", "hit, stick, glue", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("pescar", "fish", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("picar", "sting, itch, bite", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("practicar", "practice", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("propagar", "propagate", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("provocar", "provoke, cause", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("realizar", "carry out, fulfill", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("replicar", "reply", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("rezar", "pray", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("roncar", "snore", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("sacar", "take out, remove", frequency = 2, irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("secar", "dry", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("significar", "signify, mean", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("tocar", "touch, play instrument", frequency = 2, irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("tragar", "swallow", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("utilizar", "use, utilize", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("verificar", "verify", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),

        // spelling change i -> í
        Verb("aislar", "isolate, insulate", irregularities = listOf(Irregularity.SPELLING_CHANGE_I_to_ACCENTED_I)),
        Verb("ansiar", "long for", irregularities = listOf(Irregularity.SPELLING_CHANGE_I_to_ACCENTED_I)),
        Verb("chirriar", "screech, creak", irregularities = listOf(Irregularity.SPELLING_CHANGE_I_to_ACCENTED_I)),
        Verb("confiar", "confide, trust", irregularities = listOf(Irregularity.SPELLING_CHANGE_I_to_ACCENTED_I)),
        Verb("criar", "raise, breed", irregularities = listOf(Irregularity.SPELLING_CHANGE_I_to_ACCENTED_I)),
        Verb("desviar", "divert", irregularities = listOf(Irregularity.SPELLING_CHANGE_I_to_ACCENTED_I)),
        Verb("enviar", "send", irregularities = listOf(Irregularity.SPELLING_CHANGE_I_to_ACCENTED_I)),
        Verb("enfriar", "cool", irregularities = listOf(Irregularity.SPELLING_CHANGE_I_to_ACCENTED_I)),
        Verb("espiar", "spy on", irregularities = listOf(Irregularity.SPELLING_CHANGE_I_to_ACCENTED_I)),
        Verb("esquiar", "ski", irregularities = listOf(Irregularity.SPELLING_CHANGE_I_to_ACCENTED_I)),
        Verb("fotografiar", "photograph", irregularities = listOf(Irregularity.SPELLING_CHANGE_I_to_ACCENTED_I)),
        Verb("guiar", "guide", irregularities = listOf(Irregularity.SPELLING_CHANGE_I_to_ACCENTED_I)),
        Verb("piar", "cheep, tweet", irregularities = listOf(Irregularity.SPELLING_CHANGE_I_to_ACCENTED_I)),
        Verb("vaciar", "empty", irregularities = listOf(Irregularity.SPELLING_CHANGE_I_to_ACCENTED_I)),

        // spelling change u -> ú
        Verb("actuar", "act", irregularities = listOf(Irregularity.SPELLING_CHANGE_U_to_ACCENTED_U)),
        Verb("atenuar", "attenuate", irregularities = listOf(Irregularity.SPELLING_CHANGE_U_to_ACCENTED_U)),
        Verb("aullar", "howl", irregularities = listOf(Irregularity.SPELLING_CHANGE_U_to_ACCENTED_U)),
        Verb("continuar", "continue", irregularities = listOf(Irregularity.SPELLING_CHANGE_U_to_ACCENTED_U)),
        Verb("evaluar", "evaluate", irregularities = listOf(Irregularity.SPELLING_CHANGE_U_to_ACCENTED_U)),
        Verb("fluctuar", "fluctuate", irregularities = listOf(Irregularity.SPELLING_CHANGE_U_to_ACCENTED_U)),
        Verb("habituar", "habituate", irregularities = listOf(Irregularity.SPELLING_CHANGE_U_to_ACCENTED_U)),
        Verb("maullar", "meow", irregularities = listOf(Irregularity.SPELLING_CHANGE_U_to_ACCENTED_U)),
        Verb("insinuar", "insinuate", irregularities = listOf(Irregularity.SPELLING_CHANGE_U_to_ACCENTED_U)),
        Verb("perpetuar", "perpetuate", irregularities = listOf(Irregularity.SPELLING_CHANGE_U_to_ACCENTED_U)),
        Verb("puntuar", "punctuate, score", irregularities = listOf(Irregularity.SPELLING_CHANGE_U_to_ACCENTED_U)),
        Verb("rehusar", "refuse", irregularities = listOf(Irregularity.SPELLING_CHANGE_U_to_ACCENTED_U)),
        Verb("reunir", "bring together", irregularities = listOf(Irregularity.SPELLING_CHANGE_U_to_ACCENTED_U)),
        Verb("situar", "place", irregularities = listOf(Irregularity.SPELLING_CHANGE_U_to_ACCENTED_U)),
        Verb("tatuar", "tattoo", irregularities = listOf(Irregularity.SPELLING_CHANGE_U_to_ACCENTED_U)),
        Verb("valuar", "value", irregularities = listOf(Irregularity.SPELLING_CHANGE_U_to_ACCENTED_U))
)
val stemChangeArVerbs = listOf(
        // stem change o -> ue
        Verb("acordar", "agree", frequency = 2, irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("acostar", "put to bed", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("almorzar", "eat lunch", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE, Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("apostar", "bet", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("aprobar", "approve", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("avergonzar", "embarrass", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE, Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("colgar", "hang", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE, Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("consolar", "console", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("contar", "tell, count", frequency = 3, irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("costar", "cost", frequency = 2, irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("demostrar", "demonstrate, prove", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("descontar", "discount, deduct, exclude", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("encontrar", "meet, find", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("esforzar", "strain", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE, Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("forzar", "force", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE, Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("mostrar", "show", frequency = 3, irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("probar", "test, taste, prove", frequency = 2, irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
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
        Verb("alentar", "encourage", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("apretar", "press, squeeze", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("atravesar", "cross", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("calentar", "warm", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("cerrar", "close", frequency = 3, irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("comenzar", "begin, start", frequency = 3, irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE, Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("confesar", "confess", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("despertar", "wake", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("desterrar", "banish", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("empezar", "begin", frequency = 3, irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE, Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("encerrar", "enclose, lock up", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("enterrar", "bury", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("errar", "miss, wander", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("fregar", "scrub", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE, Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("gobernar", "govern", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("helar", "freeze", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("merendar", "snack", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("negar", "deny, negate", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE, Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("nevar", "snow", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("pensar", "think", frequency = 3, irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("quebrar", "break", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("recomendar", "recommend", frequency = 2, irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("regar", "water", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE, Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("remendar", "mend", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("sembrar", "plant, sow", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("sentar", "sit", frequency = 3, irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("serrar", "saw", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("temblar", "tremble", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("tentar", "tempt", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("tropezar", "stumble", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE, Irregularity.SPELLING_CHANGE_PHONETIC)),

        // stem change u -> ue
        Verb("jugar", "play", frequency = 3, irregularities = listOf(Irregularity.STEM_CHANGE_U_to_UE))
)
val irregularArVerbs = listOf(

        // no accent on preterit
        Verb("andar", "walk", altPreteritRoot = "anduv", irregularities = listOf(Irregularity.NO_ACCENT_ON_PRETERIT)),

        // Custom conjugation
        Verb("dar", "give", frequency = 3, irregularities = listOf(Irregularity.NO_ACCENT_ON_PRETERIT)) { subjectPronoun: SubjectPronoun, conjugationType: ConjugationType ->
            when (conjugationType) {
                ConjugationType.PRESENT -> {
                    when (subjectPronoun) {
                        SubjectPronoun.YO -> "doy"
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
                        else -> null
                    }
                }
                else -> null
            }
        },
        Verb("estar", "be", frequency = 3, altPreteritRoot = "estuv", irregularities = listOf(Irregularity.NO_ACCENT_ON_PRETERIT)) { subjectPronoun: SubjectPronoun, conjugationType: ConjugationType ->
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

// IR verbs
val regularIrVerbs = listOf(
        Verb("abatir", "shoot down, take down"),
        Verb("aburrir", "bore, tire"),
        Verb("acudir", "come, turn to"),
        Verb("admitir", "admit"),
        Verb("añadir", "add"),
        Verb("aplaudir", "applaud, clap"),
        Verb("asistir", "attend"),
        Verb("asumir", "assume"),
        Verb("coincidir", "coincide"),
        Verb("compartir", "share"),
        Verb("confundir", "confuse"),
        Verb("consistir", "consist of"),
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
        Verb("persuadir", "persuade"),
        Verb("pulir", "polish"),
        Verb("recibir", "receive"),
        Verb("recurrir", "turn to, resort to"),
        Verb("sacudir", "shake, beat"),
        Verb("subir", "go up, climb", frequency = 3),
        Verb("sufrir", "suffer"),
        Verb("suprimir", "eliminate, suppress"),
        Verb("unir", "join, unite"),
        Verb("vivir", "live", frequency = 3)
)
val spellingChangeIrVerbs = listOf(
        // spelling change - phonetic
        Verb("bullir", "boil, seethe", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("delinquir", "commit a crime", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("dirigir", "manage, direct", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("distinguir", "distinguish", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("esparcir", "scatter, spread", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("exigir", "demand, require", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("fingir", "feign, pretend", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("gruñir", "grumble, grunt, growl", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("sumergir", "submerge", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("surgir", "arise", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),

        // spelling change - y
        Verb("concluir", "conclude", irregularities = listOf(Irregularity.SPELLING_CHANGE_Y)),
        Verb("construir", "build", irregularities = listOf(Irregularity.SPELLING_CHANGE_Y)),
        Verb("constituir", "constitute", irregularities = listOf(Irregularity.SPELLING_CHANGE_Y)),
        Verb("contribuir", "contribute", irregularities = listOf(Irregularity.SPELLING_CHANGE_Y)),
        Verb("destruir", "destroy", irregularities = listOf(Irregularity.SPELLING_CHANGE_Y)),
        Verb("diluir", "dilute", irregularities = listOf(Irregularity.SPELLING_CHANGE_Y)),
        Verb("distribuir", "distribute", irregularities = listOf(Irregularity.SPELLING_CHANGE_Y)),
        Verb("excluir", "exclude", irregularities = listOf(Irregularity.SPELLING_CHANGE_Y)),
        Verb("fluir", "flow", irregularities = listOf(Irregularity.SPELLING_CHANGE_Y)),
        Verb("huir", "escape, flee", irregularities = listOf(Irregularity.SPELLING_CHANGE_Y)),
        Verb("incluir", "include", irregularities = listOf(Irregularity.SPELLING_CHANGE_Y)),
        Verb("influir", "influence", irregularities = listOf(Irregularity.SPELLING_CHANGE_Y)),
        Verb("instruir", "instruct", irregularities = listOf(Irregularity.SPELLING_CHANGE_Y)),
        Verb("obstruir", "obstruct", irregularities = listOf(Irregularity.SPELLING_CHANGE_Y)),

        // spelling change i -> accented í
        Verb("prohibir", "prohibit", irregularities = listOf(Irregularity.SPELLING_CHANGE_I_to_ACCENTED_I)),

        // Yo Go verbs
        Verb("asir", "seize, grasp", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO)),
        Verb("desoír", "ignore", altInfinitiveRoot = "desoir", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO)),
        Verb("oír", "hear", frequency = 3, altInfinitiveRoot = "oir", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO)),
        Verb("salir", "go out, leave", frequency = 3, altInfinitiveRoot = "saldr", irregularImperativeTu = "sal", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO))
)
val stemChangeIrVerbs = listOf(
        // stem changes e -> i
        Verb("colegir", "deduce, gather", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_I, Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("competir", "compete", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_I)),
        Verb("concebir", "conceive", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_I)),
        Verb("conseguir", "get", frequency = 2, irregularities = listOf(Irregularity.STEM_CHANGE_E_to_I)),
        Verb("constreñir", "constrain", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_I)),
        Verb("corregir", "correct", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_I, Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("derretir", "melt", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_I)),
        Verb("despedir", "say goodbye", frequency = 2, irregularities = listOf(Irregularity.STEM_CHANGE_E_to_I)),
        Verb("desvestir", "undress", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_I)),
        Verb("elegir", "choose, elect", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC, Irregularity.STEM_CHANGE_E_to_I)),
        Verb("estreñir", "constipate", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_I)),
        Verb("gemir", "groan", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_I)),
        Verb("impedir", "impede", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_I)),
        Verb("medir", "measure", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_I)),
        Verb("pedir", "ask for", frequency = 3, irregularities = listOf(Irregularity.STEM_CHANGE_E_to_I)),
        Verb("perseguir", "pursue", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_I, Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("rendir", "produce, defeat", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_I, Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("repetir", "repeat", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_I)),
        Verb("seguir", "follow", frequency = 3, irregularities = listOf(Irregularity.STEM_CHANGE_E_to_I, Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("servir", "serve", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_I)),
        Verb("vestir", "dress, wear", frequency = 2, irregularities = listOf(Irregularity.STEM_CHANGE_E_to_I)),

        // stem changes e -> accented í
        Verb("reír", "laugh", frequency = 2, altInfinitiveRoot = "reir", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_ACCENTED_I)),
        Verb("freír", "fry", altInfinitiveRoot = "freir", irregularPastParticiple = "frito", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_ACCENTED_I)),
        Verb("sonreír", "smile", altInfinitiveRoot = "sonreir", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_ACCENTED_I)),

        // stem changes o -> ue
        Verb("dormir", "sleep", frequency = 3, irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("morir", "die", irregularPastParticiple = "muerto", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),

        // stem changes i -> ie
        Verb("adquirir", "acquire, purchase", irregularities = listOf(Irregularity.STEM_CHANGE_I_to_IE)),
        Verb("inquirir", "inquire into, investigate", irregularities = listOf(Irregularity.STEM_CHANGE_I_to_IE)),

        // stem changes e -> ie
        Verb("advertir", "warn, advise, notice", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
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
        Verb("interferir", "interfere", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("invertir", "invert, invest", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("mentir", "lie", frequency = 2, irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("preferir", "prefer", frequency = 2, irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("referir", "refer", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("requerir", "require", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("sentir", "feel", frequency = 3, irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("sugerir", "suggest, hint", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("transferir", "transfer", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE))
)
val irregularIrVerbs = listOf(
        // past participle only
        Verb("abrir", "open", frequency = 3, irregularPastParticiple = "abierto"),
        Verb("cubrir", "cover", frequency = 2, irregularPastParticiple = "cubierto"),
        Verb("describir", "describe", irregularPastParticiple = "descrito"),
        Verb("descubrir", "discover", irregularPastParticiple = "descubierto"),
        Verb("escribir", "write", frequency = 3, irregularPastParticiple = "escrito"),
        Verb("inscribir", "engrave, enroll, record", irregularPastParticiple = "inscrito"),
        Verb("pudrir", "rot, decay", irregularPastParticiple = "podrido"),
        Verb("subscribir", "subscribe", irregularPastParticiple = "subscrito"),

        // no accent on preterit
        Verb("bendecir", "bless", altPreteritRoot = "bendij", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.STEM_CHANGE_E_to_I, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("conducir", "drive, conduct", altPreteritRoot = "conduj", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("contradecir", "contradict", altPreteritRoot = "contradij", altInfinitiveRoot = "contradir", irregularPastParticiple = "contradicho", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.STEM_CHANGE_E_to_I, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("convenir", "be advisable, agree", altPreteritRoot = "convin", altInfinitiveRoot = "convendr", irregularImperativeTu = "convén", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.STEM_CHANGE_E_to_IE, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("decir", "say, tell", frequency = 3, altPreteritRoot = "dij", altInfinitiveRoot = "dir", irregularImperativeTu = "di", irregularPastParticiple = "dicho", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.STEM_CHANGE_E_to_I, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("inducir", "lead to, induce", altPreteritRoot = "induj", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("intervenir", "intervene", altPreteritRoot = "intervin", altInfinitiveRoot = "intervendr", irregularImperativeTu = "intervén", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.STEM_CHANGE_E_to_IE, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("introducir", "insert, introduce", altPreteritRoot = "introduj", irregularities = listOf(Irregularity.NO_ACCENT_ON_PRETERIT, Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("maldecir", "curse", altPreteritRoot = "maldij", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.STEM_CHANGE_E_to_I, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("predecir", "predict", altPreteritRoot = "predij", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.STEM_CHANGE_E_to_I, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("prevenir", "prevent", altPreteritRoot = "previn", altInfinitiveRoot = "prevendr", irregularImperativeTu = "prevén", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.STEM_CHANGE_E_to_IE, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("producir", "produce", altPreteritRoot = "produj", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("reducir", "reduce", altPreteritRoot = "reduj", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("reproducir", "reproduce", altPreteritRoot = "reproduj", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("traducir", "translate", altPreteritRoot = "traduj", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("venir", "come", frequency = 3, altPreteritRoot = "vin", altInfinitiveRoot = "vendr", irregularImperativeTu = "ven", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.STEM_CHANGE_E_to_IE, Irregularity.NO_ACCENT_ON_PRETERIT)),

        // c -> zc
        Verb("lucir", "shine, wear", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),

        // Custom conjugations
        Verb("ir", "go", frequency = 3, altPreteritRoot = "fu", irregularities = listOf(Irregularity.NO_ACCENT_ON_PRETERIT, Irregularity.SPELLING_CHANGE_PHONETIC, Irregularity.STEM_CHANGE_E_to_I)) { subjectPronoun: SubjectPronoun, conjugationType: ConjugationType ->
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
        Verb("aprender", "learn", frequency = 3),
        Verb("atrever", "dare"),
        Verb("barrer", "sweep"),
        Verb("beber", "drink", frequency = 3),
        Verb("comer", "eat", frequency = 3),
        Verb("cometer", "commit"),
        Verb("compeler", "compel"),
        Verb("comprender", "understand", frequency = 3),
        Verb("conceder", "concede"),
        Verb("correr", "run", frequency = 3),
        Verb("corresponder", "correspond to, reciprocate"),
        Verb("deber", "owe, should", frequency = 3),
        Verb("depender", "depend"),
        Verb("esconder", "hide"),
        Verb("exceder", "exceed"),
        Verb("lamer", "lick"),
        Verb("meter", "put into", frequency = 2),
        Verb("ofender", "offend"),
        Verb("prender", "catch, light"),
        Verb("pretender", "attempt, intend, claim"),
        Verb("proceder", "proceed"),
        Verb("prometer", "promise"),
        Verb("recorrer", "travel around"),
        Verb("reprender", "reprimand"),
        Verb("responder", "respond, reply", frequency = 2),
        Verb("socorrer", "help, aid, relieve"),
        Verb("sorprender", "surprise", frequency = 2),
        Verb("suceder", "happen"),
        Verb("tejer", "knit, weave"),
        Verb("temer", "fear", frequency = 2),
        Verb("toser", "cough"),
        Verb("vender", "sell", frequency = 3))
val spellingChangeErVerbs = listOf(
        // Spelling change - phonetic
        Verb("coger", "take, catch", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("convencer", "convince", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("ejercer", "exert, practice a profession", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("escoger", "choose", frequency = 2, irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("proteger", "protect", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("recoger", "pick up, gather", frequency = 2, irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("tañer", "strum", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("vencer", "defeat, overcome", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),

        // Spelling change - y
        Verb("creer", "believe", frequency = 3, irregularities = listOf(Irregularity.SPELLING_CHANGE_Y)),
        Verb("descreer", "disbelieve", irregularities = listOf(Irregularity.SPELLING_CHANGE_Y)),
        Verb("leer", "read", frequency = 3, irregularities = listOf(Irregularity.SPELLING_CHANGE_Y)),
        Verb("poseer", "possess", irregularities = listOf(Irregularity.SPELLING_CHANGE_Y)),
        Verb("proveer", "provide", irregularities = listOf(Irregularity.SPELLING_CHANGE_Y)),
        Verb("releer", "reread", irregularities = listOf(Irregularity.SPELLING_CHANGE_Y)),

        // Yo Go verbs
        Verb("caer", "fall", frequency = 3, irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.SPELLING_CHANGE_Y)),
        Verb("valer", "be worth, cost", altInfinitiveRoot = "valdr", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO)),

        // Yo ZC verbs
        Verb("agradecer", "thank", frequency = 2, irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("amanecer", "dawn, get light", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("anochecer", "become night", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("aparecer", "appear", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("apetecer", "want, crave", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("atardecer", "get dark", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("complacer", "please", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("conocer", "know, meet", frequency = 3, irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("crecer", "grow", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("desaparecer", "disappear", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("embellecer", "embellish", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("establecer", "establish", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("merecer", "deserve", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("nacer", "be born", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("obedecer", "obey", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("ofrecer", "offer", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("parecer", "seem", frequency = 3, irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("permanecer", "remain", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("pertenecer", "belong to", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("placer", "please, gratify", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("prevalecer", "prevail", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("reconocer", "recognize", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC))
)
val stemChangeErVerbs = listOf(
        // stem changes o -> ue
        Verb("absolver", "absolve, acquit", irregularPastParticiple = "absuelto", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("cocer", "cook, bake", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE, Irregularity.SPELLING_CHANGE_PHONETIC)),
        Verb("conmover", "move emotionally, shake", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("demoler", "demolish", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("devolver", "return something", irregularPastParticiple = "devuelto", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("disolver", "dissolve", irregularPastParticiple = "disuelto", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("doler", "hurt", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("envolver", "wrap", irregularPastParticiple = "envuelto", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("llover", " rain", frequency = 2, irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
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
        Verb("volver", "return", frequency = 2, irregularPastParticiple = "vuelto", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),

        // stem changes e -> ie
        Verb("ascender", "rise, promote", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("atender", "attend to", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("defender", "defend", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("descender", "fall, descend", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("encender", "light, turn on", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("entender", "understand", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("extender", "extend", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("perder", "lose", frequency = 3, irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("tender", "hang, lay out", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("verter", "pour, spill", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE))
)
val irregularErVerbs = listOf(
        // past participle only
        Verb("romper", "break", frequency = 3, irregularPastParticiple = "roto"),

        // no accent on preterit
        Verb("componer", "compose, prepare", altPreteritRoot = "compus", altInfinitiveRoot = "compondr", irregularImperativeTu = "compón", irregularPastParticiple = "compuesto", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("contener", "contain", altPreteritRoot = "contuv", altInfinitiveRoot = "contendr", irregularImperativeTu = "contén", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.STEM_CHANGE_E_to_IE, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("contraponer", "compare, contrast", altPreteritRoot = "contrapus", altInfinitiveRoot = "contrapondr", irregularImperativeTu = "contrapón", irregularPastParticiple = "contrapuesto", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("deshacer", "undo", altPreteritRoot = "deshic", altInfinitiveRoot = "deshar", irregularImperativeTu = "deshaz", irregularPastParticiple = "deshecho", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("detener", "stop", altPreteritRoot = "detuv", altInfinitiveRoot = "detendr", irregularImperativeTu = "detén", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.STEM_CHANGE_E_to_IE, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("disponer", "arrange, set out", altPreteritRoot = "dispus", altInfinitiveRoot = "dispondr", irregularImperativeTu = "dispón", irregularPastParticiple = "dispuesto", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("distraer", "distract", altPreteritRoot = "distraj", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("entretener", "entertain", altPreteritRoot = "entretuv", altInfinitiveRoot = "entretendr", irregularImperativeTu = "entretén", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.STEM_CHANGE_E_to_IE, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("exponer", "expose", altPreteritRoot = "expus", altInfinitiveRoot = "expondr", irregularImperativeTu = "expón", irregularPastParticiple = "expuesto", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("extraer", "extract", altPreteritRoot = "extraj", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("hacer", "make, do", frequency = 3, altPreteritRoot = "hic", altInfinitiveRoot = "har", irregularImperativeTu = "haz", irregularPastParticiple = "hecho", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("imponer", "impose", altPreteritRoot = "impus", altInfinitiveRoot = "impondr", irregularImperativeTu = "impón", irregularPastParticiple = "impuesto", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("mantener", "maintain", altPreteritRoot = "mantuv", altInfinitiveRoot = "mantendr", irregularImperativeTu = "mantén", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.STEM_CHANGE_E_to_IE, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("obtener", "obtain", altPreteritRoot = "obtuv", altInfinitiveRoot = "obtendr", irregularImperativeTu = "obtén", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.STEM_CHANGE_E_to_IE, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("oponer", "oppose", altPreteritRoot = "opus", altInfinitiveRoot = "opondr", irregularImperativeTu = "opón", irregularPastParticiple = "opuesto", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("poder", "can, be able to", frequency = 3, altPreteritRoot = "pud", altInfinitiveRoot = "podr", irregularGerund = "pudiendo", irregularities = listOf(Irregularity.NO_ACCENT_ON_PRETERIT, Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("posponer", "postpone", altPreteritRoot = "pospus", altInfinitiveRoot = "pospondr", irregularImperativeTu = "pospón", irregularPastParticiple = "pospuesto", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("poner", "put", frequency = 3, altPreteritRoot = "pus", altInfinitiveRoot = "pondr", irregularImperativeTu = "pon", irregularPastParticiple = "puesto", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("proponer", "propose", altPreteritRoot = "propus", altInfinitiveRoot = "propondr", irregularImperativeTu = "propón", irregularPastParticiple = "propuesto", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("querer", "want", frequency = 3, altPreteritRoot = "quis", altInfinitiveRoot = "querr", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("retener", "retain, keep", altPreteritRoot = "retuv", altInfinitiveRoot = "retendr", irregularImperativeTu = "retén", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.STEM_CHANGE_E_to_IE, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("satisfacer", "satisfy", altPreteritRoot = "satisfic", altInfinitiveRoot = "satisfar", irregularImperativeTu = "satisfaz", irregularPastParticiple = "satisfecho", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("sostener", "hold, sustain", altPreteritRoot = "sostuv", altInfinitiveRoot = "sostendr", irregularImperativeTu = "sostén", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.STEM_CHANGE_E_to_IE, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("suponer", "suppose", altPreteritRoot = "supus", altInfinitiveRoot = "supondr", irregularImperativeTu = "supón", irregularPastParticiple = "supuesto", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("tener", "have", frequency = 3, altPreteritRoot = "tuv", altInfinitiveRoot = "tendr", irregularImperativeTu = "ten", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.STEM_CHANGE_E_to_IE, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("traer", "bring", frequency = 3, altPreteritRoot = "traj", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.NO_ACCENT_ON_PRETERIT)),

        // Custom conjugations
        Verb("caber", "fit", frequency = 2, altPreteritRoot = "cup", altInfinitiveRoot = "cabr", irregularities = listOf(Irregularity.NO_ACCENT_ON_PRETERIT)) { subjectPronoun: SubjectPronoun, conjugationType: ConjugationType ->
            when (conjugationType) {
                ConjugationType.PRESENT -> if (subjectPronoun == SubjectPronoun.YO) "quepo" else null
                else -> null
            }
        },
        Verb("haber", "have, exist", frequency = 3, altPreteritRoot = "hub", altInfinitiveRoot = "habr", irregularities = listOf(Irregularity.NO_ACCENT_ON_PRETERIT)) { subjectPronoun: SubjectPronoun, conjugationType: ConjugationType ->
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
        Verb("saber", "know", frequency = 3, altPreteritRoot = "sup", altInfinitiveRoot = "sabr", irregularities = listOf(Irregularity.NO_ACCENT_ON_PRETERIT)) { subjectPronoun: SubjectPronoun, conjugationType: ConjugationType ->
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
        Verb("ser", "be", frequency = 3, altPreteritRoot = "fu", irregularImperativeTu = "sé", irregularities = listOf(Irregularity.NO_ACCENT_ON_PRETERIT)) { subjectPronoun: SubjectPronoun, conjugationType: ConjugationType ->
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
        Verb("ver", "see", frequency = 3, irregularPastParticiple = "visto", irregularities = listOf(Irregularity.NO_ACCENT_ON_PRETERIT)) { subjectPronoun: SubjectPronoun, conjugationType: ConjugationType ->
            when (conjugationType) {
                ConjugationType.PRESENT -> {
                    when (subjectPronoun) {
                        SubjectPronoun.YO -> "veo"
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
