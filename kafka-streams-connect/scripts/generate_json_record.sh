#!/bin/bash

uploaded_list_file=$1
s3_files=$(cat "$uploaded_list_file")

for s3_file in $s3_files; do
  bucket_name=$(echo "$s3_file" | awk -F '/' '{print $3}')
  object_path=$(echo "$s3_file" | cut -d/ -f4-)
  printf '{"bucket": "%s", "path": "%s"}\n' "$bucket_name" "$object_path" >> "records.ndjson"
done
