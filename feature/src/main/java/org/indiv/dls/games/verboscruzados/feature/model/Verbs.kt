package org.indiv.dls.games.verboscruzados.feature.model

enum class InfinitiveEnding(ending: String) {
    ER("er"),
    AR("ar"),
    IR("ir")
}

enum class Irregularity {
    GERUND,
    NO_ACCENT_ON_PRETERIT, // pude/pudo, dije/dijo
    PAST_PARTICIPLE,
    SPELLING_CHANGE_PHONETIC,
    SPELLING_CHANGE_YO_ZC,
    SPELLING_CHANGE_YO_GO,
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
    VOSOTROS("Vosotros")
}

data class Verb(val infinitive: String,
                val translation: String,
                val requiresDirectObject: Boolean = false,
                val irregularities: List<Irregularity> = emptyList(),
                val irregularGerund: String? = null,
                val irregularPastParticiple: String? = null,
                val altPreteritRoot: String? = null, // e.g. supe, quepo
                val altInfinitiveRoot: String? = null, // used in future, conditional (e.g. poder/podr)
                val customConjugation: ((SubjectPronoun, ConjugationType) -> String?)? = null) {
    val infinitiveEnding = when {
        infinitive.endsWith("ar") -> InfinitiveEnding.AR
        infinitive.endsWith("er") -> InfinitiveEnding.ER
        else -> InfinitiveEnding.IR // this includes "ír" as in "oír"
    }
    val root = infinitive.substring(0, infinitive.length - 2)
    val gerund = irregularGerund ?: when (infinitiveEnding) {
        InfinitiveEnding.AR -> root + "ando"
        else -> root + "iendo"
    }
    val pastParticiple = irregularPastParticiple ?: when (infinitiveEnding) {
        InfinitiveEnding.AR -> root + "ado"
        else -> root + "ido"
    }
}

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
        Verb("quejarse", "complain"),
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
        Verb("nevar", "snow", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
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
        Verb("abrir", "open", irregularities = listOf(Irregularity.PAST_PARTICIPLE)),
        Verb("cubrir", "cover", irregularities = listOf(Irregularity.PAST_PARTICIPLE)),
        Verb("escribir", "write", irregularities = listOf(Irregularity.PAST_PARTICIPLE)),

        // spelling change - phonetic
        Verb("dirigir", "manage, direct", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC)),

        // TODO

        // TODO add stem change
        Verb("elegir", "choose", irregularities = listOf(Irregularity.SPELLING_CHANGE_PHONETIC, Irregularity.STEM_CHANGE_E_to_I)),


        // Yo Go verbs
        Verb("oír", "hear", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO)),
        Verb("salir", "go out, leave", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO)),
        Verb("venir", "come", altPreteritRoot = "vin", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.STEM_CHANGE_E_to_IE, Irregularity.NO_ACCENT_ON_PRETERIT)),

        // Yo zc
        Verb("producir", "produce", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("reducir", "reduce", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC, Irregularity.SPELLING_CHANGE_PHONETIC, Irregularity.NO_ACCENT_ON_PRETERIT)),

        // Custom conjugations
        Verb("decir", "say, tell", altPreteritRoot = "dij", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.STEM_CHANGE_E_to_I, Irregularity.NO_ACCENT_ON_PRETERIT)) { subjectPronoun: SubjectPronoun, conjugationType: ConjugationType ->
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
                else -> null
            }
        }
)
val irregularErVerbs = listOf(
        // past participle only
        Verb("romper", "break", irregularities = listOf(Irregularity.PAST_PARTICIPLE)),

        // TODO

        // stem changes
        Verb("querer", "want", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_IE)),
        Verb("conseguir", "get", irregularities = listOf(Irregularity.STEM_CHANGE_E_to_I)),
        Verb("dormir", "sleep", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),


        // no accent on preterit
        Verb("caber", "fit", altPreteritRoot = "cup", irregularities = listOf(Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("haber", "have, exist", altPreteritRoot = "hub", irregularities = listOf(Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("poder", "can, be able to", irregularities = listOf(Irregularity.NO_ACCENT_ON_PRETERIT, Irregularity.STEM_CHANGE_O_to_UE)),
        Verb("saber", "know", altPreteritRoot = "sup", irregularities = listOf(Irregularity.NO_ACCENT_ON_PRETERIT)),


        // Yo Go verbs
        Verb("caer", "fall", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO)),
        Verb("hacer", "make, do", altPreteritRoot = "hic", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("poner", "put", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("tener", "have", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO, Irregularity.STEM_CHANGE_E_to_IE, Irregularity.NO_ACCENT_ON_PRETERIT)),
        Verb("traer", "bring", altPreteritRoot = "traj", irregularities = listOf(Irregularity.NO_ACCENT_ON_PRETERIT, Irregularity.SPELLING_CHANGE_YO_GO)),
        Verb("valer", "be worth, cost", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_GO)),

        // Yo zc
        Verb("conocer", "know, meet", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("crecer", "grow", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC)),
        Verb("nacer", "be born", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC))
)


/*

https://en.wikipedia.org/wiki/Spanish_irregular_verbs

Stem Changes
------------

In word-initial position, *ie- is written ye- (errar > yerro) and *ue- is written hue- (oler > huele). Also, the -ue- diphthong is written -üe- after g, with the diaeresis to indicate that the letter is not silent (avergonzarse > me avergüenzo).

Verbs ending in -uir and -oír
All verbs ending in -uir (e.g. construir, disminuir, distribuir) add a medial -y- before all endings not starting with i: construyo, construyes, construya... Taking into account that these verbs also undergo the change of unstressed intervocalic i to y (see orthographic changes above), they have many forms containing y.

This also applies to the forms of oír and desoír that do not undergo the -ig- change: oyes, oye, oyen

Again, note that some regular forms of fluir, fruir and huir are written without stress mark if considered monosyllabic, but may bear it if pronounced as bisyllabic: vosotros huis or huís (present), yo hui or huí (preterite).

G-verbs
Before o (in the first person singular of the indicative present tense) and a (that is, in all persons of the present subjunctive), the so-called G-verbs (sometimes "go-verbs" or "yo-go" verbs) add a medial -g- after l and n (also after s in asir), add -ig- when the root ends in a vowel, or substitute -g- for -c-. Note that this change overrides diphthongization (tener, venir) but combines with vowel-raising (decir). Many of these verbs are also irregular in other ways. For example:

salir: yo salgo, tú sales...
valer: yo valgo, tú vales...
poner: yo pongo, tú pones...
tener: yo tengo, tú tienes...
venir: yo vengo , tú vienes...
caer: yo caigo, tú caes...
traer: yo traigo, tú traes...
oír: yo oigo, tú oyes...
hacer: yo hago, tú haces...
decir: yo digo, tú dices...
asir: yo asgo, tú ases...
ZC-verbs
This group of verbs—which originated in the Latin inchoative verbs but now includes other verbs as well— substitute -zc- for stem-final -c- before o and a. The group includes nearly all verbs ending in -acer (except hacer and derived verbs), -ecer (except mecer and remecer), -ocer (except cocer and derived verbs), and -ucir. For example:

nacer: yo nazco, tú naces...
crecer: yo crezco, tú creces...
conocer: yo conozco, tú conoces...
producir: yo produzco, tú produces...
Yacer may alternatively be conjugated with -zc- (yazco), -g- (yago) or a compromise -zg- (yazgo).

Irregular forms in the future, conditional and imperative
Some -er and -ir verbs (most G-verbs plus haber, saber, poder and querer) also change their stem in the future and conditional tenses. This involves:

Just dropping the infinitive e: haber → habr-..., saber → sabr-..., poder → podr-..., querer → querr-...
Dropping the infinitive e/i and padding the resulting *-lr-/*-nr- with a -d-: tener → tendr-..., poner → pondr-..., venir → vendr-..., valer → valdr-..., salir → saldr-...
Dropping the infinitive -ce- or -ec-: hacer → har-..., deshacer → deshar-..., decir → dir-... Predecir, contradecir and desdecir may share this irregularity (predir-...) or, more commonly, use the regular forms (predecir-). For bendecir and maldecir only the regular forms are used (bendecir-...).
Many of these verbs also have shortened tú imperative forms: tener → ten, contener → contén, poner → pon, disponer → dispón, venir → ven, salir → sal, hacer → haz, decir → di. However, all verbs derived from decir are regular in this form: bendice, maldice, desdícete, predice, contradice.

Anomalous stems in the preterite and derived tenses:

estar → estuv-: yo estuve, tú/vos estuviste(s), él estuvo..., ellos estuvieron; yo estuviera...
andar → anduv-: yo anduve, tú/vos anduviste(s), él anduvo..., ellos anduvieron; yo anduviera...
tener → tuv-: yo tuve, tú/vos tuviste(s), él tuvo..., ellos tuvieron; yo tuviera...
haber → hub-: yo hube, tú/vos hubiste(s), él hubo..., ellos hubieron; yo hubiera...
caber → cup-: yo cupe, tú/vos cupiste(s), él cupo..., ellos cupieron; yo cupiera...
saber → sup-: yo supe, tú/vos supiste(s), él supo..., ellos supieron; yo supiera...
venir → vin-: yo vine, tú/vos viniste(s), él vino..., ellos vinieron; yo viniera...
poder → pud-: yo pude, tú/vos pudiste(s), él pudo..., ellos pudieron; yo pudiera...
poner → pus-: yo puse, tú/vos pusiste(s), él puso..., ellos pusieron; yo pusiera...
hacer → hic-/hiz-: yo hice, tú/vos hiciste(s), él hizo..., ellos hicieron; yo hiciera...
reducir → reduj-: yo reduje, tu/vos redujiste(s), él redujo.., ellos condujeron; yo condujera...
decir → dij-: yo dije, tú/vos dijiste(s), él dijo..., ellos dijeron; yo dijera...

Irregular past participles
A number of verbs have irregular past participles, sometimes called "strong" because the change is in the root, rather than an ending. This includes verbs which are irregular in many other ways, as poner and decir, but for some other verbs this is their only irregularity (e.g. abrir, romper), while some very irregular verbs (as ser and ir) have regular past participles. Examples:

abrir → abierto, morir → muerto, volver → vuelto, devolver → devuelto...
romper → roto, escribir → escrito...
ver → visto, prever → previsto, poner → puesto, componer → compuesto...
hacer → hecho, rehacer → rehecho, decir → dicho, predecir → predicho (but bendecir → bendecido, maldecir → maldecido)...
pudrir → podrido.
There are three verbs that have both a regular and an irregular past participle. Both forms may be used when conjugating the compound tenses and the passive voice with the auxiliary verbs haber and ser, but the irregular form is generally the only one used as an adjective:

freír → he freído or he frito, but papas fritas.
imprimir → he imprimido or he impreso, but papeles impresos.
proveer → he proveído or he provisto, una despensa bien provista far more usual than una despensa bien proveída.

*/