package com.aikonia.app.data.source.remote

import com.google.gson.JsonObject
import com.aikonia.app.common.Constants
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Streaming
import retrofit2.Response
interface ConversAIService {
    @POST(Constants.Endpoints.TEXT_COMPLETIONS)
    @Streaming
    suspend fun textCompletionsWithStream(@Body body: JsonObject): Response<ResponseBody>

    @POST(Constants.Endpoints.TEXT_COMPLETIONS_TURBO)
    @Streaming
    suspend fun textCompletionsTurboWithStream(@Body body: JsonObject): Response<ResponseBody>

    @POST(Constants.Endpoints.TEXT_COMPLETIONS_TURBO)
    @Streaming
    suspend fun sendGreeting(@Body body: JsonObject): Response<ResponseBody>
}