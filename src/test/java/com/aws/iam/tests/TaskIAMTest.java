package com.aws.iam.tests;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(SoftAssertionsExtension.class)
@Slf4j
class TaskIAMTest {

    private static Region region;
    private static StaticCredentialsProvider credentialsProvider;
    private static IamClient iam;
    private ListPoliciesResponse listPolicies;
    private ListGroupsResponse listGroups;
    private ListRolesResponse listRoles;
    private ListUsersResponse listUsers;

    @BeforeAll
    static void setUp() {
        region = Region.AP_SOUTH_1;
        AwsBasicCredentials awsCreds = AwsBasicCredentials
                .create("", "");
        credentialsProvider = StaticCredentialsProvider.create(awsCreds);
        iam = IamClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
    }


    @Test
    @Order(1)
    @Tag("testUser")
    void testUser(SoftAssertions softAssertions) {
        listUsers = iam.listUsers();
        List<User> userList = listUsers.users();
        userList.forEach(user -> {
            System.out.println("User Name: " + user.userName());
        });

        //List user group membership: FullAccessUserEC2
        ListGroupsForUserRequest requestUserGroup = ListGroupsForUserRequest.builder()
                .userName("FullAccessUserEC2")
                .build();
        ListGroupsForUserResponse responseUserGroup = iam.listGroupsForUser(requestUserGroup);
        String attachedUserGroup = responseUserGroup.groups().get(0).groupName();
        System.out.println("User FullAccessUserEC2 is part of Group: " + attachedUserGroup);
        softAssertions.assertThat(attachedUserGroup).isEqualTo("FullAccessGroupEC2");

        //Verify user group membership: FullAccessPolicyS3
        requestUserGroup = ListGroupsForUserRequest.builder()
                .userName("FullAccessUserS3")
                .build();
        responseUserGroup = iam.listGroupsForUser(requestUserGroup);
        attachedUserGroup = responseUserGroup.groups().get(0).groupName();
        System.out.println("User FullAccessUserS3 is part of Group: " + attachedUserGroup);
        softAssertions.assertThat(attachedUserGroup).isEqualTo("FullAccessGroupS3");

        //Verify user group membership: ReadAccessUserS3
        requestUserGroup = ListGroupsForUserRequest.builder()
                .userName("ReadAccessUserS3")
                .build();
        responseUserGroup = iam.listGroupsForUser(requestUserGroup);
        attachedUserGroup = responseUserGroup.groups().get(0).groupName();
        System.out.println("User ReadAccessUserS3 is part of Group: " + attachedUserGroup);
        softAssertions.assertThat(attachedUserGroup).isEqualTo("ReadAccessGroupS3");
    }

    @Test
    @Order(2)
    @Tag("testPolicy")
    void testPolicy(SoftAssertions softAssertions) {
        listPolicies = iam.listPolicies();
        List<String> policyList = listPolicies.policies().stream().
                filter(p -> !p.arn().contains("aws:policy")).collect(Collectors.toList()).
                stream().map(Policy::policyName).sorted().collect(Collectors.toList());
        List<String> policyListExpected = Arrays.asList("FullAccessPolicyEC2", "FullAccessPolicyS3", "ReadAccessPolicyS3");
        softAssertions.assertThat(policyList).isEqualTo(policyListExpected);

        GetPolicyRequest request = GetPolicyRequest.builder()
                .policyArn("arn:aws:iam::355471061359:policy/FullAccessPolicyEC2")
                .build();

        GetPolicyResponse response = iam.getPolicy(request);
        System.out.format("Successfully retrieved policy %s",
                response.policy().policyName());
    }

    @Test
    @Order(3)
    @Tag("testRole")
    void testRole(SoftAssertions softAssertions) {
        listRoles = iam.listRoles();
        List<Role> rolList = listRoles.roles();
        rolList.forEach(role -> {
            System.out.println("Role Name: " + role.roleName());
        });

        //Verify Policy attached to Role: FullAccessRoleEC2
        ListAttachedRolePoliciesRequest request = ListAttachedRolePoliciesRequest.builder()
                .roleName("FullAccessRoleEC2")
                .build();
        ListAttachedRolePoliciesResponse response = iam.listAttachedRolePolicies(request);
        String attachedPolicy = response.attachedPolicies().get(0).policyName();
        System.out.println("Policy Attached to Role - FullAccessRoleEC2 is: " + attachedPolicy);
        softAssertions.assertThat(attachedPolicy).isEqualTo("FullAccessPolicyEC2");

        //Verify Policy attached to Role: FullAccessRoleS3
        request = ListAttachedRolePoliciesRequest.builder()
                .roleName("FullAccessRoleS3")
                .build();
        response = iam.listAttachedRolePolicies(request);
        attachedPolicy = response.attachedPolicies().get(0).policyName();
        System.out.println("Policy Attached to Role - FullAccessRoleS3 is: " + attachedPolicy);
        softAssertions.assertThat(attachedPolicy).isEqualTo("FullAccessPolicyS3");

        //Verify Policy attached to Role: ReadAccessRoleS3
        request = ListAttachedRolePoliciesRequest.builder()
                .roleName("ReadAccessRoleS3")
                .build();
        response = iam.listAttachedRolePolicies(request);
        attachedPolicy = response.attachedPolicies().get(0).policyName();
        System.out.println("Policy Attached to Role - ReadAccessRoleS3 is: " + attachedPolicy);
        softAssertions.assertThat(attachedPolicy).isEqualTo("ReadAccessPolicyS3");
    }

    @Test
    @Order(4)
    @Tag("testGroup")
    void testGroup(SoftAssertions softAssertions) {
        listGroups = iam.listGroups();
        List<Group> groupList = listGroups.groups();
        groupList.forEach(group -> {
            System.out.println("Group Name: " + group.groupName());
        });

        //Verify Group Policy: FullAccessGroupEC2
        ListAttachedGroupPoliciesRequest request = ListAttachedGroupPoliciesRequest.builder()
                .groupName("FullAccessGroupEC2")
                .build();
        ListAttachedGroupPoliciesResponse response = iam.listAttachedGroupPolicies(request);
        String attachedPolicy = response.attachedPolicies().get(0).policyName();
        System.out.println("Policy Attached to Group - FullAccessGroupEC2 is: " + attachedPolicy);
        softAssertions.assertThat(attachedPolicy).isEqualTo("FullAccessPolicyEC2");

        //Verify Group Policy: FullAccessGroupS3
        request = ListAttachedGroupPoliciesRequest.builder()
                .groupName("FullAccessGroupS3")
                .build();
        response = iam.listAttachedGroupPolicies(request);
        attachedPolicy = response.attachedPolicies().get(0).policyName();
        System.out.println("Policy Attached to Group - FullAccessGroupS3 is: " + attachedPolicy);
        softAssertions.assertThat(attachedPolicy).isEqualTo("FullAccessPolicyS3");

        //Verify Group Policy: ReadAccessGroupS3
        request = ListAttachedGroupPoliciesRequest.builder()
                .groupName("ReadAccessGroupS3")
                .build();
        response = iam.listAttachedGroupPolicies(request);
        attachedPolicy = response.attachedPolicies().get(0).policyName();
        System.out.println("Policy Attached to Group - ReadAccessGroupS3 is: " + attachedPolicy);
        softAssertions.assertThat(attachedPolicy).isEqualTo("ReadAccessPolicyS3");
    }
}
