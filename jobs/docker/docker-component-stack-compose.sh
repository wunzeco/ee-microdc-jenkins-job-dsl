export PYTHONUNBUFFERED=1
export ANSIBLE_FORCE_COLOR=true

PRODUCT=${STACK}
EXTRA_VARS="product=${PRODUCT} bukt_env=${ENVIRONMENT} dockerize_task=${DOCKERIZE_TASK} ${ANSIBLE_EXTRA_VARS}" 

cd $WORKSPACE/ansible/
RAX_CREDS_FILE=~/.rax-creds ansible-playbook -i inventory/rax.py products/${PRODUCT}/product.yml -e "$EXTRA_VARS" \
            -u ubuntu --private-key ~/.ssh/ubuntu --vault-password-file ~/.bukt-vault.pass 
