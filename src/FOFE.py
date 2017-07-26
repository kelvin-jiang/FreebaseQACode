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
            data['text'] = line.split(" | ")[0]
            r = requests.post(url, data=data)
            rawjson = json.loads(r.text)
            del rawjson['second_pass']
            tags = []
            for i in rawjson:
                for j in i:
                    for k in j:
                        tags.append(k['entities'])
            print(tags)

            #tags = rawjson["first_pass_hidden"]["0"]["entities"]
            #tags.extend(rawjson["first_pass_shown"]["0"]["entities"])
            #print(tags)

            for tag in tags:
                if 'NOM' in tag[1]:
                    tags.remove(tag)

            print("real: " + str(tags))

            #line = line.replace("\n", " | added\n")
            #outputfile.write(line)

