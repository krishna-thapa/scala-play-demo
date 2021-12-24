#!/bin/bash

set -e

json_path=$1
run_check=$2

if $run_check; then

  echo "Decided to run the sbt dependency checks on the project"
  printf "Checking JSON report: \n\n"

  if [ -f "$json_path" ]; then
    declare -a vulnerabilities
    readarray -t vulnerabilities < <( jq -c '.dependencies[] | select(.vulnerabilities | length > 0) | {
      filename: .fileName,
      vulnerabilities: {
        name: ([.vulnerabilities[].name] | join(", ")),
        severity: ([.vulnerabilities[].severity] | join(", ")),
        description: ([.vulnerabilities[].description] | join(", ")),
        maxCvssv3Score: (.vulnerabilities | max_by(.cvssv3.baseScore) | .cvssv3.baseScore )
      }}' "$json_path")
    if [ ${#vulnerabilities[@]} -gt 0 ]; then
        printf "Found %d vulnerabilities!\n\n" ${#vulnerabilities[@]}

        projectInfo=$(eval jq -r '.projectInfo' "$json_path")
        printf "Project Info: $projectInfo\n\n"
        i=1
        for vulnerability in "${vulnerabilities[@]}"; do
          printf "%s\n\n" "$i. $vulnerability"
          ((i=i+1))
        done
        echo "Continuing the github action build."
    else
      echo "No vulnerabilities found."
    fi
  else
    echo "Error: JSON report path '${json_path}' not found"
    exit 1
  fi
  replaceHtml="html"
  html_path=${json_path//json/$replaceHtml}

  if [ -f "$html_path" ]; then
    echo "Opening HTML page in web browse."
    xdg-open $html_path
    exit 1
  fi
else
  echo "Decided not to run the dependency checks!"
fi