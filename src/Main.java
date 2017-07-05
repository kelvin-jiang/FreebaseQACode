
public class Main {
    private static final int TEST_INDEX = 237;

    public static void main(String args[]) {
        String FILEPATH = args[0];

        RetrieveQA retrieval = new RetrieveQA(FILEPATH);
        //int size = retrieval.getSize(); unused
        String[] questions = retrieval.getQuestions();
        String[] answers = retrieval.getAnswers();

        System.out.println(questions[TEST_INDEX]);

        TagMe tagger = new TagMe(questions[TEST_INDEX]);
        String[] questionTags = tagger.retrieveEntities();

        for (String questionTag : questionTags) {
            System.out.println(questionTag);
        }
    }
}
