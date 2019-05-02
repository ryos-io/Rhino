#!/bin/bash


declare -r RESULTS_DIR=/opt/gatling/results/${TEST_IDENTIFIER}

download_from_instance()
{
  local dns=${1}
  p_ln "Collecting from ${dns} ..."
  local dir=$(ssh -q -o "StrictHostKeyChecking no" -i ${PEM} ec2-user@${dns} "ls -1td /ecs/results/${TEST_IDENTIFIER}* 2> /dev/null")
  if [[ ! -z ${dir} ]]; then
    scp -r -o StrictHostKeyChecking=no -i $PEM ec2-user@$dns:${dir} ${RESULTS_DIR}/
    p_status "OK" "Download completed."
    return 0
  fi
  return 1
}


download_reports_by_checking_all_instances()
{
  local instances=$(aws ecs list-container-instances --cluster ${CLUSTER} --region ${REGION} | jq -r '.containerInstanceArns | join(" ")')
  local instance_id=$(aws ecs describe-container-instances --cluster ${CLUSTER} --region ${REGION} --container-instances ${instances} | jq -r '[.containerInstances[].ec2InstanceId] | join(" ")')
  local ips=$(aws ec2 describe-instances --region ${REGION} --query 'Reservations[*].Instances[*].PublicDnsName' --instance-ids ${instance_id} | jq -r '[.[] | .[]] | join(" ")')
  for ip in ${ips}; do
    download_from_instance $ip &
  done
  wait

  local counter=$(ls ${RESULTS_DIR} | wc -l)
  if [[ ${counter} -eq 0 ]]; then
    p_err "There's no simulation result found on ${#ips[@]} instances. Exiting..."
  fi
  echo "Total $counter results."
  return 0
}

#-----------------------------------------------------------------------
# $ rhino report
#-----------------------------------------------------------------------
prepare_report()
{
  [ -n "${REPORTING_DIR}" ] && RESULTS_DIR=${REPORTING_DIR}
  p_status "OK" "Setting output directory to: $RESULTS_DIR"

  if [ -d "${RESULTS_DIR}" ]; then
      p_status "OK" "Directory exist. Cleaning the directory."
      # Clean up the directory.
      rm -fR $RESULTS_DIR/* >/dev/null
  else
      p_status "OK" "Directory does not exist. Creating..."
      mkdir -p $RESULTS_DIR
      [ ! $? = 0 ] && p_err "Cannot create output directory: ${RESULTS_DIR}"
  fi
}


report()
{
  echo ""
  prepare_report
  echo ""
  p_ln "Collecting simulation results. Please wait..."
  echo ""

  download_reports_by_checking_all_instances

  echo ""
  echo "Preparing report... Please wait..."
  echo ""
  prepare_report_result
}


prepare_report_result()
{
  gatling.sh -ro ${TEST_IDENTIFIER}
}
