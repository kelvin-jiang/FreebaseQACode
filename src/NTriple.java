public class NTriple {
    private String subject; //saved in form subject
    private String subjectID; //saved in form subjectID
    private String predicate; //saved in form <http://rdf.freebase.com/ns/predicate>
    private String objectID; //saved in form <http://rdf.freebase.com/ns/objectID>
    private String object; //saved in form object

    public NTriple(String s, String sID, String p, String oID, String o) {
        subject = s;
        subjectID = sID;
        predicate = p;
        objectID = oID;
        object = o;
    }

    public String getSubject() {
        return subject;
    }

    public String getSubjectID() {
        return subjectID;
    }

    public String getFormattedPredicate() {
        return predicate.substring(28, predicate.length() - 1); //extracts predicate from URL
    }

    public String getFormattedObjectID() {
        return objectID.substring(28, objectID.length() - 1); //extracts freebase ID from URL
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

    public String toString() {
        return String.format("%s     %s     %s     %s     %s",
                getSubject(), getSubjectID(), getFormattedPredicate(), getFormattedObjectID(), getObject());
    }
}
