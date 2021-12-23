#!/bin/bash

# Source the common script file that will have common functions
source ./scripts/common.sh

# Reference
# https://stackoverflow.com/questions/48133080/how-to-download-a-google-drive-url-via-curl-or-wget/48133859

curl=$(which curl)
if [ -z "curl" ]; then
  die "Please install curl in your machine!"
fi

# Download the CSV file under the data directory
cd ../data

# Get the file id from the google drive share link. File has to be access by public hence no need for API access token
# Test CSV file that has the size of 155 KB
fileid="12pZ7g0W44WYGtVDblwZWbkVOJXyF_dPY"
filename="Quotes-test.csv"

# Full CSV file that has the size of 137.7 MB
#fileid="1I4DNK9Do0BMUY8GQ9CSg2ll1H-8vpLKt"
#filename="Full_Quotes.csv"

# Remove if the file already exist in the data directory
if [ -e ${filename} ]; then
    echo 'File already exists' >&2
    echo 'Deleting the old CSV files' >&2
    rm Quotes-test.csv
fi

echo 'Downloading the CSV file from google drive' >&2
curl -c ./cookie -s -L "https://drive.google.com/uc?export=download&id=${fileid}" > /dev/null
curl -Lb ./cookie "https://drive.google.com/uc?export=download&confirm=`awk '/download/ {print $NF}' ./cookie`&id=${fileid}" -o ${filename}

# Remove the cookie that needed to download for the google drive
echo 'Deleting the cookie file' >&2
rm cookie