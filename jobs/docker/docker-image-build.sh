export PYTHONUNBUFFERED=1
export ANSIBLE_FORCE_COLOR=true

EXTRA_VARS="bukt_app_name=${APP_NAME} bukt_app_version=${APP_VERSION} dockerize_task=image_build" 

cd $WORKSPACE/ansible/
RAX_CREDS_FILE=~/.rax-creds ansible-playbook -i inventory/rax.py products/ci/docker_ci.yml -e "$EXTRA_VARS" \
        -u ubuntu --private-key ~/.ssh/ubuntu --vault-password-file ~/.bukt-vault.pass 
