#!/bin/bash

# Split the large file into smaller ones according to line number
source_file_path=$1
source_file_name="${source_file_path%.*}"
source_file_extension="${source_file_path##*.}"
line_number=$2

split -l "$line_number" -d "$source_file_path" "$source_file_name" && \
for file in $source_file_name*; do
   # Exclude original file
    if [ "$file" != "$source_file_path" ]; then
        mv "$file" "$file.$source_file_extension"
    fi
done
