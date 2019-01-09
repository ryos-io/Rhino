if [[ -d ${RHINO_HOME}/templates ]]; then
  TEMPLATES=${RHINO_HOME}/templates
else
  TEMPLATES=/usr/local/rhino/templates
fi

check_args_for_create_definition()
{
  KNOWN_SERVICES="ccstorage\naclservice"
  if [[ -z "${RHINO_SERVICE}" ]] || ! echo "$KNOWN_SERVICES" | grep -oq ${RHINO_SERVICE}; then
    p_err "Required parameter --service [${KNOWN_SERVICES/\\n/, }] missing."
  fi
  [ -z "${RHINO_FAMILY}" ] && p_err "Required parameter -s <simulation> missing."
  [ -z "${RHINO_ENV}" ] && p_err "Required parameter -e <env> missing."
}

create_ccstorage_definition()
{
  RHINO_USER_PER_MIN=15
  RHINO_USER_COUNT=100
  RHINO_DURATION=120
  PROJECT_NAME="${RHINO_FAMILY}"
  PROJECT_TAG="latest"
}

create_definition()
{
  check_args_for_create_definition
  RHINO_CPU=2048
  RHINO_MEM=4096
  RHINO_JVM_XMS="512m"
  RHINO_JVM_XMX="2048m"

  eval "create_${RHINO_SERVICE}_definition"

  TEMPLATE=$(eval "echo \""$(cat ${TEMPLATES}/${RHINO_SERVICE}.tmpl | sed 's/\"/\\\"/g')\""")
  task_temp="/var/tmp/${TEST_IDENTIFIER}.json"
  echo $TEMPLATE | jq '.' > $task_temp

  local res=$(aws ecs register-task-definition --cli-input-json "$(cat < ${task_temp})" --region ${REGION})
  if [ $? = 0 ] && [[ ! $res =~ .*error\ occurred.* ]]; then
    local result=(`echo $res | jq '.taskDefinition.revision'`)
    p_status "OK" "Created metadata revision ${result}."
  else
    p_err "$res"
  fi

  rm -f $task_temp
}
