package com.pars.financial.dto;

public class UserStatistics {
    private long totalUsers;
    private long activeUsers;
    private long inactiveUsers;
    private long usersWithApiKeys;
    private long apiKeyCapableUsers;

    public UserStatistics() {}

    public UserStatistics(long totalUsers, long activeUsers, long inactiveUsers, 
                         long usersWithApiKeys, long apiKeyCapableUsers) {
        this.totalUsers = totalUsers;
        this.activeUsers = activeUsers;
        this.inactiveUsers = inactiveUsers;
        this.usersWithApiKeys = usersWithApiKeys;
        this.apiKeyCapableUsers = apiKeyCapableUsers;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(long activeUsers) {
        this.activeUsers = activeUsers;
    }

    public long getInactiveUsers() {
        return inactiveUsers;
    }

    public void setInactiveUsers(long inactiveUsers) {
        this.inactiveUsers = inactiveUsers;
    }

    public long getUsersWithApiKeys() {
        return usersWithApiKeys;
    }

    public void setUsersWithApiKeys(long usersWithApiKeys) {
        this.usersWithApiKeys = usersWithApiKeys;
    }

    public long getApiKeyCapableUsers() {
        return apiKeyCapableUsers;
    }

    public void setApiKeyCapableUsers(long apiKeyCapableUsers) {
        this.apiKeyCapableUsers = apiKeyCapableUsers;
    }
}
