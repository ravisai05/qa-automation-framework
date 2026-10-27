// Declarative pipeline for Linux or Windows agents. Agent needs JDK 17, Maven, Python 3 and the chosen browser.
// Tool names must match Manage Jenkins > Tools; credential ids must exist in Jenkins credentials.
pipeline {
    agent any

    tools {
        jdk 'jdk17'
        maven 'maven3'
    }

    parameters {
        choice(name: 'BROWSER', choices: ['chrome', 'firefox', 'edge'], description: 'Browser for @ui scenarios')
        string(name: 'FEATURE_TO_RUN', defaultValue: '+regression',
               description: 'Comma-separated tags, OR semantics, e.g. +TC-UI-001,+TC-API-002 or +smoke')
        string(name: 'THREAD_COUNT', defaultValue: '3', description: 'Parallel scenarios (1-16, invalid -> 2)')
        choice(name: 'ENV', choices: ['qa', 'staging'], description: 'Selects config-<env>.properties')
        booleanParam(name: 'IS_RETRY', defaultValue: false, description: 'Retry normal failures once (crashes are always retried)')
    }

    options {
        timestamps()
        timeout(time: 60, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '20'))
        disableConcurrentBuilds()
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Run tests') {
            steps {
                withCredentials([
                        string(credentialsId: 'ui-user-password', variable: 'UI_USER_PASSWORD'),
                        usernamePassword(credentialsId: 'api-admin',
                                usernameVariable: 'API_ADMIN_USERNAME', passwordVariable: 'API_ADMIN_PASSWORD')
                ]) {
                    // Single-quoted: the SHELL expands the parameters (Jenkins exposes them as env vars),
                    // which avoids Groovy string interpolation of user input and of secrets.
                    script {
                        if (isUnix()) {
                            sh '''
                                mvn -B -ntp clean test \
                                  -DtestFailureIgnore=true \
                                  "-Dbrowser=${BROWSER}" -DisHeadless=Y \
                                  "-DthreadCount=${THREAD_COUNT}" \
                                  "-Denv=${ENV}" "-DisRetry=${IS_RETRY}" \
                                  "-DfeatureToRun=${FEATURE_TO_RUN}"
                            '''
                        } else {
                            // Windows agent: cmd expands %VAR%
                            bat 'mvn -B -ntp clean test -DtestFailureIgnore=true "-Dbrowser=%BROWSER%" -DisHeadless=Y "-DthreadCount=%THREAD_COUNT%" "-Denv=%ENV%" "-DisRetry=%IS_RETRY%" "-DfeatureToRun=%FEATURE_TO_RUN%"'
                        }
                    }
                }
            }
        }

        stage('Quality gate') {
            steps {
                // Fails the build when cucumber.json is missing/empty/[] or any scenario finally failed
                script {
                    if (isUnix()) {
                        sh 'python3 scripts/verify_results.py'
                    } else {
                        bat 'python scripts\\verify_results.py'
                    }
                }
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
            archiveArtifacts allowEmptyArchive: true, artifacts:
                    'target/cucumber-reports/**, target/cucumber-html-report/**, target/extent-report/**, ' +
                    'target/logs/**, target/rerun.txt, target/failure-summary.csv'
            // "HTML Publisher" plugin
            publishHTML(target: [
                    reportDir            : 'target/extent-report',
                    reportFiles          : 'SparkReport.html',
                    reportName           : 'Extent Report',
                    keepAll              : true,
                    alwaysLinkToLastBuild: true,
                    allowMissing         : true
            ])
        }
    }
}
