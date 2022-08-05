package eclub.com.cmsnuxeo.dto;

public enum EApplicationType {
    NONE, Onboarding, Expedient;
    //The problem with this approach is that it's very easy to accidentally break the ordering by adding a new enum
    // value in the middle. If you were only using the values at runtime, then it wouldn't matter,
    // but it will be problem if they must be kept in sync with an external database
    private static EApplicationType[] values = EApplicationType.values();

    public static EApplicationType getApplicationType(int i) {
        return values[i - 1];
    }
}

