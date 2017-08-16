( cat ../data/OtherQA-MATCHED1.txt | awk -F "|" '{print $1 "|" $2 "|" $3 "|" $4 "|" " null " "|" $5 "|" $6 "|" $7}' ; cat ../data/OtherQA-MATCHED2.txt | cut -d'|' -f1-4,9-12 ) | sort -t'|' -k8 | sed -e 's/ | /|/g' | sed -e 's/ $//g' | sed -e "s/'/\\'/g" | sed -e 's/"/\\"/g' | awk -F "|" '{if ($3 != $6) print "insert into FreebaseQA_Matches (`subject`, `subject_tag`, `subjectID`, `predicate`, `mediator_predicate`, `objectID`, `object`, `question`) values(", "\x27" $1"\x27,", "\x27" $2"\x27,", "\x27" $3"\x27," "\x27" $4"\x27," "\x27" $5"\x27," "\x27" $6"\x27," "\x27" $7"\x27," "\x27" $8"\x27" "); "}' > ../data/FreebaseQA.sql

( cat ../data/TriviaQA-MATCHED1.txt | awk -F "|" '{print $1 "|" $2 "|" $3 "|" $4 "|" " null " "|" $5 "|" $6 "|" $7}' ; cat ../data/TriviaQA-MATCHED2.txt | cut -d'|' -f1-4,9-12 ) | sort -t'|' -k8 | sed -e 's/ | /|/g' | sed -e 's/ $//g' | sed -e "s/'/\\'/g" | sed -e 's/"/\\"/g' | awk -F "|" '{if ($3 != $6) print "insert into FreebaseQA_Matches (`subject`, `subject_tag`, `subjectID`, `predicate`, `mediator_predicate`, `objectID`, `object`, `question`) values(", "\x27" $1"\x27,", "\x27" $2"\x27,", "\x27" $3"\x27," "\x27" $4"\x27," "\x27" $5"\x27," "\x27" $6"\x27," "\x27" $7"\x27," "\x27" $8"\x27" "); "}' >> ../data/FreebaseQA.sql




