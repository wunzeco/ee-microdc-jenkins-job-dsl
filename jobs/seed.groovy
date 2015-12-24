// If you want, you can define your seed job in the DSL and create it via the REST API.
// See README.md

/* 
    ToDo:
        - rename job dsl files e.g. docker-jobs.groovy etc
        - rename job-dsl-gradle-example to jenkins-job-dsl
        - update README based on job-dsl-gradle-example
*/

/*
    _bukt-job-dsl-seed job definition
*/

job("_dsl-seed") {
    description("DSL seed job")
    scm {
        git {
            remote {
                github('wunzeco/ee-microdc-jenkins-job-dsl')
            }
        }
    }
    triggers {
        scm 'H/2 * * * *'
    }
    steps {
        //gradle 'clean test'
        dsl {
            external('jobs/**/*Jobs.groovy')
            additionalClasspath('src/main/groovy')
        }
        dsl { 
            external("**/*Pipeline.groovy")
        }
    }
    //publishers {
    //    archiveJunit 'build/test-results/**/*.xml'
    //}
}
