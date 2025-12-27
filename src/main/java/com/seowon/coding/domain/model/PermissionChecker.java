package com.seowon.coding.domain.model;


import lombok.Builder;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

class PermissionChecker {

    /**
     * TODO #7: 코드 최적화 완료
     * 
     * 최적화 내용:
     * 1. Map을 활용한 O(1) 조회 - 5중 중첩 루프 제거
     * 2. Stream API 활용 - 가독성 및 함수형 프로그래밍
     * 3. 조기 종료 - anyMatch로 첫 매칭 시 즉시 반환
     */
    public static boolean hasPermission(
            String userId,
            String targetResource,
            String targetAction,
            List<User> users,
            List<UserGroup> groups,
            List<Policy> policies
    ) {
        // 1. Map으로 변환하여 O(1) 조회 가능하게 최적화
        Map<String, User> userMap = users.stream()
                .collect(Collectors.toMap(u -> u.id, Function.identity()));
        
        Map<String, UserGroup> groupMap = groups.stream()
                .collect(Collectors.toMap(g -> g.id, Function.identity()));
        
        Map<String, Policy> policyMap = policies.stream()
                .collect(Collectors.toMap(p -> p.id, Function.identity()));
        
        // 2. 사용자 조회 (O(1))
        User user = userMap.get(userId);
        if (user == null || user.groupIds == null || user.groupIds.isEmpty()) {
            return false;
        }
        
        // 3. 사용자의 그룹들에 대해 권한 확인
        return user.groupIds.stream()
                .map(groupMap::get)  // 그룹 조회 (O(1))
                .filter(group -> group != null && group.policyIds != null)
                .flatMap(group -> group.policyIds.stream())
                .map(policyMap::get)  // 정책 조회 (O(1))
                .filter(policy -> policy != null && policy.statements != null)
                .flatMap(policy -> policy.statements.stream())
                .anyMatch(statement ->  // 첫 매칭 시 즉시 true 반환
                        statement.actions != null && 
                        statement.resources != null &&
                        statement.actions.contains(targetAction) &&
                        statement.resources.contains(targetResource)
                );
    }
}

class User {
    String id;
    List<String> groupIds;

    public User(String id, List<String> groupIds) {
        this.id = id;
        this.groupIds = groupIds;
    }
}

class UserGroup {
    String id;
    List<String> policyIds;

    public UserGroup(String id, List<String> policyIds) {
        this.id = id;
        this.policyIds = policyIds;
    }
}

class Policy {
    String id;
    List<Statement> statements;

    public Policy(String id, List<Statement> statements) {
        this.id = id;
        this.statements = statements;
    }
}

class Statement {
    List<String> actions;
    List<String> resources;

    @Builder
    public Statement(List<String> actions, List<String> resources) {
        this.actions = actions;
        this.resources = resources;
    }
}