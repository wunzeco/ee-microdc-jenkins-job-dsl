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
        shell("echo 'Now upload application artifact (${svc}-${BUILD_NUMBER}.tar.gz) to Nexus/S3'")
    }
    publishers {
        archiveJunit '**/test-results/*.xml'
        archiveArtifacts {
            pattern("**/${svc}/build/libs/*.jar")
            pattern('**/${svc}/build/docker/Dockerfile')
            onlyIfSuccessful()
        }
    }
}

/*
job("${svc}-image-build") {
    steps {
        //copy artifact from $svc
        copyArtifacts(svc) {
            includePatterns('*.jar', 'Dockerfile')
            //excludePatterns('test.xml', 'test.properties')
            //targetDirectory('files')
            flatten()
            optional()
            buildSelector {
                latestSuccessful(true)
            }
        }
        // build image
        shell("docker build -t ee/$svc .")
        //run container
    }
}
*/
