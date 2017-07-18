public class NTriple {
    private String subject;
    private String subjectID;
    private String predicate;
    private String objectID;
    private String object;

    public NTriple(String s, String sID, String p, String oID, String o) {
        subject = s;
        subjectID = sID;
        predicate = p.substring(28, p.length() - 1); //extracts predicate from URL
        objectID = oID.substring(28, oID.length() - 1); //extracts object ID from URL
        object = o;
    }

    public String getSubject() {
        return subject;
    }

    public String getSubjectID() {
        return subjectID;
    }

    public String getPredicate() {
        return predicate;
    }

    public String getObjectID() {
        return objectID;
    }

    public String getObject() {
        return object;
    }

    public void setSubject(String s) {
        subject = s;
    }

    public void setObject(String o) {
        object = o;
    }

    @Override
    public String toString() {
        return String.format("%s     %s     %s     %s     %s", subject, subjectID, predicate, objectID, object);
    }

    public String toReverseString() {
        return String.format("%s     %s     %s     %s     %s", object, objectID, predicate, subjectID, subject);
    }
}
