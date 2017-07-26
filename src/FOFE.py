from fofe_ner_wrapper import fofe_ner_wrapper
import sys, logging, argparse

logger = logging.getLogger(__name__)

class FOFE():
    def __init__(self):
        inference, score = annotator.annotate(text, isDevMode=True)
        logger.info("inference: " + str(inference))

    def tag(self):
        input_filepath = 'libs\data.txt'
        index = input_filepath.find(".txt")
        output_filepath = input_filepath[:index] + "-fofe" + input_filepath[index:] #filename.txt -> filename-fofe.txt
        with open(input_filepath, 'r') as inputfile:
            with open(output_filepath, 'w') as outputfile:
                for line in inputfile:
                    line = line.replace("\n", " | added\n")
                    outputfile.write(line)

if __name__ == "__main__":
    logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.INFO)

    parser = argparse.ArgumentParser()
    parser.add_argument('model1st', type=str, help='basename of model trained for 1st pass')
    parser.add_argument('vocab1', type=str, help='case-insensitive word-vector for {eng,spa} or word-vector for cmn')
    parser.add_argument('vocab2', type=str, help='case-sensitive word-vector for {eng,spa} or char-vector for cmn')
    parser.add_argument('coreNLP_path', type=str, help='path to the Stanford CoreNLP folder.')
    parser.add_argument('coreNLP_port', type=str, help='set the localhost port to coreNLP_port.')
    parser.add_argument('--model2nd', type=str, default=None, help='basename of model trained for 2nd pass')
    parser.add_argument('--KBP', action='store_true', default=False)
    parser.add_argument('--gazetteer', type=str, default=None)
    parser.add_argument('--port', type=int, default=20541)
    args = parser.parse_args()

    if args.KBP:
        cls2ner = ['PER-NAME', 'ORG-NAME', 'GPE-NAME', 'LOC-NAME', 'FAC-NAME', 'PER-NOMINAL', 'ORG-NOMINAL', 'GPE-NOMINAL',
                   'LOC-NOMINAL', 'FAC-NOMINAL']

    annotator = fofe_ner_wrapper(args)

    tagger = FOFE()
    tagger.tag()