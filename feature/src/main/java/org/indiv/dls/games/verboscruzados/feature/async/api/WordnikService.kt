package org.indiv.dls.games.verboscruzados.feature.async.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WordnikService {

    @GET("v4/word.json/{word}/definitions?limit=20&includeRelated=false&sourceDictionaries=all&useCanonical=false&includeTags=false&api_key=f4e5b019cbc525972530c0bf0a0088162bff83d8464c1883a")
    fun getDefinitions(@Path("word") word: String): Call<List<WordnikDefinition>>
}