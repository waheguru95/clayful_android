package com.health.clayful.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class UserModel {
    @SerializedName("data")
    @Expose
    var data: Data? = null

    class Data {
        @SerializedName("member")
        @Expose
        var member: Member? = null

        @SerializedName("redirect")
        @Expose
        var redirect: String? = null

        @SerializedName("tokens")
        @Expose
        var tokens: Tokens? = null

        @SerializedName("contentGroups")
        @Expose
        var contentGroups: List<ContentGroup>? = null

        @SerializedName("payment")
        @Expose
        var payment: Payment? = null
    }

    class Member {
        @SerializedName("id")
        @Expose
        var id: String? = null

        @SerializedName("verified")
        @Expose
        var verified: Boolean? = null

        @SerializedName("createdAt")
        @Expose
        var createdAt: String? = null

        @SerializedName("auth")
        @Expose
        var auth: Auth? = null

        @SerializedName("metaData")
        @Expose
        var metaData: MetaData? = null

        @SerializedName("customFields")
        @Expose
        var customFields: CustomFields? = null

        @SerializedName("permissions")
        @Expose
        var permissions: List<Any>? = null

        @SerializedName("stripeCustomerId")
        @Expose
        var stripeCustomerId: Any? = null

        @SerializedName("loginRedirect")
        @Expose
        var loginRedirect: String? = null

        @SerializedName("planConnections")
        @Expose
        var planConnections: List<PlanConnection>? = null
    }

    class Tokens {
        @SerializedName("accessToken")
        @Expose
        var accessToken: String? = null

        @SerializedName("expires")
        @Expose
        var expires: Long? = null

        @SerializedName("type")
        @Expose
        var type: String? = null
    }

    class ContentGroup {
        @SerializedName("id")
        @Expose
        var id: String? = null

        @SerializedName("name")
        @Expose
        var name: String? = null

        @SerializedName("key")
        @Expose
        var key: String? = null

        @SerializedName("allowAllMembers")
        @Expose
        var allowAllMembers: Boolean? = null

        @SerializedName("activeMemberHasAccess")
        @Expose
        var activeMemberHasAccess: Boolean? = null

        @SerializedName("redirect")
        @Expose
        var redirect: String? = null

        @SerializedName("urls")
        @Expose
        var urls: List<Url>? = null

        @SerializedName("plans")
        @Expose
        var plans: List<Plan>? = null
    }

    class Auth {
        @SerializedName("email")
        @Expose
        var email: String? = null
    }

    public class MetaData {

    }

    class CustomFields {
        @SerializedName("role")
        @Expose
        var role: String? = null

        @SerializedName("userId")
        @Expose
        var userId: String? = null

        @SerializedName("accountId")
        @Expose
        var accountId: String? = null

        @SerializedName("accountName")
        @Expose
        var accountName: String? = null

        @SerializedName("parentAccountName")
        @Expose
        var parentAccountName: String? = null
    }

    class Plan {
        @SerializedName("id")
        @Expose
        var id: String? = null
    }

    class PlanConnection {
        @SerializedName("id")
        @Expose
        var id: String? = null

        @SerializedName("active")
        @Expose
        var active: Boolean? = null

        @SerializedName("status")
        @Expose
        var status: String? = null

        @SerializedName("planId")
        @Expose
        var planId: String? = null

        @SerializedName("type")
        @Expose
        var type: String? = null

        @SerializedName("payment")
        @Expose
        var payment: Payment? = null
    }

    class Url {
        @SerializedName("url")
        @Expose
        var url: String? = null

        @SerializedName("filter")
        @Expose
        var filter: String? = null
    }

    class Payment {
        @SerializedName("requirePayment")
        @Expose
        var requirePayment: List<Any>? = null

        @SerializedName("requireAuthentication")
        @Expose
        var requireAuthentication: List<Any>? = null
    }
}