( cat triviaQA-qa-TagMe-ToFreeBase-MATCHED1.txt | awk -F "|" '{print $1 "|" $2 "|" $3 "|" $4 "|" " null " "|" $5 "|" $6 "|" $7}' ; cat triviaQA-qa-TagMe-ToFreeBase-MATCHED2.txt | cut -d'|' -f1-4,6-9 ) | sort -t'|' -k8 > triviaQA-qa-ready4MySQL.txt

