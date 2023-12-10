package com.aws.iam.tests;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.policybuilder.iam.IamPolicyReader;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.*;

import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(SoftAssertionsExtension.class)
public class TaskIAMTest {

    @Test
    public void testUserList(SoftAssertions softAssertions) {
        Region region = Region.AP_SOUTH_1;
        AwsBasicCredentials awsCreds = AwsBasicCredentials
                .create("id", "key");
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(awsCreds);

        IamClient iam = IamClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();

        ListPoliciesResponse listPolicies = iam.listPolicies();
        ListUsersResponse listUsers = iam.listUsers();
        ListRolesResponse listRoles = iam.listRoles();
        ListGroupsResponse groupRoles = iam.listGroups();
        List<Role> rolList = listRoles.roles();

        rolList.forEach(role -> {
            System.out.println("Role Name: " + role.roleName());
        });
        //List Attached Policies to this Role
        ListAttachedRolePoliciesRequest request = ListAttachedRolePoliciesRequest.builder()
                .roleName("FullAccessRoleEC2")
                .build();
        ListAttachedRolePoliciesResponse  response = iam.listAttachedRolePolicies(request);
        List<AttachedPolicy> attachedPolicies = response.attachedPolicies();
        System.out.println(attachedPolicies);

        List<User> userList = listUsers.users();
        userList.forEach(user -> {
            System.out.println("User Name: " + user.userName());
        });
        //List Attached Policies to this User
        ListAttachedUserPoliciesRequest requestUser = ListAttachedUserPoliciesRequest.builder()
                .userName("FullAccessUserEC2")
                .build();
        ListAttachedRolePoliciesResponse  responseUser = iam.listAttachedRolePolicies(request);
        List<AttachedPolicy> attachedPoliciesUser = response.attachedPolicies();
        System.out.println(attachedPolicies);


        List<Policy> policyList = listPolicies.policies();
        policyList.forEach(policy -> {
            System.out.println("Policy Name: " + policy.policyName());
        });

        List<Group> groupList = groupRoles.groups();
        groupList.forEach(group -> {
            System.out.println("Group Name: " + group.groupName());
        });
    }
}
