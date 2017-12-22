# FreebaseQACode Freebase Search

The code that produced the [FreebaseQA data set](https://github.com/infinitecold/FreebaseQA/) - a set with question-answer pairs and their corresponding Freebase subject-predicate-object triples.

### Stage 1: QA Retrieval

Although question-answer pairs can be obtained through any means, the `QARetrievalMain.java` class is included that extracts QA pairs from JSON files (`Main.java` can also perform this task, which calls on `QARetrieval.java`).

It is crucial that the QA pairs are formatted in a .txt file, with each new line being a question and answer SEPARATED BY '|'. `QARetrievalMain.java` and `QARetrieval.java` automatically format them in this way.

### Stage 2: Entity Recognition

The next task is to "tag" the questions for named-entities. This is done through two services, the first [TagMe](https://tagme.d4science.org/tagme/) and the second [FOFE NER](http://www.eecs.yorku.ca/~nana/ner-home.html).

TagMe is queried using `TagMeMain.java` or through `Main.java`, which calls on `TagMe.java`. FOFE NER is queried using `FOFE.py`. All of these files create a new version of the .txt file with the tags for each question appended to the end of each line.

### Stage 3: Freebase Search

Once the .txt file is prepared, with all QA pairs and associated tags, `Main.java` performs all of the Freebase searching. Each tag is searched in Freebase, looking for a subject that is "connected" to an object that matches the answer to the question. These collected results are outputted to a final .txt file.

### Other

* `buildall.sh` compiles all Java files in a command-line setting

* `run.sh` runs the specified file in a command-line setting

* `run20.sh` runs `Main.java` which creates a new process every 20 questions, in order to bypass memory leak issues when `Main.java` is run for long periods of time