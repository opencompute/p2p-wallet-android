#!/bin/bash

branch_name="$GITHUB_HEAD_REF"
cut_branch_name=$( echo ${branch_name##*/} )
IFS='-'
read -r -a array <<< "$cut_branch_name"
jira_ticket_message="https://p2pvalidator.atlassian.net/browse/${array[0]}-${array[1]}"

slack_ktlint_message="*The process of creating a new assembly is finished (100%)*:
• *Jira Ticket* - $jira_ticket_message
• *Upload to Firebase* -:white_check_mark:
• *Upload to Slack* - :white_check_mark:"
curl \
-F token="$1" \
-F channel="$2" \
-F ts="$3" \
-F text="${slack_ktlint_message}" \
https://slack.com/api/chat.update