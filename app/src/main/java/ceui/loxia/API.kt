package ceui.loxia

import ceui.lisa.models.NullResponse
import ceui.lisa.utils.Params
import okhttp3.ResponseBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface API {

    @FormUrlEncoded
    @POST("/v1/illust/report")
    suspend fun postFlagIllust(
        @Field("illust_id") illust_id: Int,
        @Field("type_of_problem") type_of_problem: String?,
        @Field("message") message: String?
    ): NullResponse

    @FormUrlEncoded
    @POST("/v1/user/follow/add")
    suspend fun postFollow(
        @Field("user_id") user_id: Long,
        @Field("restrict") followType: String
    )

    @FormUrlEncoded
    @POST("/v1/user/follow/delete")
    suspend fun postUnFollow(
        @Field("user_id") user_id: Long
    )

    @FormUrlEncoded
    @POST("/v2/illust/bookmark/add")
    suspend fun postBookmark(
        @Field("illust_id") illust_id: Long,
        @Field("restrict") restrict: String = Params.TYPE_PUBLIC
    )

    @FormUrlEncoded
    @POST("/v1/illust/bookmark/delete")
    suspend fun removeBookmark(
        @Field("illust_id") illust_id: Long
    )


    @GET("/v1/{type}/recommended?include_ranking_illusts=true&include_privacy_policy=true&filter=for_ios")
    suspend fun getHomeData(
        @Path("type") type: String,
    ): HomeIllustResponse

    @GET("/v1/user/illusts?filter=for_ios")
    suspend fun getUserCreatedIllusts(
        @Query("user_id") user_id: Long,
        @Query("type") type: String,
    ): IllustResponse

    @GET("/v1/user/bookmarks/illust?filter=for_ios&restrict=public")
    suspend fun getUserBookmarkedIllusts(
        @Query("user_id") user_id: Long,
    ): IllustResponse


    @GET("/v2/user/detail?filter=for_ios")
    suspend fun getUserProfile(
        @Query("user_id") user_id: Long,
    ): UserResponse

    @GET("/v1/user/following")
    suspend fun getFollowingUsers(
        @Query("user_id") user_id: Long,
        @Query("restrict") restrict: String,
    ): UserPreviewResponse

    @GET("/v1/user/recommended?filter=for_ios")
    suspend fun recommendedUsers(): UserPreviewResponse

    @GET("/v1/illust/ranking?filter=for_ios")
    suspend fun getRankingIllusts(
        @Query("mode") mode: String,
    ): IllustResponse

    @GET("/v1/search/popular-preview/illust?search_ai_type=0&filter=for_ios")
    suspend fun popularPreview(
        @Query("word") word: String,
        @Query("sort") sort: String,
        @Query("search_target") search_target: String,
        @Query("merge_plain_keyword_results") merge_plain_keyword_results: Boolean,
        @Query("include_translated_tag_results") include_translated_tag_results: Boolean,
    ): IllustResponse

    @GET("/v1/search/illust?search_ai_type=0&filter=for_ios")
    suspend fun searchIllustManga(
        @Query("word") word: String,
        @Query("sort") sort: String,
        @Query("search_target") search_target: String,
        @Query("merge_plain_keyword_results") merge_plain_keyword_results: Boolean,
        @Query("include_translated_tag_results") include_translated_tag_results: Boolean,
    ): IllustResponse


    @GET("/v1/search/user?filter=for_ios")
    suspend fun searchUser(
        @Query("word") word: String,
    ): UserPreviewResponse


    // :path	/v1/trending-tags/?filter=for_ios
    @GET("/v1/trending-tags/{type}?filter=for_ios")
    suspend fun trendingTags(
        @Path("type") type: String,
    ): TrendingTagsResponse


    @GET("/v3/illust/comments")
    suspend fun getIllustComments(
        @Query("illust_id") illust_id: Long,
    ): CommentResponse

    @GET("/v3/novel/comments")
    suspend fun getNovelComments(
        @Query("novel_id") novel_id: Long,
    ): CommentResponse

    @GET("/v2/{type}/comment/replies")
    suspend fun getIllustReplyComments(
        @Path("type") type: String,
        @Query("comment_id") comment_id: Long,
    ): CommentResponse

    @FormUrlEncoded
    @POST("/v1/illust/comment/add")
    suspend fun postIllustComment(
        @Field("illust_id") illust_id: Long,
        @Field("comment") comment: String,
        @Field("parent_comment_id") parent_comment_id: Long? = null,
    ): PostCommentResponse

    @FormUrlEncoded
    @POST("/v1/novel/comment/add")
    suspend fun postNovelComment(
        @Field("novel_id") novel_id: Long,
        @Field("comment") comment: String,
        @Field("parent_comment_id") parent_comment_id: Long? = null,
    ): PostCommentResponse

    @FormUrlEncoded
    @POST("/v1/{type}/comment/delete")
    suspend fun deleteComment(
        @Path("type") type: String,
        @Field("comment_id") comment_id: Long,
    )

    @GET
    suspend fun generalGet(@Url url: String): ResponseBody
}