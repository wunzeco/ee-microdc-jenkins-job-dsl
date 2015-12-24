def docker_jobs = [ 
                    'image-build':       'docker-image-build', 
                    'container-run':     'docker-container-run',
                    'component-stack-compose':   'docker-component-stack-compose',
                    'component-stack-teardown':  'docker-component-stack-teardown' 
                    ]

project = 'ansible'

/*
    docker-container-run job
*/
job(docker_jobs['container-run']) {
    parameters {
        stringParam('APP_NAME',     defaultValue = '', description = 'application name')
        stringParam('APP_VERSION',  defaultValue = '', description = 'application version')
        stringParam('APP_PORT',     defaultValue = '', description = 'application port')
    }
    scm {
        git {
            remote {
                github("EqualExperts/bukt-${project}", 'ssh')
                credentials("bukt-${project}-git-creds-id")
            }
        }
    }
    steps {
        shell(readFileFromWorkspace("jenkins-job-dsl/jobs/docker/${docker_jobs['container-run']}.sh"))
    }
    wrappers {
        colorizeOutput()
    }
}


/*
    docker-image-build job
*/
job(docker_jobs['image-build']) {
    parameters {
        stringParam('APP_NAME',     defaultValue = '', description = 'application name')
        stringParam('APP_VERSION',  defaultValue = '', description = 'application version')
        stringParam('APP_PORT',     defaultValue = '', description = 'application port')
    }
    scm {
        git {
            remote {
                github("EqualExperts/bukt-${project}", 'ssh')
                credentials("bukt-${project}-git-creds-id")
            }
        }
    }
    steps {
        shell(readFileFromWorkspace("jenkins-job-dsl/jobs/docker/${docker_jobs['image-build']}.sh"))
    }
    wrappers {
        colorizeOutput()
    }
    publishers {
        downstreamParameterized {
            trigger('docker-container-run') {
                condition('SUCCESS')
                parameters {
                    currentBuild()
                }
            }
        }
    }
}


/*
    docker-component-stack-* job
*/
job(docker_jobs['component-stack-compose']) {
    parameters {
        stringParam('STACK',      defaultValue = '', description = 'stack name corresponds to name of product dir in ansible')
        stringParam('ENVIRONMENT',  defaultValue = '', description = 'app stack environment')
        stringParam('ANSIBLE_EXTRA_VARS',  defaultValue = '', description = '[Optional] ansible-playbook extra variables')
    }
    scm {
        git {
            remote {
                github("EqualExperts/bukt-${project}", 'ssh')
                credentials("bukt-${project}-git-creds-id")
            }
        }
    }
    steps {
        environmentVariables {
            envs('DOCKERIZE_TASK': 'component_stack_compose')
        }
        shell(readFileFromWorkspace("jenkins-job-dsl/jobs/docker/${docker_jobs['component-stack-compose']}.sh"))
    }
    wrappers {
        colorizeOutput()
        buildName('#${BUILD_NUMBER}-${ENV,var="STACK"}-${ENV,var="ENVIRONMENT"}')
    }
}

job(docker_jobs['component-stack-teardown']) {
    parameters {
        stringParam('STACK',      defaultValue = '', description = 'stack name corresponds to name of product dir in ansible')
        stringParam('ENVIRONMENT',  defaultValue = '', description = 'app stack environment')
        stringParam('ANSIBLE_EXTRA_VARS',  defaultValue = '', description = '[Optional] ansible-playbook extra variables')
    }
    scm {
        git {
            remote {
                github("EqualExperts/bukt-${project}", 'ssh')
                credentials("bukt-${project}-git-creds-id")
            }
        }
    }
    steps {
        environmentVariables {
            envs('DOCKERIZE_TASK': 'component_stack_teardown')
        }
        shell(readFileFromWorkspace("jenkins-job-dsl/jobs/docker/${docker_jobs['component-stack-compose']}.sh"))
    }
    wrappers {
        colorizeOutput()
        buildName('#${BUILD_NUMBER}-${ENV,var="STACK"}-${ENV,var="ENVIRONMENT"}')
    }
}

/*
    <env>-docker-component-stack-compose - a wrapper job that triggers docker-component-stack-compose job
*/
bukt_env = 'int'
upstream_service_jobs = [   
        'accounts-service', 
        'agent-portal', 
        'collections-service',
        'consumer-portal', 
        'notifications-service',
        'timeline-service'
        ]
job("${bukt_env}-${docker_jobs['component-stack-compose']}") {
    //triggers {
    //    upstream("${upstream_service_jobs.join('-dockerize, ')}-dockerize", 'SUCCESS')
    //}
    steps {
        downstreamParameterized {
            trigger(docker_jobs['component-stack-compose']) {
                block {
                    buildStepFailure('FAILURE')
                    failure('FAILURE')
                    unstable('UNSTABLE')
                }
                parameters {
                    predefinedProp('STACK', 'collections')
                    predefinedProp('ENVIRONMENT', 'int')
                }
            }
        }
    }
}

/*
    <env>-docker-component-stack-teardown - a wrapper job that triggers docker-component-stack-teardown job
*/
job("${bukt_env}-${docker_jobs['component-stack-teardown']}") {
    steps {
        downstreamParameterized {
            trigger(docker_jobs['component-stack-teardown']) {
                block {
                    buildStepFailure('FAILURE')
                    failure('FAILURE')
                    unstable('UNSTABLE')
                }
                parameters {
                    predefinedProp('STACK', 'collections')
                    predefinedProp('ENVIRONMENT', 'int')
                }
            }
        }
    }
}

/*
    e2e-integration-test - end to end integration test job 
*/
job('e2e-integration-test') {
    description("end to end integration test job")
    scm {
        git {
            remote {
                github("EqualExperts/bukt-consumer-portal", 'ssh')
                credentials("bukt-consumer-portal-git-creds-id")
            }
        }
    }
    steps {
        // run integration tests
        shell(readFileFromWorkspace("jenkins-job-dsl/jobs/services/e2e-integration-test-main.sh"))
    }
    wrappers {
        xvfb('Xvfb') { //ToDo: Ensure you configure an Xvfb installation on Jenkins preferably via automation
            screen('1920x1080x24')
        }
    }
    publishers {
        archiveJunit '**/test-results/*.xml'
    }
}

/*
    int-env-deploy-test - docker-compose component stack and integration test wrapper job
*/
job('int-env-deploy-test') {
    description("docker-compose component stack and integration test wrapper job")
    triggers {
        upstream("${upstream_service_jobs.join('-dockerize, ')}-dockerize", 'SUCCESS')
    }
    steps {
        // teardown stack
        downstreamParameterized {
            trigger("${bukt_env}-${docker_jobs['component-stack-teardown']}") {
                block {
                    buildStepFailure('FAILURE')
                    failure('FAILURE')
                    unstable('UNSTABLE')
                }
            }
        }
        // compose stack
        downstreamParameterized {
            trigger("${bukt_env}-${docker_jobs['component-stack-compose']}") {
                block {
                    buildStepFailure('FAILURE')
                    failure('FAILURE')
                    unstable('UNSTABLE')
                }
            }
        }
        // run integration tests
        downstreamParameterized {
            trigger("e2e-integration-test") {
                block {
                    buildStepFailure('FAILURE')
                    failure('FAILURE')
                    unstable('UNSTABLE')
                }
            }
        }
    }
}
