package com.uploadcare.android.library.api

import android.content.Context
import com.uploadcare.android.library.BuildConfig
import com.uploadcare.android.library.callbacks.*
import com.uploadcare.android.library.data.*
import com.uploadcare.android.library.urls.Urls
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * Initializes a client with custom access keys.
 * Can use simple or secure authentication.
 *
 * @param publicKey  Public key
 * @param privateKey Private key
 * @param simpleAuth If {@code false}, HMAC-based authentication is used, otherwise simple
 * authentication is used.
 */
class UploadcareClient constructor(val publicKey: String,
                                   val privateKey: String,
                                   val simpleAuth: Boolean = false) {

    constructor(publicKey: String, privateKey: String) : this(publicKey, privateKey, false)

    val httpClient: OkHttpClient
    val requestHelper: RequestHelper
    val objectMapper = ObjectMapper.build()

    init {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        httpClient = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(PublicKeyInterceptor(publicKey))
                .callTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

        requestHelper = DefaultRequestHelperProvider().get(this)
    }

    /**
     * Requests project info from the API.
     *
     * @return Project resource
     */
    fun getProject(): Project {
        val url = Urls.apiProject()
        return requestHelper.executeQuery(RequestHelper.REQUEST_GET, url.toString(), true,
                Project::class.java)
    }

    /**
     * Requests project info from the API Asynchronously.
     *
     * @param context  Application context. [android.content.Context]
     * @param callback callback  [ProjectCallback] with either
     * an Project response or a failure exception.
     */
    fun getProjectAsync(context: Context, callback: ProjectCallback? = null) {
        val url = Urls.apiProject()
        requestHelper.executeQueryAsync(context, RequestHelper.REQUEST_GET, url.toString(), true,
                Project::class.java, callback)
    }

    /**
     * Requests file data.
     *
     * @param fileId Resource UUID
     * @return UploadcareFile resource
     */
    fun getFile(fileId: String): UploadcareFile {
        val url = Urls.apiFile(fileId)
        return requestHelper.executeQuery(RequestHelper.REQUEST_GET, url.toString(),
                true, UploadcareFile::class.java)
    }

    /**
     * Requests file data Asynchronously.
     *
     * @param context  Application context. [android.content.Context]
     * @param fileId   Resource UUID
     * @param callback callback  [UploadcareFileCallback] with either
     * an UploadcareFile response or a failure exception.
     */
    fun getFileAsync(context: Context, fileId: String, callback: UploadcareFileCallback? = null) {
        val url = Urls.apiFile(fileId)
        requestHelper.executeQueryAsync(context, RequestHelper.REQUEST_GET, url.toString(), true,
                UploadcareFile::class.java, callback)
    }

    /**
     * Requests group data.
     *
     * @param groupId Group ID
     * @return UploadcareGroup resource
     */
    fun getGroup(groupId: String): UploadcareGroup {
        val url = Urls.apiGroup(groupId)
        return requestHelper.executeQuery(RequestHelper.REQUEST_GET, url.toString(),
                true, UploadcareGroup::class.java)
    }

    /**
     * Requests file data Asynchronously.
     *
     * @param context  Application context. [android.content.Context]
     * @param groupId  Group ID
     * @param callback callback  [UploadcareGroupCallback] with either
     * an UploadcareFile response or a failure exception.
     */
    fun getGroupAsync(context: Context, groupId: String, callback: UploadcareGroupCallback? = null) {
        val url = Urls.apiGroup(groupId)
        requestHelper.executeQueryAsync(context, RequestHelper.REQUEST_GET, url.toString(), true,
                UploadcareGroup::class.java, callback)
    }

    /**
     * Mark all files in a group as stored (available on CDN).
     *
     * @param groupId Group ID
     */
    fun storeGroup(groupId: String) {
        val requestBody = RequestBody.create(MediaType.parse(""), groupId)
        val url = Urls.apiGroupStorage(groupId)
        requestHelper.executeCommand(RequestHelper.REQUEST_PUT, url.toString(), true, requestBody)
    }

    /**
     * Mark all files in a group as stored (available on CDN). Asynchronously.
     *
     * @param context  Application context. [android.content.Context]
     * @param groupId  Group ID
     * @param callback callback  [RequestCallback] with either
     * an HTTP response or a failure exception.
     */
    fun storeGroupAsync(context: Context, groupId: String, callback: RequestCallback? = null) {
        val requestBody = RequestBody.create(MediaType.parse(""), groupId)
        val url = Urls.apiGroupStorage(groupId)
        requestHelper.executeCommandAsync(context, RequestHelper.REQUEST_PUT, url.toString(),
                true, callback, requestBody)
    }

    /**
     * Begins to build a request for uploaded files for the current account.
     *
     * @return UploadcareFile resource request builder
     */
    fun getFiles(): FilesQueryBuilder {
        return FilesQueryBuilder(this)
    }

    /**
     * Begins to build a request for groups for the current account.
     *
     * @return Group resource request builder
     */
    fun getGroups(): GroupsQueryBuilder {
        return GroupsQueryBuilder(this)
    }

    /**
     * Marks a file as deleted.
     *
     * @param fileId Resource UUID
     */
    fun deleteFile(fileId: String) {
        val url = Urls.apiFile(fileId)
        requestHelper.executeCommand(RequestHelper.REQUEST_DELETE, url.toString(), true)
    }

    /**
     * Marks a file as deleted Asynchronously.
     *
     * @param context Application context. [android.content.Context]
     * @param fileId  Resource UUID
     */
    fun deleteFileAsync(context: Context, fileId: String) {
        val url = Urls.apiFile(fileId)
        requestHelper.executeCommandAsync(context, RequestHelper.REQUEST_DELETE, url.toString(),
                true)
    }

    /**
     * Marks a file as deleted Asynchronously.
     *
     * @param context  Application context. [android.content.Context]
     * @param fileId   Resource UUID
     * @param callback callback  [RequestCallback] with either
     * an HTTP response or a failure exception.
     */
    fun deleteFileAsync(context: Context, fileId: String, callback: RequestCallback? = null) {
        val url = Urls.apiFile(fileId)
        requestHelper
                .executeCommandAsync(context, RequestHelper.REQUEST_DELETE, url.toString(), true,
                        callback)
    }

    /**
     * Marks a file as saved.
     *
     * This has to be done for all files you want to keep.
     * Unsaved files are eventually purged.
     *
     * @param fileId Resource UUID
     */
    fun saveFile(fileId: String) {
        val url = Urls.apiFileStorage(fileId)
        requestHelper.executeCommand(RequestHelper.REQUEST_POST, url.toString(), true)
    }

    /**
     * Marks a file as saved Asynchronously.
     *
     * This has to be done for all files you want to keep.
     * Unsaved files are eventually purged.
     *
     * @param context Application context. [android.content.Context]
     * @param fileId  Resource UUID
     */
    fun saveFileAsync(context: Context, fileId: String) {
        val url = Urls.apiFileStorage(fileId)
        requestHelper.executeCommandAsync(context, RequestHelper.REQUEST_POST, url.toString(), true)
    }

    /**
     * Marks a file as saved Asynchronously.
     *
     * This has to be done for all files you want to keep.
     * Unsaved files are eventually purged.
     *
     * @param context  Application context. @link android.content.Context
     * @param fileId   Resource UUID
     * @param callback callback  [RequestCallback] with either
     * an HTTP response or a failure exception.
     */
    fun saveFileAsync(context: Context, fileId: String, callback: RequestCallback? = null) {
        val url = Urls.apiFileStorage(fileId)
        requestHelper.executeCommandAsync(context, RequestHelper.REQUEST_POST, url.toString(), true,
                callback)
    }

    /**
     * @param fileId  Resource UUID
     * @param storage Target storage name
     * @return An object containing the results of the copy request
     */
    fun copyFile(fileId: String, storage: String?): CopyFileData {
        val url = Urls.apiFiles()

        val formBuilder = FormBody.Builder().add("source", fileId)

        if (storage != null && !storage.isEmpty()) {
            formBuilder.add("target", storage)
        }
        val formBody = formBuilder.build()
        return requestHelper.executeQuery(RequestHelper.REQUEST_POST, url.toString(), true,
                CopyFileData::class.java, formBody)
    }

    /**
     * @param context  Application context. @link android.content.Context
     * @param fileId   Resource UUID
     * @param storage  Target storage name
     * @param callback callback  [CopyFileCallback] with either
     * an CopyFileData response or a failure exception.
     */
    fun copyFileAsync(context: Context, fileId: String, storage: String?,
                      callback: CopyFileCallback? = null) {
        val url = Urls.apiFiles()

        val formBuilder = FormBody.Builder().add("source", fileId)
        if (storage != null && !storage.isEmpty()) {
            formBuilder.add("target", storage)
        }
        val formBody = formBuilder.build()

        requestHelper.executeQueryAsync(context, RequestHelper.REQUEST_POST, url.toString(), true,
                CopyFileData::class.java, callback, formBody)
    }

    companion object {

        @JvmStatic
        fun demoClient(): UploadcareClient {
            return UploadcareClient("demopublickey", "demoprivatekey")
        }
    }

}

private class PublicKeyInterceptor constructor(private val publicKey: String)
    : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        requestBuilder.addHeader("X-Uploadcare-PublicKey", publicKey)

        return chain.proceed(requestBuilder.build())
    }
}