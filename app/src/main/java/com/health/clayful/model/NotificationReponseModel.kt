package com.health.clayful.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class NotificationReponseModel {
    @SerializedName("conversationId")
    @Expose
    var conversationId: String? = null

    @SerializedName("origin")
    @Expose
    var origin: String? = null

    @SerializedName("smoochNotification")
    @Expose
    var smoochNotification: Boolean? = null

    @SerializedName("message")
    @Expose
    var message: Message? = null

    class Message {
        @SerializedName("role")
        @Expose
        var role: String? = null

        @SerializedName("source")
        @Expose
        var source: Source? = null

        @SerializedName("authorId")
        @Expose
        var authorId: String? = null

        @SerializedName("name")
        @Expose
        var name: String? = null

        @SerializedName("avatarUrl")
        @Expose
        var avatarUrl: String? = null

        @SerializedName("_id")
        @Expose
        var id: String? = null

        @SerializedName("type")
        @Expose
        var type: String? = null

        @SerializedName("received")
        @Expose
        var received: Float? = null

        @SerializedName("metadata")
        @Expose
        var metadata: Metadata? = null

        @SerializedName("text")
        @Expose
        var text: String? = null

        @SerializedName("mediaUrl")
        @Expose
        var mediaUrl: String? = null

        @SerializedName("altText")
        @Expose
        var altText: String? = null

        @SerializedName("mediaType")
        @Expose
        var mediaType: String? = null

        @SerializedName("mediaSize")
        @Expose
        var mediaSize: Int? = null
    }

    class Metadata {
        @SerializedName("__zendesk_msg.source_type")
        @Expose
        var zendeskMsgSourceType: String? = null

        @SerializedName("__zendesk_msg.agent.id")
        @Expose
        var zendeskMsgAgentId: String? = null

        @SerializedName("__zendesk_msg.id")
        @Expose
        var zendeskMsgId: String? = null
    }

    class Source {
        @SerializedName("type")
        @Expose
        var type: String? = null
    }
}