import sys, json, requests

url = 'http://www.eecs.yorku.ca/~nana/info.php'
data = {
    'mode' : 'dev',
    'lang' : 'English'
}
input_filepath = sys.argv[1]
index = input_filepath.find(".txt")
output_filepath = input_filepath[:index] + "-FOFE" + input_filepath[index:] #filename.txt -> filename-fofe.txt

with open(input_filepath, 'r') as inputfile:
    with open(output_filepath, 'w') as outputfile:
        for line in inputfile:
            data['text'] = line.split(" | ")[0]  # set text in data to question to be tagged
            r = requests.post(url, data=data)
            rawjson = json.loads(r.text)

            indicators = ['first_pass_hidden', 'first_pass_shown']
            entities = []
            tags = []
            for indicator in indicators:
                for i in range(0, len(rawjson[indicator])):
                    index = '%s' % str(i)  # formats index in 'index' form
                    entities = rawjson[indicator][index]['entities']
                    for entity in entities:
                        entity.append(rawjson[indicator][index]['text'])
                        if len(entity) == 5 and "NOM" not in entity[1]:
                            tags.append(entity)

            entities = []
            for tag in tags:
                #entity = []
                entity = tag[4][tag[2][0][0]:tag[2][0][1]]
                #entity.append(tag[3])
                entities.append(entity)

            # TODO: need to search entity in wikipedia and append that
            extension = ""
            for entity in entities:
                extension = extension + " | " + entity + " | " + entity
            line = line.rstrip() + extension
            print(line)
            outputfile.write(line  + "\n")
print("FOFE TAGGING COMPLETE")
