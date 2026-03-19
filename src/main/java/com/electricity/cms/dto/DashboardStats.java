package com.electricity.cms.dto;

public class DashboardStats {

    private long totalComplaints;
    private long resolved;
    private long unresolved;
    private long escalated;
    private long freeTechnicians;
    private long assignedTechnicians;
    private long pendingUnassigned;

    public long getTotalComplaints() {
        return totalComplaints;
    }

    public void setTotalComplaints(long totalComplaints) {
        this.totalComplaints = totalComplaints;
    }

    public long getResolved() {
        return resolved;
    }

    public void setResolved(long resolved) {
        this.resolved = resolved;
    }

    public long getUnresolved() {
        return unresolved;
    }

    public void setUnresolved(long unresolved) {
        this.unresolved = unresolved;
    }

    public long getEscalated() {
        return escalated;
    }

    public void setEscalated(long escalated) {
        this.escalated = escalated;
    }

    public long getFreeTechnicians() {
        return freeTechnicians;
    }

    public void setFreeTechnicians(long freeTechnicians) {
        this.freeTechnicians = freeTechnicians;
    }

    public long getAssignedTechnicians() {
        return assignedTechnicians;
    }

    public void setAssignedTechnicians(long assignedTechnicians) {
        this.assignedTechnicians = assignedTechnicians;
    }

    public long getPendingUnassigned() {
        return pendingUnassigned;
    }

    public void setPendingUnassigned(long pendingUnassigned) {
        this.pendingUnassigned = pendingUnassigned;
    }
}
