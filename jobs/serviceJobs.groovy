services = [
    "accounts-service"      : "3030",
    "agent-portal"          : "7070",
    "collections-service"   : "9090",
    "consumer-portal"       : "2020",
    "notifications-service" : "5050",
    "timeline-service"      : "6060"
    ]

docker_jobs = [ 
    "image-build": "docker-image-build", 
    "container-run": "docker-container-run" 
    ]

services.each { svc, svcPort ->
    def dockerWrapperJob = "${svc}-dockerize"

    /*
        service/component build job
    */
    println "svc-job = ${svc}"
    job(svc) {
        scm {
            git {
                remote {
                    github('wunzeco/ee-microdc-helloworld')
                }
                branch('master')
            }
        }
        triggers {
            scm('H/2 * * * *')
        }
        steps {
            environmentVariables {
                envs(APP_NAME: svc, APP_VERSION: '${BUILD_NUMBER}')
            }
            shell("./gradlew clean build createDockerfile distTarGz")
            downstreamParameterized {
                trigger(dockerWrapperJob) {
                    block {
                        buildStepFailure('FAILURE')
                        failure('FAILURE')
                        unstable('UNSTABLE')
                    }
                    parameters {
                        predefinedProp('APP_NAME', svc)
                        predefinedProp('APP_VERSION', '$BUILD_NUMBER')
                        predefinedProp('APP_PORT', svcPort)
                    }
                }
            }
        }
        wrappers {
            credentialsBinding {
                usernamePassword('NEXUS_LOGIN', 'bukt-nexus-login-creds-id')
            }
        }
        publishers {
            archiveJunit '**/test-results/*.xml'
        }
    }

    
    /*
        svc-dockerize - a wrapper job that triggers docker image & container jobs
    */
    println "svc-dockerize = ${dockerWrapperJob}"
    job(dockerWrapperJob) {
        parameters {
            stringParam('APP_NAME',     defaultValue = '', description = 'application name')
            stringParam('APP_VERSION',  defaultValue = '', description = 'application version')
            stringParam('APP_PORT',     defaultValue = '', description = 'application port')
        }
        steps {
            downstreamParameterized {
                trigger(docker_jobs["image-build"]) {
                    block {
                        buildStepFailure('FAILURE')
                        failure('FAILURE')
                        unstable('UNSTABLE')
                    }
                    parameters {
                        currentBuild()
                    }
                }
                trigger(docker_jobs["container-run"]) {
                    block {
                        buildStepFailure('FAILURE')
                        failure('FAILURE')
                        unstable('UNSTABLE')
                    }
                    parameters {
                        currentBuild()
                    }
                }
            }
        }
    }

}
