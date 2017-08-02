import io, json, re, requests, sys

def matchwiki(tag, count):
    wikiURL = 'https://en.wikipedia.org/w/index.php?fulltext=1&search=' + tag
    results = []

    r = requests.get(wikiURL)
    rawhtml = r.text
    start = rawhtml.find('<ul class=\'mw-search-results\'>')
    end = rawhtml.find('</ul>', start)
    rawresults = rawhtml[start:end]
    for iteration in re.finditer('title="', rawresults):
        index = rawresults.find('"', iteration.end())
        title = rawresults[iteration.end():index]
        results.append(title.lower().strip())
    return results[0:count]


fofeURL = 'http://www.eecs.yorku.ca/~nana/info.php'
data = {
    'mode' : 'dev',
    'lang' : 'English'
}
input_filepath = sys.argv[1]
index = input_filepath.find(".txt")
output_filepath = input_filepath[:index] + "-FOFE" + input_filepath[index:] #filename.txt -> filename-fofe.txt

with io.open(input_filepath, 'r', encoding='utf-8') as inputfile:
    with io.open(output_filepath, 'w', encoding='utf-8') as outputfile:
        for line in inputfile:
            # queries the website
            data['text'] = line.split(" | ")[0]  # set text in data to the question to be tagged
            r = requests.post(fofeURL, data=data)
            try:
                rawjson = json.loads(r.content, strict=False)
            except json.decoder.JSONDecodeError:  # could not solve the error caused by '%' character in question, instead the question is skipped
                outputfile.write(line)
                print(line)
                continue

            # processes outputted json from website
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
                entity = tag[4][tag[2][0][0]:tag[2][0][1]]
                entities.append(entity)

            extension = ""
            for entity in entities:
                extension = extension + " | " + matchwiki(entity, 1)[0] + " | " + entity
            line = line.rstrip() + extension
            print(line)
            outputfile.write(line  + "\n")
print("FOFE TAGGING COMPLETE")
