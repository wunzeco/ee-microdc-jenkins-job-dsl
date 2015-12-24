export PYTHONUNBUFFERED=1
export ANSIBLE_FORCE_COLOR=true

EXTRA_VARS="bukt_app_name=${APP_NAME} bukt_app_version=${APP_VERSION} dockerize_task=container_run dockerize_host_bind_port=$APP_PORT bukt_app_port=$APP_PORT" 
 
cd $WORKSPACE/ansible/
RAX_CREDS_FILE=~/.rax-creds ansible-playbook -i inventory/rax.py products/ci/docker_ci.yml -e "$EXTRA_VARS" \
            -u ubuntu --private-key ~/.ssh/ubuntu --vault-password-file ~/.bukt-vault.pass 
