/*
    service/component build job
*/
svc = 'helloworld'
job(svc) {
    scm {
        git {
            remote {
                github("wunzeco/ee-microdc-${svc}")
            }
            branch('master')
        }
    }
    triggers {
        scm('H/2 * * * *')
    }
    steps {
        shell("./gradlew clean build createDockerfile distTarGz")
    }
    publishers {
        archiveJunit '**/test-results/*.xml'
    }
}

