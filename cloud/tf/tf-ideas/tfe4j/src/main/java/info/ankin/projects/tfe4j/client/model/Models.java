package info.ankin.projects.tfe4j.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface Models {
    /**
     * @see <a href="https://developer.hashicorp.com/terraform/enterprise/api-docs#query-parameters">Query Parameters</a>
     */
    @Data
    @Accessors(chain = true)
    class PagingParameters {
        /**
         * @see <a href="https://developer.hashicorp.com/terraform/enterprise/api-docs#pagination">Pagination</a>
         */
        @JsonProperty("page[number]")
        Integer page;

        /**
         * @see <a href="https://developer.hashicorp.com/terraform/enterprise/api-docs#pagination">Pagination</a>
         */
        @JsonProperty("page[size]")
        Integer size;

        /**
         * @see <a href="https://developer.hashicorp.com/terraform/enterprise/api-docs#inclusion-of-related-resources">Inclusion of Related Resources</a>
         */
        List<String> include;
    }

    //<editor-fold desc="user">
    class SingleUser extends Wrappers.Single<User> {
    }

    class UserItem extends Wrappers.Item<User> {
        public UserItem() {
            setType("users");
        }
    }

    @Data
    @Accessors(chain = true)
    class User {
        @JsonProperty("avatar-url")
        String avatarURL;
        String email;
        @JsonProperty("is-service-account")
        Boolean isServiceAccount;
        @JsonProperty("two-factor")
        TwoFactor twoFactor;
        @JsonProperty("unconfirmed-email")
        String unconfirmedEmail;
        String username;
        /**
         * not documented but present in cloud api
         */
        String password;
        @JsonProperty("v2-only")
        Boolean v2Only;
        @JsonProperty("is-site-admin")
        Boolean isSiteAdmin;
        @JsonProperty("is-sso-login")
        Boolean isSsoLogin;
        UserPermissions permissions;

        @JsonProperty("enterprise-support")
        Boolean enterpriseSupport;
        @JsonProperty("has-git-hub-app-token")
        Boolean hasGitHubAppToken;
        @JsonProperty("is-confirmed")
        Boolean confirmed;
        @JsonProperty("is-sudo")
        Boolean sudo;
        @JsonProperty("has-linked-hcp")
        Boolean hasLinkedHcp;

        @Data
        @Accessors(chain = true)
        public static class TwoFactor {
            Boolean enabled;
            Boolean verified;
        }

        @Data
        @Accessors(chain = true)
        public static class UserPermissions {
            @JsonProperty("can-create-organizations")
            Boolean createOrganizations;
            @JsonProperty("can-change-email")
            Boolean changeEmail;
            @JsonProperty("can-change-username")
            Boolean changeUsername;
            @JsonProperty("can-manage-user-tokens")
            Boolean manageUserTokens;
            @JsonProperty("can-view2fa-settings")
            Boolean view2FaSettings;
            @JsonProperty("can-manage-hcp-account")
            Boolean manageHcpAccount;
            // undocumented
            @JsonProperty("can-view-settings")
            Boolean viewSettings;
        }
    }
    //</editor-fold>

    //<editor-fold desc="user update">
    class SingleUserUpdate extends Wrappers.Single<UserUpdate> {
    }

    class UserUpdateItem extends Wrappers.Item<UserUpdate> {
        public UserUpdateItem() {
            setType("users");
        }

        @Override
        public SingleUserUpdate toSingle() {
            return (SingleUserUpdate) new SingleUserUpdate().setData(this);
        }
    }

    @Data
    @Accessors(chain = true)
    class UserUpdate {
        /**
         * email can't be blank
         */
        String email;

        /**
         * username can't be blank
         */
        String username;

        public UserUpdateItem toItem() {
            return (UserUpdateItem) new UserUpdateItem().setAttributes(this);
        }
    }
    //</editor-fold>

    //<editor-fold desc="user password update">
    class SingleUserPasswordUpdate extends Wrappers.Single<UserPasswordUpdate> {
    }

    class UserPasswordUpdateItem extends Wrappers.Item<UserPasswordUpdate> {
        public UserPasswordUpdateItem() {
            setType("users");
        }

        @Override
        public SingleUserPasswordUpdate toSingle() {
            return (SingleUserPasswordUpdate) new SingleUserPasswordUpdate().setData(this);
        }
    }

    @Data
    @Accessors(chain = true)
    class UserPasswordUpdate {
        @JsonProperty("current_password")
        String currentPassword;
        String password;
        @JsonProperty("password_confirmation")
        String passwordConfirmation;

        public UserPasswordUpdateItem toItem() {
            return (UserPasswordUpdateItem) new UserPasswordUpdateItem().setAttributes(this);
        }
    }
    //</editor-fold>

    //<editor-fold desc="ent/admin/org">
    // see documentation about parameter models
    @ToString(callSuper = true)
    @EqualsAndHashCode(callSuper = true)
    @Data
    @Accessors(chain = true)
    class ListOrganizationsParameters extends PagingParameters {
        @JsonProperty("q")
        String query;
        @JsonProperty("q[email]")
        String email;
        @JsonProperty("q[name]")
        String name;
        @JsonProperty("filter[module_producer]")
        Boolean moduleProducer;
        @JsonProperty("filter[provider_producer]")
        Boolean providerProducer;
    }

    class MultipleOrganizations extends Wrappers.Multiple<Organization> {
    }

    class SingleOrganization extends Wrappers.Single<Organization> {
    }

    class SingleOrganizationItem extends Wrappers.Item<Organization> {
        public SingleOrganizationItem() {
            setType("organizations");
        }

        @Override
        public SingleOrganization toSingle() {
            return (SingleOrganization) new SingleOrganization().setData(this);
        }
    }

    @Data
    @Accessors(chain = true)
    class Organization {
        String name;
        @JsonProperty("access-beta-tools")
        Boolean accessBetaTools;
        // not patchable
        @JsonProperty("external-id")
        String externalID;
        @JsonProperty("global-module-sharing")
        Boolean globalModuleSharing;
        @JsonProperty("is-disabled")
        Boolean isDisabled;
        // not patchable
        @JsonProperty("notification-email")
        String notificationEmail;
        // not patchable
        @JsonProperty("sso-enabled")
        Boolean ssoEnabled;
        @JsonProperty("terraform-build-worker-apply-timeout")
        String terraformBuildWorkerApplyTimeout;
        @JsonProperty("terraform-build-worker-plan-timeout")
        String terraformBuildWorkerPlanTimeout;
        // not patchable
        @JsonProperty("terraform-worker-sudo-enabled")
        Boolean terraformWorkerSudoEnabled;
        @JsonProperty("workspace-limit")
        Integer workspaceLimit;
        // not implemented in go-tfe?
        @JsonProperty("global-provider-sharing")
        Boolean globalProviderSharing;

        public SingleOrganizationItem toItem() {
            return (SingleOrganizationItem) new SingleOrganizationItem().setAttributes(this);
        }
    }
    //</editor-fold>

    //<editor-fold desc="ent/admin/org">
    @ToString(callSuper = true)
    @EqualsAndHashCode(callSuper = true)
    @Data
    @Accessors(chain = true)
    class ListRunsParameters extends PagingParameters {
        @JsonProperty("q")
        String query;
        @JsonProperty("filter[status]")
        List<Status> statusList;

        public enum Status {
            @JsonProperty("pending") PENDING,
            @JsonProperty("plan_queued") PLAN_QUEUED,
            @JsonProperty("planning") PLANNING,
            @JsonProperty("planned") PLANNED,
            @JsonProperty("confirmed") CONFIRMED,
            @JsonProperty("apply_queued") APPLY_QUEUED,
            @JsonProperty("applying") APPLYING,
            @JsonProperty("applied") APPLIED,
            @JsonProperty("discarded") DISCARDED,
            @JsonProperty("errored") ERRORED,
            @JsonProperty("canceled") CANCELED,
            @JsonProperty("cost_estimating") COST_ESTIMATING,
            @JsonProperty("cost_estimated") COST_ESTIMATED,
            @JsonProperty("policy_checking") POLICY_CHECKING,
            @JsonProperty("policy_override") POLICY_OVERRIDE,
            @JsonProperty("policy_soft_failed") POLICY_SOFT_FAILED,
            @JsonProperty("policy_checked") POLICY_CHECKED,
            @JsonProperty("planned_and_finished") PLANNED_AND_FINISHED,
        }
    }

    class MultipleRuns extends Wrappers.Multiple<Run> {
    }

    class SingleRun extends Wrappers.Single<Run> {
    }

    class SingleRunItem extends Wrappers.Item<Run> {
        public SingleRunItem() {
            setType("runs");
        }

        @Override
        public SingleRun toSingle() {
            return (SingleRun) new SingleRun().setData(this);
        }
    }

    @Data
    @Accessors(chain = true)
    class Run {
        @JsonProperty("created-at")
        Date createdAt;
        @JsonProperty("has-changes")
        Boolean hasChanges;
        ListRunsParameters.Status status;
        @JsonProperty("status-timestamps")
        Map<Status, Date> statusTimestamps;

        // yes, they really have an -at which is different than normal status
        public enum Status {
            @JsonProperty("pending-at") PENDING,
            @JsonProperty("plan_queued-at") PLAN_QUEUED,
            @JsonProperty("planning-at") PLANNING,
            @JsonProperty("planned-at") PLANNED,
            @JsonProperty("confirmed-at") CONFIRMED,
            @JsonProperty("apply_queued-at") APPLY_QUEUED,
            @JsonProperty("applying-at") APPLYING,
            @JsonProperty("applied-at") APPLIED,
            @JsonProperty("discarded-at") DISCARDED,
            @JsonProperty("errored-at") ERRORED,
            @JsonProperty("canceled-at") CANCELED,
            @JsonProperty("cost_estimating-at") COST_ESTIMATING,
            @JsonProperty("cost_estimated-at") COST_ESTIMATED,
            @JsonProperty("policy_checking-at") POLICY_CHECKING,
            @JsonProperty("policy_override-at") POLICY_OVERRIDE,
            @JsonProperty("policy_soft_failed-at") POLICY_SOFT_FAILED,
            @JsonProperty("policy_checked-at") POLICY_CHECKED,
            @JsonProperty("planned_and_finished-at") PLANNED_AND_FINISHED,
        }
    }
    //</editor-fold>

}
