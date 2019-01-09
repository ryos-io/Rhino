#!/bin/bash


#-----------------------------------------------------------------------
# $ rhino run
#-----------------------------------------------------------------------
remove_old_report()
{
	local dns=${1}
  ssh -q -o StrictHostKeyChecking=no -i $PEM ec2-user@${dns} "sudo rm -rf /ecs/results/${TEST_IDENTIFIER}*"  2>&1
  p_status "OK" "Cleaning up on $dns completed."
}


prepare_run()
{
  check_args_for_create_definition

  p_ln "Preparing the load test. Please wait..."
  echo ""

  local instances=$(aws ecs list-container-instances --cluster ${CLUSTER} --region ${REGION} | jq -r '.containerInstanceArns | join(" ")')
  local instance_id=$(aws ecs describe-container-instances --cluster ${CLUSTER} --region ${REGION} --container-instances ${instances} | jq -r '[.containerInstances[].ec2InstanceId] | join(" ")')
  local ips=$(aws ec2 describe-instances --region ${REGION} --query 'Reservations[*].Instances[*].PublicDnsName' --instance-ids ${instance_id} | jq -r '[.[] | .[]] | join(" ")')

  for ip in ${ips}; do
    remove_old_report $ip &
  done
  wait

  p_status "OK" "Cleaning up the instances completed."
  echo ""
  echo "Updating load-test metadata. Please wait..."

  create_definition
}


run()
{
  prepare_run

  [ -n "${REV}" ] && REV=":${REV}"

  local result=`aws ecs run-task --task-definition ${RHINO_FAMILY}${REV} --cluster ${CLUSTER} --region ${REGION} --count ${COUNT} 2>&1`
  if [ $? -eq 0 ] && [[ ! $result =~ .*error\ occurred.* ]]; then
    p_header "Running scenario: ${RHINO_FAMILY}${REV} in region: ${REGION} on ${COUNT} nodes."
    taskarn=(`echo $result | jq '.' | grep "taskArn" | cut -d"\"" -f  4`)
    status=(`echo $result  | jq '.' | grep "lastStatus" | cut -d"\"" -f  4`)

    for i in ${!taskarn[@]};
    do
      if [ "$((i%2))" = "0" ]; then
        echo "* Created Load-Test task with ARN : ${taskarn[$i]} and status: ${status[$i]}"
      fi
    done

    # if command -v terminal-notifier; then
    #     # aws ecs wait tasks-running --region=${REGION} --cluster ${CLUSTER} --tasks ${taskarn}
    #     echo "would like to use terminal-notifier"
    # fi

    if [ $VERBOSE == "true" ];
    then
      echo ""
      echo "-v verbose mode enabled:"
      echo ""
      echo $result | jq '.'
    fi

  else
    p_err "${result}"
  fi

  echo ""
  echo "Done."
}
